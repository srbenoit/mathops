package dev.mathops.app.ops.snapin.messaging;

/**
 * Touch points during a semester when automated messages can be sent to a student based on status.
 */
public enum EMilestone {

    /** Welcome message. */
    WELCOME("WE"),

    /** Satisfy prerequisites. */
    PREREQ("PR"),

    /** Start the course. */
    START("ST"),

    /** User's Exam. */
    USERS("US"),

    /** Skills Review Exam. */
    SR("Sr"),

    /** Homework 1.1. */
    HW11("11"),

    /** Homework 1.2. */
    HW12("12"),

    /** Homework 1.3. */
    HW13("13"),

    /** Homework 1.4. */
    HW14("14"),

    /** Homework 1.5. */
    HW15("15"),

    /** Unit 1 Review Exam. */
    RE1("R1"),

    /** Unit 1 Exam. */
    UE1("U1"),

    /** Homework 2.1. */
    HW21("21"),

    /** Homework 2.2. */
    HW22("22"),

    /** Homework 2.3. */
    HW23("23"),

    /** Homework 2.4. */
    HW24("24"),

    /** Homework 2.5. */
    HW25("25"),

    /** Unit 2 Review Exam. */
    RE2("R2"),

    /** Unit 2 Exam. */
    UE2("U2"),

    /** Homework 3.1. */
    HW31("31"),

    /** Homework 3.2. */
    HW32("32"),

    /** Homework 3.3. */
    HW33("33"),

    /** Homework 3.4. */
    HW34("34"),

    /** Homework 3.5. */
    HW35("35"),

    /** Unit 3 Review Exam. */
    RE3("R3"),

    /** Unit 3 Exam. */
    UE3("U3"),

    /** Homework 4.1. */
    HW41("41"),

    /** Homework 4.2. */
    HW42("42"),

    /** Homework 4.3. */
    HW43("43"),

    /** Homework 4.4. */
    HW44("44"),

    /** Homework 4.5. */
    HW45("45"),

    /** Unit 4 Review Exam. */
    RE4("R4"),

    /** Unit 4 Exam. */
    UE4("U4"),

    /** Final Exam. */
    FIN("FE"),

    /** Final Exam Last Try. */
    F1("F1"),

    /** Reach minimum passing score. */
    PASS("PA"),

    /** Reach maximum possible grade. */
    MAX("MX"),

    /** Blocked. */
    BLOK("BL");

    /** The code. */
    public final String code;

    /**
     * Constructs a new {@code EMilestone}.
     *
     * @param theCode the code
     */
    EMilestone(final String theCode) {

        this.code = theCode;
    }

    /**
     * Gets the {@code EMilestone} that has a specified code.
     *
     * @param theCode the status code
     * @return the matching {@code EMilestone}; {@code null} if none match
     */
    public static EMilestone forCode(final String theCode) {

        EMilestone result = null;

        for (final EMilestone test : values()) {
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

        return this.code;
    }
}
