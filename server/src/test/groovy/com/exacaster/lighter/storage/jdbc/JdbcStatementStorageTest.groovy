package com.exacaster.lighter.storage.jdbc


import com.exacaster.lighter.application.sessions.processors.Output
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import jakarta.transaction.Transactional

import static com.exacaster.lighter.test.Factories.newSession
import static com.exacaster.lighter.test.Factories.statement

@MicronautTest
@Transactional
class JdbcStatementStorageTest extends Specification {
    @Inject
    JdbcApplicationStorage applicationStorage

    @Inject
    JdbcStatementStorage statementStorage

    def "works"() {
        given:
        def session = newSession()
        def app = applicationStorage.saveApplication(session)

        when:
        def createdStatement = statementStorage.create(app.id, statement())

        then:
        createdStatement.id != null
        createdStatement.state == "waiting"

        when:
        def statements = statementStorage.findByState(app.id, "waiting")

        then:
        statements.size() == 1

        when:
        statements = statementStorage.findByState(app.id, "error")

        then:
        statements.isEmpty()

        when:
        def updatedStatement = statementStorage.updateState(app.id, createdStatement.id, "available")

        then:
        updatedStatement.isPresent()
        updatedStatement.get().state == "available"

        when:
        statementStorage.update(app.id, createdStatement.id, "error", new Output("error", 1, [:], "", ""))
        statements = statementStorage.findByState(app.id, "error")

        then:
        statements.size() == 1
        statements.get(0).output != null
    }
}
