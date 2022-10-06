package com.exacaster.lighter.backend.kubernetes

import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.configuration.AppConfiguration
import io.fabric8.kubernetes.api.model.PodBuilder
import io.fabric8.kubernetes.api.model.PodListBuilder
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer
import spock.lang.Specification
import spock.lang.Subject

import static com.exacaster.lighter.test.Factories.*

class KubernetesBackendTest extends Specification {

    KubernetesMockServer server = new KubernetesMockServer()
    KubernetesProperties properties = kubernetesProperties()
    AppConfiguration appConfig = appConfiguration()

    @Subject
    KubernetesBackend backend = new KubernetesBackend(properties, appConfig, server.createClient())

    def "provides extra configs"() {
        given:
        def app = newApplication()

        when:
        def result = backend.getBackendConfiguration(app)

        then:
        result["spark.master"] == properties.master
        result["spark.kubernetes.driver.label.spark-app-tag"] == app.id
        result["spark.kubernetes.executor.label.spark-app-tag"] == app.id
        result["spark.kubernetes.submission.waitAppCompletion"] == "false"
        result["spark.kubernetes.driverEnv.PY_GATEWAY_PORT"] == "${appConfig.pyGatewayPort}"
        result["spark.kubernetes.driverEnv.PY_GATEWAY_HOST"] == "lighter"
        result["spark.kubernetes.driverEnv.LIGHTER_SESSION_ID"] == app.id
        result["spark.kubernetes.namespace"] == "spark"
    }

    def "gets application info"() {
        given:
        def app = newApplication()
        mockDriverPod(app)

        when:
        def info = backend.getInfo(app).get()

        then:
        info.applicationId == "app-sp123"
        info.state == ApplicationState.BUSY
    }

    def "fetches logs"() {
        given:
        def app = newApplication()
        mockDriverPod(app)
        mockLogs()

        when:
        def logs = backend.getLogs(app).get()

        then:
        logs.log == "logslogslogs"
    }

    def "get session job resource"() {
        when:
        def resource = backend.getSessionJobResources()

        then:
        resource == "${appConfig.getUrl()}/lighter/jobs/shell_wrapper.py"
    }

    def "kills application"() {
        given:
        def app = newApplication()
        mockDriverPod(app)

        when:
        backend.kill(app)

        then:
        noExceptionThrown()
    }

    private mockDriverPod(app) {
        def pod = new PodBuilder()
                .withNewStatus()
                .withPhase("Running")
                .endStatus()
                .withNewMetadata()
                .addToLabels("spark-app-selector", "app-sp123")
                .withName("pod1")
                .withNamespace("spark")
                .and().build()
        server.expect().withPath("/api/v1/namespaces/spark/pods?labelSelector=spark-app-tag%3D${app.id}%2Cspark-role%3Ddriver")
                .andReturn(200, new PodListBuilder().addNewItemLike(pod).and().build()).once()
    }

    private mockLogs() {
        server.expect().withPath("/api/v1/namespaces/spark/pods/pod1/log?pretty=true&tailLines=${properties.getMaxLogSize()}")
                .andReturn(200, "logslogslogs").once()
    }


}
