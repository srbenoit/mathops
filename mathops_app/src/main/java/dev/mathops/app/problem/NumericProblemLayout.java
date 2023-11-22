package dev.mathops.app.problem;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.problem.template.ProblemNumericTemplate;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.log.Log;

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
 * A layout manager to lay out a numeric problem. This is intended to be used in a panel that is placed in a container
 * where the width is known before layout methods are called. For example, it can be placed in the CENTER position of a
 * {@code BorderLayout}, or in a 1-column {@code GridLayout}. The layout manager handles four different objects, in
 * addition to laying out the problem and optional solution: - The label and text field for entry of the user's answer -
 * The label containing the correct answer (optional) - A button to copy a calculator's most recent answer (optional) To
 * use, construct the layout manager passing in the problem to lay out, and adding a JLabel and a JTextField, that will
 * form the answer entry area. Adding an optional answer label causes it to be displayed.
 */
final class NumericProblemLayout implements LayoutManager2 {

    /** Object constraint for the entry field label. */
    static final Long ENTRY_LABEL = Long.valueOf(1L);

    /** Object constraint for the entry field. */
    static final Long ENTRY_FIELD = Long.valueOf(2L);

    /** Object constraint for the correct answer field. */
    static final Long CORRECT_ANSWER = Long.valueOf(3L);

    /** Object constraint for the button to copy answer from the calculator. */
    static final Long COPY_FROM_CALCULATOR = Long.valueOf(4L);

    /** The numeric answer problem being presented. */
    private final ProblemNumericTemplate problem;

    /** The container width for which layout has been calculated. */
    private int width;

    /** The computed preferred size, also used as minimum/maximum size. */
    private Dimension size;

    /** The vertical gap between sections (question, choices). */
    private final int sectGap;

    /** True to include answer; false to exclude. */
    private final boolean showAnswer;

    /** True to include solution; false to exclude. */
    private final boolean showSolution;

    /** The list of objects, indexed by constraint. */
    private final Map<Object, Component> objects;

    /** The evaluation context. */
    private final EvalContext context;

    /**
     * Construct a new {@code NumericProblemLayout}.
     *
     * @param theProblem the problem being laid out
     * @param theSectGap the vertical gap to include between the question and answer
     * @param showAns    true to include answer; false to exclude
     * @param showSol    true to include solution; false to exclude
     * @param theContext the evaluation context
     */
    NumericProblemLayout(final ProblemNumericTemplate theProblem, final int theSectGap,
                         final boolean showAns, final boolean showSol, final EvalContext theContext) {

        this.problem = theProblem;
        this.sectGap = theSectGap;
        this.size = new Dimension(0, 0);
        this.showAnswer = showAns;
        this.showSolution = showSol;
        this.context = theContext;

        this.objects = new HashMap<>(5);

        this.problem.question.uncacheFont();

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
     * @param comp        the component to be added.
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

        final Dimension theSize;
        final Insets insets;
        DocColumn doc;
        int h;
        final int w;
        int w1 = 0;
        int w2 = 0;
        int x;

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        theSize = parent.getSize();

        if ((this.width != theSize.width) && (theSize.width > 0)) {

            // Get the size of the content area
            this.width = theSize.width;
            insets = parent.getInsets();
            w = this.width - insets.left - insets.right;
            h = insets.top;

            // Lay out the question document
            doc = this.problem.question;
            doc.setColumnWidth(w);
            doc.doLayout(this.context, ELayoutMode.TEXT);
            doc.setX(insets.left);
            doc.setY(insets.top);
            h += doc.getHeight();

            h += this.sectGap;

            final Component[] children = {this.objects.get(ENTRY_LABEL), this.objects.get(ENTRY_FIELD),
                            this.objects.get(CORRECT_ANSWER), this.objects.get(COPY_FROM_CALCULATOR)};

            // Now, compute the width of the prompt and entry components.
            if (children[0] != null) {
                w1 = children[0].getPreferredSize().width;
            }

            if (children[1] != null) {
                w2 = children[1].getPreferredSize().width;
            }

            // If both will fit on one line, center them below the question.
            // otherwise, center each on its own line below the question.
            if (w > (w1 + w2)) {

                x = insets.left + ((w - (w1 + w2)) / 2);

                if (x < 0) {
                    x = 0;
                }

                if (children[0] != null) {
                    children[0].setLocation(x, h);
                    children[0].setSize(children[0].getPreferredSize());
                    x += children[0].getSize().width;

                    if (x < 0) {
                        x = 0;
                    }
                }

                if (children[1] != null) {
                    children[1].setLocation(x, h);
                    children[1].setSize(children[1].getPreferredSize());
                }

                if (children[0] == null) {

                    if (children[1] != null) {
                        h += children[1].getSize().height;
                    }
                } else if (children[1] == null) {
                    h += children[0].getSize().height;
                } else {
                    h += Math.max(children[0].getSize().height, children[1].getSize().height);
                }
            } else {

                if (children[0] != null) {
                    x = insets.left + ((w - children[0].getSize().width) / 2);

                    if (x < 0) {
                        x = 0;
                    }

                    children[0].setLocation(x, h);
                    children[0].setSize(children[0].getPreferredSize());
                    h += children[0].getSize().height;
                }

                if (children[1] != null) {
                    x = insets.left + ((w - children[1].getSize().width) / 2);

                    if (x < 0) {
                        x = 0;
                    }

                    children[1].setLocation(x, h);
                    children[1].setSize(children[1].getPreferredSize());
                    h += children[1].getSize().height;
                }
            }

            h += this.sectGap;

            // If there is a button to get data from the calculator, add it.
            if (children[3] != null) {
                w1 = children[3].getPreferredSize().width;
                x = insets.left + ((w - w1) / 2);

                if (x < 0) {
                    x = 0;
                }

                children[3].setLocation(x, h);
                children[3].setSize(children[3].getPreferredSize());
                h += children[3].getSize().height;
            }

            h += this.sectGap;

            // If the problem is configured to show answers, do so here.
            if ((this.showAnswer) && (children[2] != null)) {
                children[2].setSize(children[2].getPreferredSize());
                x = insets.left + ((w - children[2].getSize().width) / 2);

                if (x < 0) {
                    x = 0;
                }

                children[2].setLocation(x, h);
                h += children[2].getSize().height;
            }

            // If the problem is configured to show solution, do so here.
            doc = this.problem.solution;

            if ((this.showSolution) && (doc != null)) {

                h += this.sectGap;

                doc.setColumnWidth(w);
                doc.doLayout(this.context, ELayoutMode.TEXT);
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
