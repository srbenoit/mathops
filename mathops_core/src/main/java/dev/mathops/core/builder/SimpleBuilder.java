package dev.mathops.core.builder;

import dev.mathops.core.CoreConstants;

/**
 * Provides methods to concatenate string representations of various primitive or object types to the superclass
 * character sequence.
 */
public class SimpleBuilder extends CoreBuilder {

    /** A commonly used character array. */
    private static final char[] TRUE = "true".toCharArray();

    /** A commonly used character array. */
    private static final char[] FALSE = "false".toCharArray();

    /** A commonly used character array. */
    private static final char[] NULL = "null".toCharArray();

    /** A commonly used character array. */
    private static final char[] CRLF_CHARS = CoreConstants.CRLF.toCharArray();

    /**
     * Constructs a new {@code SimpleBuilder}.
     *
     * @param capacity the initial capacity to allocate for storage
     */
    SimpleBuilder(final int capacity) {

        super(capacity);
    }

    /**
     * Appends the string representation of the char argument to this sequence. The argument is appended to the contents
     * of this sequence, whose length increases by 1.
     * <p>
     * The effect is exactly as if the character were converted to a string by the method {@code String.valueOf(char)}
     * and then appended to this character sequence.
     *
     * @param value the character value to append
     */
    final void innerAddChar(final char value) {

        appendChar(value);
    }

    /**
     * Appends an array of characters to this sequence. The argument is appended to the contents of this sequence, whose
     * length increases by the length of the character array.
     * <p>
     * The effect is exactly as if the character were converted to a string by the method {@code String.valueOf(char)}
     * and then appended to this character sequence.
     *
     * @param value the character value to append
     */
    final void innerAddChars(final char[] value) {

        appendChars(value, 0, value.length);
    }

    /**
     * Appends a String argument to this sequence.
     *
     * @param value the String to append
     */
    final void innerAddString(final String value) {

        if (value == null) {
            innerAddChars(NULL);
        } else {
            final char[] chars = value.toCharArray();
            innerAddChars(chars);
        }
    }

    /**
     * Appends the string representation of the boolean argument to the sequence.
     * <p>
     * The argument is converted to a string as if by the method {@code String.valueOf}, and the characters of that
     * string are then appended to this sequence.
     *
     * @param value the boolean value to append
     */
    final void innerAddBoolean(final boolean value) {

        innerAddChars(value ? TRUE : FALSE);
    }

    /**
     * Appends the string representation of the int argument to this sequence.
     * <p>
     * The argument is converted to a string as if by the method {@code String.valueOf}, and the characters of that
     * string are then appended to this sequence.
     *
     * @param value the int value to append
     */
    final void innerAddInt(final int value) {

        final String str = Integer.toString(value);
        innerAddString(str);
    }

    /**
     * Appends the string representation of the long argument to this sequence.
     * <p>
     * The argument is converted to a string as if by the method {@code String.valueOf}, and the characters of that
     * string are then appended to this sequence.
     *
     * @param value the long value to append
     */
    final void innerAddLong(final long value) {

        final String str = Long.toString(value);
        innerAddString(str);
    }

    /**
     * Appends the string representation of the float argument to this sequence.
     * <p>
     * The argument is converted to a string as if by the method {@code String.valueOf}, and the characters of that
     * string are then appended to this sequence.
     *
     * @param value the float value to append
     */
    final void innerAddFloat(final float value) {

        final String str = Float.toString(value);
        innerAddString(str);
    }

    /**
     * Appends the string representation of the double argument to this sequence.
     * <p>
     * The argument is converted to a string as if by the method {@code String.valueOf}, and the characters of that
     * string are then appended to this sequence.
     *
     * @param value the double value to append
     */
    final void innerAddDouble(final double value) {

        final String str = Double.toString(value);
        innerAddString(str);
    }

    /**
     * Appends the string representation of an {@code Object}s to this sequence.
     * <p>
     * The argument is converted to a string by its {@code toString} method, and the characters of that string are then
     * appended to this sequence.
     *
     * @param value the object to append
     */
    final void innerAddObject(final Object value) {

        if (value == null) {
            innerAddChars(NULL);
        } else if (value instanceof char[]) {
            innerAddChars((char[]) value);
        } else {
            final String str = value.toString();
            innerAddString(str);
        }
    }

    /**
     * Appends the string representation of any number of {@code Object}s to this sequence.
     * <p>
     * Each argument is converted to a string by its {@code toString} method, and the characters of that string are then
     * appended to this sequence.
     *
     * @param value the objects to append
     */
    final void innerAddObjects(final Object... value) {

        if (value != null) {
            for (final Object val : value) {
                if (val == null) {
                    innerAddChars(NULL);
                } else if (val instanceof char[]) {
                    innerAddChars((char[]) val);
                } else {
                    final String str = val.toString();
                    innerAddString(str);
                }
            }
        }
    }

    /**
     * Appends the characters for a CRLF end-of-line to this sequence.
     */
    final void innerAddCrlf() {

        innerAddChars(CRLF_CHARS);
    }

    /**
     * Generates a String by concatenating a list of strings.
     *
     * @param strings the strings to concatenate
     * @return the constructed string
     */
    public static String concat(final Object... strings) {

        final SimpleBuilder builder = new SimpleBuilder(100);

        for (final Object s : strings) {
            if (s != null) {
                builder.innerAddObject(s);
            }
        }

        return builder.toString();
    }
}
