package com.github.joostvdg.dui.api.message;

import java.util.Arrays;

public class FeiwuMessage {
    private final int messageSize;
    private final FeiwuMessageType type;
    private final String message;
    private final MessageOrigin messageOrigin;
    private final byte[] digest;

    public FeiwuMessage(final FeiwuMessageType type, final String message, final MessageOrigin messageOrigin, final byte[] digest) {
        this.messageSize = message.length();
        this.type = type;
        this.message = message;
        this.messageOrigin = messageOrigin;
        this.digest = digest;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public byte[] getDigest() {
        return digest;
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

    public boolean validateDigest(){
        return Arrays.equals(Feiwu.calculateDigest(message.getBytes()), getDigest());
    }

    @Override
    public String toString() {
        boolean digestStatus = validateDigest();
        return "FeiwuMessage{" +
            "size=" + messageSize +
            ", type=" + type +
            ", text='" + message + '\'' +
            ", " + messageOrigin +
            ", digest=" + digestStatus +
            '}';
    }
}
