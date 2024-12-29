package dev.mathops.app.sim.students;

import java.util.HashMap;
import java.util.Map;

/**
 * A student population, with a set number of students with each defined set of class preferences.
 */
public final class StudentPopulation {

    /** A step size. */
    private static final double LARGE_STEP = 0.25;

    /** A step size. */
    private static final double SMALL_STEP = 0.1;

    /** A step size. */
    private static final double TINY_STEP = 0.01;

    /** A map from preferences key to the number of students with those preferences. */
    private final Map<StudentClassPreferences, Integer> counts;

    /**
     * Constructs a new {@code StudentPopulation} from a distribution and a target population size.
     *
     * @param distribution         the population distribution
     * @param targetPopulationSize the target population size
     */
    public StudentPopulation(final StudentDistribution distribution, final int targetPopulationSize) {

        distribution.normalize();
        final Map<StudentClassPreferences, Double> dist = distribution.getDistribution();

        final int numEntries = dist.size();
        this.counts = new HashMap<>(numEntries);

        int total = scaleDistribution(dist, targetPopulationSize);

        if (total < targetPopulationSize) {
            double newTarget = (double) targetPopulationSize + LARGE_STEP;
            while (total < targetPopulationSize) {
                total = scaleDistribution(dist, newTarget);
                newTarget += LARGE_STEP;
            }
        }
        if (total > targetPopulationSize) {
            double newTarget = (double) targetPopulationSize - SMALL_STEP;
            while (total > targetPopulationSize) {
                total = scaleDistribution(dist, newTarget);
                newTarget -= SMALL_STEP;
            }
        }
        if (total < targetPopulationSize) {
            double newTarget = (double) targetPopulationSize + TINY_STEP;
            while (total < targetPopulationSize) {
                total = scaleDistribution(dist, newTarget);
                newTarget += TINY_STEP;
            }
        }

        if (total > targetPopulationSize) {

            int largest = 0;
            StudentClassPreferences largestKey = null;
            for (final Map.Entry<StudentClassPreferences, Integer> entry : this.counts.entrySet()) {
                final int count = entry.getValue().intValue();
                if (count > largest) {
                    largest = count;
                    largestKey = entry.getKey();
                }
            }

            largest -= (total - targetPopulationSize);
            final Integer largestObj = Integer.valueOf(largest);
            this.counts.put(largestKey, largestObj);
        }
    }

    /**
     * Scales a distribution, generating an integer count for each preference key.
     *
     * @param dist       the distribution to scale
     * @param targetSize the target population size
     * @return the total number of student in the scaled population
     */
    private int scaleDistribution(final Map<StudentClassPreferences, Double> dist, final double targetSize) {

        int total = 0;

        for (final Map.Entry<StudentClassPreferences, Double> entry : dist.entrySet()) {
            final StudentClassPreferences preferenceKey = entry.getKey();
            final double portion = entry.getValue().doubleValue();
            final int scaled = (int) Math.round(portion * targetSize);
            total += scaled;

            final Integer scaledObj = Integer.valueOf(scaled);
            this.counts.put(preferenceKey, scaledObj);
        }

        return total;
    }

    /**
     * Gets the total number of students in the population.
     *
     * @return the number of students
     */
    public int getSize() {

        int total = 0;

        for (final Integer count : this.counts.values()) {
            total += count.intValue();
        }

        return total;
    }

    /**
     * Gets a copy of the population's map from class preferences to the number of students with those preferences.
     *
     * @return the population map
     */
    public Map<StudentClassPreferences, Integer> getCounts() {

        return new HashMap<>(this.counts);
    }
}
