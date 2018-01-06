package com.github.joostvdg.dui.logging.impl;

import com.github.joostvdg.dui.logging.LogLevel;
import com.github.joostvdg.dui.logging.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LoggerImpl implements Logger {

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

        StringBuffer logBuffer = new StringBuffer("[");
        logBuffer.append(mainComponent);
        if (mainComponent.length() < 15) {
            logBuffer.append("]\t\t\t[");
        } else if (mainComponent.length() < 22) {
            logBuffer.append("]\t\t[");
        } else {
            logBuffer.append("]\t[");
        }
        logBuffer.append(level);
        logBuffer.append("]\t[");
        logBuffer.append(threadId);
        if (threadId < 10) {
            logBuffer.append("]\t\t");
        } else {
            logBuffer.append("]\t");
        }
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
                    System.out.println(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
