package dev.mathops.db.generalized;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.generalized.constraint.AbstractFieldConstraint;

import java.util.Arrays;

/**
 * An immutable container for updated values for an update operation.  This class distinguishes from "do not update
 * a given field" and "update the field to NULL".
 */
public final class UpdatedValues {

    /** The table to which this record belongs. */
    private final Table table;

    /**
     * Field values - null indicates the field is not to be updated; a {@code NullValue} object indicates the field is
     * to be updated to NULL.
     */
    private final Object[] fieldValues;

    /**
     * Constructs a new {@code UpdatedValues}.  The constructor ensures that the constructed object is valid with
     * respect to the fields define din the table.
     *
     * @param theTable       the table to which this record belongs
     * @param theFieldValues the field values, indexed as in {@code theTable}, where {@code null} indicates the field
     *                       is not to be updated; a {@code NullValue} object indicates the field is to be updated to
     *                       NULL, and where trailing null values (non-updated fields) may be omitted
     * @throws IllegalArgumentException if the table name is null, there are more field values provided than the
     * table defines, a field value does not satisfy a field role or constraint, or there were no field values provided
     */
    public UpdatedValues(final Table theTable, final Object... theFieldValues) throws IllegalArgumentException {

        if (theTable == null) {
            throw new IllegalArgumentException("Table may not be null");
        }
        if (theFieldValues == null || theFieldValues.length == 0) {
            throw new IllegalArgumentException("At least one updated field value must be provided.");
        }

        final int numDefined = theTable.getNumFields();
        final int numProvided = theFieldValues.length;
        if (numProvided > numDefined) {
            throw new IllegalArgumentException("More field values provided than fields defined in the table.");
        }

        this.table = theTable;

        this.fieldValues = new Object[numDefined];
        System.arraycopy(theFieldValues, 0, this.fieldValues, 0, numProvided);

        // Verify that field values are of proper type, compatible with roles, and satisfy all constraints
        int numberToUpdate = 0;
        for (int i = 0; i < numDefined; ++i) {
            final Object value = this.fieldValues[i];
            if (value == null) {
                continue;
            }

            final Field field = theTable.getField(i);
            if (value instanceof NullValue) {
                if (field.getRole().nullable) {
                    throw new IllegalArgumentException("May not update a non-nullable field to a null value.");
                }
            } else if (field.isValidType(value)) {
                // Check that the target value satisfies all constraints
                final int numConstraints = field.getNumConstraints();
                for (int j = 0; j < numConstraints; ++j) {
                    final AbstractFieldConstraint<?> constraint = field.getConstraint(j);
                    if (!constraint.isValidValue(value)) {
                        throw new IllegalArgumentException("Value of '" + field.getName()
                                + "' field does not satisfy field constraints");
                    }
                }
            } else {
                throw new IllegalArgumentException("A field of type " + field.getType().cls.getSimpleName()
                        + " may not be updated to a value of type " + value.getClass().getSimpleName());
            }

            ++numberToUpdate;
        }

        if (numberToUpdate == 0) {
            // Caller supplied nothing but null values in the input list.
            throw new IllegalArgumentException("At least one field must be updated.");
        }
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String fieldValuesStr = Arrays.toString(this.fieldValues);

        return SimpleBuilder.concat("UpdatedValues{table=", this.table, ", fieldValues=", fieldValuesStr, "}");
    }
}
