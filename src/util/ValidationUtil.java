package util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);


    private static final String PHONE_REGEX = "^[0-9]{10}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);


    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }


    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        String digits = phone.replaceAll("[^0-9]", "");
        return PHONE_PATTERN.matcher(digits).matches();
    }


    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public static boolean isPositiveInteger(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            int value = Integer.parseInt(str);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public static boolean isValidLength(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }
        int length = str.trim().length();
        return length >= minLength && length <= maxLength;
    }


    public static boolean isAlphaSpace(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        return str.matches("[a-zA-Z\\s]+");
    }


    public static boolean hasLettersAndNumbers(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasNumber = false;

        for (char c : str.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasNumber = true;
            if (hasLetter && hasNumber) return true;
        }

        return false;
    }


    public static boolean isValidDate(String date) {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }

        try {
            String[] parts = date.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);


            if (year < 1900 || year > 2100) return false;
            if (month < 1 || month > 12) return false;
            if (day < 1 || day > 31) return false;

            if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
                return false;
            }


            if (month == 2 && day > 29) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
