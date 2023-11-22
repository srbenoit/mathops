package dev.mathops.assessment.formula.edit;

/**
 * Information about the cursor and selection range.
 */
public final class FECursor {

    /**
     * The cursor position within the object (0 means left of the first cursor step, maximum value is one greater than
     * the number of cursor steps); if there is a selection range, the cursor position will be at the start or end of
     * that range to indicate which end a "SHIFT-ARROW" action would alter.
     */
    public int cursorPosition;

    /**
     * The cursor position of the start of a selection range, -1 if there is no selection range.
     *
     * <p>
     * The "start" is where the cursor was positioned at the time a selection was started (when SHIFT was pressed and
     * arrow keys were used, or when a mouse drag was started). It may be greater than or less than the selection end.
     *
     * <p>
     * When there is a selection active, the current cursor position is the "selection end".
     */
    public int selectionStart;

    /**
     * Constructs a new {@code FECursor}.
     */
    public FECursor() {

        this.selectionStart = -1;
    }

    /**
     * Tests whether the selection includes the character at a specified position.
     *
     * @param pos the position
     * @return true if that position is included in the selection range
     */
    public boolean doesSelectionInclude(final int pos) {

        final boolean result;

        if (this.selectionStart == -1) {
            result = false;
        } else if (this.selectionStart < this.cursorPosition) {
            result = this.selectionStart <= pos && this.cursorPosition > pos;
        } else {
            result = this.cursorPosition <= pos && this.selectionStart > pos;
        }

        // Log.info("Start=" + this.selectionStart + ", cursor=" + this.cursorPosition + ", test="
        // + pos + ": result=" + result);

        return result;
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    public FECursor duplicate() {

        final FECursor dup = new FECursor();

        dup.cursorPosition = this.cursorPosition;
        dup.selectionStart = this.selectionStart;

        return dup;
    }
}
