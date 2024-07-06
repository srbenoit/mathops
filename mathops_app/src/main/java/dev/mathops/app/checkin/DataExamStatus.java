package dev.mathops.app.checkin;

import dev.mathops.commons.builder.SimpleBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An exam to present in the UI.
 */
public final class DataExamStatus {

    /** The course ID for the exam (start with "M " for OLD courses, and "MATH " for NEW courses). */
    public final String courseId;

    /** The unit for the exam. */
    public final int unit;

    /** True if the exam is available to the student. */
    public boolean available;

    /** For a NEW course, the number of standards available to master. */
    int numStandardsAvailable = 0;

    /** The reason the exam is not available, or a status message to display on an available exam. */
    public String note;

    /** A list of notes about eligibility tests that were overridden. */
    public final List<String> eligibilityOverrides;

    /**
     * Constructs a new {@code ExamStatus} for an "available" exam.
     *
     * @param theCourse the course for the exam
     * @param theUnit   the unit for the exam
     * @param theNote   the reason the exam is not available
     * @return the constructed object
     */
    static DataExamStatus unavailable(final String theCourse, final int theUnit, final String theNote) {

        return new DataExamStatus(theCourse, theUnit, false, theNote);
    }

    /**
     * Constructs a new {@code ExamStatus} for an "available" exam.
     *
     * @param theCourse the course for the exam
     * @param theUnit   the unit for the exam
     * @param theNote   an optional status message to attach to the object
     * @return the constructed object
     */
    public static DataExamStatus available(final String theCourse, final int theUnit, final String theNote) {

        return new DataExamStatus(theCourse, theUnit, true, theNote);
    }

    /**
     * Constructs a new {@code ExamStatus} for an "available" exam.
     *
     * @param theCourse the course for the exam
     * @param theUnit   the unit for the exam
     * @return the constructed object
     */
    public static DataExamStatus available(final String theCourse, final int theUnit) {

        return new DataExamStatus(theCourse, theUnit, true, null);
    }

    /**
     * Constructs a new {@code ExamStatus} for an "unavailable" exam.
     *
     * @param theCourse   the course for the exam
     * @param theUnit     the unit for the exam
     * @param isAvailable true if the exam is available; false if not
     * @param theNote     a status message to attach to the object
     */
    private DataExamStatus(final String theCourse, final int theUnit, final boolean isAvailable, final String theNote) {

        this.courseId = theCourse;
        this.unit = theUnit;
        this.available = isAvailable;
        this.note = theNote;
        this.eligibilityOverrides = new ArrayList<>(5);
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
     * Marks the exam as unavailable with a specified note.
     *
     * @param theNote the note
     */
    void indicateUnavailable(final String theNote) {

        this.available = false;
        this.note = theNote;
    }

    /**
     * If this exam is (1) available, and (2) has no actual note, and (3) has at least one note of an eligibility test
     * that was overridden, then the note on the exam is set to "(ineligible)" to let the user know that an eligibility
     * test was overridden - UIs should provide "hover" popup text to indicate which tests were overridden.
     */
    void annotateIneligible() {

        if (this.available && (this.note == null || this.note.isBlank()) && !this.eligibilityOverrides.isEmpty()) {
            this.note = "(ineligible)";
        }
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

        return SimpleBuilder.concat("DataExamStatus{courseId='", this.courseId, "',unit=", unitStr, ",available=",
                availableStr, ",numStandardsAvailable=", numStandardsAvailableStr, "' note='", this.note,
                "',eligibilityOverrides='", this.eligibilityOverrides, "'}");
    }
}
