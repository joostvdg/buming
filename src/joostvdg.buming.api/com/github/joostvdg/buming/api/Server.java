package com.github.joostvdg.buming.api;

/**
 * A Server.
 */
public interface Server {

    /**
     * Start the server and listen on given port.
     * @param port the port to listen to
     * @throws Exception if anything goes wrong, it all goes up
     */
    void start(int port) throws Exception;
}
