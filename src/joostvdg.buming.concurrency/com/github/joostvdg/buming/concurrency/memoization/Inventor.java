package com.github.joostvdg.buming.concurrency.memoization;

import java.util.Random;

public class Inventor {

    private final String name;

    public Inventor(String name) {
        this.name = name;
    }

    public Invention invent(String invention) {
        long pseudoRandom = new Random().nextInt(50);
        pseudoRandom = pseudoRandom * 150;
        try {
            Thread.sleep(pseudoRandom);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Invention(invention, name);
    }
}
