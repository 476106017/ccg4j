package org.example.system.util;

public class MyMath {
    public static void main(String[] args) {
        System.out.println(maxPrimeNum(0));
    }
    public static int maxPrimeNum(int n){
        for (int i = n; i >= 2; i--) {
            for (int j = 2; j < i; j++) {
                if (i % j == 0) {
                    //余数为零不是质数直接跳过
                    //质数 只可以 1和自身整除
                    break;
                }
                if (j == i - 1) {
                    return i;
                }
            }
        }
        return 2;
    }
}
