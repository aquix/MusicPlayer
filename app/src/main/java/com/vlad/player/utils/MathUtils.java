package com.vlad.player.utils;


public class MathUtils {

    public static int getPositiveModule(int number, int module) {
        int remainder = number % module;
        if (remainder < 0) {
            remainder += module;
        }
        return remainder;
    }

}
