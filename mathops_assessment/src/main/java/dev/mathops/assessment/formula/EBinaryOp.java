package dev.mathops.assessment.formula;

/**
 * Unary operators.
 */
public enum EBinaryOp {

    /** Add. */
    ADD('+'),

    /** Subtract. */
    SUBTRACT('-'),

    /** Multiply. */
    MULTIPLY('*'),

    /** Divide. */
    DIVIDE('/'),

    /** Power. */
    POWER('^'),

    /** Remainder. */
    REMAINDER('%'),

    /** Less than. */
    LT('<'),

    /** Greater than. */
    GT('>'),

    /** Less than or equal to. */
    LE('\u2264'),

    /** Greater than or equal to. */
    GE('\u2265'),

    /** Equal to. */
    EQ('='),

    /** Approximately equal to. */
    APPROX('~'),

    /** Not equal to. */
    NE('\u2260'),

    /** Logical AND. */
    AND('&'),

    /** Logical OR. */
    OR('|');

    /** The character that symbolizes the operator. */
    public final char op;

    /**
     * Constructs a new {@code EBinaryOp}.
     *
     * @param theOp the character that symbolizes the operator
     */
    EBinaryOp(final char theOp) {

        this.op = theOp;
    }

    /**
     * Retrieves the {@code EBinaryOp} that has a specified operator character.
     *
     * @param theOp the operator character
     * @return the corresponding {@code EBinaryOp}; {@code null} if none matches
     */
    public static EBinaryOp forOp(final char theOp) {

        EBinaryOp result = null;

        for (final EBinaryOp test : values()) {
            if (test.op == theOp) {
                result = test;
                break;
            }
        }

        return result;
    }
}
