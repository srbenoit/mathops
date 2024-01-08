package dev.mathops.db.generalized;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.generalized.constraint.AbstractFieldConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * An immutable definition of a database table field.
 */
public final class Field implements Comparable<Field> {

    /** An empty constraints array. */
    private static final AbstractFieldConstraint<?>[] ZERO_LEN_CONSTRAINTS = new AbstractFieldConstraint<?>[0];

    /** The field name. */
    private final String name;

    /** The field data type. */
    private final EFieldType type;

    /** The field role in the table. */
    private final EFieldRole role;

    /** The field description. */
    private final String description;

    /** Any constraints attached to the field. */
    private final AbstractFieldConstraint<?>[] constraints;

    /**
     * Constructs a new {@code Field}.
     *
     * @param theName        the field name
     * @param theType        the field data type
     * @param theRole        the field role in the table
     * @param theDescription the field description (this should be a short phrase that could be used in a tool-tip
     *                       associated with a field where the user is entering a value for the field)
     * @param theConstraints any constraints attached to the field
     */
    public Field(final String theName, final EFieldType theType, final EFieldRole theRole,
                 final String theDescription, final AbstractFieldConstraint<?>... theConstraints) {

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
        this.description = theDescription;

        if (theConstraints == null) {
            this.constraints = ZERO_LEN_CONSTRAINTS;
        } else {
            final int len = theConstraints.length;
            this.constraints = new AbstractFieldConstraint[len];
            final Collection<String> names = new HashSet<>(len);

            for (int i = 0; i < len; ++i) {
                if (theConstraints[i] == null) {
                    throw new IllegalArgumentException("Field constraints array may not include null values");
                }
                final String constraintName = theConstraints[i].getName();
                if (names.contains(constraintName)) {
                    throw new IllegalArgumentException("All constraints on a field must have unique names");
                }
                names.add(constraintName);
                this.constraints[i] = theConstraints[i];
            }

            // We sort the constraints so two fields that have the same name/type/role/description and the same set of
            // constraints will be considered "equal" regardless of the ordering in which they provide the constraints.
            // This is the reason for having a constraint name - there is no other field shared by all types of
            // constraint that could provide a natural ordering for constraints within a field.
            Arrays.sort(this.constraints);
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
     * Gets the field description.
     *
     * @return the field description
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * Gets the constraints array.
     *
     * @return the constraints array
     */
    private AbstractFieldConstraint<?>[] innerGetConstraints() {

        return this.constraints;
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
    public AbstractFieldConstraint<?> getConstraint(final int index) {

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
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.name.hashCode() + this.type.hashCode() + this.role.hashCode() + Objects.hashCode(this.description)
                + Arrays.hashCode(this.constraints);
    }

    /**
     * Tests whether this object is equal to another.  To be equal, the other object must be a {@code Field} with the
     * same values for all fields.
     *
     * @return the hash code
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final Field objField) {
            final String objName = objField.getName();
            final String objDescription = objField.getDescription();
            final AbstractFieldConstraint<?>[] objConstraints = objField.innerGetConstraints();

            equal = this.type == objField.getType() && this.role == objField.getRole() && this.name.equals(objName)
                    && Objects.equals(this.description, objDescription)
                    && Arrays.equals(this.constraints, objConstraints);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     * the specified object
     */
    @Override
    public int compareTo(final Field o) {

        final String oName = o.getName();
        int result = this.name.compareTo(oName);

        if (result == 0) {
            final EFieldType oType = o.getType();
            result = this.type.compareTo(oType);

            if (result == 0) {
                final EFieldRole oRole = o.getRole();
                result = this.role.compareTo(oRole);
            }
        }

        return result;
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
