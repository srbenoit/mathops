package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EFieldStyle;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An instance of an input that supports the entry of an expression value. The input control allows definition of all
 * document formatting characteristics. Expression fields are drawn in a shaded outline, which highlights when the
 * object is selected. When selected, an edit caret is shown and editing is supported.
 *
 * <p>
 * Expression fields support entry of digits, decimal points, variables, arithmetic operators, fractions, exponents and
 * roots, functions, and paired parentheses.  Attributes control which of these are allowed for a given input.
 *
 * <p>
 * As the user exits the expression, the input simultaneously builds a plain-text representation.  The text
 * representation is the content that is submitted as the input value.  The input can attempt to parse the text into a
 * valid (evaluable) expression, and if this fails, the input's background is set to an error indicator color.
 */
public final class DocInputExpressionFieldInst extends AbstractDocInputFieldInst {

    /** The width. */
    private final int width;

    /**
     * Construct a new {@code DocInputExpressionFieldInst}.
     *
     * @param theStyle           the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName     the background color name ({@code null} if transparent)
     * @param theName            the name of the input's value in the parameter set
     * @param theEnabledVarName  the name of the variable whose value controls the enabled state of this input
     * @param theEnabledVarValue the value of the named variable that makes this input "enabled"
     * @param theFieldStyle      the style in which to present the field
     * @param theWidth           the width of the field's entry area, in units of digit widths
     */
    public DocInputExpressionFieldInst(final DocObjectInstStyle theStyle, final String theBgColorName,
                                       final String theName, final String theEnabledVarName,
                                       final Object theEnabledVarValue, final EFieldStyle theFieldStyle,
                                       final int theWidth) {

        super(theStyle, theBgColorName, theName, theEnabledVarName, theEnabledVarValue, theFieldStyle);

        this.width = theWidth;
    }

    /**
     * Gets the width of the field's entry area, in units of digit widths.
     *
     * @return the width
     */
    public int getWidth() {

        return this.width;
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

        xml.add("<input type='expression'");
        addDocInputFieldInstXmlAttributes(xml); // style, name, enabled var settings, field style
        xml.addAttribute("width", Integer.toString(this.width), 0);
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

        builder.add("DocInputExpressionFieldInst");
        appendInputFieldString(builder);
        builder.add("{width=", Integer.toString(this.width));
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

        return docInputFieldInstHashCode() + this.width;
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
        } else if (obj instanceof final DocInputExpressionFieldInst field) {
            equal = checkDocInputFieldInstEquals(field) && this.width == field.width;
        } else {
            equal = false;
        }

        return equal;
    }
}
