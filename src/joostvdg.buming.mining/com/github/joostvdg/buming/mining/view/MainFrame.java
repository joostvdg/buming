package com.github.joostvdg.buming.mining.view;

import com.github.joostvdg.buming.mining.constants.Constants;
import com.github.joostvdg.buming.mining.workers.MineLayer;
import com.github.joostvdg.buming.mining.workers.MineSweeper;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainFrame extends JFrame implements ButtonListener {

    private Board board;
    private Toolbar toolbar;
    private ExecutorService layersExecutor;
    private ExecutorService sweepersExecutor;
    private MineLayer[] mineLayers;
    private MineSweeper[] mineSweepers;


    public MainFrame() {
        super(Constants.APP_NAME);

        mineLayers = new MineLayer[Constants.NUMBER_OF_MINE_LAYERS];
        mineSweepers = new MineSweeper[Constants.NUMBER_OF_SWEEPERS];
        toolbar = new Toolbar();
        toolbar.setButtonListener(this);
        board = new Board();

        add(toolbar, BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);

        setSize(Constants.BOARD_WIDTH_PXL, Constants.BOARD_HEIGHT_PXL);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void startClicked() {
        System.out.println("Start the run");
        this.layersExecutor = Executors.newFixedThreadPool(Constants.NUMBER_OF_MINE_LAYERS);
        this.sweepersExecutor = Executors.newFixedThreadPool(Constants.NUMBER_OF_SWEEPERS);

        try{

            for(int i=0;i<Constants.NUMBER_OF_MINE_LAYERS;i++){
                mineLayers[i] = new MineLayer(i, board);
                layersExecutor.execute(mineLayers[i]);
            }

            for(int i=0;i<Constants.NUMBER_OF_SWEEPERS;i++){
                mineSweepers[i] = new MineSweeper(i, board);
                sweepersExecutor.execute(mineSweepers[i]);
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            layersExecutor.shutdown();
            sweepersExecutor.shutdown();
        }
    }

    @Override
    public void stopClicked() {
        System.out.println("Stop the run");
        for(MineLayer mineLayer : this.mineLayers){
            mineLayer.setLayerRunning(false);
        }

        for(MineSweeper mineSweeper : this.mineSweepers){
            mineSweeper.setSweeperRunning(false);
        }

        if (layersExecutor == null && sweepersExecutor == null) {
            System.out.println("Executors never started, so not stopping them");
            return;
        }
        layersExecutor.shutdown();
        sweepersExecutor.shutdown();

        try {
            layersExecutor.awaitTermination(1, TimeUnit.MINUTES);
            sweepersExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        layersExecutor.shutdownNow();
        sweepersExecutor.shutdownNow();

        this.board.clearBoard();

    }

}
