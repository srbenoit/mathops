package dev.mathops.db.generalized;

import dev.mathops.db.generalized.constraint.AbstractFieldConstraint;

/**
 * An immutable definition of a database table field.
 */
public final class Field {

    /** The field name. */
    private final String name;

    /** The field data type. */
    private final EFieldType type;

    /** The field role in the table. */
    private final EFieldRole role;

    /** Any constraints attached to the field. */
    private final AbstractFieldConstraint<?>[] constraints;

    /**
     * Constructs a new {@code Field}.
     *
     * @param theType the field data type
     * @param theConstraints any constraints attached to the field
     */
    public Field(final String theName, final EFieldType theType, final EFieldRole theRole,
                 final AbstractFieldConstraint<?>... theConstraints) {

        if (theName == null) {
            throw new IllegalArgumentException("Field name may not be null");
        }
        if (theType == null) {
            throw new IllegalArgumentException("Field type may not be null");
        }
        if (theRole == null) {
            throw new IllegalArgumentException("Field role may not be null");
        }

        this.name = theName;
        this.type = theType;
        this.role = theRole;

        if (theConstraints == null) {
            this.constraints = new AbstractFieldConstraint[0];
        } else {
            final int len = theConstraints.length;
            this.constraints = new AbstractFieldConstraint[len];

            for (int i = 0; i < len; ++i) {
                if (theConstraints[i] == null) {
                    throw new IllegalArgumentException("Field constraints array may not include null values");
                }
                if (theConstraints[i].getFieldType() != theType) {
                    throw new IllegalArgumentException("Field constraints type does not match field type");
                }
                this.constraints[i] = theConstraints[i];
            }
        }
    }

    /**
     * Gets the field name.
     *
     * @return the field name
     */
    public String getName() {

        return this.name;
    }

    /**
     * Gets the field data type.
     *
     * @return the field type
     */
    public EFieldType getType() {

        return this.type;
    }

    /**
     * Gets the field role.
     *
     * @return the field role
     */
    public EFieldRole getRole() {

        return this.role;
    }

    /**
     * Gets the number of field constraints.
     *
     * @return the number of constraints
     */
    public int getNumConstraints() {

        return this.constraints.length;
    }

    /**
     * Gets a specified field constraint.
     *
     * @param index the zero-based constraint index
     * @return the constraint
     */
    public AbstractFieldConstraint<?> gemConstraint(final int index) {

        return this.constraints[index];
    }
}
