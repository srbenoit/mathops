package dev.mathops.assessment.expression.editview;

/**
 * A box that bounds an object in a presented expression.
 */
class ObjectBox {

    /** The X position of the left edge of the box. */
    int x;

    /** The Y position of the typographic baseline (positive Y is downward). */
    int y;

    /** The width of the box. */
    int width;

    /** The (positive) offset relative to the baseline of the top of the box. */
    int top;

    /** The (negative) offset relative to the baseline of the bottom of the box (height = top - bottom). */
    int bottom;

    /** The (positive) offset relative to the baseline of the mathematical center-line of the box. */
    int center;

    /**
     * Constructs a new {@code ObjectBox}.
     */
    ObjectBox() {

        // No action
    }

    /**
     * Gets the total height of the box.
     *
     * @return the height
     */
    final int getHeight() {

        return this.top - this.bottom;
    }
}
