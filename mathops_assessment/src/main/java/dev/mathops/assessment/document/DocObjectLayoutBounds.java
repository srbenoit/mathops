package dev.mathops.assessment.document;

/**
 * A container for layout bounds, computed when an object is laid out.
 */
public final class DocObjectLayoutBounds {

    /** The size and position of the object. */
    public int x;

    /** The size and position of the object. */
    public int y;

    /** The size and position of the object. */
    public int width;

    /** The size and position of the object. */
    public int height;

    /** The Y offset from the top of the bounding box of the baseline. */
    public int baseLine;

    /** The Y offset from the top of the bounding box of the center line. */
    public int centerLine;

    /**
     * Construct a new {@code DocObjectLayoutBounds}.
     */
    public DocObjectLayoutBounds() {

        // No action
    }
}
