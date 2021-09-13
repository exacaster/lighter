package com.exacaster.lighter.application.sessions

import com.exacaster.lighter.application.sessions.processors.Output
import spock.lang.Specification

import static com.exacaster.lighter.test.Factories.statement

class StatementTest extends Specification {
    def "constructs new statements"() {
        given:
        def st = statement()

        when:
        def newStatement = st.withIdAndState("newId", "error")

        then:
        newStatement.code == st.code
        newStatement.output == st.output
        newStatement.id != st.id
        newStatement.id == "newId"
        newStatement.state == "error"

        when:
        newStatement = st.withStateAndOutput("error", new Output("ok", 3, [foo: 1]))

        then:
        newStatement.id == st.id
        newStatement.state == "error"
        newStatement.output.executionCount == 3
    }
}
