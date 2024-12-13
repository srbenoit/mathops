package dev.mathops.app.database.eos.s2;

import dev.mathops.app.database.eos.StepDisplay;
import dev.mathops.app.database.eos.StepList;
import dev.mathops.app.database.eos.StepManual;
import dev.mathops.commons.CoreConstants;

/** STEP 203.*/
public final class S203BoundProductionDatabaseAndExport extends StepManual {

    private static final String[] MESSAGE = {
            "Stop all web servers that connect to the database.",
            CoreConstants.SPC,
            "Log into 'baer' as 'informix', ensure the PROD profile is active.",
            "  onmode -k",
            "  oninit -s",
            "  onmode -m",
            "Log into 'baer' as 'math', ensure the PROD profile is active.",
            "  cd /usr/informix/impbackup/EXPORTS/EOS",
            "If there are leftover exports from the prior term, delete them, and",
            "move any 'data' or 'stc' files into a term-specific archive folder.",
            "  dbexport -ss math"};

    /**
     * Constructs a new {@code S203BoundProductionDatabaseAndExport}.  This should be called on the AWT event dispatch
     * thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S203BoundProductionDatabaseAndExport(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 203, "Bounce production database and export", MESSAGE, statusDisplay);
    }
}
