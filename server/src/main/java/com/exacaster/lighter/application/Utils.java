package com.exacaster.lighter.application;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Utils {

    private Utils() {
    }

    public static Map<String, String> merge(Map<String, String> current, Map<String, String> other) {
        var result = new HashMap<>(current);
        other.forEach((key, val) -> result.computeIfAbsent(key, (k) -> val));
        return result;
    }

    // Copied from internal spark code
    private static final String SECRET_REDACTION_PATTERN = "(?i)secret|password|token|access[.]?key";
    private static final String SECRET_REDACTION_PATTERN_KEY = "spark.redaction.regex";

    public static Map<String, String> redact(Map<String, String> conf) {
        var pattern = Pattern.compile(conf.getOrDefault(SECRET_REDACTION_PATTERN_KEY, SECRET_REDACTION_PATTERN));
        return conf.entrySet().stream().map(entity -> {
            var matcher = pattern.matcher(entity.getKey());
            if (matcher.find()) {
                return Map.entry(entity.getKey(), "[redacted]");
            } else {
                return entity;
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
