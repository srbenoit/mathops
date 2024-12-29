package dev.mathops.assessment.document;

import dev.mathops.text.builder.HtmlBuilder;

/**
 * Padding settings.
 */
public final class Padding {

    /** The x coordinate. */
    private final double left;

    /** The width. */
    private final double top;

    /** The y coordinate. */
    private final double right;

    /** The height. */
    private final double bottom;

    /**
     * Construct a new {@code Padding}.
     *
     * @param theLeft   the left padding
     * @param theTop    the top padding
     * @param theRight  the right padding
     * @param theBottom the bottom padding
     */
    public Padding(final double theLeft, final double theTop, final double theRight, final double theBottom) {

        this.left = theLeft;
        this.top = theTop;
        this.right = theRight;
        this.bottom = theBottom;
    }

    /**
     * Gets the left padding.
     *
     * @return the left padding
     */
    public double getLeft() {

        return this.left;
    }

    /**
     * Gets the top padding.
     *
     * @return the top padding
     */
    public double getTop() {

        return this.top;
    }

    /**
     * Gets the right padding.
     *
     * @return the right padding
     */
    public double getRight() {

        return this.right;
    }

    /**
     * Gets the bottom padding.
     *
     * @return the bottom padding
     */
    public double getBottom() {

        return this.bottom;
    }

    /**
     * Appends XML attributes for this object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to append
     * @param prefix a prefix to emit on attribute names
     */
    public void appendXmlAttributes(final HtmlBuilder xml, final String prefix) {

        xml.addAttribute(prefix + "left", Double.toString(this.left), 0);
        xml.addAttribute(prefix + "top", Double.toString(this.top), 0);
        xml.addAttribute(prefix + "right", Double.toString(this.right), 0);
        xml.addAttribute(prefix + "bottom", Double.toString(this.bottom), 0);
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(70);

        builder.add("Padding:left=", Double.toString(this.left),  ",top=", Double.toString(this.top), ",right=",
                Double.toString(this.right), ",bottom=", Double.toString(this.bottom));

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return Double.hashCode(this.left) + Double.hashCode(this.top) + Double.hashCode(this.right)
                + Double.hashCode(this.bottom);
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
        } else if (obj instanceof final Padding pad) {
            equal = this.left == pad.left && this.top == pad.top && this.right == pad.right
                    && this.bottom == pad.bottom;
        } else {
            equal = false;
        }

        return equal;
    }
}
