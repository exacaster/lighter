package com.exacaster.lighter.backend.yarn

import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.backend.yarn.resources.YarnApplication
import com.exacaster.lighter.backend.yarn.resources.YarnApplicationListResponse
import com.exacaster.lighter.backend.yarn.resources.YarnApplicationResponse
import com.exacaster.lighter.backend.yarn.resources.YarnApplicationWrapper
import spock.lang.Specification
import spock.lang.Subject

import static com.exacaster.lighter.test.Factories.appConfiguration
import static com.exacaster.lighter.test.Factories.newApplication

class YarnBackendTest extends Specification {

    def client = Mock(YarnClient)

    def config = appConfiguration()

    def yarnProps = new YarnProperties(null, null);

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
        info.applicationId == "app-sp123"
        info.state == ApplicationState.BUSY
    }


    def "fetches logs"() {
        given:
        def app = newApplication(null)
        mockYarnApp(app)

        when:
        def logs = backend.getLogs(app).get()

        then: "returns tracking url as logs"
        logs.log == "track"
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
        def yarnId = "app-sp123"
        def yarnApp = new YarnApplication(yarnId, "track", "UNDEFINED")
        client.getApps(app.id) >> new YarnApplicationListResponse(new YarnApplicationWrapper([yarnApp]))
        client.getApplication(yarnId) >> new YarnApplicationResponse(yarnApp)
    }
}
