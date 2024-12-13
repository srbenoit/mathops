package dev.mathops.app.database.eos;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

/**
 * The base class for panels that represent manual steps.  The user can enter notes and mark the step as complete, but
 * there is no automated processing.
 */
public class StepManual extends AbstractStep {

    /**
     * Constructs a new {@code StepManual}.
     *
     * @param theOwner        the step list that will hold the step
     * @param stepNumber      the step number
     * @param stepDescription the step description
     * @param stepDetails     the step details text
     * @param statusDisplay   the content of the status display for this panel
     */
    protected StepManual(final StepList theOwner, final int stepNumber, final String stepDescription,
                         final String[] stepDetails, final StepDisplay statusDisplay) {

        super(theOwner, stepNumber, stepDescription, stepDetails, statusDisplay);
    }

    /**
     * Called when an action is invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (DETAILS_CMD.equals(cmd)) {
            JOptionPane.showMessageDialog(this, this.detailsMessage, this.title, JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
