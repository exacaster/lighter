package com.exacaster.lighter.application.sessions.processors


import com.fasterxml.jackson.databind.ObjectMapper

class PythonProcessor {

    private final Process process
    private final BufferedWriter writer
    private final BufferedReader reader
    private final ObjectMapper mapper

    PythonProcessor(Process process) {
        this.process = process
        this.mapper = new ObjectMapper()
        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
    }

    Result process(String code) throws IOException {
        writer.write(mapper.writeValueAsString(Map.of("code", code, "type", "execute_request")))
        writer.newLine()
        writer.flush()

        Result line = null
        while (line == null) {
            def strLine = reader.readLine()
            try {
                line = mapper.readValue(strLine, Result.class)
            } catch (Exception e) {
                // ignore
            }
        }
        return line
    }

}
