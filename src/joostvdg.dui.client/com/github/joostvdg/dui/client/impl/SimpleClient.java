package com.github.joostvdg.dui.client.impl;

import com.github.joostvdg.dui.api.exception.MessageDeliveryException;
import com.github.joostvdg.dui.api.exception.MessageTargetDoesNotExistException;
import com.github.joostvdg.dui.api.exception.MessageTargetNotAvailableException;
import com.github.joostvdg.dui.api.Feiwu;
import com.github.joostvdg.dui.api.message.FeiwuMessageType;
import com.github.joostvdg.dui.api.ProtocolConstants;
import com.github.joostvdg.dui.api.message.MessageOrigin;
import com.github.joostvdg.dui.client.api.DuiClient;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleClient implements DuiClient {

    private final AtomicLong messageCount;
    private final AtomicLong failedMessageCount;

    public SimpleClient() {
        messageCount = new AtomicLong(0);
        failedMessageCount = new AtomicLong(0);
    }

    @Override
    public long getMessageCount() {
        return messageCount.get();
    }

    @Override
    public long getFailedMessageCount() {
        return failedMessageCount.get();
    }

    @Override
    public void sendMessage(FeiwuMessageType type, byte[] message) throws MessageTargetNotAvailableException, MessageDeliveryException, MessageTargetDoesNotExistException  {
        int pseudoRandom = new Random().nextInt(2);
        final int portNumber = ProtocolConstants.EXTERNAL_COMMUNICATION_PORT_A + pseudoRandom; // so we either contact A, B or C.
        sendMessage(type, message, portNumber);
    }

    @Override
    public void sendServerMessage(FeiwuMessageType type, byte[] message, int port)throws MessageTargetNotAvailableException, MessageDeliveryException, MessageTargetDoesNotExistException {
        sendMessage(type, message, port);
    }

    private void sendMessage(FeiwuMessageType type, byte[] message, int port) throws MessageTargetNotAvailableException, MessageDeliveryException, MessageTargetDoesNotExistException {
        final String hostName = "localhost";
        try (Socket kkSocket = new Socket(hostName, port)) {
            try (OutputStream mOutputStream = kkSocket.getOutputStream()) {
                try (BufferedOutputStream out = new BufferedOutputStream(mOutputStream)) {
                    Feiwu feiwuMessage = new Feiwu(type, message, MessageOrigin.getCurrentOrigin(hostName));
                    feiwuMessage.writeMessage(out);
                    out.flush();
                }
            }
        } catch (UnknownHostException e) {
            failedMessageCount.incrementAndGet();
            throw new MessageTargetDoesNotExistException("Could not send message to host" + hostName);
        } catch (IOException e) {
            failedMessageCount.incrementAndGet();
            if (e instanceof ConnectException) {
                throw new MessageTargetNotAvailableException("Could not send message to " + port);
            }
            throw new MessageDeliveryException("Could deliver message to " + port + " because " + e.getMessage());
        } finally {
            messageCount.incrementAndGet();
        }

    }
}
