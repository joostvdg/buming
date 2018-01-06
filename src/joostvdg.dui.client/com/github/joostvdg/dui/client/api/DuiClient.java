package com.github.joostvdg.dui.client.api;

import com.github.joostvdg.dui.api.FeiwuMessageType;

public interface DuiClient {

    long getMessageCount();

    long getFailedMessageCount();

    void sendMessage(FeiwuMessageType type, byte[] message);

    void sendServerMessage(FeiwuMessageType type, byte[] message, int ownPort);
}
