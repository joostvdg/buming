package com.github.joostvdg.buming.simpleweb.impl;

import com.github.joostvdg.buming.api.Server;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 *  https://stackoverflow.com/questions/3732109/simple-http-server-in-java-using-only-java-se-api
 */
public class ServerImpl implements Server{

    @Override
    public void start(int port) throws Exception {
        System.out.println("Starting server on port " + port);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        UnsafeCounter unsafeCounter = new UnsafeCounter();
        SafeCounter safeCounter = new SafeCounter();
        Factorizer factorizer = new Factorizer();
        server.createContext("/", new MyHandler(unsafeCounter, safeCounter, factorizer));
        Executor executor = Executors.newFixedThreadPool(5);
        server.setExecutor(executor); // creates a default executor
        server.start();
        System.out.println("Server started");
    }

    static class MyHandler implements HttpHandler {
        private UnsafeCounter unsafeCounter;
        private SafeCounter safeCounter;
        private Factorizer factorizer;

        public MyHandler(UnsafeCounter unsafeCounter, SafeCounter safeCounter, Factorizer factorizer) {
            this.unsafeCounter = unsafeCounter;
            this.safeCounter = safeCounter;
            this.factorizer = factorizer;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            System.out.println("Got a request on /");
            unsafeCounter.service();
            safeCounter.service();
            long unsafeCount = unsafeCounter.getCount();
            long safeCount = safeCounter.getCount();
            System.out.println("Counts so far:"+ unsafeCount + "::" + safeCount);
            factorizer.service(unsafeCount);
            factorizer.service(safeCount);
            System.out.println("Current factorizer hits:"+factorizer.getHits() + ", and cache hit ratio:"+factorizer.getCacheHitRatio());
            String response = "This is some other response";
            t.sendResponseHeaders(200, response.length());
            try (OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

}
