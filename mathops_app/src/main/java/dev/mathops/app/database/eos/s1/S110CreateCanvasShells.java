package dev.mathops.app.database.eos.s1;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepManual;

/**
 * STEP 109: Update ProctorU testing windows for the upcoming term.
 */
public final class S110CreateCanvasShells extends StepManual {

    private static final String[] MESSAGE = {
            "Copy and edit Canvas shells for the upcoming term."};

    /**
     * Constructs a new {@code S110CreateCanvasShells}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S110CreateCanvasShells(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 110, "Copy and populate Canvas shells.", MESSAGE, statusDisplay);
    }
}
