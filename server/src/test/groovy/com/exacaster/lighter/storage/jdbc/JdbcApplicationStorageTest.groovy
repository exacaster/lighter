package com.exacaster.lighter.storage.jdbc

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationType
import com.exacaster.lighter.storage.ApplicationAlreadyExistsException
import com.exacaster.lighter.storage.SortOrder
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import javax.transaction.Transactional
import spock.lang.Specification

import static com.exacaster.lighter.test.Factories.newApplication
import static com.exacaster.lighter.test.Factories.newPermanentSession
import static com.exacaster.lighter.test.Factories.newSession

@MicronautTest
@Transactional
class JdbcApplicationStorageTest extends Specification {

    @Inject
    JdbcApplicationStorage storage

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
//        storage.findApplications(EnumSet.of(ApplicationType.PERMANENT_SESSION, ApplicationType.SESSION), 0, 10).size() == 1
        storage.findApplicationsByStates(ApplicationType.PERMANENT_SESSION, [savedPermanentSession.state], SortOrder.DESC, 0, 10).size() == 0
        storage.findApplication(savedPermanentSession.id) == Optional.empty()
        storage.findApplication(savedRegularSession.id) != Optional.empty()
    }

    //TODO this test won't work as keepPermanentSessions interferes with it.
//    def "findAllPermanentSessions returns safe deleted"() {
//        given:
//        def savedPermanentSession = storage.saveApplication(newPermanentSession())
//
//        when: "deleting"
//        storage.deleteApplication(savedPermanentSession.id)
//
//        then: "fetching apps ignores soft deleted ones"
//        storage.findAllPermanentSessions().size() == 1
//    }


    def "insert"() {
        given:
        def savedSession =  storage.saveApplication(newPermanentSession())

        when: "inserting a session with id that already exists"
        storage.insertApplication(savedSession)

        then:
        thrown ApplicationAlreadyExistsException
    }
}
