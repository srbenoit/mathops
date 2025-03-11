package dev.mathops.dbjobs.report;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Generates a report of all students who have forfeited working on a course in order to move to a new RE deadline
 * schedule -- open_status = N/G to either stay in same track or move to a lesser # of courses track (rather than
 * withdrawing to accomplish this change)
 */
public enum StudentWorkReport {
    ;

    /** Characters used to anonymize student IDs. */
    private static final String ANONYMIZATION = "HPKLSNFCXT";

    /**
     * Runs the report, writing the result to a file.
     */
    private static void runReport() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.REPORT_PATH);
        final Cache cache = new Cache(profile);

        try {
            generate(cache);
        } catch (final SQLException | IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Generates the report and appends it to a list of strings. This can be called from external reports or batches to
     * embed this report in a larger report.
     *
     * @param cache the data cache
     * @throws SQLException if there was an error accessing the database
     * @throws IOException  if there was an error writing an output file
     */
    public static void generate(final Cache cache) throws SQLException, IOException {

        final SystemData systemData = cache.getSystemData();
        final TermRec active = systemData.getActiveTerm();

        final File reportDir = PathList.getInstance().get(EPath.REPORT_PATH);

        // First, collect all students and organize by pace and track

        final Map<Integer, Map<String, List<String>>> classifiedStudentIds = gatherAndClassifyStudents(cache,
                active.term);

        for (final Map.Entry<Integer, Map<String, List<String>>> paceEntry : classifiedStudentIds.entrySet()) {
            final Integer pace = paceEntry.getKey();
            final Map<String, List<String>> trackMap = paceEntry.getValue();

            for (final Map.Entry<String, List<String>> trackEntry : trackMap.entrySet()) {
                final String track = trackEntry.getKey();
                final List<String> students = trackEntry.getValue();

                generateCsv(cache, reportDir, pace, track, students);
            }
        }
    }

    /**
     * Gathers all students who have a current-term registration and a "STTERM" row, and classifies them by pace and
     * pace track.
     *
     * @param cache the data cache
     * @param term  the term key
     * @return a map from pace to a map from track to a list of students in that pace and track
     * @throws SQLException if there was an error accessing the database
     */
    private static Map<Integer, Map<String, List<String>>> gatherAndClassifyStudents(
            final Cache cache, final TermKey term) throws SQLException {

        // Exclude OT and Dropped
        final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(cache, term, false, false);

        final Collection<String> studentIds = new HashSet<>(7000);
        final Map<Integer, Map<String, List<String>>> classifiedStudentIds = new HashMap<>(5);
        for (int pace = 1; pace <= 5; ++pace) {
            final Integer key = Integer.valueOf(pace);
            final Map<String, List<String>> trackMap = new HashMap<>(10);
            classifiedStudentIds.put(key, trackMap);
        }

        for (final RawStcourse test : allRegs) {
            if (!"Y".equals(test.finalClassRoll) || "Y".equals(test.iInProgress)) {
                continue;
            }
            final String stuId = test.stuId;

            if (!studentIds.contains(stuId)) {
                studentIds.add(stuId);

                final RawStterm stterm = RawSttermLogic.query(cache, term, test.stuId);
                if (stterm != null) {
                    final Map<String, List<String>> trackMap = classifiedStudentIds.computeIfAbsent(
                            stterm.pace, A -> new HashMap<>(10));
                    final List<String> studentList = trackMap.computeIfAbsent(stterm.paceTrack,
                            A -> new ArrayList<>(1000));
                    studentList.add(stuId);
                }
            }
        }

        return classifiedStudentIds;
    }

    /**
     * Generates a CSV file for a particular pace and track.
     *
     * @param cache      the data cache
     * @param reportDir  the directory to which to write the report
     * @param pace       the pace
     * @param track      the track
     * @param studentIds the list of IDs of students in the pace and track
     * @throws SQLException if there was an error accessing the database
     * @throws IOException  if there was an error writing an output file
     */
    private static void generateCsv(final Cache cache, final File reportDir, final Integer pace, final String track,
                                    final List<String> studentIds) throws SQLException, IOException {

        final String examFilename = SimpleBuilder.concat("Pace_", pace, "_Track_", track, "_Exams.csv");

        final HtmlBuilder builder = new HtmlBuilder(200);

        final File examFile = new File(reportDir, examFilename);
        try (final FileWriter examWriter = new FileWriter(examFile)) {

            examWriter.write("STUDENT,DATE,START_TIME,DURATION,EXAM_TYPE,COURSE,UNIT,SCORE,PASSED\r\n");

            for (final String studentId : studentIds) {
                final List<RawStexam> exams = RawStexamLogic.queryByStudent(cache, studentId, true);
                if (!exams.isEmpty()) {
                    final String anonymized = anonymizeStudentId(studentId);

                    for (final RawStexam exam : exams) {
                        if ("Q".equals(exam.examType)) {
                            continue;
                        }

                        if ("M 117".equals(exam.course) || "M 118".equals(exam.course)
                            || "M 124".equals(exam.course) || "M 125".equals(exam.course)
                            || "M 126".equals(exam.course)) {

                            builder.reset();
                            final String dateStr = TemporalUtils.FMT_MDY_COMPACT_FIXED.format(exam.examDt);
                            final int startHour = exam.startTime / 60;
                            final int startMin = exam.startTime % 60;
                            final String startStr = startMin < 10 ? (startHour + ":0" + startMin) :
                                    (startHour + ":" + startMin);
                            final int dur = exam.finishTime - exam.startTime;
                            final String durationStr = dur >= 0 ? Integer.toString(dur) : Integer.toString(dur + 1440);
                            builder.addln(anonymized, ",", dateStr, ",", startStr, ",", durationStr, ",", exam.examType,
                                    ",", exam.course, ",", exam.unit, ",", exam.examScore, ",", exam.passed);

                            examWriter.write(builder.toString());
                        }
                    }
                }
            }
        }

        final String hwFilename = SimpleBuilder.concat("Pace_", pace, "_Track_", track, "_Homework.csv");
        final File hwFile = new File(reportDir, hwFilename);
        try (final FileWriter hwWriter = new FileWriter(hwFile)) {

            hwWriter.write("STUDENT,DATE,START_TIME,DURATION,COURSE,UNIT,OBJECTIVE,SCORE,PASSED\r\n");

            for (final String studentId : studentIds) {
                final List<RawSthomework> homeworks = RawSthomeworkLogic.queryByStudent(cache, studentId, false);
                if (!homeworks.isEmpty()) {
                    final String anonymized = anonymizeStudentId(studentId);

                    for (final RawSthomework hw : homeworks) {
                        if ("M 117".equals(hw.course) || "M 118".equals(hw.course)
                            || "M 124".equals(hw.course) || "M 125".equals(hw.course) || "M 126".equals(hw.course)) {

                            if ("HW".equals(hw.hwType)) {
                                builder.reset();

                                final String dateStr = TemporalUtils.FMT_MDY_COMPACT_FIXED.format(hw.hwDt);
                                final int startHour = hw.startTime / 60;
                                final int startMin = hw.startTime % 60;
                                final String startStr = startMin < 10 ? (startHour + ":0" + startMin) :
                                        (startHour + ":" + startMin);
                                final int dur = hw.finishTime - hw.startTime;
                                final String durationStr = dur >= 0 ? Integer.toString(dur) : Integer.toString(
                                        dur + 1440);
                                builder.addln(anonymized, ",", dateStr, ",", startStr, ",", durationStr, ",",
                                        hw.course, ",", hw.unit, ",", hw.objective, ",", hw.hwScore, ",", hw.passed);

                                hwWriter.write(builder.toString());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates an anonymized ID from a student ID.
     *
     * @param studentId the student ID
     * @return the anonymized ID
     */
    private static String anonymizeStudentId(final String studentId) {

        final int len = studentId.length();
        final StringBuilder builder = new StringBuilder(len);

        for (int i = 1; i < len; ++i) {
            final int index = (int) studentId.charAt(i) % 10;
            builder.append(ANONYMIZATION.charAt(index));
        }

        return builder.toString();
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        runReport();
    }
}
