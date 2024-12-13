package dev.mathops.app.database.eos.s1;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepManual;
import dev.mathops.commons.CoreConstants;

/**
 * STEP 102: See if Incompletes are warranted
 */
public final class S102IdentifyIncompletes extends StepManual {

    private static final String[] MESSAGE = {
            "Examine students with special circumstances to see if Incompletes are",
            "warranted.",
            CoreConstants.SPC,
            "For each, determine whether it will be counted in deadline schedule or",
            "not, and if not, what the deadline will be."};

    /**
     * Constructs a new {@code S102IdentifyIncompletes}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S102IdentifyIncompletes(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 102, "Identify Incompletes.", MESSAGE, statusDisplay);
    }
}
