package dev.mathops.app.database.eos.s2;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepManual;

/** STEP 205. */
public final class S205DropDEVDatabaseAndImport extends StepManual {

    private static final String[] MESSAGE = {
            "Log into 'baer' as 'informix'.",
            "Type 'dev' to select the DEV profile.",
            "Make sure the prompt has changed to DEV.",
            "  onmode -k",
            "  oninit -s",
            "  onmode -m",
            "Log into 'baer' as 'math'.",
            "Type 'dev' to select the DEV profile.",
            "Make sure the prompt has changed to DEV.",
            "  i (enters ISQL)",
            "Type 'd' then 's' and choose 'math' (there should be only 6 databases to",
            "choose from - if there are more, you are not in DEV).",
            "  Choose 'drop' and choose 'math' again, and confirm.",
            "Exit from ISQL.",
            "  cd /usr/informix/impbackup/EXPORTS/EOS",
            "Verify the prompt still shows DEV.",
            "  dbimport -d devspace math",
            "  mv math.exp math.exp.FA23_prod_edited_for_dev",
            "Log into 'baer' as 'informix'.",
            "Type 'dev' to select the DEV profile.",
            "Make sure the prompt has changed to DEV.",
            "  ontape -s -L 0 -U math -dexit",
            "This turns on unbuffered logging for the DEV database."};

    /**
     * Constructs a new {@code S205DropDEVDatabaseAndImport}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S205DropDEVDatabaseAndImport(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 205, "Drop DEV database and import", MESSAGE, statusDisplay);
    }
}
