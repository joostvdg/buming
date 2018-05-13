package com.github.joostvdg.dui.api.message;

import com.github.joostvdg.dui.api.Feiwu;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class MessageOrigin {
    private final String host;
    private final String ip;
    private final String name;
    private final String role;
    private final String flatRepresentation;
    private static final String SEPARATOR = ",";
    // Valid linux host names: https://stackoverflow.com/questions/3523028/valid-characters-of-a-hostname

    // TODO: add regex test for the variables

    public MessageOrigin(final String host, final String ip, final String name, final String role) {
        this.host = host;
        this.ip = ip;
        this.name = name;
        this.role = role;
        StringBuffer buffer = new StringBuffer();
        buffer.append(host);
        buffer.append(SEPARATOR);
        buffer.append(ip);
        buffer.append(SEPARATOR);
        buffer.append(name);
        buffer.append(SEPARATOR);
        buffer.append(role);
        flatRepresentation = buffer.toString();
    }

    public static MessageOrigin getCurrentOrigin(final String name, final String role) {
        // https://stackoverflow.com/questions/7348711/recommended-way-to-get-hostname-in-java
        String ip = null;
        String host = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return new MessageOrigin(host, ip, name, role);
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
        String host = split[0];
        String ip = split[1];
        String name = split[2];
        String role = split[3];
        return new MessageOrigin(host, ip, name, role);
    }

    @Override
    public String toString() {
        return "MessageOrigin{" +
            "host='" + host + '\'' +
            ", ip='" + ip + '\'' +
            ", role='" + role + '\'' +
            ", name='" + name + '\'' +
            '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageOrigin that = (MessageOrigin) o;
        return Objects.equals(host, that.host) &&
            Objects.equals(ip, that.ip) &&
            Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(host, ip, name);
    }
}
