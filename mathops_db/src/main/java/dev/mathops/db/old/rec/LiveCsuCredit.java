package dev.mathops.db.old.rec;

import dev.mathops.commons.EqualityTests;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawStcourse;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * An immutable record of a live CSU credit record.
 */
public final class LiveCsuCredit implements Serializable, Comparable<LiveCsuCredit> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -3420688824191122515L;

    /** The term data. */
    private final TermKey term;

    /** The student ID. */
    private final String studentId;

    /** The course number converted to a course ID, if possible. */
    public final String courseId;

    /** The number of credits. */
    public final Double credits;

    /** The grade. */
    public final String grade;

    /**
     * The grade. * public final String grade;
     * <p>
     * /** Constructs a new {@code LiveCsuCredit}.
     *
     * @param theTerm      the term
     * @param theStudentId the student ID
     * @param theCourseId  the course ID
     * @param theCredits   the number of credits
     * @param theGrade     the earned grade
     */
    public LiveCsuCredit(final TermKey theTerm, final String theStudentId, final String theCourseId,
                         final Double theCredits, final String theGrade) {

        if (theTerm == null) {
            throw new IllegalArgumentException("Term may not be null");
        }
        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }

        this.term = theTerm;
        this.studentId = theStudentId;
        this.courseId = theCourseId;
        this.credits = theCredits;
        this.grade = theGrade;
    }

    /**
     * Gets the grade converted to a GPA value, but only if passing (C- or higher).
     *
     * @return the grade GPA
     */
    public Float getGradeGpa() {

        Float gpa = null;

        if (this.grade != null) {
            if (RawStcourse.COURSE_GRADES_400.contains(this.grade)) {
                gpa = Float.valueOf(4.0f);
            } else if (RawStcourse.COURSE_GRADES_367.contains(this.grade)) {
                gpa = Float.valueOf(3.67f);
            } else if (RawStcourse.COURSE_GRADES_333.contains(this.grade)) {
                gpa = Float.valueOf(3.33f);
            } else if (RawStcourse.COURSE_GRADES_300.contains(this.grade)) {
                gpa = Float.valueOf(3.00f);
            } else if (RawStcourse.COURSE_GRADES_267.contains(this.grade)) {
                gpa = Float.valueOf(2.67f);
            } else if (RawStcourse.COURSE_GRADES_233.contains(this.grade)) {
                gpa = Float.valueOf(2.33f);
            } else if (RawStcourse.COURSE_GRADES_200.contains(this.grade)) {
                gpa = Float.valueOf(2.00f);
            } else if (RawStcourse.COURSE_GRADES_167.contains(this.grade)) {
                gpa = Float.valueOf(1.67f);
            }
        }

        return gpa;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.term.hashCode() + this.studentId.hashCode() + this.courseId.hashCode()
                + Objects.hashCode(this.credits) //
                + Objects.hashCode(this.grade);
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
        } else if (obj instanceof final LiveCsuCredit cred) {
            equal = this.term.equals(cred.term) //
                    && this.studentId.equals(cred.studentId) //
                    && this.courseId.equals(cred.courseId) //
                    && Objects.equals(this.credits, cred.credits) //
                    && Objects.equals(this.grade, cred.grade);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.studentId;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final LiveCsuCredit o) {

        int result = this.term.compareTo(o.term);

        if (result == 0) {
            result = this.studentId.compareTo(o.studentId);
            if (result == 0) {
                result = this.courseId.compareTo(o.courseId);
            }
        }

        return result;
    }
}
