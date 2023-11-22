package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.core.builder.HtmlBuilder;

/**
 * An instance of a run of vertical space of specified height, in units of "ems" in the current font.
 */
public final class DocVSpaceInst extends AbstractDocObjectInst {

    /** The height, as a number of "ems". */
    private final double spaceHeight;

    /**
     * Construct a new {@code DocVSpaceInst} object.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theSpaceHeight  the space height, in units of "ems"
     */
    public DocVSpaceInst(final DocObjectInstStyle theStyle, final String theBgColorName, final double theSpaceHeight) {

        super(theStyle, theBgColorName);

        this.spaceHeight = Double.isFinite(theSpaceHeight) ? Math.max(0.0, theSpaceHeight) : 0.0;
    }

    /**
     * Gets the space height.
     *
     * @return the space height, in units of "ems""
     */
    public double getSpaceHeight() {

        return this.spaceHeight;
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

        xml.add("<v-space");
        xml.addAttribute("height", Double.valueOf(this.spaceHeight), 0);
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

        builder.add("DocVSpaceInst");
        builder.add("[height=", Double.toString(this.spaceHeight), "]");
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

        return docObjectInstHashCode() + Double.hashCode(this.spaceHeight);
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
        } else if (obj instanceof final DocVSpaceInst spc) {
            equal = checkDocObjectInstEquals(spc)
                    && this.spaceHeight == spc.spaceHeight;
        } else {
            equal = false;
        }

        return equal;
    }
}
