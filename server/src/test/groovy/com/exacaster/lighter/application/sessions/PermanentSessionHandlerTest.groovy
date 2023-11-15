package com.exacaster.lighter.application.sessions

import com.exacaster.lighter.application.*
import com.exacaster.lighter.application.sessions.processors.StatementHandler
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.concurrency.EmptyWaitable
import com.exacaster.lighter.configuration.AppConfiguration
import net.javacrumbs.shedlock.core.LockAssert
import spock.lang.Specification
import spock.lang.Subject

import static com.exacaster.lighter.test.Factories.appConfiguration

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
        def expectedSession = ApplicationBuilder.builder()
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
        def unhealthySession = ApplicationBuilder.builder()
                .setSubmitParams(configPermanentSession.submitParams)
                .setState(ApplicationState.ERROR)
                .setId(configPermanentSession.id)
                .setType(ApplicationType.PERMANENT_SESSION)
                .build()
        1 * service.fetchAllPermanentSessions() >> Map.of(unhealthySession.id, unhealthySession)
        1 * service.fetchOne(unhealthySession.id) >> Optional.of(unhealthySession)
        backend.getInfo(unhealthySession) >> Optional.of(new ApplicationInfo(unhealthySession.state, unhealthySession.id))

        def expectedSession = ApplicationBuilder.builder(unhealthySession)
                .setState(ApplicationState.STARTING)
                .build()

        when:
        handler.keepPermanentSessions2()

        then: "creates a new permanent session"
        1 * service.createPermanentSession(unhealthySession.id, unhealthySession.submitParams) >> expectedSession
        1 * service.deleteOne(expectedSession)
        1 * tracker.processApplicationStarting(expectedSession)
        1 * handler.launch(expectedSession, _) >> EmptyWaitable.INSTANCE
    }

    def "recreates unhealthy perm session from storage"() {
        given:
        def configPermanentSession = conf.sessionConfiguration.permanentSessions.iterator().next()
        def healthySessionFromYaml = ApplicationBuilder.builder()
                .setSubmitParams(configPermanentSession.submitParams)
                .setState(ApplicationState.STARTING)
                .setId(configPermanentSession.id)
                .setType(ApplicationType.PERMANENT_SESSION)
                .build()
        def unhealthySessionFromStorage = ApplicationBuilder.builder()
                .setSubmitParams(configPermanentSession.submitParams)
                .setState(ApplicationState.DEAD)
                .setId("storageSessionId")
                .setType(ApplicationType.PERMANENT_SESSION)
                .build()

        def expectedSession = ApplicationBuilder.builder(unhealthySessionFromStorage)
                .setState(ApplicationState.STARTING)
                .build()

        1 * service.fetchAllPermanentSessions() >> Map.of(healthySessionFromYaml.id, healthySessionFromYaml, unhealthySessionFromStorage.id, unhealthySessionFromStorage)
        1 * service.fetchOne(configPermanentSession.id) >> Optional.of(healthySessionFromYaml)
        1 * service.fetchOne(unhealthySessionFromStorage.id) >> Optional.of(unhealthySessionFromStorage)
        backend.getInfo(healthySessionFromYaml) >> Optional.of(new ApplicationInfo(healthySessionFromYaml.state, healthySessionFromYaml.id))

        when:
        handler.keepPermanentSessions2()

        then: "creates a new permanent session"
        1 * service.createPermanentSession(unhealthySessionFromStorage.id, unhealthySessionFromStorage.submitParams) >> expectedSession
        1 * service.deleteOne(expectedSession)
        1 * tracker.processApplicationStarting(expectedSession)
        1 * handler.launch(expectedSession, _) >> EmptyWaitable.INSTANCE
    }


    def setup() {
        LockAssert.TestHelper.makeAllAssertsPass(true)
    }
}
