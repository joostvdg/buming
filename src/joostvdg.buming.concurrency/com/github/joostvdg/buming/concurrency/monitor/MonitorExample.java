package com.github.joostvdg.buming.concurrency.monitor;

import com.github.joostvdg.buming.api.ConcurrencyExample;
import com.github.joostvdg.buming.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class MonitorExample implements ConcurrencyExample {
    @Override
    public String name() {
        return "Monitor";
    }

    @Override
    public void start(Logger logger) {
        OneCarTunnel oneCarTunnel = new OneCarTunnel(logger);
        Runnable carA = new TunnelDrivingCar("A", oneCarTunnel);
        Runnable carB = new TunnelDrivingCar("B", oneCarTunnel);
        Runnable carC = new TunnelDrivingCar("C", oneCarTunnel);
        Runnable carD = new TunnelDrivingCar("D", oneCarTunnel);
        List<Runnable> cars = new ArrayList<>();
        cars.add(carA);
        cars.add(carB);
        cars.add(carC);
        cars.add(carD);

        for (Runnable car : cars) {
            (new Thread(car)).start();
        }

    }
}
