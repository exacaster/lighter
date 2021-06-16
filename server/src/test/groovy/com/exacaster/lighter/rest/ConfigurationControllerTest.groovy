package com.exacaster.lighter.rest


import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class ConfigurationControllerTest extends Specification {
    @Inject
    @Client("/lighter/api/")
    RxHttpClient client

    def "returns public configurations"() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.GET("/configuration"), Map.class)

        then:
        result.maxRunningJobs == null
        result.sparkHistoryServerUrl != null
    }
}
