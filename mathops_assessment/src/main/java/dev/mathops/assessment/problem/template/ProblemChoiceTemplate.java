package dev.mathops.assessment.problem.template;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.Realizable;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.DocColumnInst;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.formula.ErrorValue;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.problem.inst.ProblemChoiceInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.io.PrintStream;
import java.util.Objects;

/**
 * A single choice on a multiple choice or multiple selection problem.
 */
public final class ProblemChoiceTemplate extends AbstractXmlObject implements Realizable {

    /** The choice contents. */
    public DocColumn doc;

    /** The ID of the choice, to be submitted as a student selection. */
    public int choiceId;

    /** The formula that determines if this choice is correct. */
    public Formula correct;

    /** Static position in random-ordered list (0 to randomize position). */
    public int pos;

    /**
     * Constructs a new {@code ProblemChoice}.
     */
    public ProblemChoiceTemplate() {
        super();

        // Empty
    }

    /**
     * Makes a clone of the object.
     *
     * @return a copy of the original object
     */
    ProblemChoiceTemplate deepCopy() {

        final ProblemChoiceTemplate copy = new ProblemChoiceTemplate();

        if (this.doc != null) {
            copy.doc = this.doc.deepCopy();
        }

        copy.choiceId = this.choiceId;

        if (this.correct != null) {
            copy.correct = this.correct.deepCopy();
        }

        copy.pos = this.pos;

        return copy;
    }

    /**
     * Generates a realized multiple choice answer problem.
     *
     * @param context the evaluation context
     * @return {@code true} if realization succeeded; {@code false} otherwise
     */
    @Override
    public boolean realize(final EvalContext context) {

        final boolean ok = true;

        if (this.correct != null) {

            final Object obj = this.correct.evaluate(context);

            if (!(obj instanceof Boolean)) {
                Log.warning("Correctness formula does not return Boolean");

                if (obj instanceof ErrorValue) {
                    Log.warning(obj);
                }

                return false;
            }
        }

        return ok;
    }

    /**
     * Creates a static {@code ProblemChoiceIteration} based on the current realization.
     *
     * @param evalContext the evaluation context
     * @return the generated {@code ProblemChoiceIteration}; {@code null} if creation of the iteration failed
     */
    public ProblemChoiceInst createIteration(final EvalContext evalContext) {

        ProblemChoiceInst result = null;

        final DocColumnInst docIteration = this.doc.createInstance(evalContext);
        final Object correctValue = this.correct.evaluate(evalContext);
        if (correctValue instanceof final Boolean correctBoolean) {
            result = new ProblemChoiceInst(docIteration, this.choiceId, correctBoolean.booleanValue());
        } else {
            Log.warning("Choice correctness formula evaluated to " + correctValue + " rather than Boolean");
        }

        return result;
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
        final String ind2 = makeIndent(indent + 1);

        xml.add(ind, "<choice");
        writeAttribute(xml, "id", Integer.toString(this.choiceId));
        writeAttribute(xml, "correct", this.correct);

        if (this.pos != 0) {
            writeAttribute(xml, "position", Integer.toString(this.pos));
        }

        xml.addln(">");

        if (this.correct != null) {
            xml.add(ind2, "<correct>");
            this.correct.appendChildrenXml(xml);
            xml.addln("</correct>");
        }

        if (this.doc != null) {
            xml.add(ind2, "<content>");
            for (final AbstractDocObjectTemplate child : this.doc.getChildren()) {
                child.toXml(xml, 0);
            }
            xml.addln("</content>");
        }

        xml.addln(ind, "</choice>");
    }

    /**
     * Prints diagnostic data about the problem to a print stream.
     *
     * @param pstream     the print stream to output to
     * @param includeTree {@code true} to include a dump of the entire document tree structure
     */
    void printDiagnostics(final PrintStream pstream, final boolean includeTree) {

        this.doc.printDiagnostics(pstream, includeTree);
    }

    /**
     * Sets the relative size.
     *
     * @param relSize the size, from -3 to +5.
     * @param context the evaluation context
     */
    public void setRelativeSize(final int relSize, final EvalContext context) {

        if (this.doc != null) {
            this.doc.setRelativeSize(relSize);
            this.doc.doLayout(context, ELayoutMode.TEXT);
        }
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return EqualityTests.objectHashCode(this.doc) + this.choiceId << 16
                + EqualityTests.objectHashCode(this.correct) + this.pos;
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
        } else if (obj instanceof final ProblemChoiceTemplate choice) {
            equal = Objects.equals(this.doc, choice.doc)
                    && this.choiceId == choice.choiceId
                    && Objects.equals(this.correct, choice.correct)
                    && this.pos == choice.pos;
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
    void whyNotEqual(final ProblemChoiceTemplate obj, final int indent) {

        if (!Objects.equals(this.doc, obj.doc)) {
            if (this.doc == null || obj.doc == null) {
                Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (doc: ", this.doc, "!=",
                        obj.doc, ")");
            } else {
                Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (doc)");
                this.doc.whyNotEqual(obj.doc, indent + 1);
            }
        }

        if (this.choiceId != obj.choiceId) {
            Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (choiceId: ",
                    Integer.toString(this.choiceId), "!=", Integer.toString(obj.choiceId), ")");
        }

        if (!Objects.equals(this.correct, obj.correct)) {
            if (this.correct == null || obj.correct == null) {
                Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (correct: ", this.correct, "!=",
                        obj.correct, ")");
            } else {
                Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (correct)");
                this.correct.whyNotEqual(obj.correct, indent + 1);
            }
        }

        if (this.pos != obj.pos) {
            Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (pos: ", Integer.toString(this.pos),
                    "!=", Integer.toString(obj.pos), ")");
        }
    }
}
