package com.github.joostvdg.buming.logging.impl;

import com.github.joostvdg.buming.logging.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LoggerImpl implements Logger {

    private final BlockingQueue<String> logQueue;
    private final LoggerThread loggerThread;

    private boolean shutDown = false;
    private int queued;

    public LoggerImpl() {
        this.logQueue = new LinkedBlockingQueue<>();
        this.loggerThread = new LoggerThread();
    }

    @Override
    public void start() {
        this.loggerThread.start();
        System.out.println("Logger started");
    }

    @Override
    public void stop() {
        synchronized (this) {
            shutDown = true;
        }
        loggerThread.interrupt();
        System.out.println("Logger stopped");
    }

    private void log(String level, String prefix, String message )  {
        synchronized (this) {
            if (shutDown) {
                throw new IllegalStateException("We are shutdown, stop trying to log");
            }
            ++queued;
        }
        StringBuffer logBuffer = new StringBuffer("[");
        logBuffer.append(level);
        logBuffer.append("][");
        logBuffer.append(prefix);
        logBuffer.append("][");
        logBuffer.append(message);
        logBuffer.append("]");
        try {
            logQueue.put(logBuffer.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void info(String prefix, String message)  {
        log("info", prefix, message);
    }

    @Override
    public void warn(String prefix, String message) {
        log("warn", prefix, message);
    }

    @Override
    public void error(String prefix, String message) {
        log("error", prefix, message);
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
