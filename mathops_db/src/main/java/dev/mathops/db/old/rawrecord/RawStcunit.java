package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "stcunit" record.
 */
public final class RawStcunit extends RecBase implements Comparable<RawStcunit> {

    /** The table name. */
    public static final String TABLE_NAME = "stcunit";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_UNIT = "unit";

    /** A field name. */
    private static final String FLD_REVIEW_STATUS = "review_status";

    /** A field name. */
    private static final String FLD_REVIEW_SCORE = "review_score";

    /** A field name. */
    private static final String FLD_REVIEW_POINTS = "review_points";

    /** A field name. */
    private static final String FLD_PROCTORED_STATUS = "proctored_status";

    /** A field name. */
    private static final String FLD_PROCTORED_SCORE = "proctored_score";

    /** A field name. */
    private static final String FLD_PROCTORED_POINTS = "proctored_points";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'course' field value. */
    public String course;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'review_status' field value. */
    public String reviewStatus;

    /** The 'review_score' field value. */
    public Integer reviewScore;

    /** The 'review_points' field value. */
    public Integer reviewPoints;

    /** The 'proctored_status' field value. */
    public String proctoredStatus;

    /** The 'proctored_score' field value. */
    public Integer proctoredScore;

    /** The 'proctored_points' field value. */
    public Integer proctoredPoints;

    /**
     * Constructs a new {@code RawStcunit}.
     */
    private RawStcunit() {

        super();
    }

    /**
     * Constructs a new {@code RawStcunit}.
     *
     * @param theStuId           The 'stu_id' field value
     * @param theCourse          the 'course' field value
     * @param theUnit            the 'unit' field value
     * @param theReviewStatus    the 'review_status' field value
     * @param theReviewScore     the 'review_score' field value
     * @param theReviewPoints    the 'review_points' field value
     * @param theProctoredStatus the 'proctored_status' field value
     * @param theProctoredScore  the 'proctored_score' field value
     * @param theProctoredPoints the 'proctored_points' field value
     */
    public RawStcunit(final String theStuId, final String theCourse, final Integer theUnit,
                      final String theReviewStatus, final Integer theReviewScore, final Integer theReviewPoints,
                      final String theProctoredStatus, final Integer theProctoredScore,
                      final Integer theProctoredPoints) {

        super();

        this.stuId = theStuId;
        this.course = theCourse;
        this.unit = theUnit;
        this.reviewStatus = theReviewStatus;
        this.reviewScore = theReviewScore;
        this.reviewPoints = theReviewPoints;
        this.proctoredStatus = theProctoredStatus;
        this.proctoredScore = theProctoredScore;
        this.proctoredPoints = theProctoredPoints;
    }

    /**
     * Extracts an "stcunit" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStcunit fromResultSet(final ResultSet rs) throws SQLException {

        final RawStcunit result = new RawStcunit();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.course = getStringField(rs, FLD_COURSE);
        result.unit = getIntegerField(rs, FLD_UNIT);
        result.reviewStatus = getStringField(rs, FLD_REVIEW_STATUS);
        result.reviewScore = getIntegerField(rs, FLD_REVIEW_SCORE);
        result.reviewPoints = getIntegerField(rs, FLD_REVIEW_POINTS);
        result.proctoredStatus = getStringField(rs, FLD_PROCTORED_STATUS);
        result.proctoredScore = getIntegerField(rs, FLD_PROCTORED_SCORE);
        result.proctoredPoints = getIntegerField(rs, FLD_PROCTORED_POINTS);

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
    public int compareTo(final RawStcunit o) {

        int result = compareAllowingNull(this.stuId, o.stuId);

        if (result == 0) {
            result = compareAllowingNull(this.course, o.course);

            if (result == 0) {
                result = compareAllowingNull(this.unit, o.unit);
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
        appendField(htm, FLD_UNIT, this.unit);
        htm.add(DIVIDER);
        appendField(htm, FLD_REVIEW_STATUS, this.reviewStatus);
        htm.add(DIVIDER);
        appendField(htm, FLD_REVIEW_SCORE, this.reviewScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_REVIEW_POINTS, this.reviewPoints);
        htm.add(DIVIDER);
        appendField(htm, FLD_PROCTORED_STATUS, this.proctoredStatus);
        htm.add(DIVIDER);
        appendField(htm, FLD_PROCTORED_SCORE, this.proctoredScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_PROCTORED_POINTS, this.proctoredPoints);

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
                + Objects.hashCode(this.unit)
                + Objects.hashCode(this.reviewStatus)
                + Objects.hashCode(this.reviewScore)
                + Objects.hashCode(this.reviewPoints)
                + Objects.hashCode(this.proctoredStatus)
                + Objects.hashCode(this.proctoredScore)
                + Objects.hashCode(this.proctoredPoints);
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
        } else if (obj instanceof final RawStcunit rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.reviewStatus, rec.reviewStatus)
                    && Objects.equals(this.reviewScore, rec.reviewScore)
                    && Objects.equals(this.reviewPoints, rec.reviewPoints)
                    && Objects.equals(this.proctoredStatus, rec.proctoredStatus)
                    && Objects.equals(this.proctoredScore, rec.proctoredScore)
                    && Objects.equals(this.proctoredPoints, rec.proctoredPoints);
        } else {
            equal = false;
        }

        return equal;
    }
}
