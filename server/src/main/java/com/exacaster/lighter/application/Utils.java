package com.exacaster.lighter.application;

import java.util.HashMap;
import java.util.Map;

public final class Utils {

    private Utils() {
    }

    public static Map<String, String> merge(Map<String, String> current, Map<String, String> other) {
        var result = new HashMap<>(current);
        other.forEach((key, val) -> {
            result.computeIfAbsent(key, (k) -> val);
        });
        return result;
    }
}
