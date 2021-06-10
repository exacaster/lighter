package com.exacaster.lighter.application.batch

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.spark.SubmitParams
import com.exacaster.lighter.storage.InMemoryStorage
import spock.lang.Specification
import spock.lang.Subject

class BatchServiceTest extends Specification {
    @Subject
    BatchService service = new BatchService(new InMemoryStorage(100, 1), Mock(Backend))

    def "BatchService"() {
        given:
        def params = new SubmitParams(
                "application1",
                "",
                "",
                "",
                0,
                null, null, null, null, null, null, null, null, null
        )

        when: "creating application"
        def result = service.create(params)

        then: "returns batch"
        result.getId() != null
        result.getSubmitParams().getName() == params.getName()

        when: "updating"
        result = service.update(ApplicationBuilder.builder(result).setState(ApplicationState.DEAD).build())

        then: "returns updated"
        result.getState() == ApplicationState.DEAD

        when: "fetching list"
        def resultList = service.fetch(0, 1)

        then: "returns list"
        resultList.size() == 1

        when: "fetch by status"
        resultList = service.fetchByState(ApplicationState.DEAD)

        then: "returns list"
        resultList.size() == 1

        when: "fetch by missing status"
        resultList = service.fetchByState(ApplicationState.SUCCESS)

        then: "returns empty list"
        resultList.isEmpty()

        when: "delete"
        service.deleteOne(result.getId())

        then: "removes"
        noExceptionThrown()

        and: "list is empty"
        service.fetch(0, 1).isEmpty()
    }
}
