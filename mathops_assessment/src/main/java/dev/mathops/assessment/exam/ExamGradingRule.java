package dev.mathops.assessment.exam;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.Realizable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A grading rule that will compute either a pass/fail indication or a letter grade.
 */
public final class ExamGradingRule extends AbstractXmlObject implements Realizable {

    /** A rule type that creates a (boolean) pass/fail result. */
    public static final String PASS_FAIL = "pass/fail";

    /** The name of the grading rule. */
    public String gradingRuleName;

    /** The type of grading rule; one of the constants defined in this class. */
    private String gradingRuleType;

    /**
     * A set of grading conditions that contribute to the grading rule - each element in the list is a
     * {@code ExamGradingCondition} object.
     */
    private final List<ExamGradingCondition> conditions;

    /** The result of the grading rule. */
    public Object result;

    /**
     * Constructs a new {@code ExamGradingRule}.
     */
    public ExamGradingRule() {
        super();

        this.conditions = new ArrayList<>(1);
    }

    /**
     * Makes a clone of the object.
     *
     * @return a copy of the original object
     */
    ExamGradingRule deepCopy() {

        final ExamGradingRule copy = new ExamGradingRule();

        copy.gradingRuleName = this.gradingRuleName;
        copy.gradingRuleType = this.gradingRuleType;

        for (final ExamGradingCondition cond : this.conditions) {
            copy.conditions.add(cond.deepCopy());
        }

        copy.result = this.result;

        return copy;
    }

    /**
     * Gets the type of the grading rule.
     *
     * @return The grading rule type
     */
    public String getGradingRuleType() {

        return this.gradingRuleType;
    }

    /**
     * Sets the type of the grading rule.
     *
     * @param theRuleType the grading rule type
     * @throws IllegalArgumentException if the type specified is not valid
     */
    void setGradingRuleType(final String theRuleType) {

        if (PASS_FAIL.equals(theRuleType)) {
            this.gradingRuleType = theRuleType;
        } else {
            throw new IllegalArgumentException("Invalid grading rule type");
        }
    }

    /**
     * Adds a condition to the grading rule.
     *
     * @param condition the {@code ExamGradingCondition} to add
     */
    void addGradingCondition(final ExamGradingCondition condition) {

        this.conditions.add(condition);
    }

    /**
     * Retrieves an iterator over the grading conditions list. Each element in the iteration will be a
     * {@code ExamGradingCondition} object.
     *
     * @return the grading conditions iterator
     */
    public Iterator<ExamGradingCondition> getGradingConditions() {

        return this.conditions.iterator();
    }

//    /**
//     * Gets the number of grading conditions in the grading rule.
//     *
//     * @return the number of grading conditions
//     */
//    public int getNumGradingConditions() {
//
//        return this.conditions.size();
//    }

    /**
     * Realizes this grading rule, which does nothing since there are no contained resources that change with each
     * realization.
     *
     * @param context the evaluation context
     * @return {@code true} if realization succeeds; {@code false} otherwise
     */
    @Override
    public boolean realize(final EvalContext context) {

        for (final ExamGradingCondition cond : this.conditions) {
            if (!cond.realize(context)) {
                return false;
            }
        }

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

        xml.add(ind, "<grading-rule");
        writeAttribute(xml, "name", this.gradingRuleName);
        writeAttribute(xml, "type", this.gradingRuleType);
        writeAttribute(xml, "result", this.result);
        xml.addln(">");

        // Print the grading rule conditions
        for (final ExamGradingCondition cond : this.conditions) {
            cond.appendXml(xml, indent + 1);
        }

        xml.addln(ind, "</grading-rule>");
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.gradingRuleName)
                + Objects.hashCode(this.gradingRuleType)
                + Objects.hashCode(this.conditions);
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
        } else if (obj instanceof final ExamGradingRule rule) {
            equal = Objects.equals(this.gradingRuleName, rule.gradingRuleName)
                    && Objects.equals(this.gradingRuleType, rule.gradingRuleType)
                    && Objects.equals(this.conditions, rule.conditions);
        } else {
            equal = false;
        }

        return equal;
    }
}
