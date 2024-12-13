package dev.mathops.app.database.eos.s1;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepManual;
import dev.mathops.commons.CoreConstants;

/**
 * STEP 101: Edit data in "NEXT_TERM" files to be automatically imported during rollover.
 */
public final class S101EditNextTermDataFiles extends StepManual {

    private static final String[] MESSAGE = {
            "Manually edit data files in the 'NEXT_data' directory with data",
            "for the upcoming term. ",
            CoreConstants.SPC,
            "Use data stored in 'NEXT_TERM' as a starting point."};

    /**
     * Constructs a new {@code S101EditNextTermDataFiles}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S101EditNextTermDataFiles(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 101, "Edit 'NEXT_TERM' data files.", MESSAGE, statusDisplay);
    }
}
