package dev.mathops.db.generalized.constraint;

/**
 * Types of allowed values for floating point fields.
 */
public enum EFloatingPointAllow {

    /** All values allowed, including NaN and Infinities. */
    ALL_VALUES,

    /** All values except NaN are allowed, including Infinities. */
    ALL_BUT_NAN,

    /** All values except infinities are allowed, including NaN. */
    ALL_BUT_INFINITIES,

    /** Only finite values are allowed. */
    FINITE_ONLY,
}
