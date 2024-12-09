package dev.mathops.app.eos.s2;

import dev.mathops.app.eos.StepDisplay;
import dev.mathops.app.eos.StepList;
import dev.mathops.app.eos.StepManual;
import dev.mathops.commons.CoreConstants;

/** STEP 201. */
public final class S201PlaceSitesInMaintenanceMode extends StepManual {

    private static final String[] MESSAGE = {
            "Log into the admin web site and check that there are no active Placement or",
            "proctored exams in progress.",
            CoreConstants.SPC,
            "If there are not, turn on Maintenance mode in web sites, and set messages",
            "to indicate when sites will be back online (roughly one hour later for the",
            "Placement and Tutorial sites, the morning of the first day of classes for",
            "the course site."};

    /**
     * Constructs a new {@code S201PlaceSitesInMaintenanceMode}.  This should be called on the AWT event dispatch
     * thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S201PlaceSitesInMaintenanceMode(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 201, "Place Sites in Maintenance Mode", MESSAGE, statusDisplay);
    }
}
