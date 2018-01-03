package com.github.joostvdg.buming.concurrency.dining;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Philosopher {

    // we want to be fair
    private final ReentrantLock lock = new ReentrantLock(true);

    private final String name;

    private final Chopstick leftChopstick;
    private final Chopstick rightChopstick;
    private volatile int eatCounter;
    private volatile int thinkCounter;

    public Philosopher(String name, Chopstick leftChopstick, Chopstick rightChopstick){
        this.leftChopstick = leftChopstick;
        this.rightChopstick = rightChopstick;
        eatCounter = 0;
        thinkCounter = 0;
        this.name = name;
    }

    public synchronized int timesEaten(){
        return eatCounter;
    }

    public synchronized int timesThought(){
        return thinkCounter;
    }

    public void think() {
        lock.lock();
        try {
            System.out.println("["+name+"] is thinking");
            int pseudoRandom = new Random().nextInt(500);
            pseudoRandom = pseudoRandom * 10 + 100;
            Thread.sleep(pseudoRandom ) ;
            thinkCounter++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void eat() {
        lock.lock();

        int eatAttempts = 0;
        final int maxEatAttempts = 2;
        boolean eaten = false;
        while(!eaten && eatAttempts <= maxEatAttempts) {
            int pseudoRandom = new Random().nextInt(500);
            long timeToWait = pseudoRandom * 5L + 100L;
            boolean haveLeftChopstick = false;
            boolean haveRightChopstick = false;

            eatAttempts++;

            try {
                haveLeftChopstick = leftChopstick.pickUp(timeToWait, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!haveLeftChopstick) {
                System.out.println("["+name+"] Could not claim left chopstick...");
                continue;
            }

            try {
                haveRightChopstick = rightChopstick.pickUp(timeToWait, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(!haveRightChopstick) {
                System.out.println("["+name+"] Could not claim right chopstick...");
                continue;
            }

            try {
                System.out.println("["+name+"] is eating");
                eatCounter++;
                eaten = true;
                pseudoRandom = new Random().nextInt(500);
                pseudoRandom = pseudoRandom * 10 + 100;
                Thread.sleep(pseudoRandom );
                leftChopstick.putDown();
                rightChopstick.putDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lock.unlock();

    }

    public String getName() {
        return name;
    }
}
