package dev.mathops.db.generalized.criteria;

import dev.mathops.db.generalized.EFieldType;
import dev.mathops.db.generalized.Field;

/**
 *  A criterion that can be used to match an Integer field.
 */
public class IntegerFieldCriterion extends AbstractFieldCriterion {

    /** The match type. */
    final ENumericMatchType matchType;

    /** The integers to use in the match process. */
    final Integer[] integers;

    /**
     * Constructs a new {@code IntegerFieldCriterion}.
     *
     * @param theField     the field
     * @param theMatchType the type of match to perform
     * @param theIntegers  the integers to match using the match type
     */
    public IntegerFieldCriterion(final Field theField, final ENumericMatchType theMatchType,
                                 final Integer... theIntegers) {

        super(theField);

        if (theField.getType() != EFieldType.INTEGER) {
            throw new IllegalArgumentException("An Integer field criterion must be used with a Byte field");
        }
        if (theMatchType == null) {
            throw new IllegalArgumentException("The match type may not be null");
        }

        if (theIntegers != null) {
            for (final Integer test : theIntegers) {
                if (test == null) {
                    throw new IllegalArgumentException("Match Integer list may not contain null values");
                }
            }
        }

        if (theMatchType != ENumericMatchType.IS_NULL && theMatchType != ENumericMatchType.IS_NOT_NULL
            && (theIntegers == null || theIntegers.length == 0)) {
            throw new IllegalArgumentException("At least one match integer must be provided.");
        }

        this.matchType = theMatchType;
        this.integers = theIntegers == null || theIntegers.length == 0 ? new Integer[0] : theIntegers.clone();
    }
}
