package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An instance of an option within a dropdown input.
 */
public final class DocInputDropdownOptionInst {

    /** The text to show. */
    private final String text;

    /** The numeric value associated with the choice. */
    private final Long value;

    /**
     * Constructs a new {@code DocInputDropdownOptionInst}.
     *
     * @param theText  the text to show
     * @param theValue the value associated with the choice
     */
    public DocInputDropdownOptionInst(final String theText, final Long theValue) {

        this.text = theText;
        this.value = theValue;
    }

    /**
     * Gets the text to show.
     *
     * @return the text
     */
    public String getText() {

        return this.text;
    }

    /**
     * Gets the alue associated with the choice.
     *
     * @return the value
     */
    public Long getValue() {

        return this.value;
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml      the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent   the number of spaces to indent the printout
     */
    public void toXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        xml.add("<option");
        xml.addAttribute("text", this.text, 0);
        xml.addAttribute("value", this.value, 0);
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

        builder.add("DocInputDropdownInst");
        // FIXME
//        appendInputString(builder);
//        builder.add("{");
//        if (this.defaultValue != null) {
//            builder.add("default=", this.defaultValue);
//        }
        builder.add('}');

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        // FIXME
//        return docInputInstHashCode()
//               + Objects.hashCode(this.defaultValue);
        return 0;
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
        } else if (obj instanceof final DocInputDropdownOptionInst field) {
            // FIXME
//            equal = checkDocInputInstEquals(field)
//                    && Objects.equals(this.defaultValue, field.defaultValue);
            equal = false;
        } else {
            equal = false;
        }

        return equal;
    }
}
