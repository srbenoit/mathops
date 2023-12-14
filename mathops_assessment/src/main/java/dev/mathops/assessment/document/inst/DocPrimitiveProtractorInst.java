package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EAngleUnits;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.builder.SimpleBuilder;

import java.awt.Color;
import java.util.Objects;

/**
 * A protractor primitive.
 */
public final class DocPrimitiveProtractorInst extends AbstractPrimitiveInst {

    /** The center point x coordinate. */
    private final double centerX;

    /** The center point y coordinate. */
    private final double centerY;

    /** The radius. */
    private final double radius;

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
     * @param theCenterX      the center point x coordinate
     * @param theCenterY      the center point y coordinate
     * @param theRadius       the radius
     * @param theOrientation  the orientation
     * @param theAngleUnits   the angle units to display
     * @param theNumQuadrants the number of quadrants to display
     * @param theColor        the protractor color
     * @param theTextColor    the text color
     * @param theAlpha        the alpha
     */
    public DocPrimitiveProtractorInst(final double theCenterX, final double theCenterY, final double theRadius,
                                      final double theOrientation, final EAngleUnits theAngleUnits,
                                      final int theNumQuadrants, final Color theColor, final Color theTextColor,
                                      final double theAlpha) {

        super();

        this.centerX = theCenterX;
        this.centerY = theCenterY;
        this.radius = theRadius;
        this.orientation = theOrientation;
        this.angleUnits = theAngleUnits == null ? EAngleUnits.DEGREES : theAngleUnits;
        this.numQuadrants = theNumQuadrants;
        this.color = theColor;
        this.textColor = theTextColor;
        this.alpha = theAlpha;
    }

    /**
     * Gets the center point X coordinate.
     *
     * @return the center point X coordinate
     */
    public double getCenterX() {

        return this.centerX;
    }

    /**
     * Gets the center point Y coordinate.
     *
     * @return the center point Y coordinate
     */
    public double getCenterY() {

        return this.centerY;
    }

    /**
     * Gets the radius.
     *
     * @return the radius
     */
    public double getRadius() {

        return this.radius;
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
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent the number of spaces to indent the printout
     */
    public void toXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        final String ind = makeIndent(indent);
        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.add(ind);
        }
        xml.add("<protractor center-x='").add(this.centerX).add("' center-y='").add(this.centerY).add("' radius='")
                .add(this.radius).add("' orientation='").add(this.orientation).add("'");

        if (this.angleUnits == EAngleUnits.DEGREES) {
            xml.add(" units='deg'");
        } else if (this.angleUnits == EAngleUnits.RADIANS) {
            xml.add(" units='rad'");
        } else  {
            xml.add(" units='both'");
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

        builder.add("<DocPrimitiveProtractorInst{center-x=").add(this.centerX).add(" center-y=").add(this.centerY)
                .add(" radius=").add(this.radius).add(" orientation=").add(this.orientation).add(" units=");
        if (this.angleUnits == EAngleUnits.DEGREES) {
            builder.add("deg");
        } else if (this.angleUnits == EAngleUnits.RADIANS) {
            builder.add("rad");
        } else  {
            builder.add("both");
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

        return Double.hashCode(this.centerX) + Double.hashCode(this.centerY)
                + Double.hashCode(this.radius) + Double.hashCode(this.orientation)
                + this.angleUnits.hashCode() + this.numQuadrants;
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
            equal = this.centerX == prot.centerX
                    && this.centerY == prot.centerY
                    && this.radius == prot.radius
                    && this.orientation == prot.orientation
                    && this.angleUnits == prot.angleUnits
                    && this.numQuadrants == prot.numQuadrants;
        } else {
            equal = false;
        }

        return equal;
    }
}
