package dev.mathops.web.site;

/**
 * Counters for total accesses and duration.
 */
final class ServletTimerCounters {

    /** The number of accesses. */
    private int accessCount;

    /** The total elapsed time. */
    private long totalElapsed;

    /** The longest elapsed time. */
    private long longest;

    /**
     * Constructs a new {@code Counters}.
     */
    ServletTimerCounters() {

        this.accessCount = 0;
        this.totalElapsed = 0L;
        this.longest = 0L;
    }

    /**
     * Constructs a new {@code Counters}.
     *
     * @param theAccessCount  the access count
     * @param theTotalElapsed the total elapsed time
     * @param theLongest      the longest time for one transaction
     */
    private ServletTimerCounters(final int theAccessCount, final long theTotalElapsed,
                                 final long theLongest) {

        this.accessCount = theAccessCount;
        this.totalElapsed = theTotalElapsed;
        this.longest = theLongest;
    }

    /**
     * Adds one access count and a given elapsed time to a set of counters.
     *
     * @param elapsed the elapsed time
     */
    void add(final long elapsed) {

        ++this.accessCount;
        this.totalElapsed += elapsed;
        this.longest = Math.max(this.longest, elapsed);
    }

    /**
     * Returns a copy of this object.
     *
     * @return the copy
     */
    ServletTimerCounters copy() {

        return new ServletTimerCounters(this.accessCount, this.totalElapsed, this.longest);
    }
}
