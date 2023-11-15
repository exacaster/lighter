package com.exacaster.lighter.application.sessions

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationInfo
import com.exacaster.lighter.application.ApplicationStatusHandler
import com.exacaster.lighter.application.ApplicationType
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

class PermanentSessionHandlerTest extends Specification {

    SessionService service = Mock()

    Backend backend = Mock()

    AppConfiguration conf = appConfiguration()

    ApplicationStatusHandler tracker = Mock()

    StatementHandler statementHandler = Mock()

    @Subject
    SessionHandler handler = Spy(new SessionHandler(service, backend, statementHandler, tracker, conf))

    def configPermanentSession = conf.sessionConfiguration.permanentSessions.iterator().next()

    def "creates a brand new perm session from yaml"() {
        given:
        def configPermanentSession = conf.sessionConfiguration.permanentSessions.iterator().next()
        def expectedSession = ApplicationBuilder.builder(app())
                .setSubmitParams(configPermanentSession.submitParams)
                .setState(ApplicationState.STARTING)
                .setId(configPermanentSession.id)
                .setType(ApplicationType.PERMANENT_SESSION)
                .build()
        1 * service.fetchAllPermanentSessions() >> Collections.emptyMap()
        1 * service.fetchOne(configPermanentSession.id) >> Optional.empty()
        backend.getInfo(*_) >> Optional.empty()

        when:
        handler.keepPermanentSessions2()

        then: "creates a new permanent session"
        1 * service.createPermanentSession(configPermanentSession.id, configPermanentSession.submitParams) >> expectedSession
        1 * service.deleteOne(expectedSession)
        1 * tracker.processApplicationStarting(expectedSession)
        1 * handler.launch(expectedSession, _) >> EmptyWaitable.INSTANCE
    }


    def "recreates a new session when unhealthy"() {
        given:
        def unhealthySession = ApplicationBuilder.builder(app())
                .setSubmitParams(configPermanentSession.submitParams)
                .setState(ApplicationState.ERROR)
                .setId(configPermanentSession.id)
                .setType(ApplicationType.PERMANENT_SESSION)
                .build()
        1 * service.fetchAllPermanentSessions() >> Map.of(unhealthySession.id, unhealthySession)
        1 * service.fetchOne(unhealthySession.id) >> Optional.of(unhealthySession)
        backend.getInfo(unhealthySession) >> Optional.of(new ApplicationInfo(unhealthySession.state, unhealthySession.id))

        def expectedSession = ApplicationBuilder.builder(app())
                .setSubmitParams(configPermanentSession.submitParams)
                .setState(ApplicationState.STARTING)
                .setId(configPermanentSession.id)
                .setType(ApplicationType.PERMANENT_SESSION)
                .build()

        when:
        handler.keepPermanentSessions2()

        then: "creates a new permanent session"
        1 * service.createPermanentSession(unhealthySession.id, unhealthySession.submitParams) >> expectedSession
        1 * service.deleteOne(expectedSession)
        1 * tracker.processApplicationStarting(expectedSession)
        1 * handler.launch(expectedSession, _) >> EmptyWaitable.INSTANCE
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
