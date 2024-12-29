package dev.mathops.assessment.exam;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A prerequisite that must be satisfied before a grading condition will provide a positive result.
 */
public final class ExamOutcomePrereq extends AbstractXmlObject {

    /** A formula that evaluates to a boolean that tells whether the prerequisite is satisfied. */
    public Formula prerequisiteFormula;

    /**
     * Constructs a new {@code ExamGradingPrereq}.
     */
    public ExamOutcomePrereq() {
        super();

        // Empty
    }

    /**
     * Makes a clone of the object.
     *
     * @return a copy of the original object
     */
    ExamOutcomePrereq deepCopy() {

        final ExamOutcomePrereq copy = new ExamOutcomePrereq();

        if (this.prerequisiteFormula != null) {
            copy.prerequisiteFormula = this.prerequisiteFormula.deepCopy();
        }

        return copy;
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

        xml.add(ind, "<prereq>");

        if (this.prerequisiteFormula != null) {
            xml.add(escape(this.prerequisiteFormula.toString()));
        }

        xml.addln("</prereq>");
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.prerequisiteFormula);
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
        } else if (obj instanceof final ExamOutcomePrereq prereq) {
            equal = Objects.equals(this.prerequisiteFormula, prereq.prerequisiteFormula);
        } else {
            equal = false;
        }

        return equal;
    }
}
