package com.github.joostvdg.buming.sorting;

import com.github.joostvdg.buming.api.SortingExample;
import com.github.joostvdg.buming.logging.Logger;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

public class MergeSort implements SortingExample{

    private static final int AMOUNT_OF_VALUES_SMALL = 10;
    private static final int AMOUNT_OF_VALUES_MEDIUM = 5000000;
    private static final int AMOUNT_OF_VALUES_LARGE = 50000000;

    private int[] originalArray;
    private int[] tempArray;

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

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

        // SEQUENTIAL
        tempArray = new int[values.length];
        originalArray = values;
        int[] originalCopy = Arrays.copyOf(values, values.length);
        long startTime = System.currentTimeMillis();
        mergeSort(0, values.length -1, true);
        long endTime = System.currentTimeMillis();
        double runningTime = (double)(endTime - startTime) / 1000;
        System.out.println("[MergeSort][Sequential]\t[Small]\tsorting took: " + runningTime + " seconds, for " + AMOUNT_OF_VALUES_SMALL + " elements" );
        String originalArrayString = printArray(originalCopy);
        String sortedArrayString = printArray(values);
        System.out.println("[MergeSort][Sequential]\t[Small] [Original]\t" + originalArrayString);
        System.out.println("[MergeSort][Sequential]\t[Small]   [Sorted]\t" + sortedArrayString);

        tempArray = new int[mediumValues.length];
        originalArray = mediumValues;
        startTime = System.currentTimeMillis();
        mergeSort(0, mediumValues.length -1, false);
        endTime = System.currentTimeMillis();
        runningTime = (double)(endTime - startTime) / 1000;
        System.out.println("[MergeSort][Sequential]\t[Medium] sorting took: " + runningTime + " seconds, for " + AMOUNT_OF_VALUES_MEDIUM + " elements" );
        validateSorting(mediumValues);

        tempArray = new int[manyValues.length];
        originalArray = manyValues;
        startTime = System.currentTimeMillis();
        mergeSort(0, manyValues.length -1, false);
        endTime = System.currentTimeMillis();
        runningTime = (double)(endTime - startTime) / 1000;
        System.out.println("[MergeSort][Sequential]\t[Large] sorting took: " + runningTime + " seconds, for " + AMOUNT_OF_VALUES_LARGE + " elements" );
        validateSorting(manyValues);

        // NAIEVE PARALLEL
        // have to "reset" the array's
        System.out.println("[MergeSort][Naieve-Parallel] cleaning array's");
        values = new int[AMOUNT_OF_VALUES_SMALL];
        mediumValues = new int[AMOUNT_OF_VALUES_MEDIUM];
        manyValues = new int[AMOUNT_OF_VALUES_LARGE];
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

        tempArray = new int[values.length];
        originalArray = values;
        originalCopy = Arrays.copyOf(values, values.length);
        startTime = System.currentTimeMillis();
        naieveParallelMergeSort(0, values.length -1, true);
        endTime = System.currentTimeMillis();
        runningTime = (double)(endTime - startTime) / 1000;
        System.out.println("[MergeSort][Naieve-Parallel]\t[Small]\tsorting took: " + runningTime + " seconds, for " + AMOUNT_OF_VALUES_SMALL + " elements" );
        originalArrayString = printArray(originalCopy);
        sortedArrayString = printArray(values);
        System.out.println("[MergeSort][Naieve-Parallel]\t[Small] [Original]\t" + originalArrayString);
        System.out.println("[MergeSort][Naieve-Parallel]\t[Small]   [Sorted]\t" + sortedArrayString);
        validateSorting(values);
        // Cannot go beyond this amount with the naieve approach
        // Exception in thread "Thread-20388" java.lang.OutOfMemoryError: unable to create native thread: possibly out of memory or process/resource limits reached

        // Limited Threads PARALLEL
        // have to "reset" the array's
        System.out.println("[MergeSort][Parallel-Cached] cleaning array's");
        values = new int[AMOUNT_OF_VALUES_SMALL];
        for (int i =0; i < AMOUNT_OF_VALUES_SMALL; i++) {
            int pseudoRandomNumber = random.nextInt(AMOUNT_OF_VALUES_SMALL * 3);
            values[i] = pseudoRandomNumber;
        }

        // ExecutorService executorService = Executors.newCachedThreadPool();
        // cached failed with: [21,103s][warning][os,thread] Failed to start thread - pthread_create failed (EAGAIN) for attributes: stacksize: 1024k, guardsize: 0k, detached.
        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        tempArray = new int[values.length];
        originalArray = values;
        originalCopy = Arrays.copyOf(values, values.length);
        startTime = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        limitedParallelMergeSort(0, values.length -1, executorService, countDownLatch, numberOfThreads, true);
        try {
            System.out.println("[MergeSort][Parallel][Small] Waiting on countDownLatch [ "+ countDownLatch.toString() + "]");
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTime = System.currentTimeMillis();
        runningTime = (double)(endTime - startTime) / 1000;
        System.out.println("[MergeSort][Parallel]\t[Small]\tsorting took: " + runningTime + " seconds, for " + AMOUNT_OF_VALUES_SMALL + " elements and " + numberOfThreads + " threads" );
        originalArrayString = printArray(originalCopy);
        sortedArrayString = printArray(values);
        System.out.println("[MergeSort][Parallel]\t[Small] [Original]\t" + originalArrayString);
        System.out.println("[MergeSort][Parallel]\t[Small]   [Sorted]\t" + sortedArrayString);
        validateSorting(values);

        // PARALLEL MEDIUM
        tempArray = new int[mediumValues.length];
        originalArray = mediumValues;
        startTime = System.currentTimeMillis();
        countDownLatch = new CountDownLatch(1);
        limitedParallelMergeSort(0, mediumValues.length -1, executorService, countDownLatch, numberOfThreads, false);
        try {
            System.out.println("[MergeSort][Parallel]\t[Medium] Waiting on countDownLatch [ "+ countDownLatch.toString() + "]");
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTime = System.currentTimeMillis();
        runningTime = (double)(endTime - startTime) / 1000;
        System.out.println("[MergeSort][Parallel]\t[Medium]\tsorting took: " + runningTime + " seconds, for " + AMOUNT_OF_VALUES_MEDIUM + " elements and " + numberOfThreads + " threads" );
        validateSorting(mediumValues);

        tempArray = new int[manyValues.length];
        originalArray = manyValues;
        startTime = System.currentTimeMillis();
        countDownLatch = new CountDownLatch(1);
        limitedParallelMergeSort(0, manyValues.length -1, executorService, countDownLatch, numberOfThreads, false);
        try {
            System.out.println("[MergeSort][Parallel]\t[Large] Waiting on countDownLatch [ "+ countDownLatch.toString() + "]");
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTime = System.currentTimeMillis();
        runningTime = (double)(endTime - startTime) / 1000;
        System.out.println("[MergeSort][Parallel]\t[Large]\tsorting took: " + runningTime + " seconds, for " + AMOUNT_OF_VALUES_LARGE + " elements and " + numberOfThreads + " threads" );
        validateSorting(mediumValues);

        System.out.println("[MergeSort][Parallel2] cleaning array's");
        values = new int[AMOUNT_OF_VALUES_SMALL];
        mediumValues = new int[AMOUNT_OF_VALUES_MEDIUM];
        manyValues = new int[AMOUNT_OF_VALUES_LARGE];
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
        numberOfThreads = 16;
        executorService = Executors.newFixedThreadPool(numberOfThreads);
        tempArray = new int[values.length];
        originalArray = values;
        originalCopy = Arrays.copyOf(values, values.length);
        startTime = System.currentTimeMillis();
        countDownLatch = new CountDownLatch(1);
        limitedParallelMergeSort(0, values.length -1, executorService, countDownLatch, numberOfThreads, false);
        try {
            System.out.println("[MergeSort][Parallel2][Small] Waiting on countDownLatch [ "+ countDownLatch.toString() + "]");
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTime = System.currentTimeMillis();
        runningTime = (double)(endTime - startTime) / 1000;
        System.out.println("[MergeSort][Parallel2]\t[Small]\tsorting took: " + runningTime + " seconds, for " + AMOUNT_OF_VALUES_SMALL + " elements and " + numberOfThreads + " threads" );
        originalArrayString = printArray(originalCopy);
        sortedArrayString = printArray(values);
        System.out.println("[MergeSort][Parallel2]\t[Small] [Original]\t" + originalArrayString);
        System.out.println("[MergeSort][Parallel2]\t[Small]   [Sorted]\t" + sortedArrayString);
        validateSorting(values);

        // PARALLEL MEDIUM
        tempArray = new int[mediumValues.length];
        originalArray = mediumValues;
        startTime = System.currentTimeMillis();
        countDownLatch = new CountDownLatch(1);
        limitedParallelMergeSort(0, mediumValues.length -1, executorService, countDownLatch, numberOfThreads, false);
        try {
            System.out.println("[MergeSort][Parallel2]\t[Medium] Waiting on countDownLatch [ "+ countDownLatch.toString() + "]");
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTime = System.currentTimeMillis();
        runningTime = (double)(endTime - startTime) / 1000;
        System.out.println("[MergeSort][Parallel2]\t[Medium]\tsorting took: " + runningTime + " seconds, for " + AMOUNT_OF_VALUES_MEDIUM + " elements and " + numberOfThreads + " threads" );
        validateSorting(mediumValues);

        tempArray = new int[manyValues.length];
        originalArray = manyValues;
        startTime = System.currentTimeMillis();
        countDownLatch = new CountDownLatch(1);
        limitedParallelMergeSort(0, manyValues.length -1, executorService, countDownLatch, numberOfThreads, false);
        try {
            System.out.println("[MergeSort][Parallel2]\t[Large] Waiting on countDownLatch [ "+ countDownLatch.toString() + "]");
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTime = System.currentTimeMillis();
        runningTime = (double)(endTime - startTime) / 1000;
        System.out.println("[MergeSort][Parallel2]\t[Large]\tsorting took: " + runningTime + " seconds, for " + AMOUNT_OF_VALUES_LARGE + " elements and " + numberOfThreads + " threads" );
        validateSorting(mediumValues);
    }

    private void validateSorting(int[] array) {
        for(int i = 0; i < array.length -1; i++) {
            if (i < array.length && array[i] > array[i+1]) {
                throw new IllegalStateException("This is not sorted: [" + array[i] + ", " + array[i+1] + "]");
            } else if(i > 0 && array[i] < array[i-1]) {
                throw new IllegalStateException("This is not sorted: [" + array[i-1] + ", " + array[i] + "]");
            }
        }
    }

    private String printArray(int[] array) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for(int i : array) {
            stringBuilder.append(i);
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    private void naieveParallelMergeSort(int low, int high, boolean verbose) {
        // always attempt to parallelize with more threads
        if (low >= high) { // there's nothing to 'sort'
            return;
        }

        int middle = Math.floorDiv((low + high), 2);
        if (verbose) {
            System.out.println("[MergeSort][Naieve-Parallel] [" + low + "," + middle + "] [" + (middle+1) + "," + high + "]");
        }
        Thread left = new Thread(() -> naieveParallelMergeSort(low, middle, verbose));
        Thread right = new Thread(() -> naieveParallelMergeSort(middle +1, high, verbose));
        left.start();
        right.start();

        try {
            left.join();
            right.join();
            merge(low, middle, high, verbose);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void limitedParallelMergeSort(int low, int high, ExecutorService executorService, CountDownLatch countDownLatch, int numberOfThreads, boolean verbose ) {
        // only attempt to create a new thread if we have the ability to do so
        // always attempt to parallelize with more threads

        if (low >= high) { // there's nothing to 'sort'
            countDownLatch.countDown();
            if (verbose) {
                System.out.println("[MergeSort][Parallel] arrived and counting down [" + low + "," + high + "] [" + countDownLatch.toString() + "]");
            }
            return;
        }

        if (numberOfThreads <= 1) {
            mergeSort(low, high, verbose);
            countDownLatch.countDown();
            return;
        }

        final int updatedNumberOfThreads = numberOfThreads /2;

        int middle = Math.floorDiv((low + high), 2);
        if (verbose) {
            System.out.println("[MergeSort][Parallel] [" + low + "," + middle + "] [" + (middle+1) + "," + high + "]");
        }
        CountDownLatch countDownLatchLeft = new CountDownLatch(1);
        CountDownLatch countDownLatchRight = new CountDownLatch(1);
        Runnable left = () -> limitedParallelMergeSort(low, middle, executorService, countDownLatchLeft, updatedNumberOfThreads, verbose);
        Runnable right = () -> limitedParallelMergeSort(middle + 1, high, executorService, countDownLatchRight, updatedNumberOfThreads, verbose);
        executorService.submit(left);
        executorService.submit(right);

        try {
            if (verbose) {
                System.out.println("[MergeSort][Parallel] [" + low + "," + high + " ] Still waiting on countDownLatch [ left: " + countDownLatchLeft.toString() + ", " + countDownLatchRight.toString() + "]");
            }
            countDownLatchLeft.await();
            countDownLatchRight.await();
            if (verbose) {
                System.out.println("[MergeSort][Parallel] [" + low + "," + high + " ] Done waiting on countDownLatch [ left: " + countDownLatchLeft.toString() + ", " + countDownLatchRight.toString() + "]");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        merge(low, middle, high, verbose);
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    private void mergeSort(int low, int high, boolean verbose) {
        if (low >= high) { // there's nothing to 'sort'
            return;
        }

        int middle = Math.floorDiv((low + high), 2);
        if (verbose) {
            System.out.println("[MergeSort][Sequential] [" + low + "," + middle + "] [" + (middle+1) + "," + high + "]");
        }
        mergeSort(low, middle, verbose);
        mergeSort(middle + 1, high, verbose);
        merge(low, middle, high, verbose);
    }

    private void merge(int low, int middle, int high, boolean verbose) {
        // first, copy the to be sorted segment of the original array to the temp copy
        for (int i = low; i <= high; i++) {
            tempArray[i] = originalArray[i];
        }

        /*
         * imagine three array's
         *         (Original array)
         *        [<LOW>, , , , , <MIDDLE>, , , , , <HIGH>
         *  <low> [ LEFT ]   <middle>   [ RIGHT ] <high>
         *        [ TEMP ]
         * We are going to merge the LEFT and RIGHT into the TEMP.
         * We will advance through all three array's (LEFT and RIGHT being virtual, we'll be replacing the values in the original array via the temp copy)
         * The cursor from LEFT = i, RIGHT = j, TEMP = k
         * We continue until either i has caught up with middle, or j has caught with high.
         * This means that either virtual array has moved its cursor to the end of its (virtual) bound.
         */

        int i = low;
        int j = middle +1; // make sure we go TILL middle and FROM middle
        int k = low;

        // we will move from low -> middle via i, copying the lowest of i or j into k.
        // which ever was copied, that cursor progresses (either i or j) and k always progresses.
        while ((i <= middle) && (j <= high)) {
            if (tempArray[i] < tempArray[j]) {
                originalArray[k] = tempArray[i];
                i++;
            } else {
                originalArray[k] = tempArray[j];
                j++;
            }
            k++;
        }

        // now that either LEFT or RIGHT virtual array has reached its bounds,
        //   we can copy in the remainder of the array that did not yet reach it bound
        while(i <= middle) {
            originalArray[k] = tempArray[i];
            i++;
            k++;
        }

        while(j <= high) {
            originalArray[k] = tempArray[j];
            j++;
            k++;
        }
    }
}
