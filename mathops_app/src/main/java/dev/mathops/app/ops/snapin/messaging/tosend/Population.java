package dev.mathops.app.ops.snapin.messaging.tosend;

import java.util.HashMap;
import java.util.Map;

/**
 * A population. For example, 2-course Track A students who have forfeit at least one course.
 */
public final class Population {

    /**
     * A map from section number to map from student ID to student registration list for all students in the
     * population.
     */
    public final Map<String, PopulationSection> sections;

    /**
     * Constructs a new {@code Population}.
     */
    Population() {

        this.sections = new HashMap<>(5);
    }

    /**
     * Tests whether the population is not empty (has sections).
     *
     * @return true if the population is not empty
     */
    public boolean hasSections() {

        return !this.sections.isEmpty();
    }

    /**
     * Counts the total number of students in the population.
     *
     * @return the total number of students
     */
    public int countStudents() {

        int count = 0;

        for (final PopulationSection sect : this.sections.values()) {
            count += sect.countStudents();
        }

        return count;
    }

    /**
     * Counts the total number of messages due to be sent in the population.
     *
     * @return the total number of messages
     */
    public int countMessages() {

        int count = 0;

        for (final PopulationSection sect : this.sections.values()) {
            count += sect.countMessages();
        }

        return count;
    }
}
