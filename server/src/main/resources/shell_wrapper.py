import json
import sys
import io
import traceback
import os
import logging
import re
import tempfile
import zipfile
import requests
import shutil
from typing import Callable, Any, List, Dict
from pathlib import Path
from time import sleep
import threading


sys_stdin = sys.stdin
sys_stdout = sys.stdout

is_test = os.environ.get("LIGHTER_TEST") == "true"
log_level = "FATAL" if is_test else os.environ.get("SESSION_LOGLEVEL", "INFO").upper()
logging.basicConfig(stream=sys.stdout, level=log_level)
log = logging.getLogger("session")


def setup_output():
    sys.stdout.flush()
    sys.stderr.flush()
    sys.stdout = io.StringIO()
    sys.stderr = io.StringIO()


def retry(attempts: int, action: Callable[[], Any]) -> Any:
    for _ in range(attempts):
        try:
            return action()
        except Exception as e:
            last_exception = e
    raise last_exception


class Controller:
    def __init__(self, session_id: str):
        self.session_id = session_id

    def read(self) -> List[Dict[str, Any]]:
        return []

    def write(self, _id: str, _result: Dict[str, Any]) -> None:
        pass

    def cancel(self) -> bool:
        return False


class TestController(Controller):
    def read(self) -> List[Dict[str, Any]]:
        return retry(2, lambda: [json.loads(sys_stdin.readline())])

    def write(self, id: str, result: Dict[str, Any]) -> None:
        retry(2, lambda: print(json.dumps(result), file=sys_stdout, flush=True))

    def cancel(self) -> bool:
        return False


class GatewayController(Controller):
    def __init__(self, session_id: str):
        super().__init__(session_id)
        from py4j.java_gateway import JavaGateway, GatewayParameters

        port = int(os.environ.get("PY_GATEWAY_PORT", "0"))
        host = os.environ.get("PY_GATEWAY_HOST", "")
        self.gateway = JavaGateway(
            gateway_parameters=GatewayParameters(
                address=host, port=port, auto_convert=True
            )
        )
        self.endpoint = self.gateway.entry_point

    def read(self) -> List[Dict[str, Any]]:
        return retry(
            3,
            lambda: [
                {"id": stmt.getId(), "code": stmt.getCode()}
                for stmt in self.endpoint.statementsToProcess(self.session_id)
            ],
        )

    def write(self, id: str, result: Dict[str, Any]) -> None:
        retry(3, lambda: self.endpoint.handleResponse(self.session_id, id, result))

    def cancel(self) -> bool:
        return retry(3, lambda: self.endpoint.cancelProcess(self.session_id))


def is_url(words: str) -> bool:
    log.debug(f"Checking if {words} is a URL")
    if len(words.split()) != 1:
        log.debug(f"Not a single word: {words}")
        return False

    match = re.match(r"^https?://\S+$", words)
    if match:
        log.debug(f"Matched: {match.group()}")
        log.info(f"URL matched: {words}")
        return True
    log.debug(f"Not matched: {words}")
    log.info("URL not matched")
    return False


class CommandHandler:
    def __init__(self, globals: Dict[str, Any]):
        self.globals = globals
        self.code_file = "download"

    def _error_response(self, error: Exception) -> Dict[str, Any]:
        exc_type, exc_value, exc_tb = sys.exc_info()
        return {
            "content": {"text/plain": sys.stdout.getvalue().rstrip()},
            "error": type(error).__name__,
            "message": str(error),
            "traceback": traceback.format_exception(exc_type, exc_value, exc_tb),
        }

    def _exec(self, code: str) -> None:
        exec(code, self.globals)

    def _download_then_exec(self, url: str) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            self._download_and_extract(url, temp_dir)
            main_file = Path(temp_dir) / "main.py"
            if main_file.exists():
                self._execute_main_file(temp_dir)
            else:
                code_file = Path(temp_dir) / self.code_file
                with code_file.open("r") as f:
                    self._exec(f.read())

    def _download_and_extract(self, url: str, temp_dir: str) -> None:
        temp_file_path = Path(temp_dir) / self.code_file
        self._download(url, temp_file_path)
        if self._is_zip(temp_file_path):
            self._extract(temp_file_path)

    def _copy_to_worker_module_path(self, temp_dir: str) -> None:
        worker_module_path = Path(temp_dir) / "shared"
        worker_module_path.mkdir(parents=True, exist_ok=True)
        for file in Path(temp_dir).glob("**/*"):
            if file.is_file():
                shutil.copy(file, worker_module_path / file.name)

    def _remove_worker_module_path(self, temp_dir: str) -> None:
        worker_module_path = Path(temp_dir) / "shared"
        if worker_module_path.exists():
            shutil.rmtree(worker_module_path)

    def _download(self, url: str, temp_file_path: Path) -> None:
        response = requests.get(url)
        response.raise_for_status()  # Raise an exception for bad status codes
        temp_file_path.write_bytes(response.content)

    @staticmethod
    def _is_zip(file_path: Path) -> bool:
        return zipfile.is_zipfile(file_path)

    @staticmethod
    def _extract(file_path: Path) -> None:
        with zipfile.ZipFile(file_path, "r") as zip_ref:
            zip_ref.extractall(path=file_path.parent)

    def _execute_main_file(self, temp_dir: str) -> None:
        log.info(f"Inserting {temp_dir} to sys.path")
        log.info(f"Current sys.path: {sys.path}")
        sys.path.insert(0, temp_dir)
        log.info(f"New sys.path: {sys.path}")
        try:
            main_file_path = Path(temp_dir) / "main.py"
            with main_file_path.open("r") as f:
                log.info(f"Executing {main_file_path}")
                self._exec(f.read())
        finally:
            log.info(f"Removing {temp_dir} from sys.path")
            log.info(f"Current sys.path: {sys.path}")
            sys.path.remove(temp_dir)
            log.info(f"New sys.path: {sys.path}")
            remove_modules(temp_dir)

    def _exec_code(self, code: str) -> None:
        if is_url(code):
            self._download_then_exec(code)
        else:
            self._exec(code)

    def exec(self, request: Dict[str, str]) -> Dict[str, Any]:
        try:
            code = request["code"].rstrip()
            if code:
                self._exec_code(code)
                return {"content": {"text/plain": sys.stdout.getvalue().rstrip()}}
            return {"content": {"text/plain": ""}}
        except Exception as e:
            log.exception(e)
            return self._error_response(e)

    def cancel(self) -> None:


def remove_modules(temp_dir: str) -> None:
    modules_to_remove = [
        name
        for name, mod in sys.modules.items()
        if hasattr(mod, "__spec__")
        and mod.__spec__
        and mod.__spec__.origin
        and temp_dir in mod.__spec__.origin
    ]
    log.info(f"Removing {len(modules_to_remove)} modules")
    log.info(f"Modules to remove: {modules_to_remove}")
    for name in modules_to_remove:
        log.info(f"Unloading {name}")
        del sys.modules[name]


def init_globals(name: str) -> Dict[str, Any]:
    if is_test:
        return {}

    from pyspark.sql import SparkSession

    spark = SparkSession.builder.appName(name).getOrCreate()
    return {"spark": spark}


def session_exec(controller: Controller, handler: CommandHandler) -> Unit:
    while True:
        for command in controller.read():
            setup_output()
            log.debug(f"Processing command {command}")
            result = handler.exec(command)
            controller.write(command["id"], result)
            log.debug("Response sent")
        sleep(0.25)


def main() -> int:
    setup_output()
    session_id = os.environ.get("LIGHTER_SESSION_ID", "")
    log.info(f"Initiating session {session_id}")
    controller = (
        TestController(session_id) if is_test else GatewayController(session_id)
    )
    handler = CommandHandler(init_globals(session_id))
    executer = threading.Thread(target=session_exec, args=(controller,handler,))

    log.info("Starting session loop")
    try:
        executer.start()
        while True:
            if controller.cancel():
                log.debug(f"Cancelling")
                executer.terminate()
                executer.start()
            sleep(0.25)
    except Exception:
        log.exception("Error in main loop")
        return 1
    finally:
        log.info("Exiting")
    return 0


if __name__ == "__main__":
    sys.exit(main())
