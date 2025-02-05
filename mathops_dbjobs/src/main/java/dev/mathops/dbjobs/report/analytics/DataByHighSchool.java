package dev.mathops.dbjobs.report.analytics;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.IProgressListener;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawHighSchoolsLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawHighSchools;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A report that takes a list of high school codes (CEEB codes), and for each school in the list, collects a list of
 * students from that school, and assembles their average standardized test scores, placement outcomes and Precalculus
 * course outcomes.
 */
final class DataByHighSchool {

    /** The data cache. */
    private final Cache cache;

    /** An optional progress listener. */
    private final IProgressListener listener;

    /** Flag that can cancel an in-progress scan. */
    private final AtomicBoolean canceled;

    /**
     * Constructs a new {@code DataByHighSchool}.
     *
     * @param theCache    the data cache
     * @param theListener an optional progress listener
     */
    private DataByHighSchool(final Cache theCache, final IProgressListener theListener) {

        this.cache = theCache;
        this.listener = theListener;
        this.canceled = new AtomicBoolean(false);
    }

    /**
     * Cancels the operations.
     */
    public void cancel() {

        this.canceled.set(true);
    }

    /**
     * Writes a log record to the console and the report log.
     *
     * @param reportLog the report log
     * @param data      the data to write
     */
    private static void log(final HtmlBuilder reportLog, final Object... data) {

        final String concatenation = SimpleBuilder.concat(data);
        Log.fine(concatenation);

        reportLog.addln(concatenation);
    }

    /**
     * Executes the job.
     *
     * @param highSchoolCodes list of high school codes to include in the report
     */
    private void calculate(final Collection<String> highSchoolCodes) {

        // In terms of progress, the first 1% will be loading data, then next 99 will be scanning students.

        final HtmlBuilder reportLog = new HtmlBuilder(1000);

        final File reports = new File("/opt/zircon/reports");
        this.canceled.set(false);

        try {
            log(reportLog, "Querying all students");
            final List<RawStudent> allStudents = RawStudentLogic.queryAll(this.cache);
            final int numStudents = allStudents.size();
            final String numStudentsStr = Integer.toString(numStudents);
            log(reportLog, "    Loaded ", numStudentsStr, " student records.");
            log(reportLog, CoreConstants.EMPTY);

            log(reportLog, "Querying all high schools");
            final List<RawHighSchools> allHs = RawHighSchoolsLogic.queryAll(this.cache);
            final int numHs = allHs.size();
            final String numHsStr = Integer.toString(numHs);
            log(reportLog, "    Loaded ", numHsStr, " high school.");
            log(reportLog, CoreConstants.EMPTY);

            // Gather performance data for all students (!) so we can get global averages
            final List<StudentPerformanceData> performanceData = calculatePerformance(allStudents, reportLog);

            // Compute the average placement performance over all students
            final AveragePerformance global = calculateAveragePerformance(performanceData, reportLog);

            // Categorize students in the list of high schools of interest
            final Map<String, List<StudentPerformanceData>> hsStudents = categorizeStudents(performanceData,
                    highSchoolCodes);

            final int numHighSchools = hsStudents.size();
            final Map<String, AveragePerformance> averages = new HashMap<>(numHighSchools);

            for (final Map.Entry<String, List<StudentPerformanceData>> entry : hsStudents.entrySet()) {
                final String hsCode = entry.getKey();
                final List<StudentPerformanceData> studentsFromHs = entry.getValue();

                final AveragePerformance hsAverage = processHighSchool(hsCode, studentsFromHs, reportLog);
                averages.put(hsCode, hsAverage);
            }

            final String reportLogStr = reportLog.toString();
            writeReport(new File(reports, "performance_by_high_school.log"), reportLogStr);

            generateCsv(reports, global, allHs, averages);
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Generates the CSV file report output.
     *
     * @param dir           the directory in which to write the file
     * @param globalAverage the global average performance
     * @param allHs         the list of all high school records
     * @param hsAverages    the per-high school averages
     */
    private static void generateCsv(final File dir, final AveragePerformance globalAverage,
                                    final Iterable<RawHighSchools> allHs,
                                    final Map<String, AveragePerformance> hsAverages) {

        final HtmlBuilder reportCsv = new HtmlBuilder(1000);

        reportCsv.addln("HS Code,HS Name,N,Raw Elem,Raw Alg I,Raw Alg II,Raw Logs & Exp,Raw Trig I,Raw Trig II,",
                "Rel Elem,Rel Alg I,Rel Alg II,Rel Logs & Exp,Rel Trig I,Rel Trig II,Rating");

        for (final Map.Entry<String, AveragePerformance> hsEntry : hsAverages.entrySet()) {
            final String hsCode = hsEntry.getKey();

            String hsName = CoreConstants.EMPTY;
            for (final RawHighSchools test : allHs) {
                if (test.hsCode.equals(hsCode)) {
                    hsName = test.hsName;
                    break;
                }
            }

            if (hsName == null) {
                Log.warning("No high school found with code '", hsCode, "'");
            }

            reportCsv.add(hsCode, CoreConstants.COMMA, hsName, CoreConstants.COMMA);

            final AveragePerformance perf = hsEntry.getValue();
            final int n = perf.n();
            if (n > 0) {
                final String nStr = Integer.toString(n);
                final float stsA = perf.stsA();
                final String staAStr = Float.toString(stsA);
                final float sts117 = perf.sts117();
                final String sta117Str = Float.toString(sts117);
                final float sts118 = perf.sts118();
                final String sta118Str = Float.toString(sts118);
                final float sts124 = perf.sts124();
                final String sta124Str = Float.toString(sts124);
                final float sts125 = perf.sts125();
                final String sta125Str = Float.toString(sts125);
                final float sts126 = perf.sts126();
                final String sta126Str = Float.toString(sts126);

                final float relA = globalAverage.stsA() - perf.stsA();
                final String relAStr = Float.toString(relA);
                final float rel117 = globalAverage.sts117() - perf.sts117();
                final String rel117Str = Float.toString(rel117);
                final float rel118 = globalAverage.sts118() - perf.sts118();
                final String rel118Str = Float.toString(rel118);
                final float rel124 = globalAverage.sts124() - perf.sts124();
                final String rel124Str = Float.toString(rel124);
                final float rel125 = globalAverage.sts125() - perf.sts125();
                final String rel125Str = Float.toString(rel125);
                final float rel126 = globalAverage.sts126() - perf.sts126();
                final String rel126Str = Float.toString(rel126);

                final float rating = (relA + rel117 + rel118 + rel124 + rel125 + rel126) / 6.0f;
                final String ratingStr = Float.toString(rating);

                reportCsv.addln(nStr, CoreConstants.COMMA, staAStr, CoreConstants.COMMA, sta117Str,
                        CoreConstants.COMMA, sta118Str, CoreConstants.COMMA, sta124Str, CoreConstants.COMMA,
                        sta125Str, CoreConstants.COMMA, sta126Str, CoreConstants.COMMA, relAStr, CoreConstants.COMMA,
                        rel117Str, CoreConstants.COMMA, rel118Str, CoreConstants.COMMA, rel124Str, CoreConstants.COMMA,
                        rel125Str, CoreConstants.COMMA, rel126Str, CoreConstants.COMMA, ratingStr);
            } else {
                reportCsv.addln("0,,,,,,,,,,,,,");
            }
        }

        final int n = globalAverage.n();
        final String nStr = Integer.toString(n);
        final float stsA = globalAverage.stsA();
        final String staAStr = Float.toString(stsA);
        final float sts117 = globalAverage.sts117();
        final String sta117Str = Float.toString(sts117);
        final float sts118 = globalAverage.sts118();
        final String sta118Str = Float.toString(sts118);
        final float sts124 = globalAverage.sts124();
        final String sta124Str = Float.toString(sts124);
        final float sts125 = globalAverage.sts125();
        final String sta125Str = Float.toString(sts125);
        final float sts126 = globalAverage.sts126();
        final String sta126Str = Float.toString(sts126);

        reportCsv.addln(",Overall", CoreConstants.COMMA, nStr, CoreConstants.COMMA, staAStr, CoreConstants.COMMA,
                sta117Str, CoreConstants.COMMA, sta118Str, CoreConstants.COMMA, sta124Str, CoreConstants.COMMA,
                sta125Str, CoreConstants.COMMA, sta126Str, ",,,,,,,");

        final String reportCsvStr = reportCsv.toString();
        writeReport(new File(dir, "performance_by_high_school.csv"), reportCsvStr);
    }

    /**
     * Calculates placement performance data for all students who have completed math placement.
     *
     * @param allStudents the list of all students
     * @param reportLog   a {@code HtmlBuilder} to which to write messages to the report log
     * @return performance data for all students for whom we have placement data
     * @throws SQLException if there is an error accessing the database
     */
    private List<StudentPerformanceData> calculatePerformance(final Iterable<RawStudent> allStudents,
                                                              final HtmlBuilder reportLog) throws SQLException {

        log(reportLog, "Querying all placement attempt records");
        final List<RawStmpe> allStmpe = RawStmpeLogic.queryAll(this.cache);
        final int numPlacementAttempts = allStmpe.size();
        final String numPlacementAttemptsStr = Integer.toString(numPlacementAttempts);
        log(reportLog, "    Loaded ", numPlacementAttemptsStr, " placement attempts records.");
        log(reportLog, CoreConstants.EMPTY);

        log(reportLog, "Scanning for distinct students who have attempted placement");
        final Collection<String> studentsWithPlacement = new HashSet<>(numPlacementAttempts * 2 / 3);
        for (final RawStmpe stmpe : allStmpe) {
            studentsWithPlacement.add(stmpe.stuId);
        }
        final int numWithPlacement = studentsWithPlacement.size();
        final String numWithPlacementStr = Integer.toString(numWithPlacement);
        log(reportLog, "    Found ", numWithPlacementStr, " students with placement attempts.");
        log(reportLog, CoreConstants.EMPTY);

        final List<StudentPerformanceData> result = new ArrayList<>(numWithPlacement);
        final Collection<RawStmpe> stuAttempts = new ArrayList<>(5);

        log(reportLog, "Compiling placement results for all students.");
        for (final String stuId : studentsWithPlacement) {
            RawStudent stu = null;
            for (final RawStudent test : allStudents) {
                if (test.stuId.equals(stuId)) {
                    stu = test;
                    break;
                }
            }

            if (stu != null) {
                stuAttempts.clear();
                for (final RawStmpe stmpe : allStmpe) {
                    if (stmpe.stuId.equals(stuId)) {
                        final String placed = stmpe.placed;
                        if ("Y".equals(placed) || "N".equals(placed)) {
                            stuAttempts.add(stmpe);
                        }
                    }
                }

                if (!stuAttempts.isEmpty()) {
                    final StudentPerformanceData row = getStudentPerformanceData(stuAttempts, stu);
                    result.add(row);
                }
            }
        }
        final int numResults = result.size();
        final String numResultsStr = Integer.toString(numResults);
        log(reportLog, "    Computed placement performance for ", numResultsStr, " students.");
        log(reportLog, CoreConstants.EMPTY);

        return result;
    }

    /**
     * Generates a single row of performance data.
     *
     * @param attempts the set of student placement attempts
     * @param student  the student record
     * @return the performance data row
     */
    private static StudentPerformanceData getStudentPerformanceData(final Collection<RawStmpe> attempts,
                                                                    final RawStudent student) {

        int totalStsA = 0;
        int totalSts117 = 0;
        int totalSts118 = 0;
        int totalSts124 = 0;
        int totalSts125 = 0;
        int totalSts126 = 0;

        for (final RawStmpe stmpe : attempts) {
            totalStsA += stmpe.stsA.intValue();
            totalSts117 += stmpe.sts117.intValue();
            totalSts118 += stmpe.sts118.intValue();
            totalSts124 += stmpe.sts124.intValue();
            totalSts125 += stmpe.sts125.intValue();
            totalSts126 += stmpe.sts126.intValue();
        }

        final int numAttempts = attempts.size();
        final int stsA = (totalStsA + numAttempts / 2) / numAttempts;
        final int sts117 = (totalSts117 + numAttempts / 2) / numAttempts;
        final int sts118 = (totalSts118 + numAttempts / 2) / numAttempts;
        final int sts124 = (totalSts124 + numAttempts / 2) / numAttempts;
        final int sts125 = (totalSts125 + numAttempts / 2) / numAttempts;
        final int sts126 = (totalSts126 + numAttempts / 2) / numAttempts;

        return new StudentPerformanceData(student, numAttempts, stsA, sts117, sts118, sts124, sts125, sts126);
    }

    /**
     * Calculates the average performance over all students.
     *
     * @param stuPerformances the performance of all students for whom we have placement data
     * @param reportLog       a {@code HtmlBuilder} to which to write messages to the report log
     * @return the average performance
     */
    private static AveragePerformance calculateAveragePerformance(
            final Collection<StudentPerformanceData> stuPerformances, final HtmlBuilder reportLog) {

        log(reportLog, "Computing average placement scores for all students");

        int totalA = 0;
        int total117 = 0;
        int total118 = 0;
        int total124 = 0;
        int total125 = 0;
        int total126 = 0;

        for (final StudentPerformanceData row : stuPerformances) {
            totalA += row.stsA();
            total117 += row.sts117();
            total118 += row.sts118();
            total124 += row.sts124();
            total125 += row.sts125();
            total126 += row.sts126();
        }

        final int n = stuPerformances.size();
        final float avgA = (float) totalA / (float) n;
        final float avg117 = (float) total117 / (float) n;
        final float avg118 = (float) total118 / (float) n;
        final float avg124 = (float) total124 / (float) n;
        final float avg125 = (float) total125 / (float) n;
        final float avg126 = (float) total126 / (float) n;

        final String nStr = Integer.toString(n);
        final String avgAStr = Float.toString(avgA);
        final String avg117Str = Float.toString(avg117);
        final String avg118Str = Float.toString(avg118);
        final String avg124Str = Float.toString(avg124);
        final String avg125Str = Float.toString(avg125);
        final String avg126Str = Float.toString(avg126);
        log(reportLog, "    Average over ", nStr, " students: A=", avgAStr, ", 117=", avg117Str, ", 118=", avg118Str,
                ", 124=", avg124Str, ", 125=", avg125Str, ", 126=", avg126Str);

        return new AveragePerformance(n, avgA, avg117, avg118, avg124, avg125, avg126);
    }

    /**
     * Gathers the set of students who graduated from a specified high school.
     *
     * @param allStudents     the list of all students
     * @param highSchoolCodes the list of high school codes
     */
    private static Map<String, List<StudentPerformanceData>> categorizeStudents(
            final Iterable<StudentPerformanceData> allStudents, final Collection<String> highSchoolCodes) {

        final int numHs = highSchoolCodes.size();
        final Map<String, List<StudentPerformanceData>> result = new HashMap<>(numHs);

        for (final String code : highSchoolCodes) {
            final List<StudentPerformanceData> list = new ArrayList<>(100);
            result.put(code, list);
        }

        for (final StudentPerformanceData studentPerformance : allStudents) {
            final String hsCode = studentPerformance.student().hsCode;

            if (hsCode == null) {
                continue;
            }

            if (highSchoolCodes.contains(hsCode)) {
                final List<StudentPerformanceData> list = result.get(hsCode);
                list.add(studentPerformance);
            }
        }

        return result;
    }

    /**
     * Processes the data for a single high school.
     *
     * @param hsCode    the high school code
     * @param students  the list of students who attended the high school
     * @param reportLog a {@code HtmlBuilder} to which to write messages to the report log
     * @return the average performance over the high school
     */
    private static AveragePerformance processHighSchool(final String hsCode,
                                                        final Collection<StudentPerformanceData> students,
                                                        final HtmlBuilder reportLog) {

        log(reportLog, "Computing average placement performance for high school ", hsCode);

        final AveragePerformance result;

        final int n = students.size();

        if (n == 0) {
            result = new AveragePerformance(0, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
        } else {
            int totalA = 0;
            int total117 = 0;
            int total118 = 0;
            int total124 = 0;
            int total125 = 0;
            int total126 = 0;

            for (final StudentPerformanceData row : students) {
                totalA += row.stsA();
                total117 += row.sts117();
                total118 += row.sts118();
                total124 += row.sts124();
                total125 += row.sts125();
                total126 += row.sts126();
            }

            final float avgA = (float) totalA / (float) n;
            final float avg117 = (float) total117 / (float) n;
            final float avg118 = (float) total118 / (float) n;
            final float avg124 = (float) total124 / (float) n;
            final float avg125 = (float) total125 / (float) n;
            final float avg126 = (float) total126 / (float) n;

            final String nStr = Integer.toString(n);
            final String avgAStr = Float.toString(avgA);
            final String avg117Str = Float.toString(avg117);
            final String avg118Str = Float.toString(avg118);
            final String avg124Str = Float.toString(avg124);
            final String avg125Str = Float.toString(avg125);
            final String avg126Str = Float.toString(avg126);
            log(reportLog, "    Average over ", nStr, " students: A=", avgAStr, ", 117=", avg117Str, ", 118=",
                    avg118Str, ", 124=", avg124Str, ", 125=", avg125Str, ", 126=", avg126Str);

            result = new AveragePerformance(n, avgA, avg117, avg118, avg124, avg125, avg126);
        }

        return result;
    }

    /**
     * Writes a report file.
     *
     * @param target  the target file
     * @param content the report content
     */
    private static void writeReport(final File target, final String content) {

        try (final FileWriter w = new FileWriter(target, StandardCharsets.UTF_8)) {
            w.write(content);
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Fires a progress notification if there is a listener installed.
     *
     * @param description a description of the current step
     * @param done        the number of steps done
     */
    private void fireProgress(final String description, final int done) {

        // Log.info(description);
        if (this.listener != null) {
            this.listener.progress(description, done, 200);
        }
    }

    /**
     * A container for performance data for a single student.
     */
    record StudentPerformanceData(RawStudent student, int numAttempts, int stsA, int sts117, int sts118, int sts124,
                                  int sts125, int sts126) {
    }

    /**
     * A container for global average performance data.
     */
    record AveragePerformance(int n, float stsA, float sts117, float sts118, float sts124, float sts125, float sts126) {
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final List<String> highSchoolCodes = Arrays.asList("060539", "060367", "060379", "060374", "060411", "060471",
                "060389", "060536", "060138", "060408", "060529", "060496", "060608", "060011", "060376", "060517",
                "060443", "060484", "060013", "060255", "060524", "060646", "060506", "060604", "060400", "060405",
                "060508", "060444", "060413", "060513", "060418", "060492", "060611", "060003", "060420", "060012",
                "060432", "060428", "060123", "060372", "060014", "060503", "060354", "060555", "060638", "060002",
                "060509", "060459", "060455", "060502", "060457", "060552", "060460");

        final DatabaseConfig databaseConfig = DatabaseConfig.getDefault();
        final Profile profile = databaseConfig.getCodeProfile("batch");

        if (profile == null) {
            Log.warning("Unable to create production context.");
        } else {
            final Cache cache = new Cache(profile);
            Log.info("Connected to " + profile.id);

            final DataByHighSchool obj = new DataByHighSchool(cache, null);
            obj.calculate(highSchoolCodes);
        }
    }
}
