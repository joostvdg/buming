package com.github.joostvdg.dui.api.message;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Simple binary protocol to play with.
 *
 * Message[1][1][2][2][3][3][3][3][4]...[4]
 * Where:
 * 1 = FEIWU_HEADER
 * 2 = MESSAGETYPE_HEADER
 * 3 = MESSAGESIZE_HEADER
 * 4 = Message
 */
public final class Feiwu {
    // Feiwu header
    public static final byte[] FEIWU_HEADER = {0x08, 0x08};

    // size of the message type header
    public static final int MESSAGETYPE_HEADER_SIZE = 2;

    public static final int MESSAGESIZE_HEADER_SIZE = 4;

    public static final int FIXED_HEADER_SIZE = FEIWU_HEADER.length + MESSAGETYPE_HEADER_SIZE + MESSAGESIZE_HEADER_SIZE;

    private final FeiwuMessageType messageType;

    private final byte[] message;

    public Feiwu(FeiwuMessageType messageType, byte[] message) {
        this.messageType = messageType;
        this.message = message;
    }

    public byte[] getMessageTypeHeader(){
        return new byte[] {0x01, messageType.getIdentifier()};
    }

    public static FeiwuMessageType getMessageTypeFromHeader(byte[] typeHeaderSegment){
        byte identifier = typeHeaderSegment[1];
        for(FeiwuMessageType type : FeiwuMessageType.values()) {
            if(type.getIdentifier() == identifier) {
                return type;
            }
        }
        return FeiwuMessageType.UNIDENTIFIED;
    }

    public byte[] getMessage() {
        return message;
    }

    public byte[] getMessageSize(){
        // http://stackoverflow.com/questions/2183240/java-integer-to-byte-array
        return ByteBuffer.allocate(MESSAGESIZE_HEADER_SIZE).putInt(message.length).array();
    }

    public void writeMessage(BufferedOutputStream out) throws IOException {
        out.write(FEIWU_HEADER);
        out.write(getMessageTypeHeader());
        out.write(getMessageSize());
        out.write(message);
    }
}
