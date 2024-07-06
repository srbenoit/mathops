package dev.mathops.assessment.problem.template;

import dev.mathops.assessment.Randomizer;
import dev.mathops.assessment.document.inst.DocColumnInst;
import dev.mathops.assessment.formula.ErrorValue;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.problem.EProblemType;
import dev.mathops.assessment.problem.inst.ProblemChoiceInst;
import dev.mathops.assessment.problem.inst.ProblemMultipleSelectionInst;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This subclass of {@code Problem} adds the necessary data to present a multiple-selection question to the student, by
 * storing a set of choices. On a multiple-selection problem, the student will be asked to select all answers that are
 * correct. There may be more than one answer correct. There should not be a situation in which no answers are correct,
 * since we will not then know whether the student simply neglected to answer, or intended to leave the answer
 * unselected. To support this, include a "none of these" option (or something similar) in the list of choices.
 */
public final class ProblemMultipleSelectionTemplate extends AbstractProblemMultipleChoiceTemplate {

    /** An empty array of longs. */
    private static final Long[] ZERO_LEN_LONG_ARR = new Long[0];

    /** The list of choices that the student has selected. */
    private Long[] selectedChoiceList;

    /**
     * An optional formula for the minimum number of correct answers to include in the list presented to students.
     */
    public Formula minCorrect;

    /**
     * An optional formula for the maximum number of correct answers to include in the list presented to students.
     */
    public Formula maxCorrect;

    /**
     * Constructs an empty {@code ProblemMultipleSelection} object.
     */
    public ProblemMultipleSelectionTemplate() {

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
    public ProblemMultipleSelectionTemplate deepCopy() {

        final ProblemMultipleSelectionTemplate copy = new ProblemMultipleSelectionTemplate();

        innerDeepCopy(copy);

        if (this.minCorrect != null) {
            copy.minCorrect = this.minCorrect.deepCopy();
        }

        if (this.maxCorrect != null) {
            copy.maxCorrect = this.maxCorrect.deepCopy();
        }

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

        return EProblemType.MULTIPLE_SELECTION;
    }

    /**
     * Gets the list of selected choices.
     *
     * @return the list
     */
    public Long[] getSelectedChoiceList() {

        return this.selectedChoiceList;
    }

    /**
     * Records a student's answer. The only acceptable object that can be sent in is an array of Integer objects,
     * containing the list of choice numbers selected.
     *
     * @param response a list of answer objects - the answers will be passed directly into the PresentedProblem object
     */
    @Override
    public void recordAnswer(final Object[] response) {

        if (response == null) {
            throw new IllegalArgumentException("Answer object array may not be null");
        } else if (response.length == 0) {
            throw new IllegalArgumentException("Answer object array may not be empty");
        }

        final int len = response.length;
        final Long[] ints = new Long[len];

        for (int i = 0; i < len; ++i) {

            if (response[i] instanceof Long) {
                ints[i] = (Long) response[i];
            } else {
                throw new IllegalArgumentException(
                        "Answer object for multiple selection problems must be Integer array");
            }
        }

        // Record the choice answers
        this.selectedChoiceList = ints;

        // The following causes ProblemListeners to be notified.
        super.recordAnswer(response);
    }

    /**
     * Clears a student's answer.
     */
    @Override
    public void clearAnswer() {

        this.selectedChoiceList = null;

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

        return this.selectedChoiceList != null;
    }

    /**
     * Tests to see whether a particular student response is correct.
     *
     * @param response the student response - an Integer array containing the choices selected
     * @return {@code true} if correct, {@code false} if incorrect
     */
    @Override
    public boolean isCorrect(final Object[] response) {

        boolean correct = true;

        // We have two sets: responses and correct answers. We test set
        // equality by seeing if each is included in the other.

        // First, see if all responses are correct
        if (response != null) {

            final int len = response.length;
            for (int i = 0; correct && i < len; ++i) {

                if (response[i] != null) {
                    final int which = ((Long) response[i]).intValue() - 1;

                    if (which >= 0 && which < getChoices().size()) {
                        final ProblemChoiceTemplate choice = getChoices().get(which);
                        final Formula form = choice.correct;
                        final Object result = form.evaluate(this.evalContext);

                        if (result instanceof Boolean) {
                            correct = ((Boolean) result).booleanValue();
                        } else {
                            Log.warning("Unable to evaluate correctness");
                        }
                    } else {
                        Log.warning("Response outside range of choices");
                    }
                } else {
                    Log.warning("Response included a null value");
                }
            }

            // Next see if all displayed correct answers are in responses.
            if (correct) {
                final int numOrder = this.choiceOrder.length;
                for (int which = 0; correct && which < numOrder; ++which) {
                    final ProblemChoiceTemplate choice = getChoices().get(this.choiceOrder[which]);
                    final Formula form = choice.correct;
                    final Object result = form.evaluate(this.evalContext);

                    if (result instanceof Boolean) {

                        if (((Boolean) result).booleanValue()) {

                            // Answer is correct, see if in student response
                            correct = false;

                            for (final Object o : response) {
                                if ((o != null) && (this.choiceOrder[which] == ((Long) o).intValue() - 1)) {
                                    correct = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        Log.warning("Correctness formula did not evaluate to a Boolean");
                    }
                }
            }
        } else {
            correct = false;
        }

        return correct;
    }

    /**
     * Generates a realized multiple selection answer problem.
     *
     * @param context the evaluation context
     * @return {@code true} if realization succeeded; {@code false} otherwise
     */
    @Override
    public boolean realize(final EvalContext context) {

        boolean isRandom = false;
        int correct = 0;

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
                size = ((Long) obj).intValue();

                if (size > getChoices().size()) {
                    size = getChoices().size();
                }
            } else {
                Log.warning(this.id, ": Invalid object type for number of choices");

                if (obj instanceof ErrorValue) {
                    Log.warning(obj);
                }

                return false;
            }
        } else {
            // If no formula specified, show all choices.
            size = getChoices().size();
        }

        // Set defaults for number correct to permit
        int minCor = 1;
        int maxCor = size;

        // Realize each of the Choice objects
        final List<ProblemChoiceTemplate> choices = getChoices();
        final int choicesLen = choices.size();

        for (int i = 0; i < choicesLen; ++i) {
            final ProblemChoiceTemplate choice = getChoices().get(i);

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
                correct++;
            }
        }

        // Derive the number of incorrect answers we have available
        final int incorrect = choicesLen - correct;

        if (this.minCorrect != null) {
            final Object obj = this.minCorrect.evaluate(context);

            if (obj instanceof Long) {
                minCor = ((Long) obj).intValue();
            } else {
                Log.warning(this.id, ": Invalid object type for minimum correct");

                if (obj instanceof ErrorValue) {
                    Log.warning(obj);
                }

                return false;
            }
        }

        if (this.maxCorrect != null) {
            final Object obj = this.maxCorrect.evaluate(context);

            if (obj instanceof Long) {
                maxCor = ((Long) obj).intValue();
            } else {
                Log.warning(this.id, ": Invalid object type for maximum correct");

                if (obj instanceof ErrorValue) {
                    Log.warning(obj);
                }

                return false;
            }
        }

        // Ensure the number of correct answers will permit us to generate a problem.
        if (correct == 0) {
            Log.warning(this.id, ": No choices evaluate to being correct");

            return false;
        } else if (correct < minCor) {
            Log.warning(this.id, ": Not enough choices evaluate to being correct");

            return false;
        } else if (incorrect < size - maxCor) {
            Log.warning(this.id, ": Too many choices evaluate to being correct");

            return false;
        }

        // Determine whether random ordering is to be used
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

        // If no random ordering, we choose the first choices from the list. Once we find max
        // allowed number of "correct" choices, we begin skipping over other correct choices.
        if (!isRandom) {
            correct = 0;
            int which = 0;

            if (size > 0) {

                for (int i = 0; i < choicesLen; ++i) {
                    final ProblemChoiceTemplate choice = choices.get(which);
                    final Object result = choice.correct.evaluate(context);

                    if (((Boolean) result).booleanValue()) {

                        // If we already have enough correct choices, skip rest
                        if (correct >= maxCor) {
                            continue;
                        }

                        // If not, indicate that now we have added one.
                        correct++;
                    } else // If we cannot take any more incorrect, skip
                        if (minCor - correct >= choicesLen - i) {
                            continue;
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
            final List<Long> list = new ArrayList<>(choicesLen);

            do {
                Arrays.fill(this.choiceOrder, -1);

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

                // Count the number of correct choices
                correct = 0;

                for (int i = 0; i < size; ++i) {
                    final ProblemChoiceTemplate choice = choices.get(this.choiceOrder[i]);
                    final Object result = choice.correct.evaluate(context);

                    if (((Boolean) result).booleanValue()) {
                        ++correct;
                    }
                }

                // See if this is an acceptable selection of choices
            } while (correct < minCor || correct > maxCor);
        }

        return true;
    }

    /**
     * Realizes the problem and creates a static {@code ProblemMultipleSelectionIteration} based on that realization.
     *
     * @return the generated {@code ProblemMultipleSelectionIteration}; {@code null} if realization or creation of the
     *         iteration failed
     */
    @Override
    public ProblemMultipleSelectionInst createIteration() {

        ProblemMultipleSelectionInst result = null;

        if (realize(this.evalContext)) {
            // 9 characters ~ 15.9 bits of ID, 7.4E+15 possibilities
            final String iterationId = CoreConstants.newId(9);

            boolean ok = true;
            final int orderLen = this.choiceOrder.length;

            final List<ProblemChoiceInst> choiceIterations =new ArrayList<>(orderLen);
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
                    result = new ProblemMultipleSelectionInst(this.id, iterationId, this.calculator,
                            questionIteration, null, choiceIterations);
                } else {
                    final DocColumnInst solutionIteration = this.solution.createInstance(this.evalContext);

                    result = new ProblemMultipleSelectionInst(this.id, iterationId, this.calculator,
                            questionIteration, solutionIteration, choiceIterations);
                }
            }
        }

        return result;
    }

    /**
     * Prints subclass-specific attributes on the problem element.
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     */
    @Override
    public void printSubclassAttributes(final HtmlBuilder builder) {

        final HtmlBuilder inner = new HtmlBuilder(50);

        super.printSubclassAttributes(builder);

        if (this.selectedChoiceList != null) {
            final int len = this.selectedChoiceList.length;

            for (int i = 0; i < len; ++i) {
                if (this.selectedChoiceList[i] != null) {
                    if (i != 0) {
                        inner.add(CoreConstants.COMMA_CHAR);
                    }
                    inner.add(this.selectedChoiceList[i].longValue());
                }
            }

            writeAttribute(builder, "student-choices", inner.toString());
        }
    }

    /**
     * Prints subclass-specific elements.
     *
     * @param builder    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void printSubclassXmlBegin(final HtmlBuilder builder, final int indent) {

        super.printSubclassXmlBegin(builder, indent);

        final String ind = makeIndent(indent);

        if (this.minCorrect != null) {
            builder.add(ind, "<min-correct>");
            this.minCorrect.appendChildrenXml(builder);
            builder.addln("</min-correct>");
        }

        if (this.maxCorrect != null) {
            builder.add(ind, "<max-correct>");
            this.maxCorrect.appendChildrenXml(builder);
            builder.addln("</max-correct>");
        }
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
        buf.addln("<h3>Multiple Selection Problem</h3>");

        if (this.numChoices != null) {
            buf.addln("<p>Number of choices to show: ", this.numChoices, "</p>");
        }

        if (this.minCorrect != null) {
            buf.addln("<p>Minimum number of correct choices to show: ", this.minCorrect, "</p>");
        }

        if (this.maxCorrect != null) {
            buf.addln("<p>Maximum number of correct choices to show: ", this.maxCorrect, "</p>");
        }

        if (this.id != null) {
            buf.sP().add("<b>Reference base:</b> ", this.id).eP();
        }

        if (this.evalContext != null && this.evalContext.numVariables() > 0) {
            buf.addln("<p><b>Parameters:</b></p>");

            for (final AbstractVariable abstractVariable : this.evalContext.getVariables()) {
                buf.addln("<table border='1'>");

                final Object value = abstractVariable.getValue();

                buf.addln("<tr><td><b>Name:</b></td><td>", abstractVariable.name);

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
                        buf.addln("<tr><td><b>Minimum:</b></td><td>", ((IRangedVariable) abstractVariable).getMin(),
                                "</td></tr>");
                    }
                    if (ranged.getMax() != null) {
                        buf.addln("<tr><td><b>Maximum:</b></td><td>", ((IRangedVariable) abstractVariable).getMax(),
                                "</td></tr>");
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

                if (value instanceof Long) {
                    buf.addln("<tr><td><b>Integer Value:</b></td><td>", value, "</td></tr>");
                } else if (value instanceof Number) {
                    buf.addln("<tr><td><b>Real Value:</b></td><td>", value, "</td></tr>");
                } else if (value instanceof Boolean) {
                    buf.addln("<tr><td><b>Boolean Value:</b></td><td>", value, "</td></tr>");
                } else if (value instanceof String) {
                    buf.addln("<tr><td><b>String Value:</b></td><td>", value, "</td></tr>");
                }

                buf.add("</table><br>");
            }
        }

        if (this.question != null) {
            buf.addln("<p><b>Question:</b></p>");
            buf.add(this.question.toString());
        }

        if (this.solution != null) {
            buf.addln("<p><b>Solution:</b></p>");
            buf.add(this.solution.toString());
        }

        final List<ProblemChoiceTemplate> choices = getChoices();
        if (choices != null && !choices.isEmpty()) {
            for (final ProblemChoiceTemplate choice : choices) {
                buf.addln("<p><b>Choice:</b></p>");
                buf.add(choice.doc.toString());
            }
        }

        buf.addln("</body></html>");

        return buf.toString();
    }

    /**
     * Generates the LaTeX representation of the problem.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file - the value should be updated if the method writes any files
     * @param overwriteAll a 1-boolean array whose only entry contains {@code true} if the user has selected "overwrite
     *                     all"; {@code false} to ask the user each time (this method can update this value to true if
     *                     it is false and the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL])
     * @param builder          the {@code HtmlBuilder} to which to write the LaTeX
     * @param showAnswers  true to show the correct answers; false to leave blank
     * @param mode         the current LaTeX mode (T=text, $=in-line math, M=math)
     */
    @Override
    public void toLaTeX(final File dir, final int[] fileIndex, final boolean[] overwriteAll,
                        final HtmlBuilder builder, final boolean showAnswers, final char[] mode,
                        final EvalContext context) {

        // Emit the problem reference, for debugging
        builder.addln("% ", this.id);

        // Write the question, followed by a blank line
        this.question.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);

        if (mode[0] == '$') {
            builder.add('$');
        } else if (mode[0] == 'M') {
            builder.add("\\]");
        }

        mode[0] = 'T';

        // Output each choice preceded by a bubble that can be filled in. We organize this into a
        // table so the circles are lined up and the choices are lined up and line-wrap properly.
        builder.addln("\\bgroup");
        builder.addln("\\def\\arraystretch{1.2}");
        builder.addln("\\[\\begin{tabular}{r l}");

        for (final int j : this.choiceOrder) {
            final ProblemChoiceTemplate choice = getChoices().get(j);

            if (showAnswers && Boolean.TRUE.equals(choice.correct.evaluate(this.evalContext))) {
                builder.add(" \\xy @={(0,0),(0,4),(4,4),(4,0),(0,0),(4,4),(4,0),(0,4)}, ",
                        "s0=\"prev\" @@{;\"prev\";**@{-}=\"prev\"} \\endxy &");
            } else {
                builder.add(" \\xy @={(0,0),(0,4),(4,4),(4,0)}, ", "s0=\"prev\" @@{;\"prev\";**@{-}=\"prev\"} \\endxy &");
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

        final Collection<Long> selected = new ArrayList<>(5);

        for (final ProblemChoiceTemplate choice : getChoices()) {
            final String[] choices = paramMap.get("CHOICE_" + choice.choiceId);

            if (choices != null && choices.length == 1) {
                selected.add(Long.valueOf((long) choice.choiceId));
            }
        }

        if (selected.isEmpty()) {
            clearAnswer();
        } else {
            recordAnswer(selected.toArray(ZERO_LEN_LONG_ARR));
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

        String result = origHtml;

        if (this.selectedChoiceList != null) {

            for (final Long sel : this.selectedChoiceList) {
                final int index = result.indexOf("id='CHOICE_" + sel + "'");

                if (index == -1) {
                    Log.warning("Unable to locate selected choice input in multiple selection problem.");
                } else {
                    result = result.substring(0, index) + "checked " + result.substring(index);
                }
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

        return mcInnerHashCode() + Objects.hashCode(this.selectedChoiceList)
                + Objects.hashCode(this.minCorrect)
                + Objects.hashCode(this.maxCorrect);
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
        } else if (obj instanceof final ProblemMultipleSelectionTemplate problem) {
            equal = mcInnerEquals(problem)
                    && Arrays.equals(this.selectedChoiceList, problem.selectedChoiceList)
                    && Objects.equals(this.minCorrect, problem.minCorrect)
                    && Objects.equals(this.maxCorrect, problem.maxCorrect);
        } else {
            equal = false;
        }

        return equal;
    }
}
