package com.exacaster.lighter.batch

import com.exacaster.lighter.spark.SubmitParamsBuilder
import com.exacaster.lighter.storage.InMemoryStorage
import spock.lang.Specification
import spock.lang.Subject

class BatchServiceTest extends Specification {
    @Subject
    BatchService service = new BatchService(new InMemoryStorage())

    def "BatchService"() {
        given:
        def params = SubmitParamsBuilder.builder()
            .name("application1")
            .build()

        when: "creating application"
        def result = service.create(params)

        then: "returns batch"
        result.id() != null
        result.submitParams().name() == params.name()

        when: "updating"
        result = service.update(BatchBuilder.builder(result).state(BatchState.DEAD).build())

        then: "returns updated"
        result.state() == BatchState.DEAD

        when: "fetching list"
        def resultList = service.fetch(0, 1)

        then: "returns list"
        resultList.size() == 1

        when: "fetch by status"
        resultList = service.fetchByState(BatchState.DEAD)

        then: "returns list"
        resultList.size() == 1

        when: "fetch by missing status"
        resultList = service.fetchByState(BatchState.SUCCESS)

        then: "returns empty list"
        resultList.isEmpty()

        when: "delete"
        service.deleteOne(result.id())

        then: "removes"
        noExceptionThrown()

        and: "list is empty"
        service.fetch(0, 1).isEmpty()
    }
}
