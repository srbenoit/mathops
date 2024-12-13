package dev.mathops.app.database.eos.s1;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepExecutable;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepStatus;

import javax.swing.SwingWorker;
import java.util.List;

/**
 * STEP 107: Close out any Incompletes that are still open.
 */
public final class S107ProcessIncompletes extends StepExecutable {

    private static final String[] MESSAGE = {
            "Find any Incompletes that are still open and close them."};

    /**
     * Constructs a new {@code S107ProcessIncompletes}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S107ProcessIncompletes(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 107, "Process pending incompletes", MESSAGE, statusDisplay);

        setWorker(new S107Worker(this));
    }

    /**
     * A worker that manages updates during the execution of the step.
     */
    static class S107Worker extends SwingWorker<Boolean, StepStatus> {

        /** The owning step. */
        private final S107ProcessIncompletes owner;

        /**
         * Constructs a new {@code S105Worker}.
         */
        S107Worker(final S107ProcessIncompletes theOwner) {

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
