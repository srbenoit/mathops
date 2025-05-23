package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An instance of whitespace in text.  The width could be adjusted to achieve full justification.
 */
public final class DocWhitespaceInst extends AbstractDocObjectInst {

    /**
     * Construct a new {@code DocWhitespaceInst} object.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     */
    public DocWhitespaceInst(final DocObjectInstStyle theStyle, final String theBgColorName) {

        super(theStyle, theBgColorName);
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

        xml.add("<ws");
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

        builder.add("DocWhitespaceInst");
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

        return docObjectInstHashCode();
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
        } else if (obj instanceof final DocWhitespaceInst ws) {
            equal = checkDocObjectInstEquals(ws);
        } else {
            equal = false;
        }

        return equal;
    }
}
