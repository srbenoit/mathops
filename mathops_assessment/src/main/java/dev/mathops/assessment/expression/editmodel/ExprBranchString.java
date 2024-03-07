package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An expression object that represents a string, enclosed in matched double-quotes.
 */
public final class ExprBranchString extends ExprObjectBranch {

    /** The characters. */
    public char[] characters;

    /** The length (could be shorter than the allocated character array). */
    public int len;

    /**
     * Constructs a new {@code ExprBranchString}.
     */
    public ExprBranchString() {

        super();

        this.characters = new char[10];
        this.len = 0;
    }

    /**
     * Gets the number of characters in the string.
     *
     * @return the number of characters in the string
     */
    public int len() {

        return this.len;
    }

    /**
     * Gets one character of the string.
     *
     * @param index the index
     * @return the character
     */
    public char getChar(final int index) {

        return this.characters[index];
    }

    /**
     * Insets a character in the string at a specified index.
     *
     * @param index  the index at which to insert the object
     * @param character the character to insert
     */
    public void insert(final int index, final char character) {

        if (this.len == this.characters.length) {
            final char[] newName = new char[this.len + 10];
            System.arraycopy(this.characters, 0, newName, 0, this.len);
            this.characters = newName;
        }

        if (index >= this.len) {
            this.characters[this.len] = character;
        } else {
            final int count = this.len - index;
            System.arraycopy(this.characters, index, this.characters, index + 1, count);
            this.characters[index] = character;
        }

        ++this.len;
    }

    /**
     * Removes a character at a specified index in the string.
     *
     * @param index the index of the character to remove
     */
    public void remove(final int index) {

        if (index < this.len - 1) {
            final int count = this.len - index;
            System.arraycopy(this.characters, index + 1, this.characters, index, count);
        }
        --this.len;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("ExprBranchString{");
        htm.add(new String(this.characters, 0, this.len));
        htm.add("}");

        return htm.toString();
    }
}
