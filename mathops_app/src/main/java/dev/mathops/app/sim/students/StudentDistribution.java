package dev.mathops.app.sim.students;

import java.util.HashMap;
import java.util.Map;

/**
 * A container for a distribution of students by class preferences.  The portion of the student population with each set
 * of preferences is stored.  This can then generate a student population of a specified size, assigning preferences to
 * each student in the population.
 */
public final class StudentDistribution {

    /** A small number used to determine whether normalization is needed. */
    private static final double EPSILON = 0.0001;

    /**
     * A map from preferences to a value between 0.0 and 1.0 (the portion of students with this set of preferences.
     * Ideally, these values sum to 1.0, but this class will normalize values if needed.
     */
    private final Map<StudentClassPreferences, Double> distribution;

    /**
     * Constructs a new {@code StudentDistribution}.
     */
    StudentDistribution() {

        this.distribution = new HashMap<>(10);
    }

    /**
     * Adds a group.
     *
     * @param preferences the key of the class preferences for students in this group
     * @param portion     the portion of the whole population who belong to this group
     */
    void addGroup(final StudentClassPreferences preferences, final double portion) {

        if (preferences == null) {
            throw new IllegalArgumentException("Preferences key may not be null");
        }
        if (Double.isFinite(portion)) {
            if (portion <= 0.0) {
                throw new IllegalArgumentException("Portion must be positive");
            }

            final Double portionObj = Double.valueOf(portion);
            this.distribution.put(preferences, portionObj);
        } else {
            throw new IllegalArgumentException("Portion must be finite");
        }
    }

    /**
     * Gets the distribution map.
     *
     * @return the distribution map
     */
    Map<StudentClassPreferences, Double> getDistribution() {

        return this.distribution;
    }

    /**
     * Normalizes the distribution so that the portions of populations sum to 1.0.
     */
    void normalize() {

        double total = 0.0;
        for (final Double value : this.distribution.values()) {
            total += value.doubleValue();
        }

        if (Math.abs(total - 1.0) > EPSILON) {
            final double scale = 1.0 / total;

            for (final Map.Entry<StudentClassPreferences, Double> entry : this.distribution.entrySet()) {
                final Double current = entry.getValue();
                final double newValue = current.doubleValue() * scale;
                final Double newObj = Double.valueOf(newValue);
                entry.setValue(newObj);
            }
        }
    }
}
