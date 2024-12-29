package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "cuobjective" record.
 */
public final class RawCuobjective extends RawTermRecordBase implements Comparable<RawCuobjective> {

    /** The table name. */
    public static final String TABLE_NAME = "cuobjective";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_UNIT = "unit";

    /** A field name. */
    private static final String FLD_OBJECTIVE = "objective";

    /** A field name. */
    private static final String FLD_LESSON_ID = "lesson_id";

    /** A field name. */
    private static final String FLD_LESSON_NBR = "lesson_nbr";

    /** A field name. */
    private static final String FLD_START_DT = "start_dt";

    /** The 'course' field value. */
    public String course;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'objective' field value. */
    public Integer objective;

    /** The 'lesson_id' field value. */
    public String lessonId;

    /** The 'lesson_nbr' field value. */
    public String lessonNbr;

    /** The 'start_dt' field value. */
    public LocalDate startDt;

    /**
     * Constructs a new {@code RawCuobjective}.
     */
    private RawCuobjective() {

        super();
    }

    /**
     * Constructs a new {@code RawCuobjective}.
     *
     * @param theTermKey   the term key
     * @param theCourse    the course
     * @param theUnit      the unit
     * @param theObjective the objective
     * @param theLessonId  the lesson ID
     * @param theLessonNbr the lesson number
     * @param theStartDt   the start date
     */
    public RawCuobjective(final TermKey theTermKey, final String theCourse, final Integer theUnit,
                          final Integer theObjective, final String theLessonId, final String theLessonNbr,
                          final LocalDate theStartDt) {

        super(theTermKey);

        this.course = theCourse;
        this.unit = theUnit;
        this.objective = theObjective;
        this.lessonId = theLessonId;
        this.lessonNbr = theLessonNbr;
        this.startDt = theStartDt;
    }

    /**
     * Extracts an "cuobjective" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawCuobjective fromResultSet(final ResultSet rs) throws SQLException {

        final RawCuobjective result = new RawCuobjective();

        result.course = getStringField(rs, FLD_COURSE);
        result.unit = getIntegerField(rs, FLD_UNIT);
        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.objective = getIntegerField(rs, FLD_OBJECTIVE);
        result.lessonId = getStringField(rs, FLD_LESSON_ID);
        result.lessonNbr = getStringField(rs, FLD_LESSON_NBR);
        result.startDt = getDateField(rs, FLD_START_DT);

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
    public int compareTo(final RawCuobjective o) {

        int result = this.termKey.compareTo(o.termKey);

        if (result == 0) {
            result = this.course.compareTo(o.course);
            if (result == 0) {
                result = this.unit.compareTo(o.unit);
                if (result == 0) {
                    result = this.objective.compareTo(o.objective);
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

        appendField(htm, FLD_TERM, this.termKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT, this.unit);
        htm.add(DIVIDER);
        appendField(htm, FLD_OBJECTIVE, this.objective);
        htm.add(DIVIDER);
        appendField(htm, FLD_LESSON_ID, this.lessonId);
        htm.add(DIVIDER);
        appendField(htm, FLD_LESSON_NBR, this.lessonNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_START_DT, this.startDt);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.termKey)
                + Objects.hashCode(this.course)
                + Objects.hashCode(this.unit)
                + Objects.hashCode(this.objective)
                + Objects.hashCode(this.lessonId)
                + Objects.hashCode(this.lessonNbr)
                + Objects.hashCode(this.startDt);
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
        } else if (obj instanceof final RawCuobjective rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.objective, rec.objective)
                    && Objects.equals(this.lessonId, rec.lessonId)
                    && Objects.equals(this.lessonNbr, rec.lessonNbr)
                    && Objects.equals(this.startDt, rec.startDt);
        } else {
            equal = false;
        }

        return equal;
    }
}
