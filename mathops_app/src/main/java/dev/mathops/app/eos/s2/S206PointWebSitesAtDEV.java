package dev.mathops.app.eos.s2;

import dev.mathops.app.eos.StepDisplay;
import dev.mathops.app.eos.StepList;
import dev.mathops.app.eos.StepManual;

/** STEP 206. */
public final class S206PointWebSitesAtDEV extends StepManual {

    private static final String[] MESSAGE = {
            "Log in to each web server (numan, havoc, nibbler) as 'online'",
            "Do the following on each:",
            "  - Copy /opt/zircon/db/db_config_dev.xml to /opt/zircon/db/db_config.xml",
            "  - Start the web server with '/opt/tomcat/bin/catalina.sh start'"};


    /**
     * Constructs a new {@code S206PointWebSitesAtDEV}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner      the step list that will hold the step
     * @param statusDisplay the status display
     */
    public S206PointWebSitesAtDEV(final StepList theOwner, final StepDisplay statusDisplay) {

        super(theOwner, 206, "Point Web Sites at DEV", MESSAGE, statusDisplay);
    }
}
