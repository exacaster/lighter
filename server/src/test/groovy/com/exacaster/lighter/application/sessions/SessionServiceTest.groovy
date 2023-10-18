package com.exacaster.lighter.application.sessions

import com.exacaster.lighter.application.ApplicationInfo
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.sessions.processors.StatementHandler
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.storage.ApplicationStorage
import com.exacaster.lighter.storage.SortOrder
import com.exacaster.lighter.storage.StatementStorage
import com.exacaster.lighter.test.InMemoryStorage
import spock.lang.Specification
import spock.lang.Subject

import static com.exacaster.lighter.test.Factories.*

class SessionServiceTest extends Specification {
    ApplicationStorage storage = new InMemoryStorage()
    Backend backend = Mock()
    StatementHandler statementHandler = Mock()
    StatementStorage statementStorage = Mock()

    @Subject
    SessionService service = new SessionService(storage, statementStorage, backend, statementHandler)

    def "manage sessions"() {
        given:
        def params = submitParams()

        when: "creating session"
        def created = service.createSession(params)

        then: "returns created session"
        created.id !== null

        when: "fetching first sessions"
        def sessions = service.fetch(0, 10)

        then: "returns sessions"
        sessions == [created]

        when: "fetching for more"
        sessions = service.fetch(1, 10)

        then: "returns empty list"
        sessions.isEmpty()

        when: "fetch by state"
        sessions = service.fetchByState(created.state, SortOrder.DESC, 1)

        then: "returns existing app"
        sessions == [created]

        when: "fetch by other state"
        sessions = service.fetchByState(ApplicationState.IDLE, SortOrder.DESC, 1)

        then: "returns empty list"
        sessions.isEmpty()

        when: "fetch by id"
        def session = service.fetchOne(created.id)

        then: "returns with id"
        session.isPresent()

        when: "fetch by non-existing id"
        session = service.fetchOne("noo")

        then: "returns empty"
        session.isEmpty()
    }

    def "create statement"() {
        given:
        def params = newStatement()

        when: "creating statement"
        def result = service.createStatement("sessionId", params)

        then: "returns no session found"
        result instanceof StatementCreationResult.NoSessionExists
    }

    def "create statement2"() {
        given:
        def params = newStatement()
        def session = newSession(ApplicationState.KILLED)
        storage.saveApplication(session)
        backend.getInfo(session) >>  Optional.of( new ApplicationInfo(session.state, session.id))

        when: "creating statement"
        def result = service.createStatement(session.id, params)

        then: "returns session in invalid state"
        result instanceof StatementCreationResult.SessionInInvalidState
        ((StatementCreationResult.SessionInInvalidState)result).getInvalidState() == session.state
    }

    def "create statement3"() {
        given:
        def statementToCreate = newStatement()
        def session = newSession(ApplicationState.STARTING)
        storage.saveApplication(session)
        backend.getInfo(session) >>  Optional.of( new ApplicationInfo(session.state, session.id))
        def statementCreated = statement()
        statementHandler.processStatement(session.id, statementToCreate) >> statementCreated

        when: "creating statement"
        def result = service.createStatement(session.id, statementToCreate)

        then: "returns statement created"
        result instanceof StatementCreationResult.StatementCreated
        ((StatementCreationResult.StatementCreated)result).getStatement() == statementCreated
    }

}
