package com.github.joostvdg.buming.simpleweb.impl;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class SafeCounter {

    private final AtomicLong count = new AtomicLong(0);

    public long getCount() {
        return count.get();
    }

    public void service() {
        try {
            int pseudoRandom = new Random().nextInt(20);
            Thread.sleep(pseudoRandom * 100);
            count.incrementAndGet();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
