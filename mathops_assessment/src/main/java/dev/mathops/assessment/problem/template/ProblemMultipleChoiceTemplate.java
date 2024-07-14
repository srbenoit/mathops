package dev.mathops.assessment.problem.template;

import dev.mathops.assessment.Randomizer;
import dev.mathops.assessment.document.inst.DocColumnInst;
import dev.mathops.assessment.formula.ErrorValue;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.problem.EProblemType;
import dev.mathops.assessment.problem.inst.ProblemChoiceInst;
import dev.mathops.assessment.problem.inst.ProblemMultipleChoiceInst;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.IExcludableVariable;
import dev.mathops.assessment.variable.IRangedVariable;
import dev.mathops.assessment.variable.VariableBoolean;
import dev.mathops.assessment.variable.VariableDerived;
import dev.mathops.assessment.variable.VariableInputInteger;
import dev.mathops.assessment.variable.VariableInputReal;
import dev.mathops.assessment.variable.VariableInteger;
import dev.mathops.assessment.variable.VariableRandomBoolean;
import dev.mathops.assessment.variable.VariableRandomChoice;
import dev.mathops.assessment.variable.VariableRandomInteger;
import dev.mathops.assessment.variable.VariableRandomPermutation;
import dev.mathops.assessment.variable.VariableRandomReal;
import dev.mathops.assessment.variable.VariableRandomSimpleAngle;
import dev.mathops.assessment.variable.VariableReal;
import dev.mathops.assessment.variable.VariableSpan;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This subclass of {@code Problem} adds the necessary data to present a multiple-choice question to the student, by
 * storing a set of choices. On a multiple-choice question, the student will be asked to select the single, best answer
 * to the problem.
 */
public final class ProblemMultipleChoiceTemplate extends AbstractProblemMultipleChoiceTemplate {

    /** The choice that the student selected. */
    private Long selectedChoice;

    /**
     * Constructs a new {@code ProblemMultipleChoice}.
     */
    public ProblemMultipleChoiceTemplate() {

        super();
    }

    /**
     * Makes a clone of the problem. The clone is a deep copy such that any changes to the clone or its contained
     * objects will not change the original object (references are copied only when the underlying object is immutable,
     * otherwise contained objects are cloned). The exceptions to this is that the creation timestamp on the new problem
     * is set to the time when the clone is constructed, and the realization and completion timestamps of the clone are
     * set to zero. The clone also does not carry over the entered student answers from the original.
     *
     * @return a copy of the original object
     */
    @Override
    public ProblemMultipleChoiceTemplate deepCopy() {

        final ProblemMultipleChoiceTemplate copy = new ProblemMultipleChoiceTemplate();

        innerDeepCopy(copy);

        for (final ProblemChoiceTemplate choice : getChoices()) {
            copy.addChoice(choice.deepCopy());
        }

        if (this.numChoices != null) {
            copy.numChoices = this.numChoices.deepCopy();
        }

        if (this.randomOrderChoices != null) {
            copy.randomOrderChoices = this.randomOrderChoices.deepCopy();
        }

        if (this.choiceOrder != null) {
            copy.choiceOrder = new int[this.choiceOrder.length];

            System.arraycopy(this.choiceOrder, 0, copy.choiceOrder, 0, this.choiceOrder.length);
        }

        return copy;
    }

    /**
     * Gets type of problem.
     *
     * @return the problem type
     */
    @Override
    public EProblemType getType() {

        return EProblemType.MULTIPLE_CHOICE;
    }

    /**
     * Gets the selected choice.
     *
     * @return the choice
     */
    public Long getSelectedChoice() {

        return this.selectedChoice;
    }

    /**
     * Records a student's answer. The only acceptable object that can be sent in is a single Integer object, containing
     * the choice number selected.
     *
     * @param response a list of answer objects
     */
    @Override
    public void recordAnswer(final Object[] response) {

        if (response == null) {
            throw new IllegalArgumentException("Answer object array may not be null");
        } else if (response.length > 1) {
            throw new IllegalArgumentException("Multiple Choice problems may not have more than one answer object.");
        } else if (response.length == 0) {
            throw new IllegalArgumentException("Answer object array may not be empty");
        }

        // Record the choice answer
        if (response[0] instanceof Long) {
            this.selectedChoice = (Long) response[0];
        } else {
            throw new IllegalArgumentException("Answer object for multiple choice problems must be Integer");
        }

        // The following causes ProblemListeners to be notified.
        super.recordAnswer(response);
    }

    /**
     * Clears a student's answer.
     */
    @Override
    public void clearAnswer() {

        this.selectedChoice = null;

        // The following causes ProblemListeners to be notified.
        super.clearAnswer();
    }

    /**
     * Determines whether the student has recorded an answer to the problem.
     *
     * @return {@code true} if the student has recorded an answer; {@code false} otherwise
     */
    @Override
    public boolean isAnswered() {

        return this.selectedChoice != null;
    }

    /**
     * Tests to see whether a particular student response is correct.
     *
     * @param response the student response. A one-Integer array containing the choice selected
     * @return {@code true} if correct, {@code false} if incorrect
     */
    @Override
    public boolean isCorrect(final Object[] response) {

        boolean correct = false;

        if (response != null) {

            if (response.length == 1 && response[0] != null) {
                final int which = ((Long) response[0]).intValue() - 1;

                if (which >= 0 && which < getChoices().size()) {
                    final ProblemChoiceTemplate choice = getChoices().get(which);
                    final Formula form = choice.correct;
                    final Object result = form.evaluate(this.evalContext);

                    if (result instanceof Boolean) {
                        correct = ((Boolean) result).booleanValue();
                    } else {
                        Log.warning("Can't evaluate correctness");
                    }
                } else {
                    Log.warning("Invalid response to multiple choice");
                }
            } else {
                Log.warning("Response to multiple choice incorrect format");
            }
        }

        return correct;
    }

    /**
     * Generates a realized multiple choice answer problem.
     *
     * @param context the evaluation context
     * @return {@code true} if realization succeeded; {@code false} otherwise
     */
    @Override
    public boolean realize(final EvalContext context) {

        // Realize the superclass, which generates parameters and realizes the Question, Solution
        // and Hint Doc objects
        if (!super.realize(context)) {
            return false;
        }

        // Determine the number of visible choices that will be included.
        int size;

        if (this.numChoices != null) {
            final Object obj = this.numChoices.evaluate(context);

            if (obj instanceof Long) {
                size = (int) ((Long) obj).longValue();

                if (size > getChoices().size()) {
                    size = getChoices().size();
                }
            } else {
                Log.warning(this.id, ": Invalid objecch type for number of choices");

                if (obj instanceof ErrorValue) {
                    Log.warning(obj);
                }

                return false;
            }
        } else {
            // If no formula specified, show all choices.
            size = getChoices().size();
        }

        // Realize each of the Choice objects
        int correct = 0;

        for (final ProblemChoiceTemplate choice : getChoices()) {

            if (!choice.realize(context)) {
                return false;
            }

            // Make sure choice has a correctness formula
            if (choice.correct == null) {
                Log.warning(this.id, ": Choice has no correctness formula");

                return false;
            }

            final Object result = choice.correct.evaluate(context);

            if (result instanceof ErrorValue) {
                Log.warning(result);

                return false;
            }

            // See if choice is correct, and if so count it
            if (!(result instanceof Boolean)) {
                Log.warning(this.id, ": Unable to evaluate correctness of choice " + choice.choiceId);

                return false;
            }

            if (((Boolean) result).booleanValue()) {
                ++correct;
            }
        }

        // Ensure the number of correct answers will permit us to generate a problem.
        if (correct == 0) {
            Log.warning(this.id, ": No choices evaluate to being correct");

            return false;
        } else if (correct > 1) {

            // Multiple choices are correct - we will try to generate a problem where only one of
            // these is displayed, but we must check to see if this is going to be possible. We do
            // this by testing whether there are enough "incorrect" answers to fill all but one of
            // the displayed choices.
            final int incorrect = getChoices().size() - correct;

            if (incorrect < size - 1) {
                Log.warning(this.id, ": Too many answers are correct - cannot generate choice list");

                return false;
            }
        }

        // Determine whether random ordering is to be used
        boolean isRandom = false;

        if (this.randomOrderChoices != null) {
            final Object result = this.randomOrderChoices.evaluate(context);

            if (result instanceof ErrorValue) {
                Log.warning(result);

                return false;
            } else if (result instanceof Boolean) {
                isRandom = ((Boolean) result).booleanValue();
            } else {
                Log.warning(this.id, ": Problem's random-order formula does not evaluate to a boolean.");

                return false;
            }
        }

        // The presented choice ordering will be stored here.
        this.choiceOrder = new int[size];

        for (int i = 0; i < size; ++i) {
            this.choiceOrder[i] = -1;
        }

        // If no random ordering, we choose the first choices from the list. Once we find a
        // "correct" choice, we begin skipping over other correct choices to make sure we only get
        // one.
        if (!isRandom) {
            correct = 0;
            int which = 0;

            if (size > 0) {
                final List<ProblemChoiceTemplate> choices = getChoices();
                final int count = choices.size();

                for (int i = 0; i < count; ++i) {
                    final ProblemChoiceTemplate choice = choices.get(which);

                    final Object result = choice.correct.evaluate(context);

                    if (((Boolean) result).booleanValue()) {

                        // If we already added a correct choice, skip any others
                        if (correct > 0) {
                            continue;
                        }

                        // If not, indicate that now we have added one.
                        ++correct;
                    }

                    this.choiceOrder[which] = i;
                    which++;

                    if (which == size) {
                        break;
                    }
                }
            }
        }

        // If ordering is random, we loop, trying random selections of choices until we get a set
        // that has the right number of choices with exactly one correct. We should eventually get
        // one (not looping infinitely) since we tested the necessary conditions above.
        if (isRandom) {
            final List<ProblemChoiceTemplate> choices = getChoices();
            final int choicesLen = choices.size();
            final List<Long> list = new ArrayList<>(choicesLen);

            do {
                // Re-clear the choice order array
                for (int i = 0; i < size; ++i) {
                    this.choiceOrder[i] = -1;
                }

                // Build a list containing all integers from 0 through N-1
                list.clear();

                for (int i = 0; i < choicesLen; ++i) {
                    list.add(Long.valueOf((long) i));
                }

                // If any choices have hard-coded position, place them
                for (int i = 0; i < choicesLen; ++i) {
                    final ProblemChoiceTemplate choice = choices.get(i);

                    if (choice.pos > 0 && choice.pos <= this.choiceOrder.length) {
                        list.remove(Long.valueOf((long) i));
                        this.choiceOrder[choice.pos - 1] = i;
                    }
                }

                // Randomly order remaining items in list into mChoiceOrder.
                for (int i = 0; i < size; ++i) {

                    if (this.choiceOrder[i] == -1) {
                        final int which = Randomizer.nextInt(list.size());
                        final Long value = list.get(which);
                        list.remove(which);
                        this.choiceOrder[i] = value.intValue();
                    }
                }

                // See that there is exactly one choice that is correct
                correct = 0;

                for (int i = 0; i < size; ++i) {
                    final ProblemChoiceTemplate choice = choices.get(this.choiceOrder[i]);

                    final Object result = choice.correct.evaluate(context);

                    if (((Boolean) result).booleanValue()) {
                        ++correct;
                    }
                }

            } while (correct != 1);
        }

        return true;
    }

    /**
     * Realizes the problem and creates a static {@code ProblemMultipleChoiceIteration} based on that realization.
     *
     * @return the generated {@code ProblemMultipleChoiceIteration}; {@code null} if realization or creation of the
     *         iteration failed
     */
    @Override
    public ProblemMultipleChoiceInst createIteration() {

        ProblemMultipleChoiceInst result = null;

        if (realize(this.evalContext)) {
            // 9 characters ~ 15.9 bits of ID, 7.4E+15 possibilities
            final String iterationId = CoreConstants.newId(9);

            boolean ok = true;
            final int orderLen = this.choiceOrder.length;
            final List<ProblemChoiceInst> choiceIterations = new ArrayList<>(orderLen);
            for (int i = 0; i < orderLen; ++i) {
                final ProblemChoiceTemplate choiceTemplate = getPresentedChoice(i);
                final ProblemChoiceInst choiceIter = choiceTemplate.createIteration(this.evalContext);
                if (choiceIter == null) {
                    Log.warning("Unable to generate choice iteration");
                    ok = false;
                    break;
                }
                choiceIterations.add(choiceIter);
            }

            if (ok) {
                final DocColumnInst questionIteration = this.question.createInstance(this.evalContext);

                if (this.solution == null) {
                    result = new ProblemMultipleChoiceInst(this.id, iterationId, this.calculator,
                            questionIteration, null, choiceIterations);
                } else {
                    final DocColumnInst solutionIteration = this.solution.createInstance(this.evalContext);

                    result = new ProblemMultipleChoiceInst(this.id, iterationId, this.calculator,
                            questionIteration, solutionIteration, choiceIterations);
                }
            }
        }

        return result;
    }

    /**
     * Generates a string representation of the object.
     *
     * @return the string representation of the object
     */
    @Override
    public String toString() {

        final HtmlBuilder buf = new HtmlBuilder(1000);

        buf.addln("<html><head></head><body>");
        buf.addln("<h3>Multiple Choice Problem</h3>");

        if (this.numChoices != null) {
            buf.add("<p>Number of choices to show: ", this.numChoices, "</p>");
        }

        if (this.id != null) {
            buf.addln("<p><b>Reference base:</b> ", this.id, "</p>");
        }

        if (this.evalContext != null && this.evalContext.numVariables() > 0) {
            buf.addln("<p><b>Parameters:</b></p>");

            for (final AbstractVariable abstractVariable : this.evalContext.getVariables()) {
                buf.addln("<table border='1'>");

                buf.add("<tr><td><b>Name:</b></td><td>", abstractVariable.name);

                switch (abstractVariable) {
                    case VariableInteger variableInteger -> buf.add(" (Integer)");
                    case VariableReal variableReal -> buf.add(" (Real)");
                    case VariableBoolean variableBoolean -> buf.add(" (Boolean)");
                    case VariableSpan variableSpan -> buf.add(" (Span)");
                    case VariableRandomInteger variableRandomInteger -> buf.add(" (Random Integer)");
                    case VariableRandomReal variableRandomReal -> buf.add(" (Random Real)");
                    case VariableRandomPermutation variableRandomPermutation -> buf.add(" (Random Permutation)");
                    case VariableRandomBoolean variableRandomBoolean -> buf.add(" (Random Boolean)");
                    case VariableRandomChoice variableRandomChoice -> buf.add(" (Random Choice)");
                    case VariableRandomSimpleAngle variableRandomSimpleAngle -> buf.add(" (Random Simple Angle)");
                    case VariableDerived variableDerived -> buf.add(" (Derived)");
                    case VariableInputInteger variableInputInteger -> buf.add(" (Input Int)");
                    case VariableInputReal variableInputReal -> buf.add(" (Input Real)");
                    default -> buf.add(" (Unknown)");
                }

                buf.addln("</td></tr>");

                if (abstractVariable instanceof final IRangedVariable ranged) {
                    if (ranged.getMin() != null) {
                        buf.addln("<tr><td><b>Minimum:</b></td><td>", ranged.getMin(), "</td></tr>");
                    }
                    if (ranged.getMax() != null) {
                        buf.addln("<tr><td><b>Maximum:</b></td><td>", ranged.getMax(), "</td></tr>");
                    }
                }

                if (abstractVariable instanceof final IExcludableVariable excludable) {
                    final Formula[] excl = excludable.getExcludes();

                    if (excl != null) {
                        for (final Formula formula : excl) {
                            buf.addln("<tr><td><b>Exclude:</b></td><td>", formula, "</td></tr>");
                        }
                    }
                }

                if (abstractVariable instanceof final VariableDerived der && der.getFormula() != null) {
                    buf.addln("<tr><td><b>Formula:</b></td><td>", der.getFormula(), "</td></tr>");
                }

                final Object value = abstractVariable.getValue();

                if (value != null) {
                    switch (value) {
                        case Long l -> buf.add("<tr><td><b>Integer Value:</b></td><td>", value, "</td></tr>");
                        case Number number -> buf.add("<tr><td><b>Real Value:</b></td><td>", value, "</td></tr>");
                        case Boolean b -> buf.add("<tr><td><b>Boolean Value:</b></td><td>", value, "</td></tr>");
                        case String s -> buf.add("<tr><td><b>String Value:</b></td><td>", value, "</td></tr>");
                        default -> {
                        }
                    }
                }

                buf.addln("</table><br>");
            }
        }

        if (this.question != null) {
            buf.addln("<p><b>Question:</b></p>");
            buf.add(this.question);
        }

        if (this.solution != null) {
            buf.addln("<p><b>Solution:</b></p>");
            buf.add(this.solution);
        }

        final List<ProblemChoiceTemplate> choices = getChoices();
        for (final ProblemChoiceTemplate choice : choices) {
            buf.addln("<p><b>Choice:</b></p>");
            buf.add(choice.doc);
        }

        buf.addln("</body></html>");

        return buf.toString();
    }

    /**
     * Emits the start of the opening &lt;problem-... tag (attributes can be emitted after this).
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     * @param indent  the indentation string
     */
    @Override
    public void openTopLevelTag(final HtmlBuilder builder, final String indent) {

        builder.add(indent, "<problem-multiple-choice");
    }

    /**
     * Emits the closing &lt;/problem-...&gt; tag.
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     * @param indent  the indentation string
     */
    @Override
    public void closeTopLevelTag(final HtmlBuilder builder, final String indent) {

        builder.addln(indent, "</problem-multiple-choice>");
    }

    /**
     * A method that subclasses override to print their subclass-specific attributes on the problem element.
     *
     * @param builder The {@code HtmlBuilder} to which to write the XML.
     */
    @Override
    public void printSubclassAttributes(final HtmlBuilder builder) {

        super.printSubclassAttributes(builder);

        if (this.selectedChoice != null) {
            writeAttribute(builder, "student-choices", this.selectedChoice);
        }
    }

    /**
     * Generate the LaTeX representation of the problem.
     *
     * @param dir          The directory in which the LaTeX source files are being written.
     * @param fileIndex    A 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file. The value should be updated if the method writes any files.
     * @param overwriteAll A 1-boolean array whose only entry contains True if the user has selected "overwrite all";
     *                     false to ask the user each time. This method can update this value to true if it is false and
     *                     the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL].
     * @param builder      The {@code HtmlBuilder} to which to write the LaTeX.
     * @param showAnswers  True to show the correct answers; false to leave blank
     * @param mode         The current LaTeX mode (T=text, $=in-line math, M=math).
     */
    @Override
    public void toLaTeX(final File dir, final int[] fileIndex, final boolean[] overwriteAll, final HtmlBuilder builder,
                        final boolean showAnswers, final char[] mode, final EvalContext context) {

        // Emit the problem reference, for debugging
        builder.addln("% ", this.id);

        // Write the question, followed by a blank line
        this.question.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);

        if (mode[0] == '$') {
            builder.add("$");
        } else if (mode[0] == 'M') {
            builder.add("\\]");
        }

        mode[0] = 'T';

        // Output each choice preceded by a bubble that can be filled in.
        // We organize this into a table so the circles are lined up and the
        // choices are lined up and line-wrap properly.
        builder.addln("\\bgroup");
        builder.addln("\\def\\arraystretch{1.2}");
        builder.addln("\\[\\begin{tabular}{r l}");

        for (final int j : this.choiceOrder) {
            final ProblemChoiceTemplate choice = getChoices().get(j);

            if (showAnswers && Boolean.TRUE.equals(choice.correct.evaluate(this.evalContext))) {
                builder.add(" \\xy<3pt,3pt>*\\cir<6pt>{} *\\cir<5pt>{} *\\cir<4pt>{} ",
                        "*\\cir<3pt>{} *\\cir<2pt>{} *\\cir<1pt>{}\\endxy & ");
            } else {
                builder.add(" \\xy<3pt,3pt>*\\cir<6pt>{}\\endxy & ");
            }

            choice.doc.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);

            if (mode[0] == '$') {
                builder.add("$");
            } else if (mode[0] == 'M') {
                builder.add("\\]");
            }

            mode[0] = 'T';
            builder.add("\\\\");
        }

        builder.addln("\\end{tabular}\\]");
        builder.addln("\\egroup");
    }

    /**
     * Extracts answers from a parameter map (a map from string parameter name to string array of values).
     *
     * @param paramMap the parameter map
     */
    @Override
    public void extractAnswers(final Map<String, String[]> paramMap) {

        final String[] choices = paramMap.get("CHOICE");

        if (choices != null) {
            final int len = choices.length;

            if (len >= 1) {
                final Long[] ans = new Long[len];
                try {
                    for (int i = 0; i < len; ++i) {
                        ans[i] = Long.valueOf(choices[i]);
                    }
                    recordAnswer(ans);
                } catch (final NumberFormatException ex) {
                    Log.warning("ProblemMultipleChoice: Invalid value for CHOICE parameter", ex);
                    clearAnswer();
                }
            } else {
                clearAnswer();
            }
        } else {
            clearAnswer();
        }
    }

    /**
     * Alters the emitted question or solution HTML to include the student's current answer (if any).
     *
     * @param origHtml the HTML without student answers inserted
     * @return the HTML with student answers inserted
     */
    @Override
    public String insertAnswers(final String origHtml) {

        // Inputs are of this form:
        // <input type='radio' name='CHOICE' id='CHOICE_###' value='###'>

        final String result;

        if (this.selectedChoice == null) {
            result = origHtml;
        } else {
            final int index = origHtml.indexOf("id='CHOICE_" + this.selectedChoice + "'");

            if (index == -1) {
                Log.warning("Unable to locate selected choice input in multiple choice problem.");
                result = origHtml;
            } else {
                result = origHtml.substring(0, index) + "checked style='margin-left:10px;' " +
                        origHtml.substring(index);
            }
        }

        return result;
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return mcInnerHashCode() + Objects.hashCode(this.selectedChoice);
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
        } else if (obj instanceof final ProblemMultipleChoiceTemplate problem) {
            equal = mcInnerEquals(problem)
                    && Objects.equals(this.selectedChoice, problem.selectedChoice);
        } else {
            equal = false;
        }

        return equal;
    }
}
