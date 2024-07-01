package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An instance of a run of horizontal space of specified width, in units of widths of a decimal "0" digit" (clamped to
 * non-negative values).
 */
public final class DocHSpaceInst extends AbstractDocObjectInst {

    /** The width, as a number of widths of a decimal '0' digit. */
    private final double spaceWidth;

    /**
     * Construct a new {@code DocHSpaceInst} object.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theSpaceWidth  the space width, in units of width of a decimal '0'
     */
    public DocHSpaceInst(final DocObjectInstStyle theStyle, final String theBgColorName, final double theSpaceWidth) {

        super(theStyle, theBgColorName);

        this.spaceWidth = Double.isFinite(theSpaceWidth) ? Math.max(0.0, theSpaceWidth) : 0.0;
    }

    /**
     * Gets the space width.
     *
     * @return the space width, in units of width of a decimal '0'
     */
    public double getSpaceWidth() {

        return this.spaceWidth;
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml      the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent   the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        xml.add("<h-space");
        final String widthStr = Double.toString(this.spaceWidth);
        xml.addAttribute("width", widthStr, 0);
        addDocObjectInstXmlAttributes(xml);
        xml.add("/>");
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocHSpaceInst");
        final String widthStr = Double.toString(this.spaceWidth);
        builder.add("[width=", widthStr, "]");
        appendStyleString(builder);

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return docObjectInstHashCode() + Double.hashCode(this.spaceWidth);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
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
        } else if (obj instanceof final DocHSpaceInst spc) {
            equal = checkDocObjectInstEquals(spc)
                    && this.spaceWidth == spc.getSpaceWidth();
        } else {
            equal = false;
        }

        return equal;
    }
}
