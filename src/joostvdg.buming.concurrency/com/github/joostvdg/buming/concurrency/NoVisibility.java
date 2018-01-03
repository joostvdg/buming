package com.github.joostvdg.buming.concurrency;

import com.github.joostvdg.buming.api.ConcurrencyExample;
import com.github.joostvdg.buming.logging.Logger;

/**
 * NoVisibility in Listing 3.1 illustrates what can go wrong when threads share data without synchronization.
 * Two threads, the main thread and the reader thread, access the shared variables ready and number.
 * The main thread starts the reader thread and then sets number to 42 and ready to true .
 *
 * The reader thread spins until it sees ready is true , and then prints out number .
 * While it may seem obvious that NoVisibility will print 42, it is in fact possible that it will print zero, or never terminate at all!
 *
 * Because it does not use adequate synchronization, there is no guarantee that the values of ready and number written by the main thread will be visible to the reader thread.
 * Java Concurrency in Practice / Brian Goetz, with Tim Peierls. . . [et al.]
 */
public class NoVisibility implements ConcurrencyExample {
    // this should be either volatile of guarded by a lock to guarantee visibility.
    private static boolean ready;
    private static int number;

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void start(Logger logger) {

        for(int i =0; i < 10; i++) {
            runExample();
        }
    }

    private void runExample(){
        new ReaderThread().start();
        number = 42;
        ready = true;
    }

    private static class ReaderThread extends Thread {
        public void run() {
            while (!ready) {
                Thread.yield();
            }
            System.out.println(number);
        }
    }
}
