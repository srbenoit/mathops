package dev.mathops.app.sim.registration;

import java.util.ArrayList;
import java.util.List;

/**
 * A schedule of offered class sections.
 */
final class OfferedClassSchedule {

    /** The list of offered sections. */
    private final List<OfferedSection> sections;

    /**
     * Constructs a new {@code OfferedClassSchedule}.
     */
    OfferedClassSchedule() {

        this.sections = new ArrayList<>(20);
    }

    /**
     * Adds a section.
     *
     * @param section the section to add
     */
    void addSection(final OfferedSection section) {

        this.sections.add(section);
    }

    /**
     * Retrieves a copy of this object's list of offered sections.
     *
     * @return a copy of the offered sections list
     */
    List<OfferedSection> getSections() {

        return new ArrayList<>(this.sections);
    }
}
