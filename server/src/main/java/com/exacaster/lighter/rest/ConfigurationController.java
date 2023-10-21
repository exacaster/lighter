package com.exacaster.lighter.rest;

import com.exacaster.lighter.configuration.AppConfiguration;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

@Tags(@Tag(name = "Configuration"))
@Controller("/lighter/api/configuration")
public class ConfigurationController {

    private final AppConfiguration appConfiguration;

    public ConfigurationController(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    @Get
    public AppConfiguration get() {
        return appConfiguration;
    }
}
