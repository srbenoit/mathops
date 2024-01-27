package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.BoundingRect;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A line primitive.
 */
public final class DocPrimitiveLineInst extends AbstractPrimitiveInst {

    /** The bounding rectangle - the line is drawn from (x, y) to (x + width, y + height). */
    private final BoundingRect bounds;

    /** The stroke style. */
    private final StrokeStyle strokeStyle;

    /**
     * Construct a new {@code DocPrimitiveLineInst}.
     *
     * @param theBounds      the bounding rectangle - the line is drawn from (x, y) to (x + width, y + height)
     * @param theStrokeStyle the stroke style
     */
    public DocPrimitiveLineInst(final BoundingRect theBounds, final StrokeStyle theStrokeStyle) {

        super();

        this.bounds = theBounds;
        this.strokeStyle = theStrokeStyle;
    }

    /**
     * Gets the bounding rectangle.
     *
     * @return the bounding rectangle - the line is drawn from (x, y) to (x + width, y + height)
     */
    public BoundingRect getBounds() {

        return this.bounds;
    }

    /**
     * Gets the stroke style.
     *
     * @return the stroke style
     */
    public StrokeStyle getStrokeStyle() {

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
        xml.add("<line");
        this.bounds.appendXmlAttributes(xml);
        this.strokeStyle.appendXmlAttributes(xml, CoreConstants.EMPTY);

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

        builder.add("DocPrimitiveLineInst{", this.bounds.toString(), ",", this.strokeStyle, "}");

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return this.bounds.hashCode() + this.strokeStyle.hashCode();
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
        } else if (obj instanceof final DocPrimitiveLineInst line) {
            equal = this.bounds.equals(line.bounds) && this.strokeStyle.equals(line.strokeStyle);
        } else {
            equal = false;
        }

        return equal;
    }
}
