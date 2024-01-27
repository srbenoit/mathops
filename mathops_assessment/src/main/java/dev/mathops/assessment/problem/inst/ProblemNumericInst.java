
package dev.mathops.assessment.problem.inst;

import dev.mathops.assessment.document.inst.DocColumnInst;
import dev.mathops.assessment.problem.ECalculatorType;
import dev.mathops.assessment.problem.EProblemType;
import dev.mathops.commons.EqualityTests;

import java.util.Objects;

/**
 * An iteration of a problem that accepts a single numerical value.
 */
public final class ProblemNumericInst extends AbstractProblemInst {

    /** The specification of how the problem should accept a numeric answer. */
    private final ProblemAcceptNumberInst acceptNumber;

    /**
     * Constructs an empty {@code ProblemNumericIteration} object.
     *
     * @param theRef          the unique position in the organizational tree of instructional material of the template
     *                        that generated this iteration
     * @param theIterationId  the unique ID of the current generated iteration
     * @param theCalculator   the calculator allowed on the problem (null interpreted as NO_CALC)
     * @param theQuestion     the question document (may contain inputs)
     * @param theSolution     the optional solution document (may not contain inputs)
     * @param theAcceptNumber the specification of how the problem should accept a numeric answer
     */
    public ProblemNumericInst(final String theRef, final String theIterationId,
                              final ECalculatorType theCalculator, final DocColumnInst theQuestion,
                              final DocColumnInst theSolution, final ProblemAcceptNumberInst theAcceptNumber) {

        super(EProblemType.NUMERIC, theRef, theIterationId, theCalculator, theQuestion, theSolution);

        if (theAcceptNumber == null) {
            throw new IllegalArgumentException("Number acceptance settings may not be null");
        }

        this.acceptNumber = theAcceptNumber;
    }

    /**
     * Gets the number acceptance parameters.
     *
     * @return the number acceptance parameters
     */
    public ProblemAcceptNumberInst getAcceptNumber() {

        return this.acceptNumber;
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return problemInstHashCode() + Objects.hashCode(this.acceptNumber);
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
        } else if (obj instanceof final ProblemNumericInst problem) {
            equal = checkProblemInstEquals(problem)
                    && Objects.equals(this.acceptNumber, problem.acceptNumber);
        } else {
            equal = false;
        }

        return equal;
    }
}
