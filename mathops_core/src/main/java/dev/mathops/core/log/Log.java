package dev.mathops.core.log;

/**
 * Singleton logger with static convenience methods that direct log messages with a specific log level to the singleton
 * instance. Calls to log at a particular level will do nothing if the level is not included in the active log level
 * set.
 */
public final class Log extends LogBase {

    /** Indentation for lines logged without date and severity. */
    private static final String INDENT = "                     ";

    /** The singleton instance. */
    private static final Log INSTANCE = new Log();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private Log() {

        super();
    }

    /**
     * Logs a message with severity 'S'.
     *
     * @param args the list of arguments that make up the log message
     */
    public static void severe(final Object... args) {

        synchronized (INSTANCE) {
            final LogSettings settings = INSTANCE.getSettings();
            if ((settings.getLogLevel() & SEVERE_BIT) != 0) {
                INSTANCE.log('S', args);
            }
        }
    }

    /**
     * Logs a message with severity 'W'.
     *
     * @param args the list of arguments that make up the log message
     */
    public static void warning(final Object... args) {

        synchronized (INSTANCE) {
            final LogSettings settings = INSTANCE.getSettings();
            if ((settings.getLogLevel() & WARNING_BIT) != 0) {
                INSTANCE.log('W', args);
            }
        }
    }

    /**
     * Logs a message with severity 'I'.
     *
     * @param args the list of arguments that make up the log message
     */
    public static void info(final Object... args) {

        synchronized (INSTANCE) {
            final LogSettings settings = INSTANCE.getSettings();
            if ((settings.getLogLevel() & INFO_BIT) != 0) {
                INSTANCE.log('I', args);
            }
        }
    }

    /**
     * Logs a message with severity 'C'.
     *
     * @param args the list of arguments that make up the log message
     */
    public static void config(final Object... args) {

        synchronized (INSTANCE) {
            final LogSettings settings = INSTANCE.getSettings();
            if ((settings.getLogLevel() & CONFIG_BIT) != 0) {
                INSTANCE.log('C', args);
            }
        }
    }

    /**
     * Logs a message with severity '&gt;'.
     *
     * @param args the list of arguments that make up the log message
     */
    public static void entering(final Object... args) {

        synchronized (INSTANCE) {
            final LogSettings settings = INSTANCE.getSettings();
            if ((settings.getLogLevel() & ENTERING_BIT) != 0) {
                INSTANCE.log('>', args);
            }
        }
    }

    /**
     * Logs a message with severity '&lt;'.
     *
     * @param args the list of arguments that make up the log message
     */
    public static void exiting(final Object... args) {

        synchronized (INSTANCE) {
            final LogSettings settings = INSTANCE.getSettings();
            if ((settings.getLogLevel() & EXITING_BIT) != 0) {
                INSTANCE.log('<', args);
            }
        }
    }

    /**
     * Logs a message with no date, severity labeling, or source information.
     *
     * @param args the list of arguments that make up the log message
     */
    public static void fine(final Object... args) {

        synchronized (INSTANCE) {
            if ((INSTANCE.getSettings().getLogLevel() & FINE_BIT) != 0) {
                final String msg = INSTANCE.listToString(args);
                INSTANCE.getLogWriter().writeMessage(msg, true);
            }
        }
    }

    /**
     * Logs a message with no date, severity labeling, or source information, but indented to align with messages that
     * have date and severity.
     *
     * @param args the list of arguments that make up the log message
     */
    public static void fineInd(final Object... args) {

        synchronized (INSTANCE) {
            if ((INSTANCE.getSettings().getLogLevel() & FINE_BIT) != 0) {
                final String msg = INSTANCE.listToString(INDENT, args);
                INSTANCE.getLogWriter().writeMessage(msg, true);
            }
        }
    }

    /**
     * Logs a message with no date, severity labeling, or source information and with no linefeeds. This is intended to
     * allow log messages to be built up over multiple calls - it uses the same "FINE" log level.
     *
     * @param args the list of arguments that make up the log message
     */
    public static void finer(final Object... args) {

        synchronized (INSTANCE) {
            if ((INSTANCE.getSettings().getLogLevel() & FINE_BIT) != 0) {
                final String msg = INSTANCE.listToString(args);
                INSTANCE.getLogWriter().writeMessage(msg, false);
            }
        }
    }

    /**
     * Logs a message with no date, severity labeling, or source information, that is logged only to the console and
     * with no linefeeds. This is intended to be used for advancing progress information like a line of dots to indicate
     * progress.
     *
     * @param args the list of arguments that make up the log message
     */
    public static void finest(final Object... args) {

        synchronized (INSTANCE) {
            if ((INSTANCE.getSettings().getLogLevel() & FINEST_BIT) != 0) {
                final String msg = INSTANCE.listToString(args);
                INSTANCE.getLogWriter().writeConsole(msg, false);
            }
        }
    }

    /**
     * Gets the {@code LogWriter} used by this logger.
     *
     * @return the {@code LogWriter}
     */
    public static LogWriter getWriter() {

        synchronized (INSTANCE) {
            return INSTANCE.getLogWriter();
        }
    }

    /**
     * Generates the string that simply lists error messages, without timestamps. Useful when messages are being
     * accumulated during a parse process and a list of build errors is to be reported at the end of the process.
     *
     * @return the string
     */
    public static String errorMessagesAsString() {

        final LogWriter logWriter = INSTANCE.getLogWriter();
        return logWriter.errorMessagesAsString();
    }
}
