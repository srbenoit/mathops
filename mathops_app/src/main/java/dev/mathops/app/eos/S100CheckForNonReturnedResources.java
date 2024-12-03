package dev.mathops.app.eos;

import dev.mathops.commons.CoreConstants;

import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.util.List;

/**
 * STEP 100: Check for non-returned resources, email students.
 */
final class S100CheckForNonReturnedResources extends SwingWorker<Boolean, StepStatus> {

    /** The panel to update with status. */
    private final StepExecutable panel;

    /**
     * Constructs a new {@code S100CheckForNonReturnedResources}.  This should be called on the AWT event dispatch
     * thread.
     *
     * @param theOwner the step list that will hold the step
     * @param status   the status display
     */
    S100CheckForNonReturnedResources(final StepList theOwner, final StepDisplay status) {

        super();

        final String[] message = {
                "Queries the [stresource] table for records with NULL return date,",
                "indicating resources that are still checked out.",
                CoreConstants.SPC,
                "Email any students with late resources and ask them to return them",
                "immediately to avoid getting charged a replacement fee."};

        this.panel = new StepExecutable(theOwner, 100, "Check for non-returned resources", message, status, this);
    }

    /**
     * Gets the step panel.
     *
     * @return the step panel
     */
    public StepExecutable getPanel() {

        return this.panel;
    }

    /**
     * Called on the AWT event dispatch thread after "doInBackground" has completed.
     */
    public void done() {

        this.panel.setFinished(true);
    }

    /**
     * Called on the AWT event dispatch thread asynchronously with data from "publish".
     *
     * @param chunks the chunks being processed
     */
    @Override
    protected void process(final List<StepStatus> chunks) {

        if (!chunks.isEmpty()) {
            final StepStatus last = chunks.getLast();

            final String task = last.currentTask();
            final int percent = last.percentComplete();
        }
    }

    /**
     * Fires a "publish" action to send status to the UI.
     *
     * @param percentage the percentage complete
     * @param task       the current task
     */
    private void firePublish(final int percentage, final String task) {

        publish(new StepStatus(percentage, task));
    }

    /**
     * Executes table construction logic on a worker thread.
     *
     * @return TRUE if successful; FALSE if not
     */
    @Override
    protected Boolean doInBackground() {

        return Boolean.TRUE;
    }
}
