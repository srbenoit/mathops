package dev.mathops.assessment.exam;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.Realizable;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A grading condition that determines a letter grade or a pass/fail indication based on grading criteria.
 */
public final class ExamGradingCondition extends AbstractXmlObject implements Realizable {

    /** A type of grading condition that produces a passing indication. */
    static final String PASS_IF = "pass-if";

    /** A type of grading condition that produces a letter grade indication. */
    static final String LETTER_IF = "letter-if";

    /** The type of grading condition. */
    private String type;

    /**
     * The value associated with the condition (ignored for pass-if conditions, stores the letter grade to award for
     * letter-if conditions).
     */
    String gradingConditionValue;

    /**
     * A formula that evaluates to a {@code boolean} that tells whether the condition is satisfied.
     */
    public Formula gradingConditionFormula;

    /**
     * Constructs a new {@code ExamGradingCondition}.
     */
    public ExamGradingCondition() {

        super();
    }

    /**
     * Makes a clone of the object.
     *
     * @return a copy of the original object
     */
    ExamGradingCondition deepCopy() {

        final ExamGradingCondition copy = new ExamGradingCondition();

        copy.type = this.type;
        copy.gradingConditionValue = this.gradingConditionValue;

        if (this.gradingConditionFormula != null) {
            copy.gradingConditionFormula = this.gradingConditionFormula.deepCopy();
        }

        return copy;
    }

//    /**
//     * Gets the type of the grading condition.
//     *
//     * @return the grading condition type
//     */
//    public String getGradingConditionType() {
//
//        return this.type;
//    }

    /**
     * Sets the type of the grading condition.
     *
     * @param theType the grading condition type
     * @throws IllegalArgumentException if the type specified is not valid
     */
    void setGradingConditionType(final String theType) {

        if (PASS_IF.equals(theType) || LETTER_IF.equals(theType)) {
            this.type = theType;
        } else {
            throw new IllegalArgumentException("Invalid grading condition type");
        }
    }

    /**
     * Realizes this grading condition, which does nothing since there are no contained resources that change with each
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

        if (PASS_IF.equals(this.type)) {
            xml.add(ind, "<pass-if>");

            if (this.gradingConditionFormula != null) {
                xml.add(escape(this.gradingConditionFormula.toString()));
            }

            xml.addln("</pass-if>");
        } else if (LETTER_IF.equals(this.type)) {
            xml.add(ind, "<letter-if");
            writeAttribute(xml, "value", this.gradingConditionValue);
            xml.add(">");

            if (this.gradingConditionFormula != null) {
                xml.add(escape(this.gradingConditionFormula.toString()));
            }

            xml.addln("</letter-if>");
        }
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.type)
                + Objects.hashCode(this.gradingConditionValue)
                + Objects.hashCode(this.gradingConditionFormula);
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
        } else if (obj instanceof final ExamGradingCondition problem) {
            equal = Objects.equals(this.type, problem.type)
                    && Objects.equals(this.gradingConditionValue, problem.gradingConditionValue)
                    && Objects.equals(this.gradingConditionFormula, problem.gradingConditionFormula);
        } else {
            equal = false;
        }

        return equal;
    }
}
