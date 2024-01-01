package dev.mathops.app.checkin;

import dev.mathops.core.builder.SimpleBuilder;

import java.util.Objects;

/**
 * An exam to present in the UI.
 */
final class DataExamStatus {

    /** The course ID for the exam (start with "M " for OLD courses, and "MATH " for NEW courses). */
    private final String courseId;

    /** The unit for the exam. */
    private final int unit;

    /** True if the exam is available to the student. */
    boolean available;

    /** For a NEW course, the number of standards available to master. */
    int numStandardsAvailable = 0;

    /** An override label. */
    String newLabel = null;

    /** Text reason why the exam is not available to the student. */
    String whyNot = null;

    /**
     * Constructs a new {@code ExamStatus} for an "available" exam.
     *
     * @param theCourse the course for the exam
     * @param theUnit   the unit for the exam
     */
    DataExamStatus(final String theCourse, final int theUnit) {

        this.courseId = theCourse;
        this.unit = theUnit;
        this.available = true;
    }

    /**
     * Constructs a new {@code ExamStatus} for an "unavailable" exam.
     *
     * @param theCourse the course for the exam
     * @param theUnit   the unit for the exam
     * @param theWhyNot the reason the exam is not available (if {@code null}, the exam is available)
     */
    DataExamStatus(final String theCourse, final int theUnit, final String theWhyNot) {

        this.courseId = theCourse;
        this.unit = theUnit;
        this.available = theWhyNot == null;
        this.whyNot = theWhyNot;
    }

    /**
     * Gets the course ID.
     *
     * @return the course ID
     */
    private String getCourseId() {

        return this.courseId;
    }

    /**
     * Gets the unit.
     *
     * @return the unit
     */
    private int getUnit() {

        return this.unit;
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        int hash = this.unit;

        if (this.courseId != null) {
            hash += this.courseId.hashCode();
        }

        return hash;
    }

    /**
     * Tests whether this object is equal to another. Equality of an available exam occurs when the course and unit
     * match. Implementing this method allows us to use the {@code contains} method on collections of these objects.
     *
     * @param obj the object to test against
     * @return {@code true} if the objects are equal
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean result;

        if (obj == null) {
            result = false;
        } else if (obj instanceof final DataExamStatus test) {
            final int testUnit = test.getUnit();
            final String testCourseId = test.getCourseId();
            result = testUnit == this.unit && Objects.equals(testCourseId, this.courseId);
        } else {
            result = false;
        }

        return result;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String unitStr = Integer.toString(this.unit);
        final String availableStr = Boolean.toString(this.available);
        final String numStandardsAvailableStr = Integer.toString(this.numStandardsAvailable);

        return SimpleBuilder.concat("DataExamStatus{courseId='", this.courseId, "', unit=", unitStr, ", available=",
                availableStr, ", numStandardsAvailable=", numStandardsAvailableStr, ", newLabel='", this.newLabel,
                "' whyNot='", this.whyNot, "'}");
    }
}
