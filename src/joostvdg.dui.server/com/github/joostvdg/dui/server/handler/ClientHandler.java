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
                try {

                    FeiwuMessage feiwuMessage = Feiwu.fromInputStream(in);
                    if (feiwuMessage.getType().equals(FeiwuMessageType.MEMBERSHIP)) {
                        handleMembership(feiwuMessage.getMessage(), duiServer);
                    }
                    logger.log(LogLevel.INFO, serverComponent,"ClientHandler", threadId, feiwuMessage.toString());

                } catch (IOException e1) {
                    logger.log(LogLevel.WARN, serverComponent,"ClientHandler", threadId, " Error while reading message: ", e1.getMessage());
                    e1.printStackTrace();
                }
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
            duiServer.updateMembershipList("localhost", port, serverName, false);
        } else {
            duiServer.updateMembershipList("localhost",port, serverName, true);
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
