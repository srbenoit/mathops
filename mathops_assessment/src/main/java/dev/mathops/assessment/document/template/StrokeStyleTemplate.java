package dev.mathops.assessment.document.template;

import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.EStrokeCap;
import dev.mathops.assessment.document.EStrokeJoin;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Arrays;
import java.util.Objects;

/**
 * A stroke style template.
 */
final class StrokeStyleTemplate {

    /** An empty dash pattern. */
    private static final float[] NO_DASH = new float[0];

    /** The stroke width, in pixels. */
    private final NumberOrFormula strokeWidth;

    /** The stroke color name. */
    private final String strokeColorName;

    /** The dash pattern; {@code null} for a solid curve. */
    private final float[] dash;

    /** The alpha for the stroke. */
    private final NumberOrFormula alpha;

    /** The stroke cap style. */
    private final EStrokeCap cap;

    /** The stroke join style. */
    private final EStrokeJoin join;

    /** The miter limit for miter joins. */
    private final float miterLimit;

    /**
     * Constructs a new {@code StrokeStyleTemplate}.
     *
     * @param theStrokeWidth     the stroke width, in pixels
     * @param theStrokeColorName the stroke color name
     * @param theDash            the dash pattern; {@code null} or zero-length list for solid curve
     * @param theAlpha           the alpha for the stroke
     * @param theCap             the font size for the axis label
     * @param theJoin            the label color name; {@code null} to use axis color
     * @param theMiterLimit      the optional ticks
     */
    public StrokeStyleTemplate(final NumberOrFormula theStrokeWidth, final String theStrokeColorName,
                               final float[] theDash, final NumberOrFormula theAlpha, final EStrokeCap theCap,
                               final EStrokeJoin theJoin, final float theMiterLimit) {

        if (theStrokeColorName == null) {
            throw new IllegalArgumentException("Stroke color name may not be null");
        }
        if (theCap == null) {
            throw new IllegalArgumentException("Stroke cap style may not be null");
        }
        if (theJoin == null) {
            throw new IllegalArgumentException("Stroke join style may not be null");
        }

        this.strokeWidth = theStrokeWidth;
        this.strokeColorName = theStrokeColorName;
        this.dash = theDash == null ? NO_DASH : theDash.clone();
        this.alpha = theAlpha;
        this.cap = theCap;
        this.join = theJoin;
        this.miterLimit = theMiterLimit;
    }

    /**
     * Gets the stroke width, in pixels.
     *
     * @return the stroke width
     */
    public NumberOrFormula getStrokeWidth() {

        return this.strokeWidth;
    }

    /**
     * Gets stroke color name.
     *
     * @return the stroke color name
     */
    public String getStrokeColorName() {

        return this.strokeColorName;
    }

    /**
     * Gets the dash pattern.
     *
     * @return the dash pattern (a zero-length array to indicate a solid curve; never {@code null})
     */
    public float[] getDash() {

        return this.dash.clone();
    }

    /**
     * Gets the alpha for the stroke.
     *
     * @return the alpha
     */
    public NumberOrFormula getAlpha() {

        return this.alpha;
    }

    /**
     * Gets the stroke cap style.
     *
     * @return the cap style
     */
    public EStrokeCap getCap() {

        return this.cap;
    }

    /**
     * Gets the stroke join style.
     *
     * @return the join style
     */
    public EStrokeJoin getJoin() {

        return this.join;
    }

    /**
     * Gets the miter limit for the miter join style.
     *
     * @return the miter limit
     */
    public float getMiterLimit() {

        return this.miterLimit;
    }

    /**
     * Appends XML attributes that represent this object's attribute values.  This should be called after the open tag
     * has been stated, such as by emitting ("&lt;rectangle").
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param prefix the prefix for attribute values, for objects with multiple strokes
     */
    void addAttributes(final HtmlBuilder xml, final String prefix) {

        if (this.strokeWidth != null && this.strokeWidth.getNumber() instanceof final Number strokeWidthNumber) {
            xml.addAttribute(prefix + "strokewidth", strokeWidthNumber, 0);
        }

        xml.addAttribute(prefix + "strokecolor", this.strokeColorName, 0);

        final int dashLen = this.dash.length;
        if (dashLen > 0) {
            final HtmlBuilder pattern = new HtmlBuilder(40);
            pattern.add(this.dash[0]);
            for (int i = 1; i < dashLen; ++i) {
                pattern.add(',');
                pattern.add(this.dash[i]);
            }
            xml.addAttribute(prefix + "strokedash", pattern.toString(), 0);
        }

        if (this.alpha != null && this.alpha.getNumber() instanceof final Number alphaNumber) {
            final double alphaValue = alphaNumber.doubleValue();
            if (Math.abs(alphaValue - 1.0) > 0.01) {
                xml.addAttribute(prefix + "strokealpha", alphaNumber, 0);
            }
        }

        if (this.cap != EStrokeCap.BUTT) {
            xml.addAttribute(prefix + "strokecap", this.cap, 0);
        }

        if (this.join != EStrokeJoin.MITER) {
            xml.addAttribute(prefix + "strokejoin", this.join, 0);
        } else if (Math.abs(this.miterLimit - 10.0f) > 0.01f) {
            xml.addAttribute(prefix + "strokemiterlimit", Float.toString(this.miterLimit), 0);
        }
    }

    /**
     * Appends elements that represent this object's expression-based values.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param prefix the prefix for attribute values, for objects with multiple strokes
     * @param indent the indentation level
     */
    void addChildElements(final HtmlBuilder xml, final String prefix, final int indent) {

        if (this.strokeWidth != null && this.strokeWidth.getFormula() instanceof final Formula strokeWidthFormula) {
            final String tag = prefix + "strokewidth";
            xml.openElement(indent, tag);
            xml.endOpenElement(false);
            strokeWidthFormula.appendChildrenXml(xml);
            xml.endNonempty(indent, tag, true);
        }

        if (this.alpha != null && this.alpha.getFormula() instanceof final Formula alphaFormula) {
            final String tag = prefix + "strokealpha";
            xml.openElement(indent, tag);
            xml.endOpenElement(false);
            alphaFormula.appendChildrenXml(xml);
            xml.endNonempty(indent, tag, true);
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

        builder.add("StrokeStyle:");
        if (this.strokeWidth != null) {
            builder.add(" strokewidth=", this.strokeWidth);
        }
        if (this.strokeColorName != null) {
            builder.add(" strokecolor=", this.strokeColorName);
        }

        if (this.dash != null) {
            final int dashLen = this.dash.length;
            if (dashLen > 0) {
                final HtmlBuilder pattern = new HtmlBuilder(40);
                pattern.add(this.dash[0]);
                for (int i = 1; i < dashLen; ++i) {
                    pattern.add(',');
                    pattern.add(this.dash[i]);
                }
                builder.add(" strokedash=", pattern.toString());
            }
        }

        if (this.alpha != null) {
            builder.add(" strokealpha=", this.alpha);
        }

        if (this.cap != EStrokeCap.BUTT) {
            builder.add(" strokecap=", this.cap);
        }

        if (this.join != EStrokeJoin.MITER) {
            builder.add(" strokejoin=", this.join);
        } else if (Math.abs(this.miterLimit - 10.0f) > 0.01f) {
            builder.add(" strokemiterlimit=", Float.toString(this.miterLimit));
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

        return Objects.hashCode(this.strokeWidth) + this.strokeColorName.hashCode() + Arrays.hashCode(this.dash)
               + Objects.hashCode(this.alpha) + this.cap.hashCode() + this.join.hashCode()
               + Float.hashCode(this.miterLimit);
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
        } else if (obj instanceof final StrokeStyleTemplate spec) {
            equal = Objects.equals(this.strokeWidth, spec.strokeWidth)
                    && this.strokeColorName.equals(spec.strokeColorName)
                    && Arrays.equals(this.dash, spec.dash)
                    && Objects.equals(this.alpha, spec.alpha)
                    && this.cap == spec.cap
                    && this.join == spec.join
                    && this.miterLimit == spec.miterLimit;
        } else {
            equal = false;
        }

        return equal;
    }
}
