package com.exacaster.lighter.rest

import com.exacaster.lighter.application.SubmitParams
import spock.lang.Specification

class BatchParamsTest extends Specification {

    def "defaults priority to 0 when the caller sends none"() {
        expect:
        batchParams(null).priority == 0
    }

    def "keeps the priority the caller sent"() {
        expect:
        batchParams(priority).priority == priority

        where:
        priority << [-7, 0, 1, 5000]
    }

    def "converts to a plain SubmitParams, which is what keeps priority out of submit_params"() {
        given:
        def params = batchParams(5)

        when:
        def submitParams = params.toSubmitParams()

        then: "returning the subclass itself would serialize priority into the persisted submit params"
        submitParams.class == SubmitParams
    }

    def "carries every submit field through the conversion unchanged"() {
        given:
        def params = batchParams(5)

        when:
        def submitParams = params.toSubmitParams()

        then:
        submitParams.name == params.name
        submitParams.file == params.file
        submitParams.args == params.args
        submitParams.conf == params.conf
        submitParams.numExecutors == params.numExecutors
        submitParams.executorCores == params.executorCores
        submitParams.executorMemory == params.executorMemory
        submitParams.driverCores == params.driverCores
        submitParams.driverMemory == params.driverMemory
        submitParams.pyFiles == params.pyFiles
        submitParams.files == params.files
        submitParams.jars == params.jars
        submitParams.archives == params.archives
    }

    private static batchParams(Integer priority) {
        new BatchParams(priority, "name", "file.py", null, null, null, null, null, null, null,
                ["arg"], null, null, null, null, ["spark.conf": "value"])
    }
}
