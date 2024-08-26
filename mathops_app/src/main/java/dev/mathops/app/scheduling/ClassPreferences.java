package dev.mathops.app.scheduling;

import java.util.HashMap;
import java.util.Map;

/**
 * A container for a student's preferences with respect to classes taken.  Each class offered has a "desirability" value
 * in this class, where 1.0 means the student definitely wants to take the class, 0.0 means they definitely do not.
 */
public final class ClassPreferences {

    /** A map from course ID to preference level. */
    private final Map<String, Double> preferences;

    /**
     * Constructs a new {@code ClassPreferences}.
     */
    public ClassPreferences() {

        this.preferences = new HashMap<>(40);
    }

    /**
     * Sets the preference value for a course.
     *
     * @param course     the course ID
     * @param preference the preference value
     */
    public void setPreference(final String course, final double preference) {

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
    public double getPreference(final String course) {

        final Double prefObj = this.preferences.get(course);

        return prefObj == null ? 0.0 : prefObj.doubleValue();
    }
}
