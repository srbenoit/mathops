package dev.mathops.app.database.eos.s1;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepExecutable;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepStatus;

import javax.swing.SwingWorker;
import java.util.List;

/**
 * STEP 105: Scan for students for whom forfeiting their unfinished course(s) would result in an improved score in those
 * that were completed, and apply automatic forfeits.
 */
public final class S105ApplyForfeits extends StepExecutable {

    private static final String[] MESSAGE = {
            "Scan for students for whom forfeiting their unfinished course(s)",
            "would result in an improved score in those that were completed,",
            "and apply automatic forfeits then run an audit of placement test",
            "scores in Banner."};

    /**
     * Constructs a new {@code S105ApplyForfeits}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S105ApplyForfeits(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 105, "Forfeit unfinished courses to improve grades.", MESSAGE, statusDisplay);

        setWorker(new S105Worker(this));
    }

    /**
     * A worker that manages updates during the execution of the step.
     */
    static class S105Worker extends SwingWorker<Boolean, StepStatus> {

        /** The owning step. */
        private final S105ApplyForfeits owner;

        /**
         * Constructs a new {@code S105Worker}.
         */
        S105Worker(final S105ApplyForfeits theOwner) {

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
