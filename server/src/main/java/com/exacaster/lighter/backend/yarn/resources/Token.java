package com.exacaster.lighter.backend.yarn.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

@Introspected
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {
    private final String token;

    @JsonCreator
    public Token(@Nullable @JsonProperty("token") String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
