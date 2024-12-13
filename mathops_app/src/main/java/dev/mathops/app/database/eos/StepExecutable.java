package dev.mathops.app.database.eos;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.event.ActionEvent;

/**
 * The base class for panels that represent steps.
 */
public class StepExecutable extends AbstractStep {

    /** An action command. */
    private static final String EXECUTE_CMD = "EXECUTE";

    /** The worker that will run the task. */
    private SwingWorker<Boolean, StepStatus> worker;

    /** The button to execute the worker. */
    private final JButton executeBtn;

    /**
     * Constructs a new {@code StepPanel}.
     *
     * @param theOwner        the step list that will hold the step
     * @param stepNumber      the step number
     * @param stepDescription the step description
     * @param stepDetails     the step details text
     * @param statusDisplay   the content of the status display for this panel
     */
    protected StepExecutable(final StepList theOwner, final int stepNumber, final String stepDescription,
                   final String[] stepDetails, final StepDisplay statusDisplay) {

        super(theOwner, stepNumber, stepDescription, stepDetails, statusDisplay);

        this.executeBtn = new JButton("Execute");
        this.executeBtn.setActionCommand(EXECUTE_CMD);
        this.executeBtn.addActionListener(this);
        this.center.add(this.executeBtn, StackedBorderLayout.EAST);

        this.results = "(step has hot yet been executed)";
    }

    /**
     * Sets the swing worker.
     *
     * @param theWorker the worker that will run the task
     */
    public void setWorker(final SwingWorker<Boolean, StepStatus> theWorker) {

        this.worker = theWorker;
    }

    /**
     * Called when an action is invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public final void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (EXECUTE_CMD.equals(cmd)) {
            this.setEnabled(false);
            this.worker.execute();
        } else if (DETAILS_CMD.equals(cmd)) {
            JOptionPane.showMessageDialog(this, this.detailsMessage, this.title, JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
