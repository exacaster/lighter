package com.exacaster.lighter.rest;

import com.exacaster.lighter.configuration.AppConfiguration;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

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
