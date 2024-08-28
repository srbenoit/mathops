package dev.mathops.app.scheduling;

import java.util.HashMap;
import java.util.Map;

/**
 * A container for a student's preferences with respect to classes taken.  Each class offered has a "desirability" value
 * in this class, where 1.0 means the student definitely wants to take the class, 0.0 means they definitely do not.
 */
final class StudentClassPreferences implements Comparable<StudentClassPreferences> {

    /** A unique key for this set of preferences. */
    final String key;

    /** The minimum number of credits the student wants. */
    final int minCredits;

    /** The maximum number of credits the student wants. */
    final int maxCredits;

    /** A map from course ID to preference level. */
    private final Map<String, Double> preferences;

    /**
     * Constructs a new {@code StudentClassPreferences}.
     *
     * @param theKey a unique key for this set of preferences
     */
    StudentClassPreferences(final String theKey, final int theMinCredits, final int theMaxCredits) {

        if (theKey == null) {
            throw new IllegalArgumentException("Key may not be null");
        }

        this.key = theKey;
        this.minCredits = theMinCredits;
        this.maxCredits = theMaxCredits;
        this.preferences = new HashMap<>(40);
    }

    /**
     * Sets the preference value for a course.
     *
     * @param course     the course ID
     * @param preference the preference value
     */
    void setPreference(final String course, final double preference) {

        final double clampUpperBound = Math.min(preference, 1.0);
        final double clampLowerBound = Math.max(clampUpperBound, 0.0);
        final Double prefObj = Double.valueOf(clampLowerBound);

        this.preferences.put(course, prefObj);
    }

    /**
     * Gets the preference value for a course.
     *
     * @param course the course ID
     * @return the preference value (0 if the course was not found)
     */
    double getPreference(final String course) {

        final Double prefObj = this.preferences.get(course);

        return prefObj == null ? 0.0 : prefObj.doubleValue();
    }

    /**
     * Computes a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.key.hashCode();
    }

    /**
     * Tests whether this object is equal to another.  Equality of this class is tested only on equality of the unique
     * key.
     *
     * @param obj the other object
     * @return true if this object is equal
     */
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final StudentClassPreferences p) {
            equal = this.key.equals(p.key);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares this object to another for order.  Order comparisons are done on unique key strings.
     *
     * @param o the object to be compared
     * @return 0 if the argument is equal to this object; a value less than 0 if this object's key is lexicographically
     *         less than the argument's key; and a value greater than 0 if this object's key is lexicographically
     *         greater than the argument's key
     */
    @Override
    public int compareTo(StudentClassPreferences o) {

        return this.key.compareTo(o.key);
    }
}
