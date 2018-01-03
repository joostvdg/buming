package com.github.joostvdg.buming.concurrency.latch;

import com.github.joostvdg.buming.api.ConcurrencyExample;
import com.github.joostvdg.buming.logging.Logger;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchExample implements ConcurrencyExample {
    private static int AMOUNT_OF_RUNNERS = 10;

    @Override
    public String name() {
        return "CountDownLatch";
    }

    @Override
    public void start(Logger logger) {
        // basically a boolean, we can we start running!
        CountDownLatch startGate = new CountDownLatch(1);

        // we won't have an overview until all runners are finished
        CountDownLatch endGate = new CountDownLatch(AMOUNT_OF_RUNNERS);

        TrackRunner[] runners = new TrackRunner[AMOUNT_OF_RUNNERS];

        for (int i =0; i < AMOUNT_OF_RUNNERS; i++) {
            TrackRunner runner = new TrackRunner(startGate, endGate, "R" + i);
            runners[i] = runner;
            Thread thread = new Thread(runner);
            thread.start();
        }

        long start = System.nanoTime();
        startGate.countDown(); // give the start signal
        try {
            endGate.await(); // wait till all runners have finished
            long end = System.nanoTime();
            double totalTimeInSeconds = (double)(end - start) / (1000 * 1000) / 1000;
            System.out.println("[Latch] Total running time was: " + totalTimeInSeconds + " seconds");
            for (TrackRunner runner : runners) {
                System.out.println("[Latch] Runner " + runner.name() + "::Time " + runner.trackTime() + " seconds");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
