package com.exacaster.lighter.backend.local.logger;

import com.google.common.collect.EvictingQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

public class LogCollectingHandler extends Handler {

    private final EvictingQueue<String> logQueue;

    public LogCollectingHandler(int maxSize) {
        this.logQueue = EvictingQueue.create(maxSize);
        setFormatter(new LogFormatter());
    }

    @Override
    public void publish(LogRecord record) {
        var logLine = this.getFormatter().format(record);
        logQueue.add(logLine);
    }

    public String getLogs() {
        return logQueue.stream().collect(Collectors.joining());
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
