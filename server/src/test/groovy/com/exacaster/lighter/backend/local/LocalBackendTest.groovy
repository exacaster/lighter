package com.exacaster.lighter.backend.local

import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.configuration.AppConfiguration
import org.apache.spark.launcher.SparkAppHandle
import spock.lang.Specification
import spock.lang.Subject

import static com.exacaster.lighter.test.Factories.appConfiguration
import static com.exacaster.lighter.test.Factories.newApplication

class LocalBackendTest extends Specification {
    AppConfiguration appConfig = appConfiguration()

    @Subject
    LocalBackend backend = new LocalBackend(appConfig)

    def "Works"() {
        given:
        def app = newApplication()

        when:
        def appHandle = backend.prepareSparkApplication(app, [:], {})

        then:
        appHandle != null
        appHandle.getEnvVariables() == [
            FOO: "bar",
            PY_GATEWAY_HOST: "localhost",
            PY_GATEWAY_PORT: "5432",
            LIGHTER_SESSION_ID: "1"
        ]

        when:
        def info = backend.getInfo(app)

        then:
        info.isEmpty()

        when: "App is running"
        backend.handleForApp(app).ifPresent({
            it.stateChanged(mockRunningAppHandle())
        })
        info = backend.getInfo(app)

        then: "Returns BUSY status"
        info.get().applicationId == app.id
        info.get().state == ApplicationState.BUSY

        when: "Getting logs"
        def logs = backend.getLogs(app)

        then: "Returns logs with app id"
        logs.get().id == app.id

        when: "App fails"
        backend.handleForApp(app).ifPresent({
            it.stateChanged(mockFailedAppHandle())
        })
        info = backend.getInfo(app)
        logs = backend.getLogs(app)

        then: "Returns error"
        info.get().state == ApplicationState.ERROR
        logs.get().getLog() == ""

        when: "Killing"
        backend.kill(app)

        then:
        backend.handleForApp(app).isEmpty()
    }

    def mockRunningAppHandle() {
        return Mock(SparkAppHandle) {
            getState() >> SparkAppHandle.State.RUNNING
            getError() >> Optional.empty()
        }
    }

    def mockFailedAppHandle() {
        return Mock(SparkAppHandle) {
            getState() >> SparkAppHandle.State.FAILED
            getError() >> Optional.of(new Exception("Oooops!"))
        }
    }
}
