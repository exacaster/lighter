package com.exacaster.lighter.backend.yarn.resources;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class Token {
    private final String token;

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
