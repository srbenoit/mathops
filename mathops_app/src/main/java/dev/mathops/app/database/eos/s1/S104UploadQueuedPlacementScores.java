package dev.mathops.app.database.eos.s1;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepExecutable;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepStatus;

import javax.swing.SwingWorker;
import java.util.List;

/**
 * STEP 104: Upload queued placement scores, audit placement records
 */
public final class S104UploadQueuedPlacementScores extends StepExecutable {

    private static final String[] MESSAGE = {
            "Upload any placement scores that were queued due to Banner outage,",
            "run an audit of placement test scores in Banner, and then edit the",
            "Java code that audits test scores to indicate the date of last audit."};

    /**
     * Constructs a new {@code S104UploadQueuedPlacementScores}.  This should be called on the AWT event dispatch
     * thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S104UploadQueuedPlacementScores(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 104, "Upload queued placement scores and audit.", MESSAGE, statusDisplay);

        setWorker(new S104Worker(this));
    }

    /**
     * A worker that manages updates during the execution of the step.
     */
    static class S104Worker extends SwingWorker<Boolean, StepStatus> {

        /** The owning step. */
        private final S104UploadQueuedPlacementScores owner;

        /**
         * Constructs a new {@code S104Worker}.
         */
        S104Worker(final S104UploadQueuedPlacementScores theOwner) {

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
