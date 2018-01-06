package com.github.joostvdg.dui.server.handler;


import com.github.joostvdg.dui.api.Feiwu;
import com.github.joostvdg.dui.api.FeiwuMessage;
import com.github.joostvdg.dui.api.FeiwuMessageType;
import com.github.joostvdg.dui.logging.LogLevel;
import com.github.joostvdg.dui.logging.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteOrder;

public class ClientHandler implements Runnable {

    private final Socket client;
    private final Logger logger;
    private final String serverComponent;

    public ClientHandler(Socket client, Logger logger, String serverComponent) {
        this.client = client;
        this.logger = logger;
        this.serverComponent =serverComponent;
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
