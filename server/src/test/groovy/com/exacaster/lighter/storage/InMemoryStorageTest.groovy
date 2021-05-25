package com.exacaster.lighter.storage

import com.exacaster.lighter.batch.Batch
import com.exacaster.lighter.batch.BatchBuilder
import com.exacaster.lighter.batch.BatchState
import spock.lang.Specification
import spock.lang.Subject

class InMemoryStorageTest extends Specification {
    @Subject
    Storage storage = new InMemoryStorage()

    def "storage"() {
        given:
        def batch = BatchBuilder.builder()
                .id("1")
                .appId("app_123")
                .state(BatchState.error)
                .build()

        when: "storing entity"
        def result = storage.storeEntity(batch)

        then: "returns stored entity"
        result.appId() == "app_123"

        when: "searching by id"
        def findResult = storage.findEntity(result.id(), Batch.class)

        then: "returns by id"
        findResult.get().appId() == "app_123"

        when: "searching by wrong id"
        findResult = storage.findEntity("unknown", Batch.class)

        then: "returns empty"
        findResult.isEmpty()

        when: "searching by status"
        def statusResult = storage.findManyByField("state", Batch.class, BatchState.error)

        then: "returns results"
        statusResult.get(0).appId() == "app_123"

        when: "searching by not existing status"
        statusResult = storage.findManyByField("state", Batch.class, BatchState.killed)

        then: "returns empty list"
        statusResult.isEmpty()

    }
}
