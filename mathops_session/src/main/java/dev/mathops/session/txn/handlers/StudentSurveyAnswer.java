package dev.mathops.session.txn.handlers;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A record of a single answer on a survey question.
 */
public final class StudentSurveyAnswer {

    /** The problem ID. */
    public int id;

    /** The student's response. */
    public String studentAnswer;

    /**
     * Constructs a new {@code StudentSurveyAnswer}.
     */
    public StudentSurveyAnswer() {

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

        sb.addln("  <survey-answer>");
        sb.addln("   <id>", Integer.toString(this.id), "</id>");

        if (this.studentAnswer != null) {
            sb.addln("   <answer>", this.studentAnswer, "</answer>");
        }

        sb.addln("  </survey-answer>");

        return sb.toString();
    }
}
