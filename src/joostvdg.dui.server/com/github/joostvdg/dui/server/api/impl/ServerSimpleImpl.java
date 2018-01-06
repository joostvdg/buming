package com.github.joostvdg.dui.server.api.impl;

import com.github.joostvdg.dui.api.FeiwuMessageType;
import com.github.joostvdg.dui.client.api.DuiClient;
import com.github.joostvdg.dui.client.api.DuiClientFactory;
import com.github.joostvdg.dui.logging.LogLevel;
import com.github.joostvdg.dui.logging.Logger;
import com.github.joostvdg.dui.server.api.DuiServer;
import com.github.joostvdg.dui.server.handler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.joostvdg.dui.api.ProtocolConstants.INTERNAL_COMMUNICATION_PORT_A;
import static com.github.joostvdg.dui.api.ProtocolConstants.INTERNAL_COMMUNICATION_PORT_C;

public class ServerSimpleImpl implements DuiServer {
    private volatile boolean stopped = false;

    private ServerSocket serverSocket;

    private final TaskGate taskGate;

    private final ConcurrentHashMap<String, Integer> membershipList;
    private final Logger logger;
    private final String name;
    private final String mainComponent;
    private final int externalPort;
    private final int internalPort;

    public ServerSimpleImpl(String name, int port, Logger logger) {
        this.name = name;
        this.mainComponent = "Server-" + name;
        this.externalPort = port;
        this.internalPort = externalPort + 10;
        this.logger = logger;
        taskGate = TaskGate.getTaskGate(2, logger);
        membershipList = new ConcurrentHashMap<>();
        for (int i = INTERNAL_COMMUNICATION_PORT_A; i <= INTERNAL_COMMUNICATION_PORT_C; i++) { // TODO: determine actual/current membership list
            if (port != i) {
                membershipList.put(""+i, i);
            }
        }
    }

    @Override
    public synchronized void stopServer(){
        this.stopped = true;
        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, "Main", threadId, " Stopping");
        closeServer();
    }

    @Override
    public void closeServer(){
        taskGate.close();
        if (!serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized boolean isStopped() {
        return this.stopped;
    }

    @Override
    public String name() {
        return name;
    }

    private void listenToExternalCommunication() {
        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, "External", threadId, " Started");
        try {
            serverSocket = new ServerSocket(externalPort);
            while(!isStopped()) {
                String status = "running";
                if (isStopped()) {
                    status = "stopped";
                }
                logger.log(LogLevel.DEBUG, mainComponent, "External", threadId, " Status: ", status);
                try  {
                    Socket clientSocket = serverSocket.accept();
                    Runnable clientHandler = new ClientHandler(clientSocket, logger, mainComponent);
                    taskGate.addTask(clientHandler);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        logger.log(LogLevel.WARN, mainComponent, "External", threadId, " Interrupted, stopping");
                        return;
                    }
                } catch(SocketException socketException){
                    logger.log(LogLevel.WARN, mainComponent, "External", threadId, " Server socket ", ""+internalPort, " is closed, exiting.");
                    logger.log(LogLevel.WARN, mainComponent, "External", threadId, " Reason for stopping: ", socketException.getCause().toString());
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeServer();
        }
    }

    private void listenToInternalCommunication() {
        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, "Internal", threadId, " Started");
        try {
            serverSocket = new ServerSocket(internalPort);
            while(!isStopped()) {
                String status = "running";
                if (isStopped()) {
                    status = "stopped";
                }
                logger.log(LogLevel.DEBUG, mainComponent, "Internal", threadId, " Status: ",status);
                try  {
                    Socket clientSocket = serverSocket.accept();
                    Runnable clientHandler = new ClientHandler(clientSocket, logger, mainComponent);
                    taskGate.addTask(clientHandler);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        logger.log(LogLevel.WARN, mainComponent, "Internal", threadId, " Interrupted, stopping");
                        return;
                    }
                } catch(SocketException socketException){
                    logger.log(LogLevel.WARN, mainComponent, "Internal", threadId, " Server socket ", ""+internalPort, " is closed, exiting.");
                    logger.log(LogLevel.WARN, mainComponent, "Internal", threadId, " Reason for stopping:", socketException.getCause().toString());
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeServer();
        }
    }


    private void talkToOtherServers() {
        DuiClient client = DuiClientFactory.newSimpleClient();
        while (!stopped) {
            membershipList.values().forEach(port -> client.sendServerMessage(FeiwuMessageType.HELLO, ("Hi from " + name).getBytes(), port));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void startServer() {
        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, "Main", threadId, " Starting");
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.submit(this::listenToExternalCommunication);
        executorService.submit(this::listenToInternalCommunication);
        executorService.submit(this::talkToOtherServers);
    }

}
