package dev.mathops.app.eos;

import dev.mathops.commons.CoreConstants;

import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * STEP 101: Edit data in "NEXT_TERM" files to be automatically imported during rollover.
 */
final class S101EditNextTermDataFiles {
    /** The panel to update with status. */
    private final StepManual panel;

    /**
     * Constructs a new {@code S101EditNextTermDataFiles}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner the step list that will hold the step
     * @param status   the status display
     */
    S101EditNextTermDataFiles(final StepList theOwner, final StepDisplay status) {

        super();

        final String[] message = {
                "Manually edit data files in the 'NEXT_data' directory with data",
                "for the upcoming term. ",
                CoreConstants.SPC,
                "Use data stored in 'NEXT_TERM' as a starting point."};

        this.panel = new StepManual(theOwner, 101, "Edit 'NEXT_TERM' data files.", message, status);
    }

    /**
     * Gets the step panel.
     *
     * @return the step panel
     */
    public StepManual getPanel() {

        return this.panel;
    }
}
