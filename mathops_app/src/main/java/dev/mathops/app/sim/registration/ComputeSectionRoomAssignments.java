package dev.mathops.app.sim.registration;

import java.util.List;

/**
 * Given a list of courses with seat counts and lists of available classrooms and labs, this class calculates the set of
 * sections and assigns each section to a room.
 */
enum ComputeSectionRoomAssignments {
    ;

    /**
     * Calculates sections and room assignments.
     *
     * @param courses             the list of courses offered (with the number of seats needed populated)
     * @param studentDistribution the student distribution
     * @param allClassrooms       the list of available classrooms
     * @param allLabs             the list of available labs
     * @return the maximum population size
     */
    static int compute(final List<OfferedCourse> courses, final StudentDistribution studentDistribution,
                       final List<AvailableClassroom> allClassrooms, final List<AvailableLab> allLabs) {

        // TODO:
        return 1;
    }
}
