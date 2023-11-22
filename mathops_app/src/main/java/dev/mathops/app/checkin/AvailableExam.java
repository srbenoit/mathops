package dev.mathops.app.checkin;

/**
 * An exam that is available to the student.
 */
final class AvailableExam {

    /** The course for the exam. */
    private final String course;

    /** The unit for the exam. */
    private final int unit;

    /** True if the exam is available to the student. */
    public boolean available = true;

    /** An override label. */
    public String newLabel;

    /** Text reason why the exam is not available to the student. */
    public String whyNot;

    /**
     * Constructs a new {@code AvailableExam}.
     *
     * @param newCourse the course for the exam
     * @param newUnit   the unit for the exam
     */
    AvailableExam(final String newCourse, final int newUnit) {

        this.course = newCourse;
        this.unit = newUnit;
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
        } else if (obj.getClass().equals(AvailableExam.class)) {
            final AvailableExam test = (AvailableExam) obj;

            if (test.unit == this.unit) {

                if (test.course == null) {
                    result = this.course == null;
                } else {
                    result = test.course.equals(this.course);
                }
            } else {
                result = false;
            }
        } else {
            result = false;
        }

        return result;
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        int hash = this.unit;

        if (this.course != null) {
            hash += this.course.hashCode();
        }

        return hash;
    }
}
