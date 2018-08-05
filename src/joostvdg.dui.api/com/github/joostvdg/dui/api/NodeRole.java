package com.github.joostvdg.dui.api;

public enum NodeRole {
    NONE((byte)0x00),
    MANAGER((byte)0x01),
    WORKER((byte)0x02);

    private byte status;

    NodeRole(final byte status) {
        this.status = status;
    }

    public byte getStatus(){
        return this.status;
    }

    public static NodeRole byValue(final byte value) {
        for (NodeRole status : NodeRole.values()) {
            if (status.status == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Not a valid value");
    }
}
