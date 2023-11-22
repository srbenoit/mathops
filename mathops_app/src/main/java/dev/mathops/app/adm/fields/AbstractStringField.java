package dev.mathops.app.adm.fields;

import java.io.Serial;

/**
 * The base class for fields that represent a string value.
 */
/* default */ abstract class AbstractStringField extends AbstractField {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1852129817601722951L;

    /**
     * Constructs a new {@code AbstractStringField}.
     *
     * @param theName the field name
     */
    AbstractStringField(final String theName) {

        super(theName);
    }

    /**
     * Gets the value as a String.
     *
     * @return the value, null if the field is empty
     */
    public abstract String getStringValue();
}
