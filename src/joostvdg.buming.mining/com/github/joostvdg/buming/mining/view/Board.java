package com.github.joostvdg.buming.mining.view;

import com.github.joostvdg.buming.mining.constants.Constants;

import javax.swing.*;
import java.awt.*;

public class Board extends JPanel {

    private Cell[] cells;
    private int numberOfMines;

    public Board() {
        initialize();
        setLayout(new GridLayout(Constants.BOARD_ROWS, Constants.BOARD_COLUMNS));
        initializeBoard();
        System.out.println("Created " + cells.length + " cells");
    }

    private void initialize() {
        cells = new Cell[Constants.BOARD_ROWS * Constants.BOARD_COLUMNS];
        numberOfMines = 0;
    }

    private void initializeBoard() {
        for(int cellNumber =0; cellNumber < (Constants.BOARD_COLUMNS * Constants.BOARD_ROWS); cellNumber++) {
            int rowNumber = Math.floorDiv(cellNumber, Constants.BOARD_ROWS);
            Color baseColor = Color.GREEN;
            if (isEven(rowNumber)) {
                baseColor =  isEven(cellNumber) ? Color.WHITE : Color.GRAY;
            } else {
                baseColor =  isEven(cellNumber) ? Color.GRAY : Color.WHITE;
            }
            cells[cellNumber] = new Cell(cellNumber, baseColor);
            add(cells[cellNumber]);
        }
        System.out.println("Board is initialized and ready for play");
    }

    public void clearBoard() {
        System.out.println("Clearing the board");
        for(int i =0; i < cells.length; i++) {
            cells[i].clear();
        }
    }

    public boolean isEven(int number) {
        return number % 2 == 0;
    }

    public synchronized void incrementAmountOfMines(){
        this.numberOfMines++;
    }

    public synchronized void decrementAmountOfMines(){
        this.numberOfMines--;
    }

    public void setMine(int cellIndex) {
        cells[cellIndex].lock();
        incrementAmountOfMines();
        cells[cellIndex].mined();
        cells[cellIndex].unlock();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sweepMine(int cellIndex) {
        cells[cellIndex].lock();
        decrementAmountOfMines();
        cells[cellIndex].sweeped();
        cells[cellIndex].unlock();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
