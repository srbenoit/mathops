package dev.mathops.core.parser;

/**
 * A position within a file. This is a mutable object updated as the file is parsed.
 */
public final class FilePosition {

    /** The byte offset within the file. */
    public int byteOffset;

    /** The line number (the first line in the file is line 1). */
    public int lineNumber;

    /** The column within its line (the first character in a line is column 1). */
    public int column;

    /**
     * Constructs a new {@code FilePosition} with byte offset 0, line 1, and column 1.
     */
    public FilePosition() {

        this.lineNumber = 1;
        this.column = 1;
    }

    /**
     * Copies the fields from another {@code FilePosition} into this one.
     *
     * @param source the source {@code FilePosition}
     */
    public void copyFrom(final FilePosition source) {

        this.byteOffset = source.byteOffset;
        this.lineNumber = source.lineNumber;
        this.column = source.column;
    }
}
