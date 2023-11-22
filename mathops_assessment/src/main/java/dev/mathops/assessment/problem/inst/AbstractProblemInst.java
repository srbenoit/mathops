package dev.mathops.assessment.problem.inst;

import dev.mathops.assessment.document.inst.DocColumnInst;
import dev.mathops.assessment.problem.ECalculatorType;
import dev.mathops.assessment.problem.EProblemType;
import dev.mathops.core.EqualityTests;

import java.util.Objects;

/**
 * An instance of a single problem.
 *
 * <p>
 * An instance of a problem is generated from a template.  All non-input variables defined in the problem's evaluation
 * context have been assigned values, and all objects have used those values to generate static instances.
 *
 * <p>
 * An instance is stored in a binary format for high-efficiency parsing. Utilities exist to generate HTML/SVG or LaTeX
 * from a problem instance, or to present it in a native Java UI.
 */
public abstract class AbstractProblemInst {

    /** The problem type. */
    private final EProblemType type;

    /**
     * The unique position in the organizational tree of instructional material of the template that generated this
     * instance.
     */
    private final String ref;

    /** The unique ID of the current generated instance. */
    private final String instanceId;

    /** The calculator allowed on the problem. */
    private final ECalculatorType calculator;

    /** The question, with all template variables substituted. */
    private final DocColumnInst question;

    /** The optional solution, with all template variables substituted. */
    private final DocColumnInst solution;

    /**
     * Constructs an empty {@code AbstractProblemInst} object.
     *
     * @param theType        the problem type
     * @param theRef         the unique position in the organizational tree of instructional material of the template
     *                       that generated this instance
     * @param theIterationId the unique ID of the current generated instance
     * @param theCalculator  the calculator allowed on the problem (null interpreted as NO_CALC)
     * @param theQuestion    the question document (may contain inputs)
     * @param theSolution    the optional solution document (may not contain inputs)
     */
    AbstractProblemInst(final EProblemType theType, final String theRef,
                        final String theIterationId, final ECalculatorType theCalculator,
                        final DocColumnInst theQuestion, final DocColumnInst theSolution) {

        if (theType == null) {
            throw new IllegalArgumentException("Type may not be null");
        }
        if (theRef == null) {
            throw new IllegalArgumentException("Ref may not be null");
        }
        if (theIterationId == null) {
            throw new IllegalArgumentException("Iteration ID may not be null");
        }
        if (theQuestion == null) {
            throw new IllegalArgumentException("Question document may not be null");
        }

        this.type = theType;
        this.ref = theRef;
        this.instanceId = theIterationId;
        this.calculator = theCalculator == null ? ECalculatorType.NO_CALC : theCalculator;
        this.question = theQuestion;
        this.solution = theSolution;
    }

    /**
     * Gets the problem type.
     *
     * @return the type
     */
    public final EProblemType getType() {

        return this.type;
    }

    /**
     * Gets the unique position in the organizational tree of instructional material of the template that generated this
     * instance.
     *
     * @return the reference
     */
    public final String getRef() {

        return this.ref;
    }

    /**
     * Gets the unique ID of the current generated instance (unique within the reference).
     *
     * @return the instance ID
     */
    public final String getInstanceId() {

        return this.instanceId;
    }

    /**
     * Gets the calculator allowed on the problem.
     *
     * @return the calculator type
     */
    public final ECalculatorType getCalculator() {

        return this.calculator;
    }

    /**
     * Gets the question.
     *
     * @return the question
     */
    public final DocColumnInst getQuestion() {

        return this.question;
    }

    /**
     * Gets the solution.
     *
     * @return the solution
     */
    public final DocColumnInst getSolution() {

        return this.solution;
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    final int problemInstHashCode() {

        return this.type.hashCode() + this.ref.hashCode() + this.instanceId.hashCode()
                + this.calculator.hashCode() + this.question.hashCode()
                + EqualityTests.objectHashCode(this.solution);
    }

    /**
     * Tests non-transient member variables in this base class for equality with another instance.
     *
     * @param other the other instance
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    final boolean checkProblemInstEquals(final AbstractProblemInst other) {

        return this.type == other.type && this.ref.equals(other.ref)
                && this.instanceId.equals(other.instanceId)
                && this.calculator == other.calculator && this.question.equals(other.question)
                && Objects.equals(this.solution, other.solution);
    }
}
