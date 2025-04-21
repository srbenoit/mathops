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
public final class StudentCourseMasteryRec extends RecBase implements Comparable<StudentCourseMasteryRec> {

    /** The table name. */
    public static final String TABLE_NAME = "student_course_mastery";

    /** The 'student_id' field value. */
    public final String studentId;

    /** The 'course_id' field value. */
    public final String courseId;

    /** The 'course_structure' field value. */
    public final String courseStructure;

    /** The 'homework_status' field value. */
    public final String homeworkStatus;

    /** The 'mastery_status' field value. */
    public final String masteryStatus;

    /** The 'completed' field value. */
    public final String completed;

    /** The 'score' field value. */
    public final Integer score;

    /**
     * Constructs a new {@code StudentCourseMastery}.
     *
     * @param theStudentId       the student ID
     * @param theCourseId        the course ID
     * @param theCourseStructure the course structure in a format like: "aAabbbCcc...zzZ", where each letter (a-z)
     *                           represents a module and each repetition of that letter represents a standard, lowercase
     *                           = non-essential, uppercase = essential
     * @param theHomeworkStatus  the student's status on standard homeworks for each standard in a format like "YN---",
     *                           the same length as the course structure, Y=passed, N=attempted, -=not attempted
     * @param theMasteryStatus   the student 's mastery status for each standard, in a format like "Yyn---", the same
     *                           length as the course structure, Y = mastered on time, y = mastered late, N = attempted,
     *                           -= not attempted
     * @param theCompleted       "Y" if the minimum requirements to complete the course have been met; "N" if not
     * @param theScore           the student's current score
     */
    public StudentCourseMasteryRec(final String theStudentId, final String theCourseId, final String theCourseStructure,
                                   final String theHomeworkStatus, final String theMasteryStatus,
                                   final String theCompleted, final Integer theScore) {

        super();

        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }
        if (theCourseStructure == null) {
            throw new IllegalArgumentException("Course structure may not be null");
        }
        if (theHomeworkStatus == null) {
            throw new IllegalArgumentException("Homework status may not be null");
        }
        if (theMasteryStatus == null) {
            throw new IllegalArgumentException("Mastery status may not be null");
        }
        if (theCompleted == null) {
            throw new IllegalArgumentException("Completed flag may not be null");
        }
        if (theScore == null) {
            throw new IllegalArgumentException("Score may not be null");
        }

        this.studentId = theStudentId;
        this.courseId = theCourseId;
        this.courseStructure = theCourseStructure;
        this.homeworkStatus = theHomeworkStatus;
        this.masteryStatus = theMasteryStatus;
        this.completed = theCompleted;
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
    public int compareTo(final StudentCourseMasteryRec o) {

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
        appendField(htm, DataDict.FLD_COURSE_STRUCTURE, this.courseStructure);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_HOMEWORK_STATUS, this.homeworkStatus);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MASTERY_STATUS, this.masteryStatus);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_COMPLETED, this.completed);
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
               + this.courseStructure.hashCode()
               + this.homeworkStatus.hashCode()
               + this.masteryStatus.hashCode()
               + this.completed.hashCode()
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
        } else if (obj instanceof final StudentCourseMasteryRec rec) {
            equal = this.studentId.equals(rec.studentId)
                    && this.courseId.equals(rec.courseId)
                    && this.courseStructure.equals(rec.courseStructure)
                    && this.homeworkStatus.equals(rec.homeworkStatus)
                    && this.masteryStatus.equals(rec.masteryStatus)
                    && this.completed.equals(rec.completed)
                    && this.score.equals(rec.score);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Based on the course structure, finds the index in the structure, homework status, and mastery status strings that
     * corresponds to a particular module and standard.
     *
     * <p>
     * The course structure String is like "aAabbbCcc...zzZ", where each letter(a-z) represents a module and each
     * repetition of that letter represents a standard, lowercase = non-essential, uppercase = essential.
     * </p>
     *
     * @param module   the module number
     * @param standard the standard number
     * @return the index, -1 if the module/standard combination was not found
     */
    private int indexOfStandard(final int module, final int standard) {

        int result = -1;

        final int len = Math.min(this.courseStructure.length(),
                Math.min(this.homeworkStatus.length(), this.masteryStatus.length()));

        if (len > 0) {
            if (module == 1 && standard == 1) {
                result = 0;
            } else {
                char current = Character.toLowerCase(this.courseStructure.charAt(0));
                int m = 1;
                int s = 2;

                for (int i = 1; i < len; ++i) {
                    char ch = Character.toLowerCase(this.courseStructure.charAt(i));

                    if (ch == current) {
                        ++s;
                    } else {
                        ++m;
                        s = 1;
                    }
                    if (module == m && standard == s) {
                        result = i;
                        break;
                    }
                    current = ch;
                }
            }
        }

        return result;
    }

    /**
     * Tests whether a module standard is "essential".
     *
     * @param module   the module number
     * @param standard the standard number
     * @return true if the standard is marked as "essential"; false if not or if the standard was not found
     */
    public boolean isStandardEssential(final int module, final int standard) {

        boolean result = false;

        final int index = indexOfStandard(module, standard);
        if (index >= 0) {
            final char ch = this.courseStructure.charAt(index);
            result = Character.isUpperCase(ch);
        }

        return result;
    }

    /**
     * Tests whether the homework assignment for a module standard has been attempted.
     *
     * @param module   the module number
     * @param standard the standard number
     * @return true if the homework assignment has been attempted (whether it was passed or not)
     */
    public boolean isHomeworkAttempted(final int module, final int standard) {

        boolean result = false;

        final int index = indexOfStandard(module, standard);
        if (index >= 0) {
            final int ch = (int) this.homeworkStatus.charAt(index);
            result = ch == 'Y' || ch == 'N';
        }

        return result;
    }

    /**
     * Tests whether the homework assignment for a module standard has been passed.
     *
     * @param module   the module number
     * @param standard the standard number
     * @return true if the homework assignment has been passed
     */
    public boolean isHomeworkPassed(final int module, final int standard) {

        boolean result = false;

        final int index = indexOfStandard(module, standard);
        if (index >= 0) {
            final int ch = (int) this.homeworkStatus.charAt(index);
            result = ch == 'Y';
        }

        return result;
    }

    /**
     * Tests whether the mastery exam for a module standard has been attempted.
     *
     * @param module   the module number
     * @param standard the standard number
     * @return true if the mastery exam has been attempted (whether it was passed or not)
     */
    public boolean isMasteryExamAttempted(final int module, final int standard) {

        boolean result = false;

        final int index = indexOfStandard(module, standard);
        if (index >= 0) {
            final int ch = (int) this.masteryStatus.charAt(index);
            result = ch == 'Y' || ch == 'y' || ch == 'N';
        }

        return result;
    }

    /**
     * Tests whether the mastery exam for a module standard was passed on time.
     *
     * @param module   the module number
     * @param standard the standard number
     * @return true if the mastery exam was passed on time
     */
    public boolean isMasteryExamPassedOnTime(final int module, final int standard) {

        boolean result = false;

        final int index = indexOfStandard(module, standard);
        if (index >= 0) {
            final int ch = (int) this.masteryStatus.charAt(index);
            result = ch == 'Y';
        }

        return result;
    }

    /**
     * Tests whether the mastery exam for a module standard was passed late.
     *
     * @param module   the module number
     * @param standard the standard number
     * @return true if the mastery exam was passed late
     */
    public boolean isMasteryExamPassedLate(final int module, final int standard) {

        boolean result = false;

        final int index = indexOfStandard(module, standard);
        if (index >= 0) {
            final int ch = (int) this.masteryStatus.charAt(index);
            result = ch == 'y';
        }

        return result;
    }

}
