package com.github.joostvdg.dui.server.handler;

import com.github.joostvdg.dui.logging.LogLevel;
import com.github.joostvdg.dui.logging.Logger;
import com.github.joostvdg.dui.server.api.impl.TaskGate;

public class TaskHandler extends Thread {

    private final TaskGate taskGate;
    private final Logger logger;

    public TaskHandler(TaskGate taskGate, Logger logger) {
        this.logger = logger;
        this.taskGate = taskGate;
    }

    @Override
    public void run() {
        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.DEBUG, "TaskHandler", threadId, " Waiting for tasks ");
        try {
            while (taskGate.isOpen()) {
                Runnable runnable;
                while ((runnable = taskGate.getTasks().poll()) != null) {
                    runnable.run();
                }
                sleep(1);
            }
        } catch (RuntimeException | InterruptedException e) {
            logger.log(LogLevel.WARN, "TaskHandler", threadId, " We crashed ");
            e.printStackTrace();
        }
    }
}
