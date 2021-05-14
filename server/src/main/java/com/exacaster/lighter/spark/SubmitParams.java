package com.exacaster.lighter.spark;

import java.util.List;
import java.util.Map;

public record SubmitParams(String name,
                           String master,
                           String mainClass,
                           List<String> args,
                           List<String> pyFiles,
                           List<String> files,
                           List<String> jars,
                           Map<String, String> conf) {

}
