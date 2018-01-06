package com.github.joostvdg.dui.server.api;

public interface DuiServer {

    void startServer();

    void stopServer();

    void closeServer();

    boolean isStopped();

    String name();

    void updateMembershipList(int port, String serverName);

    void logMembership();
}
