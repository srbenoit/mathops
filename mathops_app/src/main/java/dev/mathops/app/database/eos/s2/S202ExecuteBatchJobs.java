package dev.mathops.app.database.eos.s2;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepExecutable;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepStatus;

import javax.swing.SwingWorker;
import java.util.List;

/** STEP 202. */
public final class S202ExecuteBatchJobs extends StepExecutable {

    private static final String[] MESSAGE = {
            "Execute ordinary batch jobs to update production data:",
            "  - Import applicants from the ODS",
            "  - Import new students from the ODS",
            "  - Import transfer credit from the ODS",
            "  - Import student registrations from Banner",
            "  - Check the STTERM table",
            "  - Send queued placement test scores",
            "  - Audit placement test scores in Banner",
            "Edit the Java code that audits test scores to indicate the date of last audit"};

    /**
     * Constructs a new {@code S202ExecuteBatchJobs}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S202ExecuteBatchJobs(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 202, "Execute batch jobs.", MESSAGE, statusDisplay);

        setWorker(new S202Worker(this));
    }

    /**
     * A worker that manages updates during the execution of the step.
     */
    static class S202Worker extends SwingWorker<Boolean, StepStatus> {

        /** The owning step. */
        private final S202ExecuteBatchJobs owner;

        /**
         * Constructs a new {@code S104Worker}.
         */
        S202Worker(final S202ExecuteBatchJobs theOwner) {

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
