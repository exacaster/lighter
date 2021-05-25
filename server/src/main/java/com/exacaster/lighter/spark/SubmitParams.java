package com.exacaster.lighter.spark;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.List;
import java.util.Map;

@RecordBuilder
public record SubmitParams(String name,
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

}
