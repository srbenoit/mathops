package dev.mathops.assessment;

/**
 * Parser modes.
 */
public enum EParserMode {

    /** Do not report any errors or warnings. */
    NO_WARNINGS(false, false),

    /** Normal mode - deprecated usages are reported. */
    NORMAL(true, true),

    /** Deprecated usages are silently allowed. */
    ALLOW_DEPRECATED(true, false);

    /** True if messages should be reported. */
    public final boolean reportAny;

    /** True if deprecated usages should be reported. */
    public final boolean reportDeprecated;

    /**
     * Constructs a new {@code EParserMode}.
     *
     * @param isReportAny        true if any messages should be reported
     * @param isReportDeprecated true if deprecated usages should be reported
     */
    EParserMode(final boolean isReportAny, final boolean isReportDeprecated) {

        this.reportAny = isReportAny;
        this.reportDeprecated = isReportDeprecated;
    }
}
