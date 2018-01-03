package com.github.joostvdg.buming.concurrency.latch;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class TrackRunner implements Runnable {

    private final CountDownLatch startGate;
    private final CountDownLatch endGate;
    private final String name;

    // we have to guarantee the end result is visible everywhere
    private volatile double trackTime;

    public TrackRunner(CountDownLatch startGate, CountDownLatch endGate, String name) {
        this.startGate = startGate;
        this.endGate = endGate;
        this.name = name;
        trackTime = -1; // we didn't run!
    }

    @Override
    public void run() {
        try {
            startGate.await();
            runOnTheTrack();
        } catch (InterruptedException e) {
            System.out.println("We ran into an interrupt: " + e.getMessage());
        } finally {
            endGate.countDown();
        }
    }

    private void runOnTheTrack() throws InterruptedException {
        int pseudoRandom = new Random().nextInt(50);
        long trackTimeRaw = pseudoRandom * 150;
        Thread.sleep(trackTimeRaw);
        trackTime = (double) trackTimeRaw / 1000;
    }

    public String name() {
        return this.name;
    }

    public double trackTime() {
        return trackTime;
    }
}
