package dev.mathops.db.rec;

import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A "student unit mastery" record.
 */
public final class StuUnitMasteryRec extends RecBase implements Comparable<StuUnitMasteryRec> {

    /** The table name. */
    public static final String TABLE_NAME = "stu_unit_mastery";

    /** A field name for serialization of records. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name for serialization of records. */
    private static final String FLD_COURSE_ID = "course_id";

    /** A field name for serialization of records. */
    private static final String FLD_UNIT = "unit";

    /** A field name for serialization of records. */
    private static final String FLD_SCORE = "score";

    /** A field name for serialization of records. */
    private static final String FLD_SR_STATUS = "sr_status";

    /** A field name for serialization of records. */
    private static final String FLD_S1_STATUS = "s1_status";

    /** A field name for serialization of records. */
    private static final String FLD_S2_STATUS = "s2_status";

    /** A field name for serialization of records. */
    private static final String FLD_S3_STATUS = "s3_status";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'course_id' field value. */
    public String courseId;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'score' field value. */
    public Integer score;

    /** The 'sr_status' field value. */
    public String srStatus;

    /** The 's1_status' field value. */
    public String s1Status;

    /** The 's2_status' field value. */
    public String s2Status;

    /** The 's3_status' field value. */
    public String s3Status;

    /**
     * Constructs a new {@code StudentUnitMasteryRec}.
     */
    public StuUnitMasteryRec() {

        super();
    }

    /**
     * Constructs a new {@code StudentUnitMasteryRec}.
     *
     * @param theStuId    the student ID
     * @param theCourseId the course ID
     * @param theUnit     the unit
     * @param theScore    the total score
     * @param theSrStatus the status of the Skills Review
     * @param theS1Status the status of standard 1
     * @param theS2Status the status of standard 2
     * @param theS3Status the status of standard 3
     */
    public StuUnitMasteryRec(final String theStuId, final String theCourseId,
                             final Integer theUnit, final Integer theScore, final String theSrStatus,
                             final String theS1Status, final String theS2Status, final String theS3Status) {

        super();

        this.stuId = theStuId;
        this.courseId = theCourseId;
        this.unit = theUnit;
        this.score = theScore;
        this.srStatus = theSrStatus;
        this.s1Status = theS1Status;
        this.s2Status = theS2Status;
        this.s3Status = theS3Status;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StuUnitMasteryRec o) {

        int result = compareAllowingNull(this.stuId, o.stuId);

        if (result == 0) {
            result = compareAllowingNull(this.courseId, o.courseId);

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
        appendField(htm, FLD_COURSE_ID, this.courseId);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT, this.unit);
        htm.add(DIVIDER);
        appendField(htm, FLD_SCORE, this.score);
        htm.add(DIVIDER);
        appendField(htm, FLD_SR_STATUS, this.srStatus);
        htm.add(DIVIDER);
        appendField(htm, FLD_S1_STATUS, this.s1Status);
        htm.add(DIVIDER);
        appendField(htm, FLD_S2_STATUS, this.s2Status);
        htm.add(DIVIDER);
        appendField(htm, FLD_S3_STATUS, this.s3Status);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.stuId) //
                + Objects.hashCode(this.courseId) //
                + Objects.hashCode(this.unit) //
                + Objects.hashCode(this.score) //
                + Objects.hashCode(this.srStatus) //
                + Objects.hashCode(this.s1Status) //
                + Objects.hashCode(this.s2Status) //
                + Objects.hashCode(this.s3Status);
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
        } else if (obj instanceof final StuUnitMasteryRec rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.courseId, rec.courseId)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.score, rec.score)
                    && Objects.equals(this.srStatus, rec.srStatus)
                    && Objects.equals(this.s1Status, rec.s1Status)
                    && Objects.equals(this.s2Status, rec.s2Status)
                    && Objects.equals(this.s3Status, rec.s3Status);
        } else {
            equal = false;
        }

        return equal;
    }
}
