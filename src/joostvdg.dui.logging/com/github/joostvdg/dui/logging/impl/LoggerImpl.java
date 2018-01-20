package com.github.joostvdg.dui.logging.impl;

import com.github.joostvdg.dui.logging.LogLevel;
import com.github.joostvdg.dui.logging.Logger;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LoggerImpl implements Logger {

    private static final String ERROR_FLAG_PREFIX = "[ERROR]";

    // FOR ANSI COLORS: https://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private final BlockingQueue<String> logQueue;
    private final LoggerThread loggerThread;
    private LogLevel level;
    private boolean shutDown = false;
    private int queued;

    public LoggerImpl() {
        this.logQueue = new LinkedBlockingQueue<>();
        this.loggerThread = new LoggerThread();
    }

    @Override
    public void start(LogLevel level) {
        this.level = level;
        this.loggerThread.start();
    }

    @Override
    public void stop() {
        synchronized (this) {
            shutDown = true;
        }
        loggerThread.interrupt();
    }

    @Override
    public void log(LogLevel level, String mainComponent, String subComponent, long threadId, String... messageParts) {
        synchronized (this) {
            if (shutDown) {
                throw new IllegalStateException("We are shutdown, stop trying to log");
            }
            ++queued;
        }
        if (level.getLevel() < this.level.getLevel()) {
            // we should not log this
            return;
        }

        String ANSI_COLOR_TO_USER = ANSI_WHITE;
        switch(level) {
            case DEBUG:
                ANSI_COLOR_TO_USER = ANSI_WHITE;
                break;
            case INFO:
                ANSI_COLOR_TO_USER = ANSI_CYAN;
                break;
            case WARN:
                ANSI_COLOR_TO_USER = ANSI_YELLOW;
                break;
            case ERROR:
                ANSI_COLOR_TO_USER = ANSI_RED;
                break;
        }

        StringBuffer logBuffer = new StringBuffer("");
        if (level.equals(LogLevel.ERROR)) {
            logBuffer.append("[ERROR]");
        }
        logBuffer.append("[");
        logBuffer.append(ANSI_PURPLE);
        logBuffer.append(mainComponent);
        logBuffer.append(ANSI_RESET);
        if (mainComponent.length() < 18) {
            logBuffer.append("]\t\t\t\t[");
        } else if (mainComponent.length() < 23) {
            logBuffer.append("]\t\t\t[");
        } else if (mainComponent.length() < 30) {
            logBuffer.append("]\t\t[");
        } else {
            logBuffer.append("]\t[");
        }
        logBuffer.append(ANSI_COLOR_TO_USER);
        logBuffer.append(level);
        logBuffer.append(ANSI_RESET);
        logBuffer.append("]\t[");
        logBuffer.append(ANSI_GREEN);
        logBuffer.append(LocalDateTime.now().toLocalTime());
        logBuffer.append(ANSI_RESET);
        logBuffer.append("]\t[");
        logBuffer.append(ANSI_GREEN);
        logBuffer.append(threadId);
        logBuffer.append(ANSI_RESET);
        if (threadId < 10) {
            logBuffer.append("]\t");
        } else {
            logBuffer.append("]\t");
        }
        logBuffer.append(ANSI_COLOR_TO_USER);
        if (subComponent != null ) {
            logBuffer.append("[");
            logBuffer.append(subComponent);
            if (subComponent.length() < 6) {
                logBuffer.append("]\t\t\t\t");
            } else if (subComponent.length() < 10) {
                logBuffer.append("]\t\t\t");
            } else if (subComponent.length() < 20){
                logBuffer.append("]\t\t");
            } else {
                logBuffer.append("]\t");
            }
        } else {
            logBuffer.append("]\t");
        }
        for (String part : messageParts) {
            logBuffer.append(part);
        }
        logBuffer.append(ANSI_RESET);
        try {
            logQueue.put(logBuffer.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void log(LogLevel level, String component, long threadId, String... messageParts) {
        log(level, component, null, threadId, messageParts);
    }

    private class LoggerThread extends Thread {
        @Override
        public void run(){
            while(true) {
                try {
                    synchronized (LoggerImpl.this) {
                        if (shutDown && queued == 0) {
                            break; // we're done now
                        }
                    }

                    String message = logQueue.take();
                    synchronized (LoggerImpl.this) {
                        --queued; // we've taken up a message, less people queue'd for sure
                    }

                    if (message.startsWith(ERROR_FLAG_PREFIX)) {
                        System.err.println(message.substring(ERROR_FLAG_PREFIX.length()));
                    } else {
                        System.out.println(message);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
