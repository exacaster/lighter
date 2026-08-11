package com.exacaster.lighter.rest

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

class CorsTest {

    static preflight(String origin) {
        HttpRequest.OPTIONS("/batches")
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST.name())
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, HttpHeaders.CONTENT_TYPE)
    }

    @MicronautTest
    static class WithoutAllowedOrigins extends Specification {
        @Inject
        @Client("/lighter/api/")
        HttpClient client

        def "rejects every origin rather than answering allow-any"() {
            when:
            client.toBlocking().exchange(preflight("https://evil.example"))

            then:
            def error = thrown(HttpClientResponseException)
            error.response.header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) == null
        }
    }

    @MicronautTest
    @Property(name = "micronaut.server.cors.configurations.default.allowed-origins",
            value = "https://ui.example,https://other.example")
    static class WithAllowedOrigins extends Specification {
        @Inject
        @Client("/lighter/api/")
        HttpClient client

        def "allows every configured origin"() {
            when:
            def response = client.toBlocking().exchange(preflight(origin))

            then:
            response.header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) == origin

            where:
            origin << ["https://ui.example", "https://other.example"]
        }

        def "rejects other origins"() {
            when:
            client.toBlocking().exchange(preflight("https://evil.example"))

            then:
            def error = thrown(HttpClientResponseException)
            error.response.header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) == null
        }
    }
}
