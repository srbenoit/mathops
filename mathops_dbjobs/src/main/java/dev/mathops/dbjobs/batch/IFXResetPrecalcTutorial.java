package dev.mathops.dbjobs.batch;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * A port of the "reset_tutorial.4gl" batch from Informix ($DEV/src/mpe).
 * <p>
 * Resets passed = Y to P for Precalculus Tutorial exams once the add deadline for the semester has been reached.
 * Deadline information comes from CSECTION.EXAM_DELETE_DT data. Based on last testing date that the student could take
 * and pass the Precalculus Exam to gain placement eligibility for MATH 117.
 * <p>
 * Exams are reset for students who (1) have placed into the corresponding course somehow, or (2) started a Precalculus
 * Tutorial long ago and did not finish by the deadline.
 */
public enum IFXResetPrecalcTutorial {
    ;

    /** Date/time formatter for report. */
    private static final DateTimeFormatter DTTM_FMT =
            DateTimeFormatter.ofPattern("yyyy'_'MM'_'dd'-'hh'_'mm'_'ss", Locale.US);

    /**
     * Executes the job.
     *
     * @param cache the data cache
     * @return a report
     * @throws SQLException if there was an error accessing the database
     */
    private static String execute(final Cache cache) throws SQLException {

        final LocalDateTime now = LocalDateTime.now();
        final HtmlBuilder report = new HtmlBuilder(1000);
        final HtmlBuilder errors = new HtmlBuilder(100);

        writeReportHeader(report, now);

        buildReport(cache, report, errors);

        File file = new File("/opt/zircon/reports/Reset_Precalc_Tutorial_" + DTTM_FMT.format(now) + ".rpt");

        try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(report.toString());
            Log.info("Report complete, written to: ", file.getAbsolutePath());
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        if (errors.length() > 0) {
            file = new File("/opt/zircon/reports/Reset_Precalc_Tutorial_" + DTTM_FMT.format(now) + ".err");

            try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
                fw.write(errors.toString());
                Log.info("Error report written to: ", file.getAbsolutePath());
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        }

        return report.toString();
    }

    /**
     * Creates the report.
     *
     * @param cache  the data cache
     * @param report the report accumulator
     * @param errors the error message accumulator
     * @throws SQLException if there was an error accessing the database
     */
    private static void buildReport(final Cache cache, final HtmlBuilder report,
                                    final HtmlBuilder errors) throws SQLException {

        final SystemData systemData = cache.getSystemData();
        final TermRec activeTerm = systemData.getActiveTerm();

        if (activeTerm == null) {
            errors.addln("Unable to query the active term.");
        } else {
            final TermKey key = activeTerm.term;

            final LocalDate csect17 = systemData.getExamDeleteDate(RawRecordConstants.M1170, "1", key);

            if (csect17 == null) {
                errors.addln("Unable to query CourseSection for 'M 1170' section '1'");
            } else {
                final LocalDate csect18 = systemData.getExamDeleteDate(RawRecordConstants.M1180, "1", key);

                if (csect18 == null) {
                    errors.addln("Unable to query CourseSection for 'M 1180' section '1'");
                } else {
                    final LocalDate csect24 = systemData.getExamDeleteDate(RawRecordConstants.M1240, "1", key);

                    if (csect24 == null) {
                        errors.addln("Unable to query CourseSection for 'M 1240' section '1'");
                    } else {
                        final LocalDate csect25 = systemData.getExamDeleteDate(RawRecordConstants.M1250, "1", key);

                        if (csect25 == null) {
                            errors.addln("Unable to query CourseSection for 'M 1250' section '1'");
                        } else {
                            final LocalDate csect26 = systemData.getExamDeleteDate(RawRecordConstants.M1260, "1", key);

                            if (csect26 == null) {
                                errors.addln("Unable to query CourseSection for 'M 1260' section '1'.");
                            } else {
                                // See if now is an appropriate time to reset Precalculus Tutorial
                                // records. This job should be run in Fall after the latest exam
                                // delete date for any precalc tutorial course for the Fall term,
                                // and will take away records for

                                final LocalDate examDeleteDate;

                                final LocalDate today = LocalDate.now();

                                // FIXME: HACK to try the delete now for testing
                                examDeleteDate = today;

                                if (examDeleteDate == null) {
                                    report.addln("** Unable to determine exam delete date.");
                                } else if (today.isBefore(examDeleteDate)) {
                                    report.addln("** Today is too early to reset Precalculus Tutorial rows.");
                                } else {
                                    report.addln("Exam delete date for Precalculus Tutorial: ",
                                            TemporalUtils.FMT_MDY.format(examDeleteDate));

                                    resetPrecalcTutorial(cache, report, errors);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Resets Precalculus Tutorial records. This marks all "passed = Y" ELM exam records to "passed = P" for any student
     * who started the tutorial too long ago and did not finish by the exam delete date for the current term.
     *
     * @param cache  the data cache
     * @param report the report accumulator
     * @param errors the error message accumulator
     * @throws SQLException if there was an error accessing the database
     */
    private static void resetPrecalcTutorial(final Cache cache, final HtmlBuilder report,
                                             final HtmlBuilder errors) throws SQLException {

        // Find all students who have at least one Precalc Tutorial exam with passed = 'Y'
        final Collection<String> studentIdsToReset = new HashSet<>(300);

        final List<RawStexam> exams17 = RawStexamLogic.getExams(cache, RawRecordConstants.M1170, true,
                "U", "R");
        for (final RawStexam rec : exams17) {
            if ("Y".equals(rec.passed)) {
                studentIdsToReset.add(rec.stuId);
            }
        }

        final List<RawStexam> exams18 = RawStexamLogic.getExams(cache, RawRecordConstants.M1180, true,
                "U", "R");
        for (final RawStexam rec : exams18) {
            if ("Y".equals(rec.passed)) {
                studentIdsToReset.add(rec.stuId);
            }
        }

        final List<RawStexam> exams24 = RawStexamLogic.getExams(cache, RawRecordConstants.M1240, true,
                "U", "R");
        for (final RawStexam rec : exams24) {
            if ("Y".equals(rec.passed)) {
                studentIdsToReset.add(rec.stuId);
            }
        }

        final List<RawStexam> exams25 = RawStexamLogic.getExams(cache, RawRecordConstants.M1250, true,
                "U", "R");
        for (final RawStexam rec : exams25) {
            if ("Y".equals(rec.passed)) {
                studentIdsToReset.add(rec.stuId);
            }
        }

        final List<RawStexam> exams26 = RawStexamLogic.getExams(cache, RawRecordConstants.M1260, true,
                "U", "R");
        for (final RawStexam rec : exams26) {
            if ("Y".equals(rec.passed)) {
                studentIdsToReset.add(rec.stuId);
            }
        }

        report.addln("# of students with passing Precalculus Tutorial exams: ",
                Integer.toString(studentIdsToReset.size()));

        // Reset those students' Precalc Tutorial exams

        int deleteCt = 0;
        int errorCt = 0;

        final Collection<RawStexam> totalList = new ArrayList<>(100);

        for (final String stuId : studentIdsToReset) {

            totalList.addAll(RawStexamLogic.getExams(cache, stuId, RawRecordConstants.M1170, true,
                    "U", "R"));
            totalList.addAll(RawStexamLogic.getExams(cache, stuId, RawRecordConstants.M1180, true,
                    "U", "R"));
            totalList.addAll(RawStexamLogic.getExams(cache, stuId, RawRecordConstants.M1240, true,
                    "U", "R"));
            totalList.addAll(RawStexamLogic.getExams(cache, stuId, RawRecordConstants.M1250, true,
                    "U", "R"));
            totalList.addAll(RawStexamLogic.getExams(cache, stuId, RawRecordConstants.M1260, true,
                    "U", "R"));

            boolean error = false;
            for (final RawStexam toReset : totalList) {
                if ("Y".equals(toReset.passed) && !RawStexamLogic.updatePassed(cache, toReset, "P")) {
                    errors.addln("Failed to change StudentExam.passed Y to P for ", stuId, " serial ",
                            toReset.serialNbr);
                    error = true;
                }
            }

            if (error) {
                report.addln("    ERROR changing Precalc exams ('Y' to 'P') for ", stuId);
                ++errorCt;
            } else {
                report.addln("    Reset Precalc Exams 'Y' --> 'P' for - ", stuId);
                ++deleteCt;
            }

            totalList.clear();
        }

        report.addln("     # of students reset: ", Integer.toString(deleteCt));
        report.addln("     # of ERRORS during reset: ", Integer.toString(errorCt));
    }

    /**
     * Appends the report header to the report accumulator.
     *
     * @param report the accumulator
     * @param now    the report date/time
     */
    private static void writeReportHeader(final HtmlBuilder report, final ChronoLocalDateTime<LocalDate> now) {

        report.addln("Reset of passed Precalculus Tutorial review & unit exams -- ",
                TemporalUtils.FMT_MDY.format(now.toLocalDate()));
        report.addln("--------------------------------------------------------");
        report.addln();
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);
        final Cache cache = new Cache(profile);

        try {
            final String report = execute(cache);
            Log.fine(CoreConstants.EMPTY);
            Log.fine(report);
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }
}
