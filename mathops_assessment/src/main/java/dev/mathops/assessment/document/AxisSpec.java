package dev.mathops.assessment.document;

import dev.mathops.assessment.document.inst.StrokeStyleInst;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A specification for an axis within a graph.
 */
public final class AxisSpec {

    /** The axis color name. */
    private final StrokeStyleInst axisStyle;

    /** The axis label; {@code null} if no label. */
    private final String label;

    /** The axis label font size. */
    private final float labelSize;

    /** The axis label color name. */
    private final String labelColor;

    /** Optional tick marks. */
    private final AxisTicksSpec ticks;

    /**
     * Constructs a new {@code AxisSpec}.
     *
     * @param theAxisStyle  the axis style
     * @param theLabel      the axis label
     * @param theLabelSize  the font size for the axis label
     * @param theLabelColor the label color name; {@code null} to use axis color
     * @param theTicks      the optional ticks
     */
    public AxisSpec(final StrokeStyleInst theAxisStyle, final String theLabel,
                    final float theLabelSize, final String theLabelColor, final AxisTicksSpec theTicks) {

        if (theAxisStyle == null) {
            throw new IllegalArgumentException("Axis style may not be null");
        }

        this.axisStyle = theAxisStyle;
        this.label = theLabel;
        this.labelSize = theLabelSize;
        this.labelColor = theLabelColor;
        this.ticks = theTicks;
    }

    /**
     * Gets the axis style.
     *
     * @return the axis style
     */
    public StrokeStyleInst getAxisStyle() {

        return this.axisStyle;
    }

    /**
     * Gets axis label.
     *
     * @return the axis label
     */
    public String getLabel() {

        return this.label;
    }

    /**
     * Gets axis label font size.
     *
     * @return the axis label font size
     */
    public float getLabelSize() {

        return this.labelSize;
    }

    /**
     * Gets axis label color name.
     *
     * @return the axis label color name
     */
    public String getLabelColor() {

        return this.labelColor;
    }

    /**
     * Gets the tick mark specification.
     *
     * @return the tick mark specification
     */
    public AxisTicksSpec getTicks() {

        return this.ticks;
    }

    /**
     * Appends XML attributes for this object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to append
     * @param prefix a prefix to prepend to attribute names, to distinguish multiple axes
     */
    public void appendXmlAttributes(final HtmlBuilder xml, final String prefix) {

        this.axisStyle.appendXmlAttributes(xml, prefix + "axis");
        if (this.label != null) {
            xml.addAttribute(prefix + "axislabel", this.label, 0);
            xml.addAttribute(prefix + "axislabelsize", Float.toString(this.labelSize), 0);
        }

        if (this.ticks != null) {
            this.ticks.appendXmlAttributes(xml, prefix);
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

        builder.add("AxisSpec:style={", this.axisStyle, "}");
        if (this.label != null) {
            builder.add(",label=", this.label, ",labelsize=", Float.toString(this.labelSize));
        }
        if (this.ticks != null) {
            builder.add(this.ticks);
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

        return this.axisStyle.hashCode() + Objects.hashCode(this.label) + Float.hashCode(this.labelSize)
                + Objects.hashCode(this.labelColor) + Objects.hashCode(this.ticks);
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
        } else if (obj instanceof final AxisSpec spec) {
            equal = this.axisStyle.equals(spec.axisStyle)
                    && Objects.equals(this.label, spec.label)
                    && this.labelSize == spec.labelSize
                    && Objects.equals(this.labelColor, spec.labelColor)
                    && Objects.equals(this.ticks, spec.ticks);
        } else {
            equal = false;
        }

        return equal;
    }
}
