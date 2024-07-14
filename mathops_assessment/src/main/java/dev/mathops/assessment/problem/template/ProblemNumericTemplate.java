package dev.mathops.assessment.problem.template;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.document.inst.DocColumnInst;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.problem.EProblemType;
import dev.mathops.assessment.problem.inst.ProblemAcceptNumberInst;
import dev.mathops.assessment.problem.inst.ProblemNumericInst;
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
import dev.mathops.commons.parser.xml.XmlEscaper;

import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * This subclass of {@code TemplateProblem} supports questions that require a numeric answer.
 */
public final class ProblemNumericTemplate extends AbstractProblemTemplate {

    /** The specification of how the problem should accept a numeric answer. */
    public ProblemAcceptNumberTemplate acceptNumber;

    /** The student's string answer, if an integer was submitted. */
    String stringAnswer;

    /**
     * Constructs an empty {@code ProblemNumeric} object.
     */
    public ProblemNumericTemplate() {

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
    public ProblemNumericTemplate deepCopy() {

        final ProblemNumericTemplate copy = new ProblemNumericTemplate();

        innerDeepCopy(copy);

        if (this.acceptNumber != null) {
            copy.acceptNumber = this.acceptNumber.deepCopy();
        }

        return copy;
    }

    /**
     * Returns the type of problem.
     *
     * @return the problem type
     */
    @Override
    public EProblemType getType() {

        return EProblemType.NUMERIC;
    }

    /**
     * Records a student's answer. The first object may be either an Integer or Double (deprecated), representing the
     * student's numeric answer, or a string, representing the unparsed answer.
     *
     * @param response a list of answer objects - the answers will be passed directly into the PresentedProblem object
     */
    @Override
    public void recordAnswer(final Object[] response) {

        if (response == null) {
            throw new IllegalArgumentException("Answer object array may not be null");
        } else if (response.length == 0) {
            throw new IllegalArgumentException("Answer object array may not be empty");
        } else if (response.length > 1) {
            final StringBuilder msg = new StringBuilder("Numeric problems may not have more than one answer object.");

            if (this.id != null) {
                msg.append("  Problem ").append(this.id).append(" had "
                ).append(response.length).append(": ");

                for (final Object o : response) {
                    msg.append(o.toString()).append(", ");
                }
            }

            throw new IllegalArgumentException(msg.toString());
        }

        // Record the answer
        if (response[0] instanceof String) {
            this.stringAnswer = sanitize(((String) response[0]));
        } else if (response[0] instanceof Number) {
            this.stringAnswer = response[0].toString();
        } else {
            final StringBuilder msg = new StringBuilder("First answer object for numeric problems must be String.");
            if (this.id != null) {
                msg.append("  Problem ").append(this.id).append(" had ").append(response[0].getClass().getName());
            }

            throw new IllegalArgumentException(msg.toString());
        }

        // The following causes ProblemListeners to be notified.
        super.recordAnswer(response);
    }

    /**
     * Attempts to sanitize an input string.
     *
     * @param raw the raw string to sanitize
     * @return the sanitized string
     */
    private static String sanitize(final String raw) {

        String sanitized = raw.replace(CoreConstants.SPC, CoreConstants.EMPTY)
                .replace(CoreConstants.COMMA, CoreConstants.EMPTY);

        // Sometimes the student will enter "(-1)" rather than just "-1"...
        if (!sanitized.isEmpty() && sanitized.charAt(0) == '(' && sanitized.charAt(sanitized.length() - 1) == ')') {
            final int len = sanitized.length();
            sanitized = sanitized.substring(1, len - 1);
        }

        return sanitized;
    }

    /**
     * Clears a student's answer.
     */
    @Override
    public void clearAnswer() {

        this.stringAnswer = null;

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

        return this.stringAnswer != null;
    }

    /**
     * Tests to see whether a particular student response is correct.
     *
     * @param response the student response
     * @return {@code true} if correct, {@code false} if incorrect
     */
    @Override
    public boolean isCorrect(final Object[] response) {

        final Number answer;

        // Get the numeric answer (integer or real)
        if (response == null || response.length != 1) {
            return false;
        } else if (response[0] instanceof String) {
            final String sanitized = sanitize((String) response[0]);

            if (this.acceptNumber != null && this.acceptNumber.forceInteger) {
                answer = parseLong(sanitized);
            } else {
                answer = parseDouble(sanitized);
            }
            if (answer == null) {
                return false;
            }
        } else if (response[0] instanceof Long) {
            answer = (Long) response[0];
        } else if (response[0] instanceof Number) {
            answer = (Number) response[0];
        } else {
            return false;
        }

        Object correctAns = null;

        // Get the acceptable number parameters
        if (this.acceptNumber != null && this.acceptNumber.correctAnswer != null) {
            correctAns = this.acceptNumber.correctAnswer.evaluate(this.evalContext);
        }

        Object correctVar = null;
        if (this.acceptNumber != null) {
            if (this.acceptNumber.varianceConstant != null) {
                correctVar = this.acceptNumber.varianceConstant;
            } else if (this.acceptNumber.varianceFormula != null) {
                correctVar = this.acceptNumber.varianceFormula.evaluate(this.evalContext);
            }
        }

        // Compute the acceptable range.
        boolean useInts = false;
        long minInt = 0L;
        long maxInt = 0L;
        double minReal = 0.0;
        double maxReal = 0.0;

        if (correctAns instanceof final Long longAns) {

            if (correctVar instanceof final Long longVar) {
                minInt = longAns.longValue() - longVar.longValue();
                maxInt = longAns.longValue() + longVar.longValue();
                useInts = true;
            } else if (correctVar instanceof final Number numVar) {
                minReal = longAns.doubleValue() - numVar.doubleValue();
                maxReal = longAns.doubleValue() + numVar.doubleValue();
            } else {
                minInt = longAns.longValue();
                maxInt = minInt;
                useInts = true;
            }
        } else if (correctAns instanceof final Number numAns) {

            if (correctVar instanceof final Long longVar) {
                minReal = numAns.doubleValue() - longVar.doubleValue();
                maxReal = numAns.doubleValue() + longVar.doubleValue();
            } else if (correctVar instanceof final Number numVar) {
                minReal = numAns.doubleValue() - numVar.doubleValue();
                maxReal = numAns.doubleValue() + numVar.doubleValue();
            } else {
                minReal = numAns.doubleValue();
                maxReal = minReal;
            }
        }

        // Test the answer against the acceptable number parameters
        boolean correct;

        if (useInts) {
            correct = answer.doubleValue() >= (double) minInt && answer.doubleValue() <= (double) maxInt;
        } else {
            correct = answer.doubleValue() >= minReal && answer.doubleValue() <= maxReal;
        }

        return correct;
    }

    /**
     * Generate a realized numeric answer problem.
     *
     * @param context the evaluation context
     * @return true if realization succeeded; false otherwise
     */
    @Override
    public boolean realize(final EvalContext context) {

        boolean ok = super.realize(context);

        if (ok && this.acceptNumber != null) {
            ok = this.acceptNumber.realize(context);
        }

        return ok;
    }

    /**
     * Realizes the problem and creates a static {@code ProblemNumericIteration} based on that realization.
     *
     * @return the generated {@code ProblemNumericIteration}; {@code null} if realization or creation of the iteration
     *         failed
     */
    @Override
    public ProblemNumericInst createIteration() {

        ProblemNumericInst result = null;

        if (realize(this.evalContext)) {
            if (this.acceptNumber == null) {
                Log.warning("Unable to create instance when acceptNumber is not present");
            } else if (this.acceptNumber.correctAnswer == null) {
                Log.warning("Unable to create instance when acceptNumber has no correct answer");
            } else {
                final Object correctAnswerObj = this.acceptNumber.correctAnswer.evaluate(this.evalContext);

                final Object varianceObj;
                if (this.acceptNumber.varianceConstant == null) {
                    if (this.acceptNumber.varianceFormula == null) {
                        varianceObj = Double.valueOf(0.0);
                    } else {
                        varianceObj = this.acceptNumber.varianceFormula.evaluate(this.evalContext);
                    }
                } else {
                    varianceObj = this.acceptNumber.varianceConstant;
                }

                if (correctAnswerObj instanceof final Number correctAnswerNbr
                        && varianceObj instanceof final Number varianceNbr) {

                    // 9 characters ~ 15.9 bits of ID, 7.4E+15 possibilities
                    final String iterationId = CoreConstants.newId(9);

                    final ProblemAcceptNumberInst acceptNumberIter =
                            new ProblemAcceptNumberInst(this.acceptNumber.forceInteger,
                                    correctAnswerNbr.doubleValue(), varianceNbr.doubleValue());

                    final DocColumnInst questionIteration = this.question.createInstance(this.evalContext);

                    if (this.solution == null) {
                        result = new ProblemNumericInst(this.id, iterationId, this.calculator,
                                questionIteration, null, acceptNumberIter);
                    } else {
                        final DocColumnInst solutionIteration = this.solution.createInstance(this.evalContext);

                        result = new ProblemNumericInst(this.id, iterationId, this.calculator,
                                        questionIteration, solutionIteration, acceptNumberIter);
                    }
                } else {
                    Log.warning("acceptNumber correct answer, variance did not generate numbers");
                }
            }
        }

        return result;
    }

    /**
     * Emits the start of the opening &lt;problem-... tag (attributes can be emitted after this).
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     * @param indent  the indentation string
     */
    @Override
    public void openTopLevelTag(final HtmlBuilder builder, final String indent) {

        builder.add(indent, "<problem-numeric");
    }

    /**
     * Emits the closing &lt;/problem-...&gt; tag.
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     * @param indent  the indentation string
     */
    @Override
    public void closeTopLevelTag(final HtmlBuilder builder, final String indent) {

        builder.addln(indent, "</problem-numeric>");
    }

    /**
     * A method that subclasses override to print their subclass-specific attributes on the problem element.
     *
     * @param builder The {@code HtmlBuilder} to which to write the XML.
     */
    @Override
    public void printSubclassAttributes(final HtmlBuilder builder) {

        super.printSubclassAttributes(builder);

        if (this.stringAnswer != null) {
            AbstractXmlObject.writeAttribute(builder, "student-string-answer", this.stringAnswer);
        }
    }

    /**
     * A method that subclasses override to print their subclass-specific elements.
     *
     * @param builder    The {@code HtmlBuilder} to which to write the XML.
     * @param indent The number of spaces to indent the printout.
     */
    @Override
    public void printSubclassXmlBegin(final HtmlBuilder builder, final int indent) {

        if (this.acceptNumber != null) {
            builder.addln();
            this.acceptNumber.appendXml(builder, indent);
        }
    }

    /**
     * A method that subclasses override to print their subclass-specific elements.
     *
     * @param builder    The {@code HtmlBuilder} to which to write the XML.
     * @param indent The number of spaces to indent the printout.
     */
    @Override
    public void printSubclassXmlEnd(final HtmlBuilder builder, final int indent) {

        // No action
    }

    /**
     * Print subclass-specific elements.
     *
     * @param ps           The print stream to which to write the data.
     * @param includeTrees True to include a dump of the entire document tree structure.
     */
    @Override
    public void printSubclassDiagnostics(final PrintStream ps, final boolean includeTrees) {

        if (this.acceptNumber != null) {
            ps.println("<tr><td valign='top'><b>Number Acceptance Criteria:</b></td><td>");
            this.acceptNumber.printDiagnostics(ps);
            ps.println("</td></tr>");
        }
    }

    /**
     * Generate a string representation of the object.
     *
     * @return The string representation of the object.
     */
    @Override
    public String toString() {

        final HtmlBuilder buf = new HtmlBuilder(1000);
        final Iterator<AbstractVariable> iter;
        AbstractVariable param;
        Object value;

        buf.addln("<html><head></head><body>");
        buf.addln("<h3>Numeric Problem</h3>");

        if (this.id != null) {
            buf.sP().add("<b>Reference base:</b> ", this.id).eP();
        }

        if (this.evalContext != null && this.evalContext.numVariables() > 0) {
            buf.sP().add("<b>Parameters:</b>").eP();
            iter = this.evalContext.getVariables().iterator();

            while (iter.hasNext()) {
                buf.addln("<table border='1'>");
                param = iter.next();
                value = param.getValue();

                buf.add("<tr><td><b>Name:</b></td><td>", param.name);

                switch (param) {
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

                if (param instanceof final IRangedVariable ranged) {
                    if (ranged.getMin() != null) {
                        buf.addln("<tr><td><b>Minimum:</b></td><td>", ranged.getMin(), "</td></tr>");
                    }

                    if (ranged.getMax() != null) {
                        buf.addln("<tr><td><b>Maximum:</b></td><td>", ranged.getMax(), "</td></tr>");
                    }
                }

                if (param instanceof final IExcludableVariable excludable) {
                    final Formula[] excl = excludable.getExcludes();

                    if (excl != null) {
                        for (final Formula formula : excl) {
                            buf.addln("<tr><td><b>Exclude:</b></td><td>", formula, "</td></tr>");
                        }
                    }
                }

                if (param instanceof final VariableDerived der && der.getFormula() != null) {
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
            buf.add(this.question.toString());
        }

        if (this.solution != null) {
            buf.addln("<p><b>Solution:</b></p>");
            buf.add(this.solution.toString());
        }

        if (this.acceptNumber != null) {
            buf.addln("<p><b>Number Acceptance Criteria</b></p>");
            buf.addln("<table border='1'>");
            buf.addln("<tr><td><b>Allow only integer entry?</b></td><td>",
                    Boolean.toString(this.acceptNumber.forceInteger), "</td></tr>");

            if (this.acceptNumber.varianceConstant != null) {
                buf.addln("<tr><td><b>Allowed variance from correct answer:</b></td><td>",
                        this.acceptNumber.varianceConstant, "</td></tr>");
            } else if (this.acceptNumber.varianceFormula != null) {
                buf.addln("<tr><td><b>Allowed variance from correct answer:</b></td><td>",
                        this.acceptNumber.varianceFormula, "</td></tr>");
            }

            if (this.acceptNumber.correctAnswer != null) {
                buf.add("<tr><td><b>Correct answer:</b></td><td>");
                buf.add(this.acceptNumber.correctAnswer.toString());
                buf.add("</td></tr>");
            }
        }

        buf.addln("</body></html>");

        return buf.toString();
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
     * @param builder          The {@code HtmlBuilder} to which to write the LaTeX.
     * @param showAnswers  True to show the correct answers; false to leave blank
     * @param mode         The current LaTeX mode (T=text, $=in-line math, M=math).
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

        // Draw a box for student entry of their answer
        builder.addln("\\begin{center}");

        if (showAnswers) {
            builder.add("\\framebox[2in]{\\strut \\rule{0pt}{6pt}");
            builder.add(this.acceptNumber.getCorrectAnswerValue(this.evalContext).toString());
        } else {
            builder.add("\\framebox[1in]{\\strut \\rule{0pt}{6pt}");
        }

        builder.addln("}");
        builder.addln("\\end{center}");
    }

    /**
     * Extracts answers from a parameter map (a map from string parameter name to string array of values).
     *
     * @param paramMap the parameter map
     */
    @Override
    public void extractAnswers(final Map<String, String[]> paramMap) {

        final String[] answer = paramMap.get("ANSWER");

        if (answer != null && answer.length == 1 && answer[0] != null && !answer[0].isEmpty()) {
            recordAnswer(answer);
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

        // The answer tag has this form:
        // <input type='text' ... name='ANSWER' id='ANSWER'>

        final String result;

        if (this.stringAnswer == null) {
            result = origHtml;
        } else {
            final int index = origHtml.indexOf("name='ANSWER' id='ANSWER'");

            if (index == -1) {
                Log.warning("Unable to locate answer input in Numeric problem.");
                result = origHtml;
            } else {
                final String escaped = XmlEscaper.escape(this.stringAnswer);
                result = origHtml.substring(0, index) + "value='" + escaped + "' " + origHtml.substring(index);
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

        return innerHashCode() + Objects.hashCode(this.stringAnswer)
                + Objects.hashCode(this.acceptNumber);
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
        } else if (obj instanceof final ProblemNumericTemplate problem) {
            equal = innerEquals(problem)
                    && Objects.equals(this.stringAnswer, problem.stringAnswer)
                    && Objects.equals(this.acceptNumber, problem.acceptNumber);
        } else {
            equal = false;
        }

        return equal;
    }
}
