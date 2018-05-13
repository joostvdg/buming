package com.github.joostvdg.dui.api;

import com.github.joostvdg.dui.api.message.FeiwuMessage;
import com.github.joostvdg.dui.api.message.FeiwuMessageType;
import com.github.joostvdg.dui.api.message.MessageOrigin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple binary protocol to play with.
 *
 * Message[1][1][2][2][3][3][3][3][4][4][4][4][5]...[5][6]...[6]
 * Where:
 * 1 = FEIWU header
 * 2 = MESSAGE_TYPE header
 * 3 = MESSAGE_ORIGIN_SIZE header
 * 4 = MESSAGE_SIZE header
 * 5 = MESSAGE_ORIGIN data (of length MESSAGE_ORIGIN_SIZE)
 * 6 = MESSAGE data (of length MESSAGE_SIZE)
 * 7 = MESSAGE_DIGEST (of length 32)
 */
public final class Feiwu {
    // Feiwu header
    public static final byte[] FEIWU_HEADER = {0x08, 0x08};

    // size of the message type header
    public static final int MESSAGE_TYPE_HEADER_SIZE = 2;

    public static final int MESSAGE_ORIGIN_HEADER_SIZE = 4;

    public static final int MESSAGE_SIZE_HEADER_SIZE = 4;

    public static final int DIGEST_SIZE = 32;

    public static final int FIXED_HEADER_SIZE = FEIWU_HEADER.length + MESSAGE_TYPE_HEADER_SIZE + MESSAGE_SIZE_HEADER_SIZE + MESSAGE_ORIGIN_HEADER_SIZE;

    private final FeiwuMessageType messageType;

    private final byte[] message;
    private final MessageOrigin messageOrigin;
    private final byte[] digest;

    public Feiwu(FeiwuMessageType messageType, byte[] message, MessageOrigin messageOrigin) {
        this.messageType = messageType;
        this.message = message;
        this.messageOrigin =messageOrigin;
        this.digest = calculateDigest(message);
    }

    public static byte[] calculateDigest(final byte[] message){
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (messageDigest != null) {
            return messageDigest.digest(message);
        } else {
            throw new IllegalStateException("Could make message digest");
        }
    }

    public Feiwu(FeiwuMessageType messageType, String message, MessageOrigin messageOrigin) {
        this(messageType, message.getBytes(), messageOrigin);
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
        return ByteBuffer.allocate(MESSAGE_SIZE_HEADER_SIZE).putInt(message.length).array();
    }

    public byte[] getDigest() {
        return digest;
    }

    public void writeMessage(BufferedOutputStream out) throws IOException {
        out.write(FEIWU_HEADER);
        out.write(getMessageTypeHeader());
        out.write(messageOrigin.size());
        out.write(getMessageSize());
        out.write(messageOrigin.flat());
        out.write(message);
        out.write(digest);
    }

    public byte[] writeToBuffer() {
        byte[] typeHeader = getMessageTypeHeader();
        byte[] originSize = messageOrigin.size();
        byte[] messageSize = getMessageSize();
        byte[] origin = messageOrigin.flat();

        int bufferSize = FEIWU_HEADER.length + typeHeader.length + messageSize.length + originSize.length + origin.length +  message.length + DIGEST_SIZE;
        byte[] responseBuffer = new byte[bufferSize];

        int bytesCopied = 0;
        bytesCopied = copyMessagePartsIntoArray(FEIWU_HEADER, responseBuffer, bytesCopied);
        bytesCopied = copyMessagePartsIntoArray(typeHeader, responseBuffer, bytesCopied);
        bytesCopied = copyMessagePartsIntoArray(originSize, responseBuffer, bytesCopied);
        bytesCopied = copyMessagePartsIntoArray(messageSize, responseBuffer, bytesCopied);
        bytesCopied = copyMessagePartsIntoArray(origin, responseBuffer, bytesCopied);
        bytesCopied = copyMessagePartsIntoArray(message, responseBuffer, bytesCopied);
        copyMessagePartsIntoArray(digest, responseBuffer, bytesCopied);
        return responseBuffer;
    }

    private int copyMessagePartsIntoArray(byte[] source, byte[] destination, int destinationStartIndex) {
        int endIndex = destinationStartIndex + source.length;
        System.arraycopy(source, 0, destination, destinationStartIndex, source.length );
        return endIndex;
    }

    public static FeiwuMessage fromBytes(byte[] bytes) throws IOException {
        try (
            BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(bytes))
        ) {
            return fromInputStream(in);
        }
    }

    public static FeiwuMessage fromInputStream(BufferedInputStream in) throws IOException {
        byte[] headerBytes = new byte[Feiwu.FIXED_HEADER_SIZE];
        int bytesRead = 0;
        bytesRead = in.read(headerBytes, 0, Feiwu.FIXED_HEADER_SIZE);
        // FEIWU CHECK HEADER
        if (headerBytes[0] == 8 && headerBytes[1] == 8) {
            // fine
            // should we get the logger?
        } else {
            byte header0 = headerBytes[0];
            byte header1 = headerBytes[1];
            String errorMessage = "It is not a FeiWu message - invalid header [" + header0 +"," + header1 + "]";
            System.err.println(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        // MESSAGETYPE HEADER
        byte[] messageTypeHeaderSegment = {headerBytes[2], headerBytes[3]};
        FeiwuMessageType messageType = Feiwu.getMessageTypeFromHeader(messageTypeHeaderSegment);

        // MESSAGE ORIGIN SIZE HEADER
        byte[] messageOriginSizeHeaderSegment = {headerBytes[4], headerBytes[5], headerBytes[6], headerBytes[7]};
        int messageOriginSize = java.nio.ByteBuffer.wrap(messageOriginSizeHeaderSegment).order(ByteOrder.BIG_ENDIAN).getInt();

        // MESSAGE SIZE HEADER
        byte[] messageSizeHeaderSegment = {headerBytes[8], headerBytes[9], headerBytes[10], headerBytes[11]};
        int messageSize = java.nio.ByteBuffer.wrap(messageSizeHeaderSegment).order(ByteOrder.BIG_ENDIAN).getInt();

        // CHECK HEADER CORRECTNESS
        if (bytesRead != Feiwu.FIXED_HEADER_SIZE) {
            String errorMessage = "It is not a FeiWu message";
            System.err.println(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        // READ MESSAGE ORIGIN
        byte[] messageOriginBytes = new byte[messageOriginSize];
        in.read(messageOriginBytes, 0, messageOriginSize);
        String flatRepresentation = new String(messageOriginBytes);
        MessageOrigin messageOrigin = MessageOrigin.fromFlat(flatRepresentation);

        // READ MESSAGE
        byte[] messageBytes = new byte[messageSize];
        in.read(messageBytes, 0, messageSize);
        String message = new String(messageBytes);

        // READ DIGEST
        byte[] digest = new byte[DIGEST_SIZE];
        in.read(digest, 0, DIGEST_SIZE);

        return new FeiwuMessage(messageType, message, messageOrigin, digest);
    }

}
