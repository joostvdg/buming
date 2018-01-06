package com.github.joostvdg.buming.mining.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Toolbar extends JPanel implements ActionListener {

    private JButton start;
    private JButton stop;
    private ButtonListener listener;

    public Toolbar(){
        setLayout(new FlowLayout(FlowLayout.CENTER));
        initialize();
        add(stop);
        add(start);
    }

    private void initialize() {
        start = new JButton("Start");
        start.addActionListener(this);
        stop = new JButton("Stop");
        stop.addActionListener(this);
    }

    public void setButtonListener(ButtonListener buttonListener) {
        this.listener = buttonListener;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (listener == null) {
            throw new IllegalStateException("No listener set");
        }

        if ( event.getSource() == start) {
            listener.startClicked();
        } else if (event.getSource() == stop) {
            listener.stopClicked();
        } else {
            throw new IllegalStateException("Event happened from unknown source");
        }
    }
}
