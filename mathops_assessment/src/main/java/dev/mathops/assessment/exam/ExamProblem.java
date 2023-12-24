package dev.mathops.assessment.exam;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.Randomizer;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A specification for a single problem on an exam. This is not a direct reference to a {@code Problem} object, but may
 * in fact refer to a set of several {@code Problem} objects, one of which is selected when the exam is to be realized.
 * This allows exam authors to create a pool of problems to test a particular skill, then allow the exam to select one
 * of those for inclusion on a realized exam, allowing exams to be random, while still guaranteeing that each skill is
 * correctly represented in the composition of the exam.
 */
public final class ExamProblem extends AbstractXmlObject {

    /** The exam to which this problem belongs. */
    private final ExamObj exam;

    /** A numeric ID for the problem (used when recording answers). */
    public int problemId;

    /**
     * A string with which to label the problem within the exam, such as "1.", "3a)", or "Extra Credit:" - the label
     * will be printed with the exam problem (There are some string substitutions that are possible in this field,
     * allowing for random ordering of problems. The following is a list of tags that, if found in the string, will be
     * replaced with the appropriate value: {num-within-exam} --> The overall problem number, starting from 1 and
     * increasing (ignoring section boundaries) to the end of the exam. {num-within-section} --> The problem number
     * (starting from 1) within the section = null(numbering will restart at 1 in each new section) {section-num} -->
     * The section number, starting at 1. Note that by combining these as in "{section-num}.{num-within-section}", a
     * problem numbering like 1.1, 1.2, 2.1, = null etc. can be achieved. In the above, the problem numbering will be
     * based on the actual order that the problems are presented to the student, which will be different from the order
     * they are specified here if random ordering is enabled. If this value is null, it will be treated as if it
     * contained only"{num-within-exam}".
     */
    public String problemName;

    /** Flag indicating the problem must be answered by the student. */
    /* default */ boolean mandatory;

    /** The number of points the problem is worth in the exam. */
    /* default */ Double numPoints;

    /** A list of the possible problems to use. */
    private final ArrayList<AbstractProblemTemplate> problems;

    /** The reference of the selected problem. */
    private String selectedRef;

    /**
     * The problem that was selected when the exam was realized, or null if the exam has not yet been realized.
     */
    private AbstractProblemTemplate selectedProblem;

    /**
     * Constructs a new {@code ExamProblem}.
     *
     * @param theExam the exam to which this problem belongs
     */
    public ExamProblem(final ExamObj theExam) {
        super();

        this.exam = theExam;
        this.problems = new ArrayList<>(5);
    }

    /**
     * Makes a clone of the object.
     *
     * @param theExam the exam to which this problem belongs
     * @return a copy of the original object
     */
    ExamProblem deepCopy(final ExamObj theExam) {

        final ExamProblem copy = new ExamProblem(theExam);

        copy.problemId = this.problemId;
        copy.problemName = this.problemName;
        copy.mandatory = this.mandatory;
        copy.numPoints = this.numPoints;

        for (final AbstractProblemTemplate prob : this.problems) {
            copy.problems.add(prob.deepCopy());
        }

        return copy;
    }

    /**
     * Adds a problem to the set of problems the {@code ExamProblem} can choose from when realizing.
     *
     * @param problem the {@code Problem} to add
     */
    public void addProblem(final AbstractProblemTemplate problem) {

        this.problems.add(problem);

        // If this problem was referenced as selected, select it.
        if (this.selectedRef != null) {

            if (this.selectedRef.equals(problem.ref)) {
                this.selectedProblem = problem;
            }
        }
    }

    /**
     * Gets the number of problems available to choose from.
     *
     * @return the number of problems
     */
    public int getNumProblems() {

        return this.problems.size();
    }

    /**
     * Gets a particular problem.
     *
     * @param index the index of the problem to get, from 0 to one less than the value returned by
     *              {@code getNumProblems}
     * @return the requested problem
     */
    public AbstractProblemTemplate getProblem(final int index) {

        return this.problems.get(index);
    }

    /**
     * Sets a particular problem.
     *
     * @param index   the index of the problem to set, from 0 to one less than the value returned by
     *                {@code getNumProblems}
     * @param problem the problem
     */
    public void setProblem(final int index, final AbstractProblemTemplate problem) {

        this.problems.set(index, problem);
    }

    /**
     * Removes all problems.
     */
    public void clearProblems() {

        this.problems.clear();
        this.selectedProblem = null;
        this.selectedRef = null;
    }

    /**
     * Sets the reference of the selected problem.
     *
     * @param theSelectedRef the selected problem reference
     */
    void setSelectedRef(final String theSelectedRef) {

        this.selectedRef = theSelectedRef;
        this.selectedProblem = null;

        for (final AbstractProblemTemplate prob : this.problems) {

            if (theSelectedRef.equals(prob.ref)) {
                this.selectedProblem = prob;

                break;
            }
        }
    }

    /**
     * Gets the reference of the problem that was selected during realization.
     *
     * @return the selected problem reference, or {@code null} if not yet realized
     */
    String getSelectedRef() {

        return this.selectedRef;
    }

    /**
     * Sets the problem that was selected during realization.
     *
     * @param theSelectedProblem the selected problem
     */
    public void setSelectedProblem(final AbstractProblemTemplate theSelectedProblem) {

        this.selectedProblem = theSelectedProblem;

        if (theSelectedProblem != null) {
            this.selectedRef = theSelectedProblem.ref;
        } else {
            this.selectedRef = null;
        }
    }

    /**
     * Gets the problem that was selected during realization.
     *
     * @return the selected problem, or {@code null} if not yet realized
     */
    public AbstractProblemTemplate getSelectedProblem() {

        return this.selectedProblem;
    }

    /**
     * Realizes this exam problem by choosing one of possibly several available problems, then realizing that problem.
     *
     * @param context the context of the owning exam
     * @return {@code true} if realization succeeded; {@code false} otherwise.
     */
    boolean realize(final EvalContext context) {

        // Choose one problem at random and save it as the selected problem
        if (this.problems.size() == 1) {
            this.selectedProblem = this.problems.get(0);
        } else if (this.problems.size() > 1) {
            final int which = Randomizer.nextInt(this.problems.size());
            this.selectedProblem = this.problems.get(which);
        } else {
            Log.warning("No problems configured in an exam problem.");

            return false;
        }

        this.selectedRef = this.selectedProblem.ref;
        this.selectedProblem.evalContext.setPrintTarget(context.isPrintTarget());

        return this.selectedProblem.realize(this.selectedProblem.evalContext);
    }

    /**
     * Appends the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void appendXml(final HtmlBuilder xml, final int indent) {

        String root = null;

        if (this.exam != null) {
            root = this.exam.ref;
        }

        final String ind = makeIndent(indent);
        xml.add(ind, "<exam-problem");
        writeAttribute(xml, "id", Integer.toString(this.problemId));
        writeAttribute(xml, "name", this.problemName);

        if (this.mandatory) {
            xml.add(" mandatory=\"true\"");
        }

        writeAttribute(xml, "points", this.numPoints);

        if (this.selectedProblem != null) {
            String ref = this.selectedProblem.ref;

            // Carve off the reference root
            if (ref != null && root != null && ref.startsWith(root)) {
                ref = ref.substring(root.length());
            }

            xml.add(" selected=\"", ref, CoreConstants.QUOTE);
        }

        xml.addln(">");

        // Print the problem references
        for (final AbstractProblemTemplate prob : this.problems) {

            if (prob != null) {
                String ref = prob.ref;

                // Carve off the reference root
                if (ref != null && root != null && ref.startsWith(root)) {
                    ref = ref.substring(root.length());

                    if (ref.startsWith(CoreConstants.DOT)) {
                        ref = ref.substring(1);
                    }
                }

                xml.addln(ind, " <reference>", ref, "</reference>");
            }
        }

        xml.addln(ind, "</exam-problem>");
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return this.problemId + Objects.hashCode(this.problemName) //
                + Boolean.hashCode(this.mandatory) //
                + Objects.hashCode(this.numPoints)
                + Objects.hashCode(this.problems)
                + Objects.hashCode(this.selectedRef)
                + Objects.hashCode(this.selectedProblem);
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
        } else if (obj instanceof final ExamProblem prob) {
            equal = this.problemId == prob.problemId
                    && Objects.equals(this.problemName, prob.problemName)
                    && this.mandatory == prob.mandatory
                    && Objects.equals(this.numPoints, prob.numPoints)
                    && Objects.equals(this.problems, prob.problems)
                    && Objects.equals(this.selectedRef, prob.selectedRef)
                    && Objects.equals(this.selectedProblem, prob.selectedProblem);
        } else {
            equal = false;
        }

        return equal;
    }
}
