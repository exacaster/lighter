package com.exacaster.lighter.rest

import com.exacaster.lighter.application.sessions.SessionService
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

import static com.exacaster.lighter.test.Factories.newSession

@MicronautTest
class SessionControllerTest extends Specification {
    @Inject
    @Client("/lighter/api/")
    RxHttpClient client

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
    }
}
