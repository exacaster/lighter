package com.exacaster.lighter.application.sessions

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationType
import com.exacaster.lighter.application.sessions.exceptions.SessionLimitExceededException
import java.time.LocalDateTime
import com.exacaster.lighter.application.sessions.processors.StatementHandler
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.configuration.AppConfiguration
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
    AppConfiguration config = appConfiguration()

    @Subject
    SessionService service = new SessionService(storage, statementStorage, backend, statementHandler, config)

    def "manage sessions"() {
        given:
        def sessionParameters = sessionParams()

        when: "creating session"
        def created = service.createSession(sessionParameters)

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

    def "validate max running sessions limit"() {
        given:
        def runningSession1 = ApplicationBuilder.builder().setAppId("app1")
                .setType(ApplicationType.SESSION)
                .setState(ApplicationState.IDLE)
                .setAppInfo("info")
                .setCreatedAt(LocalDateTime.now())
                .setId("session1")
                .setSubmitParams(null)
                .build()
        def runningSession2 = ApplicationBuilder.builder().setAppId("app2")
                .setType(ApplicationType.SESSION)
                .setState(ApplicationState.BUSY)
                .setAppInfo("info")
                .setCreatedAt(LocalDateTime.now())
                .setId("session2")
                .setSubmitParams(null)
                .build()
        storage.saveApplication(runningSession1)
        storage.saveApplication(runningSession2)
        def configWithLimit = new AppConfiguration(
                config.maxRunningJobs,
                config.maxStartingJobs,
                2, // maxRunningSessions
                config.sparkHistoryServerUrl,
                config.externalLogsUrlTemplate,
                config.pyGatewayPort,
                config.url,
                config.zombieInterval,
                config.sessionConfiguration,
                config.batchDefaultConf,
                config.sessionDefaultConf
        )
        def serviceWithLimit = new SessionService(storage, statementStorage, backend, statementHandler, configWithLimit)
        backend.getSessionJobResources() >> []

        when:
        serviceWithLimit.createSession(sessionParams())

        then:
        thrown(SessionLimitExceededException)
    }


    def "allows session creation when limit not exceeded"() {
        given:
        def runningSession = newSession(ApplicationState.IDLE)
        storage.saveApplication(runningSession)
        def configWithLimit = new AppConfiguration(
                config.maxRunningJobs,
                config.maxStartingJobs,
                5, // maxRunningSessions
                config.sparkHistoryServerUrl,
                config.externalLogsUrlTemplate,
                config.pyGatewayPort,
                config.url,
                config.zombieInterval,
                config.sessionConfiguration,
                config.batchDefaultConf,
                config.sessionDefaultConf
        )
        def serviceWithLimit = new SessionService(storage, statementStorage, backend, statementHandler, configWithLimit)
        backend.getSessionJobResources() >> []

        when:
        def created = serviceWithLimit.createSession(sessionParams())

        then:
        created != null
        created.id != null
    }

    def "blocks session creation when limit is 0"() {
        given:
        def configWithZeroLimit = new AppConfiguration(
                config.maxRunningJobs,
                config.maxStartingJobs,
                0, // maxRunningSessions = 0 means no sessions allowed
                config.sparkHistoryServerUrl,
                config.externalLogsUrlTemplate,
                config.pyGatewayPort,
                config.url,
                config.zombieInterval,
                config.sessionConfiguration,
                config.batchDefaultConf,
                config.sessionDefaultConf
        )
        def serviceWithZeroLimit = new SessionService(storage, statementStorage, backend, statementHandler, configWithZeroLimit)

        when:
        serviceWithZeroLimit.createSession(sessionParams())

        then:
        thrown(SessionLimitExceededException)
    }

}
