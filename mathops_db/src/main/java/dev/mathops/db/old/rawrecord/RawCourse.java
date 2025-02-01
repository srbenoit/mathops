package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "course" record.
 */
public final class RawCourse extends RecBase implements Comparable<RawCourse> {

    /** The table name. */
    public static final String TABLE_NAME = "course";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_NBR_UNITS = "nbr_units";

    /** A field name. */
    private static final String FLD_COURSE_NAME = "course_name";

    /** A field name. */
    private static final String FLD_NBR_CREDITS = "nbr_credits";

    /** A field name. */
    private static final String FLD_CALC_OK = "calc_ok";

    /** A field name. */
    private static final String FLD_COURSE_LABEL = "course_label";

    /** A field name. */
    private static final String FLD_INLINE_PREFIX = "inline_prefix";

    /** A field name. */
    private static final String FLD_IS_TUTORIAL = "is_tutorial";

    /** A field name. */
    private static final String FLD_REQUIRE_ETEXT = "require_etext";

    /** The 'course' field value. */
    public String course;

    /** The 'nbr_units' field value. */
    public Integer nbrUnits;

    /** The 'course_name' field value. */
    public String courseName;

    /** The 'nbr_credits' field value. */
    public Integer nbrCredits;

    /** The 'calc_ok' field value. */
    public String calcOk;

    /** The 'course_label' field value. */
    public String courseLabel;

    /** The 'inline_prefix' field value. */
    public String inlinePrefix;

    /** The 'is_tutorial' field value. */
    public String isTutorial;

    /** The 'require_etext' field value. */
    public String requireEtext;

    /**
     * Constructs a new {@code RawCourse}.
     */
    public RawCourse() {

        super();
    }

    /**
     * Constructs a new {@code RawCourse}.
     *
     * @param theCourse       the course
     * @param theNbrUnits     the number of units
     * @param theCourseName   the course name
     * @param theNbrCredits   the number of credits
     * @param theCalcOk       true if a calculator can be lent to students enrolled in this course
     * @param theCourseLabel  the course label
     * @param theInlinePrefix the inline prefix
     * @param theIsTutorial   "Y" if the course is a tutorial; false if not
     * @param theRequireEtext true if access to the course requires a valid e-text
     */
    public RawCourse(final String theCourse, final Integer theNbrUnits, final String theCourseName,
                     final Integer theNbrCredits, final String theCalcOk, final String theCourseLabel,
                     final String theInlinePrefix, final String theIsTutorial, final String theRequireEtext) {

        super();

        this.course = theCourse;
        this.nbrUnits = theNbrUnits;
        this.courseName = theCourseName;
        this.nbrCredits = theNbrCredits;
        this.calcOk = theCalcOk;
        this.courseLabel = theCourseLabel;
        this.inlinePrefix = theInlinePrefix;
        this.isTutorial = theIsTutorial;
        this.requireEtext = theRequireEtext;
    }

    /**
     * Gets the catalog URL associated with the course.
     *
     * @return the catalog URL; null if unable to determine a URL
     */
    public String getCatalogUrl() {

        final String url;

        if ("S 100".equals(this.course)) {
            url = "https://catalog.colostate.edu/search/?search=STAT+100";
        } else if ("S 201".equals(this.course)) {
            url = "https://catalog.colostate.edu/search/?search=STAT+201";
        } else if ("S 204".equals(this.course)) {
            url = "https://catalog.colostate.edu/search/?search=STAT+204";
        } else if ("D 369".equals(this.course)) {
            url = "https://catalog.colostate.edu/search/?search=DSCI+369";
        } else if (this.course.startsWith("M 100") || !this.course.startsWith("M ")) {
            url = null;
        } else {
            url = "https://catalog.colostate.edu/search/?search=MATH+" + this.course.substring(2);
        }

        return url;
    }

    /**
     * Extracts a "course" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawCourse fromResultSet(final ResultSet rs) throws SQLException {

        final RawCourse result = new RawCourse();

        result.course = getStringField(rs, FLD_COURSE);
        result.nbrUnits = getIntegerField(rs, FLD_NBR_UNITS);
        result.courseName = getStringField(rs, FLD_COURSE_NAME);
        result.nbrCredits = getIntegerField(rs, FLD_NBR_CREDITS);
        result.calcOk = getStringField(rs, FLD_CALC_OK);
        result.courseLabel = getStringField(rs, FLD_COURSE_LABEL);
        result.inlinePrefix = getStringField(rs, FLD_INLINE_PREFIX);
        result.isTutorial = getStringField(rs, FLD_IS_TUTORIAL);
        result.requireEtext = getStringField(rs, FLD_REQUIRE_ETEXT);

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
    public int compareTo(final RawCourse o) {

        return compareAllowingNull(this.course, o.course);
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

        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_UNITS, this.nbrUnits);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE_NAME, this.courseName);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_CREDITS, this.nbrCredits);
        htm.add(DIVIDER);
        appendField(htm, FLD_CALC_OK, this.calcOk);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE_LABEL, this.courseLabel);
        htm.add(DIVIDER);
        appendField(htm, FLD_INLINE_PREFIX, this.inlinePrefix);
        htm.add(DIVIDER);
        appendField(htm, FLD_IS_TUTORIAL, this.isTutorial);
        htm.add(DIVIDER);
        appendField(htm, FLD_REQUIRE_ETEXT, this.requireEtext);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.course)
                + Objects.hashCode(this.nbrUnits)
                + Objects.hashCode(this.courseName)
                + Objects.hashCode(this.nbrCredits)
                + Objects.hashCode(this.calcOk)
                + Objects.hashCode(this.courseLabel)
                + Objects.hashCode(this.inlinePrefix)
                + Objects.hashCode(this.isTutorial)
                + Objects.hashCode(this.requireEtext);
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
        } else if (obj instanceof final RawCourse rec) {
            equal = Objects.equals(this.course, rec.course)
                    && Objects.equals(this.nbrUnits, rec.nbrUnits)
                    && Objects.equals(this.courseName, rec.courseName)
                    && Objects.equals(this.nbrCredits, rec.nbrCredits)
                    && Objects.equals(this.calcOk, rec.calcOk)
                    && Objects.equals(this.courseLabel, rec.courseLabel)
                    && Objects.equals(this.inlinePrefix, rec.inlinePrefix)
                    && Objects.equals(this.isTutorial, rec.isTutorial)
                    && Objects.equals(this.requireEtext, rec.requireEtext);
        } else {
            equal = false;
        }

        return equal;
    }
}
