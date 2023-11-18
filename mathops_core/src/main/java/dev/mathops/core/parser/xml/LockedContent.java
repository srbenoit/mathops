package dev.mathops.core.parser.xml;

import dev.mathops.core.log.Locked;

/**
 * A container for a character array of arbitrary content.
 *
 * <p>
 * This class supports read and write locks. Any process that accesses the content should first acquire a read lock,
 * releasing it once all content has been accessed. Any process that will update the content must first obtain a write
 * lock, releasing it after changes have been made. The write lock can be held by only one thread and only when no read
 * locks are held, but when the write lock is not held, multiple threads may obtain read locks.
 *
 * <p>
 * If it can be guaranteed that only one thread will access the content, locks are not necessary.
 */
class LockedContent extends Locked {

    /** The content. */
    private char[] content;

    /**
     * Constructs a new, empty, {@code LockedContent}.
     */
    LockedContent() {

        super();
        this.content = null;
    }

    /**
     * Constructs a new {@code LockedContent} with some initial content.
     *
     * @param theContent the new content
     */
    LockedContent(final char[] theContent) {

        super();
        setContent(theContent);
    }

    /**
     * Sets the content.
     *
     * @param theContent the new content
     */
    private void setContent(final char[] theContent) {

        this.content = theContent.clone();
    }

    /**
     * Gets the length (number of characters) of the XML content.
     *
     * @return the length
     */
    public final int length() {

        return this.content.length;
    }

    /**
     * Gets the character at a particular index.
     *
     * @param index the index
     * @return the character at that index
     */
    public final char get(final int index) {

        return this.content[index];
    }

    /**
     * Gets the content as a {@code String}.
     *
     * @return the content
     */
    @Override
    public final String toString() {

        return String.copyValueOf(this.content);
    }

    /**
     * Gets a subset of the content as a {@code String}.
     *
     * @param start index of the first character to return
     * @param end   the index after the last character to return
     * @return the substring
     */
    public final String substring(final int start, final int end) {

        return String.valueOf(this.content, start, end - start);
    }

    /**
     * Finds the index of the first instance of a specified character, starting at a specified search position and
     * advancing toward the end of the content.
     *
     * @param chr   the character for which to search
     * @param start the starting position from which to search
     * @return the index (>= start) of the first occurrence found, or -1 if the character was not found
     */
    public final int indexOf(final char chr, final int start) {

        int index = -1;

        final int len = this.content.length;
        for (int i = start; i < len; ++i) {
            if (this.content[i] == chr) {
                index = i;
                break;
            }
        }

        return index;
    }
}
