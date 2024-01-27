package dev.mathops.app.problem;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.problem.template.AbstractProblemMultipleChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemChoiceTemplate;
import dev.mathops.commons.log.Log;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

/**
 * A layout manager to lay out a multiple choice (or multiple selection) problems. This is intended to be used in a
 * panel that is placed in a container where the width is known before layout methods are called. For example, it can be
 * placed in the CENTER position of a {@code BorderLayout}, or in a 1-column {@code GridLayout}. To use, construct the
 * layout manager passing in the problem to be laid out, and adding a series of JCheckBox or JRadioButton controls that
 * will be placed to the left of each choice. These should be added in the order that the problem has defined for its
 * presented choices.
 */
final class MultipleChoiceProblemLayout implements LayoutManager2 {

    /** The numeric answer problem being presented. */
    private final AbstractProblemMultipleChoiceTemplate problem;

    /** The container width for which layout has been calculated. */
    private int width;

    /** The computed preferred size, also used as minimum/maximum size. */
    private Dimension size;

    /** The vertical gap between sections (question, choices). */
    private final int sectGap;

    /** The vertical gap between choices. */
    private final int choiceGap;

    /** The left/right margin used to in-set choices from edges of question. */
    private final int choiceMargin;

    /** True to include solution; false to exclude. */
    private final boolean showSolutions;

    /**
     * Construct a new {@code MultipleChoiceProblemLayout}.
     *
     * @param theProblem      the problem being laid out
     * @param theSectGap      the vertical gap to include between sections
     * @param theChoiceGap    the vertical gap to include between choices
     * @param theChoiceMargin the left/right margin for the choice block
     * @param showSolution    true to include solution; false to exclude
     */
    MultipleChoiceProblemLayout(final AbstractProblemMultipleChoiceTemplate theProblem,
                                final int theSectGap, final int theChoiceGap, final int theChoiceMargin,
                                final boolean showSolution) {

        this.problem = theProblem;
        this.sectGap = theSectGap;
        this.choiceGap = theChoiceGap;
        this.choiceMargin = theChoiceMargin;
        this.size = new Dimension(0, 0);
        this.showSolutions = showSolution;

        this.problem.question.uncacheFont();

        for (final ProblemChoiceTemplate choice : this.problem.getChoices()) {
            choice.doc.uncacheFont();
        }

        if (this.problem.solution != null) {
            this.problem.solution.uncacheFont();
        }
    }

    /**
     * If the layout manager uses a per-component string, adds the component {@code comp} to the layout, associating it
     * with the string specified by {@code name}.
     *
     * @param name the string to be associated with the component
     * @param comp the component to be added
     */
    @Override
    public void addLayoutComponent(final String name, final Component comp) {

        // This method is ignored - we use the container's child list instead.
    }

    /**
     * If the layout manager uses a per-component constraints, adds the component {@code comp} to the layout,
     * associating it with the constraints specified by {@code constraints}.
     *
     * @param comp        the component to be added
     * @param constraints constraints on the object's layout
     */
    @Override
    public void addLayoutComponent(final Component comp, final Object constraints) {

        // This method is ignored - we use the container's child list instead.
    }

    /**
     * Removes the specified component from the layout.
     *
     * @param comp the component to be removed
     */
    @Override
    public void removeLayoutComponent(final Component comp) {

        // This method is ignored - we use the container's child list instead.
    }

    /**
     * Calculates the preferred size dimensions for the specified container, given the components it contains.
     *
     * @param parent the container to be laid out
     * @return the preferred component size
     */
    @Override
    public Dimension preferredLayoutSize(final Container parent) {

        layoutContainer(parent);

        return this.size;
    }

    /**
     * Calculates the minimum size dimensions for the specified container, given the components it contains.
     *
     * @param parent the container to be laid out
     * @return the preferred component size
     */
    @Override
    public Dimension minimumLayoutSize(final Container parent) {

        layoutContainer(parent);

        return this.size;
    }

    /**
     * Calculates the maximum size dimensions for the specified container, given the components it contains.
     *
     * @param target the container to be laid out
     * @return the preferred component size
     */
    @Override
    public Dimension maximumLayoutSize(final Container target) {

        layoutContainer(target);

        return this.size;
    }

    /**
     * Lays out the specified container.
     *
     * @param parent the container to be laid out
     */
    @Override
    public void layoutContainer(final Container parent) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final Dimension theSize = parent.getSize();

        // if ((this.width != theSize.width) && (theSize.width > 0)) {

        // Get the size of the content area
        this.width = theSize.width;
        final Insets insets = parent.getInsets();
        int w = this.width - insets.left - insets.right;

        // Lay out the question document
        DocColumn doc = this.problem.question;
        doc.setColumnWidth(w);
        doc.doLayout(this.problem.evalContext, ELayoutMode.TEXT);
        doc.setX(insets.left);
        doc.setY(insets.top);
        int h = insets.top + doc.getHeight() + this.sectGap;

        // Get the list of buttons
        final Component[] children = parent.getComponents();

        // Step 1: Find max width of the buttons.
        int maxW = 0;
        for (final Component child : children) {
            child.setSize(child.getPreferredSize());

            if (child.getWidth() > maxW) {
                maxW = child.getWidth();
            }
        }

        // Step 2: Compute width remaining for choices
        w = w - (2 * this.choiceMargin) - maxW;

        // Step 3: Lay out choices
        final int numPresented = this.problem.getNumPresentedChoices();

        for (int i = 0; i < numPresented; i++) {
            doc = this.problem.getPresentedChoice(i).doc;
            doc.setColumnWidth(w);
        }

        // Step 4: Place choices, centering buttons in front of choices
        final int x = insets.left + this.choiceMargin;

        int maxH;
        for (int i = 0; i < numPresented; i++) {
            if (i < children.length) { // Ignore if no matching button
                doc = this.problem.getPresentedChoice(i).doc;
                maxH = Math.max(children[i].getHeight(), doc.getHeight());

                children[i].setLocation(x, h + ((maxH - children[i].getHeight()) / 2));
                doc.setX(x + maxW);
                doc.setY(h + ((maxH - doc.getHeight()) / 2));

                h += maxH + this.choiceGap;
            }
        }

        // If answers are being shown, and the problem has a solution, show it as well.
        doc = this.problem.solution;

        if ((this.showSolutions) && (doc != null)) {
            h += this.sectGap;

            doc.setColumnWidth(w);
            doc.setX(insets.left);
            doc.setY(h);
            h += doc.getHeight();
        }

        h += insets.bottom;

        // Cache the laid out size, and set the parent's preferences
        this.size = new Dimension(this.width, h);

        if (parent instanceof JComponent) {
            parent.setPreferredSize(this.size);
        }
        // }
    }

    /**
     * Invalidates the layout, indicating that if the layout manager has cached information it should be discarded.
     *
     * @param target the container associated with this layout manager
     */
    @Override
    public void invalidateLayout(final Container target) {

        this.width = 0;
        this.size = new Dimension(0, 0);
    }

    /**
     * Returns the alignment along the x-axis. This specifies how the component would like to be aligned relative to
     * other components. The value should be a number between 0 and 1 where 0 represents alignment along the origin, 1
     * is aligned the farthest away from the origin, 0.5 is centered, etc.
     *
     * @param target the container associated with this layout manager
     * @return the preferred x alignment
     */
    @Override
    public float getLayoutAlignmentX(final Container target) {

        return 0.0f;
    }

    /**
     * Returns the alignment along the y-axis. This specifies how the component would like to be aligned relative to
     * other components. The value should be a number between 0 and 1 where 0 represents alignment along the origin, 1
     * is aligned the farthest away from the origin, 0.5 is centered, etc.
     *
     * @param target the container associated with this layout manager
     * @return the preferred y alignment
     */
    @Override
    public float getLayoutAlignmentY(final Container target) {

        return 0.0f;
    }
}
