package com.github.joostvdg.buming.cli;

import com.github.joostvdg.buming.api.SortingExample;
import com.github.joostvdg.buming.logging.Logger;

import java.util.ServiceLoader;

public class SortingExamples {
    public static void main(String[] args) {
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
        logger.stop();
    }

}
