package com.github.joostvdg.dui.api.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class MessageOrigin {
    private final String host;
    private final String ip;
    private final String name;
    private final String flatRepresentation;
    private static final String SEPARATOR = ",";
    // Valid linux host names: https://stackoverflow.com/questions/3523028/valid-characters-of-a-hostname

    // TODO: add regex test for the variables

    public MessageOrigin(String host, String ip, String name) {
        this.host = host;
        this.ip = ip;
        this.name = name;
        StringBuffer buffer = new StringBuffer();
        buffer.append(ip);
        buffer.append(SEPARATOR);
        buffer.append(host);
        buffer.append(SEPARATOR);
        buffer.append(name);
        flatRepresentation = buffer.toString();
    }

    public static MessageOrigin getCurrentOrigin(final String name) {
        // https://stackoverflow.com/questions/7348711/recommended-way-to-get-hostname-in-java
        String ip = null;
        String host = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return new MessageOrigin(ip, host, name);
    }

    public String getHost() {
        return host;
    }

    public String getIp() {
        return ip;
    }

    public String getName() {
        return name;
    }

    public byte[] size() {
        return ByteBuffer.allocate(Feiwu.MESSAGE_ORIGIN_HEADER_SIZE).putInt(flatRepresentation.length()).array();
    }

    public byte[] flat() {
        return flatRepresentation.getBytes();
    }

    public static MessageOrigin fromFlat(final String flatRepresentation) {
        String[] split = flatRepresentation.split(SEPARATOR);
        // order = ip, host, name
        String ip = split[0];
        String host = split[1];
        String name = split[2];
        return new MessageOrigin(ip, host, name);
    }

    @Override
    public String toString() {
        return "MessageOrigin{" +
            "host='" + host + '\'' +
            ", ip='" + ip + '\'' +
            ", name='" + name + '\'' +
            '}';
    }

}
