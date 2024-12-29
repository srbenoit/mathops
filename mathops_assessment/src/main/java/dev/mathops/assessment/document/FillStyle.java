package dev.mathops.assessment.document;

import dev.mathops.text.builder.HtmlBuilder;

/**
 * A specification of a fill style.
 */
public final class FillStyle {

    /** The stroke color name. */
    private final String fillColorName;

    /** The alpha for the fill. */
    private final double alpha;

    /**
     * Constructs a new {@code FillStyle}.
     *
     * @param theFillColorName the fill color name
     * @param theAlpha         the alpha for the fill
     */
    public FillStyle(final String theFillColorName, final double theAlpha) {

        if (theFillColorName == null) {
            throw new IllegalArgumentException("Fill color name may not be null");
        }

        this.fillColorName = theFillColorName;
        this.alpha = theAlpha;
    }

    /**
     * Gets fill color name.
     *
     * @return the fill color name
     */
    public String getFillColorName() {

        return this.fillColorName;
    }
    /**
     * Gets the alpha for the stroke.
     *
     * @return the alpha
     */
    public double getAlpha() {

        return this.alpha;
    }

    /**
     * Appends XML attributes for this object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to append
     * @param prefix a prefix to prepend to attribute names
     */
    public void appendXmlAttributes(final HtmlBuilder xml, final String prefix) {

        xml.addAttribute(prefix + "fillcolor", this.fillColorName, 0);
        if (Math.abs(this.alpha - 1.0) > 0.01) {
            xml.addAttribute(prefix + "fillalpha", Double.toString(this.alpha), 0);
        }
    }

    /**
     * Generate a diagnostic {@code String} representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(70);

        builder.add("FillStyle:fillcolor=", this.fillColorName);
        if (Math.abs(this.alpha - 1.0) > 0.01) {
            builder.add(",fillalpha=", Double.toString(this.alpha));
        }

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return this.fillColorName.hashCode() + Double.hashCode(this.alpha);
    }

    /**
     * Tests whether this object is equal to another.
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
        } else if (obj instanceof final FillStyle spec) {
            equal = this.fillColorName.equals(spec.fillColorName) && this.alpha == spec.alpha;
        } else {
            equal = false;
        }

        return equal;
    }
}
