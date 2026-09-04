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
        app.priority == 0
    }

    def "copies every field it carries when rebuilding from an existing application"() {
        given: "an application with every field set away from its default, so a dropped field cannot pass by accident"
        def params = submitParams()
        def createdAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0)
        def contactedAt = LocalDateTime.of(2024, 1, 2, 13, 30, 0)
        def finishedAt = LocalDateTime.of(2024, 1, 3, 14, 45, 0)
        def app = ApplicationBuilder.builder()
                .setId("app-1")
                .setType(ApplicationType.PERMANENT_SESSION)
                .setState(ApplicationState.BUSY)
                .setAppId("spark-app-1")
                .setAppInfo("info")
                .setSubmitParams(params)
                .setPriority(7)
                .setCreatedAt(createdAt)
                .setContactedAt(contactedAt)
                .setFinishedAt(finishedAt)
                .build()

        when:
        def copy = ApplicationBuilder.builder(app).build()

        then: "all ten copied fields survive, with the three timestamps distinct so a crossed wire shows up"
        copy.id == "app-1"
        copy.type == ApplicationType.PERMANENT_SESSION
        copy.state == ApplicationState.BUSY
        copy.appId == "spark-app-1"
        copy.appInfo == "info"
        copy.submitParams == params
        copy.priority == 7
        copy.createdAt == createdAt
        copy.contactedAt == contactedAt
        copy.finishedAt == finishedAt
    }

    def "does not carry deleted when rebuilding, unlike every other field"() {
        given: "a soft deleted application"
        def app = ApplicationBuilder.builder()
                .setId("app-1")
                .setDeleted(true)
                .build()

        when:
        def copy = ApplicationBuilder.builder(app).build()

        then: "the source stays deleted but the rebuilt copy comes back not deleted"
        app.deleted
        !copy.deleted
    }

    def "keeps copied fields when a rebuild overrides one of them"() {
        given: "a prioritized waiting batch, as the status tracker finds it"
        def app = ApplicationBuilder.builder()
                .setId("1")
                .setType(ApplicationType.BATCH)
                .setState(ApplicationState.NOT_STARTED)
                .setSubmitParams(submitParams())
                .setPriority(7)
                .setCreatedAt(LocalDateTime.MAX)
                .build()

        when: "rebuilding it to record a new state, as every status update does"
        def updated = ApplicationBuilder.builder(app).setState(ApplicationState.BUSY).build()

        then: "the override wins and the untouched fields still survive"
        updated.state == ApplicationState.BUSY
        updated.priority == 7
        updated.id == "1"
        updated.type == ApplicationType.BATCH
    }
}
