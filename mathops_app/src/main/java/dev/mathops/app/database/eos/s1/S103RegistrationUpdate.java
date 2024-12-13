package dev.mathops.app.database.eos.s1;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepExecutable;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepStatus;

import javax.swing.SwingWorker;
import java.util.List;

/**
 * STEP 103: Perform a registration update
 */
public final class S103RegistrationUpdate extends StepExecutable {

    private static final String[] MESSAGE = {
            "Execute batch jobs to:",
            "  - Import applicants from the ODS",
            "  - Import transfer credit from the ODS",
            "  - Import student registrations from the ODS",
            "  - Check the STTERM table",};

    /**
     * Constructs a new {@code S103RegistrationUpdate}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S103RegistrationUpdate(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 103, "Registration Update.", MESSAGE, statusDisplay);

        setWorker(new S103Worker(this));
    }

    /**
     * A worker that manages updates during the execution of the step.
     */
    static class S103Worker extends SwingWorker<Boolean, StepStatus> {

        /** The owning step. */
        private final S103RegistrationUpdate owner;

        /**
         * Constructs a new {@code S103Worker}.
         */
        S103Worker(final S103RegistrationUpdate theOwner) {

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
