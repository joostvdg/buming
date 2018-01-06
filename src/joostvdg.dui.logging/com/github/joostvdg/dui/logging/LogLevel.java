package com.github.joostvdg.dui.logging;

public enum LogLevel {
    DEBUG(0), INFO(1),WARN(2);

    private int level;

    LogLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
