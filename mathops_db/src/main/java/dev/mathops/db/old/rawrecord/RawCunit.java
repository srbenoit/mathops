package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "cunit" record.
 */
public final class RawCunit extends RawTermRecordBase implements Comparable<RawCunit> {

    /** The table name. */
    public static final String TABLE_NAME = "cunit";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_UNIT = "unit";

    /** A field name. */
    private static final String FLD_UNIT_EXAM_WGT = "unit_exam_wgt";

    /** A field name. */
    private static final String FLD_UNIT_DESC = "unit_desc";

    /** A field name. */
    private static final String FLD_UNIT_TIMELIMIT = "unit_timelimit";

    /** A field name. */
    private static final String FLD_POSSIBLE_SCORE = "possible_score";

    /** A field name. */
    private static final String FLD_NBR_QUESTIONS = "nbr_questions";

    /** A field name. */
    private static final String FLD_UNIT_TYPE = "unit_type";

    /** The 'course' field value. */
    public String course;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'unit_exam_wgt' field value. */
    public Float unitExamWgt;

    /** The 'unit_desc' field value. */
    public String unitDesc;

    /** The 'unit_timelimit' field value. */
    public Integer unitTimelimit;

    /** The 'possible_score' field value. */
    public Integer possibleScore;

    /** The 'nbr_questions' field value. */
    public Integer nbrQuestions;

    /** The 'unit_type' field value. */
    public String unitType;

    /**
     * Constructs a new {@code RawCunit}.
     */
    private RawCunit() {

        super();
    }

    /**
     * Constructs a new {@code RawCunit}.
     *
     * @param theTermKey       the term key
     * @param theCourse        the course
     * @param theUnit          the sect
     * @param theUnitExamWgt   the unit exam weight
     * @param theUnitDesc      the unit description
     * @param theUnitTimelimit the unit exam time limit
     * @param thePossibleScore the unit exam possible score
     * @param theNbrQuestions  the number of questions on the unit exam
     * @param theUnitType      the unit type ("SR", "INST", "EXAM", or "FIN")
     */
    public RawCunit(final TermKey theTermKey, final String theCourse, final Integer theUnit,
                    final Float theUnitExamWgt, final String theUnitDesc, final Integer theUnitTimelimit,
                    final Integer thePossibleScore, final Integer theNbrQuestions, final String theUnitType) {

        super(theTermKey);

        this.course = theCourse;
        this.unit = theUnit;
        this.unitExamWgt = theUnitExamWgt;
        this.unitDesc = theUnitDesc;
        this.unitTimelimit = theUnitTimelimit;
        this.possibleScore = thePossibleScore;
        this.nbrQuestions = theNbrQuestions;
        this.unitType = theUnitType;
    }

    /**
     * Extracts an "cunit" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawCunit fromResultSet(final ResultSet rs) throws SQLException {

        final RawCunit result = new RawCunit();

        result.course = getStringField(rs, FLD_COURSE);
        result.unit = getIntegerField(rs, FLD_UNIT);
        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.unitExamWgt = getFloatField(rs, FLD_UNIT_EXAM_WGT);
        result.unitDesc = getStringField(rs, FLD_UNIT_DESC);
        result.unitTimelimit = getIntegerField(rs, FLD_UNIT_TIMELIMIT);
        result.possibleScore = getIntegerField(rs, FLD_POSSIBLE_SCORE);
        result.nbrQuestions = getIntegerField(rs, FLD_NBR_QUESTIONS);
        result.unitType = getStringField(rs, FLD_UNIT_TYPE);

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
    public int compareTo(final RawCunit o) {

        int result = this.termKey.compareTo(o.termKey);

        if (result == 0) {
            result = this.course.compareTo(o.course);
            if (result == 0) {
                result = this.unit.compareTo(o.unit);
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
        appendField(htm, FLD_UNIT_EXAM_WGT, this.unitExamWgt);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT_DESC, this.unitDesc);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT_TIMELIMIT, this.unitTimelimit);
        htm.add(DIVIDER);
        appendField(htm, FLD_POSSIBLE_SCORE, this.possibleScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_QUESTIONS, this.nbrQuestions);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT_TYPE, this.unitType);

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
                + Objects.hashCode(this.unitExamWgt)
                + Objects.hashCode(this.unitDesc)
                + Objects.hashCode(this.unitTimelimit)
                + Objects.hashCode(this.possibleScore)
                + Objects.hashCode(this.nbrQuestions)
                + Objects.hashCode(this.unitType);
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
        } else if (obj instanceof final RawCunit rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.unitExamWgt, rec.unitExamWgt)
                    && Objects.equals(this.unitDesc, rec.unitDesc)
                    && Objects.equals(this.unitTimelimit, rec.unitTimelimit)
                    && Objects.equals(this.possibleScore, rec.possibleScore)
                    && Objects.equals(this.nbrQuestions, rec.nbrQuestions)
                    && Objects.equals(this.unitType, rec.unitType);
        } else {
            equal = false;
        }

        return equal;
    }
}
