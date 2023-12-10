package dev.mathops.db.generalized.criteria;

import dev.mathops.db.generalized.EFieldType;
import dev.mathops.db.generalized.Field;

/**
 *  A criterion that can be used to match a Long field.
 */
public class LongFieldCriterion extends AbstractFieldCriterion {

    /** The match type. */
    final ENumericMatchType matchType;

    /** The longs to use in the match process. */
    final Long[] longs;

    /**
     * Constructs a new {@code LongFieldCriterion}.
     *
     * @param theField the field
     * @param theMatchType the type of match to perform
     * @param theLongs the longs to match using the match type
     */
    public LongFieldCriterion(final Field theField, final ENumericMatchType theMatchType, final Long... theLongs) {

        super(theField);

        if (theField.getType() != EFieldType.LONG) {
            throw new IllegalArgumentException("A Long field criterion must be used with a Long field");
        }
        if (theMatchType == null) {
            throw new IllegalArgumentException("The match type may not be null");
        }

        if (theLongs != null) {
            for (final Long test : theLongs) {
                if (test == null) {
                    throw new IllegalArgumentException("Match Long list may not contain null values");
                }
            }
        }

        if (theMatchType != ENumericMatchType.IS_NULL && theMatchType != ENumericMatchType.IS_NOT_NULL
            && (theLongs == null || theLongs.length == 0)) {
            throw new IllegalArgumentException("At least one match long must be provided.");
        }

        this.matchType = theMatchType;
        this.longs = theLongs == null || theLongs.length == 0 ? new Long[0] : theLongs.clone();
    }
}
