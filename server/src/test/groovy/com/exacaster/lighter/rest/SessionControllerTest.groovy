package com.exacaster.lighter.rest

import com.exacaster.lighter.application.sessions.StatementList
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class SessionControllerTest extends Specification {
    @Inject
    @Client("/lighter/api/")
    HttpClient client

    def "returns new session"() {
        when:
        def result = client.toBlocking()
                .exchange(HttpRequest.POST("/sessions", String).body(
                """
                    {
                        "name": "test",
                        "conf": {
                            "spark.lighter.token": "123"
                        }
                    }
                    """
                ), Map).body()

        then:
        result.log == []
        result.submitParams.name == "test"
        result.state == "not_started"
        result.kind == "pyspark"
        result.submitParams.conf["spark.lighter.token"] == "[redacted]"
    }

    def "returns bad request, when trying to override forbidden conf"() {
        when:
        def result = client.toBlocking()
                .exchange(HttpRequest.POST("/sessions", String).body(
                        """
                    {
                        "name": "test",
                        "conf": {
                            "spark.redaction.regex": ".*"
                        }
                    }
                    """
                ), Map)

        then:
        thrown(HttpClientResponseException)
    }

    def "returns new session with generated name, when name not provided"() {
        when:
        def result = client.toBlocking()
                .exchange(HttpRequest.POST("/sessions", String).body(
                        """
                    {
                        "args": ["test"]
                    }
                    """
                ), Map).body()

        then:
        result.log == []
        result.submitParams.name.startsWith("session_")
        result.state == "not_started"
        result.kind == "pyspark"
    }

    def "returns statements"() {
        when:
        def result = client.toBlocking()
                .retrieve(HttpRequest.GET("/sessions/123/statements?from=10"), StatementList)

        then:
        result.from == 10
        result.statements.size() == 0
    }
}
