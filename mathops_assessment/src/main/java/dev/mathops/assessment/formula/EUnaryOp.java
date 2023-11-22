package dev.mathops.assessment.formula;

/**
 * Unary operators.
 */
public enum EUnaryOp {

    /** Plus. */
    PLUS('+'),

    /** Minus. */
    MINUS('-');

    /** The character that symbolizes the operator. */
    public final char op;

    /**
     * Constructs a new {@code EUnaryOp}.
     *
     * @param theOp the character that symbolizes the operator
     */
    EUnaryOp(final char theOp) {

        this.op = theOp;
    }

    /**
     * Retrieves the {@code EUnaryOp} that has a specified operator character.
     *
     * @param theOp the operator character
     * @return the corresponding {@code EUnaryOp}; {@code null} if none matches
     */
    public static EUnaryOp forOp(final char theOp) {

        EUnaryOp result = null;

        for (final EUnaryOp test : values()) {
            if (test.op == theOp) {
                result = test;
                break;
            }
        }

        return result;
    }
}
