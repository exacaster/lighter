package com.exacaster.lighter.application.batch

import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationStatusHandler
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.configuration.AppConfiguration
import spock.lang.Specification
import spock.lang.Subject

import static com.exacaster.lighter.test.Factories.*

class BatchHandlerTest extends Specification {

    Backend backend = Mock()

    BatchService service = Mock()

    ApplicationStatusHandler statusHandler = Mock()

    AppConfiguration config = new AppConfiguration(1, null, null, null, null)

    @Subject
    def handler = Spy(new BatchHandler(backend, service, config, statusHandler))

    def "triggering scheduled apps"() {
        given:
        def app = newApplication()

        when:
        handler.processScheduledBatches()

        then:
        _ * service.fetchRunning() >> []
        1 * service.fetchByState(ApplicationState.NOT_STARTED, _) >> [app]
        1 * handler.launch(app, _) >> {  }
    }

    def "does not trigger when there are no empty slots"() {
        given:
        def app = newApplication()

        when:
        handler.processScheduledBatches()

        then:
        _ * service.fetchRunning() >> [app]
        _ * service.fetchByState(ApplicationState.NOT_STARTED, 0) >> []
        0 * handler.launch(app, _) >> {  }
    }
}
