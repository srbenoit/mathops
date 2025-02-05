package dev.mathops.dbjobs.batch;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.rec.TermRec;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A port of the "reset_tutorial.4gl" batch from Informix ($DEV/src/mpe).
 * <p>
 * Resets passed = Y to P for ELM Tutorial exams once the add deadline for the semester has been reached. Deadline
 * information comes from CSECTION.EXAM_DELETE_DT data. Based on last testing date that the student could take and pass
 * the ELM Exam to gain placement eligibility for MATH 117.
 * <p>
 * Exams are reset for students who (1) have placed into MATH 117 somehow, or (2) started the ELM Tutorial long ago and
 * did not finish by the deadline.
 */
public enum IFXResetELMTutorial {
    ;

    /** Flag to run job in DEBUG mode, which prints but does not perform updates. */
    private static final boolean DEBUG = false;

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

        File file = new File("/opt/zircon/reports/Reset_ELM_Tutorial_" + DTTM_FMT.format(now) + ".rpt");

        try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(report.toString());
            Log.info("Report complete, written to: ", file.getAbsolutePath());
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        if (errors.length() > 0) {
            file = new File("/opt/zircon/reports/Reset_ELM_Tutorial_" + DTTM_FMT.format(now) + ".err");

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
            final LocalDate dueDate = systemData.getExamDeleteDate(RawRecordConstants.M100T, "1", activeTerm.term);

            final LocalDate today = LocalDate.now();
            LocalDate latestStartDate = null;
            LocalDate latestWorkDate = null;
            if ((activeTerm.term.name == ETermName.FALL) || (activeTerm.term.name == ETermName.SPRING)) {

                // We want to delete data for students who (1) started before Fall, and (2) have not
                // done any work in the last 6 weeks
                latestStartDate = activeTerm.startDate;
                latestWorkDate = today.minusWeeks(6L);
            }

            if (latestStartDate == null) {
                report.addln("** ELM Tutorial rows should not be reset during Summer.");
            } else if (today.isBefore(dueDate)) {
                report.addln("** Today is too early to reset ELM Tutorial rows (due ", dueDate, ")");
            } else {
                report.addln("Value of deleteDate prior to resetting ELM Tutorial stus: ",
                        TemporalUtils.FMT_MDY.format(latestStartDate));

                resetELMTutorial(cache, latestStartDate, latestWorkDate, report, errors);
            }
        }
    }

    /**
     * Resets ELM Tutorial records. This marks all "passed = Y" ELM exam records to "passed = P" for any student who
     * completed the tutorial (or has satisfied the prerequisite for MATH 117 some other way), or who started before
     * {@code latestStartDate} and have done no work since {@code latestWorkDate}.
     *
     * @param cache           the data cache
     * @param latestStartDate the latest start date for which to keep data
     * @param latestWorkDate  the latest work date for which to keep data
     * @param report          the report accumulator
     * @param errors          the error message accumulator
     * @throws SQLException if there is an error accessing the database
     */
    private static void resetELMTutorial(final Cache cache, final ChronoLocalDate latestStartDate,
                                         final ChronoLocalDate latestWorkDate, final HtmlBuilder report,
                                         final HtmlBuilder errors)
            throws SQLException {

        // Gather a list of all students who have placed into MATH 117 (these students no longer
        // need ELM Tutorial work)

        final List<RawMpeCredit> m100c = RawMpeCreditLogic.queryByCourse(cache, RawRecordConstants.M100C);

        final Collection<String> studentIds = new HashSet<>(m100c.size());
        for (final RawMpeCredit rec : m100c) {
            studentIds.add(rec.stuId);
        }

        // Find all students that are in this list and who have at least one ELM Tutorial exam with
        // passed = 'Y'
        final List<RawStexam> exams = RawStexamLogic.getExams(cache, RawRecordConstants.M100T, true, //
                "U", "R");

        final Collection<String> studentIdsToReset = new HashSet<>(300);
        for (final RawStexam rec : exams) {
            if ("Y".equals(rec.passed) && studentIds.contains(rec.stuId)) {
                studentIdsToReset.add(rec.stuId);
            }
        }

        report.addln("# of ELM Tutorial students to set passed=P because now OKAY: ",
                Integer.toString(studentIdsToReset.size()));

        // Reset those students' ELM exams

        int deleteCt = 0;
        int errorCt = 0;

        for (final String stuId : studentIdsToReset) {

            final List<RawStexam> stuExams = RawStexamLogic.getExams(cache, stuId, RawRecordConstants.M100T,
                    true, "U", "R");

            boolean error = false;
            for (final RawStexam toReset : stuExams) {
                if ("Y".equals(toReset.passed)) {
                    if (DEBUG) {
                        Log.info("  Resetting ", toReset.version, " for ", toReset.stuId,
                                " because cleared for MATH 117");

                    } else if (!RawStexamLogic.updatePassed(cache, toReset, "P")) {
                        errors.addln("Failed to change StudentExam.passed Y->P for ", stuId, " serial ",
                                toReset.serialNbr);
                        error = true;
                    }
                }
            }

            if (error) {
                report.addln("    ERROR changing ELM exams ('Y' to 'P') for ", stuId, " b/c M 100C found.");
                ++errorCt;
            } else {
                report.addln("    Reset obsolete ELM Exams 'Y' --> 'P' for - ", stuId);
                ++deleteCt;
            }
        }

        report.addln("     # of students reset b/c now OKAY: ", Integer.toString(deleteCt));
        report.addln("     # of ERRORS during reset b/c now OKAY: ", Integer.toString(errorCt));

        // Find students who started the tutorial too long ago and have no recent work

        report.addln("Latest start date to keep work: ", TemporalUtils.FMT_MDY.format(latestStartDate));
        report.addln("Most recent work date to keep work: ", TemporalUtils.FMT_MDY.format(latestWorkDate));

        final List<RawStexam> all = RawStexamLogic.getExams(cache, RawRecordConstants.M100T, true, "U", "R");
        final Map<String, List<RawStexam>> categorized = new HashMap<>(200);
        for (final RawStexam rec : all) {
            final String stu = rec.stuId;
            final List<RawStexam> list = categorized.computeIfAbsent(stu, s -> new ArrayList<>(20));
            list.add(rec);
        }

        report.addln("Total # of ELM Tutorial students who are NOT okay: ", Integer.toString(categorized.size()));

        deleteCt = 0;
        errorCt = 0;
        for (final Map.Entry<String, List<RawStexam>> entry : categorized.entrySet()) {

            final String stuId = entry.getKey();
            final List<RawStexam> stuExams = entry.getValue();

            LocalDate minDate = stuExams.getFirst().examDt;
            LocalDate maxDate = minDate;
            for (final RawStexam rec : stuExams) {
                if (minDate.isAfter(rec.examDt)) {
                    minDate = rec.examDt;
                }
                if (maxDate.isBefore(rec.examDt)) {
                    maxDate = rec.examDt;
                }
            }

            // report.addln(" check on value of min_dt prior to setting P: ",
            // TemporalUtils.FMT_MDY.format(minDate));

            if (minDate.isBefore(latestStartDate) && maxDate.isBefore(latestWorkDate)) {

                boolean error = false;
                boolean found = false;
                for (final RawStexam rec : stuExams) {
                    if ("Y".equals(rec.passed)) {
                        if (DEBUG) {
                            Log.info("  Resetting ", rec.version, " for ", rec.stuId,
                                    " because not completed (started ", minDate, ", last work on ", maxDate, ")");
                        } else if (RawStexamLogic.updatePassed(cache, rec, "P")) {
                            found = true;
                        } else {
                            errors.addln("Failed to change StudentExam.passed Y to P for ", stuId, " serial ",
                                    rec.serialNbr);
                            error = true;
                        }
                    }
                }

                if (error) {
                    report.addln("    ERROR changing ELM exams ('Y' to 'P') for ", stuId, " (started too long ago).");
                    ++errorCt;
                } else if (found) {
                    report.addln("    Reset expiring ELM Exams 'Y' --> 'P' for - ", stuId);
                    ++deleteCt;
                }
            }
        }

        report.addln("     # of students reset b/c too long ago: ", Integer.toString(deleteCt));
        report.addln("     # of ERRORS during reset b/c too long ago: ", Integer.toString(errorCt));
    }

    /**
     * Appends the report header to the report accumulator.
     *
     * @param report the accumulator
     * @param now    the report date/time
     */
    private static void writeReportHeader(final HtmlBuilder report, final ChronoLocalDateTime<LocalDate> now) {

        report.addln("Reset of passed ELM Tutorial review & unit exams -- ",
                TemporalUtils.FMT_MDY.format(now.toLocalDate()));
        report.addln("------------------------------------------------");
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
