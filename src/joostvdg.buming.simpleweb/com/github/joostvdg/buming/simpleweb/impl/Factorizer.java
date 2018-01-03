package com.github.joostvdg.buming.simpleweb.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Factorizer {
    private static int MAX_FACTORS = 10;
    private BigInteger lastNumber;
    private BigInteger[] lastFactors;
    private long hits;
    private long cacheHits;

    public synchronized long getHits() {
        return hits;
    }

    public synchronized double getCacheHitRatio() {
        return (double) cacheHits / (double) hits;
    }

    public void service(long counter) {
        BigInteger i = BigInteger.valueOf(counter);
        BigInteger[] factors = null;
        synchronized (this) {
            if (lastFactors == null) {
                lastFactors = new BigInteger[MAX_FACTORS];
            }
            ++hits;
            if (i.equals(lastNumber)) {
                ++cacheHits;
                factors = lastFactors.clone();
            }
        }
        if (factors == null) {
            factors = factor(i);
            synchronized (this) {
                lastNumber = i;
                lastFactors = factors.clone();
            }
        }
    }

    /**
     * Vogella factorize: http://www.vogella.com/tutorials/JavaAlgorithmsPrimeFactorization/article.html
     * Copyright © 2012-2017 vogella GmbH. Free use of the software examples is granted under the terms of the EPL License.
     * This tutorial is published under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Germany license.
     *
     * @param number
     * @return
     */
    private BigInteger[] factor(BigInteger number) {
        long n = number.longValue();
        n = n*n;
        return new BigInteger[] { BigInteger.valueOf(n)};
    }

}
