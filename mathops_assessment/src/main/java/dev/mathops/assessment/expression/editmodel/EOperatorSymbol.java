package dev.mathops.assessment.expression.editmodel;

/**
 * Operator symbols.
 */
public enum EOperatorSymbol {

    /** Plus. */
    PLUS("+"),

    /** Minus. */
    MINUS("\u2212"),

    /** Times. */
    TIMES("\u00D7"),

    /** Divided by. */
    DIVIDED_BY("\u00F7"),

    /** Remainder. */
    REMAINDER("%"),

    /** Or. */
    OR("|"),

    /** And. */
    AND("&"),

    /** Not. */
    NOT("!"),

    /** Equals. */
    EQUALS("="),

    /** Less Than. */
    LESS_THAN("<"),

    /** Greater Than. */
    GREATER_THAN(">");

    /** The string that represents the operator. */
    public final String str;

    /**
     * Constructs a new {@code EOperatorSymbol}.
     *
     * @param theStr the character that represents the operator
     */
    EOperatorSymbol(final String theStr) {

        this.str = theStr;
    }
}
