package dev.mathops.assessment.problem.template;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.Realizable;
import dev.mathops.assessment.formula.ErrorValue;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.io.PrintStream;
import java.util.Objects;

/**
 * A template specification for acceptance of a numeric answer.
 */
public final class ProblemAcceptNumberTemplate extends AbstractXmlObject implements Realizable {

    /** True if the answer must be an integer; false otherwise. */
    public boolean forceInteger;

    /**
     * The permitted variance from the correct answer in order for a student's response to be considered correct (a
     * constant - to make variance variable, use formula).
     */
    public Number varianceConstant;

    /**
     * The permitted variance from the correct answer in order for a student's response to be considered correct.
     */
    public Formula varianceFormula;

    /** The correct answer formula. */
    public Formula correctAnswer;

    /**
     * Constructs a new {@code ProblemAcceptNumberTemplate}.
     */
    ProblemAcceptNumberTemplate() {

        super();
    }

    /**
     * Makes a clone of the object.
     *
     * @return a copy of the original object
     */
    ProblemAcceptNumberTemplate deepCopy() {

        final ProblemAcceptNumberTemplate copy = new ProblemAcceptNumberTemplate();

        copy.forceInteger = this.forceInteger;

        copy.varianceConstant = this.varianceConstant;
        if (this.varianceFormula != null) {
            copy.varianceFormula = this.varianceFormula.deepCopy();
        }

        if (this.correctAnswer != null) {
            copy.correctAnswer = this.correctAnswer.deepCopy();
        }

        return copy;
    }

    /**
     * Realizes the object by testing that all formulae can be evaluated with the provided parameter set.
     *
     * @param context the evaluation context
     * @return {@code true} if realization succeeded; {@code false} otherwise
     */
    @Override
    public boolean realize(final EvalContext context) {

        if (this.varianceFormula != null) {
            final Object obj = this.varianceFormula.evaluate(context);

            if (!(obj instanceof Number)) {
                Log.warning("Invalid object type for maximum acceptable variance");

                if (obj instanceof ErrorValue) {
                    Log.warning(obj);
                }

                return false;
            }
        }

        if (this.correctAnswer != null) {
            final Object obj = this.correctAnswer.evaluate(context);

            if (!(obj instanceof Number)) {
                Log.warning("Unable to evaluate correct answer formula.");

                if (obj instanceof ErrorValue) {
                    Log.warning(obj);
                }

                return false;
            }
        }

        return true;
    }

    /**
     * Gets the correct answer.
     *
     * @param context the evaluation context
     * @return the correct answer
     */
    public Number getCorrectAnswerValue(final EvalContext context) {

        Number answer = null;

        if (this.correctAnswer != null) {
            final Object ans = this.correctAnswer.evaluate(context);

            if (ans instanceof Number) {
                answer = (Number) ans;
            }
        }

        return answer;
    }

    /**
     * Gets the allowed variance.
     *
     * @param context the evaluation context
     * @return the allowed variance
     */
    public Number getVarianceValue(final EvalContext context) {

        Number var = null;

        if (this.varianceConstant != null) {
            var = this.varianceConstant;
        } else if (this.varianceFormula != null) {
            final Object ans = this.varianceFormula.evaluate(context);

            if (ans instanceof Number) {
                var = (Number) ans;
            }
        }

        return var;
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
        final String ind2 = makeIndent(indent + 1);

        xml.add(ind, "<accept-number");
        xml.addAttribute("type", this.forceInteger ? "integer" : "real", 0);
        if (this.varianceConstant != null) {
            xml.addAttribute("variance", this.varianceConstant, 0);
        }
        xml.addln('>');

        if (this.varianceFormula != null) {
            xml.add(ind2, "<variance>");
            this.varianceFormula.appendChildrenXml(xml);
            xml.addln("</variance>");
        }

        if (this.correctAnswer != null) {
            xml.add(ind2, "<correct-answer>");
            this.correctAnswer.appendChildrenXml(xml);
            xml.addln("</correct-answer>");
        }

        xml.addln(ind, "</accept-number>");
    }

    /**
     * Prints diagnostic data about the problem to a print stream.
     *
     * @param ps the print stream to output to
     */
    void printDiagnostics(final PrintStream ps) {

        ps.print(this.forceInteger ? "Integers" : "Numbers");

        if (this.correctAnswer != null) {
            ps.print(" Correct answer = ");
            this.correctAnswer.print(ps);

            if (this.varianceConstant != null) {
                ps.print(" +/- ");
                ps.print(this.varianceConstant.toString());
            } else if (this.varianceFormula != null) {
                ps.print(" +/- ");
                this.varianceFormula.print(ps);
            }
        }
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return (this.forceInteger ? 0x00100000 : 0)
                + EqualityTests.objectHashCode(this.varianceConstant)
                + EqualityTests.objectHashCode(this.varianceFormula)
                + EqualityTests.objectHashCode(this.correctAnswer);
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
        } else if (obj instanceof final ProblemAcceptNumberTemplate accept) {
            equal = this.forceInteger == accept.forceInteger
                    && Objects.equals(this.varianceConstant, accept.varianceConstant)
                    && Objects.equals(this.varianceFormula, accept.varianceFormula)
                    && Objects.equals(this.correctAnswer, accept.correctAnswer);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Logs messages to indicate why this object is not equal to another.
     *
     * @param obj    the other object
     * @param indent the indent level
     */
    void whyNotEqual(final ProblemAcceptNumberTemplate obj, final int indent) {

        if (this.forceInteger != obj.forceInteger) {
            Log.info(makeIndent(indent), //
                    "UNEQUAL ProblemAcceptNumber (forceInteger: ",
                    Boolean.toString(this.forceInteger), "!=",
                    Boolean.toString(obj.forceInteger), ")");
        }

        if (!Objects.equals(this.varianceConstant, obj.varianceConstant)) {
            Log.info(makeIndent(indent), "UNEQUAL ProblemAcceptNumber (variance: ",
                    this.varianceConstant, "!=", obj.varianceConstant, ")");
        }

        if (!Objects.equals(this.varianceFormula, obj.varianceFormula)) {
            if (this.varianceFormula == null || obj.varianceFormula == null) {
                Log.info(makeIndent(indent), "UNEQUAL ProblemAcceptNumber (variance: ",
                        this.varianceFormula, "!=", obj.varianceFormula, ")");
            } else {
                Log.info(makeIndent(indent), "UNEQUAL ProblemAcceptNumber (variance)");
                this.varianceFormula.whyNotEqual(obj.varianceFormula, indent + 1);
            }
        }

        if (!Objects.equals(this.correctAnswer, obj.correctAnswer)) {
            if (this.correctAnswer == null || obj.correctAnswer == null) {
                Log.info(makeIndent(indent), "UNEQUAL ProblemAcceptNumber (correctAnswer: ",
                        this.correctAnswer, "!=", obj.correctAnswer, ")");
            } else {
                Log.info(makeIndent(indent), "UNEQUAL ProblemAcceptNumber (correctAnswer)");
                this.correctAnswer.whyNotEqual(obj.correctAnswer, indent + 1);
            }
        }
    }
}
