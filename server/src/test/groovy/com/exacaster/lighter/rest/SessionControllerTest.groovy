package com.exacaster.lighter.rest

import com.exacaster.lighter.application.sessions.SessionService
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static com.exacaster.lighter.test.Factories.newSession

@MicronautTest
class SessionControllerTest extends Specification {
    @Inject
    @Client("/lighter/api/")
    HttpClient client

    @MockBean
    SessionService service() {
        return Mock(SessionService) {
            createSession(*_) >> newSession()
        }
    }

    def "returns new session"() {
        when:
        def result = client.toBlocking()
                .exchange(HttpRequest.POST("/sessions", String).body(
                """
                    {
                        "name": "test"
                    }
                    """
                ), Map.class).body()

        then:
        result.log == []
        result.submitParams.name == "test"
        result.state == "not_started"
        result.kind == "pyspark"
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
                ), Map.class).body()

        then:
        result.log == []
        result.submitParams.name.startsWith("session_")
        result.state == "not_started"
        result.kind == "pyspark"
    }
}
