
package com.exacaster.lighter.application

import spock.lang.Specification

import java.time.LocalDateTime

import static com.exacaster.lighter.test.Factories.submitParams

class ApplicationBuilderTest extends Specification {

    def "builds application"() {
        given:
        def params = submitParams()
        def builder = ApplicationBuilder.builder()

        when:
        def app = builder.setAppId("app123")
                .setType(ApplicationType.BATCH)
                .setState(ApplicationState.NOT_STARTED)
                .setAppInfo("info")
                .setCreatedAt(LocalDateTime.MAX)
                .setId("1")
                .setSubmitParams(params)
                .build()

        then:
        app.appId == "app123"
        app.type == ApplicationType.BATCH
        app.state == ApplicationState.NOT_STARTED
        app.appInfo == "info"
        app.id == "1"
        app.submitParams == params
    }
}
