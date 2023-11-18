package dev.mathops.core;

/**
 * The set of configurable system directory paths.
 */
public enum EPath {

    /** The base directory of an installation. */
    BASE_DIR("base-dir", "/opt/zircon"),

    /** The path where log files are written. */
    LOG_PATH("log-path", "{base-dir}/logs"),

    /** The path where exception logs are written. */
    EXCEPTION_PATH("exception-path", "{log-path}/exceptions"),

    /** The path where configuration data are stored. */
    CFG_PATH("cfg-path", "{base-dir}/cfg"),

    /** The path where database configurations are stored. */
    DB_PATH("db-path", "{base-dir}/db"),

    /** The path where current-term data is stored. */
    CUR_DATA_PATH("cur-data-path", "{base-dir}/data"),

    /** The first path at which to search for assessment and problem XML files. */
    SOURCE_1_PATH("source-path-1", "{base-dir}/data/instruction"),

    /** The second path at which to search for assessment and problem XML files. */
    SOURCE_2_PATH("source-path-2", null),

    /** The third path at which to search for assessment and problem XML files. */
    SOURCE_3_PATH("source-path-3", null),

    /** The path where HTML fragments are stored. */
    HTML_PATH("html-path", "{base-dir}/html");

    /** The key used to refer to the path. */
    public final String key;

    /** The default path, relative to the installation base directory. */
    public final String defaultPath;

    /**
     * Constructs a new {@code EPath}.
     *
     * @param theKey         the key used to refer to the path
     * @param theDefaultPath the default path, relative to the installation base directory
     */
    EPath(final String theKey, final String theDefaultPath) {

        this.key = theKey;
        this.defaultPath = theDefaultPath;
    }
}
