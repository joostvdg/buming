package com.github.joostvdg.dui.client.api;

import com.github.joostvdg.dui.client.impl.SimpleClient;

public class DuiClientFactory {

    public static DuiClient newSimpleClient() {
        return new SimpleClient();
    }
}
