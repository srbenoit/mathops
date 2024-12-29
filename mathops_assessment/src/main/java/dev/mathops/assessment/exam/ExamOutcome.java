package dev.mathops.assessment.exam;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.Realizable;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * An outcome of the exam, which represents an action that is to be taken based on the exam results. Outcomes may
 * include updating placement status, awarding course credit, updating course grades, etc.
 */
public final class ExamOutcome extends AbstractXmlObject implements Realizable {

    /** A condition that determines whether the outcome should happen. */
    public Formula condition;

    /** A set of actions to take when the outcome should happen. */
    private final List<ExamOutcomeAction> actions;

    /**
     * a set of prerequisites that must ALL be satisfied before the outcome is awarded. Each element in the list is a
     * {@code ExamOutcomePrereq} object
     */
    private final List<ExamOutcomePrereq> prereqs;

    /**
     * A set of validating conditions for the outcome - at least one of these conditions must be true for the outcome to
     * be awarded (The validations will be evaluated in the order in which they appear in the XML file, and the
     * validating rule that allows the outcome to be awarded will have its "how" field stored in the database with the
     * outcome. Each element in the list is a {@code ExamOutcomeValidation} object.).
     */
    private final List<ExamOutcomeValidation> validations;

    /**
     * True to log an entry in the database if credit is denied, false to skip logging the denial.
     */
    public boolean logDenial = true;

    /**
     * Constructs a new {@code ExamOutcome}.
     */
    public ExamOutcome() {
        super();

        this.actions = new ArrayList<>(1);
        this.prereqs = new ArrayList<>(0);
        this.validations = new ArrayList<>(0);
    }

    /**
     * Makes a clone of the object.
     *
     * @return a copy of the original object
     */
    ExamOutcome deepCopy() {

        final ExamOutcome copy = new ExamOutcome();

        if (this.condition != null) {
            copy.condition = this.condition.deepCopy();
        }

        for (final ExamOutcomeAction action : this.actions) {
            copy.actions.add(action.deepCopy());
        }

        for (final ExamOutcomePrereq prereq : this.prereqs) {
            copy.prereqs.add(prereq.deepCopy());
        }

        for (final ExamOutcomeValidation validation : this.validations) {
            copy.validations.add(validation.deepCopy());
        }

        copy.logDenial = this.logDenial;

        return copy;
    }

    /**
     * Adds an action to the outcome.
     *
     * @param action the {@code ExamOutcomeActionn} to add
     */
    void addAction(final ExamOutcomeAction action) {

        this.actions.add(action);
    }

    /**
     * Gets an iterator over the actions associated with the outcome. Each element in the iteration will be an
     * {@code ExamOutcome}.
     *
     * @return the action iterator
     */
    public Iterator<ExamOutcomeAction> getActions() {

        return this.actions.iterator();
    }

    /**
     * Adds a prerequisite to the outcome.
     *
     * @param prereq the {@code ExamOutcomePrereq} to add
     */
    void addPrereq(final ExamOutcomePrereq prereq) {

        this.prereqs.add(prereq);
    }

    /**
     * Retrieves an iterator over the prerequisites list. Each element in the iteration will be a
     * {@code ExamOutcomePrereq} object.
     *
     * @return the outcome prerequisites iterator
     */
    public Iterator<ExamOutcomePrereq> getPrereqs() {

        return this.prereqs.iterator();
    }

    /**
     * Adds a validation rule to the outcome.
     *
     * @param validation the {@code ExamOutcomeValidation} to add
     */
    void addValidation(final ExamOutcomeValidation validation) {

        this.validations.add(validation);
    }

    /**
     * Retrieves an iterator over the validations list. Each element in the iteration will be a
     * {@code ExamOutcomeValidation} object.
     *
     * @return the outcome validations iterator
     */
    public Iterator<ExamOutcomeValidation> getValidations() {

        return this.validations.iterator();
    }

    /**
     * Realizes this exam, realizing each contained outcome action.
     *
     * @param context the evaluation context
     * @return {@code true} if realization succeeds; {@code false} otherwise
     */
    @Override
    public boolean realize(final EvalContext context) {

        for (final ExamOutcomeAction action : this.actions) {
            if (!action.realize(context)) {
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

        xml.add(ind, "<outcome");
        writeAttribute(xml, "condition", this.condition);

        if (!this.logDenial) {
            xml.add(" log-denial=\"false\"");
        }

        xml.addln(">");

        for (final ExamOutcomeAction action : this.actions) {
            action.appendXml(xml, indent + 1);
        }

        for (final ExamOutcomePrereq prereq : this.prereqs) {
            prereq.appendXml(xml, indent + 1);
        }

        for (final ExamOutcomeValidation validation : this.validations) {
            validation.appendXml(xml, indent + 1);
        }

        xml.add(ind);
        xml.addln("</outcome>");
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.condition)
                + Objects.hashCode(this.actions)
                + Objects.hashCode(this.prereqs)
                + Objects.hashCode(this.validations)
                + Boolean.hashCode(this.logDenial);
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
        } else if (obj instanceof final ExamOutcome outcome) {
            equal = Objects.equals(this.condition, outcome.condition)
                    && Objects.equals(this.actions, outcome.actions)
                    && Objects.equals(this.prereqs, outcome.prereqs)
                    && Objects.equals(this.validations, outcome.validations)
                    && this.logDenial == outcome.logDenial;
        } else {
            equal = false;
        }

        return equal;
    }
}
