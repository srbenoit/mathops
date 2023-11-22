package dev.mathops.assessment;

/**
 * Parser modes.
 */
public enum EParserMode {

    /** Normal mode - deprecated usages are reported. */
    NORMAL(true),

    /** Deprecated usages are silently allowed. */
    ALLOW_DEPRECATED(false);

    /** True if deprecated usages should be reported. */
    public final boolean reportDeprecated;

    /**
     * Constructs a new {@code EParserMode}.
     *
     * @param isReportDeprecated true if deprecated usages should be reported
     */
    EParserMode(final boolean isReportDeprecated) {

        this.reportDeprecated = isReportDeprecated;
    }
}
