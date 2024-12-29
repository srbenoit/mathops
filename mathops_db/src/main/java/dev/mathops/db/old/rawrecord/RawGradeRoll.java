package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "grade_roll" record.
 */
public final class RawGradeRoll extends RawTermRecordBase implements Comparable<RawGradeRoll> {

    /** The table name. */
    public static final String TABLE_NAME = "grade_roll";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_SECT = "sect";

    /** A field name. */
    private static final String FLD_FULLNAME = "fullname";

    /** A field name. */
    private static final String FLD_GRADE_OPT = "grade_opt";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'course' field value. */
    public String course;

    /** The 'sect' field value. */
    public String sect;

    /** The 'fullname' field value. */
    public String fullname;

    /** The 'grade_opt' field value. */
    public String gradeOpt;

    /**
     * Constructs a new {@code RawGradeRoll}.
     */
    private RawGradeRoll() {

        super();
    }

    /**
     * Constructs a new {@code RawGradeRoll}.
     *
     * @param theTermKey  the term key
     * @param theStuId    The 'stu_id' field value
     * @param theCourse   the 'course' field value
     * @param theSect     the 'sect' field value
     * @param theFullname the 'fullname' field value
     * @param theGradeOpt the 'grade_opt' field value
     */
    public RawGradeRoll(final TermKey theTermKey, final String theStuId, final String theCourse,
                        final String theSect, final String theFullname, final String theGradeOpt) {

        super(theTermKey);

        this.stuId = theStuId;
        this.course = theCourse;
        this.sect = theSect;
        this.fullname = theFullname;
        this.gradeOpt = theGradeOpt;
    }

    /**
     * Extracts a "stcourse" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawGradeRoll fromResultSet(final ResultSet rs) throws SQLException {

        final RawGradeRoll result = new RawGradeRoll();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.course = getStringField(rs, FLD_COURSE);
        result.sect = getStringField(rs, FLD_SECT);
        result.fullname = getStringField(rs, FLD_FULLNAME);
        result.gradeOpt = getStringField(rs, FLD_GRADE_OPT);
        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);

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
    public int compareTo(final RawGradeRoll o) {

        int result = compareAllowingNull(this.termKey, o.termKey);

        if (result == 0) {
            result = compareAllowingNull(this.stuId, o.stuId);

            if (result == 0) {
                result = compareAllowingNull(this.course, o.course);

                if (result == 0) {
                    result = this.sect.compareTo(o.sect);
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
        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_SECT, this.sect);
        htm.add(DIVIDER);
        appendField(htm, FLD_FULLNAME, this.fullname);
        htm.add(DIVIDER);
        appendField(htm, FLD_GRADE_OPT, this.gradeOpt);

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
                + Objects.hashCode(this.sect)
                + Objects.hashCode(this.fullname)
                + Objects.hashCode(this.gradeOpt);
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
        } else if (obj instanceof final RawGradeRoll rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.sect, rec.sect)
                    && Objects.equals(this.fullname, rec.fullname)
                    && Objects.equals(this.gradeOpt, rec.gradeOpt);
        } else {
            equal = false;
        }

        return equal;
    }
}
