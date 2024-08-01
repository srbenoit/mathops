package dev.mathops.app.eos;

import dev.mathops.commons.ui.ColorNames;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The base class for panels that represent steps.
 */
public final class StepPanel extends JPanel implements ActionListener {

    /** An action command. */
    private static final String EXECUTE_CMD = "EXECUTE";

    /** The worker that will run the task. */
    private final SwingWorker<Boolean, StepStatus> worker;

    /** The button to execute the worker. */
    private final JButton execute;

    /** A box to check when the task is finished. */
    private final JCheckBox finished;

    /**
     * Constructs a new {@code StepPanel}.
     *
     * @param stepNumber      the step number
     * @param stepDescription the step description
     * @param statusDisplay   the content of the status display for this panel
     * @param theWorker       the worker that will run the task
     */
    StepPanel(final int stepNumber, final String stepDescription, final JPanel statusDisplay,
              final SwingWorker<Boolean, StepStatus> theWorker) {

        super(new StackedBorderLayout());

        this.worker = theWorker;

        final Border etched = BorderFactory.createEtchedBorder();
        setBorder(etched);

        final String numberString = Integer.toString(stepNumber);
        final Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        final Color blue = ColorNames.getColor("blue2");

        final JPanel topFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        final JLabel lbl1 = new JLabel("Step " + numberString + ":");
        lbl1.setFont(font);
        lbl1.setForeground(blue);
        final JLabel lbl2 = new JLabel(stepDescription);
        lbl2.setFont(font);
        lbl2.setForeground(Color.black);

        topFlow.add(lbl1);
        topFlow.add(lbl2);
        add(topFlow, StackedBorderLayout.NORTH);

        final JPanel center = new JPanel(new BorderLayout(5, 3));
        final Border padding = BorderFactory.createEmptyBorder(0, 5, 3, 5);
        center.setBorder(padding);
        add(center, BorderLayout.CENTER);

        this.execute = new JButton("Execute");
        this.execute.setActionCommand(EXECUTE_CMD);
        this.execute.addActionListener(this);
        center.add(this.execute, BorderLayout.LINE_END);

        this.finished = new JCheckBox();
        this.finished.setEnabled(false);
        center.add(this.finished, BorderLayout.LINE_START);

        center.add(statusDisplay, BorderLayout.CENTER);
    }

    /**
     * Sets the "finished" checkbox state.
     *
     * @param isFinished true if "finished" should be checked; false if unchecked
     */
    void setFinished(final boolean isFinished) {

        this.finished.setSelected(isFinished);
    }

    /**
     * Called when an action is invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (EXECUTE_CMD.equals(cmd)) {
            this.setEnabled(false);
            this.worker.execute();
        }
    }
}
