package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An instance of an input that supports the entry of a real number value. Real numbers may be entered using normal or
 * scientific notation. The input control allows definition of all document formatting characteristics. Real number
 * fields are drawn in a shaded outline, which highlights when the object is selected. When selected, a text edit caret
 * is shown and editing is supported.
 */
public final class DocInputDropdownInst extends AbstractDocInputInst {

    /** The optional default value, to be used when the user leaves the field blank. */
    private final Long defaultValue;

    /**
     * Construct a new {@code DocInputDropdownInst}.
     *
     * @param theStyle           the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName     the background color name ({@code null} if transparent)
     * @param theName            the name of the input's value in the parameter set
     * @param theEnabledVarName  the name of the variable whose value controls the enabled state of this input
     * @param theEnabledVarValue the value of the named variable that makes this input "enabled"
     * @param theDefaultValue    an optional default value to use when the user leaves the field blank
     */
    public DocInputDropdownInst(final DocObjectInstStyle theStyle, final String theBgColorName, final String theName,
                                final String theEnabledVarName, final Object theEnabledVarValue,
                                final Long theDefaultValue) {

        super(theStyle, theBgColorName, theName, theEnabledVarName, theEnabledVarValue);

        this.defaultValue = theDefaultValue;
    }

    /**
     * Gets the optional default value to use when the user leaves the field blank.
     *
     * @return the default value; {@code null} if none
     */
    public Long getDefaultValue() {

        return this.defaultValue;
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

        xml.add("<input type='real'");
        addDocInputInstXmlAttributes(xml); // style, name, enabled var settings
        xml.addAttribute("default", this.defaultValue, 0);
        xml.add("/>");

        // TODO: Need to add choices...
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
        appendInputString(builder);
        builder.add("{");
        if (this.defaultValue != null) {
            builder.add("default=", this.defaultValue);
        }
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

        return docInputInstHashCode()
               + Objects.hashCode(this.defaultValue);
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
        } else if (obj instanceof final DocInputDropdownInst field) {
            equal = checkDocInputInstEquals(field)
                    && Objects.equals(this.defaultValue, field.defaultValue);
        } else {
            equal = false;
        }

        return equal;
    }
}
