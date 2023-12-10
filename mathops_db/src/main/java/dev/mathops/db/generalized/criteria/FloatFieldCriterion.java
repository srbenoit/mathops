package dev.mathops.db.generalized.criteria;

import dev.mathops.db.generalized.EFieldType;
import dev.mathops.db.generalized.Field;

/**
 *  A criterion that can be used to match a Float field.
 */
public class FloatFieldCriterion extends AbstractFieldCriterion {

    /** The match type. */
    final ENumericMatchType matchType;

    /** The floats to use in the match process. */
    final Float[] floats;

    /**
     * Constructs a new {@code FloatFieldCriterion}.
     *
     * @param theField the field
     * @param theMatchType the type of match to perform
     * @param theFloats the floats to match using the match type
     */
    public FloatFieldCriterion(final Field theField, final ENumericMatchType theMatchType, final Float... theFloats) {

        super(theField);

        if (theField.getType() != EFieldType.FLOAT) {
            throw new IllegalArgumentException("A Float field criterion must be used with a Float field");
        }
        if (theMatchType == null) {
            throw new IllegalArgumentException("The match type may not be null");
        }

        if (theFloats != null) {
            for (final Float test : theFloats) {
                if (test == null) {
                    throw new IllegalArgumentException("Match Float list may not contain null values");
                }
            }
        }

        if (theMatchType != ENumericMatchType.IS_NULL && theMatchType != ENumericMatchType.IS_NOT_NULL
            && (theFloats == null || theFloats.length == 0)) {
            throw new IllegalArgumentException("At least one match float must be provided.");
        }

        this.matchType = theMatchType;
        this.floats = theFloats == null || theFloats.length == 0 ? new Float[0] : theFloats.clone();
    }
}
