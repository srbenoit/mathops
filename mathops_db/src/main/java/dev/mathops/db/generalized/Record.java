package dev.mathops.db.generalized;

import dev.mathops.db.generalized.constraint.AbstractFieldConstraint;

/**
 * An immutable generalized record object, which references a "Table" object (with field definitions), and which stores
 * an array of field values.
 */
public class Record {

    /** The table to which this record belongs. */
    private final Table table;

    /** Field values. */
    private final Object[] fieldValues;

    /**
     * Constructs a new {@code Record}.  The constructor ensures that the constructed object is valid with respect to
     * the fields define din the table.
     *
     * @param theTable the table to which this record belongs
     * @param theFieldValues the field values, where trailing null values may be omitted (if no values are permitted,
     *                       the result is a record with all null field values)
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
                    final AbstractFieldConstraint<?> constraint = field.gemConstraint(j);
                    if (!constraint.isValidValue(value)) {
                        throw new IllegalArgumentException("Value of '" + field.getName()
                                + "' field does not satisfy field constraints");
                    }
                }
            }
        }
    }
}
