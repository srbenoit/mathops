package dev.mathops.assessment.document.inst;

import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * The base class for instances of inputs. Each input has a name under which it submits its values, and may have its
 * "enabled" state controlled by a variable.
 */
public abstract class AbstractDocInputInst extends AbstractDocObjectInst {

    /** The name of the input's value in the parameter set. */
    private final String name;

    /** The name of a variable that will control enabled state of this input. */
    private final String enabledVarName;

    /** The value of {@code enabledVarName} that will enable this input (Boolean or Long). */
    private final Object enabledVarValue;

    /**
     * Construct a new {@code AbstractDocInputInst}.
     *
     * @param theStyle           the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theName            the name of the input's value in the parameter set
     * @param theEnabledVarName  the name of the variable whose value controls the enabled state of this input
     * @param theEnabledVarValue the value of the named variable that makes this input "enabled"
     */
    AbstractDocInputInst(final DocObjectInstStyle theStyle, final String theBgColorName, final String theName,
                         final String theEnabledVarName, final Object theEnabledVarValue) {

        super(theStyle, theBgColorName);

        if (theName == null) {
            throw new IllegalArgumentException("Input name may not be null");
        }

        this.name = theName;
        this.enabledVarName = theEnabledVarName;
        this.enabledVarValue = theEnabledVarValue;
    }

    /**
     * Gets the variable name
     * @return the variable name
     */
    public final String getName() {

        return this.name;
    }

    /**
     * Gets the name of the variable whose value controls the enabled state of this input.  If this input's enabled
     * state is not controlled by a variable, this will be {@code null} and the input will always be enabled.
     *
     * @return the variable name
     */
    public final String getEnabledVarName() {

        return this.enabledVarName;
    }

    /**
     * Gets the value that the enable-control variable must have in order for this input to be enabled.
     *
     * @return the variable value
     */
    public final Object getEnabledVarValue() {

        return this.enabledVarValue;
    }

    /**
     * Add XML attributes specific to input fields to an XML block.
     *
     * @param builder the {@code HtmlBuilder} to which to append the XML attributes
     */
    final void addDocInputInstXmlAttributes(final HtmlBuilder builder) {

        addDocObjectInstXmlAttributes(builder);

        builder.add(" name='", this.name, "'");

        if (this.enabledVarName != null) {
            builder.add(" enabled-var-name='", this.enabledVarName, "'");
        }
        if (this.enabledVarValue != null) {
            builder.add(" enabled-var-value='", this.enabledVarValue, "'");
        }
    }

    /**
     * Adds style information as part of string representation generation.
     * @param builder the {@code HtmlBuilder} to which to append
     */
    final void appendInputString(final HtmlBuilder builder) {

        appendStyleString(builder);
        builder.add("{name=", this.name);
        if (this.enabledVarName != null && this.enabledVarValue != null) {
            builder.add(",enabledvarname=", this.enabledVarName, ",enabledvarvalue=", this.enabledVarValue);
        }
        builder.add('}');
    }

    /**
     * Generates an integer hash code for the style settings, input name, and enabled variable settings that can be
     * used when calculating the hash code for a subclass of this class.
     *
     * @return the hash code of the object's style settings, name, and enabled variable settings
     */
    final int docInputInstHashCode() {

        return docObjectInstHashCode() + this.name.hashCode() + Objects.hashCode(this.enabledVarName)
                + Objects.hashCode(this.enabledVarValue);
    }

    /**
     * Checks whether the style settings, input name, and enabled variable settings in a given object are equal to
     * those in this object.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the style settings, name, and enabled variable setting from the objects are equal;
     * {@code false} otherwise
     */
    final boolean checkDocInputInstEquals(final AbstractDocInputInst obj) {

        return checkDocObjectInstEquals(obj)
                && this.name.equals(obj.name)
                && Objects.equals(this.enabledVarName, obj.enabledVarName)
                && Objects.equals(this.enabledVarValue, obj.enabledVarValue);
    }
}
