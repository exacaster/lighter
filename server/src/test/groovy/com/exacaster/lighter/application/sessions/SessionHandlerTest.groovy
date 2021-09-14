package com.exacaster.lighter.application.sessions

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.configuration.AppConfiguration
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

import static com.exacaster.lighter.test.Factories.appConfiguration
import static com.exacaster.lighter.test.Factories.newSession

class SessionHandlerTest extends Specification {

    SessionService service = Mock()

    Backend backend = Mock()

    AppConfiguration conf = appConfiguration()

    @Subject
    SessionHandler handler = new SessionHandler(service, backend, null, null, conf)

    def "kills timeouted sessions"() {
        given:
        def oldSession = ApplicationBuilder.builder(newSession())
                .setContactedAt(LocalDateTime.now().minusMinutes(conf.sessionConfiguration.timeoutMinutes + 1))
                .build()
        def newSession = ApplicationBuilder.builder(newSession())
                .setContactedAt(LocalDateTime.now())
                .build()
        1 * service.fetchRunning() >> [
                oldSession,
                newSession
        ]

        when:
        handler.handleTimeout()

        then:
        1 * service.killOne(oldSession)
        0 * service.killOne(newSession)
    }
}
