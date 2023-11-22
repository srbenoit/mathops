package dev.mathops.app.problem;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.problem.template.ProblemAutoCorrectTemplate;
import dev.mathops.core.log.Log;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

/**
 * A layout manager to lay out AutoCorrect problems. This is intended to be used in a panel that is placed in a
 * container where the width is known before layout methods are called. For example, it can be placed in the CENTER
 * position of a {@code BorderLayout}, or in a 1-column {@code GridLayout}.
 */
final class AutoCorrectProblemLayout implements LayoutManager2 {

    /** The problem being presented. */
    private final ProblemAutoCorrectTemplate problem;

    /** The container width for which layout has been calculated. */
    private int width;

    /** The computed preferred size, also used as minimum/maximum size. */
    private Dimension size;

    /**
     * Construct a new {@code AutoCorrectProblemLayout}.
     *
     * @param theProblem the problem being laid out
     */
    AutoCorrectProblemLayout(final ProblemAutoCorrectTemplate theProblem) {

        this.problem = theProblem;
        this.size = new Dimension(0, 0);

        this.problem.question.uncacheFont();
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

        // Get the size of the content area
        this.width = theSize.width;
        final Insets insets = parent.getInsets();
        final int w = this.width - insets.left - insets.right;

        // Lay out the question document
        final DocColumn doc = this.problem.question;
        doc.setColumnWidth(w);
        doc.doLayout(this.problem.evalContext, ELayoutMode.TEXT);
        doc.setX(insets.left);
        doc.setY(insets.top);

        // Cache the laid out size, and set the parent's preferences
        final int h = insets.top + doc.getHeight() + insets.bottom;
        this.size = new Dimension(this.width, h);

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
