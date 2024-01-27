package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EJustification;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.List;

/**
 * A paragraph is a collection of document objects which flow naturally in a left-to-right, top-down ordering. Each
 * object has a property that controls its vertical alignment within the lines of the paragraph. Each flowed line will
 * define a baseline. Each object has a top, bottom, center line and baseline. The default is to align all baselines of
 * objects, but each object can be set regarding the alignment point for the next object. A paragraph may contain any
 * other descendant of {@code DocObject} except other paragraphs.
 */
public final class DocParagraphInst extends AbstractDocContainerInst {

    /** The paragraph justification. */
    private final EJustification justification;

    /**
     * Construct a new {@code DocParagraphInst}.
     *
     * @param theStyle         the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName   the background color name ({@code null} if transparent)
     * @param theChildren      the list of child objects
     * @param theJustification the paragraph justification
     */
    public DocParagraphInst(final DocObjectInstStyle theStyle, final String theBgColorName,
                            final List<? extends AbstractDocObjectInst> theChildren,
                            final EJustification theJustification) {

        super(theStyle, theBgColorName, theChildren);

        if (theJustification == null) {
            throw new IllegalArgumentException("Justification may not be null");
        }

        this.justification = theJustification;
    }

    /**
     * Gets the paragraph justification.
     *
     * @return the justification
     */
    public EJustification getJustification() {

        return this.justification;
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

        xml.add("<p");
        xml.addAttribute("justification", this.justification, 0);
        addDocObjectInstXmlAttributes(xml);
        xml.add('>');

        appendChildrenXml(xml, xmlStyle, 0);

        xml.add("</p>");
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocParagraphInst");
        appendStyleString(builder);
        builder.add("{justification=", this.justification, "}:");

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

        return docContainerInstHashCode() + this.justification.hashCode();
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
        } else if (obj instanceof final DocParagraphInst paragraph) {
            equal = checkDocContainerInstEquals(paragraph)
                    && this.justification == paragraph.justification;
        } else {
            equal = false;
        }

        return equal;
    }
}
