package dev.mathops.session.sitelogic;

/**
 * Information on an available course or tutorial.
 */
public final class CourseInfo implements Comparable<CourseInfo> {

    /** The course number. */
    public final String course;

    /** The course label, to appear in the menu. */
    public final String label;

    /** An available flag (only needed for tutorials). */
    public final boolean available;

    /**
     * Constructs a {@code CourseInfo}.
     *
     * @param theCourse the course number
     * @param theLabel  the course label, to appear in the menu
     */
    CourseInfo(final String theCourse, final String theLabel) {

        this.course = theCourse;
        this.label = theLabel;
        this.available = true;
    }

    /**
     * Constructs a {@code CourseInfo}.
     *
     * @param theCourse   the course number
     * @param theLabel    the course label, to appear in the menu
     * @param isAvailable {@code true} if tutorial is available
     */
    CourseInfo(final String theCourse, final String theLabel, final boolean isAvailable) {

        this.course = theCourse;
        this.label = theLabel;
        this.available = isAvailable;
    }

    /**
     * Compares this object with the specified object for order. Comparison is based on a direct string comparison of
     * course number.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final CourseInfo o) {

        return this.course.compareTo(o.course);
    }

    /**
     * Gets a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.course.hashCode();
    }

    /**
     * Gets a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public boolean equals(final Object obj) {

        return obj instanceof CourseInfo && this.course.equals(((CourseInfo) obj).course);
    }
}
