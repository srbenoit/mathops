package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.assessment.document.FillStyle;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A rectangle primitive.
 */
public final class DocPrimitiveRectangleInst extends AbstractPrimitiveInst {

    /** The rectangular shape. */
    private final RectangleShapeInst shape;

    /** The stroke style, {@code null} if not stroked. */
    private final StrokeStyle strokeStyle;

    /** The fill style; {@code null} if not filled. */
    private final FillStyle fillStyle;

    /**
     * Construct a new {@code DocPrimitiveRectangleInst}.
     *
     * @param theShape       the rectangular shape
     * @param theStrokeStyle the stroke style; {@code null} if not stroked
     * @param theFillStyle   the fill style; {@code null} if not filled
     */
    public DocPrimitiveRectangleInst(final RectangleShapeInst theShape, final StrokeStyle theStrokeStyle,
                                     final FillStyle theFillStyle) {

        super();

        if (theShape == null) {
            throw new IllegalArgumentException("Shape may not be null");
        }

        this.shape = theShape;
        this.strokeStyle = theStrokeStyle;
        this.fillStyle = theFillStyle;
    }

    /**
     * Gets the rectangular shape.
     *
     * @return the rectangular shape
     */
    public RectangleShapeInst getShape() {

        return this.shape;
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
     * @param xml      the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent   the number of spaces to indent the printout
     */
    public void toXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        final String ind = makeIndent(indent);
        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.add(ind);
        }
        xml.add("<rectangle");
        this.shape.addAttributes(xml);
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

        builder.add("DocPrimitiveRectangleInst{", this.shape);
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

        return this.shape.hashCode() + Objects.hashCode(this.strokeStyle) + Objects.hashCode(this.fillStyle);
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
        } else if (obj instanceof final DocPrimitiveRectangleInst rect) {
            equal = this.shape.equals(rect.shape)
                    && Objects.equals(this.strokeStyle, rect.strokeStyle)
                    && Objects.equals(this.fillStyle, rect.fillStyle);
        } else {
            equal = false;
        }

        return equal;
    }
}
