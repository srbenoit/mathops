package dev.mathops.app.sim.students;

import dev.mathops.app.sim.courses.Course;

import java.util.HashMap;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * A container for a student's preferences with respect to classes taken.  Each class offered has a "desirability" value
 * in this class, where 1.0 means the student definitely wants to take the class, 0.0 means they definitely do not.
 */
public final class StudentClassPreferences implements Comparable<StudentClassPreferences> {

    /** A unique key for this set of preferences. */
    public final String key;

    /** The minimum number of credits the student wants. */
    public final int minCredits;

    /** The maximum number of credits the student wants. */
    public final int maxCredits;

    /** A map from course ID to preference level. */
    private final Map<Course, Double> preferences;

    /**
     * Constructs a new {@code StudentClassPreferences}.
     *
     * @param theKey        a unique key for this set of preferences
     * @param theMinCredits the minimum number of credits the student wants
     * @param theMaxCredits the maximum number of credits the student wants
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
     * Gets the key.
     *
     * @return the key
     */
    private String getKey() {

        return this.key;
    }

    /**
     * Sets the preference value for a course.
     *
     * @param course     the course ID
     * @param preference the preference value
     */
    void setPreference(final Course course, final double preference) {

        final double clampUpperBound = Math.min(preference, 1.0);
        final double clampLowerBound = Math.max(clampUpperBound, 0.0);
        final Double prefObj = Double.valueOf(clampLowerBound);

        this.preferences.put(course, prefObj);
    }

    /**
     * Gets the student's preference for this course.
     *
     * @param course the course
     * @return the preference (normalized so all preferences sum to 1.0)
     */
    public double getPreference(final Course course) {

        final Double pref = this.preferences.get(course);
        final double result;

        if (pref == null) {
            result = 0.0;
        } else {
            double total = 0.0;
            for (final Double value : this.preferences.values()) {
                total += value.doubleValue();
            }
            result = pref.doubleValue() / total;
        }

        return result;
    }

    /**
     * Randomly selects a course with probability proportional to the course's preference value.
     *
     * @param rnd a random number generator
     * @return the selected {@code OfferedCourse}
     */
    public Course pick(final RandomGenerator rnd) {

        double total = 0.0;
        for (final Double value : this.preferences.values()) {
            total += value.doubleValue();
        }

        Course result = null;

        if (total > 0.0) {
            final double choice = rnd.nextDouble(total);

            double soFar = 0.0;
            for (final Map.Entry<Course, Double> entry : this.preferences.entrySet()) {
                if (result == null) {
                    // Ensure a choice gets made in case "choice" is right at upper bound and round-off in additions
                    // below result in "soFar" never making it to "choice".
                    result = entry.getKey();
                }
                final double step = entry.getValue().doubleValue();
                if (soFar + step >= choice) {
                    result = entry.getKey();
                    break;
                }

                soFar += step;
            }
        }

        return result;
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
            final String pKey = p.getKey();
            equal = this.key.equals(pKey);
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
    public int compareTo(final StudentClassPreferences o) {

        final String oKey = o.getKey();
        return this.key.compareTo(oKey);
    }
}
