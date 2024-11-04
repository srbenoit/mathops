package dev.mathops.dbjobs.report.analytics.longitudinal.program;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.StudentTermRec;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;

/**
 * A utility class to analyze flows of students through programs.
 */
public final class ProgramFlows {

    /** A commonly used string array. */
    private static final List<String> MODS = Arrays.asList("MATH117", "MATH118", "MATH124", "MATH125", "MATH126");

    /** A key to indicate the 1-credit courses. */
    private static final String MATHMODS = "MATHMODS";

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
                    generateTerminal("MATH 124 and MATH 125", studentIds, enrollments, studentTerms, M_124_125);
                    break;
                case AssembleProgramData.M_124_126:
                    generateTerminal("MATH 124 and MATH 126", studentIds, enrollments, studentTerms, M_124_126);
                    break;
                case AssembleProgramData.M_141:
                    generateTerminal("MATH 141", studentIds, enrollments, studentTerms, M_141);
                    break;
                case AssembleProgramData.M_124_141:
                    generateTerminal("MATH 124 and MATH 141", studentIds, enrollments, studentTerms, M_124_141);
                    break;
                case AssembleProgramData.M_141_OR_155_OR_160:
                    generateTerminal("MATH 141 or MATH 155 or MATH 160", studentIds, enrollments, studentTerms,
                            M_141_OR_155_OR_160);
                    break;
                case AssembleProgramData.M_155:
                    generateTerminal("MATH 155", studentIds, enrollments, studentTerms, M_155);
                    break;
                case AssembleProgramData.M_126_155:
                    generateTerminal("MATH 126 and MATH 155", studentIds, enrollments, studentTerms, M_126_155);
                    break;
                case AssembleProgramData.M_155_OR_160:
                    generateTerminal("MATH 155 or MATH 160", studentIds, enrollments, studentTerms, M_155_OR_160);
                    break;
                case AssembleProgramData.M_156_OR_160:
                    generateTerminal("MATH 156 or MATH 160", studentIds, enrollments, studentTerms, M_156_OR_160);
                    break;
                case AssembleProgramData.M_160:
                    generateTerminal("MATH 160", studentIds, enrollments, studentTerms, M_160);
                    break;
                case AssembleProgramData.M_161:
                    generateTerminal("MATH 161", studentIds, enrollments, studentTerms, M_161);
                    break;
                case AssembleProgramData.M_161_OR_271:
                    generateTerminal("MATH 161 or MATH 271", studentIds, enrollments, studentTerms, M_161_OR_271);
                    break;
                case AssembleProgramData.M_255:
                    generateTerminal("MATH 255", studentIds, enrollments, studentTerms, M_255);
                    break;
                case AssembleProgramData.M_161_OR_255:
                    generateTerminal("MATH 161 or MATH 255", studentIds, enrollments, studentTerms, M_161_OR_255);
                    break;
                case AssembleProgramData.M_256:
                    generateTerminal("MATH 256", studentIds, enrollments, studentTerms, M_256);
                    break;
                case AssembleProgramData.M_161_OR_256:
                    generateTerminal("MATH 161 or MATH 256", studentIds, enrollments, studentTerms, M_161_OR_256);
                    break;
                case AssembleProgramData.M_256_OR_261:
                    generateTerminal("MATH 256 or MATH 261", studentIds, enrollments, studentTerms, M_256_OR_261);
                    break;
                case AssembleProgramData.M_261:
                    generateTerminal("MATH 261", studentIds, enrollments, studentTerms, M_261);
                    break;
                case AssembleProgramData.M_261_OR_271:
                    generateTerminal("MATH 261 or MATH 271", studentIds, enrollments, studentTerms, M_261_OR_271);
                    break;
                case AssembleProgramData.M_261_OR_272:
                    generateTerminal("MATH 261 or MATH 272", studentIds, enrollments, studentTerms, M_261_OR_272);
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
        final List<String> semesterNames = new ArrayList<>(20);
        final Map<String, Integer> transitions = new HashMap<>(100);

        final String startNode = SimpleBuilder.concat("Needs ", label);

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

                final List<EnrollmentRec> termEnrollments = new ArrayList(4);
                enrollmentsBySemester.add(termEnrollments);

                for (final EnrollmentRec rec : studentEnrollments) {
                    if (rec.academicPeriod() == period) {
                        termEnrollments.add(rec);
                    }
                }
            }

            // There will be a collection of nodes: a start node like "Needs MATH 340", intermediate nodes like
            // "MATH 160, Semester 3" and two terminal nodes: "Completed" or "Did Not Complete".  We need to classify
            // students into nodes and gather counts for numbers that transitioned between nodes.
            computeSemesterNames(studentStudentTerms, semesterNames);

            String node = startNode;

            final int count = studentStudentTerms.size();
            for (int i = 0; i < count; ++i) {
                final List<EnrollmentRec> termEnrollments = enrollmentsBySemester.get(i);

                if (termEnrollments.isEmpty()) {
                    continue;
                }

                final String mathEnrollment = getMathEnrollment(termEnrollments);
                if (Objects.nonNull(mathEnrollment)) {
                    final String semesterName = semesterNames.get(i);

                    final String newNode = mathEnrollment + "." + semesterName;
                    final String transition = node + "~" + newNode;
                    incrementTransitionCount(transitions, transition);

                    node = newNode;
                }
            }

            if (didComplete(studentEnrollments, courses)) {
                final String transition = node + "~Completed";
                incrementTransitionCount(transitions, transition);
            } else {
                final String transition = node + "~Did Not Complete";
                incrementTransitionCount(transitions, transition);
            }

            enrollmentsBySemester.clear();
        }

        Log.info("Flows: ", label);

        // Print out the flows
        for (final Map.Entry<String, Integer> entry : transitions.entrySet()) {
            Log.info("    ", entry.getValue(), " did ", entry.getKey());
        }
    }

    /**
     * Increments the counter for a transition, initializing the counter if it has not yet been initialized.
     *
     * @param transitions a map from transition name to its current count
     * @param transition  the transition name whose count to increment
     */
    private static void incrementTransitionCount(final Map<String, Integer> transitions, final String transition) {

        final Integer current = transitions.get(transition);

        if (current == null) {
            transitions.put(transition, Integer.valueOf(1));
        } else {
            transitions.put(transition, Integer.valueOf(current.intValue() + 1));
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
     * Given a list of student terms, generates a corresponding list of the names of these terms, like "Fall 1", or
     * "Spring 3".
     *
     * @param studentTerms  the list of student terms
     * @param semesterNames a list to populate with semester names
     */
    private static void computeSemesterNames(final SequencedCollection<StudentTermRec> studentTerms,
                                             final Collection<? super String> semesterNames) {

        // Academic year 2023 is Fall 2023, Spring 2024, Summer 2024

        final int startPeriod = studentTerms.getFirst().academicPeriod();
        final int startYear = startPeriod / 100;
        final int startTerm = startPeriod % 100;
        final int startAcademicYear = startTerm == 90 ? startYear : (startYear + 1);

        for (final StudentTermRec rec : studentTerms) {
            final int period = rec.academicPeriod();
            final int year = period / 100;
            final int term = period % 100;
            final int academicYear = term == 90 ? year : (year + 1);

            final int yearIndex = academicYear - startAcademicYear + 1;
            final String name = (term < 30) ? "SP " + yearIndex : (term < 75) ? "SM " + yearIndex : "FA " + yearIndex;
            semesterNames.add(name);
        }
    }

    /**
     * Gets a string representation of a students enrollments in a single semester
     *
     * @param termEnrollments the enrollments
     * @return the name of the math enrollment; null if none
     */
    private static String getMathEnrollment(final Iterable<EnrollmentRec> termEnrollments) {

        boolean searchingForMods = true;
        final List<String> courses = new ArrayList<>(10);
        for (final EnrollmentRec rec : termEnrollments) {
            if (rec.isTransfer() || rec.isApIbClep()) {
                continue;
            }

            final String course = rec.course();
            if (course.startsWith("MATH")) {
                if (MODS.contains(course)) {
                    if (searchingForMods) {
                        courses.add(MATHMODS);
                        searchingForMods = false;
                    }
                } else if ("MATH180A3".equals(course)) {
                    courses.add("MATH157");
                } else if ("MATH180A4".equals(course)) {
                    courses.add("MATH159");
                } else if ("MATH180A5".equals(course)) {
                    courses.add("MATH156");
                } else if ("MATH181A1".equals(course)) {
                    courses.add("MATH116");
                } else {
                    courses.add(course);
                }
            }
        }

        String name = null;

        final int count = courses.size();
        if (count == 1) {
            name = courses.getFirst();
        } else if (count > 1) {
            courses.sort(null);
            final String first = courses.getFirst();

            final StringBuilder builder = new StringBuilder(100);
            builder.append(first);
            for (int i = 1; i < count; ++i) {
                builder.append(",");
                builder.append(courses.get(i));
            }
            name = builder.toString();
        }

        return name;
    }
}
