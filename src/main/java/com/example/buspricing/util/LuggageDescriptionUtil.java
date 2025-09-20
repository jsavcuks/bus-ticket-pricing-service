package com.example.buspricing.util;

public class LuggageDescriptionUtil {

    public static String describe(int count) {
        return switch (count) {
            case 1 -> "One bag";
            case 2 -> "Two bags";
            default -> count + " bags";
        };
    }
}
