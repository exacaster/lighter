package com.exacaster.lighter.rest;

import com.exacaster.lighter.application.SubmitParams;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.format.MapFormat;

import java.util.List;
import java.util.Map;

import static io.micronaut.core.convert.format.MapFormat.MapTransformation.FLAT;
import static io.micronaut.core.naming.conventions.StringConvention.RAW;
import static java.util.Optional.ofNullable;

@Introspected
public class BatchParams extends SubmitParams {

    private final Integer priority;

    public BatchParams(
        @Nullable Integer priority,
        @Nullable String name,
        @Nullable String file,
        @Nullable String master,
        @Nullable String mainClass,
        @Nullable Integer numExecutors,
        @Nullable Integer executorCores,
        @Nullable String executorMemory,
        @Nullable Integer driverCores,
        @Nullable String driverMemory,
        @Nullable List<String> args,
        @Nullable List<String> pyFiles,
        @Nullable List<String> files,
        @Nullable List<String> jars,
        @Nullable List<String> archives,
        @MapFormat(transformation = FLAT, keyFormat = RAW)
        @Nullable Map<String, String> conf
    ) {
        super(
            name, file, master, mainClass, numExecutors, executorCores, executorMemory, driverCores, driverMemory,
            args, pyFiles, files, jars, archives, conf
        );
        this.priority = priority;
    }

    public Integer getPriority() {
        return ofNullable(priority).orElse(0);
    }

    public SubmitParams toSubmitParams() {
        return new SubmitParams(
            getName(), getFile(), getMaster(), getMainClass(), getNumExecutors(), getExecutorCores(),
            getExecutorMemory(), getDriverCores(), getDriverMemory(), getArgs(), getPyFiles(), getFiles(), getJars(),
            getArchives(), getConf()
        );
    }
}
