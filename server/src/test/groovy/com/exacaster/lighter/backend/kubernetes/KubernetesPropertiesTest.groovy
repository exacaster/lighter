package com.exacaster.lighter.backend.kubernetes

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Subject

import javax.inject.Inject

@MicronautTest
class KubernetesPropertiesTest extends Specification {
    @Subject
    @Inject
    KubernetesProperties props

    def "binds properties"() {
        expect:
        props.namespace == "spark"
    }
}
