package dev.mathops.core.parser;

/**
 * A span of characters.
 */
public class CharSpan implements ICharSpan {

    /** The index of the first character of the span. */
    private final int start;

    /** The index after the last character in the span. */
    private final int end;

    /** The line number of the start of the span (the first line in the file is line 1). */
    private final int lineNumber;

    /** The column of the start of the span (the first character in the line is column 1). */
    private final int column;

    /**
     * Constructs a new {@code CharSpan}.
     *
     * @param theStart      the index of the first character of the token in the XML source character array
     * @param theEnd        the index after the last character in the token in the XML source character array
     * @param theLineNumber the line number of the start of the span (the first line in the file is line 1)
     * @param theColumn     the column of the start of the span (the first character in the line is column 1)
     */
    public CharSpan(final int theStart, final int theEnd, final int theLineNumber,
                    final int theColumn) {

        this.start = theStart;
        this.end = theEnd;
        this.lineNumber = theLineNumber;
        this.column = theColumn;
    }

    /**
     * Gets the index of the first character of the token in the XML source character array.
     *
     * @return the start index
     */
    @Override
    public final int getStart() {

        return this.start;
    }

    /**
     * Gets the index after the last character in the token in the XML source character array.
     *
     * @return the end index
     */
    @Override
    public final int getEnd() {

        return this.end;
    }

    /**
     * Gets the line number of the start of the span.
     *
     * @return the line number (the first line in the file is line 1)
     */
    @Override
    public final int getLineNumber() {

        return this.lineNumber;
    }

    /**
     * Gets the column within its line of the start of the span.
     *
     * @return the column (the first character in the line is column 1)
     */
    @Override
    public final int getColumn() {

        return this.column;
    }
}
