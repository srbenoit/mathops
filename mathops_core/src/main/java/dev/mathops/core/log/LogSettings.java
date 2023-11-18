package dev.mathops.core.log;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Provides read/write access to the log configuration data.
 * <p>
 * NOTE: Cannot use a BLS Log object here since this object is created during its construction!
 */
public final class LogSettings {

    /** True string. */
    private static final String TRUE = "true";

    /** Characters valid in first position of filename base. */
    private static final String VALID_1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /** Characters valid in all but first position of filename base. */
    private static final String VALID_2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";

    /** Common string. */
    private static final String ALL = "ALL";

    /** Common string. */
    private static final String NONE = "NONE";

    /** The log level name. */
    private String logLevelName;

    /** The log level integer value. */
    private int logLevel;

    /** Flag indicating log records should be written to console. */
    private boolean logToConsole;

    /** Flag indicating log records should be written to log files. */
    private boolean logToFiles;

    /** The log file path. */
    private String logFilePath;

    /** The maximum number of log files to retain (minimum of 1). */
    private int logFileCount;

    /** An approximate upper limit on log file length. */
    private int logFileSizeLimit;

    /** The prefix for log file names. */
    private String filenameBase;

    /** Flag indicating log records should be appended to existing logs on startup. */
    private boolean append;

    /** A flag indicating at least one setting has changed since last load or save. */
    private boolean dirty;

    /**
     * Constructs a new {@code LogSettings}, which loads existing configuration data.
     */
    public LogSettings() {

        // Install all default values
        final Properties defaults = new Properties();
        final ELogSetting[] values = ELogSetting.values();
        for (final ELogSetting value : values) {
            defaults.setProperty(value.key, value.defaultValue);
        }

        configure(defaults);
    }

    /**
     * Constructs a new {@code LogSettings}, copying settings from an existing object (this can be used to store the
     * settings for later restoration).
     *
     * @param source the settings whose values to copy
     */
    public LogSettings(final LogSettings source) {

        setFrom(source);
    }

    /**
     * Sets the values of this object to those of another {@code LogSettings}.
     *
     * @param source the settings whose values to copy
     */
    public void setFrom(final LogSettings source) {

        this.logLevelName = source.logLevelName;
        this.logLevel = source.logLevel;
        this.logToConsole = source.logToConsole;
        this.logToFiles = source.logToFiles;
        this.logFilePath = source.logFilePath;
        this.logFileCount = source.logFileCount;
        this.logFileSizeLimit = source.logFileSizeLimit;
        this.filenameBase = source.filenameBase;
        this.append = source.append;
    }

    /**
     * Gets the log level name.
     *
     * @return the name
     */
    public String getLogLevelName() {

        return this.logLevelName;
    }

    /**
     * Gets the log level as an integer value (bitwise OR of constants from {@code LeveledLogger}).
     *
     * @return the log level
     */
    public int getLogLevel() {

        return this.logLevel;
    }

    /**
     * Sets the log levels.
     *
     * @param levels the log levels (bitwise OR of constants from {@code LeveledLogger}).
     */
    public void setLogLevels(final int levels) {

        final int masked = levels & LogBase.ALL;

        if (this.logLevel != masked) {
            this.logLevel = masked;
            if (masked == LogBase.ALL) {
                this.logLevelName = ALL;
            } else if (masked == LogBase.NONE) {
                this.logLevelName = NONE;
            } else {
                final HtmlBuilder htm = new HtmlBuilder(30);
                boolean comma = addToList(masked, LogBase.SEVERE_BIT, LogBase.SEVERE_LVL, false, htm);
                comma = addToList(masked, LogBase.WARNING_BIT, LogBase.WARNING_LVL, comma, htm);
                comma = addToList(masked, LogBase.INFO_BIT, LogBase.INFO_LVL, comma, htm);
                comma = addToList(masked, LogBase.CONFIG_BIT, LogBase.CONFIG_LVL, comma, htm);
                comma = addToList(masked, LogBase.ENTERING_BIT, LogBase.ENTERING_LVL, comma, htm);
                comma = addToList(masked, LogBase.EXITING_BIT, LogBase.EXITING_LVL, comma, htm);
                comma = addToList(masked, LogBase.FINE_BIT, LogBase.FINE_LVL, comma, htm);
                addToList(masked, LogBase.FINEST_BIT, LogBase.FINEST_LVL, comma, htm);

                this.logLevelName = htm.toString();
            }
            this.dirty = true;
        }
    }

    /**
     * Tests whether log records should be written to the console.
     *
     * @return {@code true} to write messages to console
     */
    public boolean isLogToConsole() {

        return this.logToConsole;
    }

    /**
     * Sets the flag that controls whether log records should be written to the console.
     *
     * @param isLogToConsole {@code true} to write messages to console
     */
    public void setLogToConsole(final boolean isLogToConsole) {

        if (this.logToConsole != isLogToConsole) {
            this.logToConsole = isLogToConsole;
            this.dirty = true;
        }
    }

    /**
     * Tests whether log records should be written to the log files.
     *
     * @return {@code true} to write messages to log files
     */
    public boolean isLogToFiles() {

        return this.logToFiles;
    }

    /**
     * Sets the flag that controls whether log records should be written to the log files.
     *
     * @param isLogToFiles {@code true} to write messages to log files
     */
    public void setLogToFiles(final boolean isLogToFiles) {

        if (this.logToFiles != isLogToFiles) {
            this.logToFiles = isLogToFiles;
            this.dirty = true;
        }
    }

    /**
     * Gets the path where log files are written. If the path starts with '/', '\\', or '[A-Z]:', it is assumed to be an
     * absolute path. Otherwise, it is assumed to be relative to the installation base directory.
     *
     * @return the log file path
     */
    public String getLogFilePath() {

        return this.logFilePath;
    }

    /**
     * Gets the maximum number of log file to retain.
     *
     * @return the maximum file count
     */
    public int getLogFileCount() {

        return this.logFileCount;
    }

    /**
     * Sets the maximum number of log file to retain.
     *
     * @param theCount the maximum file count
     */
    public void setLogFileCount(final int theCount) {

        if (theCount < 1) {
            final String msg = Res.get(Res.SETTINGS_BAD_FILE_COUNT);
            throw new IllegalArgumentException(msg);
        }

        if (this.logFileCount != theCount) {
            this.logFileCount = theCount;
            this.dirty = true;
        }
    }

    /**
     * Gets the approximate file size limit for log files.
     *
     * @return the file size limit
     */
    public int getLogFileSizeLimit() {

        return this.logFileSizeLimit;
    }

    /**
     * Sets the approximate file size limit for log files.
     *
     * @param theSizeLimit the file size limit
     */
    public void setLogFileSizeLimit(final int theSizeLimit) {

        if (theSizeLimit < 1) {
            final String msg = Res.fmt(Res.SETTINGS_BAD_FILE_SIZE);
            throw new IllegalArgumentException(msg);
        }

        if (this.logFileSizeLimit != theSizeLimit) {
            this.logFileSizeLimit = theSizeLimit;
            this.dirty = true;
        }
    }

    /**
     * Sets the flag that controls whether new log records should append to existing log file on restart, or rotate logs
     * and start a new log file on restart.
     *
     * @param theFilenameBase the new filename base
     * @throws IllegalArgumentException if the filename base is not legal
     */
    public void setFilenameBase(final String theFilenameBase) throws IllegalArgumentException {

        final int length = theFilenameBase.length();
        if (length == 0) {
            final String msg = Res.get(Res.EMPTY_FNAME);
            throw new IllegalArgumentException(msg);
        }

        final int char0 = theFilenameBase.charAt(0);
        if (VALID_1.indexOf(char0) == -1) {
            final String msg = Res.get(Res.FNAME_CHAR1);
            throw new IllegalArgumentException(msg);
        }

        for (int i = 1; i < length; ++i) {
            final char chr = theFilenameBase.charAt(i);

            if (VALID_2.indexOf(chr) == -1) {
                final String charStr = Character.toString(chr);
                final String msg = Res.fmt(Res.FNAME_CHAR1, charStr);
                throw new IllegalArgumentException(msg);
            }
        }

        if (!this.filenameBase.equals(theFilenameBase)) {
            this.filenameBase = theFilenameBase;
            this.dirty = true;
        }
    }

    /**
     * Gets the prefix for log filenames. For example, if the prefix is "abc", the current log file will be "abc.log",
     * and archival log files will be "abc_001.log" (the most recent), "abc_002.log", and so forth.
     *
     * @return the filename base
     */
    public String getFilenameBase() {

        return this.filenameBase;
    }

    /**
     * Sets the flag that controls whether new log records should append to existing log file on restart, or rotate logs
     * and start a new log file on restart.
     *
     * @param isAppend {@code true} to append rather than rotating log files
     */
    public void setAppend(final boolean isAppend) {

        if (this.append != isAppend) {
            this.append = isAppend;
            this.dirty = true;
        }
    }

    /**
     * Tests whether new log records should append to existing log file on restart, or rotate logs and start a new log
     * file on restart.
     *
     * @return {@code true} to append rather than rotating log files
     */
    public boolean isAppend() {

        return this.append;
    }

    /**
     * Sets the flag that indicates whether there are unsaved changes in the object's data.
     *
     * @param isDirty {@code true} to indicate this object has unsaved changes
     */
    void setDirty(final boolean isDirty) {

        this.dirty = isDirty;
    }

    /**
     * Tests whether there are unsaved changes in the object's data.
     *
     * @return {@code true} if there are unsaved changes
     */
    public boolean isDirty() {

        return this.dirty;
    }

    /**
     * Tests for the presence of a log level bit in a log level, and if found, adds the log level name to a
     * comma-separated list of levels.
     *
     * @param levels  the levels to test
     * @param bit     the bit to test
     * @param name    the name to append if the bit is set
     * @param comma   true to prepend a comma before the name
     * @param builder the {@code HtmlBuilder} to which to append
     * @return true (use as new comma value)
     */
    private static boolean addToList(final int levels, final int bit, final String name, final boolean comma,
                                     final HtmlBuilder builder) {

        boolean newComma = comma;
        if ((levels & bit) == bit) {
            if (comma) {
                builder.add(CoreConstants.COMMA_CHAR);
            }
            builder.add(name);
            newComma = true;
        }

        return newComma;
    }

    /**
     * Configures the object based on an {@code Properties} that may or may not have values for the keys defined in
     * {@code ELogSetting}.
     *
     * @param properties the properties from which to configure settings
     */
    void configure(final Properties properties) {

        this.logLevelName = getSetting(properties, ELogSetting.LOG_LEVELS);
        this.logLevel = parseLevels(this.logLevelName);

        final String logToConsoleSetting = getSetting(properties, ELogSetting.LOG_TO_CONSOLE);
        this.logToConsole = TRUE.equalsIgnoreCase(logToConsoleSetting);

        final String logToFilesSetting = getSetting(properties, ELogSetting.LOG_TO_FILES);
        this.logToFiles = logToFilesSetting == null || TRUE.equalsIgnoreCase(logToFilesSetting);

        final String countSetting = getSetting(properties, ELogSetting.FILE_COUNT);
        try {
            final int value = Integer.parseInt(countSetting);
            if (value < 1) {
                // Use Java logger since the "Log" object is not ready to use yet
                final String valueStr = Integer.toString(value);
                final String msg = Res.fmt(Res.SETTINGS_BAD_COUNT, valueStr);
                Logger.getAnonymousLogger().warning(msg);
                this.logFileCount = Integer.MAX_VALUE;
            } else {
                this.logFileCount = value;
            }
        } catch (final NumberFormatException ex) {
            // Use Java logger since the "Log" object is not ready to use yet
            final String msg = Res.fmt(Res.SETTINGS_PARSE_COUNT, countSetting);
            Logger.getAnonymousLogger().warning(msg);
            this.logFileCount = Integer.MAX_VALUE;
        }

        final String maxSize = getSetting(properties, ELogSetting.FILE_SIZE_LIMIT);
        try {
            final int value = Integer.parseInt(maxSize);
            if (value < 1) {
                // Use Java logger since the "Log" object is not ready to use yet
                final String valueStr = Integer.toString(value);
                final String msg = Res.fmt(Res.SETTINGS_BAD_SIZE, valueStr);
                Logger.getAnonymousLogger().warning(msg);
                this.logFileSizeLimit = Integer.MAX_VALUE;
            } else {
                this.logFileSizeLimit = value;
            }
        } catch (final NumberFormatException ex) {
            // Use Java logger since the "Log" object is not ready to use yet
            final String msg = Res.fmt(Res.SETTINGS_PARSE_SIZE, maxSize);
            Logger.getAnonymousLogger().warning(msg);
            this.logFileSizeLimit = Integer.MAX_VALUE;
        }

        this.filenameBase = getSetting(properties, ELogSetting.FILE_NAME_BASE);
        this.logFilePath = getSetting(properties, ELogSetting.LOG_FILE_PATH);

        final String fileAppendSetting = getSetting(properties, ELogSetting.FILE_APPEND);
        this.append = TRUE.equalsIgnoreCase(fileAppendSetting);
    }

    /**
     * Gets the property value corresponding to a log setting from a {@code Properties} object, or returns the setting's
     * default value if there was no property with the setting's key.
     *
     * @param properties the {@code Properties} object
     * @param setting    the setting to retrieve
     * @return the property value (if found) or the default value (if not found)
     */
    private static String getSetting(final Properties properties, final ELogSetting setting) {

        final String value = properties == null ? null : properties.getProperty(setting.key);

        return value == null ? setting.defaultValue : value;
    }

    /**
     * Parses the log levels from a {@code String}.
     *
     * @param str the string to parse
     * @return the log levels
     */
    private static int parseLevels(final String str) {

        int lvls;

        if (ALL.equalsIgnoreCase(str)) {
            lvls = LogBase.ALL;
        } else if (NONE.equalsIgnoreCase(str)) {
            lvls = LogBase.NONE;
        } else if (str == null) {
            lvls = LogBase.ALL;
        } else {
            final String[] items = str.split(CoreConstants.COMMA);
            lvls = LogBase.NONE;

            for (final String item : items) {
                final String trimmed = item.trim();
                lvls |= getLogLevel(trimmed);
            }
        }

        return lvls;
    }

    /**
     * Parses a log level label into the corresponding log level bit. An unrecognized label is treated as "ALL".
     *
     * @param test the label to test
     * @return the log level bit
     */
    private static int getLogLevel(final String test) {

        final int lvls;

        if (LogBase.SEVERE_LVL.equalsIgnoreCase(test)) {
            lvls = LogBase.SEVERE_BIT;
        } else if (LogBase.WARNING_LVL.equalsIgnoreCase(test)) {
            lvls = LogBase.WARNING_BIT;
        } else if (LogBase.INFO_LVL.equalsIgnoreCase(test)) {
            lvls = LogBase.INFO_BIT;
        } else if (LogBase.CONFIG_LVL.equalsIgnoreCase(test)) {
            lvls = LogBase.CONFIG_BIT;
        } else if (LogBase.ENTERING_LVL.equalsIgnoreCase(test)) {
            lvls = LogBase.ENTERING_BIT;
        } else if (LogBase.EXITING_LVL.equalsIgnoreCase(test)) {
            lvls = LogBase.EXITING_BIT;
        } else if (LogBase.FINE_LVL.equalsIgnoreCase(test)) {
            lvls = LogBase.FINE_BIT;
        } else if (LogBase.FINEST_LVL.equalsIgnoreCase(test)) {
            lvls = LogBase.FINEST_BIT;
        } else {
            final String msg = Res.fmt(Res.SETTINGS_BAD_LEVEL, test);
            Logger.getAnonymousLogger().warning(msg);
            lvls = LogBase.ALL;
        }

        return lvls;
    }
}
