package dev.mathops.assessment.problem.inst;

import dev.mathops.assessment.document.inst.DocColumnInst;

import java.util.Objects;

/**
 * A single choice for an iteration of a multiple choice or multiple selection problem.
 */
public final class ProblemChoiceInst {

    /** The choice content document. */
    public final DocColumnInst doc;

    /** The ID of the choice, to be submitted as a student selection. */
    public final int choiceId;

    /** The flag indicating whether this choice is correct. */
    public final boolean correct;

    /**
     * Constructs a new {@code ProblemChoiceIteration}.
     *
     * @param theDoc      the choice content document
     * @param theChoiceId the choice ID, to be submitted as a student selection
     * @param isCorrect   the flag indicating whether this choice is correct
     */
    public ProblemChoiceInst(final DocColumnInst theDoc, final int theChoiceId,
                             final boolean isCorrect) {

        if (theDoc == null) {
            throw new IllegalArgumentException("Content document may not be null");
        }

        this.doc = theDoc;
        this.choiceId = theChoiceId;
        this.correct = isCorrect;
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.doc) + this.choiceId + Boolean.hashCode(this.correct);
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
        } else if (obj instanceof final ProblemChoiceInst choice) {
            equal = this.doc.equals(choice.doc) && this.choiceId == choice.choiceId && this.correct == choice.correct;
        } else {
            equal = false;
        }

        return equal;
    }
}
