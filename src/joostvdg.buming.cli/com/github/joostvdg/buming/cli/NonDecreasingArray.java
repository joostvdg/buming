package com.github.joostvdg.buming.cli;

/**
 * Example 1:
 Input: [4,2,3]
 Output: True
 Explanation: You could modify the first 4 to 1 to get a non-decreasing array.

 Example 2:
 Input: [4,2,1]
 Output: False
 Explanation: You can't get a non-decreasing array by modify at most one element.
 */

public class NonDecreasingArray {

    public static void main(String[] args) {
        NonDecreasingArray nonDecreasingArray = new NonDecreasingArray();
        int[] array1 = new int[]{4,2,1};
        int[] array2 = new int[]{4,2,3};
        int[] array3 = new int[]{3,4,2,3};
        int[] array4 = new int[]{-1,4,2,3};

        checkArray(array1, nonDecreasingArray, false);
        checkArray(array2, nonDecreasingArray, true);
        checkArray(array3, nonDecreasingArray, false);
        checkArray(array4, nonDecreasingArray, true);
    }

    private static void checkArray(int[] nums, NonDecreasingArray nonDecreasingArray, boolean expected) {

        boolean result = nonDecreasingArray.checkPossibility(nums);
        System.out.println("[Input: "+ printArray(nums) + ", Expected: " + expected + ", Actual: " + result+ "]");
    }

    private static String printArray(int[] nums) {
        StringBuilder stringBuilder = new StringBuilder("[");
        for(int i =0; i < nums.length; i++) {
            if (i > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append("" + nums[i]);
        }
        stringBuilder.append("]");
        return  stringBuilder.toString();
    }

    public boolean checkPossibility(int[] nums) {
        int count = 0;
        boolean previousWasToBeChanged = false;
        for(int i = 0; i < nums.length -1; i++) {
            int high = nums[i];
            for(int j = i; j < nums.length -1; j++) {
                if (nums[j] <= nums[j + 1]) {
                    //previousWasToBeChanged = false;
                } else if (nums[j] > high && i != j){
                    if (!previousWasToBeChanged) {
                        count++;
                        previousWasToBeChanged = true;
                        break;
                    }
                } else {
                    if (!previousWasToBeChanged) {
                        count++;
                        previousWasToBeChanged= true;
                        break;
                    }
                }
//                high = nums[j];
            }
        }

        return count < 2;
    }
}
