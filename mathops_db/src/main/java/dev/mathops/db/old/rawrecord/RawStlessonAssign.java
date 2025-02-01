package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A raw "stlesson_assign" record.
 */
public final class RawStlessonAssign extends RecBase implements Comparable<RawStlessonAssign> {

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_LESSON_ID = "lesson_id";

    /** A field name. */
    private static final String FLD_WHEN_SHOWN = "when_shown";

    /** A field name. */
    private static final String FLD_WHEN_OPEN = "when_open";

    /** A field name. */
    private static final String FLD_WHEN_CLOSED = "when_closed";

    /** A field name. */
    private static final String FLD_WHEN_HIDDEN = "when_hidden";

    /** A field name. */
    private static final String FLD_WHEN_STARTED = "when_started";

    /** A field name. */
    private static final String FLD_WHEN_FINISHED = "when_finished";

    /** A field name. */
    private static final String FLD_SCORE_TENTHS = "score_tenths";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'course' field value. */
    public String course;

    /** The 'lesson_id' field value. */
    public String lessonId;

    /** The 'when_shown' field value. */
    public LocalDateTime whenShown;

    /** The 'when_open' field value. */
    public LocalDateTime whenOpen;

    /** The 'when_closed' field value. */
    public LocalDateTime whenClosed;

    /** The 'when_hidden' field value. */
    public LocalDateTime whenHidden;

    /** The 'when_started' field value (null until started). */
    public LocalDateTime whenStarted;

    /** The 'when_finished' field value (null until finished). */
    public LocalDateTime whenFinished;

    /** The 'score' field value, in tenths of a point (null until finished). */
    public Integer scoreTenths;

    /**
     * Constructs a new {@code RawStlessonAssign}.
     */
    private RawStlessonAssign() {

        super();
    }

    /**
     * Constructs a new {@code RawStlessonAssign}.
     *
     * @param theStuId        the student ID
     * @param theCourseId     the course ID
     * @param theLessonId     the lesson ID
     * @param theWhenShown    the date/time the lesson becomes visible for preview access (often the first day of a
     *                        term)
     * @param theWhenOpen     the date/time the lesson becomes open and fully accessible
     * @param theWhenClosed   the date/time the lesson closes and can no longer be worked on
     * @param theWhenHidden   the date/time the lesson ceases to be visible for review (often the last day of the term)
     * @param theWhenStarted  the date/time the student started working on the lesson
     * @param theWhenFinished the date/time the student finished the lesson
     * @param theScoreTenths  the score, in tenths of a point
     */
    public RawStlessonAssign(final String theStuId, final String theCourseId,
                             final String theLessonId, final LocalDateTime theWhenShown,
                             final LocalDateTime theWhenOpen,
                             final LocalDateTime theWhenClosed, final LocalDateTime theWhenHidden,
                             final LocalDateTime theWhenStarted, final LocalDateTime theWhenFinished,
                             final Integer theScoreTenths) {

        super();

        this.stuId = theStuId;
        this.course = theCourseId;
        this.lessonId = theLessonId;
        this.whenShown = theWhenShown;
        this.whenOpen = theWhenOpen;
        this.whenClosed = theWhenClosed;
        this.whenHidden = theWhenHidden;
        this.whenStarted = theWhenStarted;
        this.whenFinished = theWhenFinished;
        this.scoreTenths = theScoreTenths;
    }

    /**
     * Extracts an "RawStlessonAssign" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStlessonAssign fromResultSet(final ResultSet rs) throws SQLException {

        final RawStlessonAssign result = new RawStlessonAssign();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.course = getStringField(rs, FLD_COURSE);
        result.lessonId = getStringField(rs, FLD_LESSON_ID);
        result.whenShown = getDateTimeField(rs, FLD_WHEN_SHOWN);
        result.whenOpen = getDateTimeField(rs, FLD_WHEN_OPEN);
        result.whenClosed = getDateTimeField(rs, FLD_WHEN_CLOSED);
        result.whenHidden = getDateTimeField(rs, FLD_WHEN_HIDDEN);
        result.whenStarted = getDateTimeField(rs, FLD_WHEN_STARTED);
        result.whenFinished = getDateTimeField(rs, FLD_WHEN_FINISHED);
        result.scoreTenths = getIntegerField(rs, FLD_SCORE_TENTHS);

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
    public int compareTo(final RawStlessonAssign o) {

        int result = compareAllowingNull(this.course, o.course);

        if (result == 0) {
            result = compareAllowingNull(this.whenOpen, o.whenOpen);
            if (result == 0) {
                result = compareAllowingNull(this.whenClosed, o.whenClosed);
                if (result == 0) {
                    result = compareAllowingNull(this.whenShown, o.whenShown);
                    if (result == 0) {
                        result = compareAllowingNull(this.lessonId, o.lessonId);
                        if (result == 0) {
                            result = compareAllowingNull(this.stuId, o.stuId);
                        }
                    }
                }
            }
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
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_LESSON_ID, this.lessonId);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_SHOWN, this.whenShown);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_OPEN, this.whenOpen);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_CLOSED, this.whenClosed);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_HIDDEN, this.whenHidden);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_STARTED, this.whenStarted);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_FINISHED, this.whenFinished);
        htm.add(DIVIDER);
        appendField(htm, FLD_SCORE_TENTHS, this.scoreTenths);

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
                + Objects.hashCode(this.course)
                + Objects.hashCode(this.lessonId)
                + Objects.hashCode(this.whenShown)
                + Objects.hashCode(this.whenOpen)
                + Objects.hashCode(this.whenClosed)
                + Objects.hashCode(this.whenHidden)
                + Objects.hashCode(this.whenStarted)
                + Objects.hashCode(this.whenFinished)
                + Objects.hashCode(this.scoreTenths);
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
        } else if (obj instanceof final RawStlessonAssign rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.lessonId, rec.lessonId)
                    && Objects.equals(this.whenShown, rec.whenShown)
                    && Objects.equals(this.whenOpen, rec.whenOpen)
                    && Objects.equals(this.whenClosed, rec.whenClosed)
                    && Objects.equals(this.whenHidden, rec.whenHidden)
                    && Objects.equals(this.whenStarted, rec.whenStarted)
                    && Objects.equals(this.whenFinished, rec.whenFinished)
                    && Objects.equals(this.scoreTenths, rec.scoreTenths);
        } else {
            equal = false;
        }

        return equal;
    }
}
