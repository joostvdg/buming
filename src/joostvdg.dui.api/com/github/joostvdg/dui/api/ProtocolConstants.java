package com.github.joostvdg.dui.api;

public final class ProtocolConstants {

    private ProtocolConstants() { } // class for constants should not be instantiatable

    public static final int EXTERNAL_COMMUNICATION_PORT_A = 7771;
    public static final int EXTERNAL_COMMUNICATION_PORT_B = 7772;
    public static final int EXTERNAL_COMMUNICATION_PORT_C = 7773;

    public static final int INTERNAL_COMMUNICATION_PORT_A = 7781;
    public static final int INTERNAL_COMMUNICATION_PORT_B = 7782;
    public static final int INTERNAL_COMMUNICATION_PORT_C = 7783;

    public static final int HEALTH_CHECK_PORT = 8888;

    public static final String MEMBERSHIP_LEAVE_MESSAGE = "So Long and Thanks for All the Fish";
    public static final String MESSAGE_SEGMENT_DELIMITER = ",";

    public static final String MULTICAST_GROUP = "230.0.0.0";

    public static final String[] POTENTIAL_SERVER_NAMES = new String[] {
      "Tim Berners-Lee", "Kathleen Booth", "Grace Hopper",
        "Vint Cerf", "Edsger Dijkstra","Douglas Engelbart",
        "Alan Kay", "Leslie Lamport", "John McCarthy",
        "John von Neumann", "Ken Thompson", "Alan Turing",
        "Fred Brooks", "Linus Torvalds", "Barbara Liskov",
        "Larry Page", "Guido Van Rassum", "Ada Lovelace",
        "Dennis Ritchie", "Donald Knuth", "Bjarne Stroustrup",
        "Brian Kernighan", "James Gosling", "Richard Stallman",
        "Niklaus Wirth", "Larry Wall", "Brendan Eich",
        "John D. Carmack", "John Backus", "Bram Cohen"
    };
}
