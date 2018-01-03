package com.github.joostvdg.buming.concurrency.simplelock;

import com.github.joostvdg.buming.logging.Logger;

import java.util.Random;

public class Processor {

    private static final int MAX_RUNS = 10;
    private volatile int count = 0;
    private volatile int arbitraryValue = 0;
    private final Object lock = new Object();
    private Logger logger;

    public Processor(Logger logger) {
        this.logger =logger;
    }

    public void produce() {
        synchronized (lock) {
            while(count < MAX_RUNS) {
                count++;
                if (arbitraryValue == 0) {
                    logger.info("SimpleLock", ""+Thread.currentThread().getId() + "][Producer]Producing something");
                    int pseudoRandom = new Random().nextInt(100);
                    arbitraryValue = pseudoRandom * 5;
                    lock.notify();
                } else {
                    logger.info("SimpleLock", ""+Thread.currentThread().getId() + "][Producer]Waiting for consumer");
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void consume() {
        synchronized (lock) {
            for (int i = 0; i < 10; i++) {
                logger.info("SimpleLock", ""+Thread.currentThread().getId() + "][Consumer]Trying to consume");
                if (arbitraryValue == 0) {
                    logger.info("SimpleLock", ""+Thread.currentThread().getId() + "][Consumer]Waiting on a new value");
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    logger.info("SimpleLock", ""+Thread.currentThread().getId() + "][Consumer]Value:"+arbitraryValue);
                    arbitraryValue = 0;
                    lock.notify();
                }

            }
        }
    }
}
