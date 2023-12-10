package dev.mathops.db.generalized.criteria;

import dev.mathops.db.generalized.Field;

/**
 * The base class for criteria that can be used to match fields within a table's records.
 */
public abstract class AbstractFieldCriterion {

    /** The field being queried. */
    private final Field field;

    /**
     * Constructs a new {@code AbstractFieldCriterion}.
     *
     * @param theField the field being queried
     */
    protected AbstractFieldCriterion(final Field theField) {

        this.field = theField;
    }

    /**
     * Gets the field being queried.
     *
     * @return the field
     */
    public final Field getField() {

        return this.field;
    }
}
