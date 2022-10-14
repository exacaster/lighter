package com.exacaster.lighter.backend.local.logger

import spock.lang.Specification

import java.util.logging.Level
import java.util.logging.LogRecord

class LogCollectingHandlerTest extends Specification {

    static NL = System.lineSeparator();

    def "keeps defined number of lines"() {
        given:
        def handler = new LogCollectingHandler(4)

        when:
        8.times {
            def record = new LogRecord(Level.INFO, "message ${it}")
            handler.publish(record)
        }

        then:
        handler.getLogs() == "message 4${NL}message 5${NL}message 6${NL}message 7${NL}"
    }
}
