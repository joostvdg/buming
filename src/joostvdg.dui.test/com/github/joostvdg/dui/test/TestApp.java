package com.github.joostvdg.dui.test;

import com.github.joostvdg.dui.api.ProtocolConstants;
import com.github.joostvdg.dui.server.api.DuiServer;
import com.github.joostvdg.dui.client.api.DemoClient;
import com.github.joostvdg.dui.server.api.DuiServerFactory;

import java.util.Random;

public class TestApp {
    public static void main(String[] args) {

        int pseudoRandom = new Random().nextInt(ProtocolConstants.POTENTIAL_SERVER_NAMES.length -1);
        DuiServer serverSimple = DuiServerFactory.newServerSimple(ProtocolConstants.EXTERNAL_COMMUNICATION_PORT_A, ProtocolConstants.POTENTIAL_SERVER_NAMES[pseudoRandom], null);
        DemoClient clientSimpleA = new DemoClient(3, "A");
        DemoClient clientSimpleB = new DemoClient(3, "B");
        DemoClient clientSimpleC = new DemoClient(3, "C");
        DemoClient clientSimpleD = new DemoClient(3, "D");
        DemoClient clientSimpleE = new DemoClient(3, "E");
        DemoClient clientSimpleF = new DemoClient(3, "D");

        try {
            serverSimple.startServer();
            Thread.sleep(5000);
            clientSimpleA.start();
            clientSimpleB.start();
            clientSimpleC.start();
            clientSimpleD.start();
            clientSimpleE.start();
            clientSimpleF.start();
            clientSimpleA.interrupt();
            //Thread.sleep(12000);
            //clientSimpleB.interrupt();
            Thread.sleep(30000);
            serverSimple.stopServer();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("[App] Closing server");
            serverSimple.stopServer();
        }
    }
}
