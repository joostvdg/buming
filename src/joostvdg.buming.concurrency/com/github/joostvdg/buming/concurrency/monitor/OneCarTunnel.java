package com.github.joostvdg.buming.concurrency.monitor;

import com.github.joostvdg.buming.logging.Logger;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class OneCarTunnel {

    // we want to be fair
    private final ReentrantLock lock = new ReentrantLock(true);

    // extra safety check for the amount of cars in the tunnel
    private Condition limitedAmountOfCarsInTunnel;

    // volatile or use a concurrent safe class such as AtomicInt
    private volatile int carsInTunnel;
    private static final int MAX_CARS_IN_TUNNEL = 2;
    private final Logger logger;

    public OneCarTunnel(Logger logger){
        limitedAmountOfCarsInTunnel = lock.newCondition();
        this.logger = logger;
    }

    public boolean isOccupied(){
        if(carsInTunnel < 0 || carsInTunnel > MAX_CARS_IN_TUNNEL) {
            throw new IllegalStateException("Our tunnel is broken, we have " + carsInTunnel + " cars in the tunnel");
        }
        return carsInTunnel == MAX_CARS_IN_TUNNEL;
    }

    public void enterTunnel(String car) {
        logger.info("Monitor", (""+Thread.currentThread().getId() + "]["+car+ "] <-- Queue in front of the tunnel [" + carsInTunnel +"/" + MAX_CARS_IN_TUNNEL) );
        lock.lock();
        logger.info("Monitor", ("" + Thread.currentThread().getId() + "]["+car+ "] <-- Claimed the tunnel [" + carsInTunnel +"/" + MAX_CARS_IN_TUNNEL ));
        try {
            // just to be sure, double check if the tunnel is actually empty!
            while(carsInTunnel >= MAX_CARS_IN_TUNNEL) {
                logger.info("Monitor", ( "" + Thread.currentThread().getId() + "]["+car+ "] <-- Waiting before driving into the tunnel [" + carsInTunnel +"/" + MAX_CARS_IN_TUNNEL ));
                limitedAmountOfCarsInTunnel.awaitUninterruptibly();
            }
            ++carsInTunnel;
            limitedAmountOfCarsInTunnel.signal();
            logger.info("Monitor", ( "" + Thread.currentThread().getId() + "]["+car+ "] <-- Driving into the tunnel [" + carsInTunnel +"/" + MAX_CARS_IN_TUNNEL));
        } finally {
            lock.unlock();
        }
    }

    public void exitTunnel(String car){
        logger.info("Monitor", ("" + Thread.currentThread().getId() + "]["+car+ "] --> Attempt to drive out the tunnel [" + carsInTunnel +"/" + MAX_CARS_IN_TUNNEL ));
        lock.lock();
        try {
            --carsInTunnel;
            limitedAmountOfCarsInTunnel.signal();
            logger.info("Monitor", ("" + Thread.currentThread().getId() + "]["+car+ "] --> Driving out the tunnel [" + carsInTunnel +"/" + MAX_CARS_IN_TUNNEL));
            if (carsInTunnel >= MAX_CARS_IN_TUNNEL) {
                logger.info("Monitor", ("" + Thread.currentThread().getId() + "]["+car+ "] --> Something is broken! [" + carsInTunnel +"/" + MAX_CARS_IN_TUNNEL + "] cars"));
            }
        } finally {
            lock.unlock();
        }
    }

}
