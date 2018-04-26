package com.github.joostvdg.dui.client.api;

import com.github.joostvdg.dui.api.Feiwu;
import com.github.joostvdg.dui.api.message.FeiwuMessageType;
import com.github.joostvdg.dui.api.ProtocolConstants;
import com.github.joostvdg.dui.api.message.MessageOrigin;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class DemoClient extends Thread {

    private final long messageCount;
    private final String clientName;

    public DemoClient(long messageCount, String clientName) {
        assert messageCount > 1;
        this.messageCount = messageCount;
        this.clientName = clientName;
    }

    public void run(){
        final String hostName = "localhost";
        int pseudoRandom = new Random().nextInt(2);
        final int portNumber = ProtocolConstants.EXTERNAL_COMMUNICATION_PORT_A + pseudoRandom; // so we either contact A, B or C.
        try (
                Socket kkSocket = new Socket(hostName, portNumber);
                OutputStream mOutputStream = kkSocket.getOutputStream();
                BufferedOutputStream out = new BufferedOutputStream(mOutputStream);
                //PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
        ) {
            long threadId = Thread.currentThread().getId();
            System.out.println("[Client][" + threadId + "] connect to servera");

            // out.println("[Client][" + threadId + "]" + i);
            String rawMessage = "Hello from " + clientName;
            byte[] message = rawMessage.getBytes();
            Feiwu feiwuMessage = new Feiwu(FeiwuMessageType.HELLO, message, MessageOrigin.getCurrentOrigin(hostName));
            feiwuMessage.writeMessage(out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
