package dev.mathops.app.sim.schedule;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.courses.SpurCourses;
import dev.mathops.app.sim.rooms.Room;
import dev.mathops.app.sim.rooms.SpurRooms;
import dev.mathops.app.sim.students.SpurStudents;
import dev.mathops.app.sim.students.StudentPopulation;
import dev.mathops.commons.log.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A simulation of the Spur first-year Fall semester.
 */
final class SpurFirstYearFall {

    /**
     * Constructs a new {@code SpurFirstYearFall}.
     */
    private SpurFirstYearFall() {

        // No action
    }

    /**
     * Runs the simulation.
     */
    private static void runSimulation() {

        final List<Course> immutableCourses = Arrays.asList(SpurCourses.LIFE102, SpurCourses.MATH112,
                SpurCourses.SEMINAR, SpurCourses.CS150B, SpurCourses.IDEA110, SpurCourses.HDFS101,
                SpurCourses.AGRI116, SpurCourses.AB111, SpurCourses.EHRS220, SpurCourses.POLS131,
                SpurCourses.AREC222, SpurCourses.SPCM100, SpurCourses.BZ101);
        final Collection<Course> courses = new ArrayList<>(immutableCourses);

        final List<Room> rooms = List.of(SpurRooms.CLASSROOM_1, SpurRooms.CLASSROOM_2, SpurRooms.LAB_1);

        // SIMULATION PART 1 - DETERMINE MAXIMUM POSSIBLE POPULATION SIZE THAT DOES NOT EXCEED TOTAL CLASSROOM SPACE
        final int maxPop = ComputePopulationSize.compute(courses, SpurStudents.SPUR_FALL_DISTRIBUTION, rooms);
        Log.info("The maximum population supported was " + maxPop);

        // SIMULATION PART 2 - Try to build an assignment of courses to sections across classrooms and labs
        final StudentPopulation population160 = new StudentPopulation(SpurStudents.SPUR_FALL_DISTRIBUTION, 160);
        final Map<Course, Integer> seatCounts = ComputeSectionsNeeded.compute(courses, population160, rooms);
    }

    /**
     * Main method to execute the simulation.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        runSimulation();
    }
}
