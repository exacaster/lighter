package com.exacaster.lighter.application

import spock.lang.Specification

class UtilsTest extends Specification {
    def "merges two maps"() {
        given:
        def currentMap = [foo: "bar"]
        def mapToMerge = [foo: "baz", bar: "foo"]

        when:
        def result = Utils.merge(currentMap, mapToMerge)

        then:
        result == [bar: "foo", foo: "bar"]
    }

    def "redacts sensitive values using default pattern"() {
        given:
        def conf = [
                "spring.lighter.password": "foo",
                "spark.log.level": "ALL",
        ]
        when:
        def redacted = Utils.redact(conf)
        then:
        redacted == [
                "spring.lighter.password": "[redacted]",
                "spark.log.level": "ALL",
        ]
    }
}
