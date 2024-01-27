package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.Objects;

/**
 * The base class for all instances of document objects.
 */
public abstract class AbstractDocObjectInst {

    /** Object style. */
    private final DocObjectInstStyle style;

    /** The background color of the container ({@code null} for transparent). */
    private final String bgColorName;

    /**
     * Construct a new {@code AbstractDocObjectInst}.
     *
     * @param theStyle the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     */
    AbstractDocObjectInst(final DocObjectInstStyle theStyle, final String theBgColorName) {

        this.style = theStyle;
        this.bgColorName = theBgColorName;
    }

    /**
     * Get the {@code DocObjectInstStyle} associated with this object.
     *
     * @return the style object
     */
    public final DocObjectInstStyle getStyle() {

        return this.style;
    }

    /**
     * Get the background color name.
     *
     * @return the background color name
     */
    public final String getBgColorName() {

        return this.bgColorName;
    }

    /**
     * Create a string for a particular indentation level.
     *
     * @param indent the number of spaces to indent
     * @return a string with the requested number of spaces
     */
    static String makeIndent(final int indent) {

        final HtmlBuilder builder = new HtmlBuilder(indent);

        for (int i = 0; i < indent; ++i) {
            builder.add("  ");
        }

        return builder.toString();
    }

    /**
     * Generate the XML representation of the object.
     *
     * @param xmlStyle the style to use when emitting XML
     * @param indent   the number of spaces to indent the printout
     * @return the XML representation
     */
    final String toXml(final EXmlStyle xmlStyle, final int indent) {

        final HtmlBuilder builder = new HtmlBuilder(512);

        toXml(builder, xmlStyle, indent);

        return builder.toString();
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml      the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent   the number of spaces to indent the printout
     */
    public abstract void toXml(HtmlBuilder xml, final EXmlStyle xmlStyle, int indent);

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public abstract String toString();

    /**
     * Adds style information as part of string representation generation.
     * @param builder the {@code HtmlBuilder} to which to append
     */
    final void appendStyleString(final HtmlBuilder builder) {

        if (this.style != null || this.bgColorName != null) {
            builder.add('{');
            if (this.style != null) {
                builder.add("color=", this.style.colorName, ",font=", this.style.fontName, ",size=");
                builder.add(this.style.fontSize);
                if (this.style.fontStyle != 0) {
                    builder.add(",style=");
                    builder.add(this.style.fontStyle);
                }
            }
            if (this.bgColorName != null) {
                builder.add("bgcolor=", this.bgColorName);
            }
            builder.add('}');
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public abstract int hashCode();

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Add XML attributes specific to input fields to an XML block.
     *
     * @param xml the {@code HtmlBuilder} to which to append the XML attributes
     */
    final void addDocObjectInstXmlAttributes(final HtmlBuilder xml) {

        if (this.style != null) {
            this.style.appendXmlAttributes(xml);
        }
        xml.addAttribute("bgcolor", this.bgColorName, 0);
    }

    /**
     * Generates an integer hash code for the style settings that can be used when calculating the hash code for a
     * subclass of this class.
     *
     * @return the hash code of the object's style settings
     */
    final int docObjectInstHashCode() {

        // NOTE: "parent" does not participate in equality comparisons, so it is not included in hash calculation

        return Objects.hashCode(this.style) + Objects.hashCode(this.bgColorName);
    }

    /**
     * Checks whether the style settings in a given object are equal to those in this object.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the style settings from the objects are equal; {@code false} otherwise
     */
    final boolean checkDocObjectInstEquals(final AbstractDocObjectInst obj) {

        return Objects.equals(this.style, obj.style) && Objects.equals(this.bgColorName, obj.bgColorName);
    }
}
