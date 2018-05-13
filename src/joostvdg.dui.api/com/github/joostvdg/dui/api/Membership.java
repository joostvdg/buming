package com.github.joostvdg.dui.api;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Membership {
    private final String name;
    private final String role;
    private final AtomicLong lastSeen;
    private final AtomicInteger failedChecksCount;

    public Membership(String name, String role, long lastSeen) {
        this.name = name;
        this.lastSeen = new AtomicLong(lastSeen);
        this.failedChecksCount = new AtomicInteger(0);
        this.role = role;
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

    public String getRole(){
        return role;
    }

    @Override
    public String toString() {
        return "Membership{" +
            "name='" + name + '\'' +
            ",role='" + role + '\'' +
            ", lastSeen=" + lastSeen.get() +
            ", failedChecksCount=" + failedChecksCount.get() +
            '}';
    }
}
