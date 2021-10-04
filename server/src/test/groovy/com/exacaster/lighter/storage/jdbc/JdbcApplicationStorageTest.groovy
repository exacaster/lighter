package com.exacaster.lighter.storage.jdbc

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationType
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import javax.transaction.Transactional
import spock.lang.Specification

import static com.exacaster.lighter.test.Factories.newApplication

@MicronautTest
@Transactional
class JdbcApplicationStorageTest extends Specification {

    @Inject
    JdbcApplicationStorage storage

    def "works"() {
        given:
        def app = newApplication()

        when: "fetching apps"
        def apps = storage.findApplications(ApplicationType.BATCH, 0, 10)

        then: "returns apps"
        apps.isEmpty()

        when: "saving app"
        def saved = storage.saveApplication(app)

        then: "returns saved"
        saved.id == app.id

        when: "saving updated"
        saved = storage.saveApplication(ApplicationBuilder.builder(saved).setState(ApplicationState.ERROR).build())

        then: "returns saved"
        saved.state == ApplicationState.ERROR

        when: "fetching apps"
        apps = storage.findApplications(ApplicationType.BATCH, 0, 10)

        then: "returns apps"
        apps.size() == 1
        apps.get(0).id == saved.getId()
        apps.get(0).state == saved.getState()

        when: "fetch by state"
        apps = storage.findApplicationsByStates(ApplicationType.BATCH, [ApplicationState.ERROR], 10)

        then: "returns apps"
        apps.size() == 1
        apps.get(0).id == saved.getId()

        when: "fetch by missing state"
        apps = storage.findApplicationsByStates(ApplicationType.BATCH, [ApplicationState.SHUTTING_DOWN], 1)

        then: "returns empty"
        apps.isEmpty()
    }
}
