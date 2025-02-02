package com.exacaster.lighter.application

import com.exacaster.lighter.application.sessions.SessionUtils
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.log.Log
import com.exacaster.lighter.log.LogService
import com.exacaster.lighter.test.InMemoryStorage
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime
import java.util.function.Function

import static com.exacaster.lighter.test.Factories.appConfiguration
import static com.exacaster.lighter.test.Factories.newSession

class ApplicationStatusHandlerTest extends Specification {
    def storage = new InMemoryStorage()

    Backend backend = Mock() {
        getLogs(_) >> Optional.of(new Log("", "log"))
    }

    LogService logService = Mock()

    @Subject
    ApplicationStatusHandler handler = new ApplicationStatusHandler(storage, backend, logService, appConfiguration())

    def cleanup() {
        storage.cleanup()
    }

    def "handles state with transformation"() {
        given:
        def transformation = new Function<ApplicationInfo, ApplicationInfo>() {
            @Override
            ApplicationInfo apply(ApplicationInfo info) {
                return new ApplicationInfo(SessionUtils.adjustState(false, info.state()), info.applicationId())
            }
        }

        def application = newSession(ApplicationState.IDLE)
        application = storage.saveApplication(application)

        when:
        1 * backend.getInfo(application) >> Optional.of(new ApplicationInfo(ApplicationState.BUSY, "we"))

        handler.processApplicationRunning(application, transformation)
        def updated = storage.findApplication(application.id).get()

        then:
        updated.state == ApplicationState.IDLE
        updated.appId == "we"

        when:
        1 * backend.getInfo(application) >> Optional.of(new ApplicationInfo(ApplicationState.KILLED, "we"))
        handler.processApplicationRunning(application, transformation)
        updated = storage.findApplication(application.id).get()

        then:
        updated.state == ApplicationState.KILLED
    }

    def "handles error"() {
        given:
        def application = newSession()
        application = storage.saveApplication(application)

        when:
        1 * backend.getInfo(application) >> Optional.of(new ApplicationInfo(ApplicationState.BUSY, "we"))
        handler.processApplicationError(application, new RuntimeException("Foooo"))
        def updated = storage.findApplication(application.id).get()

        then:
        updated.state == ApplicationState.ERROR
        updated.appId == "we"
        1 * logService.save(_)
    }

    def "handles starting"() {
        given:
        def application = newSession(ApplicationState.IDLE)
        application = storage.saveApplication(application)

        when:
        handler.processApplicationStarting(application)
        def updated = storage.findApplication(application.id).get()

        then:
        updated.state == ApplicationState.STARTING
    }

    def "handle running"() {
        given:
        def application = newSession(ApplicationState.STARTING)
        application = storage.saveApplication(application)

        when:
        1 * backend.getInfo(application) >> Optional.of(new ApplicationInfo(ApplicationState.BUSY, "we"))
        def returnedState = handler.processApplicationRunning(application)
        def updated = storage.findApplication(application.id).get()

        then:
        returnedState == updated.state
        updated.state == ApplicationState.BUSY

        when:
        1 * backend.getInfo(updated) >> Optional.empty()
        returnedState = handler.processApplicationRunning(updated)
        updated = storage.findApplication(application.id).get()

        then:
        returnedState == updated.state
        updated.state == ApplicationState.BUSY

        when: "No info for more than 30 mins"
        updated = storage.saveApplication(ApplicationBuilder.builder(updated).setContactedAt(LocalDateTime.now().minusHours(1)).build())
        1 * backend.getInfo(updated) >> Optional.empty()
        returnedState = handler.processApplicationRunning(updated)
        updated = storage.findApplication(application.id).get()

        then:
        returnedState == updated.state
        updated.state == ApplicationState.ERROR
    }
}
