package jwabbit.log;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * Extends {@code ObjLoggerBase} to keep track of a logging level and format different types of log entries. Calls to
 * log at a particular level will do nothing if the level is not included in the active log level set.
 */
class LeveledLogger extends ObjLoggerBase {

    /** Bit flag to enable all levels of logging. */
    static final int ALL = 0x00FF;

    /** Bit flag to enable warnings logging. */
    private static final int WARNING_BIT = 0x0002;

    /** Bit flag to enable informational logging. */
    private static final int INFO_BIT = 0x0004;

    /** Bit flag to enable fine message logging. */
    private static final int FINE_BIT = 0x0040;

    /** The log levels to include in the log output. */
    private int levels;

    /**
     * Constructs a new {@code LeveledLogger}.
     *
     * @param initialLevels the initial active log levels
     */
    LeveledLogger(final int initialLevels) {

        super();

        this.levels = initialLevels;
    }

    /**
     * Sets the set of levels which will be logged.
     *
     * @param newLevels the new set of levels - a logical OR of any combination of SEVERE, WARNING, INFO, CONFIG,
     *                  ENTERING, EXITING, and FINE
     */
    public final void setLevels(final int newLevels) {

        synchronized (getSynch()) {
            this.levels = newLevels;
        }
    }

    /**
     * Logs a message with severity 'W'.
     *
     * @param args the list of arguments that make up the log message
     */
    public final void warning(final Object... args) {

        synchronized (getSynch()) {
            if ((this.levels & WARNING_BIT) != 0) {
                log('W', args);
            }
        }
    }

    /**
     * Logs a message with severity 'I'.
     *
     * @param args the list of arguments that make up the log message
     */
    public final void info(final Object... args) {

        synchronized (getSynch()) {
            if ((this.levels & INFO_BIT) != 0) {
                log('I', args);
            }
        }
    }

    /**
     * Logs a message with no date, severity labeling, or source information.
     *
     * @param args the list of arguments that make up the log message
     */
    public final void fine(final Object... args) {

        synchronized (getSynch()) {
            if ((this.levels & FINE_BIT) != 0) {
                getWriter().writeMessage(listToString(args), true);
            }
        }
    }
}
