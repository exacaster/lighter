package com.exacaster.lighter.application.sessions

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationInfo
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationStatusHandler
import com.exacaster.lighter.application.sessions.processors.StatementHandler
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.concurrency.EmptyWaitable
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
        def oldSession = newSession()
        service.lastUsed(oldSession.id) >> LocalDateTime.now().minusMinutes(conf.sessionConfiguration.timeoutMinutes + 1)

        def newSession = app()
        service.lastUsed(newSession.id) >> newSession.createdAt

        def permanentSession = ApplicationBuilder.builder(oldSession)
                .setId(conf.sessionConfiguration.permanentSessions.iterator().next().id)
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

    def "preserves active timeouted sessions"() {
        given:
        def oldSession = newSession()
        service.lastUsed(oldSession.id) >> LocalDateTime.now().minusMinutes(conf.sessionConfiguration.timeoutMinutes + 1)
        service.isActive(oldSession) >> true

        1 * service.fetchRunning() >> [
                oldSession,
        ]

        when:
        handler.handleTimeout()

        then:
        0 * service.killOne(oldSession)
    }

    def "tracks running"() {
        given:
        def session = app()
        def session2 = app()
        def permanentSession = ApplicationBuilder.builder(app())
                .setState(ApplicationState.STARTING)
                .setId(conf.sessionConfiguration.permanentSessions.iterator().next().id)
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
        def session = conf.sessionConfiguration.permanentSessions.iterator().next()
        def permanentSession = ApplicationBuilder.builder(app())
                .setSubmitParams(session.submitParams)
                .setState(ApplicationState.STARTING)
                .setId(session.id)
                .build()
        1 * service.fetchOne(session.id) >> Optional.of(permanentSession)
        1 * backend.getInfo(permanentSession) >> Optional.of(new ApplicationInfo(permanentSession.getState(), session.id))

        when: "exists healthy permanent session"
        handler.keepPermanentSessions()

        then: "do nothing"
        0 * service.deleteOne(session.id)
        0 * service.createSession(*_)

        when: "exists unhealthy permanent session"
        permanentSession = ApplicationBuilder.builder(permanentSession)
                .setState(ApplicationState.ERROR)
                .build()
        1 * service.fetchOne(session.id) >> Optional.of(permanentSession)
        handler.keepPermanentSessions()

        then: "restart permanent session"
        1 * service.deleteOne({ it -> it.getId() == session.getId() })
        1 * service.createSession(session.submitParams, session.id) >> permanentSession
        1 * handler.launch(permanentSession, _) >> EmptyWaitable.INSTANCE
    }

    def app() {
        ApplicationBuilder.builder(newSession())
                .setId("1")
                .setContactedAt(LocalDateTime.now())
                .build()
    }

    def setup() {
        LockAssert.TestHelper.makeAllAssertsPass(true)
    }
}
