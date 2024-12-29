package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "except_stu" record.
 */
public final class RawExceptStu extends RawTermRecordBase implements Comparable<RawExceptStu> {

    /** The table name. */
    public static final String TABLE_NAME = "except_stu";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_UNIT = "unit";

    /** A field name. */
    private static final String FLD_COURSE_ENROLL = "course_enroll";

    /** A field name. */
    private static final String FLD_HWORK_STATUS = "hwork_status";

    /** A field name. */
    private static final String FLD_SECT = "sect";

    /** A field name. */
    private static final String FLD_SECT_ENROLL = "sect_enroll";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'course' field value. */
    public String course;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'course_enroll' field value. */
    public String courseEnroll;

    /** The 'hwork_status' field value. */
    public String hworkStatus;

    /** The 'sect' field value. */
    public String sect;

    /** The 'sect-enroll' field value. */
    public String sectEnroll;

    /**
     * Constructs a new {@code RawExceptStu}.
     */
    private RawExceptStu() {

        super();
    }

    /**
     * Constructs a new {@code RawExceptStu}.
     *
     * @param theTermKey         the term
     * @param theStuId        the student ID
     * @param theCourse       the course
     * @param theUnit         the unit
     * @param theCourseEnroll the course in which the student is enrolled
     * @param theHworkStatus  "Y" if student has to do homework in the course
     * @param theSect         the section
     * @param theSectEnroll   the section in which the student is enrolled
     */
    public RawExceptStu(final TermKey theTermKey, final String theStuId, final String theCourse,
                        final Integer theUnit, final String theCourseEnroll, final String theHworkStatus,
                        final String theSect, final String theSectEnroll) {

        super(theTermKey);

        this.stuId = theStuId;
        this.course = theCourse;
        this.unit = theUnit;
        this.courseEnroll = theCourseEnroll;
        this.hworkStatus = theHworkStatus;
        this.sect = theSect;
        this.sectEnroll = theSectEnroll;
    }

    /**
     * Extracts a "stcourse" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawExceptStu fromResultSet(final ResultSet rs) throws SQLException {

        final RawExceptStu result = new RawExceptStu();

        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.stuId = getStringField(rs, FLD_STU_ID);
        result.course = getStringField(rs, FLD_COURSE);
        result.unit = getIntegerField(rs, FLD_UNIT);
        result.courseEnroll = getStringField(rs, FLD_COURSE_ENROLL);
        result.hworkStatus = getStringField(rs, FLD_HWORK_STATUS);
        result.sect = getStringField(rs, FLD_SECT);
        result.sectEnroll = getStringField(rs, FLD_SECT_ENROLL);

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
    public int compareTo(final RawExceptStu o) {

        int result = compareAllowingNull(this.course, o.course);

        if (result == 0) {
            result = compareAllowingNull(this.unit, o.unit);
            if (result == 0) {
                result = compareAllowingNull(this.stuId, o.stuId);
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
        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT, this.unit);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE_ENROLL, this.courseEnroll);
        htm.add(DIVIDER);
        appendField(htm, FLD_HWORK_STATUS, this.hworkStatus);
        htm.add(DIVIDER);
        appendField(htm, FLD_SECT, this.sect);
        htm.add(DIVIDER);
        appendField(htm, FLD_SECT_ENROLL, this.sectEnroll);

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
                + Objects.hashCode(this.stuId)
                + Objects.hashCode(this.course)
                + Objects.hashCode(this.unit)
                + Objects.hashCode(this.courseEnroll)
                + Objects.hashCode(this.hworkStatus)
                + Objects.hashCode(this.sect)
                + Objects.hashCode(this.sectEnroll);
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
        } else if (obj instanceof final RawExceptStu rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.courseEnroll, rec.courseEnroll)
                    && Objects.equals(this.hworkStatus, rec.hworkStatus)
                    && Objects.equals(this.sect, rec.sect)
                    && Objects.equals(this.sectEnroll, rec.sectEnroll);
        } else {
            equal = false;
        }

        return equal;
    }
}
