package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "exam" record.
 */
public final class RawExam extends RecBase implements Comparable<RawExam> {

    /** The table name. */
    public static final String TABLE_NAME = "exam";

    /** A field name. */
    private static final String FLD_VERSION = "version";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_UNIT = "unit";

    /** A field name. */
    private static final String FLD_VSN_EXPLT = "vsn_explt";

    /** A field name. */
    private static final String FLD_TITLE = "title";

    /** A field name. */
    private static final String FLD_TREE_REF = "tree_ref";

    /** A field name. */
    private static final String FLD_EXAM_TYPE = "exam_type";

    /** A field name. */
    private static final String FLD_ACTIVE_DT = "active_dt";

    /** A field name. */
    private static final String FLD_PULL_DT = "pull_dt";

    /** A field name. */
    private static final String FLD_BUTTON_LABEL = "button_label";

    /** The 'version' field value. */
    public String version;

    /** The 'course' field value. */
    public String course;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'vsn_explt' field value. */
    public String vsnExplt;

    /** The 'title' field value. */
    public String title;

    /** The 'tree_ref' field value. */
    public String treeRef;

    /**
     * The 'exam_type' field value ('CH', 'F', 'Q', 'R', or 'U'). User's exams are tagged as "Q" (Qualifying) along with
     * placement and proctored ELM exams. Skills review exams are tagged "R". "CH is for challenge exams, and "R", "U",
     * and "F" are "Review", "Unit", and "Final", respectively.
     */
    public String examType;

    /** The 'active_dt' field value. */
    public LocalDate activeDt;

    /** The 'pull_dt' field value. */
    public LocalDate pullDt;

    /** The 'button_label' field value. */
    public String buttonLabel;

    /**
     * Constructs a new {@code RawExam}.
     */
    private RawExam() {

        super();
    }

    /**
     * Constructs a new {@code RawExam}.
     *
     * @param theVersion     the version
     * @param theCourse      the course
     * @param theUnit        the unit
     * @param theVsnExplt    the version as an expletive
     * @param theTitle       the exam title
     * @param theTreeRef     the tree reference
     * @param theExamType    the exam type
     * @param theActiveDt    the date the exam was made active
     * @param thePullDt      the date the exam was pulled from service
     * @param theButtonLabel the button label
     */
    public RawExam(final String theVersion, final String theCourse, final Integer theUnit,
                   final String theVsnExplt, final String theTitle, final String theTreeRef,
                   final String theExamType, final LocalDate theActiveDt, final LocalDate thePullDt,
                   final String theButtonLabel) {

        super();

        this.version = theVersion;
        this.course = theCourse;
        this.unit = theUnit;
        this.vsnExplt = theVsnExplt;
        this.title = theTitle;
        this.treeRef = theTreeRef;
        this.examType = theExamType;
        this.activeDt = theActiveDt;
        this.pullDt = thePullDt;
        this.buttonLabel = theButtonLabel;
    }

    /**
     * Extracts a "course" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawExam fromResultSet(final ResultSet rs) throws SQLException {

        final RawExam result = new RawExam();

        result.version = getStringField(rs, FLD_VERSION);
        result.course = getStringField(rs, FLD_COURSE);
        result.unit = getIntegerField(rs, FLD_UNIT);
        result.vsnExplt = getStringField(rs, FLD_VSN_EXPLT);
        result.title = getStringField(rs, FLD_TITLE);
        result.treeRef = getStringField(rs, FLD_TREE_REF);
        result.examType = getStringField(rs, FLD_EXAM_TYPE);
        result.activeDt = getDateField(rs, FLD_ACTIVE_DT);
        result.pullDt = getDateField(rs, FLD_PULL_DT);
        result.buttonLabel = getStringField(rs, FLD_BUTTON_LABEL);

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
    public int compareTo(final RawExam o) {

        int result = compareAllowingNull(this.course, o.course);

        if (result == 0) {
            result = compareAllowingNull(this.unit, o.unit);
            if (result == 0) {
                result = compareAllowingNull(this.examType, o.examType);
                if (result == 0) {
                    result = compareAllowingNull(this.version, o.version);
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

        appendField(htm, FLD_VERSION, this.version);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT, this.unit);
        htm.add(DIVIDER);
        appendField(htm, FLD_VSN_EXPLT, this.vsnExplt);
        htm.add(DIVIDER);
        appendField(htm, FLD_TITLE, this.title);
        htm.add(DIVIDER);
        appendField(htm, FLD_TREE_REF, this.treeRef);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_TYPE, this.examType);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACTIVE_DT, this.activeDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_PULL_DT, this.pullDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_BUTTON_LABEL, this.buttonLabel);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.version)
                + Objects.hashCode(this.course)
                + Objects.hashCode(this.unit)
                + Objects.hashCode(this.vsnExplt)
                + Objects.hashCode(this.title)
                + Objects.hashCode(this.treeRef)
                + Objects.hashCode(this.examType)
                + Objects.hashCode(this.activeDt)
                + Objects.hashCode(this.pullDt)
                + Objects.hashCode(this.buttonLabel);
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
        } else if (obj instanceof final RawExam rec) {
            equal = Objects.equals(this.version, rec.version)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.vsnExplt, rec.vsnExplt)
                    && Objects.equals(this.title, rec.title)
                    && Objects.equals(this.treeRef, rec.treeRef)
                    && Objects.equals(this.examType, rec.examType)
                    && Objects.equals(this.activeDt, rec.activeDt)
                    && Objects.equals(this.pullDt, rec.pullDt)
                    && Objects.equals(this.buttonLabel, rec.buttonLabel);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Gets the full name corresponding to an exam type.
     *
     * @param examType the exam type
     * @return the full name
     */
    public static String getExamTypeName(final String examType) {

        return switch (examType) {
            case "U" -> "Unit Exam";
            case "R" -> "Unit Review Exam";
            case "F" -> "Final Exam";
            case "Q" -> "Qualifying Exam";
            case "L" -> "User's Exam";
            case "CH" -> "Challenge Exam";
            case null, default -> "Exam";
        };
    }
}
