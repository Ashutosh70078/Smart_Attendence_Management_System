
public class ValidationHelper {

    public static boolean isPositiveInteger(String text) {
        if (text == null) {
            return false;
        }
        try {
            int roll = Integer.parseInt(text.trim());
            return roll > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidStudentName(String text) {
        if (!isNonEmpty(text)) return false;
        String trimmed = text.trim();
        if (trimmed.length() < 2) return false;
        boolean hasLetter = false;
        for (char c : trimmed.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
                break;
            }
        }
        return hasLetter;
    }

    public static boolean isNonNegativeInteger(String text) {
        if (text == null) return false;
        try {
            int val = Integer.parseInt(text.trim());
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidAttendancePair(int attended, int total) {
        return total >= 0 && attended >= 0 && attended <= total;
    }

    public static boolean checkPassword(String input, String expected) {
        if (input == null || expected == null) {
            return false;
        }
        return input.equals(expected);
    }
}

