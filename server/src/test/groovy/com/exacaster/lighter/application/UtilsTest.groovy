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
}
