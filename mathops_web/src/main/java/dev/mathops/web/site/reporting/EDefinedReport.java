package dev.mathops.web.site.reporting;

/**
 * The set of defined reports.
 */
public enum EDefinedReport {

    /** Math Placement status by student category. */
    MPT_BY_CATEGORY("MptByCat"),

    /** Math Placement status by student ID list. */
    MPT_BY_IDS("MptByIDs"),

    /** Precalculus Course status by section. */
    PROGRESS_BY_SECTION("ProgBySec"),

    /** Precalculus Course status by student ID list. */
    PROGRESS_BY_IDS("ProgByIDs");

    /** The report ID. */
    public final String id;

    /**
     * Constructs a new {@code EDefinedReport}.
     *
     * @param theId the ID
     */
    EDefinedReport(final String theId) {

        this.id = theId;
    }

    /**
     * Gets the {@code EDefinedReport} that has a specified ID.
     *
     * @param theId the ID
     * @return the corresponding {@code EDefinedReport}; {@code null} if none have the specified ID
     */
    public static EDefinedReport forId(final String theId) {

        EDefinedReport found = null;

        for (final EDefinedReport test : values()) {
            if (test.id.equals(theId)) {
                found = test;
                break;
            }
        }

        return found;
    }
}
