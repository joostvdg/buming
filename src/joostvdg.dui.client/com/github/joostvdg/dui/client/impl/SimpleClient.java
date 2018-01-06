package com.github.joostvdg.dui.client.impl;

import com.github.joostvdg.dui.api.Feiwu;
import com.github.joostvdg.dui.api.FeiwuMessageType;
import com.github.joostvdg.dui.api.ProtocolConstants;
import com.github.joostvdg.dui.client.api.DuiClient;

import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleClient implements DuiClient {

    private final AtomicLong messageCount;
    private final AtomicLong failedMessageCount;
    private final String clientName;

    public SimpleClient() {
        messageCount = new AtomicLong(0);
        failedMessageCount = new AtomicLong(0);
        int pseudoRandom = new Random().nextInt(ProtocolConstants.POTENTIAL_SERVER_NAMES.length -1);
        clientName = ProtocolConstants.POTENTIAL_SERVER_NAMES[pseudoRandom];
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
    public void sendMessage(FeiwuMessageType type, byte[] message) {
        int pseudoRandom = new Random().nextInt(2);
        final int portNumber = ProtocolConstants.EXTERNAL_COMMUNICATION_PORT_A + pseudoRandom; // so we either contact A, B or C.
        sendMessage(type, message, portNumber);
    }

    @Override
    public void sendServerMessage(FeiwuMessageType type, byte[] message, int port) {
        sendMessage(type, message, port);
    }

    private void sendMessage(FeiwuMessageType type, byte[] message, int port) {
        final String hostName = "localhost";
        try (
            Socket kkSocket = new Socket(hostName, port);
            OutputStream mOutputStream = kkSocket.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(mOutputStream);
            // BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream())); // TODO: receive response as well
        ) {
            long threadId = Thread.currentThread().getId();
//            System.out.println("[Client]["+ clientName +"][" + threadId + "] connect to server on " + port);
            Feiwu feiwuMessage = new Feiwu(type, message);
            feiwuMessage.writeMessage(out);
            out.flush();
        } catch (IOException e) {
            failedMessageCount.incrementAndGet();
            e.printStackTrace();
        } finally {
            messageCount.incrementAndGet();
        }
    }
}
