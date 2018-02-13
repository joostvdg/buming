package com.github.joostvdg.dui.server.api.impl;

import com.github.joostvdg.dui.api.ProtocolConstants;
import com.github.joostvdg.dui.api.exception.MessageDeliveryException;
import com.github.joostvdg.dui.api.exception.MessageTargetDoesNotExistException;
import com.github.joostvdg.dui.api.exception.MessageTargetNotAvailableException;
import com.github.joostvdg.dui.api.message.Feiwu;
import com.github.joostvdg.dui.api.message.FeiwuMessage;
import com.github.joostvdg.dui.api.message.FeiwuMessageType;
import com.github.joostvdg.dui.api.message.MessageOrigin;
import com.github.joostvdg.dui.logging.LogLevel;
import com.github.joostvdg.dui.logging.Logger;
import com.github.joostvdg.dui.server.api.DuiServer;
import com.github.joostvdg.dui.server.api.Membership;
import com.github.joostvdg.dui.server.handler.ClientHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DistributedServer implements DuiServer {
    private volatile boolean stopped = false;
    private volatile boolean closed = false;

    private final ExecutorService socketExecutors;
    private final ExecutorService messageHandlerExecutor;

    private static final int MAX_NUMBER_MEMBERS = 5;

    private final ConcurrentHashMap<String, Membership> membershipList;
    private final Logger logger;
    private final String name;
    private final String mainComponent;
    private ServerSocket serverSocket;
    private final int externalPort;
    private final int internalPort;
    private final int groupPort;
    private final String membershipGroup;
    private final MessageOrigin messageOrigin;

    public DistributedServer(int listenPort, String membershipGroup, String name, Logger logger) {
        this.name = name;
        this.mainComponent = "Server-" + name;
        this.externalPort = listenPort;
        this.internalPort = externalPort + 10;
        this.groupPort = externalPort + 20;
        this.membershipGroup = membershipGroup;
        this.logger = logger;
        membershipList = new ConcurrentHashMap<>();

        socketExecutors = Executors.newFixedThreadPool(4);
        messageHandlerExecutor = Executors.newFixedThreadPool(3);
        messageOrigin = MessageOrigin.getCurrentOrigin(name);
    }

    private void listenToInternalCommunication() {
        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, "Internal", threadId, " Going to listen on port " + internalPort, " for internal communication");
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
                    logger.log(LogLevel.DEBUG,mainComponent, "Internal", threadId, " Socket Established on Port: ", ""+ clientSocket.getRemoteSocketAddress());
                    try (BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream())) {
                        try {

                            FeiwuMessage feiwuMessage = Feiwu.fromInputStream(in);
                            MessageOrigin messageOrigin = feiwuMessage.getMessageOrigin();
                            if (feiwuMessage.getType().equals(FeiwuMessageType.MEMBERSHIP)) {
                                // TODO: implement this
                                // Runnable runnable = () -> updateMemberShipList(feiwuMessage);
                                // messageHandlerExecutor.submit(runnable);
                                logger.log(LogLevel.WARN, mainComponent,"Internal", threadId, " Received: ", feiwuMessage.toString());
                            } else {
                                logger.log(LogLevel.INFO, mainComponent,"Internal", threadId, " Received: ", feiwuMessage.toString());
                            }


                        } catch (IOException e1) {
                            logger.log(LogLevel.WARN, mainComponent,"Internal", threadId, " Error while reading message: ", e1.getMessage());
                            e1.printStackTrace();
                        }
                    }

                    try {
                        Thread.sleep(5);
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

    private void listenToInternalGroup() {
        long threadId = Thread.currentThread().getId();
        byte[] buf = new byte[256];
        try(MulticastSocket socket = new MulticastSocket(groupPort)) {
            InetAddress group = InetAddress.getByName(membershipGroup);
            logger.log(LogLevel.INFO, mainComponent, "Group", threadId, " Going to listen on port " + groupPort, " for group communication ("+ membershipGroup+")");
            socket.joinGroup(group);
            while(!isStopped()) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                FeiwuMessage feiwuMessage = Feiwu.fromBytes(packet.getData());
                MessageOrigin messageOrigin = feiwuMessage.getMessageOrigin();
                if (!messageOrigin.getHost().equals(this.messageOrigin.getHost())) { // we received our own message, no need to deal with this

                    logger.log(LogLevel.INFO, mainComponent, "Group", threadId, " Received: ", feiwuMessage.toString());

                    switch (feiwuMessage.getType()) {
                        case MEMBERSHIP:
                            Runnable runnable = () -> updateMemberShipList(feiwuMessage);
                            messageHandlerExecutor.submit(runnable);
                            break;
                        default:
                            logger.log(LogLevel.WARN, mainComponent, "Group", threadId, " Received unsupported type: ", feiwuMessage.toString());
                    }
                }
            }
            socket.leaveGroup(group);
        } catch (IOException e) {
            logger.log(LogLevel.ERROR, mainComponent, "Group", threadId, e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateMemberShipList(final FeiwuMessage feiwuMessage) {
        long threadId = Thread.currentThread().getId();
        MessageOrigin messageOrigin = feiwuMessage.getMessageOrigin();
        if (feiwuMessage.getMessage().equals(ProtocolConstants.MEMBERSHIP_LEAVE_MESSAGE)) {
            logger.log(LogLevel.WARN, mainComponent, "Main", threadId, "Received membership leave notice from ", messageOrigin.toString());
            membershipList.remove(messageOrigin.getHost());
            try {
                propagateMembershipLeaveNotice(messageOrigin);
            } catch (MessageTargetDoesNotExistException | MessageDeliveryException | MessageTargetNotAvailableException e) {
                logger.log(LogLevel.ERROR, mainComponent, "Main", threadId, e.getMessage());
                e.printStackTrace();
            }
        } else {
            if (membershipList.containsKey(messageOrigin.getHost())) {
                updateMember(messageOrigin);
            } else {
                addMember(messageOrigin);
            }
        }
    }

    private void propagateMembershipLeaveNotice(MessageOrigin messageOriginLeaver) throws MessageTargetDoesNotExistException, MessageDeliveryException, MessageTargetNotAvailableException {
        for(String hostname : membershipList.keySet()) {
            sendMembershipLeaveMessage(hostname, messageOriginLeaver);
        }
    }

    private void sendMembershipLeaveMessage(String targetHostname,MessageOrigin messageOriginLeaver) throws MessageTargetDoesNotExistException, MessageDeliveryException, MessageTargetNotAvailableException {
        try (Socket socket = new Socket(targetHostname, internalPort)) {
            try (OutputStream mOutputStream = socket.getOutputStream()) {
                try (BufferedOutputStream out = new BufferedOutputStream(mOutputStream)) {
                    Feiwu feiwuMessage = new Feiwu(FeiwuMessageType.MEMBERSHIP, ProtocolConstants.MEMBERSHIP_LEAVE_MESSAGE, messageOriginLeaver);
                    feiwuMessage.writeMessage(out);
                    out.flush();
                }
            }
        } catch (UnknownHostException e) {
            throw new MessageTargetDoesNotExistException("Could not send message to host" + targetHostname);
        } catch (IOException e) {
            if (e instanceof ConnectException) {
                throw new MessageTargetNotAvailableException("Could not send message to " + targetHostname+ " on port " + internalPort);
            }
            throw new MessageDeliveryException("Could deliver message to " + internalPort + " because " + e.getMessage());
        }
    }

    private void updateMember(MessageOrigin messageOrigin) {
        long threadId = Thread.currentThread().getId();
        Membership membership = membershipList.get(messageOrigin.getHost());
        if (membership == null) {
            logger.log(LogLevel.WARN, mainComponent, "Main", threadId, "Attempt to update member that does not exist");
        } else {
            long currentTime = System.currentTimeMillis();
            membership.updateLastSeen(currentTime);
        }
    }

    private void addMember(MessageOrigin messageOrigin) {
        long threadId = Thread.currentThread().getId();
        Membership membership = membershipList.get(messageOrigin.getHost());
        if (membership != null) {
            logger.log(LogLevel.WARN, mainComponent, "Main", threadId, "Attempt to add new member that already exists");
        } else {
            if (membershipList.size() >= MAX_NUMBER_MEMBERS) {
                removeLastSeenMember();
            }
            long currentTime = System.currentTimeMillis();
            Membership newMembership = new Membership(messageOrigin.getName(), currentTime);
            membershipList.put(messageOrigin.getHost(), newMembership);
        }
    }

    private void removeLastSeenMember() {
        long lastSeen = System.currentTimeMillis();
        String keyToRemove = null;
        for(String key : membershipList.keySet()) {
            Membership membership = membershipList.get(key);
            if (membership.getLastSeen() < lastSeen) {
                keyToRemove = key.toString();
            }
        }
        if (keyToRemove != null) {
            membershipList.remove(keyToRemove);
        }
    }

    private void sendMemberShipUpdates(){
        try (DatagramSocket socket = new DatagramSocket()) {
            while (!stopped) {
                InetAddress group = InetAddress.getByName(membershipGroup);
                String message = "Hello from " + name;
                Feiwu feiwu = new Feiwu(FeiwuMessageType.MEMBERSHIP, message, this.messageOrigin);
                byte[] buf = feiwu.writeToBuffer();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, groupPort);
                socket.send(packet);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    long threadId = Thread.currentThread().getId();
                    logger.log(LogLevel.WARN, mainComponent, "Main", threadId, "Interrupted, going to close");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startServer() {
        if (closed) {
            throw new IllegalStateException("Server is already closed!");
        }

        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, "Main", threadId, " Starting: ", this.messageOrigin.toString());
//        executorService.submit(this::listenToExternalCommunication);
        socketExecutors.submit(this::listenToInternalCommunication);
        socketExecutors.submit(this::listenToInternalGroup);

        // Lets first wait before we start updating the membership lists
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        socketExecutors.submit(this::sendMemberShipUpdates);
    }

    @Override
    public void stopServer() {
        synchronized (this) {
            this.stopped = true;
            long threadId = Thread.currentThread().getId();
            logger.log(LogLevel.INFO, mainComponent, "Main", threadId, " Stopping");
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendLeaveMessage();
        }
    }

    private void sendLeaveMessage() {
        for(String hostname : membershipList.keySet()) {
            try {
                sendMembershipLeaveMessage(hostname, messageOrigin);
            } catch (MessageTargetDoesNotExistException | MessageDeliveryException | MessageTargetNotAvailableException e) {
                long threadId = Thread.currentThread().getId();
                logger.log(LogLevel.WARN, mainComponent, "Main", threadId, "Could not deliver leave notice to ", hostname, ",because: ", e.getMessage());
            }
        }
    }

    @Override
    public void updateMembershipList(String host, int port, String serverName, boolean active) {

    }

    @Override
    public void closeServer() {
        synchronized (this) {
            this.closed = true;
            long threadId = Thread.currentThread().getId();
            logger.log(LogLevel.INFO, mainComponent, "Main", threadId, " Closing");
        }
        socketExecutors.shutdown();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        socketExecutors.shutdownNow();
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
    public void logMembership() {
        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, "Main", threadId, " Listing memberships");
        membershipList.keySet().forEach(port -> {
            Membership membership = membershipList.get(port);
            logger.log(LogLevel.INFO, mainComponent, "Main", threadId, "  > ", membership.toString());
        });
    }
}
