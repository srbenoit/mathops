package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.assessment.document.FillStyle;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;

import java.util.Arrays;
import java.util.Objects;

/**
 * A polygon primitive.
 */
public final class DocPrimitivePolygonInst extends AbstractPrimitiveInst {

    /** The array of x coordinates. */
    private final double[] x;

    /** The array of y coordinates. */
    private final double[] y;

    /** The stroke style; {@code null} if not stroked. */
    private final StrokeStyle strokeStyle;

    /** The fill style; {@code null} if not filled. */
    private final FillStyle fillStyle;

    /**
     * Construct a new {@code DocPrimitivePolygonInst}.
     *
     * @param theX           the array of x coordinates
     * @param theY           the array of y coordinates (must be same length as {@code x})
     * @param theStrokeStyle the stroke style
     * @param theFillStyle   the fill style
     */
    public DocPrimitivePolygonInst(final double[] theX, final double[] theY, final StrokeStyle theStrokeStyle,
                            final FillStyle theFillStyle) {

        super();

        if (theX == null || theY == null || theX.length == 0 || theX.length != theY.length) {
            throw new IllegalArgumentException("X and Y coordinate arrays must be present, nonempty, the same size");
        }

        this.x = theX.clone();
        this.y = theY.clone();
        this.strokeStyle = theStrokeStyle;
        this.fillStyle = theFillStyle;
    }

    /**
     * Gets the array of x coordinates.
     *
     * @return a copy of the x coordinate array
     */
    public double[] getX() {

        return this.x.clone();
    }

    /**
     * Gets the array of y coordinates.
     *
     * @return a copy of the y coordinate array
     */
    public double[] getY() {

        return this.y.clone();
    }

    /**
     * Gets the stroke style.
     *
     * @return the stroke style; {@code null} if not stroked
     */
    public StrokeStyle getStrokeStyle() {

        return this.strokeStyle;
    }

    /**
     * Gets the fill style.
     *
     * @return the fill style; {@code null} if not filled
     */
    public FillStyle getFillStyle() {

        return this.fillStyle;
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent the number of spaces to indent the printout
     */
    public void toXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        final String ind = makeIndent(indent);
        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.add(ind);
        }
        xml.add("<polygon");

        final HtmlBuilder builder = new HtmlBuilder(this.x.length * 20);
        final int count = this.x.length;

        builder.add(this.x[0]);
        for (int i = 1; i < count; ++i) {
            builder.add(',');
            builder.add(this.x[i]);
        }
        xml.addAttribute("x", builder.toString(), 0);

        builder.reset();
        builder.add(this.y[0]);
        for (int i = 1; i < count; ++i) {
            builder.add(',');
            builder.add(this.y[i]);
        }
        xml.addAttribute("y", builder.toString(), 0);

        if (this.strokeStyle != null) {
            this.strokeStyle.appendXmlAttributes(xml, CoreConstants.EMPTY);
        }
        if (this.fillStyle != null) {
            this.fillStyle.appendXmlAttributes(xml, CoreConstants.EMPTY);
        }

        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.addln("/>");
        } else {
            xml.add("/>");
        }
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(200);
        final int count = this.x.length;

        builder.add("DocPrimitivePolygonInst{x=");

        builder.add(this.x[0]);
        for (int i = 1; i < count; ++i) {
            builder.add(',');
            builder.add(this.x[i]);
        }

        builder.add(",y=");
        builder.add(this.y[0]);
        for (int i = 1; i < count; ++i) {
            builder.add(',');
            builder.add(this.y[i]);
        }

        if (this.strokeStyle != null) {
            builder.add(",", this.strokeStyle);
        }
        if (this.fillStyle != null) {
            builder.add(",", this.fillStyle);
        }
        builder.add('}');

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return Arrays.hashCode(this.x) + Arrays.hashCode(this.y) + EqualityTests.objectHashCode(this.strokeStyle)
                + EqualityTests.objectHashCode(this.fillStyle);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DocPrimitivePolygonInst polygon) {
            equal = Arrays.equals(this.x, polygon.x)
                    && Arrays.equals(this.y, polygon.y)
                    && Objects.equals(this.strokeStyle, polygon.strokeStyle)
                    && Objects.equals(this.fillStyle, polygon.fillStyle);
        } else {
            equal = false;
        }

        return equal;
    }
}
