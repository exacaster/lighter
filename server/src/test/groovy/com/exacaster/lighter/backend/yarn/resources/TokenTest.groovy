package com.exacaster.lighter.backend.yarn.resources

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

class TokenTest extends Specification {

    def "deserialize token"() {
        given:
        def token = '''
            {
              "Token": {
                "urlString": "KQAJZXhhY2FzdGVyB2xpZ2h0ZXIAigF-uY9SkIoBft2b1pCNODxOjgJxFDKUzUCmoGSSlWPLpH4UD2uHgDyXEldFQkhERlMgZGVsZWdhdGlvbhIxOTIuMTY4LjQwLjQxOjgwMjA"
              }
            }
        '''

        when:
        def result = new ObjectMapper().readValue(token, Token.class)

        then:
        result.getTokenWrapper() != null
        result.getTokenWrapper().getToken() != null
    }
}
