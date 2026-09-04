package com.exacaster.lighter.rest

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.storage.ApplicationStorage
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static com.exacaster.lighter.test.Factories.newSession

@Property(
        name = "datasources.default.url",
        value = "jdbc:h2:mem:batchcontroller;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL"
)
// prevents scheduler from picking up submitted jobs
@Property(name = "lighter.max-running-jobs", value = "0")
@MicronautTest(transactional = false)
class BatchControllerTest extends Specification {

    @Inject
    @Client("/lighter/api/")
    HttpClient client

    @Inject
    ApplicationStorage storage

    def "accepts a priority on submit and returns it at the top level"() {
        when:
        def result = submit('{"name": "prioritized", "file": "app.py", "priority": 5}')

        then:
        result.priority == 5
        result.state == "not_started"
        result.submitParams.name == "prioritized"
    }

    def "defaults priority to 0 when the caller omits it"() {
        when: "a body identical to what an existing client sends today"
        def result = submit('{"name": "plain", "file": "app.py"}')

        then:
        result.priority == 0
    }

    def "never echoes priority inside submit params"() {
        given:
        def id = submit('{"name": "no-copy", "file": "app.py", "priority": 5}').id

        expect: "the submitted priority is reported only at the top level"
        fetch(id).priority == 5
        fetch(id).submitParams.priority == null

        when: "the priority is later changed"
        setPriority(id, '{"priority": 9}')

        then: "there is still no second, now-stale copy to disagree with the top level one"
        fetch(id).priority == 9
        fetch(id).submitParams.priority == null
    }

    def "submitting a priority leaves the rest of the submit params untouched"() {
        given: "two otherwise identical submits, one carrying a priority"
        def body = '{"name": "twin", "file": "app.py", "numExecutors": 2, "conf": {"spark.a": "b"}%s}'

        when:
        def withPriority = submit(String.format(body, ', "priority": 5'))
        def withoutPriority = submit(String.format(body, ''))

        then: "the priority changes nothing else, so an existing caller sees exactly what it sees today"
        withPriority.submitParams == withoutPriority.submitParams
        withPriority.priority == 5
        withoutPriority.priority == 0
    }

    def "ignores submit properties it does not declare"() {
        when: "a body carrying the extra properties an existing caller sends"
        def result = submit('''
                {
                    "name": "unknown-props",
                    "file": "app.py",
                    "proxyUser": "someone",
                    "className": "com.example.Main",
                    "queue": "default"
                }
                ''')

        then: "the submit succeeds and priority still defaults"
        result.submitParams.name == "unknown-props"
        result.priority == 0
    }

    def "changes the priority of a waiting batch"() {
        given:
        def id = submit('{"name": "waiting", "file": "app.py"}').id

        when:
        def response = setPriority(id, '{"priority": 7}')

        then:
        response.status == HttpStatus.OK
        response.body().priority == 7
        fetch(id).priority == 7
    }

    def "silently ignores a priority change once the batch has left NOT_STARTED"() {
        given: "a batch the scheduler already moved on from"
        def id = submit('{"name": "started", "file": "app.py", "priority": 3}').id
        storage.saveApplication(ApplicationBuilder.builder(storage.findApplication(id).get())
                .setState(state).build())

        when:
        def response = setPriority(id, '{"priority": 99}')

        then: "no error, and no change"
        response.status == HttpStatus.OK
        response.body().priority == 3
        fetch(id).priority == 3

        where:
        state << [ApplicationState.STARTING, ApplicationState.BUSY, ApplicationState.SUCCESS, ApplicationState.KILLED]
    }

    def "returns 404 when changing the priority of an unknown batch"() {
        when:
        setPriority("no-such-batch", '{"priority": 1}')

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.NOT_FOUND
    }

    def "returns 404 when the id belongs to a session, leaving the session untouched"() {
        given: "a waiting session, which shares the application table with batches"
        def session = storage.saveApplication(newSession(ApplicationState.NOT_STARTED))

        when:
        setPriority(session.id, '{"priority": 9}')

        then: "the batches endpoint does not resolve a session"
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.NOT_FOUND

        and: "nothing was written to it"
        storage.findApplication(session.id).get().priority == 0
    }

    def "returns 404 when changing the priority of a deleted batch"() {
        given:
        def id = submit('{"name": "deleted", "file": "app.py"}').id
        storage.deleteApplication(id)

        when:
        setPriority(id, '{"priority": 1}')

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.NOT_FOUND
    }

    def "rejects a priority change with no priority in the body"() {
        given:
        def id = submit('{"name": "bad-body", "file": "app.py"}').id

        when:
        setPriority(id, '{}')

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
    }

    def "returns priority in the batch list"() {
        given:
        def id = submit('{"name": "listed", "file": "app.py", "priority": 4}').id

        when:
        def result = client.toBlocking().retrieve(HttpRequest.GET("/batches?size=100"), Map)

        then:
        result.applications.find { it.id == id }.priority == 4
    }

    private submit(String body) {
        client.toBlocking().exchange(HttpRequest.POST("/batches", String).body(body), Map).body()
    }

    private setPriority(String id, String body) {
        client.toBlocking().exchange(HttpRequest.POST("/batches/$id/priority", String).body(body), Map)
    }

    private fetch(String id) {
        client.toBlocking().retrieve(HttpRequest.GET("/batches/$id"), Map)
    }
}
