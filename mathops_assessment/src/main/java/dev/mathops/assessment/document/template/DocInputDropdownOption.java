package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.inst.DocInputDropdownOptionInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * A document object that supports the entry of an integer value through the selection of an item from a dropdown box.
 */
public final class DocInputDropdownOption {

    /** The option text. */
    private final String text;

    /** The value associated with the option. */
    private final Long value;

    /**
     * Construct a new {@code DocInputDropdownOption}.
     *
     * @param theText  the option text
     * @param theValue the value associated with the option
     */
    DocInputDropdownOption(final String theText, final Long theValue) {

        this.text = theText;
        this.value = theValue;
    }

    /**
     * Get the option text.
     *
     * @return the option text
     */
    public String getText() {

        return this.text;
    }

    /**
     * Get the value associated with the option.
     *
     * @return the value
     */
    public Long getValue() {

        return this.value;
    }

    /**
     * Generates an instance of this document object based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the instance  object
     */
    public DocInputDropdownOptionInst createInstance(final EvalContext evalContext) {

        // At the moment, text and value cannot be variable-based
        return new DocInputDropdownOptionInst(this.text, this.value);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    public void toXml(final HtmlBuilder xml, final int indent) {

        // Since the text value should never contain any characters that would be invalid in XML
        // (", ', <, >, \, /), we can just write out the text.
        xml.add("<option");
        xml.addAttribute("text", this.text, 0);
        xml.addAttribute("value", this.value, 0);
        xml.add("/>");
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return this.text.hashCode() + this.value.hashCode();
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
        } else if (obj instanceof final DocInputDropdownOption other) {
            equal = this.text.equals(other.text) && this.value.equals(other.value);
        } else {
            equal = false;
        }

        return equal;
    }
}
