package com.github.joostvdg.buming.concurrency.memoization;

import com.github.joostvdg.buming.logging.Logger;

import java.util.concurrent.*;

public class ReinventingWheelsPreventor implements Inventing {

    private final ConcurrentHashMap<String, Future<Invention>> inventionsCache = new ConcurrentHashMap<>();

    private Logger logger;

    public ReinventingWheelsPreventor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Invention invent(String inventionName, String inventorName) {
        logger.info("Memoization", inventorName + " is going to invent " + inventionName);
        while(true) { // careful there
            Future<Invention> invented = inventionsCache.get(inventionName); // has it already been invented?
            if (invented == null) {
                logger.info("Memoization", inventionName + " did not exist, we shall commence!");
                Callable<Invention> eval = new Callable<Invention>() {
                    @Override
                    public Invention call() throws Exception {
                        Inventor inventor = new Inventor(inventorName);
                        return inventor.invent(inventionName);
                    }
                };
                FutureTask<Invention> inventing = new FutureTask<>(eval);
                invented = inventionsCache.putIfAbsent(inventionName, inventing); // has someone invented this in between checks or is someone inventing this now?
                if (invented == null) { // it has not been invented nor is anyone working on this now
                    logger.info("Memoization",  inventionName + " is going to be invented by " + inventorName);
                    invented = inventing;
                    inventing.run();
                }
            } else {
                logger.info("Memoization",  inventionName + " already existed");
            }
            try {
                return invented.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

    }
}
