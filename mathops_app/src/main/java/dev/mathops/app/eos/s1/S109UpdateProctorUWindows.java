package dev.mathops.app.eos.s1;

import dev.mathops.app.eos.StepDisplay;
import dev.mathops.app.eos.StepList;
import dev.mathops.app.eos.StepManual;

/**
 * STEP 109: Update ProctorU testing windows for the upcoming term.
 */
public final class S109UpdateProctorUWindows extends StepManual {

    private static final String[] MESSAGE = {
            "Update testing windows in ProctorU for the upcoming term."};

    /**
     * Constructs a new {@code S109UpdateProctorUWindows}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S109UpdateProctorUWindows(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 109, "Update ProctorU testing windows.", MESSAGE, statusDisplay);
    }
}
