package dev.mathops.app.database.eos.s1;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepExecutable;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepStatus;
import dev.mathops.commons.CoreConstants;

import javax.swing.SwingWorker;
import java.util.List;

/**
 * STEP 100: Check for non-returned resources, email students.
 */
public final class S100CheckForNonReturnedResources extends StepExecutable {

    private static final String[] MESSAGE = {
            "Queries the [stresource] table for records with NULL return date,",
            "indicating resources that are still checked out.",
            CoreConstants.SPC,
            "Email any students with late resources and ask them to return them",
            "immediately to avoid getting charged a replacement fee."};

    /**
     * Constructs a new {@code S100CheckForNonReturnedResources}.  This should be called on the AWT event dispatch
     * thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S100CheckForNonReturnedResources(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 100, "Check for non-returned resources", MESSAGE, statusDisplay);

        setWorker(new S100Worker(this));
    }

    /**
     * A worker that manages updates during the execution of the step.
     */
    static class S100Worker extends SwingWorker<Boolean, StepStatus> {

        /** The owning step. */
        private final S100CheckForNonReturnedResources owner;

        /**
         * Constructs a new {@code S100Worker}.
         */
        S100Worker(final S100CheckForNonReturnedResources theOwner) {

            this.owner = theOwner;
        }

        /**
         * Called on the AWT event dispatch thread after "doInBackground" has completed.
         */
        public void done() {

            this.owner.setFinished(true);
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
        protected final Boolean doInBackground() {

            return Boolean.TRUE;
        }
    }
}
