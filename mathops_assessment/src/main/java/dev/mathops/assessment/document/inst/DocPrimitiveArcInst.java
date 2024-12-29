package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EArcFillStyle;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.assessment.document.FillStyle;
import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An arc primitive.
 */
public final class DocPrimitiveArcInst extends AbstractPrimitiveInst {

    /** The rectangular shape. */
    private final RectangleShapeInst shape;

    /** The start angle, in degrees (zero is in the +x direction). */
    private final double startAngle;

    /** The arc angle, in degrees (positive is counter-clockwise). */
    private final double arcAngle;

    /** The arc stroke style; {@code null} if not stroked. */
    private final StrokeStyleInst strokeStyle;

    /** The fill style. */
    private final EArcFillStyle arcFill;

    /** The fill style; {@code null} if not filled. */
    private final FillStyle fillStyle;

    /**
     * Construct a new {@code DocPrimitiveArcInst}.
     *
     * @param theShape       the rectangular shape
     * @param theStartAngle  the start angle, in degrees
     * @param theArcAngle    the arc angle, in degrees
     * @param theStrokeStyle the stroke style
     * @param theArcFill     the fill type for the arc
     * @param theFillStyle   the fill style
     */
    public DocPrimitiveArcInst(final RectangleShapeInst theShape, final double theStartAngle, final double theArcAngle,
                               final StrokeStyleInst theStrokeStyle, final EArcFillStyle theArcFill,
                               final FillStyle theFillStyle) {

        super();

        if (theShape == null) {
            throw new IllegalArgumentException("Shape may not be null");
        }
        if (theArcFill == null) {
            throw new IllegalArgumentException("Arc fill type may not be null");
        }

        this.shape = theShape;
        this.startAngle = theStartAngle;
        this.arcAngle = theArcAngle;
        this.strokeStyle = theStrokeStyle;
        this.arcFill = theArcFill;
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
     * Gets the start angle, in degrees.
     *
     * @return the start angle
     */
    public double getStartAngle() {

        return this.startAngle;
    }

    /**
     * Gets the arc angle, in degrees.
     *
     * @return the arc angle
     */
    public double getArcAngle() {

        return this.arcAngle;
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
     * Gets the arc fill type.
     *
     * @return the fill type
     */
    public EArcFillStyle getArcFill() {

        return this.arcFill;
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
        xml.add("<arc");
        this.shape.addAttributes(xml);
        xml.addAttribute("start-angle", Double.toString(this.startAngle), 0);
        xml.addAttribute("arc-angle", Double.toString(this.arcAngle), 0);
        if (this.strokeStyle != null) {
            this.strokeStyle.appendXmlAttributes(xml, CoreConstants.EMPTY);
        }
        xml.addAttribute("arc-fill", this.arcFill, 0);
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

        builder.add("DocPrimitiveArcInst{", this.shape.toString(), ",startAngle=", Double.toString(this.startAngle),
                ",arcAngle=", Double.toString(this.arcAngle), ",arcFill=", this.arcFill);
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

        return this.shape.hashCode() + Double.hashCode(this.startAngle) + Double.hashCode(this.arcAngle)
               + Objects.hashCode(this.strokeStyle) + this.arcFill.hashCode()
               + Objects.hashCode(this.fillStyle);
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
        } else if (obj instanceof final DocPrimitiveArcInst arc) {
            equal = this.shape.equals(arc.shape)
                    && this.startAngle == arc.startAngle
                    && this.arcAngle == arc.arcAngle
                    && Objects.equals(this.strokeStyle, arc.strokeStyle)
                    && this.arcFill == arc.arcFill
                    && Objects.equals(this.fillStyle, arc.fillStyle);
        } else {
            equal = false;
        }

        return equal;
    }
}
