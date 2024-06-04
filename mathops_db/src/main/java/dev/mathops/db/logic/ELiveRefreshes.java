package dev.mathops.db.logic;

/**
 * Possible settings for doing live refreshes of data.
 */
public enum ELiveRefreshes {

    /** Refresh all data from live system. */
    ALL,

    /** Query the live system only if there is no local record. */
    IF_MISSING,

    /** Do not query the live system. */
    NONE,
}
