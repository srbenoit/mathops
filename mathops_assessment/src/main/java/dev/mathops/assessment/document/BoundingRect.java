package dev.mathops.assessment.document;

import dev.mathops.text.builder.HtmlBuilder;

/**
 * A bounding rectangle.
 */
public final class BoundingRect {

    /** The x coordinate. */
    private final double x;

    /** The y coordinate. */
    private final double y;

    /** The width. */
    private final double width;

    /** The height. */
    private final double height;

    /**
     * Construct a new {@code BoundingRect}.
     *
     * @param theX      the x coordinate
     * @param theY      the y coordinate
     * @param theWidth  the width
     * @param theHeight the height
     */
    public BoundingRect(final double theX, final double theY, final double theWidth, final double theHeight) {

        this.x = theX;
        this.y = theY;
        this.width = theWidth;
        this.height = theHeight;
    }

    /**
     * Gets the x coordinate.
     *
     * @return the x coordinate
     */
    public double getX() {

        return this.x;
    }

    /**
     * Gets the y coordinate.
     *
     * @return the y coordinate
     */
    public double getY() {

        return this.y;
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    public double getWidth() {

        return this.width;
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    public double getHeight() {

        return this.height;
    }

    /**
     * Appends XML attributes for this object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to append
     */
    public void appendXmlAttributes(final HtmlBuilder xml) {

        xml.addAttribute("x", Double.toString(this.x), 0);
        xml.addAttribute("y", Double.toString(this.y), 0);
        xml.addAttribute("width", Double.toString(this.width), 0);
        xml.addAttribute("height", Double.toString(this.height), 0);
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(70);

        builder.add("BoundingRect:x=", Double.toString(this.x), ",y=", Double.toString(this.y), ",width=",
                Double.toString(this.width), ",height=", Double.toString(this.height));

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return Double.hashCode(this.x) + Double.hashCode(this.y) + Double.hashCode(this.width)
                + Double.hashCode(this.height);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        // NOTE: We don't do a "Math.abs(x - y) < epsilon" comparison since that could result in two objects having
        // different hash codes, but still being considered equal, which violates the contract for hashCode.

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final BoundingRect rect) {
            equal = this.x == rect.x && this.y == rect.y && this.width == rect.width && this.height == rect.height;
        } else {
            equal = false;
        }

        return equal;
    }
}
