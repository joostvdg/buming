package com.github.joostvdg.buming.sorting;

import com.github.joostvdg.buming.api.SortingExample;
import com.github.joostvdg.buming.logging.Logger;

import java.util.Random;

public class SelectionSort implements SortingExample {
    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    private static final int AMOUNT_OF_VALUES_SMALL = 10;
    private static final int AMOUNT_OF_VALUES_MEDIUM = 2500;
    private static final int AMOUNT_OF_VALUES_LARGE = 50000;

    @Override
    public void sort(Logger logger) {
        int[] values = new int[AMOUNT_OF_VALUES_SMALL];
        int[] mediumValues = new int[AMOUNT_OF_VALUES_MEDIUM];
        int[] manyValues = new int[AMOUNT_OF_VALUES_LARGE];
        Random random = new Random();

        for (int i =0; i < AMOUNT_OF_VALUES_SMALL; i++) {
            int pseudoRandomNumber = random.nextInt(AMOUNT_OF_VALUES_SMALL * 3);
            values[i] = pseudoRandomNumber;
        }
        for (int i =0; i < AMOUNT_OF_VALUES_MEDIUM; i++) {
            int pseudoRandomNumber = random.nextInt(AMOUNT_OF_VALUES_MEDIUM * 3);
            mediumValues[i] = pseudoRandomNumber;
        }
        for (int i =0; i < AMOUNT_OF_VALUES_LARGE; i++) {
            int pseudoRandomNumber = random.nextInt(AMOUNT_OF_VALUES_LARGE * 3);
            manyValues[i] = pseudoRandomNumber;
        }

        long startTime = System.currentTimeMillis();
        selectionSort(values, true);
        long endTime = System.currentTimeMillis();
        double runningTimeSmall = (double)(endTime - startTime) / 1000;

        startTime = System.currentTimeMillis();
        selectionSort(mediumValues, false);
        endTime = System.currentTimeMillis();
        double mediumTimeLarge = (double)(endTime - startTime) / 1000;

        startTime = System.currentTimeMillis();
        selectionSort(manyValues, false);
        endTime = System.currentTimeMillis();
        double runningTimeLarge = (double)(endTime - startTime) / 1000;

        System.out.println("[SelectionSort][Small] sorting took: " + runningTimeSmall + " seconds, for " + AMOUNT_OF_VALUES_SMALL + " elements" );
        System.out.println("[SelectionSort][Medium] sorting took: " + mediumTimeLarge + " seconds, for " + AMOUNT_OF_VALUES_MEDIUM + " elements" );
        System.out.println("[SelectionSort][Large] sorting took: " + runningTimeLarge + " seconds, for " + AMOUNT_OF_VALUES_LARGE + " elements" );
    }

    private void selectionSort(int[] values, boolean verbose){
        int cursor = 0;
        while(cursor < (values.length -1)) {
            int cursorValue = values[cursor];
            int minValue = cursorValue;
            int minValueIndex = cursor;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(cursor);
            stringBuilder.append("=");
            stringBuilder.append(cursorValue);
            for(int i = cursor+1; i < values.length; i++) {
                stringBuilder.append(", ");
                stringBuilder.append(i);
                stringBuilder.append("=");
                stringBuilder.append(values[i]);
                if(values[i] < cursorValue) {
                    minValue = values[i];
                    minValueIndex = i;
                }
            }
            values[minValueIndex] = cursorValue;
            values[cursor] = minValue;
            if (verbose) {
                System.out.println("Finished round: minValue=" + minValue + ", at index=" + minValueIndex);
                System.out.println(stringBuilder.toString());
            }
            cursor++;
        }
    }
}
