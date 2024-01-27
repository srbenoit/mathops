package dev.mathops.session.txn.handlers;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A record of a single answer on an exam question.
 */
public final class StudentExamAnswerRec {

    /** The problem ID. */
    public int id;

    /** The student's response. */
    public String studentAnswer;

    /** The number of points earned on the problem. */
    public double score;

    /** True if the student's answer was correct; false otherwise. */
    public boolean correct;

    /** The objective the problem applies to. */
    public String objective;

    /** The name of the subtest this answer relates to. */
    public String subtest;

    /** The tree reference of the selected item. */
    public String treeRef;

    /**
     * Constructs a new {@code StudentExamAnswer}.
     */
    public StudentExamAnswerRec() {

        // No action
    }

    /**
     * Generate a string representation of the object, in XML format.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder sb = new HtmlBuilder(200);

        sb.addln("  <exam-answer>");

        sb.addln("   <id>", Integer.toString(this.id), "</id>");

        if (this.studentAnswer != null) {
            sb.addln("   <answer>", this.studentAnswer,
                    "</answer>");
            sb.addln("   <correct>", Boolean.toString(this.correct),
                    "</correct>");
            sb.addln("   <score>", Double.toString(this.score),
                    "</score>");
        }

        if (this.subtest != null) {
            sb.addln("   <subtest>", this.subtest, "</subtest>");
        }

        if (this.objective != null) {
            sb.addln("   <objective>", this.objective, "</objective>");
        }

        sb.addln("  </exam-answer>");

        return sb.toString();
    }
}
