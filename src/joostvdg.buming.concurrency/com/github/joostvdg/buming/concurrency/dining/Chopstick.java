package com.github.joostvdg.buming.concurrency.dining;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Chopstick {

    // we want to be fair
    private final ReentrantLock lock = new ReentrantLock(true);

    public boolean pickUp(long time, TimeUnit unit) throws InterruptedException {
        if(lock.tryLock(time, unit)) {
            return true;
        }
        return false;
    }

    public void putDown(){
        lock.unlock();
    }
}
