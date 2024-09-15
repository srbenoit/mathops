package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EAngleUnits;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.builder.HtmlBuilder;

import java.awt.Color;

/**
 * A protractor primitive.
 */
public final class DocPrimitiveProtractorInst extends AbstractPrimitiveInst {

    /** The rectangular shape. */
    private final RectangleShapeInst shape;

    /** The orientation angle, in degrees */
    private final double orientation;

    /** The type of angle units to display. */
    private final EAngleUnits angleUnits;

    /** The number of quadrants to display (2 for half-circle, 4 for full circle, etc.) */
    private final int numQuadrants;

    /** The protractor color. */
    private final Color color;

    /** The text color. */
    private final Color textColor;

    /** The alpha. */
    private final double alpha;

    /**
     * Construct a new {@code DocPrimitiveProtractorInst}.
     *
     * @param theShape        the rectangular shape
     * @param theOrientation  the orientation
     * @param theAngleUnits   the angle units to display
     * @param theNumQuadrants the number of quadrants to display
     * @param theColor        the protractor color
     * @param theTextColor    the text color
     * @param theAlpha        the alpha
     */
    public DocPrimitiveProtractorInst(final RectangleShapeInst theShape, final double theOrientation,
                                      final EAngleUnits theAngleUnits, final int theNumQuadrants,
                                      final Color theColor, final Color theTextColor, final double theAlpha) {

        super();

        this.shape = theShape;
        this.orientation = theOrientation;
        this.angleUnits = theAngleUnits == null ? EAngleUnits.DEGREES : theAngleUnits;
        this.numQuadrants = theNumQuadrants;
        this.color = theColor;
        this.textColor = theTextColor;
        this.alpha = theAlpha;
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
     * Gets the orientation angle, in degrees.
     *
     * @return the orientation angle
     */
    public double getOrientation() {

        return this.orientation;
    }

    /**
     * Gets the angle units to display.
     *
     * @return the angle units
     */
    public EAngleUnits getAngleUnits() {

        return this.angleUnits;
    }

    /**
     * Gets the number of quadrants the protractor covers (1 to 4).
     *
     * @return the number of quadrants
     */
    public int getNumQuadrants() {

        return this.numQuadrants;
    }

    /**
     * Gets the protractor color.
     *
     * @return the protractor color
     */
    public Color getColor() {

        return this.color;
    }

    /**
     * Gets the text color.
     *
     * @return the text color
     */
    public Color getTextColor() {

        return this.textColor;
    }

    /**
     * Gets the alpha.
     *
     * @return the alpha
     */
    public double getAlpha() {

        return this.alpha;
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
        xml.add("<protractor");

        this.shape.addAttributes(xml);
        xml.add(" orientation='").add(this.orientation).add("'");

        if (this.angleUnits == EAngleUnits.DEGREES) {
            xml.add(" units='deg'");
        } else {
            xml.add(" units='rad'");
        }

        xml.add(" quadrants='").add(this.numQuadrants).add("'/>");
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(200);

        builder.add("<DocPrimitiveProtractorInst{", this.shape, " orientation=").add(this.orientation).add(" units=");
        if (this.angleUnits == EAngleUnits.DEGREES) {
            builder.add("deg");
        } else {
            builder.add("rad");
        }
        builder.add(" quadrants=").add(this.numQuadrants).add("}");

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return this.shape.hashCode() + Double.hashCode(this.orientation) + this.angleUnits.hashCode()
               + this.numQuadrants;
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
        } else if (obj instanceof final DocPrimitiveProtractorInst prot) {
            equal = this.shape.equals(prot.shape)
                    && this.orientation == prot.orientation
                    && this.angleUnits == prot.angleUnits
                    && this.numQuadrants == prot.numQuadrants;
        } else {
            equal = false;
        }

        return equal;
    }
}
