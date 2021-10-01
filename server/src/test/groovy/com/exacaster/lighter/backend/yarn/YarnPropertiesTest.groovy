package com.exacaster.lighter.backend.yarn

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Subject

import javax.inject.Inject

@MicronautTest
@Property(name="lighter.yarn.enabled", value="true")
class YarnPropertiesTest extends Specification {
    @Subject
    @Inject
    YarnProperties yarnProperties

    def "binds properties"() {
        expect:
        yarnProperties != null
    }

}
