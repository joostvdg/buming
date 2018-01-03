package com.github.joostvdg.buming.concurrency.monitor;

import java.util.Random;

public class TunnelDrivingCar implements Runnable  {

    private String name;
    private OneCarTunnel oneCarTunnel;

    public TunnelDrivingCar(String name, OneCarTunnel oneWayTunnel) {
        this.name = name;
        this.oneCarTunnel = oneWayTunnel;
    }

    @Override
    public void run() {
        try {
            int pseudoRandom = new Random().nextInt(20);
            Thread.sleep(pseudoRandom * 10);
            oneCarTunnel.enterTunnel(name);
            pseudoRandom = new Random().nextInt(20);
            Thread.sleep(pseudoRandom * 10);
            oneCarTunnel.exitTunnel(name);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
