package com.github.joostvdg.buming.concurrency.range;

public interface NumberRange {

    /**
     * Set the lower bound of the range.
     * @param lower must be lower than the current upper bound
     */
    void setLower(int lower);

    /**
     * Get the lower bound.
     * @return the lower bound int
     */
    int getLower();

    /**
     * Set the upper bound of the range.
     * @param upper the upper bound
     */
    void setUpper(int upper);

    /**
     * Get the upper bound.
     * @return the upper bound int
     */
    int getUpper();

    /**
     * Check if the supplied int is in range.
     * @param i the value to check
     * @return true if it is between the upper and lower bound (inclusive)
     */
    boolean isInRange(int i);
}
