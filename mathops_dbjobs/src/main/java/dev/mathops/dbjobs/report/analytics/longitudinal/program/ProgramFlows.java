package dev.mathops.dbjobs.report.analytics.longitudinal.program;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.StudentTermRec;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A utility class to analyze flows of students through programs.
 */
public final class ProgramFlows {

    /** A commonly used string array. */
    private static final String[][] M_124 = new String[][]{{"MATH124"}};

    /** A commonly used string array. */
    private static final String[][] M_125 = new String[][]{{"MATH125"}};

    /** A commonly used string array. */
    private static final String[][] M_124_125 = new String[][]{{"MATH124", "MATH125"}};

    /** A commonly used string array. */
    private static final String[][] M_124_126 = new String[][]{{"MATH124", "MATH126"}};

    /** A commonly used string array. */
    private static final String[][] M_141 = new String[][]{{"MATH141"}};

    /** A commonly used string array. */
    private static final String[][] M_124_141 = new String[][]{{"MATH124", "MATH141"}};

    /** A commonly used string array. */
    private static final String[][] M_141_OR_155_OR_160 = new String[][]{{"MATH141"}, {"MATH155"}, {"MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M_155 = new String[][]{{"MATH155"}};

    /** A commonly used string array. */
    private static final String[][] M_126_155 = new String[][]{{"MATH126", "MATH155"}};

    /** A commonly used string array. */
    private static final String[][] M_155_OR_160 = new String[][]{{"MATH155"}, {"MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M_156_OR_160 = new String[][]{{"MATH156"}, {"MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M_160 = new String[][]{{"MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M_161 = new String[][]{{"MATH161"}};

    /** A commonly used string array. */
    private static final String[][] M_161_OR_271 = new String[][]{{"MATH161"}, {"MATH271"}};

    /** A commonly used string array. */
    private static final String[][] M_255 = new String[][]{{"MATH255"}};

    /** A commonly used string array. */
    private static final String[][] M_161_OR_255 = new String[][]{{"MATH161"}, {"MATH255"}};

    /** A commonly used string array. */
    private static final String[][] M_256 = new String[][]{{"MATH256"}};

    /** A commonly used string array. */
    private static final String[][] M_161_OR_256 = new String[][]{{"MATH161"}, {"MATH256"}};

    /** A commonly used string array. */
    private static final String[][] M_256_OR_261 = new String[][]{{"MATH256"}, {"MATH261"}};

    /** A commonly used string array. */
    private static final String[][] M_261 = new String[][]{{"MATH261"}};

    /** A commonly used string array. */
    private static final String[][] M_261_OR_271 = new String[][]{{"MATH261"}, {"MATH271"}};

    /** A commonly used string array. */
    private static final String[][] M_261_OR_272 = new String[][]{{"MATH261"}, {"MATH272"}};

    /** A commonly used string array. */
    private static final String[][] M_340 = new String[][]{{"MATH340"}};

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
    private void generateTerminal(final String label, final List<String> studentIds,
                                  final Map<String, ? extends List<EnrollmentRec>> enrollments,
                                  final Map<String, ? extends List<StudentTermRec>> studentTerms,
                                  final String[][] courses) {

        final List<List<EnrollmentRec>> enrollmentsBySemester = new ArrayList<>(20);
        final List<String> semesterNames = new ArrayList<>(20);
        final Map<String, Integer> transitions = new HashMap<>(100);

        final Map<String, Integer> flows = new HashMap<>(100);
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
            // "MATH 117, Semester 3" and two terminal nodes: "Completed" or "Did Not Complete".  We need to classify
            // students into nodes and gather counts for numbers that transitioned between nodes.
            final int firstTerm = studentStudentTerms.getFirst().academicPeriod();
            final int lastTerm = studentStudentTerms.getLast().academicPeriod();
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
                    final StudentTermRec studentTerm = studentStudentTerms.get(i);
                    final String semesterName = semesterNames.get(i);

                    final String newNode = mathEnrollment + "." + semesterName;
                    final String transition = node + "~" + newNode;
                    final Integer current = transitions.get(transition);
                    if (current == null) {
                        transitions.put(transition, Integer.valueOf(1));
                    } else {
                        transitions.put(transition, Integer.valueOf(current.intValue() + 1));
                    }
                }
            }

            enrollmentsBySemester.clear();
        }

        Log.info("Flows: ", label);

        // Print out the flows
    }

    /**
     * Given a list of student terms, generates a corresponding list of the names of these terms, like "Fall 1", or
     * "Spring 3".
     *
     * @param studentTerms  the list of student terms
     * @param semesterNames a list to populate with semester names
     */
    private static void computeSemesterNames(final List<StudentTermRec> studentTerms,
                                             final List<? super String> semesterNames) {

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
     * Tests whether the student has a math enrollment in a list of enrollments.
     *
     * @param termEnrollments the enrollments
     * @return the name of the math enrollment; null if none
     */
    private static String getMathEnrollment(final List<EnrollmentRec> termEnrollments) {

        String name;

        int count = 0;
        for (final EnrollmentRec rec : termEnrollments) {
            if (rec.course().startsWith("MATH")) {
                ++count;
                name = rec.course();
            }
        }

        if (count > 1) {
            // name holds the last one found, but there were more than 1, might be "MATH 117 + MATH 118"
        }

        return name;
    }
}
