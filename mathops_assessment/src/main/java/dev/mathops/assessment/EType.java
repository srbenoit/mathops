package dev.mathops.assessment;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Types for values returned by formulas or stored in variables.
 */
public enum EType {

    /** Boolean. */
    BOOLEAN("boolean"),

    /** Integer. */
    INTEGER("integer"),

    /** Real. */
    REAL("real"),

    /** Vector of integer values. */
    INTEGER_VECTOR("int-vector"),

    /** Vector of real values. */
    REAL_VECTOR("vector"),

    /** String (inputs only). */
    STRING("string"),

    /** Span. */
    SPAN("span"),

    /** Error. */
    ERROR("error");

    /** The value. */
    public final String label;

    /**
     * Constructs a new {@code EType}.
     *
     * @param theLabel the label
     */
    EType(final String theLabel) {

        this.label = theLabel;
    }

    /**
     * Gets the string representation of the type.
     *
     * @return the label
     */
    @Override
    public String toString() {

        return this.label;
    }

    /**
     * Gets the {@code EType} that has a specified label.
     *
     * @param theLabel the label
     * @return the corresponding {@code EType} ; {@code null} if none
     */
    public static EType forLabel(final CharSequence theLabel) {

        EType result = null;

        for (final EType value : values()) {
            if (value.label.contentEquals(theLabel)) {
                result = value;
                break;
            }
        }

        return result;
    }

    /**
     * Tests whether a given type is "compatible" with a required type. A type is compatible with the same type, plus
     * INTEGER as a given type is compatible with REAL as a required type, and INTEGER_VECTOR as a given type is
     * compatible with REAL_VECTOR as a required type.
     *
     * @param requiredType the required type
     * @param givenType    the given type
     * @return true if the given type is compatible with the required type; false if not
     */
    public static boolean isCompatible(final EType requiredType, final EType givenType) {

        boolean compatible = false;

        if (requiredType == null) {
            // Null indicates any type can be accepted
            compatible = true;
        } else if (givenType != null) {
            if (requiredType == givenType) {
                compatible = true;
            } else if (requiredType == REAL) {
                compatible = givenType == INTEGER;
            } else if (requiredType == REAL_VECTOR) {
                compatible = givenType == INTEGER_VECTOR;
            }
        }

        return compatible;
    }

    /**
     * Given a set of allowed types, and a set of potential types, generates the set of all the potential types that are
     * allowed.
     *
     * @param allowed   all allowed types
     * @param potential the set of potential types
     * @return the filtered set of potential types that are allowed
     */
    public static EnumSet<EType> filter(final Collection<EType> allowed,
                                        final Iterable<EType> potential) {

        final EnumSet<EType> result = EnumSet.noneOf(EType.class);

        for (final EType test : potential) {
            if (allowed.contains(test)) {
                result.add(test);
            }
        }

        return result;
    }
}
