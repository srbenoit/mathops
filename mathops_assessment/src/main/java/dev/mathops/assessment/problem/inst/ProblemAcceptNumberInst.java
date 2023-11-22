package dev.mathops.assessment.problem.inst;

/**
 * Parameters for acceptance of a number in a numeric problem instance.
 */
public final class ProblemAcceptNumberInst {

    /** True if the answer must be an integer; false otherwise. */
    private final boolean forceInteger;

    /** The correct answer. */
    private final double correctAnswer;

    /** The permitted variance from the correct answer in order for a student's response to be considered correct. */
    private final double variance;

    /**
     * Constructs a new {@code ProblemAcceptNumberIteration}.
     *
     * @param isForceInteger   if the student is only allowed to enter an integer
     * @param theCorrectAnswer the correct answer
     * @param theVariance      the variance
     */
    public ProblemAcceptNumberInst(final boolean isForceInteger, final double theCorrectAnswer,
                                   final double theVariance) {

        this.forceInteger = isForceInteger;
        this.correctAnswer = theCorrectAnswer;
        this.variance = theVariance;
    }

    /**
     * Tests whether the response must be an integer.
     *
     * @return {@code true} if the response must be an integer
     */
    public boolean isForceInteger() {

        return this.forceInteger;
    }

    /**
     * Gets the correct answer.
     *
     * @return the correct answer
     */
    public double getCorrectAnswer() {

        return this.correctAnswer;
    }

    /**
     * Gets the allowed variance.
     *
     * @return the allowed variance
     */
    public double getVariance() {

        return this.variance;
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return (this.forceInteger ? 0x00100000 : 0) + Double.hashCode(this.correctAnswer)
                + Double.hashCode(this.variance);
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
        } else if (obj instanceof final ProblemAcceptNumberInst accept) {
            equal = this.forceInteger == accept.forceInteger
                    && this.correctAnswer == accept.correctAnswer
                    && this.variance == accept.variance;
        } else {
            equal = false;
        }

        return equal;
    }
}
