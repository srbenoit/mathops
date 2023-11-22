package dev.mathops.web.cron;

import dev.mathops.core.CoreConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * A "cron"-like service that gets invoked by a {@code ScheduledExecutorService} every 10 seconds. This allows tasks to
 * register themselves with the "cron".
 */
public final class Cron implements Runnable {

    /** The single instance. */
    private static Cron instance;

    /** Registered jobs. */
    private final List<ICronJob> jobs;

    /**
     * Constructs a new {@code Cron}.
     */
    private Cron() {

        this.jobs = new ArrayList<>(10);

        // Log.info("Cron configured as of ", this.mostRecent);
    }

    /**
     * Retrieves the single instance, creating it if it has not yet been created.
     *
     * @return the instance
     */
    public static Cron getInstance() {

        synchronized (CoreConstants.INSTANCE_SYNCH) {

            if (instance == null) {
                instance = new Cron();
            }

            return instance;
        }
    }

    /**
     * Registers a job for periodic execution.
     *
     * @param job the job
     */
    public void registerJob(final ICronJob job) {

        synchronized (this.jobs) {
            this.jobs.add(job);
        }
    }

    /**
     * Called every 10 seconds by the {@code ScheduledExecutorService} started by the context listener when the servlet
     * container starts (and stopped when the servlet container shuts down).
     */
    @Override
    public void run() {

        synchronized (this.jobs) {
            for (final ICronJob job : this.jobs) {
                job.exec();
            }
        }
    }
}
