package dev.mathops.web.site;

import dev.mathops.db.cfg.Profile;

import java.util.Map;
import java.util.TreeMap;

/**
 * A singleton, thread-safe timer that can record the number of accesses to any named context and the total time spent
 * processing that context. It tracks name-status pairs where the status is an access counter and total elapsed time
 * value.
 */
final class ServletTimer {

    /** The singleton instance. */
    private static final Object INSTANCE_SYNCH = new Object();

    /** The singleton instance. */
    private static ServletTimer instance;

    /** Object on which to synchronize access to map. */
    private final Object synch;

    /** Map from database profile to map from location name to counters. */
    private final Map<Profile, Map<String, ServletTimerCounters>> map;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private ServletTimer() {

        this.synch = new Object();
        this.map = new TreeMap<>();
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    static ServletTimer getInstance() {

        synchronized (INSTANCE_SYNCH) {
            if (instance == null) {
                instance = new ServletTimer();
            }

            return instance;
        }
    }

    /**
     * Records access to a location within a context.
     *
     * @param profile  the database profile under which the access was made
     * @param location the location being called
     * @param elapsed  the elapsed time
     */
    void recordAccess(final Profile profile, final String location, final long elapsed) {

        if (profile != null && location != null) {
            synchronized (this.synch) {
                final Map<String, ServletTimerCounters> submap = this.map.computeIfAbsent(profile,
                        s -> new TreeMap<>());

                final ServletTimerCounters counters = submap.computeIfAbsent(location, k -> new ServletTimerCounters());

                counters.add(elapsed);
            }
        }
    }
}
