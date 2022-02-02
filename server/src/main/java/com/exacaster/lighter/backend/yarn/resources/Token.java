package com.exacaster.lighter.backend.yarn.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {
    private final TokenWrapper tokenWrapper;

    @JsonCreator
    public Token(@Nullable @JsonProperty("Token") TokenWrapper tokenWrapper) {
        this.tokenWrapper = tokenWrapper;
    }

    public TokenWrapper getTokenWrapper() {
        return tokenWrapper;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenWrapper {
        private final String token;

        @JsonCreator
        public TokenWrapper(@Nullable @JsonProperty("urlString") String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
}
