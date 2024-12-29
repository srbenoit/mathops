package dev.mathops.assessment.document;

import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A specification for tick marks on an axis within a graph.
 */
public final class AxisTicksSpec {

    /** The tick mark width, in pixels. */
    private final int tickWidth;

    /** The length of ticks on the positive side of the axis, in pixels. */
    private final int tickPosLength;

    /** The length of ticks on the negative side of the axis, in pixels. */
    private final int tickNegLength;

    /** The tick mark color name. */
    private final String tickColor;

    /** The interval between tick marks. */
    private final Number tickInterval;

    /** Font size to use when drawing tick labels (0 to hide). */
    private final float tickLabelSize;

    /** The color for tick mark labels. */
    private final String tickLabelColor;

    /**
     * Constructs a new {@code AxisTicksSpec}.
     *
     * @param theTickWidth      the tick mark width, in pixels
     * @param theTickPosLen     the tick mark positive length, in pixels
     * @param theTickNegLen     the tick mark negative length, in pixels
     * @param theTickColor      the tick mark color name
     * @param theTickInterval   the interval between tick marks
     * @param theTickLabelSize  the font size for tick labels (0 to hide labels)
     * @param theTickLabelColor the color for tick labels
     */
    public AxisTicksSpec(final int theTickWidth, final int theTickPosLen, final int theTickNegLen,
                         final String theTickColor, final Number theTickInterval, final float theTickLabelSize,
                         final String theTickLabelColor) {

        if (theTickColor == null) {
            throw new IllegalArgumentException("Tick mark color name may not be null");
        }
        if (theTickInterval == null) {
            throw new IllegalArgumentException("Tick interval may not be null");
        }

        this.tickWidth = theTickWidth;
        this.tickPosLength = theTickPosLen;
        this.tickNegLength = theTickNegLen;
        this.tickColor = theTickColor;
        this.tickInterval = theTickInterval;
        this.tickLabelSize = theTickLabelSize;
        this.tickLabelColor = theTickLabelColor;
    }

    /**
     * Gets the tick mark width, in pixels.
     *
     * @return the tick mark width
     */
    public int getTickWidth() {

        return this.tickWidth;
    }

    /**
     * Gets the tick mark length on the positive side of the axis.
     *
     * @return the positive-side tick mark length
     */
    public int getTickPosLen() {

        return this.tickPosLength;
    }

    /**
     * Gets the tick mark length on the negative side of the axis.
     *
     * @return the negative-side tick mark length
     */
    public int getTickNegLen() {

        return this.tickNegLength;
    }

    /**
     * Gets the tick mark color name.
     *
     * @return the tick mark color name
     */
    public String getTickColor() {

        return this.tickColor;
    }

    /**
     * Gets the tick mark spacing interval.
     *
     * @return the tick mark interval
     */
    public Number getTickInterval() {

        return this.tickInterval;
    }

    /**
     * Gets the tick label font point size.
     *
     * @return the tick label point size
     */
    public float getTickLabelSize() {

        return this.tickLabelSize;
    }

    /**
     * Gets the tick label color name.
     *
     * @return the tick label color name
     */
    public String getTickLabelColor() {

        return this.tickLabelColor;
    }

    /**
     * Appends XML attributes for this object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to append
     * @param prefix a prefix to prepend to attribute names, to distinguish multiple axes
     */
    public void appendXmlAttributes(final HtmlBuilder xml, final String prefix) {

        xml.addAttribute(prefix + "tickwidth", Integer.toString(this.tickWidth), 0);
        xml.addAttribute(prefix + "tickposlen", Integer.toString(this.tickPosLength), 0);
        xml.addAttribute(prefix + "tickneglen", Integer.toString(this.tickNegLength), 0);
        xml.addAttribute(prefix + "tickcolor", this.tickColor, 0);
        xml.addAttribute(prefix + "tickinterval", this.tickInterval, 0);
        if (this.tickLabelSize > 0.0f) {
            xml.addAttribute(prefix + "ticklabelsize", Float.toString(this.tickLabelSize), 0);
            xml.addAttribute(prefix + "ticklabelcolor", this.tickLabelColor, 0);

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

        builder.add("AxisTicksSpec:tickwidth=", Integer.toString(this.tickWidth), ",tickposlen=",
                Integer.toString(this.tickPosLength), ",tickneglen=", Integer.toString(this.tickNegLength),
                ",tickcolor=", this.tickColor, ",tickinterval=", this.tickInterval, ",ticklabelsize=",
                Float.toString(this.tickLabelSize), ",ticklabelcolor=", this.tickLabelColor);

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return this.tickWidth + this.tickPosLength << 12 + this.tickNegLength << 24 + this.tickColor.hashCode()
                + this.tickInterval.hashCode() + Float.hashCode(this.tickLabelSize)
                + Objects.hashCode(this.tickLabelColor);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        // NOTE: We don't do a "Math.abs(x - y) < epsilon" comparison since that could result in two object having
        // different hash codes, but still being considered equal, which violates the contract for hashCode.

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final AxisTicksSpec spec) {
            equal = this.tickWidth == spec.tickWidth
                    && this.tickPosLength == spec.tickPosLength
                    && this.tickNegLength == spec.tickNegLength
                    && this.tickColor.equals(spec.tickColor)
                    && this.tickInterval.equals(spec.tickInterval)
                    && this.tickLabelSize == spec.tickLabelSize
                    && Objects.equals(this.tickLabelColor, spec.tickLabelColor);
        } else {
            equal = false;
        }

        return equal;
    }
}
