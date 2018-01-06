package com.github.joostvdg.dui.client.api;

import com.github.joostvdg.dui.api.exception.MessageDeliveryException;
import com.github.joostvdg.dui.api.exception.MessageTargetDoesNotExistException;
import com.github.joostvdg.dui.api.exception.MessageTargetNotAvailableException;
import com.github.joostvdg.dui.api.message.FeiwuMessageType;

public interface DuiClient {

    long getMessageCount();

    long getFailedMessageCount();

    void sendMessage(FeiwuMessageType type, byte[] message) throws MessageTargetNotAvailableException, MessageDeliveryException, MessageTargetDoesNotExistException;

    void sendServerMessage(FeiwuMessageType type, byte[] message, int ownPort) throws MessageTargetNotAvailableException, MessageDeliveryException, MessageTargetDoesNotExistException;
}
