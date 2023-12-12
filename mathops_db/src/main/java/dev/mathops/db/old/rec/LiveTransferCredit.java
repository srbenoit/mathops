package dev.mathops.db.old.rec;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawStcourse;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * An immutable record of a live transfer credit record.
 */
public final class LiveTransferCredit implements Serializable, Comparable<LiveTransferCredit> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 3250535224003481285L;

    /** The student ID. */
    public final String studentId;

    /** The term data. */
    private final TermKey term;

    /** The transferred course. */
    public final String transferred;

    /** The course number converted to a course ID, if possible. */
    public final String courseId;

    /** The number of credits. */
    public final Double credits;

    /** The grade. */
    public final String grade;

    /**
     * Constructs a new {@code LiveTransferCredit}.
     *
     * @param theStudentId   the student ID
     * @param theTerm        the term
     * @param theTransferred the transferred course (course number from other institution)
     * @param theCredits     the number of credits
     * @param theGrade       the earned grade
     */
    public LiveTransferCredit(final String theStudentId, final TermKey theTerm,
                              final String theTransferred, final Double theCredits, final String theGrade) {

        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (theTransferred == null) {
            throw new IllegalArgumentException("Transferred course may not be null");
        }
        this.studentId = theStudentId;
        this.term = theTerm;
        this.transferred = theTransferred;
        this.credits = theCredits;
        this.grade = theGrade;

        if (theTransferred.contains("+")) {
            this.courseId = theTransferred;
        } else if (theTransferred.charAt(0) == 'M' && theTransferred.length() < 5) {
            this.courseId = "M " + theTransferred.substring(1);
        } else if (theTransferred.startsWith("MATH")) {
            this.courseId = "M " + theTransferred.substring(4);
        } else {
            this.courseId = theTransferred;
        }
    }

    /**
     * Gets the grade converted to a GPA value, but only if passing (C- or higher).
     *
     * @return the grade GPA
     */
    public Float getGradeGpa() {

        final String actual = this.grade == null || this.grade.isEmpty() ? CoreConstants.EMPTY
                : this.grade.charAt(0) == 'T' ? this.grade.substring(1) : this.grade;

        Float result = null;

        if (RawStcourse.COURSE_GRADES_400.contains(actual)) {
            result = Float.valueOf(4.0f);
        } else if (RawStcourse.COURSE_GRADES_367.contains(actual)) {
            result = Float.valueOf(3.67f);
        } else if (RawStcourse.COURSE_GRADES_333.contains(actual)) {
            result = Float.valueOf(3.33f);
        } else if (RawStcourse.COURSE_GRADES_300.contains(actual)) {
            result = Float.valueOf(3.00f);
        } else if (RawStcourse.COURSE_GRADES_267.contains(actual)) {
            result = Float.valueOf(2.67f);
        } else if (RawStcourse.COURSE_GRADES_233.contains(actual)) {
            result = Float.valueOf(2.33f);
        } else if (RawStcourse.COURSE_GRADES_200.contains(actual)) {
            result = Float.valueOf(2.00f);
        } else if (RawStcourse.COURSE_GRADES_167.contains(actual)) {
            result = Float.valueOf(1.67f);
        }

        return result;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.studentId.hashCode() + this.term.hashCode() + this.transferred.hashCode()
                + this.courseId.hashCode() + EqualityTests.objectHashCode(this.credits)
                + EqualityTests.objectHashCode(this.grade);
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
        } else if (obj instanceof final LiveTransferCredit cred) {
            equal = this.studentId.equals(cred.studentId) //
                    && this.term.equals(cred.term) //
                    && this.transferred.equals(cred.transferred) //
                    && this.courseId.equals(cred.courseId) //
                    && Objects.equals(this.credits, cred.credits)
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
    public int compareTo(final LiveTransferCredit o) {

        int result = this.term == null || o.term == null ? 0 : this.term.compareTo(o.term);

        if (result == 0) {
            result = this.studentId.compareTo(o.studentId);
            if (result == 0) {
                result = this.courseId.compareTo(o.courseId);
                if (result == 0) {
                    result = this.transferred.compareTo(o.transferred);
                }
            }
        }

        return result;
    }
}
