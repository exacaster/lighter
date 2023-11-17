package com.exacaster.lighter.rest;

import com.exacaster.lighter.application.SubmitParams;
import io.micronaut.core.annotation.Introspected;

import java.util.List;
import java.util.Map;

@Introspected
public class SessionParameters extends SubmitParams {
    private final Boolean permanent;

    public SessionParameters(Boolean permanent, String name, String file, String master, String mainClass, Integer numExecutors, Integer executorCores,
                             String executorMemory, Integer driverCores, String driverMemory, List<String> args,
                             List<String> pyFiles, List<String> files, List<String> jars, List<String> archives, Map<String, String> conf) {
        super(name, file, master, mainClass, numExecutors, executorCores, executorMemory, driverCores, driverMemory,
                args, pyFiles, files, jars, archives, conf);
        this.permanent = permanent;
    }

    public Boolean getPermanent() {
        return permanent;
    }

}
