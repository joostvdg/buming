package com.github.joostvdg.buming.concurrency.range;

import java.util.concurrent.atomic.AtomicInteger;

public class UnsafeNumberRange implements NumberRange{

    // INVARIANT: lower <= upper
    private final AtomicInteger lower = new AtomicInteger(0);
    private final AtomicInteger upper = new AtomicInteger(0);

    @Override
    public void setLower(int i) {
        // Warning -- unsafe check-then-act
        if (i > upper.get())
            throw new IllegalArgumentException(
                "can’t set lower to " + i + " > upper");
        lower.set(i);
    }

    @Override
    public int getLower() {
        return lower.get();
    }

    @Override
    public void setUpper(int i) {
    // Warning -- unsafe check-then-act
        if (i < lower.get())
            throw new IllegalArgumentException(
                "can’t set upper to " + i + " < lower");
        upper.set(i);
    }

    @Override
    public int getUpper() {
        return upper.get();
    }

    @Override
    public boolean isInRange(int i) {
        return (i >= lower.get() && i <= upper.get());
    }
}
