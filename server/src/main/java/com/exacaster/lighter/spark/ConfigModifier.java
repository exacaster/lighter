package com.exacaster.lighter.spark;

import java.util.Map;
import java.util.function.Function;

public interface ConfigModifier extends Function<Map<String, String>, Map<String, String>> {

}
