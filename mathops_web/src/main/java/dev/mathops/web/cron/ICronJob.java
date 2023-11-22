package dev.mathops.web.cron;

/**
 * A job that can be registered with the Cron service for periodic execution.
 */
@FunctionalInterface
public interface ICronJob {

    /**
     * Executes the job. Called every 10 seconds by the ScheduledExecutorService that is started by the context listener
     * when the servlet container starts (and stopped when the servlet container shuts down).
     *
     * <p>
     * This can serve as a heartbeat for processes that require periodic processing (like testing session timeouts or
     * sending push data on web sockets), or can be used by jobs to test whether the next run time has arrived, in which
     * case the job is executed.
     */
    void exec();
}
