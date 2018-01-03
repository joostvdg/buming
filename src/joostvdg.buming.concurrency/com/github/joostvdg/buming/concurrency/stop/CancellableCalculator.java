package com.github.joostvdg.buming.concurrency.stop;

import java.util.ArrayList;
import java.util.List;

public class CancellableCalculator implements Runnable {
    private volatile boolean cancelled;
    private volatile long cancellationTime;
    private final List<Long> calculations = new ArrayList<>();

    @Override
    public void run() {
        long n = 1L;
        long start = System.nanoTime();
        while(!cancelled) {
            n = n * (n+1);
            synchronized (this) {
                calculations.add(n);
            }
        }
        long end = System.nanoTime();
        double totalRunningTime = (double) (end - start) / 1000000000; // assuming we will run for more than a second
        long timeBetweenCancelAndEnding = end - cancellationTime; // this should be in nano's, milli's is to large to see it happen ;)
        System.out.println("[CancellableCalculator] [totalRunningTime(second): " + totalRunningTime + ", timeBetweenCancelAndEnding (nano):" + timeBetweenCancelAndEnding + "]");
    }

    public synchronized List<Long> calculationResults(){
        return new ArrayList<>(calculations);
    }

    public void cancel() {
        cancelled = true;
        cancellationTime = System.nanoTime();
    }
}
