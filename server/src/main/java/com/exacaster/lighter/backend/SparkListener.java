package com.exacaster.lighter.backend;

import com.exacaster.lighter.concurrency.Waitable;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkAppHandle.Listener;

public interface SparkListener extends Listener, Waitable {

    void onError(Throwable error);

    default void infoChanged(SparkAppHandle handle) {
        // default: do nothing
    }
}
