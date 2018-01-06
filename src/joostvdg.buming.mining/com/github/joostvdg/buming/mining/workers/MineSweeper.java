package com.github.joostvdg.buming.mining.workers;

import com.github.joostvdg.buming.mining.constants.Constants;
import com.github.joostvdg.buming.mining.view.Board;

import java.util.Random;

public class MineSweeper implements Runnable {
    private final int id;
    private final Board board;
    private volatile boolean sweeperRunning;

    public MineSweeper(final int id, final Board board) {
        this.id = id;
        this.board = board;
        this.sweeperRunning = true;
    }

    public void setSweeperRunning(boolean sweeperRunning) {
        this.sweeperRunning = sweeperRunning;
    }

    @Override
    public void run() {
        Random random = new Random();

        while(sweeperRunning) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            int cellIndex = random.nextInt(Constants.BOARD_COLUMNS * Constants.BOARD_ROWS);
            board.sweepMine(cellIndex);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
