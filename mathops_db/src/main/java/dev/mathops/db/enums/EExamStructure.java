package dev.mathops.db.enums;

/**
 * Types of official registration information that can be received.
 */
public enum EExamStructure {

    /** Unit and Final Exams. */
    UNIT_FINAL("UF"),

    /** Unit Exams Only. */
    UNIT_ONLY("UO"),

    /** Final Exams Only. */
    FINAL_ONLY("FO");

    /** The status code. */
    private final String code;

    /**
     * Constructs a new {@code EExamStructure}.
     *
     * @param theCode the status code
     */
    EExamStructure(final String theCode) {

        this.code = theCode;
    }

//    /**
//     * Gets the {@code EExamStructure} that has a specified status code.
//     *
//     * @param theCode the status code
//     * @return the matching {@code EExamStructure}; {@code null} if none match
//     */
//     public static EExamStructure forCode(final String theCode)
//
//     EExamStructure result = null;
//
//     for (EExamStructure test : EExamStructure.values()) {
//     if (test.code.equals(theCode)) {
//     result = test;
//     break;
//     }
//     }
//
//     return result;
//     }

    /**
     * Returns the string representation of the object.
     *
     * @return the string representation (the code)
     */
    @Override
    public String toString() {

        return this.code;
    }
}
