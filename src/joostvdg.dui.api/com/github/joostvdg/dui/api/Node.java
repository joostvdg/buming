package com.github.joostvdg.dui.api;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class Node {

    private final String name;
    private final String hostName;
    private final String hostIp;
    private long uptime;

    private LeaderElectionStatus status;
    private NodeRole role;

    public Node(String name, String hostName, String hostIp) {
        this.name = name;
        this.hostName = hostName;
        this.hostIp = hostIp;
    }

    public String getName() {
        return name;
    }

    public String getHostName() {
        return hostName;
    }

    public String getHostIp() {
        return hostIp;
    }

    public long getUptime() {
        return uptime;
    }

    public LeaderElectionStatus getStatus() {
        return status;
    }

    public void setStatus(LeaderElectionStatus status) {
        this.status = status;
    }

    public void updateUptime() {
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        this.uptime = rb.getUptime();
    }

    public NodeRole getRole() {
        return role;
    }

    public void setRole(NodeRole role) {
        this.role = role;
    }

    @Override
    public String toString() {

        return "Node{" +
            "name='" + name + '\'' +
            ", hostName='" + hostName + '\'' +
            ", hostIp='" + hostIp + '\'' +
            ", uptime=" + uptime +
            ", status=" + status +
            ", role=" + role +
            '}';
    }
}


/*
    RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
    System.out.println("Up time: " + rb.getUptime() + " ms");
    Thread.sleep(1000 * 10);
    System.out.println("Up time: " + rb.getUptime() + " ms");
 */
