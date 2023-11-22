package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.core.builder.HtmlBuilder;

import java.util.List;

/**
 * A series of document objects that will be rendered without line wrap. Objects will be laid out using mathematical
 * formatting. Some characters will automatically be rendered in italics.
 */
public final class DocMathSpanInst extends AbstractDocContainerInst {

    /**
     * Construct a new {@code DocMathSpanInst}.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theChildren    the list of child objects
     */
    public DocMathSpanInst(final DocObjectInstStyle theStyle, final String theBgColorName,
                           final List<? extends AbstractDocObjectInst> theChildren) {

        super(theStyle, theBgColorName, theChildren);
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

        xml.add("<math");
        addDocObjectInstXmlAttributes(xml);
        xml.add('>');

        appendChildrenXml(xml, xmlStyle, 0);

        xml.add("</math>");
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocMathSpanInst");
        appendStyleString(builder);
        builder.add(':');

        for (final AbstractDocObjectInst child : getChildren()) {
            builder.add(child.toString());
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

        return docContainerInstHashCode();
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
        } else if (obj instanceof final DocMathSpanInst math) {
            equal = checkDocContainerInstEquals(math);
        } else {
            equal = false;
        }

        return equal;
    }
}
