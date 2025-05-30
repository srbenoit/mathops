package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Arrays;
import java.util.Objects;

/**
 * A polyline primitive.
 */
public final class DocPrimitivePolylineInst extends AbstractPrimitiveInst {

    /** The array of x coordinates. */
    private final double[] x;

    /** The array of y coordinates. */
    private final double[] y;

    /** The stroke style; {@code null} if not stroked. */
    private final StrokeStyleInst strokeStyle;

    /**
     * Construct a new {@code DocPrimitivePolylineInst}.
     *
     * @param theX           the array of x coordinates
     * @param theY           the array of y coordinates (must be same length as {@code x})
     * @param theStrokeStyle the stroke style
     */
    DocPrimitivePolylineInst(final double[] theX, final double[] theY, final StrokeStyleInst theStrokeStyle) {

        super();

        if (theX == null || theY == null || theX.length == 0 || theX.length != theY.length) {
            throw new IllegalArgumentException("X and Y coordinate arrays must be present, nonempty, the same size");
        }

        this.x = theX.clone();
        this.y = theY.clone();
        this.strokeStyle = theStrokeStyle;
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
    public StrokeStyleInst getStrokeStyle() {

        return this.strokeStyle;
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

        return Arrays.hashCode(this.x) + Arrays.hashCode(this.y) + Objects.hashCode(this.strokeStyle);
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
        } else if (obj instanceof final DocPrimitivePolylineInst polygon) {
            equal = Arrays.equals(this.x, polygon.x)
                    && Arrays.equals(this.y, polygon.y)
                    && Objects.equals(this.strokeStyle, polygon.strokeStyle);
        } else {
            equal = false;
        }

        return equal;
    }
}
