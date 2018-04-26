package com.github.joostvdg.dui.server.api.impl;

import com.github.joostvdg.dui.api.ProtocolConstants;
import com.github.joostvdg.dui.api.exception.MessageDeliveryException;
import com.github.joostvdg.dui.api.exception.MessageTargetDoesNotExistException;
import com.github.joostvdg.dui.api.exception.MessageTargetNotAvailableException;
import com.github.joostvdg.dui.api.Feiwu;
import com.github.joostvdg.dui.api.message.FeiwuMessage;
import com.github.joostvdg.dui.api.message.FeiwuMessageType;
import com.github.joostvdg.dui.api.message.MessageOrigin;
import com.github.joostvdg.dui.logging.LogLevel;
import com.github.joostvdg.dui.logging.Logger;
import com.github.joostvdg.dui.server.api.DuiServer;
import com.github.joostvdg.dui.api.Membership;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.joostvdg.dui.api.ProtocolConstants.HEALTH_CHECK_PORT;


public class DistributedServer implements DuiServer {
    private volatile boolean stopped = false;
    private volatile boolean closed = false;

    private final ExecutorService socketExecutors;
    private final ExecutorService messageHandlerExecutor;

    private static final int MAX_NUMBER_MEMBERS = 5;

    private final ConcurrentHashMap<String, Membership> membershipList;
    private final ConcurrentHashMap<byte[], Long> recentProcessedMessages;

    private final Logger logger;
    private final String name;
    private final String mainComponent;
    private ServerSocket serverSocket;
    private final int externalPort;
    private final int internalPort;
    private final int groupPort;
    private final String membershipGroup;
    private final MessageOrigin messageOrigin;

    private static final int MAX_RECENT_DIGEST = 100;
    private static final int MAX_AGE_RECENT_DIGEST = 1000 * 60; // one minute

    private static final byte[] INTERNAL_SERVER_ERROR_RESPONSE = "HTTP/1.0 500 Internal Server Error\r\n".getBytes();
    private static final byte[] SERVICE_TEMPORARILY_UNAVAILABLE_RESPONSE = "HTTP/1.0 503 Service Unavailable\r\n".getBytes();
    private static final byte[] OK_RESPONSE = "HTTP/1.1 200 OK\r\n".getBytes();

    public DistributedServer(int listenPort, String membershipGroup, String name, Logger logger) {
        this.name = name;
        this.mainComponent = "Server-" + name;
        this.externalPort = listenPort;
        this.internalPort = externalPort + 10;
        this.groupPort = externalPort + 20;
        this.membershipGroup = membershipGroup;
        this.logger = logger;

        membershipList = new ConcurrentHashMap<>();
        recentProcessedMessages = new ConcurrentHashMap<>();

        socketExecutors = Executors.newFixedThreadPool(6);
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
                            if (messageOrigin.equals(this.messageOrigin) || recentProcessedMessages.containsKey(feiwuMessage.getDigest())){
                                // its ourselves, don't process it, or we have already processed it
                                return;
                            }

                            recentProcessedMessages.put(feiwuMessage.getDigest(), System.currentTimeMillis());
                            if (recentProcessedMessages.size() <= MAX_RECENT_DIGEST) {
                                removeOldestRecentDigest();
                            }

                            if (feiwuMessage.getType().equals(FeiwuMessageType.MEMBERSHIP)) {
                                Runnable runnable = () -> updateMemberShipList(feiwuMessage);
                                messageHandlerExecutor.submit(runnable);
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

    private void listenToHealthCheck() {
        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, "HealthCheck", threadId, " Going to listen on port " + HEALTH_CHECK_PORT, " for health checks");
        ServerSocket healthCheckSocket = null;
        try {
            Socket clientSocket;
            try {
                healthCheckSocket = new ServerSocket(HEALTH_CHECK_PORT);
            } catch (IOException e1) {
                throw new RuntimeException(e1.getMessage());
            }
            while(!isStopped()) {
                clientSocket = healthCheckSocket.accept();
                try (OutputStream out = clientSocket.getOutputStream()) {

                    if (closed) {
                        logger.log(LogLevel.ERROR, mainComponent, "HealthCheck", threadId, " Closed, 500");
                        out.write(INTERNAL_SERVER_ERROR_RESPONSE);
                    } else if (isStopped()) {
                        logger.log(LogLevel.WARN, mainComponent, "HealthCheck", threadId, " Stopped, 503");
                        out.write(SERVICE_TEMPORARILY_UNAVAILABLE_RESPONSE);
                    } else {
                        logger.log(LogLevel.INFO, mainComponent, "HealthCheck", threadId, " 200 OK");
                        out.write(OK_RESPONSE);
                    }
                    out.flush();
                    out.close();
                } finally {
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (healthCheckSocket != null && !healthCheckSocket.isClosed()) {
                try {
                    healthCheckSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
                    recentProcessedMessages.put(feiwuMessage.getDigest(), System.currentTimeMillis());

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
        byte[] messageDigest = feiwuMessage.getDigest();
        if (feiwuMessage.getMessage().equals(ProtocolConstants.MEMBERSHIP_LEAVE_MESSAGE)) {
            logger.log(LogLevel.WARN, mainComponent, "Main", threadId, "Received membership leave notice from ", messageOrigin.toString());
            membershipList.remove(messageOrigin.getHost());
            try {
                propagateMembershipLeaveNotice(messageOrigin, messageDigest);
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

    private void propagateMembershipLeaveNotice(MessageOrigin messageOriginLeaver, byte[] messageDigest) throws MessageTargetDoesNotExistException, MessageDeliveryException, MessageTargetNotAvailableException {
        if (recentProcessedMessages.containsKey(messageDigest)) {
            long threadId = Thread.currentThread().getId();
            logger.log(LogLevel.WARN, mainComponent, "Main", threadId, " not propagating message as digest is in recent list");
        } else {
            for (String hostname : membershipList.keySet()) {
                sendMembershipLeaveMessage(hostname, messageOriginLeaver);
            }
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
        socketExecutors.submit(this::listenToHealthCheck);
        socketExecutors.submit(this::listenToInternalCommunication);
        socketExecutors.submit(this::listenToInternalGroup);
        socketExecutors.submit(this::cleanUpRecentDigests);

        // Lets first wait before we start updating the membership lists
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        socketExecutors.submit(this::sendMemberShipUpdates);
    }

    // TODO: http://www.baeldung.com/java-8-comparator-comparing
    //      http://www.baeldung.com/java-collection-min-max
    // http://www.java67.com/2017/06/how-to-remove-entry-keyvalue-from-HashMap-in-java.html
    private void removeOldestRecentDigest() {
        Long oldest = recentProcessedMessages.values().stream().mapToLong(v -> v).min().orElse(0L);
        if (oldest != 0L) {
            recentProcessedMessages.values().removeAll(Collections.singleton(oldest));
        }
    }

    private void cleanUpRecentDigests() {
        while (!stopped) {
            long currentTime = System.currentTimeMillis();
            List<byte[]> keysToRemove = new ArrayList<>();
            recentProcessedMessages.forEach((k,v) -> {
                if (currentTime - v > MAX_AGE_RECENT_DIGEST) {
                    keysToRemove.add(k);
                }
            });
            for (byte[] key : keysToRemove) {
                recentProcessedMessages.remove(key);
            }

            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                long threadId = Thread.currentThread().getId();
                logger.log(LogLevel.WARN, mainComponent, "Main", threadId, "Interrupted, going to close");
            }
        }
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
        logger.log(LogLevel.INFO, mainComponent, "Main", threadId, " Currently holding ", ""+recentProcessedMessages.size(), " recent messages");
    }
}
