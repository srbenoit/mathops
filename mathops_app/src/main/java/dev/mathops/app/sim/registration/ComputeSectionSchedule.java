package dev.mathops.app.sim.registration;

import java.util.Collection;
import java.util.List;

/**
 * Given a list of course with their room assignments, and a list of enrolled students, attempts to organize the course
 * room assignments into a schedule that optimizes students' access to courses, driving, "hang time", office hours
 * access, and instructor schedules.  The resulting optimized schedule has a "quality rating" based on these factors.
 *
 * <p>
 * Once this is accomplished, we can simulate the registration process with the actual offered schedule, where students
 * take into account these factors to see if we can further optimize.
 */
public enum ComputeSectionSchedule {
    ;

    /**
     * Calculates the largest population size that can be accommodated for a given set of course offerings, a given set
     * of classrooms and labs, and a given student distribution.  In the process, the number of seats needed in each
     * offered course is computed and stored.
     *
     * @param courses  the list of courses offered
     * @param students the list of students, each with a list of courses
     * @return the maximum population size
     */
    static int compute(final Collection<Course> courses, final List<EnrollingStudent> students) {


    }
}
