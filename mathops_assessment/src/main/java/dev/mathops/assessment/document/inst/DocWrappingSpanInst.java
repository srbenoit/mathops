package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.List;

/**
 * A series of document objects that will be rendered with line wrap. Objects will align themselves as they do in
 * paragraphs.
 */
public final class DocWrappingSpanInst extends AbstractDocContainerInst {

    /**
     * Construct a new {@code DocWrappingSpanInst}.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theChildren    the list of child objects
     */
    public DocWrappingSpanInst(final DocObjectInstStyle theStyle, final String theBgColorName,
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

        xml.add("<wrap");
        addDocObjectInstXmlAttributes(xml);
        xml.add('>');

        appendChildrenXml(xml, xmlStyle, 0);

        xml.add("</wrap>");
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocWrappingSpanInst");
        appendStyleString(builder);
        builder.add(':');

        for (final AbstractDocObjectInst child : getChildren()) {
            final String childStr = child.toString();
            builder.add(childStr);
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
        } else if (obj instanceof final DocWrappingSpanInst wrap) {
            equal = checkDocContainerInstEquals(wrap);
        } else {
            equal = false;
        }

        return equal;
    }
}
