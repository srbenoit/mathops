package dev.mathops.core.log;

/**
 * An entry in the in-memory log.
 */
public final class LogEntry {

    /** The log message. */
    public final String message;

    /** The timestamp when the message was logged. */
    public final long millis;

    /**
     * Constructs a new {@code LogEntry}.
     *
     * @param logMsg the log message
     */
    LogEntry(final String logMsg) {

        this.message = logMsg;
        this.millis = System.currentTimeMillis();
    }
}
