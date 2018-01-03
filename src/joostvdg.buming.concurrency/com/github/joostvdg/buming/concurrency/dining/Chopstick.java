package com.github.joostvdg.buming.concurrency.dining;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Chopstick {

    // we want to be fair
    private final ReentrantLock lock = new ReentrantLock(true);

    private final Condition condition;
    private volatile ChopstickStatus status = ChopstickStatus.FREE;

    public Chopstick(){
        condition = lock.newCondition();
    }

    public boolean pickUp(long time, TimeUnit unit) throws InterruptedException {
        lock.lock();
        boolean wasPickedUp = true; // we're going to asume this will happen
        try {
            if(status.equals(ChopstickStatus.FREE)) {
                status = ChopstickStatus.IN_USE;
            } else {
                wasPickedUp = condition.await(time, unit); // if the timeout elapsed, it will be false and we will not have picked it up
            }

        } finally {
            lock.unlock();
        }
        return wasPickedUp;
    }

    public void putDown(){
        lock.lock();
        try {
            status = ChopstickStatus.FREE;
            condition.signal();
        } finally {
            lock.unlock();
        }
    }
}
