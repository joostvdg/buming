package com.github.joostvdg.buming.concurrency.stop;

import com.github.joostvdg.buming.api.ConcurrencyExample;
import com.github.joostvdg.buming.logging.Logger;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

public class GracefulStop implements ConcurrencyExample {
    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void start(Logger logger) {
        CancellableCalculator calculator = new CancellableCalculator();
        Thread one = new Thread(calculator);
        Thread two = new Thread(calculator);
        one.start();
        two.start();
        try {
            SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            calculator.cancel();
        }

        while(!Thread.State.TERMINATED.equals(one.getState())) {
            System.out.println("[GracefulStop] Still waiting on thread one to terminate");
            try {
                SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while(!Thread.State.TERMINATED.equals(two.getState())) {
            System.out.println("[GracefulStop] Still waiting on thread two to terminate");
            try {
                SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("[GracefulStop] Both threads are terminated, collecting results...");

        List<Long> results = calculator.calculationResults();
        System.out.println("[GracefulStop]We calculated " + results.size() + " results and last result was: " + results.get(results.size() -1));
    }
}
