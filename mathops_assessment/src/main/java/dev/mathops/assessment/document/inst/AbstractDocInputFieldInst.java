package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EFieldStyle;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * The base class for instances of inputs. Each input has a name under which it submits its values, and may have its
 * "enabled" state controlled by a variable.
 */
public abstract class AbstractDocInputFieldInst extends AbstractDocInputInst {

    /** The field style. */
    private final EFieldStyle fieldStyle;

    /**
     * Construct a new {@code AbstractDocInputFieldInst}.
     *
     * @param theStyle           the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theName            the name of the input's value in the parameter set
     * @param theEnabledVarName  the name of the variable whose value controls the enabled state of this input
     * @param theEnabledVarValue the value of the named variable that makes this input "enabled"
     * @param theFieldStyle      the style in which to present the field
     */
    AbstractDocInputFieldInst(final DocObjectInstStyle theStyle, final String theBgColorName, final String theName,
                              final String theEnabledVarName, final Object theEnabledVarValue,
                              final EFieldStyle theFieldStyle) {

        super(theStyle, theBgColorName, theName, theEnabledVarName, theEnabledVarValue);

        if (theStyle == null) {
            throw new IllegalArgumentException("Field style may not be null");
        }

        this.fieldStyle = theFieldStyle;
    }

    /**
     * Gts the style in which the field is to be presented.
     *
     * @return the field style
     */
    public final EFieldStyle theFieldStyle() {

        return  this.fieldStyle;
    }


    /**
     * Add XML attributes specific to input fields to an XML block.
     *
     * @param builder the {@code HtmlBuilder} to which to append the XML attributes
     */
    final void addDocInputFieldInstXmlAttributes(final HtmlBuilder builder) {

        addDocInputInstXmlAttributes(builder);

        builder.add(" field-style='", this.fieldStyle, "'");
    }

    /**
     * Adds style information as part of string representation generation.
     * @param builder the {@code HtmlBuilder} to which to append
     */
    final void appendInputFieldString(final HtmlBuilder builder) {

        appendInputString(builder);
        builder.add("{fieldStyle=", this.fieldStyle, "}");
    }

    /**
     * Generates an integer hash code for the style settings, input name, and enabled variable settings that can be
     * used when calculating the hash code for a subclass of this class.
     *
     * @return the hash code of the object's style settings, name, and enabled variable settings
     */
    final int docInputFieldInstHashCode() {

        return docInputInstHashCode() + this.fieldStyle.hashCode() ;
    }

    /**
     * Checks whether the style settings, input name, and enabled variable settings in a given object are equal to
     * those in this object.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the style settings, name, and enabled variable setting from the objects are equal;
     * {@code false} otherwise
     */
    final boolean checkDocInputFieldInstEquals(final AbstractDocInputFieldInst obj) {

        return checkDocInputInstEquals(obj) && this.fieldStyle == obj.fieldStyle;
    }
}
