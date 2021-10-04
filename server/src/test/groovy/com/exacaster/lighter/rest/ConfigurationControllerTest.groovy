package com.exacaster.lighter.rest

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class ConfigurationControllerTest extends Specification {
    @Inject
    @Client("/lighter/api/")
    HttpClient client

    def "returns public configurations"() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.GET("/configuration"), Map.class)

        then:
        result.maxRunningJobs == null
        result.sparkHistoryServerUrl != null
    }
}
