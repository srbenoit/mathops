package dev.mathops.db.generalized.criteria;

import dev.mathops.db.generalized.EFieldType;
import dev.mathops.db.generalized.Field;

/**
 *  A criterion that can be used to match a String field.
 */
public class StringFieldCriterion extends AbstractFieldCriterion {

    /** The match type. */
    final EStringMatchType matchType;

    /** The strings to use in the match process. */
    final String[] strings;

    /**
     * Constructs a new {@code StringFieldCriterion}.
     *
     * @param theField the field
     * @param theMatchType the type of match to perform
     * @param theStrings the strings to match using the match type
     */
    public StringFieldCriterion(final Field theField, final EStringMatchType theMatchType, final String... theStrings) {

        super(theField);

        if (theField.getType() != EFieldType.STRING) {
            throw new IllegalArgumentException("A String field criterion must be used with a String field");
        }
        if (theMatchType == null) {
            throw new IllegalArgumentException("The match type may not be null");
        }

        if (theStrings != null) {
            for (final String test : theStrings) {
                if (test == null) {
                    throw new IllegalArgumentException("Match string list may not contain null values");
                }
            }
        }

        if (theMatchType != EStringMatchType.IS_NULL && theMatchType != EStringMatchType.IS_NOT_NULL
            && (theStrings == null || theStrings.length == 0)) {
            throw new IllegalArgumentException("At least one match string must be provided.");
        }

        this.matchType = theMatchType;
        this.strings = theStrings == null || theStrings.length == 0 ? new String[0] : theStrings.clone();
    }
}
