package dev.mathops.assessment.problem.template;

import dev.mathops.assessment.problem.EProblemType;
import dev.mathops.assessment.problem.inst.AbstractProblemInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.builder.HtmlBuilder;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;

/**
 * A dummy concrete implementation of a problem. Used as a placeholder when a problem cannot be instantiated, but we
 * want to retain some known data fields.
 */
public final class ProblemDummyTemplate extends AbstractProblemTemplate {

    /**
     * Constructs an empty {@code ProblemDummy} object.
     */
    public ProblemDummyTemplate() {

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
    public ProblemDummyTemplate deepCopy() {

        final ProblemDummyTemplate copy = new ProblemDummyTemplate();

        innerDeepCopy(copy);

        return copy;
    }

    /**
     * Returns the type of problem.
     *
     * @return the problem type
     */
    @Override
    public EProblemType getType() {

        return EProblemType.DUMMY;
    }

    /**
     * Records a student's answer. There are four acceptable combinations of answer objects that can be included. The
     * first object may be either an Integer or Double, and represents the student's numeric answer. A second (optional)
     * object will be a String containing the units for the answer.
     *
     * @param response a list of answer objects - the answers will be passed directly into the PresentedProblem object
     */
    @Override
    public void recordAnswer(final Object[] response) {

        super.recordAnswer(response);
    }

    /**
     * Clears a student's answer.
     */
    @Override
    public void clearAnswer() {

        super.clearAnswer();
    }

    /**
     * Determines whether the student has recorded an answer to the problem.
     *
     * @return {@code true} if the student has recorded an answer; {@code false} otherwise
     */
    @Override
    public boolean isAnswered() {

        return false;
    }

    /**
     * Tests to see whether a particular student response is correct.
     *
     * @param response the student response
     * @return {@code true} if correct, {@code false} if incorrect
     */
    @Override
    public boolean isCorrect(final Object[] response) {

        return false;
    }

    /**
     * Generate a realized numeric answer problem.
     *
     * @param context the evaluation context
     * @return true if realization succeeded; false otherwise
     */
    @Override
    public boolean realize(final EvalContext context) {

        return super.realize(context);
    }

    /**
     * Realizes the problem and creates a static {@code AbstractProblemIteration} based on that realization.
     *
     * @return {@code null}, since this type of template cannot generate an iteration
     */
    @Override
    public AbstractProblemInst createIteration() {

        return null;
    }

    /**
     * A method that subclasses override to print their subclass-specific attributes on the problem element.
     *
     * @param builder The {@code HtmlBuilder} to which to write the XML.
     */
    @Override
    public void printSubclassAttributes(final HtmlBuilder builder) {

        super.printSubclassAttributes(builder);
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
     * Generate a string representation of the object.
     *
     * @return The string representation of the object.
     */
    @Override
    public String toString() {

        final HtmlBuilder buf = new HtmlBuilder(150);

        buf.addln("<html><head></head><body>");
        buf.addln("<h3>Dummy Problem</h3>");
        if (this.ref != null) {
            buf.addln("<p><b>Reference base:</b> ", this.ref, "</p>");
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
    public void toLaTeX(final File dir, final int[] fileIndex,
                        final boolean[] overwriteAll, final HtmlBuilder builder, final boolean showAnswers,
                        final char[] mode, final EvalContext context) {

        // Emit the problem reference, for debugging
        builder.addln("% ", this.ref);

        builder.addln("Dummy problem placeholder.\\");
    }

    /**
     * Extracts answers from a parameter map (a map from string parameter name to string array of values).
     *
     * @param paramMap the parameter map
     */
    @Override
    public void extractAnswers(final Map<String, String[]> paramMap) {

        clearAnswer();
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
        } else if (obj instanceof final ProblemDummyTemplate problem) {
            equal = innerEquals(problem);
        } else {
            equal = false;
        }

        return equal;
    }
}
