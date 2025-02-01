package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A raw "stvisit" record.
 */
public final class RawStvisit extends RecBase implements Comparable<RawStvisit> {

    /** The table name. */
    public static final String TABLE_NAME = "stvisit";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_WHEN_STARTED = "when_started";

    /** A field name. */
    private static final String FLD_WHEN_ENDED = "when_ended";

    /** A field name. */
    private static final String FLD_LOCATION = "location";

    /** A field name. */
    private static final String FLD_SEAT = "seat";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'when_started' field value. */
    public LocalDateTime whenStarted;

    /** The 'when_ended' field value. */
    public LocalDateTime whenEnded;

    /** The 'location' field value. */
    public String location;

    /** The 'seat' field value. */
    public String seat;

    /**
     * Constructs a new {@code RawStvisit}.
     */
    private RawStvisit() {

        super();
    }

    /**
     * Constructs a new {@code RawStvisit}.
     *
     * @param theStuId       the student ID
     * @param theWhenStarted the date/time the visit started
     * @param theWhenEnded   the date/time the visit ended
     * @param theLocation    the pace track
     * @param theSeat        the assigned cohort
     */
    public RawStvisit(final String theStuId, final LocalDateTime theWhenStarted,
                      final LocalDateTime theWhenEnded, final String theLocation, final String theSeat) {

        super();

        this.stuId = theStuId;
        this.whenStarted = theWhenStarted;
        this.whenEnded = theWhenEnded;
        this.location = theLocation;
        this.seat = theSeat;
    }

    /**
     * Extracts a "stvisit" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStvisit fromResultSet(final ResultSet rs) throws SQLException {

        final RawStvisit result = new RawStvisit();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.whenStarted = getDateTimeField(rs, FLD_WHEN_STARTED);
        result.whenEnded = getDateTimeField(rs, FLD_WHEN_ENDED);
        result.location = getStringField(rs, FLD_LOCATION);
        result.seat = getStringField(rs, FLD_SEAT);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawStvisit o) {

        int result = this.stuId.compareTo(o.stuId);

        if (result == 0) {
            result = this.whenStarted.compareTo(o.whenStarted);
        }

        return result;
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(40);

        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_STARTED, this.whenStarted);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_ENDED, this.whenEnded);
        htm.add(DIVIDER);
        appendField(htm, FLD_LOCATION, this.location);
        htm.add(DIVIDER);
        appendField(htm, FLD_SEAT, this.seat);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.stuId)
                + Objects.hashCode(this.whenStarted)
                + Objects.hashCode(this.whenEnded)
                + Objects.hashCode(this.location)
                + Objects.hashCode(this.seat);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final RawStvisit rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.whenStarted, rec.whenStarted)
                    && Objects.equals(this.whenEnded, rec.whenEnded)
                    && Objects.equals(this.location, rec.location)
                    && Objects.equals(this.seat, rec.seat);
        } else {
            equal = false;
        }

        return equal;
    }
}
