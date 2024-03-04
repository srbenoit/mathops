package dev.mathops.assessment.expression.editmodel;

/**
 * An expression object that represents a string, enclosed in matched double-quotes.
 */
public final class ExprBranchString extends AbstractExprBranch {

    /** The characters. */
    public char[] characters;

    /** The length (could be shorter than the allocated character array). */
    public int len;

    /**
     * Constructs a new {@code ExprBranchString}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    public ExprBranchString(final AbstractExprObject theParent) {

        super(theParent);

        this.characters = new char[10];
        this.len = 0;

        // Initially, there is one cursor position for the opening quote and one for the closing quote
        innerSetNumCursorPositions(2);
    }

    /**
     * Called when something potentially changes the number of cursor positions in a child.  This method recalculates
     * the starting cursor positions for each child of this object, and updates this objects total cursor position
     * count.  If this results in a change to this object's cursor position count, the call is propagated upward to the
     * parent (if any).
     */
    void recalculate(final int theFirstCursorPosition) {

        final int origCount = getNumCursorPositions();
        final int newCount = 2 + this.len;

        if (newCount != origCount) {
            innerSetNumCursorPositions(newCount);
            if (getParent() instanceof final AbstractExprBranch parentBranch) {
                final int parentFirst = parentBranch.getFirstCursorPosition();
                parentBranch.recalculate(parentFirst);
            }
        }
    }

    /**
     * Processes an action represented by an integer.  If the action code is 0xFFFF or smaller, it is interpreted
     * as a Unicode character;  otherwise, it is interpreted as an enumerated code.
     *
     * <p>
     * If there is a selection and an action is performed that would result in the deletion of that selection, the
     * deletion is done before this method is called.  Actions on objects are called only when there is no selection
     * region.  Actions like CUT/COPY/PASTE/DELETE (and undo/redo) are handled by other mechanisms.
     *
     * @param action the action code
     * @param cursorPosition the cursor position
     */
    void processAction(final int action, final int cursorPosition) {

        if (action < 0x00010000) {
            final char character = (char)action;

            if (this.len == this.characters.length) {
                final char[] newCharactrers = new char[this.len + 10];
                System.arraycopy(this.characters, 0, newCharactrers, 0, this.len);
                this.characters = newCharactrers;
            }

            final int firstPos = getFirstCursorPosition();
            final int offset = cursorPosition - firstPos - 1;
            if (offset >= this.len) {
                this.characters[this.len] = character;
            } else {
                final int toCopy = this.len - offset;
                System.arraycopy(this.characters, offset, this.characters, offset + 1, toCopy);
                this.characters[offset] = character;
            }
            ++this.len;

            recalculate(firstPos);
        }
    }
}
