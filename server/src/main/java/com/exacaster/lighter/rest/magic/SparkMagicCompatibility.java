package com.exacaster.lighter.rest.magic;

import java.util.function.Supplier;
import jakarta.inject.Singleton;

@Singleton
public class SparkMagicCompatibility {
    public Object transformOrElse(String mode, Supplier<Object> doIf, Supplier<Object> doElse) {
        if ("sparkmagic".equals(mode)) {
            return doIf.get();
        }
        return doElse.get();
    }
}
