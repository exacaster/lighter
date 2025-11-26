package com.exacaster.lighter.application.batch

import com.exacaster.lighter.Application
import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationStatusHandler
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.concurrency.EmptyWaitable
import com.exacaster.lighter.configuration.AppConfiguration
import com.exacaster.lighter.storage.ApplicationStorage
import com.exacaster.lighter.storage.SortOrder
import net.javacrumbs.shedlock.core.LockAssert
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

import static com.exacaster.lighter.test.Factories.*

class BatchHandlerTest extends Specification {

    Backend backend = Mock()

    BatchService service = Mock()

    ApplicationStatusHandler statusHandler = Mock()

    AppConfiguration config = appConfiguration()

    ApplicationStorage applicationStorage = Mock()

    @Subject
    def handler = Spy(new BatchHandler(backend, service, config, statusHandler, applicationStorage))

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
        _ * service.fetchByState(ApplicationState.NOT_STARTED, SortOrder.ASC, 0, _) >> []
    }

    def "validate max running jobs limit"() {
        given:
        def app = newApplication()
        def runningApps = [app, app]

        when:
        handler.processScheduledBatches()

        then:
        _ * service.fetchRunning() >> runningApps
        _ * service.fetchByState(ApplicationState.NOT_STARTED, SortOrder.ASC, 0, config.getMaxRunningJobs() - runningApps.size()) >> [app]
        (config.getMaxRunningJobs() - runningApps.size()) * handler.launch(app, _) >> EmptyWaitable.INSTANCE
    }

    def "validate max starting jobs limit"() {
        given:
        def app = newApplication()
        def appsToRun = [app, app]

        when:
        handler.processScheduledBatches()

        then:
        _ * service.fetchRunning() >> []
        _ * service.fetchByState(ApplicationState.NOT_STARTED, SortOrder.ASC, 0, config.getMaxStartingJobs()) >> appsToRun
        config.getMaxStartingJobs() * handler.launch(app, _) >> EmptyWaitable.INSTANCE
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

    def "cleans up old finished batches"() {
        given:
        def oldBatch = ApplicationBuilder.builder(newApplication())
                .setId("old-1")
                .setState(ApplicationState.SUCCESS)
                .setFinishedAt(LocalDateTime.now().minus(config.stateRetainInterval.plusMinutes(1)))
                .build()

        when:
        handler.cleanupFinishedBatches()

        then:
        1 * service.fetchFinishedBatchesOlderThan(_, _) >> [oldBatch]
        1 * applicationStorage.hardDeleteApplication("old-1")
    }

    def "does not clean up recent finished batches"() {
        when:
        handler.cleanupFinishedBatches()

        then:
        1 * service.fetchFinishedBatchesOlderThan(_, _) >> []
        0 * applicationStorage.hardDeleteApplication(_)
    }
}
