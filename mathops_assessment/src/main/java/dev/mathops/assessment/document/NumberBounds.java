package dev.mathops.assessment.document;

import dev.mathops.text.builder.SimpleBuilder;

/**
 * An immutable bounding rectangle specified by four {@code Number} objects.
 */
public final class NumberBounds {

    /** The x coordinate at the left edge. */
    private final Number leftX;

    /** The x coordinate at the right edge. */
    private final Number rightX;

    /** The y coordinate at the bottom edge. */
    private final Number bottomY;

    /** The y coordinate at the top edge. */
    private final Number topY;

    /**
     * Constructs a new {@code NumberBounds}.
     *
     * @param theLeftX   the x coordinate at the left edge
     * @param theRightX  the x coordinate at the right edge
     * @param theBottomY the y coordinate at the bottom edge
     * @param theTopY    the y coordinate at the top edge
     */
    public NumberBounds(final Number theLeftX, final Number theRightX, final Number theBottomY, final Number theTopY) {

        if (theLeftX == null || theRightX == null || theBottomY == null || theTopY == null) {
            throw new IllegalArgumentException("Bounds values may not be null");
        }

        this.leftX = theLeftX;
        this.rightX = theRightX;
        this.bottomY = theBottomY;
        this.topY = theTopY;
    }

    /**
     * Gets the x coordinate at the left edge.
     *
     * @return the x coordinate at the left edge
     */
    public Number getLeftX() {

        return this.leftX;
    }

    /**
     * Gets the x coordinate at the right edge.
     *
     * @return the x coordinate at the right edge
     */
    public Number getRightX() {

        return this.rightX;
    }

    /**
     * Gets the y coordinate at the bottom edge.
     *
     * @return the y coordinate at the bottom edge
     */
    public Number getBottomY() {

        return this.bottomY;
    }

    /**
     * Gets the y coordinate at the top edge.
     *
     * @return the y coordinate at the top edge
     */
    public Number getTopY() {

        return this.topY;
    }

    /**
     * Generate a diagnostic {@code String} representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("NumberBounds:leftX=", this.leftX, ",rightX=", this.rightX, ",bottomY=",
                this.bottomY, ",topY=", this.topY);
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return this.leftX.hashCode() + this.rightX.hashCode() + this.bottomY.hashCode() + this.topY.hashCode();
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final NumberBounds bounds) {
            equal = this.leftX.equals(bounds.leftX)
                    && this.rightX.equals(bounds.rightX)
                    && this.bottomY.equals(bounds.bottomY)
                    && this.topY.equals(bounds.topY);
        } else {
            equal = false;
        }

        return equal;
    }
}
