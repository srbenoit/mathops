package dev.mathops.web.front;

import dev.mathops.commons.installation.Installation;
import dev.mathops.commons.installation.Installations;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LoggingSubsystem;
import dev.mathops.web.cron.Cron;
import dev.mathops.web.cron.CronJobs;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A listener to do installation configuration for the front controller context (rather than within the servlet
 * itself).
 */
public final class ContextListener implements ServletContextListener { // Public so Tomcat can see it...

    /** The installation. */
    private Installation installation;

    /** The scheduler. */
    private ScheduledExecutorService scheduler;

    /**
     * Constructs a new {@code ContextListener}.
     */
    public ContextListener() { // Public so Tomcat can see it...

        // No action
    }

    /**
     * Called when the context is initialized.
     */
    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {

        final ServletContext ctx = servletContextEvent.getServletContext();

        // Gather enough information to configure logging before logging anything
        final String baseDir = ctx.getInitParameter("zircon-base-dir");
        final String cfgFile = ctx.getInitParameter("zircon-cfg-file");
        final String reports = ctx.getInitParameter("zircon-run-reports");

        final File baseFile = baseDir == null ? null : new File(baseDir);
        this.installation = Installations.get().getInstallation(baseFile, cfgFile);
        LoggingSubsystem.setInstallation(this.installation);

        Log.info(Res.get(Res.CONTEXT_INITIALIZING), ctx.getServerInfo());
        Log.config(Res.get(Res.BASE_DIR), baseDir);
        Log.config(Res.get(Res.CFG_FILE), cfgFile);

        ctx.setAttribute("Installation", this.installation);

        // final ServerInstance instance = //
        ServerInstance.get(this.installation);
        // Log.config(CoreConstants.SPC, instance.toString());

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        final Cron cron = Cron.getInstance();
        this.scheduler.scheduleAtFixedRate(cron, 0L, 10L, TimeUnit.SECONDS);

        if ("true".equalsIgnoreCase(reports)) {
            cron.registerJob(new CronJobs());
            Log.info(Res.get(Res.REPORTS_ENABLED));
        }

        Log.info(Res.get(Res.CONTEXT_INITIALIZED));
    }

    /**
     * Called when the context is destroyed.
     */
    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {

        Log.info(Res.get(Res.CRON_TERMINATING));
        this.scheduler.shutdownNow();

        Log.info(Res.get(Res.CONTEXT_DESTROYED));
        this.installation = null;
    }
}
