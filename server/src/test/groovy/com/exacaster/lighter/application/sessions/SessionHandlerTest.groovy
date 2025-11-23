package com.exacaster.lighter.application.sessions

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationStatusHandler
import com.exacaster.lighter.application.sessions.processors.StatementHandler
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.configuration.AppConfiguration
import com.exacaster.lighter.storage.ApplicationStorage
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

    ApplicationStorage applicationStorage = Mock()

    @Subject
    SessionHandler handler = Spy(new SessionHandler(service, backend, statementHandler, tracker, conf, applicationStorage))

    def "kills timed-out sessions"() {
        given:
        def oldSession = newSession()
        service.lastUsed(oldSession.id) >> LocalDateTime.now() - conf.sessionConfiguration.timeoutInterval.plusMinutes(1)

        def newSession = app()
        service.lastUsed(newSession.id) >> newSession.createdAt

        1 * service.fetchRunningSession() >> [
                oldSession,
                newSession,
        ]

        when:
        handler.handleTimeout()

        then:
        1 * service.killOne(oldSession)
        0 * service.killOne(newSession)
    }

    def "preserves active timed-out sessions"() {
        given:
        def oldSession = newSession()
        service.lastUsed(oldSession.id) >> LocalDateTime.now() - conf.sessionConfiguration.timeoutInterval.plusMinutes(1)
        service.isActive(oldSession) >> true

        1 * service.fetchRunningSession() >> [
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
        service.fetchRunningSession() >> [session, session2, permanentSession]
        statementHandler.hasWaitingStatement(session) >> false
        statementHandler.hasWaitingStatement(session2) >> true
        statementHandler.hasWaitingStatement(permanentSession) >> false

        when:
        handler.trackRunning()

        then:
        1 * tracker.processApplicationRunning(session, _) >> ApplicationState.BUSY
        1 * tracker.processApplicationRunning(session2, _) >> ApplicationState.BUSY
        1 * tracker.processApplicationRunning(permanentSession, _) >> ApplicationState.BUSY
    }

    def app() {
        ApplicationBuilder.builder(newSession())
                .setId("1")
                .setContactedAt(LocalDateTime.now())
                .build()
    }

    def "cleans up old finished sessions"() {
        given:
        def oldSession = ApplicationBuilder.builder(newSession(ApplicationState.SUCCESS))
                .setId("old-1")
                .setContactedAt(LocalDateTime.now().minus(conf.stateRetainInterval.plusMinutes(1)))
                .build()

        when:
        handler.cleanupFinishedSessions()

        then:
        1 * service.fetchFinishedSessionsOlderThan(_) >> [oldSession]
        1 * applicationStorage.hardDeleteApplication("old-1")
    }

    def "does not clean up recent finished sessions"() {
        when:
        handler.cleanupFinishedSessions()

        then:
        1 * service.fetchFinishedSessionsOlderThan(_) >> []
        0 * applicationStorage.hardDeleteApplication(_)
    }

    def setup() {
        LockAssert.TestHelper.makeAllAssertsPass(true)
    }
}
