package dev.mathops.app.ui;

import javax.swing.JTextField;

/**
 * A component that allows the user to enter an integer ot to specify NULL or a query criteria for integer fields.
 *
 * <p>
 * This field can display integer data from an existing record (including displaying "NULL" when the value is null), can
 * allow the user to enter integer data for a new record, or can enter a criterion for a query.  Query criteria must be
 * one of the following:
 * <ul>
 *     <li>IS NULL (a special "character" rendered as a single glyph)</li>
 *     <li>IS NOT NULL (a special "character" rendered as a single glyph)</li>
 *     <li>15 (matches field values of 15 exactly)</li>
 *     <li>=15 (matches field values of 15 exactly)</li>
 *     <li>!=15 (matches any field value except 15)</li>
 *     <li>&lt;&gt;15 (matches any field value except 15)</li>
 *     <li>&lt;15 (matches any field value less than 15)</li>
 *     <li>&lt;=15 (matches any field value less than or equal to 15)</li>
 *     <li>&gt;15 (matches any field value greater than 15)</li>
 *     <li>&gt;=15 (matches any field value greater than or equal to 15)</li>
 *     <li>Any two of the greater than or less than comparisons</li>
 * </ul>
 */
public final class DbIntegerField extends AbstractDbField {

    /** The minimum allowed value. */
    private final int min;

    /** The maximum allowed value. */
    private final int max;

    /**
     * Constructs a new {@code DBIntegerField} in the "DISPLAY_EXISTING_DATA" mode.
     *
     * @param theNullability the nullability of the field
     * @param theMin         the minimum allowed value
     * @param theMax         the maximum allowed value
     */
    public DbIntegerField(final ENullability theNullability, final int theMin, final int theMax) {

        super(theNullability);

        this.min = theMin;
        this.max = theMax;

        final JTextField text = new JTextField();
        setBorder(text.getBorder());
        setPreferredSize(text.getPreferredSize());
        setBackground(text.getBackground());
        setForeground(text.getForeground());

        setFocusable(true);
    }

    /**
     * Sets the field mode.
     *
     * @param newMode the new mode
     */
    public void setMode(final EDbFieldMode newMode) {

        innerSetMode(newMode);
    }

    /**
     * Clears the field.
     */
    @Override
    public void clear() {

    }

    /**
     * Sets the field to display a "NULL" value (used in modes that display data; for modes that accept query criteria,
     * for example to set an "IS NULL" or "IS NOT NULL" query condition, use {@see setToIsNull} or
     * {@see setToIsNotNull}).
     */
    public void setToNull() {

    }

    /**
     * Sets the field to "IS NULL" as a query condition.
     */
    public void setToIsNull() {

    }

    /**
     * Sets the field to "IS NOT NULL" as a query condition.
     */
    public void setToIsNotNull() {

    }
}
