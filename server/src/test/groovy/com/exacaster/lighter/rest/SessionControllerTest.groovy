package com.exacaster.lighter.rest

import com.exacaster.lighter.application.sessions.SessionService
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static com.exacaster.lighter.test.Factories.newSession

@MicronautTest
@Property(name = "lighter.session.permanent-session-id", value = "permanentSessionId")
@Property(name = "lighter.session.permanent-session-params.conf.spark.kubernetes.node.selector.dedicated", value = "app")
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
        result.state == "not_started"
        result.kind == "pyspark"
    }

    def "returns permanent session"() {
        when:
        def result = client.toBlocking()
                .exchange(HttpRequest.GET("/sessions/permanentSessionId"), Map.class).body()

        then:
        result.id == "permanentSessionId"
        result.state == "starting"
        result.kind == "pyspark"
        result.submitParams.conf.'spark.kubernetes.node.selector.dedicated' == "app"
    }
}
