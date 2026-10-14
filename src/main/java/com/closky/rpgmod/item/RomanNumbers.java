package com.closky.rpgmod.item;

/** Крохотный помощник: 1 -> "I", 2 -> "II" ... (для красивых подсказок). */
public final class RomanNumbers {
    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private RomanNumbers() {}

    public static String of(int value) {
        if (value > 0 && value < ROMAN.length) return ROMAN[value];
        return String.valueOf(value);
    }
}
