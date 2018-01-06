package com.github.joostvdg.dui.server.api;

import com.github.joostvdg.dui.logging.Logger;
import com.github.joostvdg.dui.server.api.impl.ServerSimpleImpl;

public class DuiServerFactory {

    public static DuiServer newServerSimple(final int port, final String name, final Logger logger) {
        return new ServerSimpleImpl(name, port, logger);
    }

}
