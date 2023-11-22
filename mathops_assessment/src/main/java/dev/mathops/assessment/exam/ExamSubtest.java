package dev.mathops.assessment.exam;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A specification for a subtest on an exam. A subtest is a group of problems taken from an exam, with weights assigned
 * to each problem. After an exam is submitted, each subtest generates a raw score by taking the student's score on each
 * problem (the problems may have a particular point value, as specified in the {@code ExamProblem} class, but default
 * to 1 point each), and multiplying it by the weight assigned to that problem, then summing these over the set of
 * problems in the subtest.<br>
 * <br>
 * Subtests are then used in grading rules to make decisions based on the student's performance. Each subtest is given a
 * name, and the resulting raw score can be referred to using parameter notation in the grading rules. For example, if
 * there is a subtest named "trigonometry", then grading rules can include the tag {trigonometry} to access the
 * student's raw score on that subtest.
 */
public final class ExamSubtest extends AbstractXmlObject {

    /** The name of the subtest. */
    public String subtestName;

    /**
     * The list of {@code ExamSubtestProblem} objects describing the problems included in this subtest.
     */
    private final List<ExamSubtestProblem> examSubtestProblems;

    /** The student's evaluated score on the subtest, null if not scored. */
    public Double score;

    /**
     * Constructs a new {@code ExamSubtest}.
     */
    public ExamSubtest() {
        super();

        this.examSubtestProblems = new ArrayList<>(1);
    }

    /**
     * Makes a clone of the object.
     *
     * @return a copy of the original object
     */
    ExamSubtest deepCopy() {

        final ExamSubtest copy = new ExamSubtest();

        copy.subtestName = this.subtestName;

        for (final ExamSubtestProblem prob : this.examSubtestProblems) {
            copy.examSubtestProblems.add(prob.deepCopy());
        }

        return copy;
    }

    /**
     * Adds an exam problem to the section.
     *
     * @param problem the {@code ExamSubtestProblem} to add
     */
    void addSubtestProblem(final ExamSubtestProblem problem) {

        this.examSubtestProblems.add(problem);
    }

    /**
     * Retrieves an iterator over the exam subtest problems list. Each element in the iteration will be a
     * {@code ExamSubtestProblem} object.
     *
     * @return the subtest problems iterator
     */
    public Iterator<ExamSubtestProblem> getSubtestProblems() {

        return this.examSubtestProblems.iterator();
    }

    /**
     * Gets the number of subtest problems in the subtest.
     *
     * @return the number of subtest problems
     */
    public int getNumSubtestProblems() {

        return this.examSubtestProblems.size();
    }

    /**
     * Realizes this subtest, realizing each contained subtest problem object.
     *
     * @param context the evaluation context
     * @return {@code true} if realization succeeds; {@code false} otherwise
     */
    boolean realize(final EvalContext context) {

        for (final ExamSubtestProblem prob : this.examSubtestProblems) {
            if (!prob.realize(context)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Appends the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void appendXml(final HtmlBuilder xml, final int indent) {

        final String ind = makeIndent(indent);

        xml.add(ind, "<subtest");
        writeAttribute(xml, "name", this.subtestName);
        writeAttribute(xml, "score", this.score);
        xml.addln(">");

        // Print the subtest problems
        for (final ExamSubtestProblem prob : this.examSubtestProblems) {
            prob.appendXml(xml, indent + 1);
        }

        xml.addln(ind, "</subtest>");
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return EqualityTests.objectHashCode(this.subtestName)
                + EqualityTests.objectHashCode(this.examSubtestProblems);
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
        } else if (obj instanceof final ExamSubtest subtest) {
            equal = Objects.equals(this.subtestName, subtest.subtestName)
                    && Objects.equals(this.examSubtestProblems, subtest.examSubtestProblems);
        } else {
            equal = false;
        }

        return equal;
    }
}
