package com.github.joostvdg.buming.concurrency.memoization;

import com.github.joostvdg.buming.api.ConcurrencyExample;
import com.github.joostvdg.buming.logging.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Memoization implements ConcurrencyExample {

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void start(Logger logger) {
        String[] inventionNames = new String[]{ "Wheel", "Fire", "Writing", "Democracy"};
        String[] inventors = new String[] {"Edgar Dijkstra", "Nikola Tesla", "Alexander Graham Bell", "Leonardo da Vinci", "Tim Berners-Lee", "Alan Turing", "Grace Hopper", "Mary Currie" };

        CountDownLatch endGate = new CountDownLatch(inventors.length -1);

        Map<String, Invention> inventions = new ConcurrentHashMap<>();
        Inventing reinventingWheelsPreventor = new ReinventingWheelsPreventor(logger);
        for (String inventorName : inventors) {
            int pseudoRandom = new Random().nextInt(inventionNames.length);
            String invention = inventionNames[pseudoRandom];
            new Thread(() -> {
                inventions.put(invention, reinventingWheelsPreventor.invent(invention, inventorName));
                endGate.countDown();
            }).start();
        }

        try {
            endGate.await();
            logger.info("Memoization","final inventions -----------------");
            inventions.values()
                .stream()
                .distinct()
                .forEach(invention ->
                    logger.info("Memoization", "" + invention.getName() + " was invented by " + invention.getInventorName())
                );
            logger.info("Memoization", "final inventions -----------------");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
