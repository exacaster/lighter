package com.exacaster.lighter.spark;

import static java.util.Optional.ofNullable;

import io.micronaut.core.annotation.Introspected;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Introspected
@RecordBuilder
public record SubmitParams(@NotBlank String name,
                           String master,
                           String mainClass,
                           Integer numExecutors,
                           Integer executorCores,
                           String executorMemory,
                           Integer driverCores,
                           String driverMemory,
                           List<String> args,
                           List<String> pyFiles,
                           List<String> files,
                           List<String> jars,
                           Map<String, String> conf) {

    public List<String> args() {
        return ofNullable(args).orElse(List.of());
    }

    public List<String> pyFiles() {
        return ofNullable(pyFiles).orElse(List.of());
    }

    public List<String> files() {
        return ofNullable(files).orElse(List.of());
    }

    public List<String> jars() {
        return ofNullable(jars).orElse(List.of());
    }

    public Map<String, String> conf() {
        return ofNullable(conf).orElse(Map.of());
    }
}
