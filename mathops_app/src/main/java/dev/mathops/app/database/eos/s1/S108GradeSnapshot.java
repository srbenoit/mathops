package dev.mathops.app.database.eos.s1;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepExecutable;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepStatus;

import javax.swing.SwingWorker;
import java.util.List;

/**
 * STEP 108: Take a snapshot of grades as computed by step 106.
 */
public final class S108GradeSnapshot extends StepExecutable {

    private static final String[] MESSAGE = {
            "Exports a snapshot of the grades computed earlier for comparison to",
            "results of the final grade calculations."};

    /**
     * Constructs a new {@code S108GradeSnapshot}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S108GradeSnapshot(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 108, "Take grade snapshot", MESSAGE, statusDisplay);

        setWorker(new S108Worker(this));
    }

    /**
     * A worker that manages updates during the execution of the step.
     */
    static class S108Worker extends SwingWorker<Boolean, StepStatus> {

        /** The owning step. */
        private final S108GradeSnapshot owner;

        /**
         * Constructs a new {@code S105Worker}.
         */
        S108Worker(final S108GradeSnapshot theOwner) {

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
