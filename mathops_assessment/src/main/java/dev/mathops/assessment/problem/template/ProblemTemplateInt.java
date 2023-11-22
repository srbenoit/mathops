package dev.mathops.assessment.problem.template;

import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.builder.HtmlBuilder;

import java.io.File;
import java.io.PrintStream;

/**
 * An interface implemented by problems.
 */
interface ProblemTemplateInt {

    /**
     * Determines whether the student has recorded an answer to the problem.
     *
     * @return {@code true} if the student has recorded an answer; {@code false} otherwise
     */
    boolean isAnswered();

    /**
     * Sets the choice that the student entered.
     *
     * @param response the student's response, as an object array
     */
    void recordAnswer(Object[] response);

    /**
     * Clears a student's answer.
     */
    void clearAnswer();

    /**
     * Gets the choice that the student entered.
     *
     * @return the student's response, as an object array
     */
    Object[] getAnswer();

    /**
     * Tests to see whether a particular student response is correct. Subclasses should override to test their
     * particular answer types.
     *
     * @param response the student response
     * @return {@code true} if correct, {@code false} if incorrect
     */
    boolean isCorrect(Object[] response);

    /**
     * Prints subclass-specific attributes on the problem element.
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     */
    void printSubclassAttributes(HtmlBuilder builder);

    /**
     * Prints subclass-specific elements.
     *
     * @param builder    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    void printSubclassXmlBegin(HtmlBuilder builder, int indent);

    /**
     * Prints subclass-specific elements.
     *
     * @param builder    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    void printSubclassXmlEnd(HtmlBuilder builder, int indent);

    /**
     * Prints diagnostic data about the problem to a print stream.
     *
     * @param ps           the print stream to output to
     * @param includeTrees {@code true} to include a dump of contained document tree structures
     */
    void printDiagnostics(PrintStream ps, boolean includeTrees);

    /**
     * Prints subclass-specific diagnostic information.
     *
     * @param ps           the print stream to which to write the data
     * @param includeTrees {@code true} to include a dump of the entire document tree structure
     */
    void printSubclassDiagnostics(PrintStream ps, boolean includeTrees);

    /**
     * Generates the LaTeX representation of the problem.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    an index used to uniquely name files to be included by the LaTeX file
     * @param overwriteAll a 1-boolean array whose only entry contains {@code true} if the user has selected "overwrite
     *                     all"; {@code false} to ask the user each time (this method can update this value to true if
     *                     it is false and the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL])
     * @param builder          the {@code HtmlBuilder} to which to write the LaTeX
     * @param showAnswers  {@code true} to show the correct answers; {@code false} to leave blank
     * @param mode         the current LaTeX mode (T=text, $=in-line math, M=math)
     * @param context      the evaluation context
     */
    void toLaTeX(File dir, int[] fileIndex, boolean[] overwriteAll, HtmlBuilder builder,
                 boolean showAnswers, char[] mode, EvalContext context);
}
