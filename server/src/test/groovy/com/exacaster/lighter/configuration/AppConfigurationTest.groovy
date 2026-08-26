package com.exacaster.lighter.configuration

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.context.env.PropertySource
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Subject

@MicronautTest
@Property(name="lighter.batch-default-conf", value='{"spark.driver.cores": "1"}')
@Property(name="lighter.session-default-conf", value='{"spark.driver.cores": "2"}')
@Property(name="lighter.py-gateway-auth-token", value='s3cret')
class AppConfigurationTest extends Specification {
    @Inject
    @Subject
    AppConfiguration appConfiguration

    def "binds properties form yaml"() {
        expect:
        appConfiguration.maxRunningJobs == 5
        appConfiguration.sessionConfiguration.timeoutInterval.toMinutes() == 90
        appConfiguration.sessionConfiguration.permanentSessions.size() == 1
        appConfiguration.sessionConfiguration.permanentSessions.get(0).id == "permanentId1"
        appConfiguration.sessionConfiguration.permanentSessions.get(0).submitParams.conf == [
            "spark.kubernetes.namespace": "spark",
            "spark.kubernetes.driver.secrets.spark-secret": "/etc/secret"
        ]
        appConfiguration.batchDefaultConf != null
        appConfiguration.batchDefaultConf.get("spark.driver.cores") == "1"
        appConfiguration.sessionDefaultConf.get("spark.driver.cores") == "2"
        appConfiguration.pyGatewayAuthToken == "s3cret"
        appConfiguration.pyGatewayReadTimeoutInSec == 60
        appConfiguration.hasPyGatewayAuthToken()
    }

    def "binds the gateway read timeout from its environment variable"() {
        given:
        def ctx = ApplicationContext.run(
                PropertySource.of(
                        "env",
                        ["LIGHTER_PY_GATEWAY_READ_TIMEOUT_IN_SEC": "5"],
                        PropertySource.PropertyConvention.ENVIRONMENT_VARIABLE,
                        null
                )
        )

        expect:
        ctx.getBean(AppConfiguration).pyGatewayReadTimeoutInSec == 5

        cleanup:
        ctx.close()
    }

    def "keeps the gateway auth token out of toString"() {
        expect:
        !appConfiguration.toString().contains("s3cret")
    }

}
