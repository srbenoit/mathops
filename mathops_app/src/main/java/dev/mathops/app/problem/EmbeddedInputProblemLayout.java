package dev.mathops.app.problem;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.document.template.DocColumnPanel;
import dev.mathops.assessment.problem.template.ProblemEmbeddedInputTemplate;
import dev.mathops.commons.log.Log;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.HashMap;
import java.util.Map;

/**
 * A layout manager to lay out an embedded input problem. This is intended to be used in a panel that is placed in a
 * container where the width is known before layout methods are called. For example, it can be placed in the CENTER
 * position of a {@code BorderLayout}, or in a 1-column {@code GridLayout}. The layout manager handles the question
 * object, which contains the embedded inputs, the optional solution, and a label to indicate the correctness of the
 * entered answer.
 */
final class EmbeddedInputProblemLayout implements LayoutManager2 {

    /** Object constraint for the question. */
    static final Long QUESTION = Long.valueOf(1L);

    /** Object constraint for the solution. */
    static final Long SOLUTION = Long.valueOf(2L);

    /** Object constraint for the correctness label. */
    static final Long CORRECTNESS = Long.valueOf(3L);

    /** Object constraint for the correct answer panel. */
    static final Long CORRECT_ANSWER = Long.valueOf(4L);

    /** The embedded input problem being presented. */
    private final ProblemEmbeddedInputTemplate problem;

    /** The vertical gap between sections (question, solution). */
    private final int sectGap;

    /** The computed preferred size, also used as minimum/maximum size. */
    private Dimension size;

    /** True to include answer; false to exclude. */
    private final boolean showAnswers;

    /** True to include solution; false to exclude. */
    private final boolean showSolutions;

    /** The list of objects, indexed by constraint. */
    private final Map<Object, Component> objects;

    /**
     * Construct a new {@code EmbeddedInputProblemLayout}.
     *
     * @param theProblem   the problem being laid out
     * @param theSectGap   the vertical gap to include between the question and solution
     * @param showAnswer   true to include answer; false to exclude
     * @param showSolution true to include solution; false to exclude
     */
    EmbeddedInputProblemLayout(final ProblemEmbeddedInputTemplate theProblem, final int theSectGap,
                               final boolean showAnswer, final boolean showSolution) {

        this.problem = theProblem;
        this.sectGap = theSectGap;
        this.showAnswers = showAnswer;
        this.showSolutions = showSolution;
        this.size = new Dimension(0, 0);

        this.objects = new HashMap<>(5);

        this.problem.question.uncacheFont();

        if (this.problem.correctAnswer != null) {
            this.problem.correctAnswer.uncacheFont();
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

        this.objects.put(constraints, comp);
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

        // Get the size of the content area
        final int width = theSize.width;
        final Insets insets = parent.getInsets();
        final int w = width - insets.left - insets.right;

        // Lay out the question document
        DocColumn doc = this.problem.question;
        doc.setColumnWidth(w);
        doc.doLayout(this.problem.evalContext, ELayoutMode.TEXT);
        doc.setX(0);
        doc.setY(0);
        int h = insets.top + doc.getHeight();

        Component child = this.objects.get(QUESTION);
        child.setSize(new Dimension(doc.getWidth(), doc.getHeight()));
        child.setLocation(insets.left, insets.top);

        h += this.sectGap;

        child = this.objects.get(CORRECTNESS);

        int x;
        if (child != null) {

            if (child instanceof DocColumnPanel) {
                doc = ((DocColumnPanel) child).column;
                doc.setColumnWidth(w);
                doc.doLayout(this.problem.evalContext, ELayoutMode.TEXT);

                x = insets.left + (w - child.getSize().width) / 2;
                if (x < 0) {
                    x = 0;
                }

                doc.setX(0);
                doc.setY(0);

                child.setSize(new Dimension(doc.getWidth(), doc.getHeight()));
            } else {
                child.setSize(child.getPreferredSize());
                x = insets.left + (w - child.getSize().width) / 2;
                if (x < 0) {
                    x = 0;
                }
            }

            child.setLocation(x, h);
            child.setVisible(this.showAnswers || this.showSolutions);

            h += child.getHeight();
            h += this.sectGap;
        }

        // If answers are being shown, and the problem has a correct
        // answer, show it as well.
        child = this.objects.get(CORRECT_ANSWER);

        if (this.showAnswers && child instanceof final DocColumnPanel colPanel) {
            doc = colPanel.column;
            doc.setColumnWidth(w);
            doc.doLayout(this.problem.evalContext, ELayoutMode.TEXT);

            doc.setX(0);
            doc.setY(0);

            child.setSize(new Dimension(doc.getWidth(), doc.getHeight()));
            child.setLocation(insets.left, h);
            child.setVisible(true);

            h += doc.getHeight();
            h += this.sectGap;
        }

        child = this.objects.get(SOLUTION);

        if (this.showSolutions && child instanceof final DocColumnPanel colPanel) {

            doc = colPanel.column;
            doc.setColumnWidth(w);
            doc.doLayout(this.problem.evalContext, ELayoutMode.TEXT);

            doc.setX(0);
            doc.setY(0);

            child.setSize(new Dimension(doc.getWidth(), doc.getHeight()));
            child.setLocation(insets.left, h);
            child.setVisible(true);

            h += doc.getHeight();
        }

        h += insets.bottom;

        // Cache the laid out size, and set the parent's preferences
        this.size = new Dimension(width, h);

        if (parent instanceof JComponent) {
            parent.setPreferredSize(this.size);
        }
    }

    /**
     * Invalidates the layout, indicating that if the layout manager has cached information it should be discarded.
     *
     * @param target the container associated with this layout manager
     */
    @Override
    public void invalidateLayout(final Container target) {

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
