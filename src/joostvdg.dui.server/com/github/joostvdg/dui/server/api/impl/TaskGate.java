package com.github.joostvdg.dui.server.api.impl;

import com.github.joostvdg.dui.logging.LogLevel;
import com.github.joostvdg.dui.logging.Logger;
import com.github.joostvdg.dui.server.handler.TaskHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class TaskGate {

    private static AtomicInteger poolCount = new AtomicInteger(0);

    private Queue<Runnable> tasks;

    private final List<TaskHandler> threadPool;
    private final Logger logger;

    private boolean isOpen;

    private TaskGate(final int threadCount, Logger logger) {
        this.logger = logger;
        threadPool = new ArrayList<>(threadCount);
        for (int i=0; i < threadCount; i++) {
            TaskHandler taskHandler = new TaskHandler(this, logger);
            threadPool.add(taskHandler);
        }
        poolCount.set(threadCount);
        tasks = new ConcurrentLinkedQueue<>();
    }

    public synchronized boolean isOpen(){
        return this.isOpen;
    }

    public synchronized void close() {
        this.isOpen = false;
    }

    private synchronized void setOpen(){
        this.isOpen = true;
    }

    public void addTask(Runnable task) {
        tasks.add(task);
    }

    public Queue<Runnable> getTasks() {
        return tasks;
    }


    public static TaskGate getTaskGate(int threadCount, Logger logger) {
        TaskGate taskGate = new TaskGate(threadCount, logger);
        taskGate.setOpen();
        for (TaskHandler handler : taskGate.getTaskHandlers()) {
            handler.start();
        }
        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.DEBUG, "TaskGate", threadId, "Opened TaskGate with ",  ""+taskGate.getTaskHandlers().size(), " handlers.");
        return taskGate;
    }

    private List<TaskHandler> getTaskHandlers() {
        return threadPool;
    }
}
