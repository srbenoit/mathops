package dev.mathops.core.builder;

/**
 * Supplements the methods of {@code SimpleBuilder} to add CRLF to the ends of lines.
 */
class LineBuilder extends SimpleBuilder {

    /**
     * Constructs a new {@code LineBuilder}.
     *
     * @param capacity the initial capacity to allocate for storage
     */
    LineBuilder(final int capacity) {

        super(capacity);
    }

    /**
     * Appends the string representation of the char argument followed by a newline to this sequence.
     *
     * @param value the character value to append
     */
    final void innerAddLnChar(final char value) {

        innerAddChar(value);
        innerAddCrlf();
    }

    /**
     * Appends the string representation of the char argument to this sequence. The argument is appended to the contents
     * of this sequence, whose length increases by the length of the argument plus 2 newline characters.
     * <p>
     * The effect is exactly as if the character were converted to a string by the method {@code String.valueOf(char)}
     * and then appended to this character sequence.
     *
     * @param value the character value to append
     */
    final void innerAddLnChars(final char... value) {

        innerAddChars(value);
        innerAddCrlf();
    }

    /**
     * Appends the string representation of the char argument followed by a newline to this sequence.
     *
     * @param value the character value to append
     */
    final void innerAddLnString(final String value) {

        innerAddString(value);
        innerAddCrlf();
    }

    /**
     * Appends the string representation of the boolean argument followed by a newline to the sequence.
     *
     * @param value the boolean value to append
     */
    final void innerAddLnBoolean(final boolean value) {

        innerAddBoolean(value);
        innerAddCrlf();
    }

    /**
     * Appends the string representation of the float argument followed by a newline to this sequence.
     *
     * @param value the float value to append
     */
    final void innerAddLnFloat(final float value) {

        innerAddFloat(value);
        innerAddCrlf();
    }

    /**
     * Appends the string representation of the double argument followed by a newline to this sequence.
     *
     * @param value the double value to append
     */
    final void innerAddLnDouble(final double value) {

        innerAddDouble(value);
        innerAddCrlf();
    }

    /**
     * Appends the string representation of the int argument followed by a newline to this sequence.
     *
     * @param value the int value to append
     */
    final void innerAddLnInt(final int value) {

        innerAddInt(value);
        innerAddCrlf();
    }

    /**
     * Appends the string representation of the long argument followed by a newline to this sequence.
     *
     * @param value the long value to append
     */
    final void innerAddLnLong(final long value) {

        innerAddLong(value);
        innerAddCrlf();
    }

    /**
     * Appends the string representation of an {@code Object} followed by a newline to this sequence.
     *
     * @param value the objects to append
     */
    final void innerAddLnObject(final Object value) {

        innerAddObject(value);
        innerAddCrlf();
    }

    /**
     * Appends the string representation of any number of {@code Object}s followed by a newline to this sequence.
     *
     * @param value the objects to append
     */
    final void innerAddLnObjects(final Object... value) {

        innerAddObjects(value);
        innerAddCrlf();
    }
}
