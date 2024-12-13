package dev.mathops.app.database.eos.s2;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepManual;

/** STEP 204. */
public final class S204EditDatabaseExportForDEV extends StepManual {

    private static final String[] MESSAGE = {
            "Edit the database export so it can be imported as DEV:",
            "  cd math.exp",
            "  vi stude*.unl",
            "(change 'PROD' to 'DEV' in two places)",
            "  vi which*.unl",
            "(change 'PROD' to 'DEV' in two places)",
            "  vi math.sql",
            "Change 'prodspace' to 'devspace' globally with the following:",
            "  :1,$s/prodspace/devspace/g"};

    /**
     * Constructs a new {@code S204EditDatabaseExportForDEV}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S204EditDatabaseExportForDEV(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 204, "Edit database export for loading into DEV", MESSAGE, statusDisplay);
    }
}
