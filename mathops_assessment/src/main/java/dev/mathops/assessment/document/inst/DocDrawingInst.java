package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.CoordinateSystems;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.List;

/**
 * A drawing in a document.
 */
public final class DocDrawingInst extends AbstractPrimitiveContainerInst {

    /**
     * Construct a new {@code DocDrawingInst}.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theWidth       the width of the object
     * @param theHeight      the height of the object
     * @param theCoordinates the coordinate systems in which coordinates can be specified
     * @param theAltText     the alternative text for accessibility of generated images
     * @param theBorder      the border specification; {@code null} if there is no border
     * @param thePrimitives  the list of primitives
     */
    public DocDrawingInst(final DocObjectInstStyle theStyle, final String theBgColorName, final int theWidth,
                          final int theHeight, final CoordinateSystems theCoordinates, final String theAltText,
                          final StrokeStyleInst theBorder, final List<? extends AbstractPrimitiveInst> thePrimitives) {

        super(theStyle, theBgColorName, theWidth, theHeight, theCoordinates, theAltText, theBorder, thePrimitives);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        final String ind = makeIndent(indent);

        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.add(ind);
        }
        xml.add("<drawing");
        addPrimitiveContainerInstXmlAttributes(xml); // width, height, border, bgColor, style attributes
        xml.add('>');
        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.addln();
        }
        appendPrimitivesXml(xml, xmlStyle, indent + 1);
        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.addln();
            xml.add(ind);
        }
        xml.add("</drawing>");
        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.addln();
        }
    }

    /**
     * Generate a {@code String} representation of the image (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocDrawingInst");
        appendPrimitiveContainerString(builder);
        builder.add(':');

        boolean comma = false;
        for (final AbstractPrimitiveInst primitive : getPrimitives()) {
            if (comma) {
                builder.add(',');
            }
            builder.add(primitive.toString());
            comma = true;
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

        return docPrimitiveContainerInstHashCode();
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DocDrawingInst drw) {
            equal = checkDocPrimitiveContainerInstEquals(drw);
        } else {
            equal = false;
        }

        return equal;
    }
}
