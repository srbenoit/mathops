package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A document object that places another object under a radical with an optional number specifying the root.
 */
public final class DocRadicalInst extends AbstractDocObjectInst {

    /** The base. */
    private final AbstractDocObjectInst base;

    /** The root. */
    private final AbstractDocObjectInst root;

    /**
     * Construct a new {@code DocRadicalInst} object.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theBase        the object acting as the base of the radical (inside the radical symbol)
     * @param theRoot        the object acting as a root (at the upper left of the surd); {@code null} if none
     */
    public DocRadicalInst(final DocObjectInstStyle theStyle, final String theBgColorName,
                          final AbstractDocObjectInst theBase, final AbstractDocObjectInst theRoot) {

        super(theStyle, theBgColorName);

        if (theBase == null) {
            throw new IllegalArgumentException("Base may not be null");
        }

        this.base = theBase;
        this.root = theRoot;
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
     * Gets the root.
     *
     * @return the root
     */
    public AbstractDocObjectInst getRoot() {

        return this.root;
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

        xml.add("<radical");
        addDocObjectInstXmlAttributes(xml);
        xml.add('>');

        xml.add("<base>");
        this.base.toXml(xml, xmlStyle, indent+1);
        xml.add("</base>");
        if (this.root != null) {
            xml.add("<root>");
            this.root.toXml(xml, xmlStyle, indent + 1);
            xml.add("</root>");
        }

        xml.add("</radical>");
    }

    /**
     * Generate a {@code String} representation of the fraction (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocRadicalInst");
        appendStyleString(builder);
        builder.add(':');

        final String baseStr = this.base.toString();
        builder.add("base=", baseStr);

        if (this.root != null) {
            final String rootStr = this.root.toString();
            builder.add(",root=", rootStr);
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

        return docObjectInstHashCode() + this.base.hashCode() + Objects.hashCode(this.root);
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
        } else if (obj instanceof final DocRadicalInst radical) {
            final AbstractDocObjectInst objBase = radical.getBase();
            final AbstractDocObjectInst objRoot = radical.getRoot();
            equal = checkDocObjectInstEquals(radical)
                    && this.base.equals(objBase)
                    && Objects.equals(this.root, objRoot);
        } else {
            equal = false;
        }

        return equal;
    }
}
