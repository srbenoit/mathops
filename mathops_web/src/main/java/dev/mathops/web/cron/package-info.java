/**
 * A cron-like service that calls a list of registered jobs once every 10 seconds while the web server is running. Those
 * jobs can use this heartbeat to test whether they are due to execute some long-running task.
 */
package dev.mathops.web.cron;
