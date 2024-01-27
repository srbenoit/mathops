package dev.mathops.assessment.document;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A bounding rectangle specified by four {@code Number} objects.
 */
public final class NumberBounds {

    /** The minimum x coordinate. */
    private final Number minX;

    /** The maximum x coordinate. */
    private final Number maxX;

    /** The minimum y coordinate. */
    private final Number minY;

    /** The maximum y coordinate. */
    private final Number maxY;

    /**
     * Constructs a new {@code NumberBounds}.
     *
     * @param theMinX the minimum x coordinate
     * @param theMaxX the maximum x coordinate
     * @param theMinY the minimum y coordinate
     * @param theMaxY the maximum y coordinate
     */
    public NumberBounds(final Number theMinX, final Number theMaxX, final Number theMinY, final Number theMaxY) {

        if (theMinX == null || theMaxX == null || theMinY == null || theMaxY == null) {
            throw new IllegalArgumentException("Bounds values may not be null");
        }

        this.minX = theMinX;
        this.maxX = theMaxX;
        this.minY = theMinY;
        this.maxY = theMaxY;
    }

    /**
     * Gets the minimum x coordinate.
     *
     * @return the minimum x coordinate
     */
    public Number getMinX() {

        return this.minX;
    }

    /**
     * Gets the maximum x coordinate.
     *
     * @return the maximum x coordinate
     */
    public Number getMaxX() {

        return this.maxX;
    }

    /**
     * Gets the minimum y coordinate.
     *
     * @return the minimum y coordinate
     */
    public Number getMinY() {

        return this.minY;
    }

    /**
     * Gets the maximum y coordinate.
     *
     * @return the maximum y coordinate
     */
    public Number getMaxY() {

        return this.maxY;
    }

    /**
     * Generate a diagnostic {@code String} representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(70);

        builder.add("NumberBounds:minX=", this.minX, ",maxX=", this.maxX, ",minY=", this.minY, ",maxY=", this.maxY);

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return this.minX.hashCode() + this.maxX.hashCode() + this.minY.hashCode() + this.maxY.hashCode();
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
            equal = this.minX.equals(bounds.minX)
                    && this.maxX.equals(bounds.maxX)
                    && this.minY.equals(bounds.minY)
                    && this.maxY.equals(bounds.maxY);
        } else {
            equal = false;
        }

        return equal;
    }
}
