package com.example.coday.util;

public class PostalCodeUtil {

    public static String formatPostalCode(String input) {
        if (input == null) return null;
        String digits = input.replaceAll("\\s+", "");
        if (!digits.matches("\\d{5}")) {
            throw new IllegalArgumentException("Postnummer måste innehålla exakt 5 siffror.");
        }
        return digits.substring(0, 3) + " " + digits.substring(3);
    }
}
