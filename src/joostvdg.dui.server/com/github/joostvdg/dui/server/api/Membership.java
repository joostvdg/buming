package com.github.joostvdg.dui.server.api;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Membership {
    private final String name;
    private final AtomicLong lastSeen;
    private final AtomicInteger failedChecksCount;

    public Membership(String name, long lastSeen) {
        this.name = name;
        this.lastSeen = new AtomicLong(lastSeen);
        this.failedChecksCount = new AtomicInteger(0);
    }

    public String getName() {
        return name;
    }

    public long getLastSeen() {
        return lastSeen.get();
    }

    public void updateLastSeen(long lastSeen){
        this.lastSeen.set(lastSeen);
    }

    public int failedCheckCount() {
        return failedChecksCount.get();
    }

    public int incrementFailedCheckCount() {
        return failedChecksCount.incrementAndGet();
    }

    @Override
    public String toString() {
        return "Membership{" +
            "name='" + name + '\'' +
            ", lastSeen=" + lastSeen.get() +
            ", failedChecksCount=" + failedChecksCount.get() +
            '}';
    }
}
