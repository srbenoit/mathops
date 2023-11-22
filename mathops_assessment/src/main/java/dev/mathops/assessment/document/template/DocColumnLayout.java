package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.log.Log;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

/**
 * A layout manager to lay out a document consisting of multiple DocColumns. This is intended to be used in a panel that
 * is placed in a container where the width is known before layout methods are called. For example, it can be placed in
 * the CENTER position of a {@code BorderLayout}, or in a 1-column {@code GridLayout}.
 */
final class DocColumnLayout implements LayoutManager2 {

    /** The column to be laid out. */
    private final DocColumn column;

    /** The container width for which layout has been calculated. */
    private int width;

    /** The computed preferred size, also used as minimum/maximum size. */
    private Dimension size;

    /** The evaluation context. */
    private final EvalContext context;

    /**
     * Construct a new {@code DocColumnLayout}.
     *
     * @param theColumn  the {@code DocColumn} to be laid out
     * @param theContext the evaluation context
     */
    DocColumnLayout(final DocColumn theColumn, final EvalContext theContext) {

        this.column = theColumn;
        this.size = new Dimension(0, 0);
        this.context = theContext;
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
     * @param parent the parent container
     */
    @Override
    public void layoutContainer(final Container parent) {

        final Insets insets;
        int h;
        final int w;

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final Dimension theSize = parent.getSize();

        if ((this.width != theSize.width) && (theSize.width > 0)) {

            // Get the size of the content area
            this.width = theSize.width;
            insets = parent.getInsets();
            w = this.width - insets.left - insets.right;
            h = insets.top;

            this.column.setColumnWidth(w);
            this.column.doLayout(this.context, ELayoutMode.TEXT);
            this.column.setX(insets.left);
            this.column.setY(h);
            h += this.column.getHeight();
            h += insets.bottom;

            // Cache the laid out size, and set the parent's preferences
            this.size = new Dimension(this.width, h);

            if (parent instanceof JComponent) {
                parent.setPreferredSize(this.size);
            }
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
