package dev.mathops.app.adm.fields;

import javax.swing.JPanel;
import java.io.Serial;

/**
 * The base class for all fields. Each field tracks the underlying table name, and is capable of generating a "
 * name='value'" string that can be used to construct a WHERE clause in an SQL statement.
 */
public abstract class AbstractField extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 6766399113945885596L;

    /** The field name. */
    private final String name;

    /**
     * Constructs a new {@code AbstractField}.
     *
     * @param theName the field name
     */
    AbstractField(final String theName) {

        super();

        this.name = theName;
    }

    /**
     * Gets the field name.
     *
     * @return the name
     */
    @Override
    public final String getName() {

        return this.name;
    }

    /**
     * Tests whether the field has a non-empty value.
     *
     * @return true if the field has a value.
     */
    public abstract boolean hasValue();

    /**
     * Sets the editable status of the field.
     *
     * @param editable true if editable; false if not
     */
    public abstract void setEditable(boolean editable);

    /**
     * Sets the enabled status of the field.
     *
     * @param enabled true if enabled; false if not
     */
    @Override
    public abstract void setEnabled(boolean enabled);

    /**
     * Tests whether the field is enabled.
     *
     * @return true if enabled; false if not
     */
    @Override
    public abstract boolean isEnabled();

    /**
     * Requests focus.
     */
    @Override
    public abstract void requestFocus();

    /**
     * Sets the field value.
     *
     * @param value the new value
     */
    public abstract void setValue(Object value);
}
