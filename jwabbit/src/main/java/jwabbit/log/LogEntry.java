package jwabbit.log;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * An entry in the in-memory log.
 */
final class LogEntry {

    /** The log message. */
    private final String message;

    /**
     * Constructs a new {@code LogEntry}.
     *
     * @param logMsg the log message
     */
    LogEntry(final String logMsg) {

        this.message = logMsg;
    }

    /**
     * Gets the log message.
     *
     * @return the log message
     */
    public String getMessage() {

        return this.message;
    }
}
