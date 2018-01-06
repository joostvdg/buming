package com.github.joostvdg.buming.sorting;

import com.github.joostvdg.buming.api.SortingExample;
import com.github.joostvdg.buming.logging.Logger;

import java.util.Arrays;
import java.util.Random;

public class BinarySearch implements SortingExample {

    private static final int AMOUNT_OF_VALUES_SMALL = 1000;
    private final int[] values;

    private static final int AMOUNT_OF_VALUES_LARGE = 10000000;
    private final int[] manyValues;

    public BinarySearch() {
        values = new int[AMOUNT_OF_VALUES_SMALL];
        manyValues = new int[AMOUNT_OF_VALUES_LARGE];
        Random random = new Random();

        for (int i =0; i < AMOUNT_OF_VALUES_SMALL; i++) {
            int pseudoRandomNumber = random.nextInt(AMOUNT_OF_VALUES_SMALL -1);
            values[i] = pseudoRandomNumber;
        }
        for (int i =0; i < AMOUNT_OF_VALUES_LARGE; i++) {
            int pseudoRandomNumber = random.nextInt(AMOUNT_OF_VALUES_LARGE -1);
            manyValues[i] = pseudoRandomNumber;
        }
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void sort(Logger logger) {
        // LINEAR SMALL
        Random random = new Random();
        int linearSmallTarget = random.nextInt(AMOUNT_OF_VALUES_SMALL);
        System.out.println("[BinarySearch][Linear Search Small] Start");
        long startTime = System.nanoTime();
        int linearSmallResult = linearSearch(values, linearSmallTarget);
        long endTime = System.nanoTime();
        long linearSmallrunningTime = endTime - startTime;
        if (linearSmallResult == -1) {
            System.out.println("[BinarySearch][Linear Search Small] Did not find " + linearSmallTarget + " in " + linearSmallrunningTime + " nano's" );
        } else {
            System.out.println("[BinarySearch][Linear Search Small] Found target " + linearSmallTarget + " at index " + linearSmallResult + " in " + linearSmallrunningTime + " nano's" );
        }
        System.out.println("[BinarySearch][Linear Search Small] End");

        // LINEAR LARGE
        int linearLargeTarget = random.nextInt(AMOUNT_OF_VALUES_LARGE);
        System.out.println("[BinarySearch][Linear Search Large] Start");
        startTime = System.nanoTime();
        int linearLargeResult = linearSearch(manyValues, linearLargeTarget);
        endTime = System.nanoTime();
        long linearLargeRunningTime = endTime - startTime;
        if (linearLargeResult == -1) {
            System.out.println("[BinarySearch][Linear Search Large] Did not find " + linearLargeTarget + " in " + linearLargeRunningTime + " nano's");
        } else {
            System.out.println("[BinarySearch][Linear Search Large] Found target " + linearLargeTarget + " at index " + linearLargeResult + " in " + linearLargeRunningTime + " nano's");
        }
        System.out.println("[BinarySearch][Linear Search Large] End");

        // SORTED
        Arrays.sort(values);
        Arrays.sort(manyValues);

        // BINARY SMALL
        int binarySmallTarget = random.nextInt(AMOUNT_OF_VALUES_SMALL);
        System.out.println("[BinarySearch][Binary Search Small] Start");
        startTime = System.nanoTime();
        int binarySmallResult = binarySearch(values, binarySmallTarget);
        endTime = System.nanoTime();
        long binarySmallrunningTime = endTime - startTime;
        if (binarySmallResult == -1) {
            System.out.println("[BinarySearch][Binary Search Small] Did not find " + binarySmallTarget + " in " + binarySmallrunningTime + " nano's"  );
        } else {
            System.out.println("[BinarySearch][Binary Search Small] Found target " + binarySmallTarget + " at index " + binarySmallResult + " in " + binarySmallrunningTime + " nano's"  );
        }
        System.out.println("[BinarySearch][Binary Search Small] End");

        // BINARY LARGE
        int binaryLargeTarget = random.nextInt(AMOUNT_OF_VALUES_LARGE);
        System.out.println("[BinarySearch][Binary Search Large] Start");
        startTime = System.nanoTime();
        int binaryLargeResult = binarySearch(manyValues, binaryLargeTarget);
        endTime = System.nanoTime();
        long binaryLargeRunningTime = endTime - startTime;
        if (binaryLargeResult == -1) {
            System.out.println("[BinarySearch][Binary Search Large] Did not find " + binaryLargeTarget + " in " + binaryLargeRunningTime + " nano's"  );
        } else {
            System.out.println("[BinarySearch][Binary Search Large] Found target " + binaryLargeTarget + " at index " + binaryLargeResult + " in " + binaryLargeRunningTime + " nano's" );
        }
        System.out.println("[BinarySearch][Binary Search Large] End");

        System.out.println("[Linear Small]\t[Linear Large]\t[Binary Small]\t[Binary Large]");
        System.out.println("" + linearSmallrunningTime + "\t" + "\t"  + linearLargeRunningTime + "\t"  + "\t" + binarySmallrunningTime+ "\t" + "\t" + binaryLargeRunningTime);

    }

    private int binarySearch(int[] array, int targetValue) {
        int min = 0;
        int max = array.length - 1;
        int middle = max / 2;

        while(max > min) {
            if (array[middle] == targetValue) {
                return middle;
            } else if (array[middle] > targetValue) {
                max = middle -1;
            } else {
                min = middle +1;
            }
            middle = Math.floorDiv((min + max), 2);
            //System.out.println("Min:"+ min + ",Middle:"+middle + ", Max:" + max + ", Middle Value:"+array[middle] + ", Target Value:" + targetValue );
        }
        return  -1;
    }

    private int linearSearch(int[] array, int targetValue) {
        for(int i =0; i < array.length; i++) {
            if(targetValue == array[i]) {
                return i;
            }
        }
        return -1;
    }

}
