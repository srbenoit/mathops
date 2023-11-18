package dev.mathops.core.log;

/**
 * The set of configurable settings for the logging system.
 */
enum ELogSetting {

    /** The set of log levels enabled. */
    LOG_LEVELS("log-levels", "ALL"),

    /** Boolean flag indicating log messages should be written to the console. */
    LOG_TO_CONSOLE("log-to-console", "true"),

    /** Boolean flag indicating log messages should be written to log files. */
    LOG_TO_FILES("log-to-files", "false"),

    /** Log file directory name (absolute if starts with '/', otherwise relative to base dir). */
    LOG_FILE_PATH("log-file-path", "logs"),

    /** Maximum number of log files to retain. */
    FILE_COUNT("log-file-count", Integer.toString(Integer.MAX_VALUE)),

    /** Approximate maximum size (in bytes) of each log file before rolling to the next. */
    FILE_SIZE_LIMIT("log-file-size-limit", Integer.toString(Integer.MAX_VALUE)),

    /** Base for log file names. */
    FILE_NAME_BASE("log-file-name-base", "zircon"),

    /** Boolean flag indicating log messages should append to existing file at startup. */
    FILE_APPEND("log-file-append", "true");

    /** The key used to refer to the path. */
    public final String key;

    /** The default value of the setting (could be null). */
    public final String defaultValue;

    /**
     * Constructs a new {@code ELogSetting}.
     *
     * @param theKey          the key used to refer to the path
     * @param theDefaultValue the default path, relative to the installation base directory
     */
    ELogSetting(final String theKey, final String theDefaultValue) {

        this.key = theKey;
        this.defaultValue = theDefaultValue;
    }
}
