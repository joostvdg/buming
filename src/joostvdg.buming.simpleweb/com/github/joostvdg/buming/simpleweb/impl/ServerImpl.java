package com.github.joostvdg.buming.simpleweb.impl;

import com.github.joostvdg.buming.api.Server;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class ServerImpl implements Server{

    @Override
    public void start(int port) throws Exception {
        System.out.println("Starting server on port " + port);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/test", new MyTestHandler());
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started");
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            System.out.println("Got a request on /");
            String response = "This is some other response";
            t.sendResponseHeaders(200, response.length());
            try (OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    static class MyTestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            System.out.println("Got a request on /test");
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            try (OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
