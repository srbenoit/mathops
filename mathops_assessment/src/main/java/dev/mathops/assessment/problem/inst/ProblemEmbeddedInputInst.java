package dev.mathops.assessment.problem.inst;

import dev.mathops.assessment.document.inst.DocColumnInst;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.problem.ECalculatorType;
import dev.mathops.assessment.problem.EProblemType;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An iteration of a problem that may have arbitrary inputs within the Question document.
 */
public final class ProblemEmbeddedInputInst extends AbstractProblemInst {

    /** An optional representative correct answer. */
    private final DocColumnInst correctAnswer;

    /**
     * The formula used to evaluate correctness (this formula may only contain variable references for input
     * variable values, and must evaluate to a Boolean).
     */
    private final Formula correctness;

    /**
     * Construct an empty {@code ProblemEmbeddedInputIteration} object.
     *
     * @param theRef           the unique position in the organizational tree of instructional material of the template
     *                         that generated this iteration
     * @param theIterationId   the unique ID of the current generated iteration
     * @param theCalculator    the calculator allowed on the problem (null interpreted as NO_CALC)
     * @param theQuestion      the question document (may contain inputs)
     * @param theSolution      the optional solution document (may not contain inputs)
     * @param theCorrectAnswer an optional representative correct answer
     * @param theCorrectness   the correctness formula
     */
    public ProblemEmbeddedInputInst(final String theRef, final String theIterationId,
                                    final ECalculatorType theCalculator, final DocColumnInst theQuestion,
                                    final DocColumnInst theSolution, final DocColumnInst theCorrectAnswer,
                                    final Formula theCorrectness) {

        super(EProblemType.EMBEDDED_INPUT, theRef, theIterationId, theCalculator, theQuestion, theSolution);

        if (theCorrectness == null) {
            throw new IllegalArgumentException("Correctness formula may not be null");
        }

        this.correctAnswer = theCorrectAnswer;
        this.correctness = theCorrectness;
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

        return problemInstHashCode() + Objects.hashCode(this.correctness)
                + Objects.hashCode(this.correctAnswer);
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
        } else if (obj instanceof final ProblemEmbeddedInputInst problem) {
            equal = checkProblemInstEquals(problem)
                    && Objects.equals(this.correctness, problem.correctness)
                    && Objects.equals(this.correctAnswer, problem.correctAnswer);
        } else {
            equal = false;
        }

        return equal;
    }
}
