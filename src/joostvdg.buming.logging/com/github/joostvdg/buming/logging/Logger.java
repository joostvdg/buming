package com.github.joostvdg.buming.logging;

public interface Logger {

    void start();

    void stop();

    void info(String prefix, String message);

    void warn(String prefix, String message);

    void error(String prefix, String message);
}
