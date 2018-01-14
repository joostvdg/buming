package com.github.joostvdg.dui.server.handler;


import com.github.joostvdg.dui.api.ProtocolConstants;
import com.github.joostvdg.dui.api.message.Feiwu;
import com.github.joostvdg.dui.api.message.FeiwuMessage;
import com.github.joostvdg.dui.api.message.FeiwuMessageType;
import com.github.joostvdg.dui.logging.LogLevel;
import com.github.joostvdg.dui.logging.Logger;
import com.github.joostvdg.dui.server.api.DuiServer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteOrder;

public class ClientHandler implements Runnable {

    private final Socket client;
    private final Logger logger;
    private final String serverComponent;
    private final DuiServer duiServer;

    public ClientHandler(Socket client, Logger logger, DuiServer duiServer) {
        this.client = client;
        this.logger = logger;
        this.duiServer = duiServer;
        this.serverComponent = "Server-" + duiServer.name();
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        long threadId = Thread.currentThread().getId();
        try (PrintWriter out =
                     new PrintWriter(client.getOutputStream(), true)) {
            try (
                    // BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))
                    BufferedInputStream in = new BufferedInputStream(client.getInputStream())
                ) {
                logger.log(LogLevel.DEBUG,serverComponent, "ClientHandler", threadId, " Socket Established on Port: ", ""+ client.getRemoteSocketAddress());
                long lastCheck = System.currentTimeMillis();
                String inputLine, outputLine;

                // First, read the headers
                byte[] headerBytes = new byte[Feiwu.FIXED_HEADER_SIZE];
                int bytesRead = 0;
                try {
                    bytesRead = in.read(headerBytes, 0, Feiwu.FIXED_HEADER_SIZE);

                    // FEIWU CHECK HEADER
                    if (headerBytes[0] == 8 && headerBytes[1] == 8) {
                        logger.log(LogLevel.DEBUG, serverComponent,"ClientHandler", threadId, " Processing FEIWU message");
                    } else {
                        logger.log(LogLevel.WARN, serverComponent,"ClientHandler", threadId, " Invalid message, cannot process");
                        return;
                    }
                    //printByteArrayBlocks(headerBytes, 0,2);

                    // MESSAGETYPE HEADER
                    byte[] messageTypeHeaderSegment = {headerBytes[2], headerBytes[3]};
                    FeiwuMessageType messageType = Feiwu.getMessageTypeFromHeader(messageTypeHeaderSegment);
                    //printByteArrayBlocks(headerBytes, 2,2);

                    // MESSAGE SIZE HEADER
                    byte[] messageSizeHeaderSegment = {headerBytes[4], headerBytes[5], headerBytes[6], headerBytes[7]};
                    int messageSize = java.nio.ByteBuffer.wrap(messageSizeHeaderSegment).order(ByteOrder.BIG_ENDIAN).getInt();

                    //printByteArrayBlocks(headerBytes, 4,4);

                    // READ MESSAGE
                    if (!(bytesRead == Feiwu.FIXED_HEADER_SIZE)) {
                        System.out.println("[ClientHandler][" + threadId + "] ");
                        logger.log(LogLevel.WARN, serverComponent,"ClientHandler", threadId, " Read incorrect amount of bytes, message corrupt");
                        return;
                    }
                    byte[] messageBytes = new byte[messageSize];
                    in.read(messageBytes, 0, messageSize);
                    String message = new String(messageBytes);
                    FeiwuMessage feiwuMessage = new FeiwuMessage(messageSize, messageType, message);
                    if (messageType.equals(FeiwuMessageType.MEMBERSHIP)) {
                        handleMembership(message, duiServer);
                    }
                    logger.log(LogLevel.INFO, serverComponent,"ClientHandler", threadId, feiwuMessage.toString());

                } catch (IOException e1) {
                    logger.log(LogLevel.WARN, serverComponent,"ClientHandler", threadId, " Error while reading message: ", e1.getMessage());
                    e1.printStackTrace();
                }
                //while ((inputLine = in.readLine()) != null) {
                //    System.out.println("[ClientHandler][" + threadId + "] received input: " + inputLine);
                //}

            }
        } catch (IOException e) {
            logger.log(LogLevel.WARN, serverComponent,"ClientHandler", threadId, " Error while reading message: ", e.getMessage());
            e.printStackTrace();
        } finally {
            logger.log(LogLevel.DEBUG, serverComponent,"ClientHandler", threadId, "Socket Closed on Port: " + client.getRemoteSocketAddress());
            if(!client.isClosed()) {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleMembership(String message, DuiServer duiServer) { // TODO: move this to specific message type object
        if (!message.contains(",")) {
            throw new IllegalArgumentException("This is not a proper Membership message");
        }
        String[] messageParts = message.split(",");
        int port = Integer.parseInt(messageParts[0]);
        String serverName = messageParts[1];

//        System.out.println("ClientHandler.handleMembership:");
//        for(int i =0; i < messageParts.length; i++) {
//            System.out.println("MessagePart["+i+"]: " + messageParts[i]);
//            System.out.println("Is it a membership leave notice: " + messageParts[i].equals(ProtocolConstants.MEMBERSHIP_LEAVE_MESSAGE));
//        }

        if (messageParts.length >= 3 && messageParts[2].equals(ProtocolConstants.MEMBERSHIP_LEAVE_MESSAGE)) {
            duiServer.updateMembershipList(port, serverName, false);
        } else {
            duiServer.updateMembershipList(port, serverName, true);
        }
    }

    private void printByteArrayBlocks(byte[] responseBytes, int offset, int bytesToPrint) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i =offset; i < (offset + bytesToPrint); i++) {
            stringBuilder.append("[" );
            stringBuilder.append(responseBytes[i]);
            stringBuilder.append("]");
        }
        System.out.println(stringBuilder.toString());
    }
}
