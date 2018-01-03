package com.github.joostvdg.buming.concurrency.range;

public class SafeNumberRange implements NumberRange{
    private int lower;
    private int upper;

    @Override
    public synchronized void setLower(int lower) {
        if (lower > upper) {
            throw new IllegalArgumentException("can’t set lower to " + lower + " > upper");
        }
        this.lower = lower;
    }

    @Override
    public int getLower() {
        return lower;
    }

    @Override
    public synchronized void setUpper(int upper) {
        if (upper < lower) {
            throw new IllegalArgumentException("can’t set upper to " + upper + " < lower");
        }
        this.upper = upper;
    }

    @Override
    public int getUpper() {
        return upper;
    }

    @Override
    public synchronized boolean isInRange(int i) {
        return (i >= lower && i <= upper);
    }
}
