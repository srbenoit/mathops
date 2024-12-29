package dev.mathops.assessment.expression.editmodel;

import dev.mathops.text.builder.HtmlBuilder;

/**
 * An expression object that represents a variable name, with optional accent and/or subscript.
 *
 * <p>
 * When a variable with a subscript is converted to a variable name, the ASCII control character 0x001F is inserted
 * between the name and the subscript.  If the variable name has an accent, control character 0x001E followed by the
 * accent character are appended.
 * </p>
 */
public final class ExprBranchVariableName extends ExprObjectBranch {

    /** The optional accent. */
    private EVariableAccent accent;

    /** The name characters. */
    private char[] name;

    /** The name length (could be shorter than the allocated name array). */
    private int nameLen;

    /** The subscript characters. */
    private char[] subscript;

    /** The subscript length (could be shorter than the allocated subscript array). */
    private int subscriptLen;

    /**
     * Constructs a new {@code ExprBranchVariableName}.
     */
    public ExprBranchVariableName() {

        super();

        this.name = new char[10];
        this.nameLen = 0;
        this.subscript = new char[4];
        this.subscriptLen = 0;
    }

    /**
     * Gets the accent.
     *
     * @return the accent ({@code null} if none)
     */
    public EVariableAccent getAccent() {

        return this.accent;
    }

    /**
     * Sets the accent.
     *
     * @param theAccent the new accent ({@code null} if none)
     */
    public void setAccent(final EVariableAccent theAccent) {

        this.accent = theAccent;
    }

    /**
     * Gets the number of characters in the variable name.
     *
     * @return the number of characters in the name
     */
    public int nameLen() {

        return this.nameLen;
    }

    /**
     * Gets one character of the name.
     *
     * @param index the index
     * @return the character
     */
    public char getNameChar(final int index) {

        return this.name[index];
    }

    /**
     * Insets a character in the name at a specified index.
     *
     * @param index  the index at which to insert the object
     * @param character the character to insert
     */
    public void insertName(final int index, final char character) {

        if (this.nameLen == this.name.length) {
            final char[] newName = new char[this.nameLen + 10];
            System.arraycopy(this.name, 0, newName, 0, this.nameLen);
            this.name = newName;
        }

        if (index >= this.nameLen) {
            this.name[this.nameLen] = character;
        } else {
            final int count = this.nameLen - index;
            System.arraycopy(this.name, index, this.name, index + 1, count);
            this.name[index] = character;
        }

        ++this.nameLen;
    }

    /**
     * Removes a character at a specified index in the name.
     *
     * @param index the index of the character to remove
     */
    public void removeName(final int index) {

        if (index < this.nameLen - 1) {
            final int count = this.nameLen - index;
            System.arraycopy(this.name, index + 1, this.name, index, count);
        }
        --this.nameLen;
    }

    /**
     * Gets the number of characters in the variable subscript.
     *
     * @return the number of characters in the subscript
     */
    public int subscriptLen() {

        return this.subscriptLen;
    }

    /**
     * Gets one character of the subscript.
     *
     * @param index the index
     * @return the character
     */
    public char getSubscriptChar(final int index) {

        return this.subscript[index];
    }

    /**
     * Insets a character in the subscript at a specified index.
     *
     * @param index  the index at which to insert the object
     * @param character the character to insert
     */
    public void insertSubscript(final int index, final char character) {

        if (this.subscriptLen == this.subscript.length) {
            final char[] newName = new char[this.subscriptLen + 10];
            System.arraycopy(this.subscript, 0, newName, 0, this.subscriptLen);
            this.subscript = newName;
        }

        if (index >= this.subscriptLen) {
            this.subscript[this.subscriptLen] = character;
        } else {
            final int count = this.subscriptLen - index;
            System.arraycopy(this.subscript, index, this.subscript, index + 1, count);
            this.subscript[index] = character;
        }

        ++this.subscriptLen;
    }

    /**
     * Removes a character at a specified index in the name.
     *
     * @param index the index of the character to remove
     */
    public void removeSubscript(final int index) {

        if (index < this.subscriptLen - 1) {
            final int count = this.subscriptLen - index;
            System.arraycopy(this.subscript, index + 1, this.subscript, index, count);
        }
        --this.subscriptLen;
    }

    /**
     * Generates a variable name string from this variable name.
     * @return the name string
     */
    public String toVariableNameString() {

        final StringBuilder str = new StringBuilder(20);

        if (this.nameLen > 0) {
            str.append(this.name, 0, this.nameLen);
        }
        if (this.subscriptLen > 0) {
            str.append('\u001F');
            str.append(this.subscript, 0, this.subscriptLen);
        }
        if (this.accent != null) {
            str.append('\u001E');
            str.append(this.accent.character);
        }

        return str.toString();
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("ExprBranchVariableName{name='");
        htm.add(new String(this.name, 0, this.nameLen));
        htm.add('\'');

        if (this.subscript != null) {
            htm.add(",subscript='");
            htm.add(new String(this.subscript, 0, this.subscriptLen));
            htm.add('\'');
        }

        if (this.accent != null) {
            htm.add(",accent=", this.accent);
        }

        htm.add("}");

        return htm.toString();
    }
}
