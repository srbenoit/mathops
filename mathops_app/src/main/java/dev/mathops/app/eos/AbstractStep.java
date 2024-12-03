package dev.mathops.app.eos;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * The base class panels that represent steps.
 */
abstract class AbstractStep extends JPanel implements ActionListener, MouseListener {

    /** An action command. */
    static final String DETAILS_CMD = "DETAILS";

    /** The button to show details. */
    private final JButton detailsBtn;

    /** A box to check when the task is finished. */
    private final JCheckBox finished;

    /** The owning step list. */
    private final StepList owner;

    /** The display. */
    private final StepDisplay display;

    /** The step details text. */
    final String title;

    /** The step details text. */
    final String[] detailsMessage;

    /** The center panel (subclasses can add controls here). */
    protected final JPanel center;

    /** Results text to be displayed. */
    String results = "";

    /** Notes to be displayed. */
    String notes = "";

    /** The normal background color. */
    private final Color normalColor;

    /** The "selected" background color. */
    private final Color selectedColor;

    /**
     * Constructs a new {@code AbstractStep}.
     *
     * @param theOwner        the owning step list
     * @param stepNumber      the step number
     * @param stepDescription the step description
     * @param stepDetails     the step details text
     * @param statusDisplay   the content of the status display for this panel
     */
    AbstractStep(final StepList theOwner, final int stepNumber, final String stepDescription,
                 final String[] stepDetails, final StepDisplay statusDisplay) {

        super(new StackedBorderLayout());

        this.normalColor = getBackground();
        this.selectedColor = this.normalColor.brighter();

        this.owner = theOwner;
        this.display = statusDisplay;

        this.detailsMessage = stepDetails == null ? null : stepDetails.clone();
        this.title = "Step " + stepNumber + ": " + stepDescription;

        final Border etched = BorderFactory.createEtchedBorder();
        setBorder(etched);

        final String numberString = Integer.toString(stepNumber);
        final Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        final Color blue = ColorNames.getColor("blue2");

        this.finished = new JCheckBox();
        this.finished.setEnabled(true);

        final JPanel top = new JPanel(new StackedBorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        final JLabel lbl1 = new JLabel("Step " + numberString + ":");
        lbl1.setFont(font);
        lbl1.setForeground(blue);
        final JLabel lbl2 = new JLabel(stepDescription);
        lbl2.setFont(font);
        lbl2.setForeground(Color.black);

        this.detailsBtn = new JButton("Details...");
        this.detailsBtn.setActionCommand(DETAILS_CMD);
        this.detailsBtn.addActionListener(this);

        top.add(this.finished, StackedBorderLayout.WEST);
        top.add(lbl1, StackedBorderLayout.WEST);
        top.add(lbl2, StackedBorderLayout.WEST);
        top.add(this.detailsBtn, StackedBorderLayout.EAST);
        add(top, StackedBorderLayout.NORTH);

        this.center = new JPanel(new StackedBorderLayout(5, 3));
        this.center.setOpaque(false);
        final Border padding = BorderFactory.createEmptyBorder(0, 5, 3, 5);
        this.center.setBorder(padding);
        add(this.center, BorderLayout.CENTER);

        addMouseListener(this);
    }

    /**
     * Sets the "finished" checkbox state.
     *
     * @param isFinished true if "finished" should be checked; false if unchecked
     */
    final void setFinished(final boolean isFinished) {

        this.finished.setSelected(isFinished);
    }

    /**
     * Sets the "selected" state of this step.
     *
     * @param isSelected true if selected; false if not
     */
    final void setSelected(final boolean isSelected) {

        if (isSelected) {
            setBackground(this.selectedColor);
            this.display.setStep(this);
        } else {
            setBackground(this.normalColor);
            this.display.setStep(null);
        }
        repaint();
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on a component.
     *
     * @param e the event to be processed
     */
    public final void mouseClicked(final MouseEvent e) {

        this.owner.select(this);
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e the event to be processed
     */
    public final void mousePressed(final MouseEvent e) {

        // No action
    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e the event to be processed
     */
    public final void mouseReleased(final MouseEvent e) {

        // No action
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e the event to be processed
     */
    public final void mouseEntered(final MouseEvent e) {

        // No action
    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e the event to be processed
     */
    public final void mouseExited(final MouseEvent e) {

        // No action
    }
}
