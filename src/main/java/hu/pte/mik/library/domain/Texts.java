package hu.pte.mik.library.domain;

/** Small validation helper shared by the domain classes. */
final class Texts {

    private Texts() {
    }

    static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be empty.");
        }
        if (value.contains("|")) {
            throw new IllegalArgumentException(fieldName + " must not contain the '|' character.");
        }
        return value.trim();
    }
}
