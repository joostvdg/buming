package com.github.joostvdg.buming.simpleweb.impl;

import java.util.Random;

public class UnsafeCounter {
    private long count = 0;

    public long getCount() {
        return count;
    }

    public void service() {
        // do some work
        try {
            int pseudoRandom = new Random().nextInt(20);
            Thread.sleep(pseudoRandom * 100);
            ++count;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
