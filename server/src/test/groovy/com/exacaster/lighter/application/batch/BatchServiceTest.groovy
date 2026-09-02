package com.exacaster.lighter.application.batch

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.storage.SortOrder
import com.exacaster.lighter.test.InMemoryStorage
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

import static com.exacaster.lighter.test.Factories.submitParams

class BatchServiceTest extends Specification {
    @Subject
    BatchService service = new BatchService(new InMemoryStorage(), Mock(Backend))

    def "BatchService"() {
        given:
        def params = submitParams()

        when: "creating application"
        def result = service.create(params, 0)

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
        resultList = service.fetchByState(ApplicationState.DEAD, SortOrder.DESC, 0, 10)

        then: "returns list"
        resultList.size() == 1

        when: "fetch by missing status"
        resultList = service.fetchByState(ApplicationState.SUCCESS, SortOrder.DESC, 0, 10)

        then: "returns empty list"
        resultList.isEmpty()

        when: "delete"
        service.deleteOne(result.getId())

        then: "removes"
        noExceptionThrown()

        and: "list is empty"
        service.fetch(0, 1).isEmpty()
    }

    def "stores the priority it is handed"() {
        when:
        def result = service.create(submitParams(), priority)

        then:
        result.priority == priority
        service.fetchOne(result.id).get().priority == priority

        where:
        priority << [-3, 0, 5, 42]
    }

    def "picks waiting batches by priority, keeping submission order within a priority"() {
        given: "waiting batches with explicit submission times, so the assertion cannot depend on clock resolution"
        def base = LocalDateTime.of(2024, 1, 1, 12, 0, 0)
        def normalPriorityFirst = createdAt(service.create(submitParams(), 0), base)
        def normalPrioritySecond = createdAt(service.create(submitParams(), 0), base.plusMinutes(1))
        def highPriority = createdAt(service.create(submitParams(), 5), base.plusMinutes(2))

        when:
        def picked = service.fetchByStatePrioritized(ApplicationState.NOT_STARTED, 10)

        then:
        picked.collect { it.id } == [highPriority.id, normalPriorityFirst.id, normalPrioritySecond.id]
    }

    def "updates the priority of a waiting batch"() {
        given:
        def created = service.create(submitParams(), 0)

        when:
        def result = service.updatePriority(created.id, 8)

        then:
        result.get().priority == 8
        service.fetchOne(created.id).get().priority == 8
    }

    def "leaves an already started batch untouched instead of failing"() {
        given:
        def created = service.create(submitParams(), 2)
        service.update(ApplicationBuilder.builder(created).setState(ApplicationState.BUSY).build())

        when:
        def result = service.updatePriority(created.id, 99)

        then: "the current batch comes back, unchanged"
        result.isPresent()
        result.get().priority == 2
    }

    def "returns empty for an unknown batch"() {
        expect:
        service.updatePriority("no-such-batch", 1).isEmpty()
    }

    private createdAt(application, LocalDateTime createdAt) {
        service.update(ApplicationBuilder.builder(application).setCreatedAt(createdAt).build())
    }
}
