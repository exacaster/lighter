package com.exacaster.lighter.log


import com.exacaster.lighter.storage.InMemoryStorage
import spock.lang.Specification
import spock.lang.Subject

class LogServiceTest extends Specification {
    @Subject
    LogService service = new LogService(new InMemoryStorage(10, 10))

    def "LogService"() {
        given:
        def log = "fooo"

        when: "saving log"
        service.save(new Log("1", log))
        def result = service.fetch("1")

        then: "log saved"
        result.get().log() == log
    }
}
