package dev.mathops.db.old.logic;

import dev.mathops.commons.TemporalUtils;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A range of dates, sortable first by start date, then by end date.
 */
public final class DateRange implements Comparable<DateRange> {

    /** The start date. */
    public final LocalDate start;

    /** The end date. */
    public final LocalDate end;

    /**
     * Constructs a new {@code DateRange}.
     *
     * @param theStart the start date (null if none)
     * @param theEnd   the end date (null if none)
     */
    public DateRange(final LocalDate theStart, final LocalDate theEnd) {

        if (theStart != null && theEnd != null && theEnd.isBefore(theStart)) {
            throw new IllegalArgumentException(Res.get(Res.DAT_END_PRECEDES_START));
        }

        this.start = theStart;
        this.end = theEnd;
    }

    /**
     * Tests whether this range ends before a given date.
     *
     * @param date the date to test
     * @return true if this range ends before the given date
     */
    boolean endsBefore(final ChronoLocalDate date) {

        return this.end != null && this.end.isBefore(date);
    }

    /**
     * Tests whether a date is contained in this date range.
     *
     * @param date the date to test
     * @return true if the date falls within this range
     */
    public boolean contains(final ChronoLocalDate date) {

        return !endsBefore(date) && !startsAfter(date);
    }

    /**
     * Tests whether this range starts after a given date.
     *
     * @param date the date to test
     * @return true if this range starts after the given date
     */
    boolean startsAfter(final ChronoLocalDate date) {

        return this.start != null && this.start.isAfter(date);
    }

    /**
     * Computes a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.start) + Objects.hashCode(this.end);
    }

    /**
     * Tests whether this object is equal to another object. In order to be equal, {@code o} must be a {@code DateRange}
     * whose start and end dates are equal to those dates within this object.
     *
     * @param obj the object to test
     * @return true if this object is equal to {@code o}; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DateRange dr) {
            equal = Objects.equals(this.start, dr.start)
                    && Objects.equals(this.end, dr.end);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generates a string representation of the date range.
     */
    @Override
    public String toString() {

        final String result;

        if (this.start == null) {
            if (this.end == null) {
                result = "forever";
            } else {
                result = "until " + TemporalUtils.FMT_MDY.format(this.end);
            }
        } else if (this.end == null) {
            result = "starting " + TemporalUtils.FMT_MDY.format(this.start);
        } else if (this.start.equals(this.end)) {
            result = TemporalUtils.FMT_MDY.format(this.start);
        } else {
            result = TemporalUtils.FMT_MDY.format(this.start) + " through " + TemporalUtils.FMT_MDY.format(this.end);
        }

        return result;
    }

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     * @throws NullPointerException if the specified object is null
     */
    @Override
    public int compareTo(final DateRange o) {

        int result;

        if (this.start == null) {
            result = o.start == null ? 0 : -1;
        } else if (o.start == null) {
            result = 1;
        } else {
            result = this.start.compareTo(o.start);
        }

        if (result == 0) {
            if (this.end == null) {
                result = o.end == null ? 0 : -1;
            } else if (o.end == null) {
                result = 1;
            } else {
                result = this.end.compareTo(o.end);
            }
        }

        return result;
    }

    /**
     * Merges any overlapping ranges in a list of date ranges.
     *
     * @param ranges the list of ranges
     */
    static void mergeRanges(final List<DateRange> ranges) {

        if (!ranges.isEmpty()) {

            // Find and remove all that have a null start or end date and capture the latest
            // (anything before this) date and earliest (anything after this) date
            DateRange forever = null;
            LocalDate anyBefore = null;
            LocalDate anyAfter = null;

            final Iterator<DateRange> i1 = ranges.iterator();
            while (i1.hasNext()) {
                final DateRange rng = i1.next();
                if (rng.start == null) {
                    if (rng.end == null) {
                        forever = rng;
                        i1.remove();
                    } else if (anyBefore == null || rng.end.isAfter(anyBefore)) {
                        anyBefore = rng.end;
                        i1.remove();
                    }
                } else if (rng.end == null && (anyAfter == null || rng.start.isBefore(anyAfter))) {
                    anyAfter = rng.start;
                    i1.remove();
                }
            }

            if (forever == null) {
                Collections.sort(ranges);

                // If any ranges end before the "anyBefore" date, or start after the "anyAfter"
                // date, remove them.
                if (anyBefore != null || anyAfter != null) {
                    final Iterator<DateRange> i2 = ranges.iterator();
                    while (i1.hasNext()) {
                        final DateRange rng = i2.next();

                        boolean remove = anyBefore != null && !rng.end.isAfter(anyBefore);
                        if (anyAfter != null && !rng.start.isBefore(anyAfter)) {
                            remove = true;
                        }
                        if (remove) {
                            i2.remove();
                        }
                    }

                    // If any ranges overlap the "anyBefore" or "anyAfter" blocks, extend those
                    // blocks and remove those ranges. This operation needs to be repeated until no
                    // such overlap is found as the blocks can change as we go.
                    boolean working = true;
                    while (working) {
                        working = false;

                        final Iterator<DateRange> i3 = ranges.iterator();
                        while (i1.hasNext()) {
                            final DateRange rng = i3.next();

                            boolean remove = false;
                            if (anyBefore != null && !rng.start.isAfter(anyBefore)) {
                                anyBefore = rng.end;
                                working = true;
                                remove = true;
                            } else if (anyAfter != null && !rng.end.isBefore(anyAfter)) {
                                anyAfter = rng.start;
                                working = true;
                                remove = true;
                            }
                            if (remove) {
                                i3.remove();
                            }
                        }
                    }
                }

                if (anyBefore != null && anyAfter != null && !anyBefore.isBefore(anyAfter)) {
                    // Convert to single "forever" range
                    ranges.clear();
                    ranges.add(new DateRange(null, null));
                } else {
                    if (ranges.isEmpty()) {
                        if (anyBefore != null) {
                            ranges.add(new DateRange(null, anyBefore));
                        }
                    } else {
                        // What remains at this point are ranges with non-null start and end dates
                        // that do not overlap the "anyBefore" or "anyAfter" ranges.

                        // Scan through each index, merging each entry with the next until the next
                        // is not contiguous
                        int index = 0;
                        while (index < ranges.size() - 1) { // NOTE: ranges.size() can change within loop
                            final DateRange current = ranges.get(index);
                            final DateRange next = ranges.get(index + 1);

                            if (Objects.equals(current.start, next.start)) {
                                // Sort puts later end dates later, so keep 'next'
                                ranges.remove(index); // index is unchanged
                            } else {
                                // Current must start earlier
                                final LocalDate gap = current.end.plusDays(1L);

                                if (next.start.isAfter(gap)) {
                                    // Not contiguous - no merge needed
                                    ++index;
                                } else if (!current.end.isBefore(next.end)) {
                                    // Current ends on or after next ends, so eliminate 'next'
                                    ranges.remove(index + 1); // index is unchanged
                                } else {
                                    // Current starts earlier but Next ends later - merge
                                    ranges.remove(index + 1);
                                    ranges.remove(index);
                                    ranges.add(index, new DateRange(current.start, next.end));
                                }
                            }
                        }

                        // Add the "anyBefore" and "anyAfter" ranges (in order)
                        if (anyBefore != null) {
                            ranges.addFirst(new DateRange(null, anyBefore));
                        }
                    }
                    if (anyAfter != null) {
                        ranges.add(new DateRange(anyAfter, null));
                    }
                }
            } else {
                // There is at least one "forever" date range, so just use that
                ranges.clear();
                ranges.add(forever);
            }
        }
    }
}
