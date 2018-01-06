package com.github.joostvdg.dui.logging;

public interface Logger {

    void start(LogLevel level);

    void stop();

    void log(LogLevel level, String mainComponent, String subComponent, long threadId, String...messageParts);
    void log(LogLevel level, String component, long threadId, String...messageParts);
}
