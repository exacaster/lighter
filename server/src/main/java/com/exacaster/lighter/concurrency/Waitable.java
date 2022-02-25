package com.exacaster.lighter.concurrency;

public interface Waitable {

    void waitCompletion() throws InterruptedException;
}
