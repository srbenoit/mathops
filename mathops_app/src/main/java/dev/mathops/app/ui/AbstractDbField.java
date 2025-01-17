package dev.mathops.app.ui;

import javax.swing.JPanel;

/**
 * A base class for database fields that support specifying query criteria.
 */
public abstract class AbstractDbField extends JPanel {

    /** The current field mode. */
    private EDbFieldMode mode;

    /** A flag indicating the field should allow a NULL value. */
    private final ENullability nullability;

    /**
     * Constructs a new {@code AbstractDbField} in the "DISPLAY_EXISTING_DATA" mode.
     *
     * @param theNullability the nullability of the field
     */
    AbstractDbField(final ENullability theNullability) {

        super(null);

        this.nullability = theNullability;
        this.mode = EDbFieldMode.DISPLAY_EXISTING_DATA;
    }

    /**
     * Gets the nullability for the field.
     *
     * @return the nullability
     */
    public final ENullability getNullability() {

        return this.nullability;
    }

    /**
     * Sets the field mode.
     *
     * @param newMode the new mode
     */
    public abstract void setMode(final EDbFieldMode newMode);

    /**
     * Stores the field mode in the member variable.  Call this as part of the implementation of {@code setMode}.
     *
     * @param newMode the new mode
     */
    protected void innerSetMode(final EDbFieldMode newMode) {

        this.mode = newMode;
    }

    /**
     * Gets the current field mode.
     *
     * @return the field mode
     */
    public EDbFieldMode getMode() {

        return this.mode;
    }

    /**
     * Clears the field.  This is different from displaying a NULL field value.  When inserting new data, fields are
     * cleared and the user can enter values.  Fields left blank could be assumed to be NULL for the inserted record.
     */
    public abstract void clear();

    /**
     * Sets the field to display a "NULL" value (used in modes that display data; for modes that accept query criteria,
     * for example to set an "IS NULL" or "IS NOT NULL" query condition, use {@see setToIsNull} or
     * {@see setToIsNotNull}).
     */
    public abstract void setToNull();

    /**
     * Sets the field to "IS NULL" as a query condition.
     */
    public abstract void setToIsNull();

    /**
     * Sets the field to "IS NOT NULL" as a query condition.
     */
    public abstract void setToIsNotNull();
}
