package dev.mathops.app.sim.schedule;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.courses.SpurCourses;
import dev.mathops.app.sim.campus.Room;
import dev.mathops.app.sim.campus.SpurRooms;
import dev.mathops.app.sim.students.SpurStudents;
import dev.mathops.commons.log.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simulation of the Spur first-year Spring semester.
 */
final class SpurFirstYearSpring {

    /**
     * Constructs a new {@code SpurFirstYearSpring}.
     */
    private SpurFirstYearSpring() {

        // No action
    }

    /**
     * Runs the simulation.
     */
    private static void runSimulation() {

        final List<Course> immutableCourses = Arrays.asList(SpurCourses.SEMINAR, SpurCourses.CO150,
                SpurCourses.SOC220, SpurCourses.LIFE103, SpurCourses.CHEM111, SpurCourses.MIP101, SpurCourses.IDEA210,
                SpurCourses.CS201, SpurCourses.HISTORY, SpurCourses.IU173, SpurCourses.IU174);
        final List<Course> courses = new ArrayList<>(immutableCourses);

        final List<Room> rooms = List.of(SpurRooms.CLASSROOM_1, SpurRooms.CLASSROOM_2, SpurRooms.LAB_1);

        // SIMULATION PART 1 - DETERMINE MAXIMUM POSSIBLE POPULATION SIZE THAT DOES NOT EXCEED TOTAL CLASSROOM SPACE
        final String csv = ComputePopulationSize.compute(courses, SpurStudents.SPUR_SPRING_DISTRIBUTION, rooms, 80,
                160);
        final String userHome = System.getProperty("user.home");
        final File home = new File(userHome);
        final File target = new File(home, "FirstYearSpringPopulationSim.csv");
        Log.info("Writing CSV file to ", target.getAbsolutePath());
        try (final FileWriter writer = new FileWriter(target, StandardCharsets.UTF_8)) {
            writer.write(csv);
        } catch (final IOException ex) {
            Log.warning("Failed to write file", ex);
        }

        // SIMULATION PART 2 - Try to build an assignment of courses to sections across classrooms and labs
//        final StudentPopulation population160 = new StudentPopulation(SpurStudents.SPUR_SPRING_DISTRIBUTION, 160);
//        final Map<Course, Integer> seatCounts = ComputeSectionsNeeded.compute(courses, population160, rooms);
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
