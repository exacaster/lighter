package com.exacaster.lighter.log

import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.test.InMemoryStorage
import spock.lang.Specification
import spock.lang.Subject

class LogServiceTest extends Specification {
    @Subject
    LogService service = new LogService(new InMemoryStorage(), Mock(Backend))

    def "LogService"() {
        given:
        def log = "fooo"

        when: "saving log"
        service.save(new Log("1", log))
        def result = service.fetch("1")

        then: "log saved"
        result.get().getLog() == log
    }
}
