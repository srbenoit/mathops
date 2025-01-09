package dev.mathops.dbjobs.report.analytics.longitudinal.program;

import dev.mathops.commons.log.Log;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.StudentTermRec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    /** A commonly used integer. */
    private static final Integer ONE = Integer.valueOf(1);

    /** A key to indicate the 1-credit courses. */
    private static final String MATHMODS = "(MODS)";

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

        final Set<String> allStudentIds = new HashSet<>(100000);

        for (final Map.Entry<String, List<String>> entry : studentsByTerminalCourse.entrySet()) {
            final String key = entry.getKey();
            final List<String> studentIds = entry.getValue();

            allStudentIds.addAll(studentIds);

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

        final Iterable<String> allStudentIdsList = new ArrayList<>(allStudentIds);
        generateTerminal("Overall", allStudentIdsList, enrollments, studentTerms, null);
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
        final Map<String, Path> paths = new TreeMap<>();

        final List<String> pathCourses = new ArrayList<>(10);
        final String startNode = SimpleBuilder.concat("Needs ", label);
        final List<String> allMathEnrollments = new ArrayList<>(10);

        final Map<Integer, Integer> totalCreditsHistogram = new TreeMap<>();
        final Map<Integer, Integer> failedCreditsHistogram = new TreeMap<>();
        final Map<Integer, Integer> totalSemestersHistogram = new TreeMap<>();
        final Set<Integer> termsWithMath = new HashSet<>();

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

            pathCourses.add(startNode);

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
                    pathCourses.add(newNode);

                    allMathEnrollments.addAll(mathEnrollments);
                }
            }

            if (foundEnrollment) {
                // This student took MATH...
                termsWithMath.clear();
                int mathCredits = 0;
                int failedCredits = 0;

                for (final EnrollmentRec enrollment : studentEnrollments) {
                    if (enrollment.isApIbClep() || enrollment.isTransfer()) {
                        continue;
                    }
                    termsWithMath.add(Integer.valueOf(enrollment.academicPeriod()));

                    final String course = enrollment.course();
                    final int courseCredits;

                    if (enrollment.course().startsWith("MATH")) {
                        if ("MATH 117".equals(course)
                            || "MATH118".equals(course)
                            || "MATH124".equals(course)
                            || "MATH125".equals(course)
                            || "MATH126".equals(course)
                            || "MATH116".equals(course) || "MATH181A1".equals(course)
                            || "MATH151".equals(course)
                            || "MATH152".equals(course)
                            || "MATH158".equals(course)
                            || "MATH192".equals(course)
                            || "MATH384".equals(course)) {
                            courseCredits = 1;
                        } else if ("MATH229".equals(course)
                                   || "MATH269".equals(course)
                                   || "MATH235".equals(course)) {
                            courseCredits = 2;
                        } else if ("MATH127".equals(course)
                                   || "MATH155".equals(course)
                                   || "MATH156".equals(course) || "MATH180A5".equals(course)
                                   || "MATH160".equals(course)
                                   || "MATH161".equals(course)
                                   || "MATH255".equals(course)
                                   || "MATH256".equals(course)
                                   || "MATH261".equals(course)
                                   || "MATH271".equals(course)
                                   || "MATH272".equals(course)
                                   || "MATH340".equals(course)
                                   || "MATH345".equals(course)
                                   || "MATH348".equals(course)) {
                            courseCredits = 4;
                        } else {
                            courseCredits = 3;
                        }

                        mathCredits += courseCredits;
                        if (enrollment.isPassed()) {
                            failedCredits += courseCredits;
                        }
                    }
                }

                // Update histogram tracking number of terms in which math was taken
                final int numTermsWithMath = termsWithMath.size();
                final Integer numTermsValue = Integer.valueOf(numTermsWithMath);
                final Integer existingNumTermsCount = totalSemestersHistogram.get(numTermsValue);
                if (existingNumTermsCount == null) {
                    totalSemestersHistogram.put(numTermsValue, ONE);
                } else {
                    final Integer newValue = Integer.valueOf(existingNumTermsCount.intValue() + 1);
                    totalSemestersHistogram.put(numTermsValue, newValue);
                }

                // Update histogram tracking number of MATH credits
                final Integer numCreditsValue = Integer.valueOf(mathCredits);
                final Integer existingNumCreditsCount = totalCreditsHistogram.get(numCreditsValue);
                if (existingNumCreditsCount == null) {
                    totalCreditsHistogram.put(numCreditsValue, ONE);
                } else {
                    final Integer newValue = Integer.valueOf(existingNumCreditsCount.intValue() + 1);
                    totalCreditsHistogram.put(numCreditsValue, newValue);
                }

                // Update histogram tracking number of failed MATH credits
                final Integer numFailedValue = Integer.valueOf(failedCredits);
                final Integer existingNumFailedCount = failedCreditsHistogram.get(numFailedValue);
                if (existingNumFailedCount == null) {
                    failedCreditsHistogram.put(numFailedValue, ONE);
                } else {
                    final Integer newValue = Integer.valueOf(existingNumFailedCount.intValue() + 1);
                    failedCreditsHistogram.put(numFailedValue, newValue);
                }

                final Path path = new Path(pathCourses);
                final String key = path.getKey();
                final Path existing = paths.get(key);
                final boolean completed = courses != null && isFinished(studentEnrollments, courses);

                if (existing == null) {
                    path.incrementCount(completed);

                    // Ensure parentage paths exist
                    Path current = path;
                    String parentKey = current.makeParentKey();

                    while (paths.get(parentKey) == null) {
                        final Path parent = current.makeParentPath();
                        final String actualParentKey = parent.getKey();
                        if (!parentKey.equals(actualParentKey)) {
                            final String currentKey = current.getKey();
                            Log.warning("Computed and actual parent keys differ: '", parentKey, "'/'", actualParentKey,
                                    "', child is '", currentKey, "'");
                        }
                        paths.put(parentKey, parent);
                        current = parent;
                        parentKey = parent.makeParentKey();
                    }

                    paths.put(key, path);
                } else {
                    existing.incrementCount(completed);
                }
            }

            enrollmentsBySemester.clear();
            pathCourses.clear();
        }

        // Organize paths by parent/child relationship

        final Collection<Path> pathsValues = paths.values();
        final List<Path> allPaths = new ArrayList<>(pathsValues);
        allPaths.sort(null);

        final int totalPaths = allPaths.size();
        int totalCompleted = 0;
        int totalNotCompleted = 0;

        for (final Path path : allPaths) {
            totalCompleted += path.getNumCompleted();
            totalNotCompleted += path.getNumDidNotComplete();
        }

        final int overallTotal = totalCompleted + totalNotCompleted;

        final Collection<Path> toplevel = new ArrayList<>(10);
        for (int i = totalPaths - 1; i >= 0; --i) {
            final Path path = allPaths.get(i);
            if (path.size() <= 2) {
                toplevel.add(path);
                allPaths.remove(i);
            }
        }

        for (final Path path : toplevel) {
            path.collectChildPaths(allPaths);
        }

        for (final Path path : allPaths) {
            final String pathKey = path.getKey();
            Log.warning("A path was found with no parent: ", pathKey);
        }

        final HtmlBuilder builder = new HtmlBuilder(1000);
        final String overallTotalStr = Integer.toString(overallTotal);
        builder.addln(overallTotalStr, " students analyzed whose program needs ", label);
        builder.addln();

        // Emit statistics

        final String totalCompletedStr = Integer.toString(totalCompleted);
        final String totalNotCompletedStr = Integer.toString(totalNotCompleted);
        builder.addln(totalCompletedStr, " completed initial requirements; ", totalNotCompletedStr, " did not");
        builder.addln();

        builder.addln("Counts of students based on number of semesters in which MATH was taken:");
        for (final Map.Entry<Integer, Integer> entry : totalSemestersHistogram.entrySet()) {
            builder.addln("    ", entry.getKey(), " semesters of MATH: ", entry.getValue(), " students");
        }
        builder.addln();

        builder.addln("Counts of students based on number of credits of MATH taken:");
        for (final Map.Entry<Integer, Integer> entry : totalCreditsHistogram.entrySet()) {
            builder.addln("    ", entry.getKey(), " credits of MATH taken: ", entry.getValue(), " students");
        }
        builder.addln();

        builder.addln("Counts of students based on number of credits of MATH failed:");
        for (final Map.Entry<Integer, Integer> entry : failedCreditsHistogram.entrySet()) {
            builder.addln("    ", entry.getKey(), " credits of MATH failed: ", entry.getValue(), " students");
        }
        builder.addln();

        // TODO: Input and output majors for Sankey diagram

        // Emit paths
        if (courses != null) {
            for (final Path top : toplevel) {
                if (top.size() > 1) {
                    emitPath(builder, top, 0);
                }
            }
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
     * Emits a line for a path to an {@code HtmlBuilder}.
     *
     * @param builder the {@code HtmlBuilder} to which to append
     * @param path    the path
     * @param indent  the indentation level that shows parent/child relationships
     */
    private static void emitPath(final HtmlBuilder builder, final Path path, final int indent) {

        builder.indent(indent * 2);

        final String entryKey = path.getKey();

        final int completed = path.getNumCompleted();
        final String completedStr = Integer.toString(completed);

        final int didNotComplete = path.getNumDidNotComplete();
        final String didNotCompleteStr = Integer.toString(didNotComplete);

        final int count = completed + didNotComplete;
        final String countStr = Integer.toString(count);

        final int total = path.getTotalCount();
        final String totalStr = Integer.toString(total);

        builder.addln("[", countStr, "/", totalStr, "] ", entryKey, " (", completedStr, " FINISHED, ",
                didNotCompleteStr, " EXITED)");

        final int numChildren = path.getNumChildren();
        for (int i = 0; i < numChildren; ++i) {
            final Path child = path.getChild(i);
            emitPath(builder, child, indent + 1);
        }
    }

    /**
     * Tests whether a student finished required courses for a program.
     *
     * @param studentEnrollments the students complete enrollment record
     * @param courses            the lists of required terminal courses (each row is an option, all courses in a row
     *                           need to be completed)
     * @return true if the student completed at least one required list of courses; false if not
     */
    private static boolean isFinished(final Iterable<EnrollmentRec> studentEnrollments, final String[][] courses) {

        boolean completed = false;

        for (final String[] courseArray : courses) {
            boolean doneAll = true;
            for (final String course : courseArray) {
                if (!isCourseFinished(studentEnrollments, course)) {
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
    private static boolean isCourseFinished(final Iterable<EnrollmentRec> studentEnrollments, final String course) {

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
    private static String getMathEnrollmentString(final Iterable<String> courses,
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
                final String countStr = Integer.toString(count + 1);
                builder.add(".", countStr);
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
