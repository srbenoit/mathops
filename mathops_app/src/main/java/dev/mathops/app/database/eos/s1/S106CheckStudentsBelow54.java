package dev.mathops.app.database.eos.s1;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepExecutable;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepStatus;
import dev.mathops.commons.CoreConstants;

import javax.swing.SwingWorker;
import java.util.List;

/**
 * STEP 106: Precompute grades and scan for students who passed all exams but did not reach the 54 point threshold for a
 * "C" grade.  Offer these students a "D" grade if they want to finish.
 */
public final class S106CheckStudentsBelow54 extends StepExecutable {

    private static final String[] MESSAGE = {
            "Precompute grades and scan for students who passed all exams but",
            "did not reach the 54 point threshold for a 'C' grade.",
            CoreConstants.SPC,
            "Offer these students a 'D' grade if they want to finish."};

    /**
     * Constructs a new {@code S106CheckStudentsBelow54}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S106CheckStudentsBelow54(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 106, "Scan for students below 54 points", MESSAGE, statusDisplay);

        setWorker(new S106Worker(this));
    }

    /**
     * A worker that manages updates during the execution of the step.
     */
    static class S106Worker extends SwingWorker<Boolean, StepStatus> {

        /** The owning step. */
        private final S106CheckStudentsBelow54 owner;

        /**
         * Constructs a new {@code S105Worker}.
         */
        S106Worker(final S106CheckStudentsBelow54 theOwner) {

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
