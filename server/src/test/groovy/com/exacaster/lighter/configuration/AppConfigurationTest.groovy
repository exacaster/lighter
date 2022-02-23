package com.exacaster.lighter.configuration

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Subject

@MicronautTest
class AppConfigurationTest extends Specification {
    @Inject
    @Subject
    AppConfiguration appConfiguration

    def "binds properties form yaml"() {
        expect:
        appConfiguration.maxRunningJobs == 5
        appConfiguration.sessionConfiguration.timeoutMinutes == 90
        appConfiguration.sessionConfiguration.permanentSessions.size() == 1
        appConfiguration.sessionConfiguration.permanentSessions.get(0).id == "permanentId1"
        appConfiguration.sessionConfiguration.permanentSessions.get(0).submitParams.conf == [
                "spark.kubernetes.namespace": "spark",
                "spark.kubernetes.driver.secrets.spark-secret": "/etc/secret"
        ]
    }

}
