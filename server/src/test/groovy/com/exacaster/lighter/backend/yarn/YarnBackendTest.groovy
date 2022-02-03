package com.exacaster.lighter.backend.yarn

import com.exacaster.lighter.application.ApplicationState
import org.apache.hadoop.yarn.api.records.ApplicationId
import org.apache.hadoop.yarn.api.records.ApplicationReport
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus
import org.apache.hadoop.yarn.client.api.YarnClient
import org.apache.hadoop.yarn.exceptions.YarnException
import spock.lang.Specification
import spock.lang.Subject

import static com.exacaster.lighter.test.Factories.appConfiguration
import static com.exacaster.lighter.test.Factories.newApplication

class YarnBackendTest extends Specification {

    def client = Mock(YarnClient)

    def config = appConfiguration()

    def yarnProps = new YarnProperties(null, null)

    @Subject
    def backend = new YarnBackend(yarnProps, client, config)

    def "provides extra configs"() {
        given:
        def app = newApplication()

        when:
        def result = backend.getSubmitConfiguration(app)

        then:
        result["spark.master"] == "yarn"
        result["spark.yarn.tags"] == "lighter,${app.id}"
        result["spark.yarn.submit.waitAppCompletion"] == "false"
    }

    def "gets app info"() {
        given:
        def app = newApplication(null)
        mockYarnApp(app)

        when:
        def info = backend.getInfo(app).get()

        then:
        info.applicationId == "application_123_0123"
        info.state == ApplicationState.BUSY
    }


    def "fetches logs"() {
        given:
        def app = newApplication(null)
        mockYarnApp(app)

        when:
        def logs = backend.getLogs(app)

        then: "returns tracking url as logs"
        logs.isPresent()
        logs.get().log == "track"
    }

    def "fetches logs empty"() {
        given:
        def app = newApplication(null)
        client.getApplicationReport(_) >> {throw new YarnException()}
        mockYarnApp(app)

        when:
        def logs = backend.getLogs(app)

        then: "returns tracking url as logs"
        logs.isEmpty()
    }

    def "get session job resource"() {
        when:
        def resource = backend.getSessionJobResources()

        then:
        resource.endsWith("/shell_wrapper.py")
    }

    def "kills application"() {
        given:
        def app = newApplication()

        when:
        backend.kill(app)

        then:
        noExceptionThrown()
    }

    private mockYarnApp(app) {
        def yarnId = ApplicationId.fromString("application_123_0123")
        def yarnApp = Mock(ApplicationReport)
        yarnApp.getTrackingUrl() >> 'track'
        yarnApp.getApplicationId() >> yarnId
        yarnApp.getFinalApplicationStatus() >> FinalApplicationStatus.UNDEFINED
        client.getApplications(*_) >> [yarnApp]
        client.getApplicationReport(yarnId) >> yarnApp
    }
}
