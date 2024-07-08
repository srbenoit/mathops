package dev.mathops.app.adm.fields;

import java.io.Serial;

/**
 * The base class for fields that represent an integer ({@code Long}) value.
 */
abstract class AbstractLongField extends AbstractField {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8249138086661820151L;

    /**
     * Constructs a new {@code AbstractLongField}.
     *
     * @param theName the field name
     */
    AbstractLongField(final String theName) {

        super(theName);
    }

    /**
     * Gets the value as a {@code Integer}.
     *
     * @return the value, null if the field is empty
     */
    public abstract Integer getIntegerValue();

    /**
     * Gets the value as a {@code Long}.
     *
     * @return the value, null if the field is empty
     */
    public abstract Long getLongValue();
}
