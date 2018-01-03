package com.github.joostvdg.buming.concurrency.simplelock;

import com.github.joostvdg.buming.api.ConcurrencyExample;
import com.github.joostvdg.buming.logging.Logger;

public class SimpleLock implements ConcurrencyExample{
    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void start(Logger logger) {
        Processor processor = new Processor(logger);

        Thread t1 = new Thread(processor::produce);
        Thread t2 = new Thread(processor::consume);
        t1.start();
        t2.start();

        try {
            t1.join(1000);
            t2.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
