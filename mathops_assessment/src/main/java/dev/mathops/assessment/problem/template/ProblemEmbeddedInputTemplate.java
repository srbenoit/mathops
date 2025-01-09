package dev.mathops.assessment.problem.template;

import dev.mathops.assessment.document.inst.DocColumnInst;
import dev.mathops.assessment.document.template.AbstractDocInput;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.document.template.DocInputCheckbox;
import dev.mathops.assessment.document.template.DocInputDoubleField;
import dev.mathops.assessment.document.template.DocInputLongField;
import dev.mathops.assessment.document.template.DocInputRadioButton;
import dev.mathops.assessment.document.template.DocInputStringField;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.problem.EProblemType;
import dev.mathops.assessment.problem.inst.ProblemEmbeddedInputInst;
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
import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.xml.XmlEscaper;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This subclass of {@code Problem} supports questions that include user inputs embedded within the question.
 */
public final class ProblemEmbeddedInputTemplate extends AbstractProblemTemplate {

    /** The formula used to evaluate correctness. */
    public Formula correctness;

    /** The solution. */
    public DocColumn correctAnswer;

    /** Flag indicating answers are being recorded, to prevent recursion. */
    private boolean recording;

    /**
     * Construct an empty {@code ProblemEmbeddedInputTemplate} object.
     */
    public ProblemEmbeddedInputTemplate() {

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
    public ProblemEmbeddedInputTemplate deepCopy() {

        final ProblemEmbeddedInputTemplate copy = new ProblemEmbeddedInputTemplate();

        innerDeepCopy(copy);

        if (this.correctness != null) {
            copy.correctness = this.correctness.deepCopy();
        }

        if (this.correctAnswer != null) {
            copy.correctAnswer = this.correctAnswer.deepCopy();
        }

        return copy;
    }

    /**
     * Gets the type of problem.
     *
     * @return the problem type
     */
    @Override
    public EProblemType getType() {

        return EProblemType.EMBEDDED_INPUT;
    }

    /**
     * Records a student's answer.
     *
     * @param response a list of answer objects - the answers will be passed directly into the {@code PresentedProblem}
     *               object
     */
    @Override
    public void recordAnswer(final Object[] response) {

        if (!this.recording) {
            this.recording = true;
            this.question.setInputValues(response);
            super.recordAnswer(response);

            // Log.info("Recording answer: ");
            // for (final Object o : answer) {
            // Log.info(" ", o);
            // }

            this.recording = false;
        }
    }

    /**
     * Clears a student's answer.
     */
    @Override
    public void clearAnswer() {

        this.question.clearInputs();
        super.clearAnswer();
    }

    /**
     * Determines whether the student has recorded an answer to the problem.
     *
     * @return {@code true} if the student has recorded an answer; {@code false} otherwise
     */
    @Override
    public boolean isAnswered() {

        final Object[] answers = this.question.getInputValues();
        boolean ok = false;

        if (answers != null) {
//            Log.info("There are " + answers.length + " input values submitted");
            for (final Object answer : answers) {

//                Log.info("    Input value: ", answer);

                if (answer instanceof final String str) {

                    if ("null".equals(str) || CoreConstants.EMPTY.equals(str)) {
                        continue;
                    }

                    if (!str.endsWith("}=")) {
                        ok = true;
                        break;
                    }
                }
            }
        }

//        Log.info("Answered = " + ok);

        return ok;
    }

    /**
     * Tests to see whether a particular student response is correct.
     *
     * @param response the student response
     * @return {@code true} if correct, {@code false}s if incorrect
     */
    @Override
    public boolean isCorrect(final Object[] response) {

//         Log.info("Problem ", this.ref, " correctness evaluation");
//         if (response == null) {
//            Log.fine(" Responses: null");
//         } else {
//             for (int i = 0; i < response.length; ++i) {
//                Log.fine(" Response " + i + ": " + response[i]);
//             }
//         }
//         if (getAnswer() != null) {
//             for (int i = 0; i < getAnswer().length; ++i) {
//                 Log.fine(" Answer " + i + ": " + getAnswer()[i]);
//             }
//         }

        boolean correct = false;

        if (this.correctness != null) {
//             Log.info(" Correctness:" + this.correctness.toString());
//             for (final String varName : this.correctness.parameterNames()) {
//                 final AbstractVariable var = this.evalContext.getVariable(varName);
//                 Log.fine(" Var [", var, "] = ", (var == null ? "null" : var.getValue()));
//             }

            final Object obj = this.correctness.evaluate(this.evalContext);

            if (obj instanceof Boolean) {
                correct = ((Boolean) obj).booleanValue();
            }
        }

//         Log.info(" Correct: " + correct);

        return correct;
    }

    /**
     * Emits the start of the opening &lt;problem-... tag (attributes can be emitted after this).
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     * @param indent  the indentation string
     */
    @Override
    public void openTopLevelTag(final HtmlBuilder builder, final String indent) {

        builder.add(indent, "<problem-embedded-input");
    }

    /**
     * Emits the closing &lt;/problem-...&gt; tag.
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     * @param indent  the indentation string
     */
    @Override
    public void closeTopLevelTag(final HtmlBuilder builder, final String indent) {

        builder.addln(indent, "</problem-embedded-input>");
    }

    /**
     * Prints subclass-specific elements.
     *
     * @param builder    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void printSubclassXmlBegin(final HtmlBuilder builder, final int indent) {

        final String ind = makeIndent(indent);

        if (this.correctness != null) {
            builder.addln();
            builder.add(ind, "<correct>");
            this.correctness.appendChildrenXml(builder);
            builder.addln("</correct>");
        }
    }

    /**
     * Prints subclass-specific elements.
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     * @param indent  the number of spaces to indent the printout
     */
    @Override
    public void printSubclassXmlPreQuestion(final HtmlBuilder builder, final int indent) {

        // No action
    }

    /**
     * Prints subclass-specific elements.
     *
     * @param builder    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void printSubclassXmlEnd(final HtmlBuilder builder, final int indent) {

        final String ind = makeIndent(indent);

        if (this.correctAnswer != null) {
            builder.addln();
            builder.addln(ind, "<answer>");
            for (final AbstractDocObjectTemplate child : this.correctAnswer.getChildren()) {
                child.toXml(builder, indent + 1);
            }
            builder.addln(ind, "</answer>");
        }
    }

    /**
     * Prints subclass-specific elements.
     *
     * @param pstream      the print stream to which to write the data
     * @param includeTrees {@code true} to include a dump of the entire document tree structure
     */
    @Override
    public void printSubclassDiagnostics(final PrintStream pstream, final boolean includeTrees) {

        if (this.correctness != null) {
            pstream.println("<tr><td valign='top'><b>Correctness formula:</b></td><td>");
            pstream.println(this.correctness);
            pstream.println("</td></tr>");
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
        buf.addln("<h3>Embedded Input Problem</h3>");

        if (this.id != null) {
            buf.addln("<p><b>Reference base:</b> ", this.id, "</p>");
        }

        if (this.evalContext != null && this.evalContext.numVariables() > 0) {
            buf.add("<p><b>Parameters:</b></p>");

            for (final AbstractVariable abstractVariable : this.evalContext.getVariables()) {
                buf.addln("<table border='1'>");

                final Object value = abstractVariable.getValue();

                buf.add("<tr><td><b>Name:</b></td><td>", abstractVariable.name);

                switch (abstractVariable) {
                    case final VariableInteger variableInteger -> buf.add(" (Integer)");
                    case final VariableReal variableReal -> buf.add(" (Real)");
                    case final VariableBoolean variableBoolean -> buf.add(" (Boolean)");
                    case final VariableSpan variableSpan -> buf.add(" (Span)");
                    case final VariableRandomInteger variableRandomInteger -> buf.add(" (Random Integer)");
                    case final VariableRandomReal variableRandomReal -> buf.add(" (Random Real)");
                    case final VariableRandomPermutation variableRandomPermutation -> buf.add(" (Random Permutation)");
                    case final VariableRandomBoolean variableRandomBoolean -> buf.add(" (Random Boolean)");
                    case final VariableRandomChoice variableRandomChoice -> buf.add(" (Random Choice)");
                    case final VariableRandomSimpleAngle variableRandomSimpleAngle -> buf.add(" (Random Simple Angle)");
                    case final VariableDerived variableDerived -> buf.add(" (Derived)");
                    case final VariableInputInteger variableInputInteger -> buf.add(" (Input Int)");
                    case final VariableInputReal variableInputReal -> buf.add(" (Input Real)");
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

                if (value instanceof Long) {
                    buf.addln("<tr><td><b>Integer Value:</b></td><td>", value, "</td></tr>");
                } else if (value instanceof Number) {
                    buf.addln("<tr><td><b>Real Value:</b></td><td>", value, "</td></tr>");
                } else if (value instanceof Boolean) {
                    buf.addln("<tr><td><b>Boolean Value:</b></td><td>", value, "</td></tr>");
                } else if (value instanceof String) {
                    buf.addln("<tr><td><b>String Value:</b></td><td>", value, "</td></tr>");
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

        if (this.correctness != null) {
            buf.addln("<tr><td><b>Correctness formula:</b></td><td>", this.correctness, "</td></tr>");
        }

        buf.addln("</body></html>");

        return buf.toString();
    }

    /**
     * Realizes the problem and creates a static {@code ProblemEmbeddedInputIteration} based on that realization.
     *
     * @return the generated {@code ProblemEmbeddedInputIteration}; {@code null} if realization or creation of the
     *         iteration failed
     */
    @Override
    public ProblemEmbeddedInputInst createIteration() {

        ProblemEmbeddedInputInst result = null;

        if (realize(this.evalContext)) {
            // 9 characters ~ 15.9 bits of ID, 7.4E+15 possibilities
            final String iterationId = CoreConstants.newId(9);

            final DocColumnInst questionIteration = this.question.createInstance(this.evalContext);

            final DocColumnInst solutionIteration;
            if (this.solution == null) {
                solutionIteration = null;
            } else {
                solutionIteration = this.solution.createInstance(this.evalContext);
            }

            final DocColumnInst answerIteration;
            if (this.correctAnswer == null) {
                answerIteration = null;
            } else {
                answerIteration = this.correctAnswer.createInstance(this.evalContext);
            }

            final Formula correctnessIteration = this.correctness.createIteration(this.evalContext);

            result = new ProblemEmbeddedInputInst(this.id, iterationId, this.calculator,
                    questionIteration, solutionIteration, answerIteration, correctnessIteration);
        }

        return result;
    }

    /**
     * Generates the LaTeX representation of the problem.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file (the value should be updated if the method writes any files)
     * @param overwriteAll a 1-boolean array whose only entry contains True if the user has selected "overwrite all";
     *                     false to ask the user each time (this method can update this value to true if it is false and
     *                     the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL])
     * @param builder          the {@code HtmlBuilder} to which to write the LaTeX
     * @param showAnswers  {@code true} to show the correct answers; {@code false} to leave blank
     * @param mode         the current LaTeX mode (T=text, $=in-line math, M=math)
     */
    @Override
    public void toLaTeX(final File dir, final int[] fileIndex, final boolean[] overwriteAll,
                        final HtmlBuilder builder, final boolean showAnswers, final char[] mode,
                        final EvalContext context) {

        // Emit the problem reference, for debugging
        builder.addln("% ", this.id);

        // Write the question, followed by a blank line. The question will
        // include the inputs, so we don't need to print anything else.
        this.question.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);

        if (mode[0] == '$') {
            builder.add("$");
        } else if (mode[0] == 'M') {
            builder.add("\\]");
        }

        mode[0] = 'T';

        if (showAnswers && this.correctAnswer != null) {
            this.correctAnswer.toLaTeX(dir, fileIndex, overwriteAll, builder, true, mode, context);
        }
    }

    /**
     * Extracts answers from a parameter map (a map from string parameter name to string array of values).
     *
     * @param paramMap the parameter map
     */
    @Override
    public void extractAnswers(final Map<String, String[]> paramMap) {

        final List<AbstractDocInput> inputs = this.question.getInputs();

        // In HTML, input names are prefixed with "INP_" to avoid conflicts.

        if (inputs != null) {
            final int numInputs = inputs.size();
//            Log.info("Extracting answers - there are " + numInputs + " inputs in question");

            // Checkboxes have multiple inputs with the same name - we want to store one value per name
            final Map<String, AbstractDocInput> actualInputs = new HashMap<>(numInputs);
            for (final AbstractDocInput input : inputs) {
                final String name = input.getName();
                if (!actualInputs.containsKey(name)) {
                    actualInputs.put(name, input);
                }
            }

            final List<AbstractDocInput> namedInputs = new ArrayList<>(actualInputs.values());
            final int numNamedInputs = namedInputs.size();

            final String[] answers = new String[numNamedInputs];
            int numFound = 0;
            final int ansLen = answers.length;
            for (int i = 0; i < ansLen; ++i) {
                final AbstractDocInput input = namedInputs.get(i);
                final String inputName = input.getName();

                final String paramName = "INP_" + inputName;
                final String[] param = paramMap.get(paramName);

//                Log.info("    Answer '", paramName, "' is ", Arrays.toString(param));

                if (param == null) {
                    answers[i] = "{" + inputName + "}=null";
                } else {
                    final int paramLen = param.length;

                    if (input instanceof DocInputDoubleField || input instanceof DocInputLongField) {

                        // Eliminate extraneous spaces or "fluff" in numeric entry fields
                        for (int j = 0; j < paramLen; ++j) {
                            param[j] = param[j].replace(CoreConstants.SPC, CoreConstants.EMPTY);

                            // Sometimes the student will enter "(-1)" rather than just "-1"...
                            if (param[j].startsWith("(-") && param[j].charAt(param[j].length() - 1) == ')') {
                                final int len = param[j].length();
                                param[i] = param[j].substring(1, len - 1);
                            }
                        }
                    }

                    if (param.length == 1) {
                        answers[i] = "{" + inputName + "}=" + param[0];
                    } else {
                        // Parameter length is greater than 1; this can occur with checkboxes, in
                        // which case all values should be integers that get added together
                        int sum = 0;
                        for (final String s : param) {
                            try {
                                sum += Integer.parseInt(s);
                            } catch (final NumberFormatException ex) {
                                Log.warning("Invalid array of values for ", inputName, " input", ex);
                                sum = 0;
                                break;
                            }
                        }
                        answers[i] = "{" + inputName + "}=" + sum;
                    }

//                    Log.info("Answers[" + i + "] =" + answers[i]);
                    ++numFound;
                }
            }

            if (numFound > 0) {
                recordAnswer(answers);
            } else {
                clearAnswer();
            }
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

        // TODO: Make a warning icon visible if an entered value fails to parse.

        final List<AbstractDocInput> inputs = this.question.getInputs();
        final Object[] answers = getAnswer();

        String result = origHtml;
        if (inputs == null || inputs.isEmpty()) {
            Log.warning("Question has no inputs");
        } else if (answers != null) {

            for (final Object o : answers) {
                if (o instanceof final String answerStr) {

                    for (final AbstractDocInput input : inputs) {

                        final String name = input.getName();
                        final String tag = "{" + name + "}=";

                        if (answerStr.startsWith(tag) && answerStr.length() > tag.length()) {
                            final String answer = answerStr.substring(tag.length());
                            if ("null".equals(answer)) {
                                continue;
                            }

                            if (input instanceof DocInputDoubleField || input instanceof DocInputLongField ||
                                    input instanceof DocInputStringField) {

                                // Long and Double inputs are of this form:
                                // <input type='text' ... id='INP_{NAME} name='INP_{NAME}'/>

                                final int index = result.indexOf("id='INP_" + name + "' name='INP_" + name + "'");

                                if (index == -1) {
//                                    Log.info("Could not find input '", name, "'");
                                    Log.warning("Unable to locate field input in embedded input problem.");
                                } else {
                                    final String escaped = XmlEscaper.escape(answer);
                                    result = result.substring(0, index) + "value='" + escaped + "' "
                                            + result.substring(index);
                                }
                                break;

                            } else if (input instanceof final DocInputRadioButton radio) {

                                final int value = radio.value;

                                if (Integer.toString(value).equals(answer)) {

                                    // Radio button inputs are of this form:
                                    // <input type='radio' ... id='INP_{NAME}_{VALUE}'
                                    // name='INP_{NAME}' value='{VALUE}'/>");

                                    int index = result.indexOf(
                                            "id='INP_" + name + "_" + value + "' name='INP_" + name + "'");

                                    if (index == -1) {
//                                        Log.info("Could not find input '", name, "'");
                                        Log.warning("Unable to locate radio button in embedded input problem.");
                                    } else {
                                        result = result.substring(0, index) + "checked " + result.substring(index);
                                    }

                                    // Now enable all inputs associated with the radio button
                                    final String marker = " disabled data-choice='INP_" + name + "_" + value + "'";

                                    index = result.indexOf(marker);
                                    while (index != -1) {
                                        result = result.substring(0, index) + result.substring(index + 9);
                                        index = result.indexOf(marker);
                                    }
                                    break;
                                }
                            } else if (input instanceof final DocInputCheckbox check) {

                                // Checkbox inputs are of this form:
                                // <input type='checkbox' ... id='INP_{NAME}_{VALUE}'
                                // name='INP_{NAME}' value='{VALUE}'/>");

                                final long value = check.value;
                                try {
                                    final long parsedAnswer = Long.parseLong(answer);

                                    if ((parsedAnswer & value) == value) {
                                        // Answer should be marked as "checked"

                                        final int index = result.indexOf(
                                                "id='INP_" + name + "_" + value + "' name='INP_" + name + "'");

                                        if (index == -1) {
//                                            Log.info("Could not find input '", name, "'");
                                            Log.warning("Unable to locate checkbox in embedded input problem.");
                                        } else {
                                            result = result.substring(0, index) + "checked " + result.substring(index);
                                        }
                                    }
                                } catch (final NumberFormatException ex) {
                                    // TODO:
                                }
                            }
                        }
                    } // End for(j) looping through inputs

                } // End if answer instanceof String
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

        return innerHashCode() + Objects.hashCode(this.correctness)
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
        } else if (obj instanceof final ProblemEmbeddedInputTemplate problem) {
            equal = innerEquals(problem)
                    && Objects.equals(this.correctness, problem.correctness)
                    && Objects.equals(this.correctAnswer, problem.correctAnswer);
        } else {
            equal = false;
        }

        return equal;
    }
}
