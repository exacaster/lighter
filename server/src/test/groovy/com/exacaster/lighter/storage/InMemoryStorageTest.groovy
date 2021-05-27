package com.exacaster.lighter.storage

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationType
import spock.lang.Specification
import spock.lang.Subject

class InMemoryStorageTest extends Specification {
    @Subject
    Storage storage = new InMemoryStorage(10, 10)

    def "storage"() {
        given:
        def batch = ApplicationBuilder.builder()
                .id("1")
                .appId("app_123")
                .state(ApplicationState.ERROR)
                .type(ApplicationType.BATCH)
                .build()

        when: "storing entity"
        def result = storage.saveApplication(batch)

        then: "returns stored entity"
        result.appId() == "app_123"

        when: "searching by id"
        def findResult = storage.findApplication(result.id())

        then: "returns by id"
        findResult.get().appId() == "app_123"

        when: "searching by wrong id"
        findResult = storage.findApplication("unknown")

        then: "returns empty"
        findResult.isEmpty()

        when: "searching by status"
        def statusResult = storage.findApplicationsByStates(ApplicationType.BATCH, [ApplicationState.ERROR])

        then: "returns results"
        statusResult.get(0).appId() == "app_123"

        when: "searching by not existing status"
        statusResult = storage.findApplicationsByStates(ApplicationType.BATCH, [ApplicationState.KILLED])

        then: "returns empty list"
        statusResult.isEmpty()

    }
}
