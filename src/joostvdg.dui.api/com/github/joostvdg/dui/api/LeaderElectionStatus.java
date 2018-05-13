package com.github.joostvdg.dui.api;

public enum LeaderElectionStatus {
    NONE((byte)0x00),
    LEADER((byte)0x01),
    CANDIDATE((byte)0x02),
    FOLLOWER((byte)0x03);

    private byte status;

    LeaderElectionStatus(final byte status) {
        this.status = status;
    }

    public byte getStatus(){
        return this.status;
    }

    public static LeaderElectionStatus byValue(final byte value) {
        for (LeaderElectionStatus status : LeaderElectionStatus.values()) {
            if (status.status == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Not a valid value");
    }
}
