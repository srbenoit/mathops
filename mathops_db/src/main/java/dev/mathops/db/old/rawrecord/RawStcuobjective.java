package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "stcuobjective" record.
 */
public final class RawStcuobjective extends RecBase implements Comparable<RawStcuobjective> {

    /** The table name. */
    public static final String TABLE_NAME = "stcuobjective";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_UNIT = "unit";

    /** A field name. */
    private static final String FLD_OBJECTIVE = "objective";

    /** A field name. */
    private static final String FLD_LECTURE_VIEWED_DT = "lecture_viewed_dt";

    /** A field name. */
    private static final String FLD_SEED = "seed";

    /** A field name. */
    private static final String FLD_LAST_COMPONENT_FINISHED = "last_component_finished";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'course' field value. */
    public String course;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'objective' field value. */
    public Integer objective;

    /** The 'lecture_viewed_dt' field value. */
    public LocalDate lectureViewedDt;

    /** The 'seed' field value. */
    public Integer seed;

    /** The 'last_component_finished' field value. */
    public Integer lastComponentFinished;

    /**
     * Constructs a new {@code RawStcuobjective}.
     */
    private RawStcuobjective() {

        super();
    }

    /**
     * Constructs a new {@code RawStcuobjective}.
     *
     * @param theStuId                 The 'stu_id' field value
     * @param theCourse                the 'course' field value
     * @param theUnit                  the 'unit' field value
     * @param theObjective             the 'objective' field value
     * @param theLectureViewedDt       the 'lecture_viewed_dt' field value
     * @param theSeed                  the 'seed' field value
     * @param theLastComponentFinished the 'last_component_finished' field value
     */
    public RawStcuobjective(final String theStuId, final String theCourse, final Integer theUnit,
                            final Integer theObjective, final LocalDate theLectureViewedDt, final Integer theSeed,
                            final Integer theLastComponentFinished) {

        super();

        this.stuId = theStuId;
        this.course = theCourse;
        this.unit = theUnit;
        this.objective = theObjective;
        this.lectureViewedDt = theLectureViewedDt;
        this.seed = theSeed;
        this.lastComponentFinished = theLastComponentFinished;
    }

    /**
     * Extracts a "stcuobjective" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStcuobjective fromResultSet(final ResultSet rs) throws SQLException {

        final RawStcuobjective result = new RawStcuobjective();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.course = getStringField(rs, FLD_COURSE);
        result.unit = getIntegerField(rs, FLD_UNIT);
        result.objective = getIntegerField(rs, FLD_OBJECTIVE);
        result.lectureViewedDt = getDateField(rs, FLD_LECTURE_VIEWED_DT);
        result.seed = getIntegerField(rs, FLD_SEED);
        result.lastComponentFinished = getIntegerField(rs, FLD_LAST_COMPONENT_FINISHED);

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
    public int compareTo(final RawStcuobjective o) {

        int result = compareAllowingNull(this.stuId, o.stuId);

        if (result == 0) {
            result = compareAllowingNull(this.course, o.course);

            if (result == 0) {
                result = compareAllowingNull(this.unit, o.unit);

                if (result == 0) {
                    result = compareAllowingNull(this.objective, o.objective);
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
        appendField(htm, FLD_UNIT, this.unit);
        htm.add(DIVIDER);
        appendField(htm, FLD_OBJECTIVE, this.objective);
        htm.add(DIVIDER);
        appendField(htm, FLD_LECTURE_VIEWED_DT, this.lectureViewedDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_SEED, this.seed);
        htm.add(DIVIDER);
        appendField(htm, FLD_LAST_COMPONENT_FINISHED, this.lastComponentFinished);

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
                + Objects.hashCode(this.objective)
                + Objects.hashCode(this.lectureViewedDt)
                + Objects.hashCode(this.seed)
                + Objects.hashCode(this.lastComponentFinished);
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
        } else if (obj instanceof final RawStcuobjective rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.objective, rec.objective)
                    && Objects.equals(this.lectureViewedDt, rec.lectureViewedDt)
                    && Objects.equals(this.seed, rec.seed)
                    && Objects.equals(this.lastComponentFinished, rec.lastComponentFinished);
        } else {
            equal = false;
        }

        return equal;
    }
}
