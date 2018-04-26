package com.github.joostvdg.buming.cli;

class Solution {
/*
Symbol	I	V	X	L	C	D	M
Value	1	5	10	50	100	500	1,000
*/

    public enum Numeral {
        I('I', 1, 0),
        V('V', 5, 1),
        X('X', 10, 2),
        L('L', 50, 3),
        C('C', 100, 4),
        D('D', 500, 5),
        M('M', 1000, 6);

        private Character letter;
        private int value;
        private int order;

        private Numeral(Character letter, int value, int order) {
            this.letter = letter;
            this.value = value;
            this.order = order;
        }

        public Character letter(){
            return this.letter;
        }
        public int value(){
            return this.value;
        }

        public int order(){
            return this.order;
        }
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        System.out.println("Start");
        long startTime = System.nanoTime();
        doSomething(solution, "I", 1);
        doSomething(solution, "II", 2);
        doSomething(solution, "III", 3);
        doSomething(solution, "IV", 4);
        doSomething(solution, "V", 5);
        doSomething(solution, "X", 10);
        doSomething(solution, "MDCCLXXVI", 1776);
        doSomething(solution, "MCMLIV", 1954);
        doSomething(solution, "MCMXC", 1990);
        doSomething(solution, "MMXIV", 2014);
        long endTime = System.nanoTime();
        long runningTime = endTime - startTime;
        System.out.println("Finished in " + runningTime  + " nano's");
    }

    public static void doSomething(Solution solution, String input, int expectedOutput) {
        int result = solution.romanToInt(input);
        System.out.println("[Input: " + input + ", expected: " + expectedOutput + ", actual: " + result + "]");
    }

    public int romanToInt(String s) {
        int result = 0;
        String upperCased = s.toUpperCase();
        char[] chars = upperCased.toCharArray();
        for (int i =0; i < chars.length; i++) {
            char c =chars[i];
            Numeral numeral =  Numeral.valueOf("" + c);
            int value = numeral.value();
            Numeral nextNumeral = null;
            if (i +1 < chars.length) {
                char d =chars[i+1];
                nextNumeral = Numeral.valueOf("" + d);
            }
            if (nextNumeral != null && nextNumeral.order > numeral.order) {
                result = result - value;
            } else {
                result = result + value;
            }
        }

        return result;
    }

    private Numeral charToNumeral(char c) {
        for(Numeral numeral : Numeral.values()) {
            if (numeral.letter().equals(c)) {
                return numeral;
            }
        }
        throw new IllegalArgumentException();
    }
}
