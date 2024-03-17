package dev.mathops.assessment.expression.editmodel;

/**
 * Possible justifications for entries in a matrix or vector.
 */
public enum EEntryJustification {

    /** Left. */
    LEFT,

    /** Right. */
    RIGHT,

    /** Decimal points or phantom trailing radixes are aligned. */
    DECIMAL,

    /** Center. */
    CENTER;
}
