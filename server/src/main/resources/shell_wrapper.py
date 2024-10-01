import json
import sys
import io
import ast
import traceback
import os
import logging

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


def _do_with_retry(attempts, action):
    attempts_left = attempts
    last_exception = None
    while attempts_left:
        try:
            return action()
        except Exception as e:
            last_exception = e
            attempts_left -= 1
    raise last_exception


class Controller:
    def __init__(self, session_id):
        self.session_id = session_id

    def read(self):
        return []

    def write(self, _id, _result):
        pass


class TestController(Controller):
    def read(self):
        def do_read():
            str_line = sys_stdin.readline()
            line = json.loads(str_line)
            return [line]

        return _do_with_retry(2, do_read)

    def write(self, id, result):
        def do_write():
            print(json.dumps(result), file=sys_stdout)
            sys_stdout.flush()

        _do_with_retry(2, do_write)


class GatewayController(Controller):
    def __init__(self, session_id):
        super().__init__(session_id)
        from py4j.java_gateway import JavaGateway, GatewayParameters

        port = int(os.environ.get("PY_GATEWAY_PORT"))
        host = os.environ.get("PY_GATEWAY_HOST")
        self.gateway = JavaGateway(
            gateway_parameters=GatewayParameters(
                address=host, port=port, auto_convert=True
            )
        )
        self.endpoint = self.gateway.entry_point

    def read(self):
        return _do_with_retry(
            3,
            lambda: [
                {"id": stmt.getId(), "code": stmt.getCode()}
                for stmt in self.endpoint.statementsToProcess(self.session_id)
            ],
        )

    def write(self, id, result):
        _do_with_retry(
            3, lambda: self.endpoint.handleResponse(self.session_id, id, result)
        )


def is_url(words: str) -> bool:
    import re

    log.info(f"Checking if {words} is a URL")
    length = len(words.split(" "))
    if length != 1:
        log.error(f"Not a single word: {words} ({length} words)")
        return False
    
    match = re.match(r'^https?://\S+$', words)
    if match:
        log.info(f"Matched: {match.group()}")
        return bool(match)
    else:
        log.error(f"Not matched: {words}")
        return False


class CommandHandler:
    def __init__(self, globals) -> None:
        self.globals = globals

    def _error_response(self, error):
        exc_type, exc_value, exc_tb = sys.exc_info()
        return {
            "content": {"text/plain": str(sys.stdout.getvalue()).rstrip()},
            "error": type(error).__name__,
            "message": str(error),
            "traceback": traceback.format_exception(exc_type, exc_value, exc_tb),
        }

    def _exec_then_eval(self, code):
        block = ast.parse(code, mode="exec")

        # assumes last node is an expression
        last = ast.Interactive([block.body.pop()])

        exec(compile(block, "<string>", "exec"), self.globals)
        exec(compile(last, "<string>", "single"), self.globals)

    def _download_then_exec(self, url):
        temp_dir = self._download_and_extract(url)
        try:
            self._execute_main_file(temp_dir)
        finally:
            temp_dir.cleanup()

    @staticmethod
    def _download_and_extract(url: str):
        import tempfile
        import zipfile
        import requests
        
        temp_dir = tempfile.TemporaryDirectory()
        temp_file_path = os.path.join(temp_dir.name, "export")

        response = requests.get(url)
        with open(temp_file_path, "wb") as f:
            f.write(response.content)

        with zipfile.ZipFile(temp_file_path, "r") as zip_ref:
            zip_ref.extractall(temp_dir.name)

        return temp_dir
    
    @staticmethod
    def _add_to_pythonpath(temp_dir):
        log.info(f"Adding {temp_dir.name} to pythonpath")
        sys.path.append(temp_dir.name)
    
    @staticmethod
    def _remove_from_pythonpath(temp_dir):
        log.info(f"Removing {temp_dir.name} from pythonpath")
        sys.path.remove(temp_dir.name)

    @staticmethod
    def _remove_module(temp_dir):
        modules_to_remove = []
        for name, mod in sys.modules.items():
            try:
                if hasattr(mod, '__spec__') and mod.__spec__ and mod.__spec__.origin and temp_dir.name in mod.__spec__.origin:
                    modules_to_remove.append(name)
            except AttributeError:
                continue
            except TypeError:
                continue
        
        for name in modules_to_remove:
            log.info(f"Unloading {name}")
            del sys.modules[name]

    def _execute_main_file(self, temp_dir):
        self._add_to_pythonpath(temp_dir)
        main_file_path = os.path.join(temp_dir.name, "main.py")
        with open(main_file_path, "r") as f:
            log.info(f"Executing {main_file_path}")
            self._exec_then_eval(f.read())
        self._remove_from_pythonpath(temp_dir)
        self._remove_module(temp_dir)

    
    def _exec_code(self, code):
        if not is_url(code):
            self._exec_then_eval(code)
        else:
            self._download_then_exec(code)

    def exec(self, request):
        try:
            code = request["code"].rstrip()
            if code:
                self._exec_code(code)
                return {"content": {"text/plain": str(sys.stdout.getvalue()).rstrip()}}
            return {"content": {"text/plain": ""}}
        except Exception as e:
            log.exception(e)
            return self._error_response(e)


def init_globals(name):
    if is_test:
        return {}

    from pyspark.sql import SparkSession

    spark = SparkSession.builder.appName(name).getOrCreate()

    return {"spark": spark}


def main():
    setup_output()
    session_id = os.environ.get("LIGHTER_SESSION_ID")
    log.info(f"Initiating session {session_id}")
    controller = (
        TestController(session_id) if is_test else GatewayController(session_id)
    )
    handler = CommandHandler(init_globals(session_id))

    log.info("Starting session loop")
    try:
        while True:
            for command in controller.read():
                setup_output()
                log.debug(f"Processing command {command}")
                result = handler.exec(command)
                controller.write(command["id"], result)
                log.debug("Response sent")
    except:
        exc_type, exc_value, exc_tb = sys.exc_info()
        log.error(f"Error: {traceback.format_exception(exc_type, exc_value, exc_tb)}")
        log.info("Exiting")
        return 1


if __name__ == "__main__":
    sys.exit(main())
