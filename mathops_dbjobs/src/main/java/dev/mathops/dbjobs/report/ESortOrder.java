package dev.mathops.dbjobs.report;

/**
 * Sort orders for reports.
 */
public enum ESortOrder {

    /** Sorted by last name. */
    LAST_NAME("lastname"),

    /** Sorted by CSU ID. */
    CSUID("csiud"),

    /** Preserve the order of provided data. */
    PRESERVE_ORDER("preserve");

    /** The report ID. */
    public final String id;

    /**
     * Constructs a new {@code ESortOrder}.
     *
     * @param theId the ID
     */
    ESortOrder(final String theId) {

        this.id = theId;
    }

    /**
     * Gets the {@code ESortOrder} that has a specified ID.
     *
     * @param theId the ID
     * @return the corresponding {@code ESortOrder}; {@code null} if none have the specified ID
     */
    public static ESortOrder forId(final String theId) {

        ESortOrder found = null;

        for (final ESortOrder test : values()) {
            if (test.id.equals(theId)) {
                found = test;
                break;
            }
        }

        return found;
    }
}
