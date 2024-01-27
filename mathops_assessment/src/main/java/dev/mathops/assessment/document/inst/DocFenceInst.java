package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EFenceType;
import dev.mathops.assessment.document.EPrimaryBaseline;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.List;

/**
 * An instance of a fenced construction, with content surrounded by "stretchy" fence characters such as a parentheses
 * or brackets.
 */
public final class DocFenceInst extends AbstractDocContainerInst {

    /** The type of fence this is. */
    private final EFenceType type;

    /** The baseline used for alignment of this construction. */
    private final EPrimaryBaseline baseline;

    /**
     * Construct a new {@code DocFenceInst} object.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theChildren    the list of child objects
     * @param theType        the fenced construction type
     * @param theBaseline    the baseline used for alignment
     */
    public DocFenceInst(final DocObjectInstStyle theStyle, final String theBgColorName,
                        final List<? extends AbstractDocObjectInst> theChildren, final EFenceType theType,
                        final EPrimaryBaseline theBaseline) {

        super(theStyle, theBgColorName, theChildren);

        if (theType == null) {
            throw new IllegalArgumentException("Fence type may not be null");
        }
        if (theBaseline == null) {
            throw new IllegalArgumentException("Alignment baseline may not be null");
        }

        this.type = theType;
        this.baseline = theBaseline;
    }

    /**
     * Gets the type of fenced construction.
     *
     * @return the type
     */
    public EFenceType getType() {

        return this.type;
    }

    /**
     * Gets the baseline used to align this construction within surrounding content.
     *
     * @return the baseline
     */
    public EPrimaryBaseline getBaseline() {

        return this.baseline;
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

        xml.add("<fence");
        xml.addAttribute("type", this.type, 0);
        xml.addAttribute("baseline", this.baseline, 0);
        addDocObjectInstXmlAttributes(xml);
        xml.add('>');

        appendChildrenXml(xml, EXmlStyle.INLINE, 0);

        xml.add("</fence>");
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocFenceInst");
        builder.add("[type=", this.type, ",baseline=", this.baseline, "]");
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

        return docContainerInstHashCode() + this.type.hashCode() + this.baseline.hashCode();
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
        } else if (obj instanceof final DocFenceInst fence) {
            equal = checkDocContainerInstEquals(fence)
                    && this.type == fence.type
                    && this.baseline == fence.baseline;
        } else {
            equal = false;
        }

        return equal;
    }
}
