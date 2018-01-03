package com.github.joostvdg.buming.concurrency.memoization;

import java.util.Objects;

public class Invention {

    private final String name;
    private final String inventorName;

    public Invention(String name, String inventorName) {
        this.name = name;
        this.inventorName = inventorName;
    }

    public String getName() {
        return name;
    }

    public String getInventorName() {
        return inventorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invention wheel = (Invention) o;
        return Objects.equals(name, wheel.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
