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
is_yarn = os.environ.get("LIGHTER_YARN_ENABLED") == "true"
logging.basicConfig(stream=sys.stdout,
                    level=logging.FATAL if is_test else logging.INFO)
log = logging.getLogger("session")

def setup_output():
    sys.stdout.flush()
    sys.stderr.flush()
    sys.stdout = io.StringIO()
    sys.stderr = io.StringIO()


class Controller:
    def __init__(self, session_id):
        self.session_id = session_id

    def read(self):
        return []

    def write(self, _id, _result):
        pass


class TestController(Controller):
    def read(self):
        try:
            str_line = sys_stdin.readline()
            line = json.loads(str_line)
            return [line]
        except:
            return []

    def write(self, id, result):
        print(json.dumps(result), file=sys_stdout)
        sys_stdout.flush()


class GatewayController(Controller):
    def __init__(self, session_id):
        super().__init__(session_id)
        from py4j.java_gateway import JavaGateway, GatewayParameters
        port = int(os.environ.get("PY_GATEWAY_PORT"))
        host = os.environ.get("PY_GATEWAY_HOST")
        self.gateway = JavaGateway(gateway_parameters=GatewayParameters(
            address=host, port=port, auto_convert=True))
        self.endpoint = self.gateway.entry_point

    def read(self):
        try:
            return [{"id": stmt.getId(), "code": stmt.getCode()} for stmt in self.endpoint.statementsToProcess(self.session_id)]
        except Exception as e:
            log.exception(e)
            return []

    def write(self, id, result):
        try:
            self.endpoint.handleResponse(self.session_id, id, result)
        except Exception as e:
            log.exception(e)


class CommandHandler:

    def __init__(self, globals) -> None:
        self.globals = globals

    def _error_response(self, error):
        exc_type, exc_value, exc_tb = sys.exc_info()
        return {
            "error": type(error).__name__,
            "message": str(error),
            "traceback": traceback.format_exception(exc_type, exc_value, exc_tb),
        }

    def _exec_then_eval(self, code):
        block = ast.parse(code, mode='exec')

        # assumes last node is an expression
        last = ast.Interactive([block.body.pop()])

        exec(compile(block, '<string>', 'exec'), self.globals)
        exec(compile(last, '<string>', 'single'), self.globals)


    def exec(self, request):
        try:
            code = request["code"]
            self._exec_then_eval(code.rstrip())

            return {"content": {"text/plain": str(sys.stdout.getvalue()).rstrip()}}
        except Exception as e:
            log.exception(e)
            return self._error_response(e)


def init_globals(name):
    if is_test:
        return {}

    from pyspark.sql import SparkSession

    spark_builder = SparkSession \
        .builder \
        .appName(name)

    if is_yarn:
        spark_builder = spark_builder.enableHiveSupport()

    spark = spark_builder.getOrCreate()

    return {"spark": spark}


def main():
    setup_output()
    session_id = os.environ.get("LIGHTER_SESSION_ID")
    log.info(f"Initiating session {session_id}")
    controller = TestController(
        session_id) if is_test else GatewayController(session_id)
    handler = CommandHandler(init_globals(session_id))

    log.info("Starting session loop")
    try:
        while True:
            setup_output()
            for command in controller.read():
                log.info(f"Processing command {command}")
                result = handler.exec(command)
                controller.write(command["id"], result)
                log.info("Response sent")
    except:
        exc_type, exc_value, exc_tb = sys.exc_info()
        log.info(
            f"Error: {traceback.format_exception(exc_type, exc_value, exc_tb)}")
        log.info("Exiting")
        return 1


if __name__ == '__main__':
    sys.exit(main())
