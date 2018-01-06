package com.github.joostvdg.buming.mining.workers;

import com.github.joostvdg.buming.mining.constants.Constants;
import com.github.joostvdg.buming.mining.view.Board;

import java.util.Random;

public class MineLayer implements Runnable {

    private final int id;
    private final Board board;
    private volatile boolean layerRunning;

    public MineLayer(final int id, final Board board) {
        this.id = id;
        this.board = board;
        this.layerRunning = true;
    }

    public void setLayerRunning(boolean layerRunning) {
        this.layerRunning = layerRunning;
    }

    @Override
    public void run() {
        Random random = new Random();

        while(layerRunning) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            int cellIndex = random.nextInt(Constants.BOARD_COLUMNS * Constants.BOARD_ROWS);
            board.setMine(cellIndex);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
