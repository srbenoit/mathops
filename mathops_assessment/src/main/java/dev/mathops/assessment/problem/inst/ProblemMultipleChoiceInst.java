package dev.mathops.assessment.problem.inst;

import dev.mathops.assessment.document.inst.DocColumnInst;
import dev.mathops.assessment.problem.ECalculatorType;
import dev.mathops.assessment.problem.EProblemType;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.List;

/**
 * An iteration of a problem that allows the user to select a single choice from a list of choices.
 */
public final class ProblemMultipleChoiceInst extends AbstractProblemMultipleChoiceInst {

    /**
     * Constructs a new {@code ProblemMultipleChoiceIteration}.
     *
     * @param theRef         the unique position in the organizational tree of instructional material of the template
     *                       that generated this iteration
     * @param theIterationId the unique ID of the current generated iteration
     * @param theCalculator  the calculator allowed on the problem (null interpreted as NO_CALC)
     * @param theQuestion    the question document (may contain inputs)
     * @param theSolution    the optional solution document (may not contain inputs)
     * @param theChoices     the ordered list of choices
     */
    public ProblemMultipleChoiceInst(final String theRef, final String theIterationId,
                                     final ECalculatorType theCalculator, final DocColumnInst theQuestion,
                                     final DocColumnInst theSolution, final List<ProblemChoiceInst> theChoices) {

        super(EProblemType.MULTIPLE_CHOICE, theRef, theIterationId, theCalculator, theQuestion, theSolution,
                theChoices);
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

        return problemMultipleChoiceInstHashCode();
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
        } else if (obj instanceof final ProblemMultipleChoiceInst problem) {
            equal = checkProblemMultipleChoiceInstEquals(problem);
        } else {
            equal = false;
        }

        return equal;
    }
}
