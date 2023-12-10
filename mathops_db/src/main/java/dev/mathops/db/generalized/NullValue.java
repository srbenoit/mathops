package dev.mathops.db.generalized;

/**
 * A singleton null value place-holder used within an {@code UpdatedValues} object to indicate the operation of
 * updating a field to a null value.
 */
public final class NullValue {

    /** The single instance. */
    public static final NullValue INSTANCE = new NullValue();

    /**
     * Constructs a new {@code NullValue}.
     */
    private NullValue() {

        // No action
    }
}
