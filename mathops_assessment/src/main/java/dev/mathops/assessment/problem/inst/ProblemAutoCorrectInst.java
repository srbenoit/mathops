package dev.mathops.assessment.problem.inst;

import dev.mathops.assessment.document.inst.DocColumnInst;
import dev.mathops.assessment.problem.ECalculatorType;
import dev.mathops.assessment.problem.EProblemType;
import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A generated instance of an "Auto-Correct" problem. This type of problem will automatically be counted correct, and
 * requires no answer.
 */
public final class ProblemAutoCorrectInst extends AbstractProblemInst {

    /**
     * Constructs a new {@code ProblemAutoCorrectIteration}.
     *
     * @param theIterationId the unique ID of the current generated iteration
     * @param theCalculator  the calculator allowed on the problem (null interpreted as NO_CALC)
     * @param theQuestion    the question document (may contain inputs)
     */
    public ProblemAutoCorrectInst(final String theIterationId, final ECalculatorType theCalculator,
                                  final DocColumnInst theQuestion) {

        super(EProblemType.AUTO_CORRECT, "autocorrect", theIterationId, theCalculator, theQuestion, null);
    }

    /**
     * Appends the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    public void appendXml(final HtmlBuilder xml, final int indent) {

    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return problemInstHashCode();
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
        } else if (obj instanceof final ProblemAutoCorrectInst problem) {
            equal = checkProblemInstEquals(problem);
        } else {
            equal = false;
        }

        return equal;
    }
}
