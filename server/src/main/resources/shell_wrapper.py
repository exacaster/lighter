import json
import sys
import io
import ast
import traceback
import os
import logging
import datetime
import decimal
import base64

sys_stdin = sys.stdin
sys_stdout = sys.stdout

is_test = os.environ.get("LIGHTER_TEST") == "true"
logging.basicConfig(stream=sys.stdout,
                    level=logging.FATAL if is_test else logging.INFO)
log = logging.getLogger("session")


# Magic Table
class MagicTable:
    TABLE_TYPES = {
        type(None): lambda x: ('NULL_TYPE', x),
        bool: lambda x: ('BOOLEAN_TYPE', x),
        int: lambda x: ('INT_TYPE', x),
        float: lambda x: ('DOUBLE_TYPE', x),
        str: lambda x: ('STRING_TYPE', str(x)),
        datetime.date: lambda x: ('DATE_TYPE', str(x)),
        datetime.datetime: lambda x: ('TIMESTAMP_TYPE', str(x)),
        decimal.Decimal: lambda x: ('DECIMAL_TYPE', str(x)),
    }

    def __init__(self, data):
        self.data = data

    def _row_iterator(self, row):
        if 'Row' == row.__class__.__name__:
            row = row.asDict()

        if not isinstance(row, (list, tuple, dict)):
            row = [row]

        if isinstance(row, (list, tuple)):
            return enumerate(row)
        return sorted(row.items())

    def _convert_value(self, value):
        converter = MagicTable.TABLE_TYPES.get(
            type(value), MagicTable.TABLE_TYPES[str])
        return converter(value)

    def to_livy_table(self):
        value = self.data
        if not isinstance(value, (list, tuple)):
            value = [value]

        headers = {}
        data = []

        for data_line in value:
            cols = []
            data.append(cols)
            iterator = self._row_iterator(data_line)

            for name, col in iterator:
                col_type, col = self._convert_value(col)

                if name not in headers:
                    headers[name] = {
                        'name': str(name),
                        'type': col_type,
                    }

                cols.append(col)

        headers = [v for k, v in sorted(headers.items())]
        return {
            "headers": headers,
            "data": data,
        }


class MatPlot:
    def __init__(self, data):
        self.data = data

    def to_plot(self):
        fig = self.data.gcf()
        imgdata = io.BytesIO()
        fig.savefig(imgdata, format='png')
        imgdata.seek(0)
        encode = base64.b64encode(imgdata.getvalue()).decode()

        return encode


def setup_output():
    if not is_test:
        sys.stdout.close()
        sys.stderr.close()
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

    def _is_magic(self, line):
        return line.startswith("%")

    def _json(self, key):
        return {"content": {"application/json": self.globals.get(key)}}

    def _table(self, key):
        value = self.globals.get(key)
        table = MagicTable(value)

        return {
            "content": {
                "application/vnd.livy.table.v1+json": table.to_livy_table()
            }
        }

    def _plot(self, key):
        value = self.globals.get(key)
        plot = MatPlot(value)

        return {
            "content": {
                "image/png": plot.to_plot()
            }
        }

    def do_magic(self, line):
        cmd, arg = line[1:].split(' ', 1)
        if cmd == "json":
            return self._json(arg)
        if cmd == "table":
            return self._table(arg)
        if cmd == "matplot":
            return self._plot(arg)

    def exec(self, request):
        try:
            code = request["code"]
            code_lines = code.rstrip().splitlines()

            py_code = "\n".join(
                [line for line in code_lines if not self._is_magic(line)])

            self._exec_then_eval(py_code)

            if code_lines and self._is_magic(code_lines[-1]):
                return self.do_magic(code_lines[-1])

            return {"content": {"text/plain": str(sys.stdout.getvalue()).rstrip()}}
        except Exception as e:
            log.exception(e)
            return self._error_response(e)


def init_globals(name):
    if is_test:
        return {}

    from pyspark.sql import SparkSession

    spark = SparkSession \
        .builder \
        .appName(name) \
        .getOrCreate()

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
