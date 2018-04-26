package com.github.joostvdg.buming.sorting;

import com.github.joostvdg.buming.api.SortingExample;
import com.github.joostvdg.buming.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

// 1000000000
// Classic For:             352642789
// Lambda:                  353963458
// Joost's Parallel(4):     162060399

// 2000000000
// Classic For:             699754392
// Lambda:                  706284182
// Joost's Parallel(6):     328334299

public class SumProblem implements SortingExample {

    private static final int AMOUNT_OF_VALUES_SMALL = 1450;
    private final int[] values;

    private static final int AMOUNT_OF_VALUES_LARGE = 2000000000;
    private final int[] manyValues;

    public static void main(String[] args) {
        SumProblem sumProblem = new SumProblem();
        sumProblem.sort(null);
    }

    public SumProblem() {
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
        return "Sum";
    }

    @Override
    public void sort(Logger logger) {
        System.out.println("[Sum][Sequential-Classic][Small] Start");
        long startTime = System.nanoTime();
        long result = sequentialSumationClassic(values);
        long endTime = System.nanoTime();
        long runningTime = endTime - startTime;
        System.out.println("[Sum][Sequential-Classic][Small] Finished in " + runningTime  + " nano's, result: " +result);

        System.out.println("[Sum][Sequential-Classic][Large] Start");
        startTime = System.nanoTime();
        result = sequentialSumationClassic(manyValues);
        endTime = System.nanoTime();
        runningTime = endTime - startTime;
        System.out.println("[Sum][Sequential-Classic][Large] Finished in " + runningTime  + " nano's, result: " +result);

        System.out.println("[Sum][Sequential-Lambda][Small] Start");
        startTime = System.nanoTime();
        result = sequentialSumationLambda(values);
        endTime = System.nanoTime();
        runningTime = endTime - startTime;
        System.out.println("[Sum][Sequential-Lambda][Small] Finished in " + runningTime  + " nano's, result: " +result);

        System.out.println("[Sum][Sequential-Lambda][Large] Start");
        startTime = System.nanoTime();
        result = sequentialSumationLambda(manyValues);
        endTime = System.nanoTime();
        runningTime = endTime - startTime;
        System.out.println("[Sum][Sequential-Lambda[Large] Finished in " + runningTime  + " nano's, result: " +result);

        System.out.println("[Sum][Parallel][Small] Start");
        startTime = System.nanoTime();
        result = parallelSumation(values);
        endTime = System.nanoTime();
        runningTime = endTime - startTime;
        System.out.println("[Sum][Parallel[Small] Finished in " + runningTime  + " nano's, result: " +result);

        System.out.println("[Sum][Parallel][Large] Start");
        startTime = System.nanoTime();
        result = parallelSumation(manyValues);
        endTime = System.nanoTime();
        runningTime = endTime - startTime;
        System.out.println("[Sum][Parallel[Large] Finished in " + runningTime  + " nano's, result: " +result);
    }

    private long sequentialSumationClassic(int[] values){
        long result = 0L;
        for(int i=0; i < values.length; i++) {
            result += values[i];
        }
        return result;
    }

    private long sequentialSumationLambda(int[] values){
        return Arrays.stream(values).sum();
    }

    private long parallelSumation(int[] values) {
        int threadCount = 1;
        if (values.length > 500 && values.length < 100000) {
            threadCount = 2;
        } else if (values.length > 100000 && values.length < 100000000) {
            threadCount = 4;
        } else {
            threadCount = 6;
        }
        System.out.println("ThreadCount::"+threadCount);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        int naieveSplit = values.length / threadCount;
        int index = 0;
        int[] indexes = new int[threadCount+1];
        for(int i = 0; i < threadCount; i++) {
            indexes[i] = index;
            index += naieveSplit + 1;
        }
        indexes[threadCount] = values.length;

        List<Future<Long>> calculators = new ArrayList<>();
        for(int i = 0; i < threadCount; i++) {
            int copyFromIndex= indexes[i];
            int copyToIndex = indexes[i+1];

            System.out.println("StartIndex::"+copyFromIndex + ", EndIndex::"+copyToIndex+ "(" + i + ")");
            Callable<Long> task = () -> {
                long result = 0L;
                for(int j=copyFromIndex; j < copyToIndex; j++) {
                    result += values[j];
                }
                return result;
            };
            Future<Long> future = executorService.submit(task);
            calculators.add(future);
        }

        long result = 0L;
        for(Future<Long> future : calculators) {
            try {
                result += future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdownNow();


        return result;
    }

}
