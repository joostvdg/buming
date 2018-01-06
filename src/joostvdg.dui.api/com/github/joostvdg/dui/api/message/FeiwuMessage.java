package com.github.joostvdg.dui.api.message;

public class FeiwuMessage {
    private final int messageSize;
    private final FeiwuMessageType type;
    private final String message;


    public FeiwuMessage(int messageSize, FeiwuMessageType type, String message) {
        this.messageSize = messageSize;
        this.type = type;
        this.message = message;
    }

    @Override
    public String toString() {
        return "FeiwuMessage{" +
            "messageSize=" + messageSize +
            ", type=" + type +
            ", message='" + message + '\'' +
            '}';
    }
}
