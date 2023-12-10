package dev.mathops.db.generalized.criteria;

import dev.mathops.db.generalized.EFieldType;
import dev.mathops.db.generalized.Field;

/**
 *  A criterion that can be used to match a Double field.
 */
public class DoubleFieldCriterion extends AbstractFieldCriterion {

    /** The match type. */
    final ENumericMatchType matchType;

    /** The doubles to use in the match process. */
    final Double[] doubles;

    /**
     * Constructs a new {@code DoubleFieldCriterion}.
     *
     * @param theField the field
     * @param theMatchType the type of match to perform
     * @param theDoubles the doubles to match using the match type
     */
    public DoubleFieldCriterion(final Field theField, final ENumericMatchType theMatchType,
                                final Double... theDoubles) {

        super(theField);

        if (theField.getType() != EFieldType.DOUBLE) {
            throw new IllegalArgumentException("A Double field criterion must be used with a Double field");
        }
        if (theMatchType == null) {
            throw new IllegalArgumentException("The match type may not be null");
        }

        if (theDoubles != null) {
            for (final Double test : theDoubles) {
                if (test == null) {
                    throw new IllegalArgumentException("Match Double list may not contain null values");
                }
            }
        }

        if (theMatchType != ENumericMatchType.IS_NULL && theMatchType != ENumericMatchType.IS_NOT_NULL
            && (theDoubles == null || theDoubles.length == 0)) {
            throw new IllegalArgumentException("At least one match double must be provided.");
        }

        this.matchType = theMatchType;
        this.doubles = theDoubles == null || theDoubles.length == 0 ? new Double[0] : theDoubles.clone();
    }
}
