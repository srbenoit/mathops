package dev.mathops.db.rec;

import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A "student course mastery" record.
 */
public final class StuCourseMasteryRec extends RecBase
        implements Comparable<StuCourseMasteryRec> {

    /** The table name. */
    public static final String TABLE_NAME = "stu_course_mastery";

    /** A field name for serialization of records. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name for serialization of records. */
    private static final String FLD_COURSE_ID = "course_id";

    /** A field name for serialization of records. */
    private static final String FLD_SCORE = "score";

    /** A field name for serialization of records. */
    private static final String FLD_NBR_MASTERED_H1 = "nbr_mastered_h1";

    /** A field name for serialization of records. */
    private static final String FLD_NBR_MASTERED_H2 = "nbr_mastered_h2";

    /** A field name for serialization of records. */
    private static final String FLD_NBR_ELIGIBLE = "nbr_eligible";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'course_id' field value. */
    public String courseId;

    /** The 'score' field value. */
    public Integer score;

    /** The 'nbr_mastered_h1' field value. */
    public Integer nbrMasteredH1;

    /** The 'nbr_mastered_h2' field value. */
    public Integer nbrMasteredH2;

    /** The 'nbr_eligible' field value. */
    public Integer nbrEligible;

    /**
     * Constructs a new {@code StudentCourseMasteryRec}.
     */
    public StuCourseMasteryRec() {

        super();
    }

    /**
     * Constructs a new {@code StudentCourseMasteryRec}.
     *
     * @param theStuId         the student ID
     * @param theCourseId      the course ID
     * @param theScore         the total score
     * @param theNbrMasteredH1 the number of standards mastered in half 1 of course
     * @param theNbrMasteredH2 the number of standards mastered in half 2 of course
     * @param theNbrEligible   the number of standards eligible but not yet mastered
     */
    public StuCourseMasteryRec(final String theStuId, final String theCourseId, final Integer theScore,
                               final Integer theNbrMasteredH1, final Integer theNbrMasteredH2,
                               final Integer theNbrEligible) {

        super();

        this.stuId = theStuId;
        this.courseId = theCourseId;
        this.score = theScore;
        this.nbrMasteredH1 = theNbrMasteredH1;
        this.nbrMasteredH2 = theNbrMasteredH2;
        this.nbrEligible = theNbrEligible;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StuCourseMasteryRec o) {

        int result = compareAllowingNull(this.stuId, o.stuId);

        if (result == 0) {
            result = compareAllowingNull(this.courseId, o.courseId);
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
        appendField(htm, FLD_SCORE, this.score);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_MASTERED_H1, this.nbrMasteredH1);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_MASTERED_H2, this.nbrMasteredH2);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_ELIGIBLE, this.nbrEligible);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.stuId) + Objects.hashCode(this.courseId) + Objects.hashCode(this.score)
                + Objects.hashCode(this.nbrMasteredH1) + Objects.hashCode(this.nbrMasteredH2)
                + Objects.hashCode(this.nbrEligible);
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
        } else if (obj instanceof final StuCourseMasteryRec rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.courseId, rec.courseId)
                    && Objects.equals(this.score, rec.score)
                    && Objects.equals(this.nbrMasteredH1, rec.nbrMasteredH1)
                    && Objects.equals(this.nbrMasteredH2, rec.nbrMasteredH2)
                    && Objects.equals(this.nbrEligible, rec.nbrEligible);
        } else {
            equal = false;
        }

        return equal;
    }
}
