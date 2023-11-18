package dev.mathops.core.parser.xml;

import dev.mathops.core.parser.CharSpan;

/**
 * A generic span that covers a tag.
 */
public final class TagSpan extends CharSpan {

    /** {@code true} if this is an open (or empty) tag. */
    public final boolean open;

    /** {@code true} if this is a close (or empty) tag. */
    public final boolean close;

    /** The index of the start of the tag name. */
    public final int nameStart;

    /** The index of the end of the tag name. */
    public final int nameEnd;

    /**
     * Constructs a new {@code TagSpan}.
     *
     * @param theStart     the start position of the tag
     * @param theEnd       the end position of the tag
     * @param theLineNumber   the line number of the start of the span
     * @param theColumn       the column of the start of the span
     * @param isOpen       {@code true} if this is an open (or empty) tag
     * @param isClose      {@code true} if this is a close (or empty) tag
     * @param nameStartPos the start position of the tag name
     * @param nameEndPos   the end position of the tag name
     */
    TagSpan(final int theStart, final int theEnd, final int theLineNumber, final int theColumn,
            final boolean isOpen, final boolean isClose, final int nameStartPos, final int nameEndPos) {

        super(theStart, theEnd, theLineNumber, theColumn);

        this.open = isOpen;
        this.close = isClose;
        this.nameStart = nameStartPos;
        this.nameEnd = nameEndPos;
    }
}
