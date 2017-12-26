package com.github.joostvdg.buming.cli;

import com.github.joostvdg.buming.api.Server;

import java.util.ServiceLoader;

public class HelloWorld {
    public static void main(String... args) {

        System.out.println("-------------------------------\n");
        System.out.println("Listing Modules before loading plugins");
        ModuleManager moduleManager = new ModuleManager();
        moduleManager.printModulesInBootLayer();
        System.out.println("-------------------------------\n");

        Iterable<Server> servers = ServiceLoader.load(Server.class);
        if (!servers.iterator().hasNext()) {
            System.out.println("Did not find any servers, quiting");
            System.exit(1);
        }

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

    }

}
