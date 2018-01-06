package com.github.joostvdg.dui.api.message;

public enum FeiwuMessageType {
    UNIDENTIFIED((byte)0x00),
    HELLO((byte)0x01),
    MEMBERSHIP((byte)0x02);

    private byte identifier;

    FeiwuMessageType(byte identifier) {
        this.identifier = identifier;
    }

    public byte getIdentifier(){
        return this.identifier;
    }

}
