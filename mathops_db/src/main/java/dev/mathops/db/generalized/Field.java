package dev.mathops.db.generalized;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.generalized.constraint.AbstractFieldConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

/**
 * An immutable definition of a database table field.
 */
public final class Field {

    /** An empty constraints array. */
    private static final AbstractFieldConstraint<?>[] ZERO_LEN_CONSTRAINTS = new AbstractFieldConstraint<?>[0];

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
     * @param theName the field name
     * @param theType the field data type
     * @param theRole the field role in the table
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
            this.constraints = ZERO_LEN_CONSTRAINTS;
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
    int getNumConstraints() {

        return this.constraints.length;
    }

    /**
     * Gets a specified field constraint.
     *
     * @param index the zero-based constraint index
     * @return the constraint
     */
    AbstractFieldConstraint<?> gemConstraint(final int index) {

        return this.constraints[index];
    }

    /**
     * Tests whether an object is of a type that is valid for this field.  Some types that are promotable to a valid
     * type are considered valid (an Integer is valid for a Long field, for example).
     *
     * @param object the object whose type to test (not {@code null})
     * @return true if the object's type is valid for this field
     */
    boolean isValidType(final Object object) {

        boolean valid = false;

        switch (this.type) {
            case STRING -> valid = object instanceof String;
            case BOOLEAN -> valid = object instanceof Boolean;
            case BYTE -> valid = object instanceof Byte;
            case INTEGER -> valid = object instanceof Byte || object instanceof Integer;
            case LONG -> valid = object instanceof Byte || object instanceof Integer || object instanceof Long;
            case FLOAT -> valid = object instanceof Float;
            case DOUBLE -> valid = object instanceof Float || object instanceof Double;
            case BLOB -> valid = object instanceof byte[];
            case LOCAL_DATE ->valid = object instanceof LocalDate;
            case LOCAL_TIME -> valid = object instanceof LocalTime;
            case LOCAL_DATE_TIME -> valid = object instanceof LocalDateTime;
        }

        return valid;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String constraintsString = Arrays.toString(this.constraints);

        return SimpleBuilder.concat("Field{name='", this.name , "', type=", this.type, ", role=", this.role,
                ", constraints=", constraintsString, "}");
    }
}
