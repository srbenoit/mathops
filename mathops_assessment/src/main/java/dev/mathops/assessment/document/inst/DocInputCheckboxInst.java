package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An instance of an input that supports a boolean choice. The choice will be presented to the student as a checkbox,
 * which can either be checked or unchecked, or can be in an unanswered state.
 *
 * <p>
 * Multiple checkboxes may be assigned to the same input variable name, each with their own value. In this case, values
 * represent bit flags, which are combined into the variable's value by logical OR. For example, four checkboxes with
 * the same name and values 1, 2, 4, and 8 would collectively produce a value in the range 0-15.
 */
public final class DocInputCheckboxInst extends AbstractDocInputInst {

    /** The value this checkbox contributes to the associated variable value. */
    public final long value;

    /**
     * Construct a new {@code DocInputCheckboxInst}.
     *
     * @param theStyle           the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName     the background color name ({@code null} if transparent)
     * @param theName            the name of the input's value in the parameter set
     * @param theEnabledVarName  the name of the variable whose value controls the enabled state of this input
     * @param theEnabledVarValue the value of the named variable that makes this input "enabled"
     * @param theValue           the value corresponding to this choice
     */
    public DocInputCheckboxInst(final DocObjectInstStyle theStyle, final String theBgColorName, final String theName,
                                final String theEnabledVarName, final Object theEnabledVarValue, final long theValue) {

        super(theStyle, theBgColorName, theName, theEnabledVarName, theEnabledVarValue);

        this.value = theValue;
    }

    /**
     * Gets the value this checkbox contributes (in the form of a bitwise OR) to the variable value.
     *
     * @return the value
     */
    public long getValue() {

        return this.value;
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

        xml.add("<input type='checkbox'");
        addDocInputInstXmlAttributes(xml); // style, name, enabled var settings
        xml.addAttribute("value", Long.toString(this.value), 0);
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

        builder.add("DocInputCheckboxInst");
        appendInputString(builder);
        builder.add("{value=", Long.valueOf(this.value), "}");

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return docInputInstHashCode() + (int) this.value;
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
        } else if (obj instanceof final DocInputCheckboxInst checkbox) {
            equal = checkDocInputInstEquals(checkbox) && this.value == checkbox.value;
        } else {
            equal = false;
        }

        return equal;
    }
}
