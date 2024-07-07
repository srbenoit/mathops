package dev.mathops.assessment.exam;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.Realizable;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A prerequisite that must be satisfied before a grading condition will provide a positive result.
 */
public final class ExamOutcomeValidation extends AbstractXmlObject implements Realizable {

    /** The flag to use when recording how the grade was validated. */
    public String howValidated;

    /** A formula that evaluates to a boolean that tells whether the prerequisite is satisfied. */
    public Formula validationFormula;

    /**
     * Construct a new {@code ExamOutcomeValidation}.
     */
    public ExamOutcomeValidation() {
        super();

        // Empty
    }

    /**
     * Makes a clone of the object.
     *
     * @return a copy of the original object
     */
    ExamOutcomeValidation deepCopy() {

        final ExamOutcomeValidation copy = new ExamOutcomeValidation();

        copy.howValidated = this.howValidated;

        if (this.validationFormula != null) {
            copy.validationFormula = this.validationFormula.deepCopy();
        }

        return copy;
    }

    /**
     * Realizes this validation rule, which does nothing since there are no contained resources that change with each
     * realization.
     *
     * @param context the evaluation context
     * @return {@code true} if realization succeeds; {@code false} otherwise
     */
    @Override
    public boolean realize(final EvalContext context) {

        return true;
    }

    /**
     * Appends the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void appendXml(final HtmlBuilder xml, final int indent) {

        final String ind = makeIndent(indent);

        xml.add(ind, "<valid-if");

        if (this.howValidated != null) {
            xml.add(" how=\"", this.howValidated, CoreConstants.QUOTE);
        }

        xml.add('>');

        if (this.validationFormula != null) {
            xml.add(escape(this.validationFormula.toString()));
        }

        xml.addln("</valid-if>");
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.validationFormula);
    }

    /**
     * Tests non-transient member variables in this base class for equality with another instance.
     *
     * @param obj the other instance
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final ExamOutcomeValidation validation) {
            equal = Objects.equals(this.howValidated, validation.howValidated)
                    && Objects.equals(this.validationFormula, validation.validationFormula);
        } else {
            equal = false;
        }

        return equal;
    }
}
