package dev.mathops.assessment.document;

import dev.mathops.core.builder.HtmlBuilder;

/**
 * A grid specification
 */
public final class GridSpec {

    /** The grid line width, in pixels. */
    private final int width;

    /** The grid color name. */
    private final String color;

    /**
     * Constructs a new {@code GridSpec}.
     *
     * @param theWidth the border width, in pixels
     * @param theColor the border color name
     */
    public GridSpec(final int theWidth, final String theColor) {

        if (theColor == null) {
            throw new IllegalArgumentException("Border color name may not be null");
        }

        this.width = theWidth;
        this.color = theColor;
    }

    /**
     * Gets the grid line width, in pixels.
     *
     * @return the grid line width
     */
    public int getWidth() {

        return this.width;
    }

    /**
     * Gets grid color name.
     *
     * @return the grid color name
     */
    public String getColor() {

        return this.color;
    }

    /**
     * Appends XML attributes for this object to an {@code HtmlBuilder}.
     *
     * @param xml the {@code HtmlBuilder} to which to append
     */
    public void appendXmlAttributes(final HtmlBuilder xml) {

        xml.addAttribute("gridwidth", Integer.toString(this.width), 0);
        xml.addAttribute("gridcolor", this.color, 0);
    }

    /**
     * Generate a diagnostic {@code String} representation of the object.
     *
     * @return the string representation     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(70);

        builder.add("GridSpec:width=", Integer.toString(this.width), ",color=", this.color);

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return this.width + this.color.hashCode();
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
        } else if (obj instanceof final GridSpec spec) {
            equal = this.width == spec.width
                    && this.color.equals(spec.color);
        } else {
            equal = false;
        }

        return equal;
    }
}
