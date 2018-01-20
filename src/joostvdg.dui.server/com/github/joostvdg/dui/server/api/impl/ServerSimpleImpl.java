package com.github.joostvdg.dui.server.api.impl;

import com.github.joostvdg.dui.api.exception.MessageDeliveryException;
import com.github.joostvdg.dui.api.exception.MessageTargetDoesNotExistException;
import com.github.joostvdg.dui.api.exception.MessageTargetNotAvailableException;
import com.github.joostvdg.dui.api.message.FeiwuMessageType;
import com.github.joostvdg.dui.client.api.DuiClient;
import com.github.joostvdg.dui.client.api.DuiClientFactory;
import com.github.joostvdg.dui.logging.LogLevel;
import com.github.joostvdg.dui.logging.Logger;
import com.github.joostvdg.dui.server.api.DuiServer;
import com.github.joostvdg.dui.server.api.Membership;
import com.github.joostvdg.dui.server.handler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.joostvdg.dui.api.ProtocolConstants.*;

public class ServerSimpleImpl implements DuiServer {
    private volatile boolean stopped = false;

    private ServerSocket serverSocket;

    private final TaskGate taskGate;

    private final ConcurrentHashMap<Integer, Membership> membershipList;
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
            if (internalPort != i) {
                Membership membership = new Membership(""+i, System.currentTimeMillis());
                membershipList.put(i, membership);
            }
        }
    }

    @Override
    public void stopServer(){
        sendMembershipLeaveMessage();
        synchronized (this) {
            this.stopped = true;
            long threadId = Thread.currentThread().getId();
            logger.log(LogLevel.INFO, mainComponent, "Main", threadId, " Stopping");
            closeServer();
        }
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

    @Override
    public synchronized boolean isStopped() {
        return this.stopped;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void updateMembershipList(String host, int port, String serverName, boolean active) {
        // we ignore host, as the simple server is single host
        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, "Main", threadId, " updateMembershipList ", port+ ",", serverName, ",active="+ active);
        if (active) {
            handleActiveMember(port, serverName);
        } else {
            handleInactiveMember(port, serverName);
        }
    }

    private void handleInactiveMember(int port, String serverName) {
        long threadId = Thread.currentThread().getId();
        if (membershipList.containsKey(port)) {
            if (membershipList.get(port).getName().equalsIgnoreCase(serverName)) {
                logger.log(LogLevel.WARN, mainComponent, "Main", threadId, " Removing ", serverName, " from membership because is it no longer active");
                membershipList.remove(port);
            } else {
                logger.log(LogLevel.WARN, mainComponent, "Main", threadId, " Membership of '", serverName, "' not valid, because port "+port, " is claimed by '", membershipList.get(port).getName()+ "'");
            }
        } else {
            logger.log(LogLevel.WARN, mainComponent, "Main", threadId, " Received invalid membership for port " + port);
        }
    }

    private void handleActiveMember(int port, String serverName) {
        if (membershipList.containsKey(port) && membershipList.get(port).getName().equals(serverName)) {
            Membership existingMemberShip = membershipList.get(port);
            existingMemberShip.updateLastSeen(System.currentTimeMillis());
        } else {
            Membership newMembership = new Membership(serverName, System.currentTimeMillis());
            membershipList.put(port, newMembership);
        }
    }

    @Override
    public void logMembership() {
        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, "Main", threadId, " Listing memberships");
        membershipList.keySet().forEach(port -> {
           Membership membership = membershipList.get(port);
           logger.log(LogLevel.INFO, mainComponent, "Main", threadId, "  > ", membership.toString());
        });
    }

    private void listenToExternalCommunication() {
        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, "External", threadId, " Started");
        try(ServerSocket serverSocket = new ServerSocket(externalPort)) {

            while(!isStopped()) {
                String status = "running";
                if (isStopped()) {
                    status = "stopped";
                }
                logger.log(LogLevel.DEBUG, mainComponent, "External", threadId, " Status: ", status);
                try  {
                    Socket clientSocket = serverSocket.accept();
                    Runnable clientHandler = new ClientHandler(clientSocket, logger, this);
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
                    Runnable clientHandler = new ClientHandler(clientSocket, logger, this);
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

    private void sendMembershipLeaveMessage() {
        DuiClient client = DuiClientFactory.newSimpleClient();
        long threadId = Thread.currentThread().getId();
        // TODO: 1 - make this multi-cast
        membershipList.keySet().forEach(port -> {
            Membership membership = membershipList.get(port);
            try {
                String message = internalPort + MESSAGE_SEGMENT_DELIMITER + name + MESSAGE_SEGMENT_DELIMITER + MEMBERSHIP_LEAVE_MESSAGE;
                client.sendServerMessage(FeiwuMessageType.MEMBERSHIP, message.getBytes(), port);
            } catch (MessageTargetNotAvailableException | MessageDeliveryException | MessageTargetDoesNotExistException e) {
                logger.log(LogLevel.WARN, mainComponent, "Main", threadId, " Could not send leave notice to ", membership.toString());
            }
        });
    }

    private void sendMemberShipUpdate() {
        DuiClient client = DuiClientFactory.newSimpleClient();
        long threadId = Thread.currentThread().getId();
        while (!stopped) {

            membershipList.keySet().forEach(port -> {
                Membership membership = membershipList.get(port);
                long currentTime = System.currentTimeMillis();
                if (membership.failedCheckCount() > 2 || (currentTime - membership.getLastSeen()) > 15000){
                    String reasonForRemoval = " because we haven't seen the server for 15000 milisec";
                    if (membership.failedCheckCount() > 2) {
                        reasonForRemoval = " because of to many failed checks";
                    }
                    membershipList.remove(port);
                    logger.log(LogLevel.WARN, mainComponent, "Main", threadId, " Removed ", membership.toString(), reasonForRemoval);
                } else {
                    try {
                        client.sendServerMessage(FeiwuMessageType.MEMBERSHIP, (internalPort+MESSAGE_SEGMENT_DELIMITER+name).getBytes(), port);
                    } catch (MessageTargetNotAvailableException e) {
                        logger.log(LogLevel.WARN, mainComponent, "Main", threadId, " Could not contact ", membership.toString());
                        membership.incrementFailedCheckCount();
                    } catch (MessageDeliveryException | MessageTargetDoesNotExistException e) { // TODO: when changing to multi-host, we should do something else
                        membership.incrementFailedCheckCount();
                        logger.log(LogLevel.ERROR, mainComponent, "Main", threadId, e.getMessage());
                    }
                }

            });
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

        // Lets first wait before we start updating the membership lists
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.submit(this::sendMemberShipUpdate);
    }

}
