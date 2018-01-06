package com.github.joostvdg.buming.cli;

import com.github.joostvdg.buming.api.ConcurrencyExample;
import com.github.joostvdg.buming.api.Server;
import com.github.joostvdg.buming.api.SortingExample;
import com.github.joostvdg.buming.logging.Logger;

import java.util.ServiceLoader;

public class HelloWorld {
    public static void main(String... args) {

        System.out.println("-------------------------------\n");
        System.out.println("Listing Modules before loading plugins");
        ModuleManager moduleManager = new ModuleManager();
        moduleManager.printModulesInBootLayer();
        System.out.println("-------------------------------\n");

        ServiceLoader<Server> servers = ServiceLoader.load(Server.class);
        if (!servers.iterator().hasNext()) {
            System.err.println("Did not find any servers, quiting");
            System.exit(1);
        }

        ServiceLoader<Logger> loggers = ServiceLoader.load(Logger.class);
        Logger logger = loggers.findFirst().isPresent() ? loggers.findFirst().get() : null;
        if (logger == null) {
            System.err.println("Did not find any loggers, quiting");
            System.exit(1);
        }
        logger.start();

        System.out.println("Running Examples");
        System.out.println("-------------------------------");
        System.out.println("Sorting Examples");
        ServiceLoader<SortingExample> sortingExamples = ServiceLoader.load(SortingExample.class);
        sortingExamples.forEach(example -> {
                System.out.println("-------------------------------");
                System.out.println("Example::"+example.name());
                example.sort(logger);
                System.out.println("-------------------------------");
            }
        );
        System.out.println("-------------------------------");
        System.out.println("Concurrency Examples");
        ServiceLoader<ConcurrencyExample> examples = ServiceLoader.load(ConcurrencyExample.class);
        examples.forEach(example -> {
                System.out.println("-------------------------------");
                System.out.println("Example::"+example.name());
                //example.start(logger);
                System.out.println("-------------------------------");
            }
        );
        System.out.println("-------------------------------");

        final Server mainServer = servers.iterator().next();
        Thread serverThread = new Thread(() -> {
            try {
                mainServer.start(8080);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        serverThread.start();

        try {
            serverThread.join(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.stop();
    }

}
