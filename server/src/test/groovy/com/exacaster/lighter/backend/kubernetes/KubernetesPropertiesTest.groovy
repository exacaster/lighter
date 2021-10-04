package com.exacaster.lighter.backend.kubernetes

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Subject

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
