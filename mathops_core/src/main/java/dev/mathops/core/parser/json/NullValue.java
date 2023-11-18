package dev.mathops.core.parser.json;

/**
 * A singleton object to represent a JSON "null" value.
 */
public final class NullValue {

    /** The single instance. */
    public static final NullValue INSTANCE = new NullValue();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private NullValue() {

        // No action
    }
}
