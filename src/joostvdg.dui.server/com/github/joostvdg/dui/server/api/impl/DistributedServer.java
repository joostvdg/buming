package com.github.joostvdg.dui.server.api.impl;

import com.github.joostvdg.dui.api.ProtocolConstants;
import com.github.joostvdg.dui.api.message.Feiwu;
import com.github.joostvdg.dui.api.message.FeiwuMessage;
import com.github.joostvdg.dui.api.message.FeiwuMessageType;
import com.github.joostvdg.dui.api.message.MessageOrigin;
import com.github.joostvdg.dui.logging.LogLevel;
import com.github.joostvdg.dui.logging.Logger;
import com.github.joostvdg.dui.server.api.DuiServer;
import com.github.joostvdg.dui.server.api.Membership;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DistributedServer implements DuiServer {
    private volatile boolean stopped = false;
    private volatile boolean closed = false;

    private final ExecutorService socketExecutors;
    private final ExecutorService messageHandlerExecutor;

    private final ConcurrentHashMap<String, Membership> membershipList;
    private final Logger logger;
    private final String name;
    private final String mainComponent;
    private final int externalPort;
    private final int internalPort;
    private final String membershipGroup;
    private final MessageOrigin messageOrigin;

    public DistributedServer(int listenPort, String membershipGroup, String name, Logger logger) {
        this.name = name;
        this.mainComponent = "Server-" + name;
        this.externalPort = listenPort;
        this.internalPort = externalPort + 10;
        this.membershipGroup = membershipGroup;
        this.logger = logger;
        membershipList = new ConcurrentHashMap<>();

        socketExecutors = Executors.newFixedThreadPool(3);
        messageHandlerExecutor = Executors.newFixedThreadPool(3);
        messageOrigin = MessageOrigin.getCurrentOrigin(name);
    }

    private void listenToInternalCommunication() {
        long threadId = Thread.currentThread().getId();
        byte[] buf = new byte[256];
        try(MulticastSocket socket = new MulticastSocket(internalPort)) {
            InetAddress group = InetAddress.getByName(membershipGroup);
            socket.joinGroup(group);
            while(!isStopped()) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                FeiwuMessage feiwuMessage = Feiwu.fromBytes(packet.getData());
                MessageOrigin messageOrigin = feiwuMessage.getMessageOrigin();
                if (!messageOrigin.getHost().equals(this.messageOrigin.getHost())) { // we received our own message, no need to deal with this

                    logger.log(LogLevel.INFO, mainComponent, "Main", threadId, " Received multicast: ", feiwuMessage.toString());

                    switch (feiwuMessage.getType()) {
                        case MEMBERSHIP:
                            Runnable runnable = () -> updateMemberShipList(feiwuMessage);
                            messageHandlerExecutor.submit(runnable);
                            break;
                        default:
                            logger.log(LogLevel.WARN, mainComponent, "Main", threadId, " Received multicast of unsupported type: ", feiwuMessage.toString());
                    }
                }
            }
            socket.leaveGroup(group);
        } catch (IOException e) {
            logger.log(LogLevel.ERROR, mainComponent, "Main", threadId, e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateMemberShipList(final FeiwuMessage feiwuMessage) {
        long threadId = Thread.currentThread().getId();
        MessageOrigin messageOrigin = feiwuMessage.getMessageOrigin();
        if (feiwuMessage.getMessage().equals(ProtocolConstants.MEMBERSHIP_LEAVE_MESSAGE)) {
            logger.log(LogLevel.WARN, mainComponent, "Main", threadId, "Received membership leave notice from ", messageOrigin.toString());
            membershipList.remove(messageOrigin.getHost());
        } else {
            if (membershipList.containsKey(messageOrigin.getHost())) {
                updateMember(messageOrigin);
            } else {
                addMember(messageOrigin);
            }
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
            long currentTime = System.currentTimeMillis();
            Membership newMembership = new Membership(messageOrigin.getName(), currentTime);
            membershipList.put(messageOrigin.getHost(), newMembership);
        }
    }

    private void sendMemberShipUpdates(){
        try (DatagramSocket socket = new DatagramSocket()) {
            while (!stopped) {
                InetAddress group = InetAddress.getByName(membershipGroup);
                String message = "Hello from " + name;
                Feiwu feiwu = new Feiwu(FeiwuMessageType.MEMBERSHIP, message, this.messageOrigin);
                byte[] buf = feiwu.writeToBuffer();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, internalPort);
                socket.send(packet);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
            sendLeaveMessage();
        }
    }

    private void sendLeaveMessage() {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(membershipGroup);
            String message = ProtocolConstants.MEMBERSHIP_LEAVE_MESSAGE;
            Feiwu feiwu = new Feiwu(FeiwuMessageType.MEMBERSHIP, message, this.messageOrigin);
            byte[] buf = feiwu.writeToBuffer();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, internalPort);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
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
