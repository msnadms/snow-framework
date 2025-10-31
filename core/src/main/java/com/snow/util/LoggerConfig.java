package com.snow.util;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class LoggerConfig {
    static {
        Logger logger = Logger.getLogger("");

        for (Handler handler : logger.getHandlers()) {
            handler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    if (record.getThrown() == null) {
                        return String.format("[%s]: %s\n", record.getLevel(), record.getMessage());
                    }
                    return String.format("[%s]: %s, %s\n",
                            record.getLevel(),
                            record.getMessage(),
                            record.getThrown().getMessage());
                }
            });
        }
    }
}
