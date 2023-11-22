package dev.mathops.app.adm;

import java.io.Serial;
import java.util.HashMap;

/**
 * A generic record.
 */
public class GenericRecord extends HashMap<String, Object> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 652420490547500625L;

    /**
     * Constructs a new {@code GenericRecord}.
     *
     * @param initialCapacity the number of fields
     */
    public GenericRecord(final int initialCapacity) {

        super(initialCapacity);
    }
}
