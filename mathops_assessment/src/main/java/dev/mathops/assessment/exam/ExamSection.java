package dev.mathops.assessment.exam;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.Randomizer;
import dev.mathops.assessment.Realizable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A section within an exam. Sections are a portion of an exam that have the property that they may deliver their
 * problems in random order, and may limit the set of resources the student has access to when working on the section.
 * They may also restrict whether a student can return to a section once it has been completed (for example, if one
 * section of an exam does not permit calculators, and another section permits calculators, the student may not be
 * allowed to return to the first section after working on the second, and using a calculator).
 */
public final class ExamSection extends AbstractXmlObject implements Realizable {

    /** Student may use a calculator on the exam. */
    private static final String CALCULATOR = "calculator";

    /** Student may use a preprinted hand-out on the exam. */
    private static final String HANDOUT = "handout";

    /** Student may use their notes on the exam. */
    private static final String NOTES = "notes";

    /** Student may use a textbook on the exam. */
    private static final String TEXTBOOK = "textbook";

    /** The name of the exam section. */
    public String sectionName;

    /** The short name of the exam section. */
    public String shortName;

    /** True if the section presents problems to the student in random order; false otherwise. */
    /* default */ boolean randomOrder;

    /**
     * True if the student may return to the section after moving on to another section of the exam; false otherwise.
     */
    public boolean canComeBack = true;

    /**
     * True if the student can elect to regenerate the section after it is submitted if the student wants to try for a
     * higher score.
     */
    public boolean canRegenerate;

    /**
     * The minimum score the student must achieve on this section before next section becomes available - if the student
     * does not get enough correct on this section when the section is submitted, the section will regenerate a new set
     * of problems and the student can try again (this is intended for homework assignments); if {@code null}, students
     * may view and work on the next section at the same time as this section (subject to can-come-back restrictions and
     * resource availability).
     */
    public Long minMoveonScore;

    /**
     * The minimum score the student must achieve on this section in order to have mastered the material (an exam can be
     * configured via grading rules and outcomes to generate homework credits based on whether all sections were
     * mastered); if {@code null}, the mastery parameters will not be generated, and cannot be used in grading rules or
     * outcomes.
     */
    public Long minMasteryScore;

    /**
     * A list of the resources the student is permitted to use on this section of the exam. Each entry in the list will
     * be a predefined constant {@code String} defined in this class.
     */
    private final List<String> resources;

    /** The set of exam problems that make up the section. */
    private final List<ExamProblem> examProblems;

    /** The order in which to present the problems (possibly random). */
    private int[] problemOrder;

    /** True if the student can access the section. */
    public boolean enabled;

    /** True if the section has been completed at a passing score. */
    public boolean passed;

    /** True if the section has been completed at a mastery score. */
    public boolean mastered;

    /** The student's most recent score on the assignment. */
    public Long score;

    /**
     * Constructs a new, empty {@code ExamSection}.
     */
    public ExamSection() {

        super();

        this.resources = new ArrayList<>(0);
        this.examProblems = new ArrayList<>(5);
    }

    /**
     * Makes a clone of the object.
     *
     * @param exam the exam to which this section belongs
     * @return a copy of the original object
     */
    ExamSection deepCopy(final ExamObj exam) {

        final ExamSection copy = new ExamSection();

        copy.sectionName = this.sectionName;
        copy.shortName = this.shortName;
        copy.randomOrder = this.randomOrder;
        copy.canComeBack = this.canComeBack;
        copy.canRegenerate = this.canRegenerate;
        copy.minMoveonScore = this.minMoveonScore;
        copy.minMasteryScore = this.minMasteryScore;

        copy.resources.addAll(this.resources);

        for (final ExamProblem prob : this.examProblems) {
            copy.examProblems.add(prob.deepCopy(exam));
        }

        if (!this.randomOrder && this.problemOrder != null) {
            copy.problemOrder = new int[this.problemOrder.length];

            System.arraycopy(this.problemOrder, 0, copy.problemOrder, 0, this.problemOrder.length);
        }

        return copy;
    }

    /**
     * Adds a resource that the student is permitted to use during the exam.
     *
     * @param resource the name of the resource to add. One of the predefined {@code String} constants from this class
     * @return {@code true} if the resource name was valid; {@code false} otherwise.
     */
    boolean addResource(final String resource) {

        final boolean ok;

        ok = CALCULATOR.equals(resource) || HANDOUT.equals(resource) || NOTES.equals(resource)
                || TEXTBOOK.equals(resource);

        if (ok) {
            this.resources.add(resource);
        }

        return ok;
    }

    /**
     * Retrieves a list of resources the student is permitted to use during the exam section. Each entry will be a
     * {@code String} constant defined in this class.
     *
     * @return the resources iterator
     */
    public List<String> getResources() {

        return this.resources;
    }

    /**
     * Adds an exam problem to the section.
     *
     * @param problem the {@code ExamProblem} to add
     */
    public void addProblem(final ExamProblem problem) {

        this.examProblems.add(problem);
    }

    /**
     * Retrieves an iterator over the exam problems list. Each element in the iteration will be a {@code ExamProblem}
     * object.
     *
     * @return the problems iterator
     */
    public Iterator<ExamProblem> problems() {

        return this.examProblems.iterator();
    }

    /**
     * Gets a single {@code ExamProblem} from the exam section.
     *
     * @param index the index of the exam problem to get
     * @return the requested exam problem
     */
    public ExamProblem getProblem(final int index) {

        return this.examProblems.get(index);
    }

    /**
     * Gets the number of problems in the section.
     *
     * @return the number of problems
     */
    public int getNumProblems() {

        return this.examProblems.size();
    }

    /**
     * Gets the order in which the problems should be presented.
     *
     * @return the problem order, if the section has been realized; null otherwise
     */
    public int[] getProblemOrder() {

        int[] order = null;

        if (this.problemOrder != null) {
            order = new int[this.problemOrder.length];

            System.arraycopy(this.problemOrder, 0, order, 0, order.length);
        }

        return order;
    }

    /**
     * Sets the order in which the problems should be presented.
     *
     * @param theProblemOrder the problem order
     */
    public void setProblemOrder(final int[] theProblemOrder) {

        int[] order = null;

        if (theProblemOrder != null) {
            order = new int[theProblemOrder.length];

            System.arraycopy(theProblemOrder, 0, order, 0, order.length);
        }

        this.problemOrder = order;
    }

    /**
     * Gets a problem in presentation order. That is, calling this method with the index ranging from 0 to one less than
     * the total number of problems will provide the entire list of problems for the section, but if the section
     * specifies random ordering, they will be returned in random order (but a consistent order, until {@code realize}
     * is called again). This method should not be called before the section is realized, as it will return null.
     *
     * @param index the index of the problem to retrieve
     * @return the problem, or {@code null} if section has not been realized
     */
    public ExamProblem getPresentedProblem(final int index) {

        ExamProblem problem = null;
        final int which;

        if (this.problemOrder != null && index >= 0 && this.problemOrder.length > index) {
            which = this.problemOrder[index];

            if (this.examProblems.size() > which) {
                problem = this.examProblems.get(which);
            }
        }

        return problem;
    }

    /**
     * Realizes this exam, substituting any computed parameter values for parameter tags in source XML, generating the
     * instructions Doc object, and realizing each exam section, subtest, grading rule and outcome.
     *
     * @param context the evaluation context
     * @return {@code true} of realization succeeds; {@code false} otherwise
     */
    @Override
    public boolean realize(final EvalContext context) {

        // Do the random ordering of problems.
        final int size = this.examProblems.size();

        if (size > 0) {
            this.problemOrder = new int[size];

            if (this.randomOrder) {

                // Build a list containing all integers from 0 through N-1
                final List<Long> list = new ArrayList<>(size);

                for (int i = 0; i < size; i++) {
                    list.add(Long.valueOf((long) i));
                }

                // Now randomly order that list into problemOrder.
                for (int i = 0; i < size; i++) {
                    final int which = Randomizer.nextInt(size - i);
                    final Long value = list.get(which);
                    this.problemOrder[i] = value.intValue();

                    Log.info("Choosing number ", value, ": ",
                            this.examProblems.get(value.intValue()).getSelectedRef());

                    list.remove(which);
                }
            } else {
                for (int i = 0; i < size; i++) {
                    this.problemOrder[i] = i;
                }
            }
        } else {
            this.problemOrder = null;
        }

        boolean result = true;

        // Now realize each problem
        for (final ExamProblem prob : this.examProblems) {
            if (prob == null) {
                Log.warning("Problem was null while realizing exam section");
                result = false;
                break;
            } else if (!prob.realize(context)) {
                Log.warning("Problem was null while realizing exam problem");
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * Appends the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void appendXml(final HtmlBuilder xml, final int indent) {

        final String ind = makeIndent(indent);

        xml.add(ind, "<exam-section");
        writeAttribute(xml, "name", this.sectionName);
        writeAttribute(xml, "short-name", this.shortName);

        if (this.randomOrder) {
            xml.add(" random-order=\"true\"");
        }

        if (!this.canComeBack) {
            xml.add(" can-come-back=\"false\"");
        }

        if (this.canRegenerate) {
            xml.add(" can-regenerate=\"true\"");
        }

        if (this.minMoveonScore != null) {
            xml.add(" min-moveon-score=\"", this.minMoveonScore.toString(),
                    CoreConstants.QUOTE);
        }

        if (this.minMasteryScore != null) {
            xml.add(" min-mastery-score=\"", this.minMasteryScore.toString(),
                    CoreConstants.QUOTE);
        }

        if (!this.resources.isEmpty()) {
            xml.add(" resources=\"");

            boolean comma = false;

            for (final String res : this.resources) {

                if (comma) {
                    xml.add(CoreConstants.COMMA_CHAR);
                }

                comma = true;
                xml.add(res);
            }

            xml.add('\"');
        }

        // Print the problem ordering
        if (this.problemOrder != null) {
            xml.add(" order=\"", Integer.toString(this.problemOrder[0]));

            final int count = this.problemOrder.length;
            for (int i = 1; i < count; i++) {
                xml.add(CoreConstants.COMMA, Integer.toString(this.problemOrder[i]));
            }

            xml.add('\"');
        }

        xml.addln('>');

        // Print the exam problems
        for (final ExamProblem prob : this.examProblems) {
            prob.appendXml(xml, indent + 1);
        }

        xml.addln(ind, "</exam-section>");
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.sectionName)
                + Objects.hashCode(this.shortName)
                + Boolean.hashCode(this.randomOrder)
                + Boolean.hashCode(this.canComeBack)
                + Boolean.hashCode(this.canRegenerate)
                + Objects.hashCode(this.minMoveonScore)
                + Objects.hashCode(this.minMasteryScore)
                + Objects.hashCode(this.resources)
                + Objects.hashCode(this.examProblems)
                + Objects.hashCode(this.problemOrder);
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
        } else if (obj instanceof final ExamSection sect) {
            equal = Objects.equals(this.sectionName, sect.sectionName)
                    && Objects.equals(this.shortName, sect.shortName)
                    && this.randomOrder == sect.randomOrder
                    && this.canComeBack == sect.canComeBack
                    && this.canRegenerate == sect.canRegenerate
                    && Objects.equals(this.minMoveonScore, sect.minMoveonScore)
                    && Objects.equals(this.minMasteryScore, sect.minMasteryScore)
                    && Objects.equals(this.resources, sect.resources)
                    && Objects.equals(this.examProblems, sect.examProblems)
                    && Objects.equals(this.problemOrder, sect.problemOrder);
        } else {
            equal = false;
        }

        return equal;
    }
}
