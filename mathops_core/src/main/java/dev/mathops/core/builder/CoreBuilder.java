package dev.mathops.core.builder;

import dev.mathops.core.CoreConstants;

/**
 * Supports the construction of strings by appending to a character array which resizes as needed.
 */
public class CoreBuilder {

    /** The array of characters, generally larger than the current size. */
    private char[] chars;

    /** The number of characters in the array. */
    private int size;

    /**
     * Constructs a new {@code CoreBuilder}.
     *
     * @param capacity the initial capacity to allocate for storage
     */
    CoreBuilder(final int capacity) {

        this.chars = new char[capacity];
        this.size = 0;
    }

    /**
     * Gets the current length (number of characters) currently in the builder.
     *
     * @return the length
     */
    public final int length() {

        return this.size;
    }

    /**
     * Adds spaces to the buffer until the length is at least some specified length. Useful for constructing vertically
     * aligned reports or tables.
     *
     * @param len the length to which to pad
     * @return this object
     */
    public final CoreBuilder padToLength(final int len) {

        ensureCapacity(len);

        while (this.size < len) {
            this.chars[this.size] = CoreConstants.SPC_CHAR;
            ++this.size;
        }

        return this;
    }

    /**
     * Gets the character at a specified position.
     *
     * @param index the index
     * @return the character at that index
     */
    public final char charAt(final int index) {

        return this.chars[index];
    }

    /**
     * Resets the builder (clears any string content).
     */
    public final void reset() {

        this.size = 0;
    }

    /**
     * Truncates the string at a specified size.
     *
     * @param newSize the new size (if longer than current string, no action is taken)
     * @return this object
     */
    public final CoreBuilder truncate(final int newSize) {

        if (newSize >= 0) {
            this.size = Math.min(this.size, newSize);
        }

        return this;
    }

    /**
     * Returns a string representing the data in this sequence. A new {@code String} object is allocated and initialized
     * to contain the character sequence currently represented by this object. This {@code String} is then returned.
     * Subsequent changes to this sequence do not affect the contents of the {@code String}.
     *
     * @return the string
     */
    @Override
    public final String toString() {

        return new String(this.chars, 0, this.size);
    }

    /**
     * Ensures that the builder has at least a given capacity.
     *
     * @param cap the target capacity
     */
    private void ensureCapacity(final int cap) {

        if (this.chars.length < cap) {

            final int newLen = Math.max(cap, this.chars.length * 3 / 2);
            final char[] array = new char[newLen];
            System.arraycopy(this.chars, 0, array, 0, this.size);
            this.chars = array;
        }
    }

    /**
     * Appends a character to the builder's character array.
     *
     * @param ch the character
     */
    public final void appendChar(final char ch) {

        ensureCapacity(this.size + 1);
        this.chars[this.size] = ch;
        ++this.size;
    }

    /**
     * Appends some characters to the builder's character array.
     *
     * @param source the source character array
     * @param start  the starting position from which to copy
     * @param len    the number of characters to copy
     */
    final void appendChars(final char[] source, final int start, final int len) {

        ensureCapacity(this.size + len);
        System.arraycopy(source, start, this.chars, this.size, len);
        this.size += len;
    }
}
