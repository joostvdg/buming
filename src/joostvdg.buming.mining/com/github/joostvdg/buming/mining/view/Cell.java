package com.github.joostvdg.buming.mining.view;

import com.github.joostvdg.buming.mining.constants.State;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Cell extends JPanel {
    private final int id;
    private final Lock lock;
    private final Color baseColor;
    private State state;
    private boolean hasMine;

    public Cell(final int id, final Color baseColor) {
        this.id = id;
        this.baseColor = baseColor;
        this.lock = new ReentrantLock(true);
        this.state = State.EMPTY;
        this.hasMine = false;
        setBackground(baseColor);
        setLayout(new GridLayout());
    }

    public void lock() {
        try {
            lock.tryLock(10, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unlock() {
        lock.unlock();
    }

    @Override
    public String toString() {
        return "Cell{" +
            "id=" + id +
            ", state=" + state +
            ", hasMine=" + hasMine +
            '}';
    }

    public void mined() {
        lock();
        this.setBackground(Color.ORANGE);
        this.hasMine = true;
        unlock();
    }

    public void sweeped() {
        lock();
        this.setBackground(this.baseColor);
        this.hasMine = false;
        unlock();
    }

    public void clear() {
        this.setBackground(this.baseColor);
        this.hasMine = false;
        this.state = State.EMPTY;
    }

}
