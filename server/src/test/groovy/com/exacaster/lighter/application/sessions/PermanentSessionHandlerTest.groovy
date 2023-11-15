package com.exacaster.lighter.application.sessions

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
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

    def "keeps permanent session"() {
        given:
        def configPermanentSession = conf.sessionConfiguration.permanentSessions.iterator().next()
        def permanentSession = ApplicationBuilder.builder(app())
                .setSubmitParams(configPermanentSession.submitParams)
                .setState(ApplicationState.STARTING)
                .setId(configPermanentSession.id)
                .setType(ApplicationType.PERMANENT_SESSION)
                .build()

        when: "new perm session from config"
        1 * service.fetchAllPermanentSessions() >> Collections.emptyMap()
        1 * service.fetchOne(configPermanentSession.id) >> Optional.empty()
        handler.keepPermanentSessions2()

        then: "creates a new permanent session"
        1 * service.createPermanentSession(configPermanentSession.id, configPermanentSession.submitParams) >> permanentSession
        1 * service.deleteOne(permanentSession)
        1 * tracker.processApplicationStarting(permanentSession)
        1 * handler.launch(permanentSession, _) >> EmptyWaitable.INSTANCE


//        when: "exists unhealthy permanent session"
//        permanentSession = ApplicationBuilder.builder(permanentSession)
//                .setState(ApplicationState.ERROR)
//                .build()
//        1 * service.fetchOne(configPermanentSession.id) >> Optional.of(permanentSession)
//        handler.keepPermanentSessions()
//
//        then: "restart permanent session"
//        1 * service.deleteOne({ it -> it.getId() == configPermanentSession.getId() })
//        1 * service.createSession(configPermanentSession.submitParams, configPermanentSession.id) >> permanentSession
//        1 * handler.launch(permanentSession, _) >> EmptyWaitable.INSTANCE
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
