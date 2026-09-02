package com.exacaster.lighter.rest;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotNull;

@Introspected
public record PriorityParams(@NotNull Integer priority) {

}
