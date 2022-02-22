package com.exacaster.lighter.application.sessions

import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.sessions.processors.StatementHandler
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.storage.ApplicationStorage
import com.exacaster.lighter.storage.StatementStorage
import com.exacaster.lighter.test.InMemoryStorage
import spock.lang.Specification
import spock.lang.Subject

import static com.exacaster.lighter.test.Factories.submitParams

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
        sessions = service.fetchByState(created.state, 1)

        then: "returns existing app"
        sessions == [created]

        when: "fetch by other state"
        sessions = service.fetchByState(ApplicationState.IDLE, 1)

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
}
