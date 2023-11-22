package dev.mathops.assessment.problem.inst;

import dev.mathops.assessment.document.inst.DocColumnInst;
import dev.mathops.assessment.problem.ECalculatorType;
import dev.mathops.assessment.problem.EProblemType;

import java.util.ArrayList;
import java.util.List;

/**
 * This subclass of {@code AbstractProblemIteration} adds a set of choices from which the student will choose.
 */
abstract class AbstractProblemMultipleChoiceInst extends AbstractProblemInst {

    /**
     * T set of {@code ProblemChoiceInstance} to be offered to the student, in the order in which they should be
     * presented.
     */
    private final List<ProblemChoiceInst> choices;

    /**
     * Constructs a new {@code AbstractProblemMultipleChoiceIteration}.
     *
     * @param theType        the problem type
     * @param theRef         the unique position in the organizational tree of instructional material of the template
     *                       that generated this iteration
     * @param theIterationId the unique ID of the current generated iteration
     * @param theCalculator  the calculator allowed on the problem (null interpreted as NO_CALC)
     * @param theQuestion    the question document (may contain inputs)
     * @param theSolution    the optional solution document (may not contain inputs)
     * @param theChoices     the ordered list of choices
     */
    AbstractProblemMultipleChoiceInst(final EProblemType theType, final String theRef,
                                      final String theIterationId, final ECalculatorType theCalculator,
                                      final DocColumnInst theQuestion, final DocColumnInst theSolution,
                                      final List<ProblemChoiceInst> theChoices) {

        super(theType, theRef, theIterationId, theCalculator, theQuestion, theSolution);

        if (theChoices == null || theChoices.isEmpty()) {
            throw new IllegalArgumentException("Choices list may not be null or empty");
        }

        this.choices = new ArrayList<>(theChoices);
    }

    /**
     * Gets a particular presented choice.
     *
     * @param index the index of the choice, as it is to be presented (0 is the first presented choice, 1 is the second,
     *              and so on)
     * @return the requested choice
     */
    public final ProblemChoiceInst getPresentedChoice(final int index) {

        return this.choices.get(index);
    }

    /**
     * Gets the choice with a specified choice ID.
     *
     * @param choiceId the choice ID
     * @return the matching choice; null if none found
     */
    final ProblemChoiceInst getChoiceById(final int choiceId) {

        ProblemChoiceInst result = null;

        for (final ProblemChoiceInst test : this.choices) {
            if (test.choiceId == choiceId) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Gets the list of choices.
     *
     * @return the list of choices (a copy; altering the returned list does not alter this object)
     */
    final List<ProblemChoiceInst> getChoices() {

        return new ArrayList<>(this.choices);
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    final int problemMultipleChoiceInstHashCode() {

        return problemInstHashCode() + this.choices.hashCode();
    }

    /**
     * Tests non-transient member variables in this base class for equality with another instance.
     *
     * @param other the other instance
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    final boolean checkProblemMultipleChoiceInstEquals(final AbstractProblemMultipleChoiceInst other) {

        return checkProblemInstEquals(other) && this.choices.equals(other.choices);
    }
}
