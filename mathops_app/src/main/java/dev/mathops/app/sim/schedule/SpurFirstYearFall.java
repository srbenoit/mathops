package dev.mathops.app.sim.schedule;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.courses.SpurCourses;
import dev.mathops.app.sim.rooms.RoomSchedule;
import dev.mathops.app.sim.rooms.SpurRooms;
import dev.mathops.app.sim.students.SpurStudents;
import dev.mathops.app.sim.students.StudentPopulation;
import dev.mathops.commons.log.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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

        final List<Course> immutableCourses = Arrays.asList(SpurCourses.CO150, SpurCourses.PRECALC, SpurCourses.KEY175,
                SpurCourses.LIFE102, SpurCourses.AB111, SpurCourses.ERHS220, SpurCourses.CS150B, SpurCourses.SPCM100,
                SpurCourses.IDEA110, SpurCourses.ECON202, SpurCourses.HDFS101, SpurCourses.ART100,
                SpurCourses.MATH160, SpurCourses.IU174, SpurCourses.SOC220, SpurCourses.LIFE103, SpurCourses.CHEM111,
                SpurCourses.IDEA210, SpurCourses.CS201, SpurCourses.HIST15X, SpurCourses.AMST101, SpurCourses.ETST253,
                SpurCourses.KEY192A, SpurCourses.ETST240);
        final List<Course> courses = new ArrayList<>(immutableCourses);

        final List<RoomSchedule> rooms = List.of(SpurRooms.CLASSROOM_1, SpurRooms.CLASSROOM_2, SpurRooms.LAB_1);

        // SIMULATION PART 1 - DETERMINE MAXIMUM POSSIBLE POPULATION SIZE THAT DOES NOT EXCEED TOTAL CLASSROOM SPACE
        final String csv = ComputePopulationSize.compute(courses, SpurStudents.SPUR_FALL_DISTRIBUTION, rooms, 80, 160);
        final String userHome = System.getProperty("user.home");
        final File home = new File(userHome);
        final File target = new File(home, "FirstYearFallPopulationSim.csv");
        Log.info("Writing CSV file to ", target.getAbsolutePath());
        try (
                final FileWriter writer = new FileWriter(target, StandardCharsets.UTF_8)) {
            writer.write(csv);
        } catch (
                final IOException ex) {
            Log.warning("Failed to write file", ex);
        }

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
