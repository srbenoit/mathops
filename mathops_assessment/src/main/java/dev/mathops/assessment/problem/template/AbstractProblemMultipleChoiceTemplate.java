package dev.mathops.assessment.problem.template;

import dev.mathops.assessment.formula.Formula;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This subclass of {@code AbstractProblemTemplate} adds the necessary data to present a multiple-choice question to the
 * student, by storing a set of choices. On a multiple-choice question, the student will be asked to select the single,
 * best answer to the problem.
 */
public abstract class AbstractProblemMultipleChoiceTemplate extends AbstractProblemTemplate {

    /** A set of {@code ProblemChoice} to be offered to the student. */
    private final ArrayList<ProblemChoiceTemplate> choices;

    /** An optional formula for the number of choices to show the student. */
    public Formula numChoices;

    /** Formula that evaluates to true to present choices in random order; false otherwise. */
    public Formula randomOrderChoices;

    /** The order in which to present the choices (possibly random). */
    public int[] choiceOrder;

    /**
     * Constructs a new {@code ProblemMultipleChoiceBase}.
     */
    AbstractProblemMultipleChoiceTemplate() {

        super();

        this.choices = new ArrayList<>(5);
    }

    /**
     * Gets the number of choices to be presented to the student.
     *
     * @return the number of choices to be shown to the student
     */
    public final int getNumPresentedChoices() {

        if (this.choiceOrder != null) {
            return this.choiceOrder.length;
        }

        return 0;
    }

    /**
     * Gets a particular presented choice.
     *
     * @param index the index of the choice, as it is to be presented (0 is the first presented choice, 1 is the second,
     *              and so on)
     * @return the requested choice
     */
    public final ProblemChoiceTemplate getPresentedChoice(final int index) {

        if (this.choiceOrder != null) {
            final int which = this.choiceOrder[index];

            return this.choices.get(which);
        }

        return null;
    }

    /**
     * Adds a choice to the problem.
     *
     * @param choice the choice document
     */
    final void addChoice(final ProblemChoiceTemplate choice) {

        this.choices.add(choice);
    }

    /**
     * Gets the list of choices.
     *
     * @return the list of choices
     */
    public final List<ProblemChoiceTemplate> getChoices() {

        return this.choices;
    }

    /**
     * Print subclass-specific attributes on the problem element.
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     */
    @Override
    public void printSubclassAttributes(final HtmlBuilder builder) {

        super.printSubclassAttributes(builder);

        // These were moved to child elements
        // writeAttribute(xml, "num-choices", this.numChoices);
        // writeAttribute(xml, "random-order", this.randomOrderChoices);

        if (this.choiceOrder != null) {
            final int len = this.choiceOrder.length;
            if (len > 0) {
                builder.add(" choice-order=\"", Integer.toString(this.choiceOrder[0]));

                for (int i = 1; i < len; ++i) {
                    builder.add(CoreConstants.COMMA, Integer.toString(this.choiceOrder[i]));
                }

                builder.add('\"');
            }
        }
    }

    /**
     * Prints subclass-specific elements.
     *
     * @param builder    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void printSubclassXmlBegin(final HtmlBuilder builder, final int indent) {

        final String ind = makeIndent(indent);

        if (this.numChoices != null) {
            builder.add(ind, "<num-choices>");
            this.numChoices.appendChildrenXml(builder);
            builder.addln("</num-choices>");
        }

        if (this.randomOrderChoices != null) {
            builder.add(ind, "<random-order>");
            this.randomOrderChoices.appendChildrenXml(builder);
            builder.addln("</random-order>");
        }
    }

    /**
     * Prints subclass-specific elements.
     *
     * @param builder    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public final void printSubclassXmlEnd(final HtmlBuilder builder, final int indent) {

        if (this.choices != null) {
            for (final ProblemChoiceTemplate choice : this.choices) {
                choice.appendXml(builder, indent);
            }
        }
    }

    /**
     * Prints subclass-specific elements.
     *
     * @param ps       the print stream to which to write the data
     * @param includeTrees {@code true} to include a dump of the entire document tree structure
     */
    @Override
    public void printSubclassDiagnostics(final PrintStream ps, final boolean includeTrees) {

        if (this.choices != null && !this.choices.isEmpty()) {
            ps.println("<tr><td valign='top'><b>" + Res.get(Res.CHOICES_LBL) + ":</b></td><td>");

            for (final ProblemChoiceTemplate choice : this.choices) {
                choice.printDiagnostics(ps, includeTrees);

                if (Boolean.TRUE.equals(choice.correct.evaluate(this.evalContext))) {
                    ps.println("</td></tr>");
                    ps.println("<tr><td></td>");
                    ps.println("<td class='red'>" + Res.get(Res.CHOICES_ABOVE_COR) + "</td></tr>");
                    ps.println("<tr><td></td><td>");
                }
            }

            ps.println("</td></tr>");
        }
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    final int mcInnerHashCode() {

        return innerHashCode() + EqualityTests.objectHashCode(this.choices)
                + EqualityTests.objectHashCode(this.numChoices)
                + EqualityTests.objectHashCode(this.randomOrderChoices)
                + EqualityTests.objectHashCode(this.choiceOrder);
    }

    /**
     * Tests non-transient member variables in this base class for equality with another instance.
     *
     * @param other the other instance
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    final boolean mcInnerEquals(final AbstractProblemMultipleChoiceTemplate other) {

        return innerEquals(other) && Objects.equals(this.choices, other.choices)
                && Objects.equals(this.numChoices, other.numChoices)
                && Objects.equals(this.randomOrderChoices, other.randomOrderChoices)
                && Arrays.equals(this.choiceOrder, other.choiceOrder);
    }

    /**
     * Logs messages to indicate why this object is not equal to another.
     *
     * @param obj    the other object
     * @param indent the indent level
     */
    final void mcWhyNotEqual(final AbstractProblemTemplate obj, final int indent) {

        innerWhyNotEqual(obj, indent);

        if (obj instanceof final AbstractProblemMultipleChoiceTemplate prob) {

            if (!Objects.equals(this.choices, prob.choices)) {
                if (this.choices == null || prob.choices == null) {
                    Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (choices: ", this.choices,
                            "!=", prob.choices, ")");
                } else {
                    final int count = this.choices.size();
                    if (count == prob.choices.size()) {
                        for (int i = 0; i < count; ++i) {
                            final ProblemChoiceTemplate c1 = this.choices.get(i);
                            final ProblemChoiceTemplate c2 = prob.choices.get(i);

                            if (!Objects.equals(c1, c2)) {
                                if (c1 == null || c2 == null) {
                                    Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (choices[",
                                            Integer.toString(i), "]: ", c1, "!=", c2, ")");
                                } else {
                                    Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (choices[",
                                            Integer.toString(i), "])");
                                    c1.whyNotEqual(c2, indent + 1);
                                }
                            }
                        }
                    } else {
                        Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (choices.size: ",
                                Integer.toString(this.choices.size()), "!=", Integer.toString(prob.choices.size()), ")");
                    }
                }
            }

            if (!Objects.equals(this.numChoices, prob.numChoices)) {
                if (this.numChoices == null || prob.numChoices == null) {
                    Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (numChoices: ",
                            this.numChoices, "!=", prob.numChoices, ")");
                } else {
                    Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (numChoices)");
                    this.numChoices.whyNotEqual(prob.numChoices, indent + 1);
                }
            }

            if (!Objects.equals(this.randomOrderChoices, prob.randomOrderChoices)) {
                if (this.randomOrderChoices == null || prob.randomOrderChoices == null) {
                    Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (randomOrderChoices: ",
                            this.randomOrderChoices, "!=", prob.randomOrderChoices, ")");
                } else {
                    Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (randomOrderChoices)");
                    this.randomOrderChoices.whyNotEqual(prob.randomOrderChoices, indent + 1);
                }
            }

            if (!Objects.equals(this.choiceOrder, prob.choiceOrder)) {
                Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(), " (choiceOrder: ",
                        this.choiceOrder, "!=", prob.choiceOrder, ")");
            }
        }
    }
}
