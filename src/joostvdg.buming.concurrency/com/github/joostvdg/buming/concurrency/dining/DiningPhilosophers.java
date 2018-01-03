package com.github.joostvdg.buming.concurrency.dining;

import com.github.joostvdg.buming.api.ConcurrencyExample;
import com.github.joostvdg.buming.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class DiningPhilosopher implements Callable<Integer>{

    private final Philosopher philosopher;

    DiningPhilosopher(Philosopher philosopher) {
        this.philosopher = philosopher;
    }

    @Override
    public String toString(){
        return "[" + philosopher.getName() + "] ate " + philosopher.timesEaten() + " times and thought " + philosopher.timesThought() + " times";
    }

    @Override
    public Integer call() throws Exception {
        philosopher.think();
        philosopher.eat();
        philosopher.think();
        philosopher.eat();
        philosopher.think();
        philosopher.eat();
        philosopher.eat();
        return philosopher.timesEaten();
    }
}

public class DiningPhilosophers implements ConcurrencyExample {
    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void start(Logger logger) {
        String[] names = new String[] {"Aristotle", "Voltaire", "Laoze", "Confucius", "Nietzche" };
        int amountOfPhilosophers = 5;

        List<DiningPhilosopher> philosophers = new ArrayList<>();
        Chopstick[] chopsticks = new Chopstick[amountOfPhilosophers];
        for(int i=0; i < amountOfPhilosophers; i++) {
            chopsticks[i] = new Chopstick();
        }

        for(int i=0; i < amountOfPhilosophers; i++) {
            Chopstick leftChopstick = chopsticks[i];
            Chopstick rightChopstick = chopsticks[(i+1) % amountOfPhilosophers]; // this way, we will circle back to the start of the array when i == amountOfPhilosophers
            Philosopher philosopher = new Philosopher(names[i], leftChopstick, rightChopstick);
            philosophers.add(new DiningPhilosopher(philosopher));
        }

        System.out.println("[" + getClass().getSimpleName() + "] Chopsticks: " + chopsticks.length + ", dining philosophers: " + philosophers.size() );
        ExecutorService executorService = Executors.newFixedThreadPool(amountOfPhilosophers);
        try {
            executorService.invokeAll(philosophers);
            executorService.awaitTermination(10000, TimeUnit.MILLISECONDS);
            philosophers.forEach(System.out::println);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
