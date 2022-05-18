package com.exacaster.lighter.application.batch

import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationStatusHandler
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.concurrency.EmptyWaitable
import com.exacaster.lighter.configuration.AppConfiguration
import com.exacaster.lighter.storage.SortOrder
import net.javacrumbs.shedlock.core.LockAssert
import spock.lang.Specification
import spock.lang.Subject

import static com.exacaster.lighter.test.Factories.*

class BatchHandlerTest extends Specification {

    Backend backend = Mock()

    BatchService service = Mock()

    ApplicationStatusHandler statusHandler = Mock()

    AppConfiguration config = appConfiguration()

    @Subject
    def handler = Spy(new BatchHandler(backend, service, config, statusHandler))

    def setup() {
        LockAssert.TestHelper.makeAllAssertsPass(true)
    }

    def "triggering scheduled apps"() {
        given:
        def app = newApplication()

        when:
        handler.processScheduledBatches()

        then:
        _ * service.fetchRunning() >> []
        1 * service.fetchByState(ApplicationState.NOT_STARTED, *_) >> [app]
        1 * handler.launch(app, _) >> EmptyWaitable.INSTANCE
    }

    def "does not trigger when there are no empty slots"() {
        given:
        def app = newApplication()

        when:
        handler.processScheduledBatches()

        then:
        _ * service.fetchRunning() >> [app]
        _ * service.fetchByState(ApplicationState.NOT_STARTED, SortOrder.ASC, 0, config.getMaxRunningJobs() - 1) >> []
        0 * handler.launch(app, _) >> {  }
    }

    def "tracks running jobs"() {
        given:
        def app = newApplication()
        service.fetchRunning() >> [app]
        service.fetchByState(*_) >> []

        when:
        handler.trackRunning()

        then:
        1 * statusHandler.processApplicationRunning(app) >> ApplicationState.BUSY
        0 * handler.processScheduledBatches()

        when:
        handler.trackRunning()

        then:
        1 * statusHandler.processApplicationRunning(app) >> ApplicationState.SUCCESS
        1 * handler.processScheduledBatches()
    }
}
