package com.exacaster.lighter.rest;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.SubmitParams;
import com.exacaster.lighter.application.sessions.SessionService;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Status;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import javax.validation.Valid;

@Validated
@Tags(@Tag(name = "Sessions"))
@Controller("/lighter/api/permanentSessions")
public class PermanentSessionController {

    private final SessionService sessionService;

    public PermanentSessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Put("/{id}")
    @Status(HttpStatus.CREATED)
    public Application create(@PathVariable String id, @Valid @Body SubmitParams params) {
        return sessionService.createPermanentSession(id, params);
    }


    @Delete("/{id}")
    public void delete(@PathVariable String id) {
        //TODO to implement
    }


}
