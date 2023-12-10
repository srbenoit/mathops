package dev.mathops.db.generalized.criteria;

import dev.mathops.db.generalized.EFieldType;
import dev.mathops.db.generalized.Field;

/**
 *  A criterion that can be used to match a Byte field.
 */
public class ByteFieldCriterion extends AbstractFieldCriterion {

    /** The match type. */
    final ENumericMatchType matchType;

    /** The bytes to use in the match process. */
    final Byte[] bytes;

    /**
     * Constructs a new {@code ByteFieldCriterion}.
     *
     * @param theField the field
     * @param theMatchType the type of match to perform
     * @param theBytes the bytes to match using the match type
     */
    public ByteFieldCriterion(final Field theField, final ENumericMatchType theMatchType, final Byte... theBytes) {

        super(theField);

        if (theField.getType() != EFieldType.BYTE) {
            throw new IllegalArgumentException("A Byte field criterion must be used with a Byte field");
        }
        if (theMatchType == null) {
            throw new IllegalArgumentException("The match type may not be null");
        }

        if (theBytes != null) {
            for (final Byte test : theBytes) {
                if (test == null) {
                    throw new IllegalArgumentException("Match Byte list may not contain null values");
                }
            }
        }

        if (theMatchType != ENumericMatchType.IS_NULL && theMatchType != ENumericMatchType.IS_NOT_NULL
            && (theBytes == null || theBytes.length == 0)) {
            throw new IllegalArgumentException("At least one match byte must be provided.");
        }

        this.matchType = theMatchType;
        this.bytes = theBytes == null || theBytes.length == 0 ? new Byte[0] : theBytes.clone();
    }
}
