package dev.mathops.db.generalized;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.generalized.constraint.AbstractFieldConstraint;

import java.util.Arrays;

/**
 * An immutable generalized record object, which references a "Table" object (with field definitions), and which stores
 * an array of field values.
 */
public final class Record {

    /** The table to which this record belongs. */
    private final Table table;

    /** Field values, where null values indicate fields whose table record was NULL. */
    private final Object[] fieldValues;

    /**
     * Constructs a new {@code Record}.  The constructor ensures that the constructed object is valid with respect to
     * the fields define din the table.
     *
     * @param theTable the table to which this record belongs
     * @param theFieldValues the field values, indexed as in {@code theTable}, where trailing null values may be
     *                       omitted (if no values are permitted, the result is a record with all null field values)
     * @throws IllegalArgumentException if the table name is null, there are more field values provided than the
     * table defines, or a field value does not satisfy a field role or constraint
     */
    public Record(final Table theTable, final Object... theFieldValues) throws IllegalArgumentException {

        if (theTable == null) {
            throw new IllegalArgumentException("Table may not be null");
        }
        final int numDefined = theTable.getNumFields();

        this.table = theTable;

        this.fieldValues = new Object[numDefined];
        if (theFieldValues != null) {
            final int numProvided = theFieldValues.length;
            if (numProvided > numDefined) {
                throw new IllegalArgumentException("More field values provided than fields defined in the table.");
            }
            System.arraycopy(theFieldValues, 0, this.fieldValues, 0, numProvided);
        }

        // Verify that field values are compatible with roles and satisfy all constraints
        for (int i = 0; i < numDefined; ++i) {
            final Field field = theTable.getField(i);
            final Object value = this.fieldValues[i];

            if (value == null) {
                if (field.getRole() != EFieldRole.NULLABLE) {
                    throw new IllegalArgumentException("Value of '" + field.getName()
                            + "' field was null but this field does not allow nulls");
                }
            } else {
                final int numConstraints = field.getNumConstraints();
                for (int j = 0; j < numConstraints; ++j) {
                    final AbstractFieldConstraint<?> constraint = field.getConstraint(j);
                    if (!constraint.isValidValue(value)) {
                        throw new IllegalArgumentException("Value of '" + field.getName()
                                + "' field does not satisfy field constraints");
                    }
                }
            }
        }
    }

    /**
     * Gets a particular field value.
     *
     * @param index the field index
     * @return the value
     */
    public Object getFieldValue(final int index) {

        return this.fieldValues[index];
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String fieldValuesStr = Arrays.toString(this.fieldValues);

        return SimpleBuilder.concat("Record{table=", this.table, ", fieldValues=", fieldValuesStr, "}");
    }
}
