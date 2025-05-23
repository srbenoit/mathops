package dev.mathops.assessment.problem.template;

import dev.mathops.assessment.document.inst.DocColumnInst;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.document.template.DocParagraph;
import dev.mathops.assessment.document.template.DocText;
import dev.mathops.assessment.document.template.DocWhitespace;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.problem.EProblemType;
import dev.mathops.assessment.problem.inst.ProblemAutoCorrectInst;
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
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;

/**
 * This subclass of {@code Problem} that will automatically be counted correct. This can be substituted for exam
 * problems for which the student has already demonstrated mastery some required number of times.
 */
public final class ProblemAutoCorrectTemplate extends AbstractProblemTemplate {

    /**
     * The number of times the user has already answered this item correctly (the reason they are getting this
     * autocorrect question instead).
     */
    private final int numAlreadyCorrect;

    /**
     * Constructs a new {@code ProblemAutoCorrect}.
     *
     * @param theNumAlreadyCorrect the number of times the user has already answered this item correctly (the reason
     *                          they are getting this autocorrect question instead).
     */
    public ProblemAutoCorrectTemplate(final int theNumAlreadyCorrect) {

        super();

        this.numAlreadyCorrect = theNumAlreadyCorrect;

        this.id = "autocorrect";

        this.question = new DocColumn();
        this.question.tag = "question";

        final DocParagraph para = new DocParagraph();
        para.setColorName("navy");

        para.add(new DocText("You"));
        para.add(new DocWhitespace());
        para.add(new DocText("have"));
        para.add(new DocWhitespace());
        para.add(new DocText("already"));
        para.add(new DocWhitespace());
        para.add(new DocText("answered"));
        para.add(new DocWhitespace());
        para.add(new DocText("this"));
        para.add(new DocWhitespace());
        para.add(new DocText("question"));
        para.add(new DocWhitespace());
        para.add(new DocText("correctly"));
        para.add(new DocWhitespace());
        para.add(new DocText("on"));
        para.add(new DocWhitespace());
        if (theNumAlreadyCorrect == 1) {
            para.add(new DocText("an"));
        } else if (theNumAlreadyCorrect == 2) {
            para.add(new DocText("two"));
        } else {
            para.add(new DocText(Integer.toString(theNumAlreadyCorrect)));
        }
        para.add(new DocWhitespace());
        para.add(new DocText("earlier"));
        para.add(new DocWhitespace());
        if (theNumAlreadyCorrect == 1) {
            para.add(new DocText("attempt"));
        } else {
            para.add(new DocText("attempts"));
        }
        para.add(new DocWhitespace());
        para.add(new DocText("on"));
        para.add(new DocWhitespace());
        para.add(new DocText("this"));
        para.add(new DocWhitespace());
        if (theNumAlreadyCorrect == 1) {
            para.add(new DocText("assignment,"));
        } else {
            para.add(new DocText("exam,"));
        }
        para.add(new DocWhitespace());
        para.add(new DocText("so"));
        para.add(new DocWhitespace());
        para.add(new DocText("you"));
        para.add(new DocWhitespace());
        para.add(new DocText("will"));
        para.add(new DocWhitespace());
        para.add(new DocText("not"));
        para.add(new DocWhitespace());
        para.add(new DocText("have"));
        para.add(new DocWhitespace());
        para.add(new DocText("to"));
        para.add(new DocWhitespace());
        para.add(new DocText("answer"));
        para.add(new DocWhitespace());
        para.add(new DocText("it"));
        para.add(new DocWhitespace());
        para.add(new DocText("again."));
        para.add(new DocWhitespace());
        para.add(new DocText("This"));
        para.add(new DocWhitespace());
        para.add(new DocText("question"));
        para.add(new DocWhitespace());
        para.add(new DocText("will"));
        para.add(new DocWhitespace());
        para.add(new DocText("automatically"));
        para.add(new DocWhitespace());
        para.add(new DocText("be"));
        para.add(new DocWhitespace());
        para.add(new DocText("counted"));
        para.add(new DocWhitespace());
        para.add(new DocText("as"));
        para.add(new DocWhitespace());
        para.add(new DocText("correct"));
        para.add(new DocWhitespace());
        para.add(new DocText("on"));
        para.add(new DocWhitespace());
        para.add(new DocText("this"));
        para.add(new DocWhitespace());
        para.add(new DocText("attempt."));
        this.question.add(para);

        recordAnswer(new Object[]{"Y"});
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
    public ProblemAutoCorrectTemplate deepCopy() {

        final ProblemAutoCorrectTemplate copy = new ProblemAutoCorrectTemplate(this.numAlreadyCorrect);

        innerDeepCopy(copy);

        return copy;
    }

    /**
     * Gets type of problem.
     *
     * @return the problem type
     */
    @Override
    public EProblemType getType() {

        return EProblemType.AUTO_CORRECT;
    }

    /**
     * Determines whether the student has recorded an answer to the problem.
     *
     * @return {@code true}
     */
    @Override
    public boolean isAnswered() {

        return true;
    }

    /**
     * Tests to see whether a particular student response is correct.
     *
     * @param response the student response (ignored)
     * @return {@code true}
     */
    @Override
    public boolean isCorrect(final Object[] response) {

        return true;
    }

    /**
     * Generates a realized multiple choice answer problem.
     *
     * @param context the evaluation context
     * @return {@code true} if realization succeeded; {@code false} otherwise
     */
    @Override
    public boolean realize(final EvalContext context) {

        return super.realize(context);
    }

    /**
     * Realizes the problem and creates a static {@code ProblemAutoCorrectIteration} based on that realization.
     *
     * @return the generated {@code ProblemAutoCorrectIteration}; {@code null} if realization or creation of the
     *         iteration failed
     */
    @Override
    public ProblemAutoCorrectInst createIteration() {

        ProblemAutoCorrectInst result = null;

        if (realize(this.evalContext)) {
            // 9 characters ~ 15.9 bits of ID, 7.4E+15 possibilities
            final String iterationId = CoreConstants.newId(9);

            final DocColumnInst questionIteration = this.question.createInstance(this.evalContext);

            result = new ProblemAutoCorrectInst(iterationId, this.calculator, questionIteration);
        }

        return result;
    }

    /**
     * Sets the choice that the student entered.
     *
     * @param response the student's response, as an object array
     */
    @Override
    public void recordAnswer(final Object[] response) {

        super.recordAnswer(new Object[]{"Y"});

        this.completionTime = System.currentTimeMillis();
    }

    /**
     * Clears a student's answer.
     */
    @Override
    public void clearAnswer() {

        // Do nothing - this answer should always be present and correct
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
        buf.addln("<h3>Auto-Correct Problem</h3>");

        if (this.id != null) {
            buf.addln("<p><b>Reference base:</b> ", this.id, "</p>");
        }

        if (this.evalContext != null && this.evalContext.numVariables() > 0) {
            buf.addln("<p><b>Parameters:</b></p>");

            for (final AbstractVariable abstractVariable : this.evalContext.getVariables()) {
                buf.addln("<table border='1'>");

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

                final Object value = abstractVariable.getValue();

                if (value != null) {

                    switch (value) {
                        case final Long l -> buf.add("<tr><td><b>Integer Value:</b></td><td>", value, "</td></tr>");
                        case final Number number -> buf.add("<tr><td><b>Real Value:</b></td><td>", value, "</td></tr>");
                        case final Boolean b -> buf.add("<tr><td><b>Boolean Value:</b></td><td>", value, "</td></tr>");
                        case final String s -> buf.add("<tr><td><b>String Value:</b></td><td>", value, "</td></tr>");
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
    }

    /**
     * Extracts answers from a parameter map (a map from string parameter name to string array of values).
     *
     * @param paramMap the parameter map
     */
    @Override
    public void extractAnswers(final Map<String, String[]> paramMap) {

        recordAnswer(new Object[]{"Y"});
    }

    /**
     * Emits the start of the opening &lt;problem-... tag (attributes can be emitted after this).
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     * @param indent  the indentation string
     */
    @Override
    public void openTopLevelTag(final HtmlBuilder builder, final String indent) {

        builder.add(indent, "<problem-auto-correct");
    }

    /**
     * Emits the closing &lt;/problem-...&gt; tag.
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     * @param indent  the indentation string
     */
    @Override
    public void closeTopLevelTag(final HtmlBuilder builder, final String indent) {

        builder.addln(indent, "</problem-auto-correct>");
    }

    /**
     * A method that subclasses override to print their subclass-specific elements.
     *
     * @param builder    The {@code HtmlBuilder} to which to write the XML.
     * @param indent The number of spaces to indent the printout.
     */
    @Override
    public void printSubclassXmlBegin(final HtmlBuilder builder, final int indent) {

        // No action
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

        // No action
    }

    /**
     * Alters the emitted question or solution HTML to include the student's current answer (if any).
     *
     * @param origHtml the HTML without student answers inserted
     * @return the HTML with student answers inserted
     */
    @Override
    public String insertAnswers(final String origHtml) {

        return origHtml;
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return innerHashCode();
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
        } else if (obj instanceof final ProblemAutoCorrectTemplate problem) {
            equal = innerEquals(problem);
        } else {
            equal = false;
        }

        return equal;
    }
}
