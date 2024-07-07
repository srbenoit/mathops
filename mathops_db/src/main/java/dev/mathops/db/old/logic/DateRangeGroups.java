package dev.mathops.db.old.logic;

import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A container for an immutable sequence of non-overlapping date ranges, some of which are in the past, zero or one that
 * is currently active, and some that are in the future.
 */
public final class DateRangeGroups {

    /** The current date range (null if none). */
    public final DateRange current;

    /** Ranges in the future+(sorted). */
    public final List<DateRange> future;

    /** Ranges in the past (sorted). */
    public final List<DateRange> past;

    /**
     * Constructs a new {@code DateRanges}.
     *
     * @param theRanges the list of ranges to categorize (does not need to be sorted)
     * @param today     the current date
     */
    DateRangeGroups(final Collection<DateRange> theRanges, final ChronoLocalDate today) {

        final int numRanges = theRanges.size();
        final List<DateRange> p = new ArrayList<>(numRanges);
        final List<DateRange> f = new ArrayList<>(numRanges);

        DateRange c = null;
        for (final DateRange test : theRanges) {
            if (test.endsBefore(today)) {
                p.add(test);
            } else if (test.startsAfter(today)) {
                f.add(test);
            } else {
                c = test;
            }
        }

        Collections.sort(p);
        Collections.sort(f);

        this.past = Collections.unmodifiableList(p);
        this.current = c;
        this.future = Collections.unmodifiableList(f);
    }

    /**
     * Gets the most recent past date range (if any).
     *
     * @return the most recent past date range; {@code null} if none
     */
    public DateRange getMostRecentPast() {

        return this.past.isEmpty() ? null : this.past.getLast();
    }

    /**
     * Gets the future date ranges.
     *
     * @return the future ranges (sorted)
     */
    public List<DateRange> getAll() {

        final List<DateRange> all = new ArrayList<>(this.past.size() + 1 + this.future.size());

        all.addAll(this.past);
        if (this.current != null) {
            all.add(this.current);
        }
        all.addAll(this.future);

        return all;
    }

    /**
     * Tests whether this container has either a current or future range.
     *
     * @return true if a current or future range exists
     */
    public boolean hasCurrentOrFuture() {

        return this.current != null || !this.future.isEmpty();
    }
}
