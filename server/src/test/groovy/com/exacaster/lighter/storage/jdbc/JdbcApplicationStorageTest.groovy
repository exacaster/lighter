package com.exacaster.lighter.storage.jdbc

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationType
import com.exacaster.lighter.storage.ApplicationAlreadyExistsException
import com.exacaster.lighter.storage.SortOrder
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import spock.lang.Specification

import java.time.LocalDateTime

import static com.exacaster.lighter.test.Factories.newApplication
import static com.exacaster.lighter.test.Factories.newPermanentSession
import static com.exacaster.lighter.test.Factories.newSession
import static com.exacaster.lighter.test.Factories.submitParams

@MicronautTest
class JdbcApplicationStorageTest extends Specification {

    @Inject
    JdbcApplicationStorage storage

    @Inject
    Jdbi jdbi

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0)

    // A null priority leaves setPriority uncalled, reproducing a batch submitted before priorities existed.
    private static waitingBatch(String id, Integer priority, LocalDateTime createdAt) {
        var builder = ApplicationBuilder.builder()
                .setId(id)
                .setType(ApplicationType.BATCH)
                .setState(ApplicationState.NOT_STARTED)
                .setSubmitParams(submitParams())
                .setCreatedAt(createdAt)
        if (priority != null) {
            builder.setPriority(priority)
        }
        builder.build()
    }

    private storedSubmitParams(String id) {
        jdbi.withHandle { handle ->
            handle.createQuery("SELECT submit_params FROM application WHERE id=:id")
                    .bind("id", id)
                    .mapTo(String)
                    .one()
        }
    }

    private pickup(Integer limit = 10) {
        storage.findPrioritizedApplicationsByStates(ApplicationType.BATCH, [ApplicationState.NOT_STARTED], limit)
                .collect { it.id }
    }

    def "works"() {
        given:
        def app = newApplication()

        when: "fetching apps"
        def apps = storage.findApplications(EnumSet.of(ApplicationType.BATCH), 0, 10)

        then: "returns apps"
        apps.isEmpty()

        when: "saving app"
        def saved = storage.saveApplication(app)

        then: "returns saved"
        saved.id == app.id
        saved.createdAt == app.createdAt

        when: "saving updated"
        saved = storage.saveApplication(ApplicationBuilder.builder(saved).setState(ApplicationState.ERROR).build())

        then: "returns saved"
        saved.state == ApplicationState.ERROR

        when: "fetching apps"
        apps = storage.findApplications(EnumSet.of(ApplicationType.BATCH), 0, 10)

        then: "returns apps"
        apps.size() == 1
        apps.get(0).id == saved.getId()
        apps.get(0).state == saved.getState()

        when: "fetch by state"
        apps = storage.findApplicationsByStates(ApplicationType.BATCH, [ApplicationState.ERROR], SortOrder.DESC, 0, 10)

        then: "returns apps"
        apps.size() == 1
        apps.get(0).id == saved.getId()

        when: "fetch by missing state"
        apps = storage.findApplicationsByStates(ApplicationType.BATCH, [ApplicationState.SHUTTING_DOWN], SortOrder.DESC, 0, 1)

        then: "returns empty"
        apps.isEmpty()
    }

    def "handles soft deletes"() {
        given:
        def savedPermanentSession = storage.saveApplication(newPermanentSession())
        def savedRegularSession = storage.saveApplication(newSession())

        when: "deleting"
        storage.deleteApplication(savedPermanentSession.id)

        then: "fetching apps ignores soft deleted ones"
        storage.findApplicationsByStates(ApplicationType.PERMANENT_SESSION, [savedPermanentSession.state], SortOrder.DESC, 0, 10).size() == 0
        storage.findApplication(savedPermanentSession.id) == Optional.empty()
        storage.findApplication(savedRegularSession.id) != Optional.empty()
    }

    def "findAllPermanentSessions returns safe deleted"() {
        given:
        def savedPermanentSession = storage.saveApplication(newPermanentSession())

        when: "deleting"
        storage.deleteApplication(savedPermanentSession.id)

        then: "fetching apps ignores soft deleted ones"
        storage.findAllApplications(ApplicationType.PERMANENT_SESSION).any { it.id == savedPermanentSession.id }
        storage.findApplications(EnumSet.of(ApplicationType.PERMANENT_SESSION, ApplicationType.SESSION), 0, 10).every { it.id != savedPermanentSession.id }
    }


    def "prioritized pickup takes highest priority first and keeps FCFS within a priority"() {
        given: "waiting batches submitted in a known order, with mixed priorities"
        storage.saveApplication(waitingBatch("normal-first", 0, BASE_TIME))
        storage.saveApplication(waitingBatch("normal-second", 0, BASE_TIME.plusMinutes(1)))
        storage.saveApplication(waitingBatch("high-but-last-submitted", 5, BASE_TIME.plusMinutes(2)))
        storage.saveApplication(waitingBatch("low-but-first-submitted", -5, BASE_TIME.minusMinutes(1)))

        expect: "high jumps the queue, equal priorities stay in submission order, negative sinks below normal"
        pickup() == ["high-but-last-submitted", "normal-first", "normal-second", "low-but-first-submitted"]
    }

    def "prioritized pickup preserves submission order for every equal priority"() {
        given: "three waiting batches all sharing one priority"
        storage.saveApplication(waitingBatch("third", priority, BASE_TIME.plusMinutes(2)))
        storage.saveApplication(waitingBatch("first", priority, BASE_TIME))
        storage.saveApplication(waitingBatch("second", priority, BASE_TIME.plusMinutes(1)))

        expect: "created_at ASC decides, regardless of which priority they share"
        pickup() == ["first", "second", "third"]

        where:
        priority << [-3, 0, 1, 42]
    }

    def "batches with no priority set are picked in pure created_at order"() {
        given: "batches saved without ever touching priority, as an existing client submits them"
        storage.saveApplication(waitingBatch("b", null, BASE_TIME.plusMinutes(1)))
        storage.saveApplication(waitingBatch("a", null, BASE_TIME))
        storage.saveApplication(waitingBatch("c", null, BASE_TIME.plusMinutes(2)))

        expect: "identical to today's created_at ASC pickup, and the column defaulted to 0"
        pickup() == ["a", "b", "c"]
        storage.findPrioritizedApplicationsByStates(ApplicationType.BATCH, [ApplicationState.NOT_STARTED], 10)
                .every { it.priority == 0 }
    }

    def "prioritized pickup honours the limit and ignores other types and states"() {
        given:
        storage.saveApplication(waitingBatch("high", 9, BASE_TIME.plusMinutes(5)))
        storage.saveApplication(waitingBatch("normal", 0, BASE_TIME))
        storage.saveApplication(ApplicationBuilder.builder(waitingBatch("running", 9, BASE_TIME))
                .setState(ApplicationState.BUSY).build())
        storage.saveApplication(newSession())

        expect: "only waiting batches, highest priority first, capped at the requested slot count"
        pickup(1) == ["high"]
        pickup() == ["high", "normal"]
    }

    def "updatePriority applies to a waiting batch"() {
        given:
        storage.saveApplication(waitingBatch("waiting", 0, BASE_TIME))

        when:
        storage.updatePriority("waiting", ApplicationType.BATCH, 8)

        then:
        storage.findApplication("waiting").get().priority == 8
    }

    def "updatePriority does not touch a batch that already left NOT_STARTED"() {
        given: "a batch that the scheduler already picked up"
        storage.saveApplication(ApplicationBuilder.builder(waitingBatch("started", 1, BASE_TIME))
                .setState(state).build())

        when:
        storage.updatePriority("started", ApplicationType.BATCH, 99)

        then: "the database guard rejects the write, so the stored priority is unchanged"
        storage.findApplication("started").get().priority == 1

        where:
        state << [ApplicationState.STARTING, ApplicationState.BUSY, ApplicationState.SUCCESS, ApplicationState.ERROR]
    }

    def "updatePriority does not resurrect a soft deleted batch"() {
        given:
        storage.saveApplication(waitingBatch("gone", 1, BASE_TIME))
        storage.deleteApplication("gone")

        when:
        storage.updatePriority("gone", ApplicationType.BATCH, 99)

        then:
        storage.findApplication("gone") == Optional.empty()
    }

    def "updatePriority does not touch an application of another type"() {
        given: "a waiting session, which shares the application table with batches"
        def session = storage.saveApplication(newSession(ApplicationState.NOT_STARTED))

        when:
        storage.updatePriority(session.id, ApplicationType.BATCH, 99)

        then: "the type guard rejects the write, so the session keeps its priority"
        storage.findApplication(session.id).get().priority == 0
    }

    def "keeps priority out of the stored submit params"() {
        given: "a batch stored with a priority"
        storage.saveApplication(waitingBatch("no-copy", 5, BASE_TIME))

        expect: "the priority lives only in its own column, so there is no copy that can go stale"
        !storedSubmitParams("no-copy").contains("priority")
        storage.findApplication("no-copy").get().priority == 5

        when: "the priority is later changed"
        storage.updatePriority("no-copy", ApplicationType.BATCH, 9)

        then: "the submit params are still free of it"
        !storedSubmitParams("no-copy").contains("priority")
        storage.findApplication("no-copy").get().priority == 9
    }

    def "the shared fetch query still orders by created_at only"() {
        given: "a high priority batch submitted last"
        storage.saveApplication(waitingBatch("older-normal", 0, BASE_TIME))
        storage.saveApplication(waitingBatch("newer-high", 9, BASE_TIME.plusMinutes(1)))

        expect: "priority does not leak into the user-facing list or the session pickup ordering"
        storage.findApplicationsByStates(ApplicationType.BATCH, [ApplicationState.NOT_STARTED], SortOrder.DESC, 0, 10)
                .collect { it.id } == ["newer-high", "older-normal"]
        storage.findApplicationsByStates(ApplicationType.BATCH, [ApplicationState.NOT_STARTED], SortOrder.ASC, 0, 10)
                .collect { it.id } == ["older-normal", "newer-high"]
    }

    def "insert"() {
        given:
        def savedSession =  storage.saveApplication(newPermanentSession())

        when: "inserting a session with id that already exists"
        storage.insertApplication(savedSession)

        then:
        thrown ApplicationAlreadyExistsException
    }
}
