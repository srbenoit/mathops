package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;

import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A document object that supports two items with a relative offset position, such as a superscript, subscript, over,
 * under, and so on. The type of alignment is controlled by a parameter.
 */
public final class DocRelativeOffsetInst extends AbstractDocObjectInst {

    /** The base. */
    private final AbstractDocObjectInst base;

    /** The superscript. */
    private final AbstractDocObjectInst superscript;

    /** The subscript. */
    private final AbstractDocObjectInst subscript;

    /** The object drawn over the base. */
    private final AbstractDocObjectInst over;

    /** The object drawn under the base. */
    private final AbstractDocObjectInst under;

    /**
     * Construct a new {@code DocRelativeOffsetInst} object.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theBase        the object acting as the base of the radical (inside the radical symbol)
     * @param theSuperscript the object to draw in superscript position
     * @param theSubscript   the object to draw in subscript position
     * @param theOver        the object to draw over the base
     * @param theUnder       the object to draw under the base
     */
    public DocRelativeOffsetInst(final DocObjectInstStyle theStyle, final String theBgColorName,
                                 final AbstractDocObjectInst theBase, final AbstractDocObjectInst theSuperscript,
                                 final AbstractDocObjectInst theSubscript, final AbstractDocObjectInst theOver,
                                 final AbstractDocObjectInst theUnder) {

        super(theStyle, theBgColorName);

        if (theBase == null) {
            throw new IllegalArgumentException("Base may not be null");
        }

        this.base = theBase;
        this.superscript = theSuperscript;
        this.subscript = theSubscript;
        this.over = theOver;
        this.under = theUnder;
    }

    /**
     * Gets the base.
     *
     * @return the base
     */
    public AbstractDocObjectInst getBase() {

        return this.base;
    }

    /**
     * Gets the superscript.
     *
     * @return the superscript
     */
    public AbstractDocObjectInst getSuperscript() {

        return this.superscript;
    }

    /**
     * Gets the subscript.
     *
     * @return the subscript
     */
    public AbstractDocObjectInst getSubscript() {

        return this.subscript;
    }

    /**
     * Gets the over.
     *
     * @return the over
     */
    public AbstractDocObjectInst getOver() {

        return this.over;
    }

    /**
     * Gets the under.
     *
     * @return the under
     */
    public AbstractDocObjectInst getUnder() {

        return this.under;
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

        xml.add("<rel-offset");
        addDocObjectInstXmlAttributes(xml);
        xml.add('>');

        xml.add("<base>");
        this.base.toXml(xml, xmlStyle, indent+1);
        xml.add("</base>");
        if (this.superscript != null) {
            xml.add("<sup>");
            this.superscript.toXml(xml, xmlStyle, indent + 1);
            xml.add("</sup>");
        }
        if (this.subscript != null) {
            xml.add("<sub>");
            this.subscript.toXml(xml, xmlStyle, indent + 1);
            xml.add("</sub>");
        }
        if (this.over != null) {
            xml.add("<over>");
            this.over.toXml(xml, xmlStyle, indent + 1);
            xml.add("</over>");
        }
        if (this.under != null) {
            xml.add("<under>");
            this.under.toXml(xml, xmlStyle, indent + 1);
            xml.add("</under>");
        }

        xml.add("</rel-offset>");
    }

    /**
     * Generate a {@code String} representation of the fraction (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocRelOffsetInst");
        appendStyleString(builder);
        builder.add(':');

        final String baseStr = this.base.toString();
        builder.add("base=", baseStr);

        if (this.superscript != null) {
            final String supStr = this.superscript.toString();
            builder.add(",sup=", supStr);
        }
        if (this.subscript != null) {
            final String subStr = this.subscript.toString();
            builder.add(",sub=", subStr);
        }
        if (this.over != null) {
            final String overStr = this.over.toString();
            builder.add(",over=", overStr);
        }
        if (this.under != null) {
            final String underStr = this.under.toString();
            builder.add(",under=", underStr);
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

        return docObjectInstHashCode() + this.base.hashCode() + Objects.hashCode(this.superscript)
                + Objects.hashCode(this.subscript) + Objects.hashCode(this.over) + Objects.hashCode(this.under);
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
        } else if (obj instanceof final DocRelativeOffsetInst radical) {
            final AbstractDocObjectInst objBase = radical.getBase();
            final AbstractDocObjectInst objSuperscript = radical.getSuperscript();
            final AbstractDocObjectInst objSubscript = radical.getSubscript();
            final AbstractDocObjectInst objOver = radical.getOver();
            final AbstractDocObjectInst objUnder = radical.getUnder();
            equal = checkDocObjectInstEquals(radical)
                    && this.base.equals(objBase)
                    && Objects.equals(this.superscript, objSuperscript)
                    && Objects.equals(this.subscript, objSubscript)
                    && Objects.equals(this.over, objOver)
                    && Objects.equals(this.under, objUnder);
        } else {
            equal = false;
        }

        return equal;
    }
}
