package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An immutable raw "student standard mastery" record.
 *
 * <p>
 * Each record represents the status of a single student's mastery of a single standard in a standards-based course.
 *
 * <p>
 * The primary key on the underlying table is the student ID, course ID, module number, and standard number.
 */
public final class StudentStandardMasteryRec extends RecBase implements Comparable<StudentStandardMasteryRec> {

    /** The table name. */
    public static final String TABLE_NAME = "student_standard_mastery";

    /** The 'student_id' field value. */
    public final String studentId;

    /** The 'course_id' field value. */
    public final String courseId;

    /** The 'module_nbr' field value. */
    public final Integer moduleNbr;

    /** The 'standard_nbr' field value. */
    public final Integer standardNbr;

    /** The 'score' field value. */
    public final Integer score;

    /** The 'mastered' field value. */
    public final String mastered;

    /**
     * Constructs a new {@code StudentStandardMastery}.
     *
     * @param theStudentId   the student ID
     * @param theCourseId    the course ID
     * @param theModuleNbr   the module number
     * @param theStandardNbr the standard number
     * @param theScore       the student's current score
     * @param theMastered    "Y" if the standard is mastered; "N" if not
     */
    public StudentStandardMasteryRec(final String theStudentId, final String theCourseId, final Integer theModuleNbr,
                                     final Integer theStandardNbr, final Integer theScore, final String theMastered) {

        super();

        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }
        if (theModuleNbr == null) {
            throw new IllegalArgumentException("Module number may not be null");
        }
        if (theStandardNbr == null) {
            throw new IllegalArgumentException("Standard number may not be null");
        }
        if (theScore == null) {
            throw new IllegalArgumentException("Score may not be null");
        }
        if (theMastered == null) {
            throw new IllegalArgumentException("Mastered flag may not be null");
        }

        this.studentId = theStudentId;
        this.courseId = theCourseId;
        this.moduleNbr = theModuleNbr;
        this.standardNbr = theStandardNbr;
        this.score = theScore;
        this.mastered = theMastered;
    }

    /**
     * Compares two records for order.  Order is based on student ID, course ID, module number, then standard number.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StudentStandardMasteryRec o) {

        int result = compareAllowingNull(this.studentId, o.studentId);

        if (result == 0) {
            result = compareAllowingNull(this.courseId, o.courseId);
            if (result == 0) {
                result = compareAllowingNull(this.moduleNbr, o.moduleNbr);
                if (result == 0) {
                    result = compareAllowingNull(this.standardNbr, o.standardNbr);
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

        appendField(htm, DataDict.FLD_STUDENT_ID, this.studentId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_COURSE_ID, this.courseId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MODULE_NBR, this.moduleNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_STANDARD_NBR, this.standardNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_SCORE, this.score);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MASTERED, this.mastered);

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
               + this.moduleNbr.hashCode()
               + this.standardNbr.hashCode()
               + this.score.hashCode()
               + this.mastered.hashCode();
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
        } else if (obj instanceof final StudentStandardMasteryRec rec) {
            equal = this.studentId.equals(rec.studentId)
                    && this.courseId.equals(rec.courseId)
                    && this.moduleNbr.equals(rec.moduleNbr)
                    && this.standardNbr.equals(rec.standardNbr)
                    && this.score.equals(rec.score)
                    && this.mastered.equals(rec.mastered);
        } else {
            equal = false;
        }

        return equal;
    }
}
