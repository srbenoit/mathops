package dev.mathops.db.enums;

/**
 * Types of incidents that can trigger a discipline record on a student's account.
 */
public enum EDisciplineIncidentType {

    /** Online and available. */
    STD_CRIB_SHEET("01", "Standard crib sheet"),

    /** Online and available. */
    NOTES_ON_PENCIL("02", "Crib notes on pencil"),

    /** Online and available. */
    NOTES_ON_ERASER("03", "Crib notes on eraser"),

    /** Online and available. */
    NOTES_ON_HAND("04", "Crob notes on hand"),

    /** Online and available. */
    NONSTD_CRIB_SHEET("05", "Nonstandard crib sheet"),

    /** Online and available. */
    EXAM_SWITCH("06", "Exam switch, not with another person"),

    /** Online and available. */
    EXAM_SWITCH_W_PERSON("07", "Exam switch with another person"),

    /** Online and available. */
    LEFT_WITH_ANS_SHEET("08", "Left center with answer sheet"),

    /** Online and available. */
    TRASH_ANS_SHEET("09", "Trashed answer sheet"),

    /** Online and available. */
    IMPERSONATION("10", "Impersonation"),

    /** Online and available. */
    MEMORIZATION("11", "Memorization"),

    /** Online and available. */
    OTHER("12", "Other");

    /** The status code. */
    public final String code;

    /** The status label. */
    public final String label;

    /**
     * Constructs a new {@code EDisciplineIncidentType}.
     *
     * @param theCode  the status code
     * @param theLabel the status label
     */
    EDisciplineIncidentType(final String theCode, final String theLabel) {

        this.code = theCode;
        this.label = theLabel;
    }

    /**
     * Gets the {@code EDisciplineIncidentType} that has a specified status code.
     *
     * @param theCode the status code
     * @return the matching {@code EDisciplineIncidentType}; {@code null} if none match
     */
    public static EDisciplineIncidentType forCode(final String theCode) {

        EDisciplineIncidentType result = null;

        for (final EDisciplineIncidentType test : values()) {
            if (test.code.equals(theCode)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Returns the string representation of the object.
     *
     * @return the string representation (the code)
     */
    @Override
    public String toString() {

        return this.label;
    }
}
