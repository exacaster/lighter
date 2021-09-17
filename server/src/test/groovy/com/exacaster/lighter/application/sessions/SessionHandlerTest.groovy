package com.exacaster.lighter.application.sessions

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationStatusHandler
import com.exacaster.lighter.application.sessions.processors.StatementHandler
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.configuration.AppConfiguration
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
    SessionHandler handler = new SessionHandler(service, backend, statementHandler, tracker, conf)

    def "kills timeouted sessions"() {
        given:
        def oldSession = ApplicationBuilder.builder(newSession())
                .setContactedAt(LocalDateTime.now().minusMinutes(conf.sessionConfiguration.timeoutMinutes + 1))
                .build()
        def newSession = app()
        1 * service.fetchRunning() >> [
                oldSession,
                newSession
        ]

        when:
        handler.handleTimeout()

        then:
        1 * service.killOne(oldSession)
        0 * service.killOne(newSession)
    }

    def "tracks running"() {
        given:
        def session = app()
        def session2 = app()
        service.fetchRunning() >> [session, session2]
        statementHandler.hasWaitingStatement(session) >> false
        statementHandler.hasWaitingStatement(session2) >> true

        when:
        handler.trackRunning()

        then:
        1 * tracker.processApplicationIdle(session)
        0 * tracker.processApplicationIdle(session2)
        1 * tracker.processApplicationRunning(session2)
        0 * tracker.processApplicationRunning(session)
    }

    def app() {
        ApplicationBuilder.builder(newSession())
                .setContactedAt(LocalDateTime.now())
                .build()
    }
}
