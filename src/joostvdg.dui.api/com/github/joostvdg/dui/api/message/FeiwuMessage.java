package com.github.joostvdg.dui.api.message;

public class FeiwuMessage {
    private final int messageSize;
    private final FeiwuMessageType type;
    private final String message;
    private final MessageOrigin messageOrigin;

    public FeiwuMessage(final FeiwuMessageType type, final String message, final MessageOrigin messageOrigin) {
        this.messageSize = message.length();
        this.type = type;
        this.message = message;
        this.messageOrigin = messageOrigin;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public FeiwuMessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public MessageOrigin getMessageOrigin() {
        return this.messageOrigin;
    }

    @Override
    public String toString() {
        return "FeiwuMessage{" +
            "messageSize=" + messageSize +
            ", type=" + type +
            ", message='" + message + '\'' +
            ", messageOrigin=" + messageOrigin +
            '}';
    }
}
