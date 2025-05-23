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
    private Installation installation = null;

    /** The scheduler. */
    private ScheduledExecutorService scheduler = null;

    /**
     * Constructs a new {@code ContextListener}.
     */
    public ContextListener() { // Public so Tomcat can see it...

        // No action
    }

    /**
     * Called when the context is initialized.
     *
     * @param sce the servlet context event
     */
    @Override
    public void contextInitialized(final ServletContextEvent sce) {

        final ServletContext ctx = sce.getServletContext();

        // Gather enough information to configure logging before logging anything
        final String baseDir = ctx.getInitParameter("zircon-base-dir");
        final String cfgFile = ctx.getInitParameter("zircon-cfg-file");
        final String reports = ctx.getInitParameter("zircon-run-reports");
        final String canvasToken = ctx.getInitParameter("zircon-canvas-token");

        final File baseFile = baseDir == null ? null : new File(baseDir);
        this.installation = Installations.get().getInstallation(baseFile, cfgFile);
        LoggingSubsystem.setInstallation(this.installation);

        final String initializintMsg = Res.get(Res.CONTEXT_INITIALIZING);
        final String serverInfo = ctx.getServerInfo();
        Log.info(initializintMsg, serverInfo);

        final String baseDirMsg = Res.get(Res.BASE_DIR);
        Log.config(baseDirMsg, baseDir);

        final String configFileMsg = Res.get(Res.CFG_FILE);
        Log.config(configFileMsg, cfgFile);

        ctx.setAttribute("Installation", this.installation);
        if (canvasToken != null) {
            ctx.setAttribute("CanvasToken", canvasToken);
        }

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        final Cron cron = Cron.getInstance();
        this.scheduler.scheduleAtFixedRate(cron, 0L, 10L, TimeUnit.SECONDS);

        if ("true".equalsIgnoreCase(reports)) {
            cron.registerJob(new CronJobs());
            final String reportsMsg = Res.get(Res.REPORTS_ENABLED);
            Log.info(reportsMsg);
        }

        final String initializedMsg = Res.get(Res.CONTEXT_INITIALIZED);
        Log.info(initializedMsg);
    }

    /**
     * Called when the context is destroyed.
     *
     * @param sce the servlet context event
     */
    @Override
    public void contextDestroyed(final ServletContextEvent sce) {

        final String cronEndingMsg = Res.get(Res.CRON_TERMINATING);
        Log.info(cronEndingMsg);
        this.scheduler.shutdownNow();

        final String descroyedMsg = Res.get(Res.CONTEXT_DESTROYED);
        Log.info(descroyedMsg);
        this.installation = null;
    }
}
