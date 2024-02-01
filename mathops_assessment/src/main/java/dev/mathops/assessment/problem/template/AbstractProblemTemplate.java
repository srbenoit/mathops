package dev.mathops.assessment.problem.template;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.Realizable;
import dev.mathops.assessment.document.template.AbstractDocInput;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.problem.ECalculatorType;
import dev.mathops.assessment.problem.EProblemType;
import dev.mathops.assessment.problem.inst.AbstractProblemInst;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EqualityTests;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * A problem, or sub-problem, for an exam, homework set or practice set. A problem is basically a container for an XML
 * that holds the problem's presentation, and for any number of nested sub-parts of the problem.<br>
 * <br>
 * This class represents a problem in the "unrealized" state. That is, a problem that may include parameters for
 * algorithmic generation. Once the parameters have been chosen by a randomizer process, then the
 * {@code RealizedProblem} object can be generated.<br>
 * <br>
 * Problems are stored in an XML format, defined by {@code problem.dtd}, and can come from the local file system, a
 * remote URL, a database, or a network repository.
 */
public abstract class AbstractProblemTemplate extends AbstractXmlObject
        implements ProblemTemplateInt, Realizable {

    /** The problem type. */
    private EProblemType type;

    /**
     * The problem's unique position in the organizational tree, in which all instructional material is maintained.
     */
    public String ref;

    /** An evaluation context for algorithmically generating problems. */
    public EvalContext evalContext;

    /** The question. */
    public DocColumn question;

    /** The solution. */
    public DocColumn solution;

    /** The time/date when the problem object was most recently completed. */
    public long completionTime;

    /** The calculator allowed on the problem. */
    public ECalculatorType calculator = ECalculatorType.FULL_CALC;

    /** The student's response. */
    private Object[] studentResponse;

    /** The HTML representation of the problem. */
    public String questionHtml;

    /** The disabled HTML representation of the problem. */
    public String disabledHtml;

    /** The HTML representation of the problem with answer shown. */
    public String answerHtml;

    /** The HTML representation of the problem with solution shown. */
    public String solutionHtml;

    /** The score, once the problem bas been graded. */
    public double score;

    /**
     * Constructs an empty {@code AbstractProblem} object.
     */
    AbstractProblemTemplate() {

        super();

        this.evalContext = new EvalContext();
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
    public abstract AbstractProblemTemplate deepCopy();

    /**
     * Populates the member variables of a copy of this object.
     *
     * @param copy the copy to populate
     */
    void innerDeepCopy(final AbstractProblemTemplate copy) {

        copy.type = this.type;
        copy.ref = this.ref;
        copy.evalContext = this.evalContext.deepCopy();

        if (this.question != null) {
            copy.question = this.question.deepCopy();

            // Bind question inputs to question's eval context
            copy.question.refreshInputs(copy.evalContext, true);
            for (final AbstractDocInput input : copy.question.getInputs()) {
                input.bind(copy.evalContext);
            }
        }

        if (this.solution != null) {
            copy.solution = this.solution.deepCopy();
        }

        // copy.realizationTime = 0;
        copy.completionTime = 0L;
        copy.calculator = this.calculator;
        copy.studentResponse = null;
    }

    /**
     * Gets the problem type.
     *
     * @return the type
     */
    public EProblemType getType() {

        return this.type;
    }

    /**
     * Determines whether the student has recorded an answer to the problem.
     *
     * @return {@code true} if the student has recorded an answer; {@code false} otherwise
     */
    @Override
    public abstract boolean isAnswered();

    /**
     * Sets the choice that the student entered.
     *
     * @param response the student's response, as an object array
     */
    final void setStudentResponse(final Object[] response) {

        this.studentResponse = response.clone();
    }

    /**
     * Sets the choice that the student entered.
     *
     * @param response the student's response, as an object array
     */
    @Override
    public void recordAnswer(final Object[] response) {

        this.studentResponse = response == null ? null : response.clone();
        this.completionTime = System.currentTimeMillis();
    }

    /**
     * Clears a student's answer.
     */
    @Override
    public void clearAnswer() {

        this.studentResponse = null;
        this.completionTime = 0L;
    }

    /**
     * Gets the choice that the student entered.
     *
     * @return the student's response, as an object array
     */
    @Override
    public Object[] getAnswer() {

        Object[] response = null;

        if (this.studentResponse != null) {
            response = this.studentResponse.clone();
        }

        return response;
    }

    /**
     * Tests to see whether a particular student response is correct. Subclasses should override to test their
     * particular answer types.
     *
     * @param response the student response
     * @return {@code true} if correct, {@code false} if incorrect
     */
    @Override
    public abstract boolean isCorrect(Object[] response);

    /**
     * Realizes the problem by generating values for all parameters, and computing all formula values.
     *
     * @param context the evaluation context
     * @return {@code true} if realization succeeded; {@code false} otherwise
     */
    @Override
    public boolean realize(final EvalContext context) {

        if (context != this.evalContext) {
            Log.warning("Contexts differ!");
        }

        // Reset the problem to an "unrealized" state.
        this.completionTime = 0L;
        clearAnswer();

        // Generate a new set of random parameter values
        final boolean ok = context.generate(this.ref);

        if (!ok) {
            Log.warning("Problem " + this.ref + " failed to generate");
        }

        // Log.info("GENERATED VARS:");
        // for (String vname : this.evalContext.getVariableNames()) {
        // AbstractVariable v = this.evalContext.getVariable(vname);
        // Log.info(CoreConstants.SPC, vname, " = ", v.getValue());
        // }

        this.questionHtml = null;
        this.disabledHtml = null;
        this.answerHtml = null;
        this.solutionHtml = null;

        return ok;
    }

    /**
     * Realizes the problem and creates a static iteration based on that realization.
     *
     * @return the generated iteration; {@code null} if realization or creation of the iteration failed
     */
    public abstract AbstractProblemInst createIteration();

    /**
     * Appends the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void appendXml(final HtmlBuilder xml, final int indent) {

        final String ind0 = makeIndent(indent);
        final String ind1 = makeIndent(indent + 1);

        xml.add(ind0, "<problem");
        writeAttribute(xml, "type", getType().label);
        printSubclassAttributes(xml);
        xml.addln(">");

        printSubclassXmlBegin(xml, indent + 1);

        if (this.ref != null) {
            xml.addln();
            xml.addln(ind1, "<ref-base>", this.ref, "</ref-base>");
        }

        if (this.evalContext != null) {
            final Collection<AbstractVariable> vars = this.evalContext.getVariables();
            if (!vars.isEmpty()) {
                xml.addln();
                for (final AbstractVariable var : vars) {
                    var.appendXml(xml, indent + 1);
                }
            }
        }

        if (this.question != null) {
            xml.addln();
            this.question.toXml(xml, indent + 1);
        }

        if (this.solution != null) {
            xml.addln();
            this.solution.toXml(xml, indent + 1);
        }

        if (this.studentResponse != null) {
            final String ind2 = makeIndent(indent + 1);
            xml.addln();
            xml.addln(ind1, "<student-response>");
            for (final Object o : this.studentResponse) {
                if (o instanceof Long) {
                    xml.addln(ind2, "<long>", o, "</long>");
                } else if (o instanceof Double) {
                    xml.addln(ind2, "<double>", o, "</double>");
                } else if (o instanceof String) {
                    xml.addln(ind2, "<string>", o, "</string>");
                } else {
                    xml.addln(ind2, "<unknown>", o, "</unknown>");
                }
            }
            xml.addln(ind1, "</student-response>");
        }

        if (this.score != 0.0) {
            xml.addln();
            xml.addln(ind1, " <score>", Double.toString(this.score), "</score>");
        }

        printSubclassXmlEnd(xml, indent + 1);

        xml.addln(ind0, "</problem>");
    }

    /**
     * Prints subclass-specific attributes on the problem element.
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     */
    @Override
    public void printSubclassAttributes(final HtmlBuilder builder) {

        if (this.calculator != null && this.calculator != ECalculatorType.FULL_CALC) {
            writeAttribute(builder, "calculator", this.calculator.label);
        }

        if (this.completionTime != 0L) {
            writeAttribute(builder, "completed", Long.toString(this.completionTime));
        }
    }

    /**
     * Prints subclass-specific elements.
     *
     * @param builder    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public abstract void printSubclassXmlBegin(HtmlBuilder builder, int indent);

    /**
     * Prints subclass-specific elements.
     *
     * @param builder    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public abstract void printSubclassXmlEnd(HtmlBuilder builder, int indent);

    /**
     * Prints diagnostic data about the problem to a print stream.
     *
     * @param ps      the print stream to output to
     * @param includeTrees {@code true} to include a dump of contained document tree structures
     */
    @Override
    public void printDiagnostics(final PrintStream ps, final boolean includeTrees) {

        ps.println("<html><head>");

        ps.println("<style>");
        ps.print("  body {");
        ps.print("font-family:geneva,helvetica,arial,\"lucida sans\",sans-serif;");
        ps.print("font-size:11pt;");
        ps.print("color:#333;");
        ps.print("margin-top:0px;");
        ps.print("padding-top:0px;");
        ps.println("}");
        ps.print("  p {");
        ps.print("margin-top:0px;");
        ps.print("padding-top:0px;");
        ps.println("}");
        ps.print("  table {");
        ps.print("border-width:1;border-style:solid;");
        ps.println("}");
        ps.print("  td {");
        ps.print("font-family:geneva,helvetica,arial,\"lucida sans\",sans-serif;");
        ps.print("font-size:11pt;");
        ps.print("color:#333;");
        ps.println("}");
        ps.print("  ol {");
        ps.print("margin-top:0;margin-bottom:0;margin-left:20;");
        ps.println("}");
        ps.print("  .red {");
        ps.print("color:red;");
        ps.println("}");
        ps.println("</style>");
        ps.println("</head><body>");

        ps.println("<table>");
        ps.print("<tr><td><b>Problem Type:</b></td><td>");
        ps.print(getType().label);
        ps.println("</td></tr>");

        if (this.ref != null) {
            ps.print("<tr><td><b>Position in tree:</b></td><td>");
            ps.print(this.ref);
            ps.println("</td></tr>");
        }

        if (this.evalContext != null) {
            ps.print("<tr><td valign='top'><b>Parameters:</b></td><td>");
            ps.print("<table>");

            for (final AbstractVariable abstractVariable : this.evalContext.getVariables()) {
                ps.print("<tr><td>");
                abstractVariable.printDiagnostics(ps, includeTrees);
                ps.println("</td></tr>");
            }

            ps.println("</table>");
            ps.println("</td></tr>");
        }

        if (this.question != null) {
            ps.println("<tr><td valign='top'><b>Question:</b></td><td>");
            this.question.printDiagnostics(ps, includeTrees);
            ps.println("</td></tr>");
        }

        if (this.solution != null) {
            ps.println("<tr><td valign='top'><b>Solution:</b></td><td>");
            this.solution.printDiagnostics(ps, includeTrees);
            ps.println("</td></tr>");
        }

        printSubclassDiagnostics(ps, includeTrees);

        ps.println("</body></html>");
    }

    /**
     * Prints subclass-specific diagnostic information.
     *
     * @param ps      the print stream to which to write the data
     * @param includeTrees {@code true} to include a dump of the entire document tree structure
     */
    @Override
    public abstract void printSubclassDiagnostics(PrintStream ps, boolean includeTrees);

    /**
     * Generates the LaTeX representation of the problem.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    an index used to uniquely name files to be included by the LaTeX file
     * @param overwriteAll a 1-boolean array whose only entry contains True if the user has selected "overwrite all";
     *                     false to ask the user each time. This method can update this value to true if it is false and
     *                     the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL]
     * @param builder          the {@code HtmlBuilder} to which to write the LaTeX
     * @param showAnswers  {@code true} to show the correct answers; {@code false} to leave blank
     * @param mode         the current LaTeX mode (T=text, $=in-line math, M=math)
     * @param context      the evaluation context
     */
    @Override
    public abstract void toLaTeX(File dir, int[] fileIndex, boolean[] overwriteAll, HtmlBuilder builder,
                                 boolean showAnswers, char[] mode, EvalContext context);

    /**
     * Extracts answers from a parameter map (a map from string parameter name to string array of values).
     *
     * @param paramMap the parameter map
     */
    public abstract void extractAnswers(Map<String, String[]> paramMap);

    /**
     * Alters the emitted question or solution HTML to include the student's current answer (if any).
     *
     * @param origHtml the HTML without student answers inserted
     * @return the HTML with student answers inserted
     */
    public abstract String insertAnswers(String origHtml);

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    final int innerHashCode() {

        return Objects.hashCode(this.ref)
                + Objects.hashCode(this.evalContext)
                + Objects.hashCode(this.question)
                + Objects.hashCode(this.solution);
    }

    /**
     * Tests non-transient member variables in this base class for equality with another instance.
     *
     * @param other the other instance
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    final boolean innerEquals(final AbstractProblemTemplate other) {

        return Objects.equals(this.ref, other.ref)
                && Objects.equals(this.evalContext, other.evalContext)
                && Objects.equals(this.question, other.question)
                && Objects.equals(this.solution, other.solution);
    }

    /**
     * Attempts to parse a string as a {@code Long}.
     *
     * <p>
     * This method ignores embedded commas, leading or trailing spaces, a leading "$", or a trailing "%". The result
     * could also be in the form of a double, like "123.000" or "1.23E3" that works out to an integer. It could even be
     * something of the form "10/2".
     *
     * @param str the string to parse
     * @return the parsed {@code Long} value; {@code null} if the string cannot be parsed
     */
    static Long parseLong(final String str) {

        String trimmed = str.trim();

        if (!trimmed.isEmpty() && trimmed.charAt(0) == '$') {
            trimmed = trimmed.substring(1);
        }
        if (!trimmed.isEmpty() && trimmed.charAt(trimmed.length() - 1) == '%') {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        trimmed = trimmed.replace(CoreConstants.COMMA, CoreConstants.EMPTY);

        Long result = null;

        final int slash = trimmed.indexOf('/');
        if (slash == -1) {
            // No slash
            try {
                result = Long.valueOf(trimmed);
            } catch (final NumberFormatException ex) {
                try {
                    final Double d = Double.valueOf(trimmed);
                    if (d.doubleValue() == (double) d.longValue()) {
                        result = Long.valueOf(d.longValue());
                    }
                } catch (final NumberFormatException ex2) {
                    // No action
                }
            }
        } else {
            // Found a slash
            try {
                final long numer = Long.parseLong(trimmed.substring(0, slash));
                final long denom = Long.parseLong(trimmed.substring(slash + 1));

                if (denom != 0L && numer % denom == 0L) {
                    result = Long.valueOf(numer / denom);
                }
            } catch (final NumberFormatException ex2) {
                // No action
            }
        }

        return result;
    }

    /**
     * Attempts to parse a string as a {@code Double}.
     *
     * @param str the string to parse
     * @return the parsed {@code Double} value; {@code null} if the string cannot be parsed
     */
    static Double parseDouble(final String str) {

        String trimmed = str.trim();

        if (!trimmed.isEmpty() && trimmed.charAt(0) == '$') {
            trimmed = trimmed.substring(1);
        }
        if (!trimmed.isEmpty() && trimmed.charAt(0) == '(') {
            trimmed = trimmed.substring(1);
        }
        if (!trimmed.isEmpty() && trimmed.charAt(trimmed.length() - 1) == '%') {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (!trimmed.isEmpty() && trimmed.charAt(trimmed.length() - 1) == ')') {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        trimmed = trimmed.replace(CoreConstants.COMMA, CoreConstants.EMPTY);

        Double result = null;

        final int slash = trimmed.indexOf('/');
        if (slash == -1) {
            // No slash
            try {
                result = Double.valueOf(trimmed);
            } catch (final NumberFormatException ex2) {
                // No action
            }
        } else // Found a slash - test for 2 slashes
            if (trimmed.indexOf('/', slash + 1) == -1) {
                try {
                    final double denom = Double.parseDouble(trimmed.substring(slash + 1));

                    if (denom != 0.0) {
                        final double numer = Double.parseDouble(trimmed.substring(0, slash));
                        result = Double.valueOf(numer / denom);
                    }
                } catch (final NumberFormatException ex1) {
                    // No action
                }
            }

        return result;
    }
}
