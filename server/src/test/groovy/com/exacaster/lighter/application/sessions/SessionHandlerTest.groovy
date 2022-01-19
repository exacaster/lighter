package com.exacaster.lighter.application.sessions

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationStatusHandler
import com.exacaster.lighter.application.sessions.processors.StatementHandler
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.configuration.AppConfiguration
import net.javacrumbs.shedlock.core.LockAssert
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

import static com.exacaster.lighter.test.Factories.appConfiguration
import static com.exacaster.lighter.test.Factories.newSession

class SessionHandlerTest extends Specification {

    SessionService service = Mock()

    Backend backend = Mock()

    AppConfiguration conf = appConfiguration()

    ApplicationStatusHandler tracker = Mock()

    StatementHandler statementHandler = Mock()

    @Subject
    SessionHandler handler = Spy(new SessionHandler(service, backend, statementHandler, tracker, conf))

    def "kills timeouted sessions"() {
        given:
        def oldSession = ApplicationBuilder.builder(newSession())
                .setCreatedAt(LocalDateTime.now().minusMinutes(conf.sessionConfiguration.timeoutMinutes + 1))
                .build()
        def newSession = app()
        def permanentSession = ApplicationBuilder.builder(oldSession)
                .setId(conf.sessionConfiguration.permanentSessionId)
                .build()
        1 * service.fetchRunning() >> [
                oldSession,
                newSession,
                permanentSession
        ]

        when:
        handler.handleTimeout()

        then:
        1 * service.killOne(oldSession)
        0 * service.killOne(newSession)
        0 * service.killOne(permanentSession)
    }

    def "tracks running"() {
        given:
        def session = app()
        def session2 = app()
        def permanentSession = ApplicationBuilder.builder(app())
                .setState(ApplicationState.STARTING)
                .setId(conf.sessionConfiguration.permanentSessionId)
                .build()
        service.fetchRunning() >> [session, session2, permanentSession]
        statementHandler.hasWaitingStatement(session) >> false
        statementHandler.hasWaitingStatement(session2) >> true
        statementHandler.hasWaitingStatement(permanentSession) >> false

        when:
        handler.trackRunning()

        then:
        1 * tracker.processApplicationIdle(session)
        0 * tracker.processApplicationIdle(session2)
        1 * tracker.processApplicationIdle(permanentSession)
        1 * tracker.processApplicationRunning(session2)
        0 * tracker.processApplicationRunning(session)
        0 * tracker.processApplicationRunning(permanentSession)
    }

    def "keeps permanent session"() {
        given:
        def sessionId = conf.sessionConfiguration.permanentSessionId
        def params = conf.sessionConfiguration.permanentSessionParams
        def permanentSession = ApplicationBuilder.builder(app())
                .setSubmitParams(params)
                .setState(ApplicationState.STARTING)
                .setId(sessionId)
                .build()
        1 * service.fetchOne(sessionId) >> Optional.of(permanentSession)

        when: "exists healthy permanent session"
        handler.keepPermanentSession()

        then: "do nothing"
        0 * service.deleteOne(sessionId)
        0 * service.createSession(*_)

        when: "exists unhealthy permanent session"
        permanentSession = ApplicationBuilder.builder(permanentSession)
                .setState(ApplicationState.ERROR)
                .build()
        1 * service.fetchOne(sessionId) >> Optional.of(permanentSession)
        handler.keepPermanentSession()

        then: "restart permanent session"
        1 * service.deleteOne(sessionId)
        1 * service.createSession(params, sessionId) >> permanentSession
        1 * handler.launch(permanentSession, _) >> {}
    }

    def app() {
        ApplicationBuilder.builder(newSession())
                .setContactedAt(LocalDateTime.now())
                .build()
    }

    def setup() {
        LockAssert.TestHelper.makeAllAssertsPass(true)
    }
}
