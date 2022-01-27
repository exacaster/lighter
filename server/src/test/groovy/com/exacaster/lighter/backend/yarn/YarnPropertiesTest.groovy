package com.exacaster.lighter.backend.yarn

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Subject

@MicronautTest
@Property(name="lighter.yarn.enabled", value="true")
@Property(name="lighter.yarn.url", value="http://localhost")
@Property(name="lighter.kubernetes.enabled", value="false")
class YarnPropertiesTest extends Specification {
    @Subject
    @Inject
    YarnProperties yarnProperties

    def "binds properties"() {
        expect:
        yarnProperties != null
    }

}
