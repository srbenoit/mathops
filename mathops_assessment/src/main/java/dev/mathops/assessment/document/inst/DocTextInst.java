package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An item of text in a paragraph.
 */
public final class DocTextInst extends AbstractDocObjectInst {

    /** The text. */
    private final String text;

    /**
     * Construct a new {@code DocTextInst} object.
     *
     * @param theStyle         the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName   the background color name ({@code null} if transparent)
     * @param theText          the text
     */
    public DocTextInst(final DocObjectInstStyle theStyle, final String theBgColorName, final String theText) {

        super(theStyle, theBgColorName);

        if (theText == null) {
            throw new IllegalArgumentException("Text may not be null");
        }

        this.text = theText;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {

        return this.text;
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml      the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent   the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        final String ind = makeIndent(indent);
        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.add(ind);
        }

        xml.add("<text");
        addDocObjectInstXmlAttributes(xml);
        xml.addAttribute("text", this.text, 0);
        xml.add("/>");
    }

    /**
     * Generate a {@code String} representation of the fraction (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("Text");
        appendStyleString(builder);
        builder.add("{", this.text, "}");

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return docObjectInstHashCode() + this.text.hashCode();
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
        } else if (obj instanceof final DocTextInst txt) {
            equal = checkDocObjectInstEquals(txt) && this.text.equals(txt.text);
        } else {
            equal = false;
        }

        return equal;
    }
}
