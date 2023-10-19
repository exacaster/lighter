package com.exacaster.lighter.application.sessions

import com.exacaster.lighter.application.ApplicationInfo
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.sessions.processors.StatementHandler
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.storage.ApplicationStorage
import com.exacaster.lighter.storage.StatementStorage
import com.exacaster.lighter.test.InMemoryStorage
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static com.exacaster.lighter.test.Factories.*

class CreateStatementTest extends Specification {
    ApplicationStorage storage = new InMemoryStorage()
    Backend backend = Mock()
    StatementHandler statementHandler = Mock()

    @Subject
    SessionService service = new SessionService(storage, Mock(StatementStorage), backend, statementHandler)

    def 'on non existing session id returns NoSessionExists'() {
        given:
        def params = newStatement()

        when: "creating statement"
        def result = service.createStatement("sessionId", params)

        then: "returns no session found"
        result instanceof StatementCreationResult.NoSessionExists
    }

    @Unroll
    def "on completed session returns SessionInInvalidState"() {
        given:
        def params = newStatement()

        and: "session is stored with status"
        def session = newSession(savedState)
        storage.saveApplication(session)

        and: "session 'live' status is"
        backend.getInfo(session) >>  Optional.of( new ApplicationInfo(liveState, session.id))

        when: "creating statement"
        def result = service.createStatement(session.id, params)

        then: "returns session in invalid state"
        result instanceof StatementCreationResult.SessionInInvalidState
        ((StatementCreationResult.SessionInInvalidState)result).getInvalidState() == liveState

        where:
        savedState | liveState
        ApplicationState.NOT_STARTED| ApplicationState.DEAD
        ApplicationState.STARTING | ApplicationState.ERROR
        ApplicationState.IDLE | ApplicationState.KILLED
        ApplicationState.IDLE | ApplicationState.ERROR
        ApplicationState.NOT_STARTED | ApplicationState.DEAD
        ApplicationState.NOT_STARTED | ApplicationState.SUCCESS
        ApplicationState.KILLED | ApplicationState.KILLED
        ApplicationState.ERROR | ApplicationState.ERROR
    }


    def "on non-completed session returns StatementCreated"() {
        given:
        def statementToCreate = newStatement()

        and: "session is stored with non completed state "
        def session = newSession(ApplicationState.STARTING)
        storage.saveApplication(session)

        and: "session 'live' status is not completed "
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
