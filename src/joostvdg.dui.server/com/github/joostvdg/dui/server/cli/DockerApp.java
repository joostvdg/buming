package com.github.joostvdg.dui.server.cli;

import com.github.joostvdg.dui.api.ProtocolConstants;
import com.github.joostvdg.dui.logging.LogLevel;
import com.github.joostvdg.dui.logging.Logger;
import com.github.joostvdg.dui.server.api.DuiServer;
import com.github.joostvdg.dui.server.api.DuiServerFactory;

import java.util.Random;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DockerApp {
    public static void main(String[] args) {
        ServiceLoader<Logger> loggers = ServiceLoader.load(Logger.class);
        Logger logger = loggers.findFirst().isPresent() ? loggers.findFirst().get() : null;
        if (logger == null) {
            System.err.println("Did not find any loggers, quiting");
            System.exit(1);
        }
        logger.start(LogLevel.INFO);

        int pseudoRandom = new Random().nextInt(ProtocolConstants.POTENTIAL_SERVER_NAMES.length -1);
        String serverName = ProtocolConstants.POTENTIAL_SERVER_NAMES[pseudoRandom];
        int listenPort = ProtocolConstants.EXTERNAL_COMMUNICATION_PORT_A;
        String multicastGroup = ProtocolConstants.MULTICAST_GROUP;

        DuiServer distributedServer = DuiServerFactory.newDistributedServer(listenPort,multicastGroup , serverName, logger);

        distributedServer.logMembership();

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(distributedServer::startServer);

        long threadId = Thread.currentThread().getId();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook called!");
            logger.log(LogLevel.WARN, "App", "ShutdownHook", threadId, "Shutting down at request of Docker");
            distributedServer.stopServer();
            distributedServer.closeServer();
            executorService.shutdown();
            try {
                Thread.sleep(100);
                executorService.shutdownNow();
                logger.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        for(int i = 0; i < 15; i++){
            try {
                Thread.sleep(10000);
                distributedServer.logMembership();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
