package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.List;
import java.util.Objects;

/**
 * An instance  of a column of paragraphs, which will lay out the paragraphs vertically, one after the other, with the
 * top of each paragraph aligned with the bottom of the prior one. All paragraphs will be the same width.
 */
public final class DocColumnInst extends AbstractDocContainerInst {

    /**
     * An optional XML tag - if absent, the XML representation will not be wrapped in any XML element; only child
     * elements will be emitted.
     */
    private final String tag;

    /**
     * Construct a new {@code DocColumnInst}.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theChildren    the list of child objects
     * @param theTag         an optional XML tag - if absent, the XML representation will not be wrapped in any XML
     *                       element; only child elements will be emitted.
     */
    public DocColumnInst(final DocObjectInstStyle theStyle, final String theBgColorName,
                         final List<? extends AbstractDocObjectInst> theChildren, final String theTag) {

        super(theStyle, theBgColorName, theChildren);

        this.tag = theTag;
    }

    /**
     * Gets the optional XML tag - if absent, the XML representation will not be wrapped in any XML element; only child
     * elements will be emitted.
     *
     * @return the tag
     */
    public String getTag() {

        return this.tag;
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        final String ind = makeIndent(indent);

        if (this.tag != null) {
            xml.addln(ind, "<", this.tag, ">");
            appendChildrenXml(xml, xmlStyle, indent + 1);
            xml.addln(ind, "</", this.tag, ">");
        } else {
            appendChildrenXml(xml, xmlStyle, indent);
        }
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocColumnInst");
        if (this.tag != null) {
            builder.add('[').add(this.tag).add(']');
        }
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

        return docContainerInstHashCode() + Objects.hashCode(this.tag);
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
        } else if (obj instanceof final DocColumnInst column) {
            equal = checkDocContainerInstEquals(column)
                    && Objects.equals(this.tag, column.tag);
        } else {
            equal = false;
        }

        return equal;
    }
}
