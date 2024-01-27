package dev.mathops.assessment.exam;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.Realizable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A specification for a problem that contributes to a subtest.
 */
public final class ExamSubtestProblem extends AbstractXmlObject implements Realizable {

    /** The ID of the problem referenced by the subtest. */
    public int problemId;

    /** The weight to assign to the problem. */
    public double weight = 1.0;

    /**
     * Constructs a new {@code ExamSubtestProblem}.
     */
    public ExamSubtestProblem() {
        super(); /* Empty */

    }

    /**
     * Makes a clone of the object.
     *
     * @return a copy of the original object
     */
    ExamSubtestProblem deepCopy() {

        final ExamSubtestProblem copy = new ExamSubtestProblem();

        copy.problemId = this.problemId;
        copy.weight = this.weight;

        return copy;
    }

    /**
     * Realizes this subtest problem, which does nothing since there are no contained resources that change with each
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

        xml.add(ind, "<subtest-problem");
        writeAttribute(xml, "problem-id", Integer.toString(this.problemId));

        if (this.weight != 1.0) {
            writeAttribute(xml, "weight", Double.toString(this.weight));
        }

        xml.addln("/>");
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return this.problemId + Double.hashCode(this.weight);
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
        } else if (obj instanceof final ExamSubtestProblem prob) {
            equal = this.problemId == prob.problemId && this.weight == prob.weight;
        } else {
            equal = false;
        }

        return equal;
    }
}
