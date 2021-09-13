package com.exacaster.lighter.application.sessions

import spock.lang.Specification
import spock.lang.Unroll

import static com.exacaster.lighter.application.ApplicationState.BUSY
import static com.exacaster.lighter.application.ApplicationState.IDLE
import static com.exacaster.lighter.application.ApplicationState.DEAD

class SessionUtilsTest extends Specification {
    @Unroll
    def "converts #originalState to #expectedState when hasWaitingStatements: #hasWaitingStatements"() {
        expect:
        SessionUtils.adjustState(!hasWaitingStatements, originalState) == expectedState

        where:
        originalState | hasWaitingStatements | expectedState
        BUSY          | false                | IDLE
        BUSY          | true                 | BUSY
        DEAD          | false                | DEAD

    }
}
