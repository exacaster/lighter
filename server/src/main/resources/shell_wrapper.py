import json
import sys
import io
import ast
import traceback
import os

sys_stdin = sys.stdin
sys_stdout = sys.stdout

is_test = os.environ.get("LIGHTER_TEST") == "true"


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
        print(result, file=sys_stdout)
        sys_stdout.flush()


class GatewayController(Controller):
    def __init__(self, session_id):
        super().__init__(session_id)
        from py4j.java_gateway import JavaGateway
        from py4j.java_collections import MapConverter
        port = os.environ.get("PY_GATEWAY_PORT")
        self.gateway = JavaGateway(python_proxy_port=port)
        self.endpoint = self.gateway.entry_point
        self.map_converter = MapConverter()

    def read(self):
        return [stmt.getCode() for stmt in self.endpoint.statementsToProcess(self.session_id)]

    def write(self, id, result):
        self.endpoint.handleResponse(
            self.session_id, id, self.map_converter.convert(result, self.gateway))


class CommandHandler:

    def __init__(self) -> None:
        self.globals = {}

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
            self._exec_then_eval(code)
            return {"content": {"text/plain": str(sys.stdout.getvalue()).rstrip()}}
        except Exception as e:
            return self._error_response(e)


def main():
    sys.stdout = io.StringIO()
    sys.stderr = io.StringIO()

    session_id = os.environ.get("LIGHTER_SESSION_ID")
    controller = TestController(
        session_id) if is_test else GatewayController(session_id)
    handler = CommandHandler()

    while True:
        for command in controller.read():
            result = handler.exec(command)
            response = json.dumps(result)
            controller.write(command["id"], response)


if __name__ == '__main__':
    sys.exit(main())
