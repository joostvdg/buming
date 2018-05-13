package com.github.joostvdg.dui.server.api.impl;

import com.github.joostvdg.dui.api.*;
import com.github.joostvdg.dui.api.exception.MessageDeliveryException;
import com.github.joostvdg.dui.api.exception.MessageTargetDoesNotExistException;
import com.github.joostvdg.dui.api.exception.MessageTargetNotAvailableException;
import com.github.joostvdg.dui.api.message.FeiwuMessage;
import com.github.joostvdg.dui.api.message.FeiwuMessageType;
import com.github.joostvdg.dui.api.message.MessageOrigin;
import com.github.joostvdg.dui.logging.LogLevel;
import com.github.joostvdg.dui.logging.Logger;
import com.github.joostvdg.dui.server.api.DuiServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

public class DistributedServer implements DuiServer {

    private static final int MAX_NUMBER_MEMBERS = 15;
    private static final byte[] INTERNAL_SERVER_ERROR_RESPONSE = "HTTP/1.0 500 Internal Server Error\r\n".getBytes();
    private static final byte[] SERVICE_TEMPORARILY_UNAVAILABLE_RESPONSE = "HTTP/1.0 503 Service Unavailable\r\n".getBytes();
    private static final byte[] OK_RESPONSE = "HTTP/1.1 200 OK\r\n".getBytes();

    private static final class LogComponents {
        private static final String INTERNAL = "Internal";
        private static final String HEALTH_CHECK = "HealthCheck";
        private static final String MAIN = "Main";
        private static final String GROUP = "Group";
        private static final String INIT = "Init";
        private static final String LEADER_ELECTION = "LeaderElection";
    }

    private static final class Defaults {
        private static final int MAX_RECENT_DIGEST = 100;
        private static final int MAX_AGE_RECENT_DIGEST = 1000 * 60; // one minute
        private static final int MAX_AGE_MISSING_MEMBER = 1000 * 60; // one minute
        private static final long MEMBERSHIP_UPDATE_RATE_IN_MILLIS = 5000;
        private static final byte NODE_ROLE = 0x00; // manager
    }

    private static final class ENV {
        private static final String MAX_RECENT_DIGEST = "MAX_RECENT_DIGEST";
        private static final String MAX_AGE_RECENT_DIGEST = "MAX_AGE_RECENT_DIGEST";
        private static final String MAX_AGE_MISSING_MEMBER = "MAX_AGE_MISSING_MEMBER";
        private static final String MEMBERSHIP_UPDATE_RATE_IN_MILLIS = "membershipUpdateRateInMillis";
        private static final String NODE_ROLE = "NODE_ROLE";
    }

    private final ExecutorService leaderElectionExecutor;
    private final ExecutorService socketExecutors;
    private final ExecutorService messageHandlerExecutor;
    private final ConcurrentHashMap<String, Membership> membershipList;

    /* For any processed messages we get from outside
     * TODO: we should probably filter on types
     */
    private final ConcurrentHashMap<byte[], Long> recentProcessedMessages;

    /*
     * For any specific message we've send out, such as leave propagation, data, leadership election etc.
     */
    private final ConcurrentHashMap<byte[], Long> recentSendMessages;

    private final Logger logger;
    private final String name;
    private final String mainComponent;
    private final int internalPort;
    private final int groupPort;
    private final int healthCheckPort;
    private final String membershipGroup;
    private final MessageOrigin messageOrigin;

    /* PARAMETERS */
    private final int maxRecentDigest;
    private final int maxAgeRecentDigest;
    private final int maxAgeMissingMember;
    private final long membershipUpdateRateInMillis;
    private final byte role;
    private final Node node;

    private ServerSocket serverSocket;
    private AtomicInteger leaderElectionRound;
    private volatile boolean stopped = false;
    private volatile boolean closed = false;
    private volatile long leaderElectionTimeout;
    private volatile long leaderElectionTimerStart;

    public DistributedServer(final int listenPort, final String membershipGroup, final String name, final Logger logger) {
        this.name = name;
        this.mainComponent = "Server-" + name;
        this.internalPort = listenPort + 10;
        this.groupPort = listenPort + 20;
        this.healthCheckPort = ProtocolConstants.HEALTH_CHECK_PORT;
        this.membershipGroup = membershipGroup;
        this.logger = logger;

        membershipList = new ConcurrentHashMap<>();
        recentProcessedMessages = new ConcurrentHashMap<>();
        recentSendMessages = new ConcurrentHashMap<>();

        membershipUpdateRateInMillis = getLongEnvOrDefault(ENV.MEMBERSHIP_UPDATE_RATE_IN_MILLIS, Defaults.MEMBERSHIP_UPDATE_RATE_IN_MILLIS);
        maxRecentDigest = getIntEnvOrDefault(ENV.MAX_RECENT_DIGEST, Defaults.MAX_RECENT_DIGEST);
        maxAgeRecentDigest = getIntEnvOrDefault(ENV.MAX_AGE_RECENT_DIGEST, Defaults.MAX_AGE_RECENT_DIGEST);
        maxAgeMissingMember = getIntEnvOrDefault(ENV.MAX_AGE_MISSING_MEMBER, Defaults.MAX_AGE_MISSING_MEMBER);
        role = getByteEnvOrDefault(ENV.NODE_ROLE, Defaults.NODE_ROLE);

        var roleDescription = role == 0x00 ? "Manager" : "Worker";
        socketExecutors = Executors.newFixedThreadPool(8);
        messageHandlerExecutor = Executors.newFixedThreadPool(3);
        leaderElectionExecutor = Executors.newSingleThreadExecutor();
        messageOrigin = MessageOrigin.getCurrentOrigin(name, roleDescription);
        node = new Node(name, messageOrigin.getHost(), messageOrigin.getIp());
        node.setRole(role);
        node.setStatus(LeaderElectionStatus.NONE);
        node.updateUptime();

        final long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, LogComponents.INIT, threadId, "Name\t\t\t\t:: ", name);
        logger.log(LogLevel.INFO, mainComponent, LogComponents.INIT, threadId, "Role\t\t\t\t:: ", roleDescription);
        logger.log(LogLevel.INFO, mainComponent, LogComponents.INIT, threadId, "Internal Listening Port\t\t:: " + internalPort);
        logger.log(LogLevel.INFO, mainComponent, LogComponents.INIT, threadId, "HealthCheck Listening Port\t:: " + healthCheckPort);
        logger.log(LogLevel.INFO, mainComponent, LogComponents.INIT, threadId, "Group Listening Port\t\t:: " + groupPort);
        logger.log(LogLevel.INFO, mainComponent, LogComponents.INIT, threadId, "Group Listening Group\t\t:: ", membershipGroup);
        logger.log(LogLevel.INFO, mainComponent, LogComponents.INIT, threadId, "Message Origin\t\t\t:: " + messageOrigin);
        logger.log(LogLevel.INFO, mainComponent, LogComponents.INIT, threadId, "Node\t\t\t:: " + node.toString());

        logSystemInfo(threadId);

        leaderElectionRound = new AtomicInteger(0);
        setLeaderElectionTimeout();
    }

    @Override
    public void startServer() {
        if (closed) {
            throw new IllegalStateException("Server is already closed!");
        }

        final long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, LogComponents.MAIN, threadId, " Starting: ", this.messageOrigin.toString());
        socketExecutors.submit(this::listenToHealthCheck);
        socketExecutors.submit(this::listenToInternalCommunication);
        socketExecutors.submit(this::listenToInternalGroup);
        socketExecutors.submit(this::cleanUpRecentDigests);

        leaderElectionExecutor.submit(this::handleLeadershipElectionCycles);

        // Lets first wait before we start updating the membership lists
        pauseAndWait(membershipUpdateRateInMillis, threadId);
        socketExecutors.submit(this::sendMemberShipUpdates);
        socketExecutors.submit(this::checkMembers);
        socketExecutors.submit(this::removeFailedMembers);
    }

    private void handleLeadershipElectionCycles(){
        final long threadId = Thread.currentThread().getId();
        // go into election proposal mode
        electionProposal();

        // loop every x millis,waiting for timer
        while (!stopped) {
            if (System.currentTimeMillis() > (leaderElectionTimeout + leaderElectionTimerStart)) {
                electionProposal();
            }
            try {
                sleep(100);
            } catch (InterruptedException e) {
                logger.log(LogLevel.WARN, mainComponent, LogComponents.LEADER_ELECTION, threadId, " Something went wrong waiting for timer" );
                Thread.currentThread().interrupt();
            }
        }
    }

    // TODO: complete this
    private void electionProposal() {
        final long threadId = Thread.currentThread().getId();
        setLeaderElectionTimeout();
        node.setStatus(LeaderElectionStatus.CANDIDATE);
        logger.log(LogLevel.INFO, mainComponent, LogComponents.LEADER_ELECTION, threadId, "Electing myself leader [status: ", node.getStatus().toString(), ",round: " +leaderElectionRound, "]");
        leaderElectionTimerStart = System.currentTimeMillis();
        leaderElectionRound.getAndIncrement();
    }

    // TODO: introduce leader election message type
    // if election message received, and NOT in election mode, accept leader and reset timer
    // if election message received, and IN election mode, ignore message

    private void setLeaderElectionTimeout() {
        final long threadId = Thread.currentThread().getId();
        leaderElectionTimeout = new Random().nextInt(3000)
            + new Random().nextInt(3000)
            + new Random().nextInt(3000)
            + new Random().nextInt(3000)
            + 8000L;
        logger.log(LogLevel.INFO, mainComponent, LogComponents.LEADER_ELECTION, threadId, "Leader election timer set to: " + leaderElectionTimeout);
    }

    private long getLongEnvOrDefault(String envKey, long defaultValue) {
        return System.getenv(envKey) != null ? Long.parseLong(System.getenv(envKey)) : defaultValue;
    }

    private int getIntEnvOrDefault(String envKey, int defaultValue) {
        return System.getenv(envKey) != null ? Integer.parseInt(System.getenv(envKey)) : defaultValue;
    }

    private byte getByteEnvOrDefault(String envKey, byte defaultValue) {
        return System.getenv(envKey) != null ? Byte.parseByte(System.getenv(envKey)) : defaultValue;
    }


    private void logSystemInfo(final long threadId) {
        long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory == Long.MAX_VALUE) {
            maxMemory = 0L;
        } else {
            maxMemory = maxMemory / 1024 / 1024;
        }
        logger.log(LogLevel.INFO, mainComponent, LogComponents.INIT, threadId, "Available Processors\t\t:: " + Runtime.getRuntime().availableProcessors());
        logger.log(LogLevel.INFO, mainComponent, LogComponents.INIT, threadId, "Free Memory\t\t\t:: " + Runtime.getRuntime().freeMemory() / 1024 / 1024, "MB");
        logger.log(LogLevel.INFO, mainComponent, LogComponents.INIT, threadId, "Total Memory\t\t\t:: " + Runtime.getRuntime().totalMemory() / 1024 / 1024, "MB");
        logger.log(LogLevel.INFO, mainComponent, LogComponents.INIT, threadId, "Max Memory\t\t\t:: " + maxMemory, "MB");
    }

    private void listenToInternalCommunication() {
        final long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, LogComponents.INTERNAL, threadId, " Going to listen on port " + internalPort, " for internal communication");
        try {
            serverSocket = new ServerSocket(internalPort);
            while(!isStopped()) {
                logger.log(LogLevel.DEBUG, mainComponent, LogComponents.INTERNAL, threadId, " Stopped: ", ""+isStopped());
                handleInternalCommunication(threadId);
            }
            serverSocket.close();
        } catch (IOException e) {
            logger.log(LogLevel.ERROR, mainComponent, LogComponents.INTERNAL, threadId, " Something went wrong listening to internal communication:", e.getCause().toString());
        } finally {
            closeServer();
        }
    }

    private void handleInternalCommunication(final long threadId) {
        try  {
            Socket clientSocket = serverSocket.accept();
            logger.log(LogLevel.DEBUG,mainComponent, LogComponents.INTERNAL, threadId, " Socket Established on Port: ", ""+ clientSocket.getRemoteSocketAddress());
            try (BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream())) {
                try {
                    FeiwuMessage feiwuMessage = Feiwu.fromInputStream(in);
                    MessageOrigin messageOrigin = feiwuMessage.getMessageOrigin();
                    if (messageOrigin.equals(this.messageOrigin) || recentProcessedMessages.containsKey(feiwuMessage.getDigest())){
                        // its ourselves, don't process it, or we have already processed it
                        return;
                    }

                    if (feiwuMessage.getType().equals(FeiwuMessageType.MEMBERSHIP)) {
                        Runnable runnable = () -> updateMemberShipListByMessage(feiwuMessage);
                        messageHandlerExecutor.submit(runnable);
                        logger.log(LogLevel.INFO, mainComponent, LogComponents.INTERNAL, threadId, " Received: ", feiwuMessage.toString());
                    }

                    recentProcessedMessages.put(feiwuMessage.getDigest(), System.currentTimeMillis());
                    if (recentProcessedMessages.size() <= maxRecentDigest) {
                        removeOldestRecentDigest();
                    }

                } catch (IOException e1) {
                    logger.log(LogLevel.WARN, mainComponent, LogComponents.INTERNAL, threadId, " Error while reading message: ", e1.getMessage());
                }
            }
            pauseAndWait(5L, threadId);
        } catch(SocketException socketException){
            logger.log(LogLevel.WARN, mainComponent, LogComponents.INTERNAL, threadId, " Server socket ", ""+internalPort, " is closed, exiting.");
            logger.log(LogLevel.WARN, mainComponent, LogComponents.INTERNAL, threadId, " Reason for stopping:", socketException.getCause().toString());
        } catch (IOException e) {
            logger.log(LogLevel.ERROR, mainComponent, LogComponents.INTERNAL, threadId, " Connection broken:", e.getCause().toString());
        }
    }

    private void pauseAndWait(final long waitTimeInMillis, final long threadId) {
        try {
            sleep(waitTimeInMillis);
        } catch (InterruptedException e) {
            logger.log(LogLevel.WARN, mainComponent, LogComponents.INTERNAL, threadId, " Interrupted, stopping");
            // restoring interrupt
            Thread.currentThread().interrupt();
        }
    }

    private void listenToHealthCheck() {
        long threadId = Thread.currentThread().getId();
        logger.log(LogLevel.INFO, mainComponent, LogComponents.HEALTH_CHECK, threadId, " Going to listen on port " + healthCheckPort, " for health checks");
        try (ServerSocket healthCheckSocket = new ServerSocket(healthCheckPort)){
            Socket clientSocket;
            while(!isStopped()) {
                clientSocket = healthCheckSocket.accept();
                try (OutputStream out = clientSocket.getOutputStream()) {
                    if (closed) {
                        logger.log(LogLevel.ERROR, mainComponent, LogComponents.HEALTH_CHECK, threadId, " Closed, 500");
                        out.write(INTERNAL_SERVER_ERROR_RESPONSE);
                    } else if (isStopped()) {
                        logger.log(LogLevel.WARN, mainComponent, LogComponents.HEALTH_CHECK, threadId, " Stopped, 503");
                        out.write(SERVICE_TEMPORARILY_UNAVAILABLE_RESPONSE);
                    } else {
                        node.updateUptime();
                        logger.log(LogLevel.INFO, mainComponent, LogComponents.HEALTH_CHECK, threadId, " 200 OK ", this.node.toString());
                        out.write(OK_RESPONSE);
                    }
                    out.flush();
                } finally {
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            logger.log(LogLevel.WARN, mainComponent, LogComponents.HEALTH_CHECK, threadId, " Something happened with the health check", e.getMessage());
        }
    }

    private void listenToInternalGroup() {
        long threadId = Thread.currentThread().getId();
        byte[] buf = new byte[256];
        try(MulticastSocket socket = new MulticastSocket(groupPort)) {
            InetAddress group = InetAddress.getByName(membershipGroup);
            logger.log(LogLevel.INFO, mainComponent, LogComponents.GROUP, threadId, " Going to listen on port " + groupPort, " for group communication ("+ membershipGroup+")");
            socket.joinGroup(group);
            while(!isStopped()) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                FeiwuMessage feiwuMessage = Feiwu.fromBytes(packet.getData());
                MessageOrigin messageOrigin = feiwuMessage.getMessageOrigin();
                if (!messageOrigin.getHost().equals(this.messageOrigin.getHost())) { // we received our own message, no need to deal with this

                    logger.log(LogLevel.INFO, mainComponent, LogComponents.GROUP, threadId, " Received: ", feiwuMessage.toString());
                    recentProcessedMessages.put(feiwuMessage.getDigest(), System.currentTimeMillis());

                    switch (feiwuMessage.getType()) {
                        case MEMBERSHIP:
                            Runnable runnable = () -> updateMemberShipListByMessage(feiwuMessage);
                            messageHandlerExecutor.submit(runnable);
                            break;
                        default:
                            logger.log(LogLevel.WARN, mainComponent, LogComponents.GROUP, threadId, " Received unsupported type: ", feiwuMessage.toString());
                    }
                }
            }
            socket.leaveGroup(group);
        } catch (IOException e) {
            logger.log(LogLevel.ERROR, mainComponent, LogComponents.GROUP, threadId, e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateMemberShipListByMessage(final FeiwuMessage feiwuMessage) {
        final long threadId = Thread.currentThread().getId();
        MessageOrigin messageOrigin = feiwuMessage.getMessageOrigin();
        byte[] messageDigest = feiwuMessage.getDigest();
        if (feiwuMessage.getMessage().equals(ProtocolConstants.MEMBERSHIP_LEAVE_MESSAGE) ) {
            if (recentProcessedMessages.containsKey(feiwuMessage.getDigest())) {
                return;
            }
            logger.log(LogLevel.WARN, mainComponent, LogComponents.MAIN, threadId, "Received membership leave notice from ", messageOrigin.toString());
            membershipList.remove(messageOrigin.getHost());
            try {
                propagateMembershipLeaveNotice(messageOrigin, messageDigest);
            } catch (MessageTargetDoesNotExistException | MessageDeliveryException | MessageTargetNotAvailableException e) {
                logger.log(LogLevel.ERROR, mainComponent, LogComponents.MAIN, threadId, e.getMessage());
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

    private void propagateMembershipLeaveNotice(final MessageOrigin messageOriginLeaver, final byte[] messageDigest) throws MessageTargetDoesNotExistException, MessageDeliveryException, MessageTargetNotAvailableException {
        if (recentSendMessages.containsKey(messageDigest)) {
            long threadId = Thread.currentThread().getId();
            logger.log(LogLevel.WARN, mainComponent, LogComponents.MAIN, threadId, " not propagating message as digest is in recent list");
        } else {
            recentSendMessages.put(messageDigest, System.currentTimeMillis());
            for (String hostname : membershipList.keySet()) {
                sendMembershipLeaveMessage(hostname, messageOriginLeaver);
            }
        }
    }

    private void sendMembershipLeaveMessage(final String memberHost, final MessageOrigin messageOriginLeaver) throws MessageTargetDoesNotExistException, MessageDeliveryException, MessageTargetNotAvailableException {
        Feiwu feiwuMessage = new Feiwu(FeiwuMessageType.MEMBERSHIP, ProtocolConstants.MEMBERSHIP_LEAVE_MESSAGE, messageOriginLeaver);
        sendMessageToMember(memberHost, feiwuMessage);
    }

    private void sendMessageToMember(final String memberHost, final Feiwu feiwuMessage) throws MessageTargetDoesNotExistException, MessageDeliveryException, MessageTargetNotAvailableException {
        try (Socket socket = new Socket(memberHost, internalPort)) {
            try (OutputStream mOutputStream = socket.getOutputStream()) {
                try (BufferedOutputStream out = new BufferedOutputStream(mOutputStream)) {
                    feiwuMessage.writeMessage(out);
                    out.flush();
                    recentSendMessages.put(feiwuMessage.getDigest(), System.currentTimeMillis());
                }
            }
        } catch (UnknownHostException e) {
            handleMemberNotReachable(memberHost);
            throw new MessageTargetDoesNotExistException("Could not send message to unknown host " + memberHost);
        } catch (IOException e) {
            if (e instanceof ConnectException) {
                throw new MessageTargetNotAvailableException("Could not send message to " + memberHost+ " on port " + internalPort);
            }
            throw new MessageDeliveryException("Could deliver message to " + internalPort + " because " + e.getMessage());
        }
    }

    private void handleMemberNotReachable(String memberKey) {
        final long threadId = Thread.currentThread().getId();
        Membership membership = membershipList.get(memberKey);
        if (membership == null) {
            logger.log(LogLevel.WARN, mainComponent, LogComponents.MAIN, threadId, "Membership ", memberKey, " does not exist in membership (already deleted?)");
            return;
        }
        logger.log(LogLevel.WARN, mainComponent, LogComponents.MAIN, threadId, "Membership ", memberKey, " was not reachable, increase failed check count");
        membership.incrementFailedCheckCount();
    }

    private void updateMember(final MessageOrigin messageOrigin) {
        final long threadId = Thread.currentThread().getId();
        Membership membership = membershipList.get(messageOrigin.getHost());
        if (membership == null) {
            logger.log(LogLevel.WARN, mainComponent, LogComponents.MAIN, threadId, "Attempt to update member that does not exist");
        } else {
            long currentTime = System.currentTimeMillis();
            membership.updateLastSeen(currentTime);
        }
    }

    private void addMember(final MessageOrigin messageOrigin) {
        long threadId = Thread.currentThread().getId();
        Membership membership = membershipList.get(messageOrigin.getHost());
        if (membership != null) {
            logger.log(LogLevel.WARN, mainComponent, LogComponents.MAIN, threadId, "Attempt to add new member that already exists");
        } else {
            if (membershipList.size() >= MAX_NUMBER_MEMBERS) {
                removeLastSeenMember();
            }
            long currentTime = System.currentTimeMillis();
            //TODO: determine role
            Membership newMembership = new Membership(messageOrigin.getName(), "UNKNOWN" ,currentTime);
            membershipList.put(messageOrigin.getHost(), newMembership);
        }
    }

    private void removeLastSeenMember() {
        long lastSeen = System.currentTimeMillis();
        String keyToRemove = null;
        for(Map.Entry<String, Membership> entry : membershipList.entrySet()) {
            if (entry.getValue().getLastSeen() < lastSeen) {
                keyToRemove = entry.getKey();
            }
        }
        if (keyToRemove != null) {
            membershipList.remove(keyToRemove);
        }
    }

    private void checkMembers() {
        final long threadId = Thread.currentThread().getId();
        final long waitTimeInMillis = 10000L;
        while(!isStopped()) {
            logger.log(LogLevel.INFO, mainComponent, LogComponents.MAIN, threadId, "Checking member status of " + membershipList.keySet().size() + " members, again in " + (waitTimeInMillis / 1000), " seconds");
            for (String host : membershipList.keySet()) {
                Feiwu feiwuMessage = new Feiwu(FeiwuMessageType.HELLO, "Check", messageOrigin);
                try {
                    sendMessageToMember(host, feiwuMessage);
                } catch (MessageTargetDoesNotExistException | MessageDeliveryException | MessageTargetNotAvailableException e) {
                    logger.log(LogLevel.ERROR, mainComponent, LogComponents.MAIN, threadId, "Failed checking up on a member: ", e.getMessage());
                }
            }
            pauseAndWait(waitTimeInMillis, threadId);
        }
    }

    private void removeFailedMembers() {
        final long threadId = Thread.currentThread().getId();
        final long waitTimeInMillis = 10000L;
        while(!isStopped()) {
            var keysToRemove = new ArrayList<String>();
            logger.log(LogLevel.INFO, mainComponent, LogComponents.MAIN, threadId, "Removing failed members, again in " + (waitTimeInMillis / 1000), " seconds");
            for (Map.Entry<String, Membership> entry : membershipList.entrySet()) {
                var member = entry.getValue();
                long lastSeenDelta = System.currentTimeMillis() - member.getLastSeen();
                if (member.failedCheckCount() > 2 || lastSeenDelta > maxAgeMissingMember) {
                    keysToRemove.add(entry.getKey());
                }
            }
            for (String keyToRemove : keysToRemove) {
                membershipList.remove(keyToRemove);
                logger.log(LogLevel.WARN, mainComponent, LogComponents.MAIN, threadId, "Removing member ", keyToRemove);
            }
            pauseAndWait(waitTimeInMillis, threadId);
        }
    }

    private void sendMemberShipUpdates(){
        final long threadId = Thread.currentThread().getId();
        try (DatagramSocket socket = new DatagramSocket()) {
            while (!stopped) {
                InetAddress group = InetAddress.getByName(membershipGroup);
                String message = "Hello from " + name;
                Feiwu feiwu = new Feiwu(FeiwuMessageType.MEMBERSHIP, message, this.messageOrigin);
                byte[] buf = feiwu.writeToBuffer();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, groupPort);
                socket.send(packet);
                pauseAndWait(membershipUpdateRateInMillis, threadId);
            }
        } catch (IOException e) {
            String cause = e.getCause() == null ? "" : e.getCause().getMessage();
            logger.log(LogLevel.ERROR, mainComponent, LogComponents.MAIN, threadId, " Problem occurred sending membership updates", e.getMessage(), cause);
        }
    }

    // TODO: http://www.baeldung.com/java-8-comparator-comparing
    //      http://www.baeldung.com/java-collection-min-max
    // http://www.java67.com/2017/06/how-to-remove-entry-keyvalue-from-HashMap-in-java.html
    private void removeOldestRecentDigest() {
        removeOldestRecentEntries(recentProcessedMessages);
        removeOldestRecentEntries(recentSendMessages);
    }

    private void removeOldestRecentEntries(final ConcurrentHashMap<byte[], Long> map) {
        Long oldest = map.values().stream().mapToLong(v -> v).min().orElse(0L);
        if (oldest != 0L) {
            map.values().removeAll(Collections.singleton(oldest));
        }
    }


    private void cleanUpRecentDigests() {
        while (!stopped) {
            cleanUpRecentEntries(recentSendMessages);
            cleanUpRecentEntries(recentProcessedMessages);

            try {
                sleep(100000);
            } catch (InterruptedException e) {
                final long threadId = Thread.currentThread().getId();
                logger.log(LogLevel.WARN, mainComponent, LogComponents.MAIN, threadId, "Interrupted, going to close");
                Thread.currentThread().interrupt();
            }
        }
    }

    private void cleanUpRecentEntries(ConcurrentHashMap<byte[],Long> entries) {
        long currentTime = System.currentTimeMillis();
        var keysToRemove = new ArrayList<byte[]>();
        entries.forEach((k,v) -> {
            if (currentTime - v > maxAgeRecentDigest) {
                keysToRemove.add(k);
            }
        });
        for (byte[] key : keysToRemove) {
            entries.remove(key);
        }
    }

    @Override
    public void stopServer() {
        synchronized (this) {
            this.stopped = true;
            long threadId = Thread.currentThread().getId();
            logger.log(LogLevel.INFO, mainComponent, LogComponents.MAIN, threadId, " Stopping");
            try {
                sendLeaveMessage();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendLeaveMessage() {
        for(String hostname : membershipList.keySet()) {
            try {
                sendMembershipLeaveMessage(hostname, messageOrigin);
            } catch (MessageTargetDoesNotExistException | MessageDeliveryException | MessageTargetNotAvailableException e) {
                long threadId = Thread.currentThread().getId();
                logger.log(LogLevel.WARN, mainComponent, LogComponents.MAIN, threadId, "Could not deliver leave notice to ", hostname, ",because: ", e.getMessage());
            }
        }
    }

    @Override
    public void updateMembershipList(String host, int port, String serverName, boolean active) {
        throw new UnsupportedOperationException("Use updateMembershipListByMessage instead");
    }

    @Override
    public void closeServer() {
        final long threadId = Thread.currentThread().getId();
        synchronized (this) {
            this.closed = true;
            logger.log(LogLevel.INFO, mainComponent, LogComponents.MAIN, threadId, " Closing");
        }
        socketExecutors.shutdown();
        try {
            sleep(500);
        } catch (InterruptedException e) {
            logger.log(LogLevel.WARN, mainComponent, LogComponents.MAIN, threadId, "Was interrupted while closing");
            // restoring interrupt
            Thread.currentThread().interrupt();
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
        logger.log(LogLevel.INFO, mainComponent, LogComponents.MAIN, threadId, " Listing memberships");
        membershipList.keySet().forEach(port -> {
            Membership membership = membershipList.get(port);
            logger.log(LogLevel.INFO, mainComponent, LogComponents.MAIN, threadId, "  > ", membership.toString());
        });
        logger.log(LogLevel.INFO, mainComponent, LogComponents.MAIN, threadId, " Currently holding ", ""+recentProcessedMessages.size(), " recent messages");
    }
}
