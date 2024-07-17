package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An instance of a horizontal alignment object, in units of widths of a decimal "0" digit" (clamped to non-negative
 * values).
 */
public final class DocHAlignInst extends AbstractDocObjectInst {

    /** The position, as a number of widths of a decimal '0' digit. */
    private final double position;

    /**
     * Construct a new {@code DocHAlignInst} object.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param thePosition  the space width, in units of width of a decimal '0'
     */
    public DocHAlignInst(final DocObjectInstStyle theStyle, final String theBgColorName, final double thePosition) {

        super(theStyle, theBgColorName);

        this.position = Double.isFinite(thePosition) ? Math.max(0.0, thePosition) : 0.0;
    }

    /**
     * Gets the position.
     *
     * @return the position, in units of width of a decimal '0'
     */
    public double getPosition() {

        return this.position;
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

        xml.add("<h-align");
        final String positionStr = Double.toString(this.position);
        xml.addAttribute("position", positionStr, 0);
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

        builder.add("DocHAlignInst");
        final String positionStr = Double.toString(this.position);
        builder.add("[position=", positionStr, "]");
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

        return docObjectInstHashCode() + Double.hashCode(this.position);
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
        } else if (obj instanceof final DocHAlignInst spc) {
            equal = checkDocObjectInstEquals(spc) && this.position == spc.getPosition();
        } else {
            equal = false;
        }

        return equal;
    }
}
