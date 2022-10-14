package com.exacaster.lighter.backend.local.logger;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        String message = formatMessage(record);
        return message + System.lineSeparator();
    }
}
