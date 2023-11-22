package dev.mathops.app.exam;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.document.template.DocColumnPanel;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.log.Log;
import dev.mathops.core.ui.ColorNames;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.Serial;
import java.util.Objects;

/**
 * A panel in which to show the exam instructions.
 */
class InstructionsPanel extends JPanel implements ComponentListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4746897716687268925L;

    /** The scroll pane containing the instructions. */
    private final JScrollPane scrollPane;

    /** The panel that displays the instructions. */
    private final DocColumnPanel instructions;

    /**
     * Constructs a new {@code InstructionsPanel}.
     *
     * @param exam the exam that the student is taking
     */
    InstructionsPanel(final ExamObj exam) {

        super(new BorderLayout(0, 0));

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        setBackground(ColorNames.getColor("white"));

        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1),
                BorderFactory.createLoweredBevelBorder()));

        this.instructions = new DocColumnPanel(Objects.requireNonNullElseGet(exam.instructions, DocColumn::new),
                exam.getEvalContext());

        this.scrollPane = new JScrollPane(this.instructions);
        this.scrollPane.addComponentListener(this);
        this.scrollPane.getVerticalScrollBar().setUnitIncrement(36);
        this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.scrollPane.setWheelScrollingEnabled(true);
        add(this.scrollPane, BorderLayout.CENTER);
    }

    /**
     * Implementation of the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentHidden(final ComponentEvent e) {

        // No action
    }

    /**
     * Implementation of the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentMoved(final ComponentEvent e) {

        // No action
    }

    /**
     * Implementation of the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentShown(final ComponentEvent e) {

        // No action
    }

    /**
     * Implementation of the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentResized(final ComponentEvent e) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if ((this.scrollPane != null) && (this.instructions != null)) {
            final JViewport view = this.scrollPane.getViewport();

            // Set the view's width to the pane's width less that of the bar
            final int width = this.scrollPane.getWidth() - this.scrollPane.getVerticalScrollBar().getWidth();
            view.setSize(new Dimension(width, view.getHeight()));

            // Re-layout the instructions
            this.instructions.setSize(view.getSize());
            this.instructions.doLayout();
        }
    }

    /**
     * Sets the relative size.
     *
     * @param relSize the size, from -3 to +5.
     * @param context the evaluation context
     */
    public void setRelativeSize(final int relSize, final EvalContext context) {

        if (this.instructions != null) {
            this.instructions.column.setRelativeSize(relSize);
            this.instructions.column.doLayout(context, ELayoutMode.TEXT);
        }

        revalidate();
    }
}
