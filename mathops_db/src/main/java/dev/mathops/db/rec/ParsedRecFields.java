package dev.mathops.db.rec;

import java.util.HashMap;
import java.util.Map;

/**
 * A container for fields parsed from the serialized representation of a record.
 */
public final class ParsedRecFields {

    /** A map from field name to the string representation of its value. */
    private final Map<String, String> fields;

    /**
     * Constructs a new {@code ParsedRecFields} from a string representation.
     *
     * @param maxFields the maximum number of fields to expect (used to size storage)
     * @param toParse   the string to parse
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    public ParsedRecFields(final int maxFields, final String toParse) throws IllegalArgumentException {

        this.fields = new HashMap<>(maxFields);

        final String[] parts = toParse.split(RecBase.DIVIDER);

        for (final String part : parts) {
            if (part.isBlank()) {
                // String serialization of null value can leave two adjacent delimiters (OK)
                continue;
            }

            final int eq = part.indexOf('=');
            if (eq == -1) {
                throw new IllegalArgumentException("String part without '=' sign: '" + part + "'");
            }

            final String name = part.substring(0, eq);
            final String value = part.substring(eq + 1);
            this.fields.put(name, value);
        }
    }

    /**
     * Tests whether this object has a value for a specified field name.
     *
     * @param name the name
     * @return {@code true} if a value is present; {@code false} if not
     */
    public boolean has(final String name) {

        return this.fields.containsKey(name);
    }

    /**
     * Gets the value associated with a name.
     *
     * @param name the name
     * @return the value
     */
    public String get(final String name) {

        return this.fields.get(name);
    }
}
