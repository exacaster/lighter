package com.exacaster.lighter.storage.jdbc

import com.exacaster.lighter.log.Log
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static com.exacaster.lighter.test.Factories.newApplication

@MicronautTest
class JdbcLogStorageTest extends Specification {

    @Inject
    JdbcApplicationStorage applicationStorage

    @Inject
    JdbcLogStorage logStorage

    def "works"() {
        given:
        def app = newApplication()
        applicationStorage.saveApplication(app)
        def log = new Log(app.getId(), "log")

        when: "creating"
        def result = logStorage.saveApplicationLog(log)

        then: "returns created"
        result.id == app.getId()
        result.log == log.getLog()

        when: "updating"
        result = logStorage.saveApplicationLog(new Log(log.getId(), "new log"))

        then: "returns updated"
        result.getLog() == "new log"

        when: "fetching"
        result = logStorage.findApplicationLog(log.getId())

        then:
        result.get().getLog() == "new log"
    }
}
