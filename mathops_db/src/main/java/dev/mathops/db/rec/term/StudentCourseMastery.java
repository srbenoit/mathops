package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An immutable raw "student course mastery" record.
 *
 * <p>
 * Each record represents the status of a single student's mastery status in a standards-based course.
 *
 * <p>
 * The primary key on the underlying table is the student ID, course ID, module number, and standard number.
 */
public final class StudentCourseMastery extends RecBase implements Comparable<StudentCourseMastery> {

    /** The table name. */
    public static final String TABLE_NAME = "student_course_mastery";

    /** The 'student_id' field value. */
    public final String studentId;

    /** The 'course_id' field value. */
    public final String courseId;

    /** The 'nbr_completed_hw' field value. */
    public final Integer nbrCompletedHw;

    /** The 'nbr_mastered_stds' field value. */
    public final Integer nbrMasteredStds;

    /** The 'score' field value. */
    public final Integer score;

    /**
     * Constructs a new {@code StudentCourseMastery}.
     *
     * @param theStudentId            the student ID
     * @param theCourseId             the course ID
     * @param theNbrCompletedHw       the number of homework assignments completed
     * @param theNbrStandardsMastered the number of standards mastered
     * @param theScore                the student's current score
     */
    public StudentCourseMastery(final String theStudentId, final String theCourseId, final Integer theNbrCompletedHw,
                                final Integer theNbrStandardsMastered, final Integer theScore) {

        super();

        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }
        if (theNbrCompletedHw == null) {
            throw new IllegalArgumentException("Number of homework complete may not be null");
        }
        if (theNbrStandardsMastered == null) {
            throw new IllegalArgumentException("Number of standards mastered may not be null");
        }
        if (theScore == null) {
            throw new IllegalArgumentException("Score may not be null");
        }

        this.studentId = theStudentId;
        this.courseId = theCourseId;
        this.nbrCompletedHw = theNbrCompletedHw;
        this.nbrMasteredStds = theNbrStandardsMastered;
        this.score = theScore;
    }

    /**
     * Compares two records for order.  Order is based on student ID then course ID.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StudentCourseMastery o) {

        int result = compareAllowingNull(this.studentId, o.studentId);

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

        appendField(htm, DataDict.FLD_STUDENT_ID, this.studentId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_COURSE_ID, this.courseId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_NBR_COMPLETED_HW, this.nbrCompletedHw);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_NBR_MASTERED_STDS, this.nbrMasteredStds);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_SCORE, this.score);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.studentId.hashCode()
               + this.courseId.hashCode()
               + this.nbrCompletedHw.hashCode()
               + this.nbrMasteredStds.hashCode()
               + this.score.hashCode();
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
        } else if (obj instanceof final StudentCourseMastery rec) {
            equal = this.studentId.equals(rec.studentId)
                    && this.courseId.equals(rec.courseId)
                    && this.nbrCompletedHw.equals(rec.nbrCompletedHw)
                    && this.nbrMasteredStds.equals(rec.nbrMasteredStds)
                    && this.score.equals(rec.score);
        } else {
            equal = false;
        }

        return equal;
    }
}
