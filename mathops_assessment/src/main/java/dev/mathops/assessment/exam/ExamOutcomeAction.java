package dev.mathops.assessment.exam;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.Realizable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An outcome of the exam, which represents an action that is to be taken based on the exam results. Outcomes may
 * include updating placement status, awarding course credit, updating course grades, etc.
 */
public final class ExamOutcomeAction extends AbstractXmlObject implements Realizable {

    /** Action to grant placement out of a particular course. */
    public static final String INDICATE_PLACEMENT = "indicate-placement";

    /** Action to grant credit for a particular course. */
    public static final String INDICATE_CREDIT = "indicate-credit";

    /** Action to grant credit for a particular course. */
    public static final String INDICATE_LICENSED = "indicate-licensed";

    /** The type of action, one of the constants defined in this class. */
    public String type;

    /** The course that the action applies to. */
    public String course;

    /**
     * Constructs a new {@code ExamOutcomeAction}.
     */
    public ExamOutcomeAction() {
        super();

        // Empty
    }

    /**
     * Makes a clone of the object.
     *
     * @return a copy of the original object
     */
    ExamOutcomeAction deepCopy() {

        final ExamOutcomeAction copy = new ExamOutcomeAction();

        copy.type = this.type;
        copy.course = this.course;

        return copy;
    }

    /**
     * Realizes this exam, which does nothing since there are no contained objects that require realization.
     *
     * @param context the evaluation context
     * @return {@code true} if realization succeeds; {@code false} otherwise
     */
    @Override
    public boolean realize(final EvalContext context) {

        return true;
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

        if (INDICATE_PLACEMENT.equals(this.type) || INDICATE_CREDIT.equals(this.type)
                || INDICATE_LICENSED.equals(this.type)) {
            xml.add(ind, "<", this.type);
            writeAttribute(xml, "course", this.course);
            xml.addln("/>");
        }
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.type) + Objects.hashCode(this.course);
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
        } else if (obj instanceof final ExamOutcomeAction action) {
            equal = Objects.equals(this.type, action.type)
                    && Objects.equals(this.course, action.course);
        } else {
            equal = false;
        }

        return equal;
    }
}
