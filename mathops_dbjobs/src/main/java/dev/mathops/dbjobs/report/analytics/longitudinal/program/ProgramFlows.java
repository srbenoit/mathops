package dev.mathops.dbjobs.report.analytics.longitudinal.program;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.StudentTermRec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.TreeMap;

/**
 * A utility class to analyze flows of students through programs.
 */
public final class ProgramFlows {

    /** A commonly used string array. */
    private static final List<String> MODS = Arrays.asList("MATH116", "MATH117", "MATH118", "MATH124", "MATH125",
            "MATH126", "MATH181A1");

    /** A commonly used string array. */
    private static final List<String> NONMODS = Arrays.asList("MATH101", "MATH105", "MATH120", "MATH127", "MATH141",
            "MATH155", "MATH156", "MATH157", "MATH159", "MATH160", "MATH161", "MATH255", "MATH256", "MATH261",
            "MATH340", "STAT100");

    /** A key to indicate the 1-credit courses. */
    private static final String MATHMODS = "MODS";

    /** A commonly used string array. */
    private static final String[][] M_124 = {{"MATH124"}};

    /** A commonly used string array. */
    private static final String[][] M_125 = {{"MATH125"}};

    /** A commonly used string array. */
    private static final String[][] M_124_125 = {{"MATH124", "MATH125"}};

    /** A commonly used string array. */
    private static final String[][] M_124_126 = {{"MATH124", "MATH126"}};

    /** A commonly used string array. */
    private static final String[][] M_141 = {{"MATH141"}};

    /** A commonly used string array. */
    private static final String[][] M_124_141 = {{"MATH124", "MATH141"}};

    /** A commonly used string array. */
    private static final String[][] M_141_OR_155_OR_160 = {{"MATH141"}, {"MATH155"}, {"MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M_155 = {{"MATH155"}};

    /** A commonly used string array. */
    private static final String[][] M_126_155 = {{"MATH126", "MATH155"}};

    /** A commonly used string array. */
    private static final String[][] M_155_OR_160 = {{"MATH155"}, {"MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M_156_OR_160 = {{"MATH156"}, {"MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M_160 = {{"MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M_161 = {{"MATH161"}};

    /** A commonly used string array. */
    private static final String[][] M_161_OR_271 = {{"MATH161"}, {"MATH271"}};

    /** A commonly used string array. */
    private static final String[][] M_255 = {{"MATH255"}};

    /** A commonly used string array. */
    private static final String[][] M_161_OR_255 = {{"MATH161"}, {"MATH255"}};

    /** A commonly used string array. */
    private static final String[][] M_256 = {{"MATH256"}};

    /** A commonly used string array. */
    private static final String[][] M_161_OR_256 = {{"MATH161"}, {"MATH256"}};

    /** A commonly used string array. */
    private static final String[][] M_256_OR_261 = {{"MATH256"}, {"MATH261"}};

    /** A commonly used string array. */
    private static final String[][] M_261 = {{"MATH261"}};

    /** A commonly used string array. */
    private static final String[][] M_261_OR_271 = {{"MATH261"}, {"MATH271"}};

    /** A commonly used string array. */
    private static final String[][] M_261_OR_272 = {{"MATH261"}, {"MATH272"}};

    /** A commonly used string array. */
    private static final String[][] M_340 = {{"MATH340"}};

    /** A decimal formatter. */
    private final DecimalFormat format;

    /** The directory in which to write CSV files. */
    private final File targetDir;

    /**
     * Constructs a new {@code ProgramFlows}.
     *
     * @param theTargetDir the directory in which to write CSV files
     */
    public ProgramFlows(final File theTargetDir) {

        this.format = new DecimalFormat("0.00");
        this.targetDir = theTargetDir;
    }

    /**
     * Analysis of student flows through programs
     *
     * @param studentsByTerminalCourse a map from terminal course (or pick list) key to the list of students with that
     *                                 requirement
     * @param enrollments              a map from student ID to the list of all enrollments for that student
     * @param studentTerms             a map from student ID to the list of all student term records for that student
     */
    public void generate(final Map<String, List<String>> studentsByTerminalCourse,
                         final Map<String, ? extends List<EnrollmentRec>> enrollments,
                         final Map<String, ? extends List<StudentTermRec>> studentTerms) {

        for (final Map.Entry<String, List<String>> entry : studentsByTerminalCourse.entrySet()) {
            final String key = entry.getKey();
            final List<String> studentIds = entry.getValue();

            final int count = studentIds.size();
            final String countStr = Integer.toString(count);
            Log.fine("Found ", countStr, " students whose terminal course is ", key);

            switch (key) {
                case AssembleProgramData.M_124:
                    generateTerminal("MATH 124", studentIds, enrollments, studentTerms, M_124);
                    break;
                case AssembleProgramData.M_125:
                    generateTerminal("MATH 124", studentIds, enrollments, studentTerms, M_125);
                    break;
                case AssembleProgramData.M_124_125:
                    generateTerminal("MATH 124 and 125", studentIds, enrollments, studentTerms, M_124_125);
                    break;
                case AssembleProgramData.M_124_126:
                    generateTerminal("MATH 124 and 126", studentIds, enrollments, studentTerms, M_124_126);
                    break;
                case AssembleProgramData.M_141:
                    generateTerminal("MATH 141", studentIds, enrollments, studentTerms, M_141);
                    break;
                case AssembleProgramData.M_124_141:
                    generateTerminal("MATH 124 and 141", studentIds, enrollments, studentTerms, M_124_141);
                    break;
                case AssembleProgramData.M_141_OR_155_OR_160:
                    generateTerminal("MATH 141 or 155 or 160", studentIds, enrollments, studentTerms,
                            M_141_OR_155_OR_160);
                    break;
                case AssembleProgramData.M_155:
                    generateTerminal("MATH 155", studentIds, enrollments, studentTerms, M_155);
                    break;
                case AssembleProgramData.M_126_155:
                    generateTerminal("MATH 126 and 155", studentIds, enrollments, studentTerms, M_126_155);
                    break;
                case AssembleProgramData.M_155_OR_160:
                    generateTerminal("MATH 155 or 160", studentIds, enrollments, studentTerms, M_155_OR_160);
                    break;
                case AssembleProgramData.M_156_OR_160:
                    generateTerminal("MATH 156 or 160", studentIds, enrollments, studentTerms, M_156_OR_160);
                    break;
                case AssembleProgramData.M_160:
                    generateTerminal("MATH 160", studentIds, enrollments, studentTerms, M_160);
                    break;
                case AssembleProgramData.M_161:
                    generateTerminal("MATH 161", studentIds, enrollments, studentTerms, M_161);
                    break;
                case AssembleProgramData.M_161_OR_271:
                    generateTerminal("MATH 161 or 271", studentIds, enrollments, studentTerms, M_161_OR_271);
                    break;
                case AssembleProgramData.M_255:
                    generateTerminal("MATH 255", studentIds, enrollments, studentTerms, M_255);
                    break;
                case AssembleProgramData.M_161_OR_255:
                    generateTerminal("MATH 161 or 255", studentIds, enrollments, studentTerms, M_161_OR_255);
                    break;
                case AssembleProgramData.M_256:
                    generateTerminal("MATH 256", studentIds, enrollments, studentTerms, M_256);
                    break;
                case AssembleProgramData.M_161_OR_256:
                    generateTerminal("MATH 161 or 256", studentIds, enrollments, studentTerms, M_161_OR_256);
                    break;
                case AssembleProgramData.M_256_OR_261:
                    generateTerminal("MATH 256 or 261", studentIds, enrollments, studentTerms, M_256_OR_261);
                    break;
                case AssembleProgramData.M_261:
                    generateTerminal("MATH 261", studentIds, enrollments, studentTerms, M_261);
                    break;
                case AssembleProgramData.M_261_OR_271:
                    generateTerminal("MATH 261 or  271", studentIds, enrollments, studentTerms, M_261_OR_271);
                    break;
                case AssembleProgramData.M_261_OR_272:
                    generateTerminal("MATH 261 or 272", studentIds, enrollments, studentTerms, M_261_OR_272);
                    break;
                case AssembleProgramData.M_340:
                    generateTerminal("MATH 340", studentIds, enrollments, studentTerms, M_340);
                    break;

                case AssembleProgramData.M3_117_118_120_124_141_155_160:
                    // TODO:
                    break;
                case AssembleProgramData.M3_117_118_120_124_125_126_127_141_155_160:
                    // TODO:
                    break;
                case AssembleProgramData.M3_118_124_125_126_155_160:
                    // TODO:
                    break;
                case AssembleProgramData.M3_117_118_125_141:
                    // TODO:
                    break;
            }
        }
    }

    /**
     * Analysis of student flows through a program that requires specific terminal Math courses.
     *
     * @param label        the label indicating the terminal course
     * @param studentIds   the list of students IDs
     * @param enrollments  a map from student ID to the list of all enrollments for that student
     * @param studentTerms a map from student ID to the list of all student term records for that student
     * @param courses      the lists of required terminal courses (each row is an option, all courses in a row need to
     *                     be completed)
     */
    private void generateTerminal(final String label, final Iterable<String> studentIds,
                                  final Map<String, ? extends List<EnrollmentRec>> enrollments,
                                  final Map<String, ? extends List<StudentTermRec>> studentTerms,
                                  final String[][] courses) {

        final List<List<EnrollmentRec>> enrollmentsBySemester = new ArrayList<>(20);
        final Map<List<String>, Integer> pathCounts = new TreeMap<>();

        final List<String> path = new ArrayList<>(10);
        final String startNode = SimpleBuilder.concat("Needs ", label);
        final List<String> allMathEnrollments = new ArrayList<>(10);

        for (final String studentId : studentIds) {

            final List<StudentTermRec> studentStudentTerms = studentTerms.get(studentId);
            if (studentStudentTerms == null || studentStudentTerms.isEmpty()) {
                continue;
            }

            final List<EnrollmentRec> studentEnrollments = enrollments.get(studentId);

            // Organize enrollments into one list for each term the student was enrolled (in term order)
            studentStudentTerms.sort(null);
            for (final StudentTermRec studentTerm : studentStudentTerms) {
                final int period = studentTerm.academicPeriod();

                final List<EnrollmentRec> termEnrollments = new ArrayList<>(4);
                enrollmentsBySemester.add(termEnrollments);

                for (final EnrollmentRec rec : studentEnrollments) {
                    if (rec.academicPeriod() == period) {
                        termEnrollments.add(rec);
                    }
                }
            }

            path.add(startNode);

            allMathEnrollments.clear();

            final int count = studentStudentTerms.size();
            boolean foundEnrollment = false;

            for (int i = 0; i < count; ++i) {
                final List<EnrollmentRec> termEnrollments = enrollmentsBySemester.get(i);

                if (termEnrollments.isEmpty()) {
                    continue;
                }

                final List<String> mathEnrollments = getMathEnrollment(termEnrollments);
                if (!mathEnrollments.isEmpty()) {
                    foundEnrollment = true;

                    final String newNode = getMathEnrollmentString(mathEnrollments, allMathEnrollments);
                    path.add(newNode);

                    allMathEnrollments.addAll(mathEnrollments);
                }
            }

            if (foundEnrollment) {
                if (didComplete(studentEnrollments, courses)) {
                    path.add("Completed");
                } else {
                    path.add("Did Not Complete");
                }

                incrementPathCount(pathCounts, path);
            }

            enrollmentsBySemester.clear();
            path.clear();
        }

        final HtmlBuilder builder = new HtmlBuilder(1000);

        for (final Map.Entry<List<String>, Integer> entry : pathCounts.entrySet()) {
            final List<String> key = entry.getKey();
            final Integer value = entry.getValue();
            builder.addln("[", value, "] ", key);
        }

        final String fileData = builder.toString();

        final String filename = label + " Program Flows.txt";
        final File file = new File(this.targetDir, filename);
        try (final FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write(fileData);
        } catch (final IOException ex) {
            Log.warning("Failed to write file", ex);
        }
    }

    /**
     * Increments the counter for a oath, initializing the counter if it has not yet been initialized.
     *
     * @param counts a map from transition name to its current count
     * @param path   the oath whose count to increment
     */
    private static void incrementPathCount(final Map<List<String>, Integer> counts, final List<String> path) {

        final Integer current = counts.get(path);

        if (current == null) {
            counts.put(path, Integer.valueOf(1));
        } else {
            counts.put(path, Integer.valueOf(current.intValue() + 1));
        }
    }

    /**
     * Tests whether a student completed required courses for a program.
     *
     * @param studentEnrollments the students complete enrollment record
     * @param courses            the lists of required terminal courses (each row is an option, all courses in a row
     *                           need to be completed)
     * @return true if the student completed at least one required set of courses; false if not
     */
    private static boolean didComplete(final List<EnrollmentRec> studentEnrollments, final String[][] courses) {

        boolean completed = false;

        for (int row = 0; row < courses.length; ++row) {
            boolean doneAll = true;
            for (final String course : courses[row]) {
                if (!didCompleteCourse(studentEnrollments, course)) {
                    doneAll = false;
                    break;
                }
            }

            if (doneAll) {
                completed = true;
                break;
            }
        }

        return completed;
    }

    /**
     * Tests whether a student completed a single course.
     *
     * @param studentEnrollments the students complete enrollment record
     * @param course             the  course
     * @return true if the student completed the course; false if not
     */
    private static boolean didCompleteCourse(final List<EnrollmentRec> studentEnrollments, final String course) {

        boolean completed = false;

        for (final EnrollmentRec rec : studentEnrollments) {
            if (rec.course().equals(course)) {
                if (rec.isPassed() || rec.isTransfer() || rec.isApIbClep()) {
                    completed = true;
                    break;
                }
            }
        }

        return completed;
    }

    /**
     * Gets a string representation of a students enrollments in a single semester.
     *
     * @param termEnrollments the enrollments
     * @return the list of math enrollments (coursed IDs)
     */
    private static List<String> getMathEnrollment(final Iterable<EnrollmentRec> termEnrollments) {

        boolean searchingForMods = true;
        final List<String> courses = new ArrayList<>(10);
        for (final EnrollmentRec rec : termEnrollments) {
            if (rec.isTransfer() || rec.isApIbClep()) {
                continue;
            }

            final String course = rec.course();
            if (course.startsWith("MATH")) {
                String toAdd = null;

                if (MODS.contains(course)) {
                    if (searchingForMods) {
                        toAdd = MATHMODS;
                        searchingForMods = false;
                    }
                } else if ("MATH180A3".equals(course)) {
                    toAdd = "MATH157";
                } else if ("MATH180A4".equals(course)) {
                    toAdd = "MATH159";
                } else if ("MATH180A5".equals(course)) {
                    toAdd = "MATH156";
                } else if (NONMODS.contains(course)) {
                    toAdd = course;
                }

                if (toAdd != null && !courses.contains(toAdd)) {
                    courses.add(toAdd);
                }
            }
        }

        courses.sort(null);

        return courses;
    }

    /**
     * Gets the string representation of a list of enrollments.
     *
     * @param courses            the list of math enrollments
     * @param allMathEnrollments the list of all enrollments from prior terms (used to append a "repeat-count" to course
     *                           IDs)
     * @return the string representation
     */
    private String getMathEnrollmentString(final Iterable<String> courses,
                                           final List<String> allMathEnrollments) {

        final HtmlBuilder builder = new HtmlBuilder(100);
        boolean comma = false;
        for (final String course : courses) {
            if (comma) {
                builder.add(",");
            }
            builder.add(course);
            final int count = count(course, allMathEnrollments);
            if (count > 0) {
                builder.add(".", Integer.toString(count + 1));
            }

            comma = true;
        }

        return builder.toString();
    }

    /**
     * Counts the number of times a student has previously taken a course.
     *
     * @param course      the course
     * @param pastCourses the list of all previously taken courses
     * @return the number of times {@code course} appears in {@code pastCourses}
     */
    private static int count(final String course, final List<String> pastCourses) {

        int count = 0;

        for (final String test : pastCourses) {
            if (test.equals(course)) {
                ++count;
            }
        }

        return count;
    }
}
