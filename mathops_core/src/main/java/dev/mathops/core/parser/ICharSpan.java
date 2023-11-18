package dev.mathops.core.parser;

/**
 * An interface for a span of characters.
 */
public interface ICharSpan {

    /**
     * Gets the index of the first character of the span in the XML source character array.
     *
     * @return the start index
     */
    int getStart();

    /**
     * Gets the index after the last character in the span in the XML source character array.
     *
     * @return the end index
     */
    int getEnd();

    /**
     * Gets the line number of the start of the span.
     *
     * @return the line number (the first line in the file is line 1)
     */
    int getLineNumber();

    /**
     * Gets the column within its line of the start of the span.
     *
     * @return the column (the first character in the line is column 1)
     */
    int getColumn();
}
