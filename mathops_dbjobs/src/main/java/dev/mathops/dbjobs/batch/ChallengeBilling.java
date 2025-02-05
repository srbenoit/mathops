package dev.mathops.dbjobs.batch;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawChallengeFeeLogic;
import dev.mathops.db.old.rawlogic.RawParametersLogic;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawChallengeFee;
import dev.mathops.db.old.rawrecord.RawParameters;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * A port of the "challenge_fee.4gl" batch from Informix ($DEV/src/mpe).
 *
 * <p>
 * Finds all billable challenge exams for which the student has not been billed, and creates a report and upload for the
 * TWARBUS system to bill the students.
 */
public enum ChallengeBilling {
    ;

    /** Debug flag - true to skip (but print) updates; false to actually perform updates. */
    private static final boolean DEBUG = false;

    /** Program name in parameters table. */
    private static final String PGM_NAME = "CHALLENGEFEE";

    /**
     * Counts the number of unbilled challenge exams, but does not perform billing.
     *
     * @param cache the data cache
     * @return the number of unbilled challenge exams
     * @throws SQLException if there is an error accessing the database
     */
    public static int countUnbilled(final Cache cache) throws SQLException {

        final HtmlBuilder errors = new HtmlBuilder(100);
        final RawParameters params = gatherParams(cache, errors);

        final LocalDate earliest = params.parm10.minusDays(1L);

        final List<RawStchallenge> stchallengeRows = RawStchallengeLogic.queryOnOrAfter(cache, earliest);
        Collections.sort(stchallengeRows); // Orders by student ID

        int numExams = 0;
        for (final RawStchallenge record : stchallengeRows) {
            final String pass = record.passed;

            if ("Y".equals(pass) || "N".equals(pass)) {
                final RawChallengeFee existing = RawChallengeFeeLogic.queryByStudentCourse(cache, record.stuId,
                        record.course);
                if (existing == null) {
                    ++numExams;
                }
            }
        }

        return numExams;
    }

    /**
     * Executes the job.
     *
     * @param cache the data cache
     * @throws SQLException if there is an error accessing the database
     */
    public static void execute(final Cache cache) throws SQLException {

        File dir = new File("/opt/zircon/reports/");
        if (!dir.exists()) {
            dir = new File("C:\\opt\\zircon\\reports\\");
        }

        if (dir.exists() && dir.isDirectory()) {
            final LocalDateTime now = LocalDateTime.now();
            final HtmlBuilder report = new HtmlBuilder(1000);
            final HtmlBuilder upload = new HtmlBuilder(1000);
            final HtmlBuilder errors = new HtmlBuilder(100);

            final RawParameters params = gatherParams(cache, errors);

            if (params != null) {
                writeReportHeader(params, report, now);

                final int count = runJob(cache, params, report, upload, errors);

                if (count > 0) {

                    final File hardcopyFile = new File(dir, "challenge_fee_hardcopy.txt");

                    if (hardcopyFile.exists()) {
                        boolean hit = true;
                        File bakFile = null;
                        int bak = 1;
                        while (hit) {
                            final String bakName = "challenge_fee_hardcopy.bak" + bak;
                            bakFile = new File(dir, bakName);
                            hit = bakFile.exists();
                            ++bak;
                        }
                        if (!hardcopyFile.renameTo(bakFile)) {
                            Log.warning("Failed to move hardcopy file to bak");
                        }
                    }

                    try (final FileWriter fw = new FileWriter(hardcopyFile, StandardCharsets.UTF_8)) {
                        fw.write(report.toString());
                        Log.info("Hardcopy report complete, written to: ", hardcopyFile.getAbsolutePath());
                    } catch (final IOException ex) {
                        Log.warning(ex);
                    }

                    final File uploadFile = new File(dir, "challenge_fee_upload.txt");

                    if (uploadFile.exists()) {
                        boolean hit = true;
                        File bakFile = null;
                        int bak = 1;
                        while (hit) {
                            final String bakName = "challenge_fee_upload.bak" + bak;
                            bakFile = new File(dir, bakName);
                            hit = bakFile.exists();
                            ++bak;
                        }
                        if (!uploadFile.renameTo(bakFile)) {
                            Log.warning("Failed to move upload file to bak");
                        }
                    }

                    try (final FileWriter fw = new FileWriter(uploadFile, StandardCharsets.UTF_8)) {
                        fw.write(upload.toString());
                        Log.info("Upload file complete, written to: ", uploadFile.getAbsolutePath());
                    } catch (final IOException ex) {
                        Log.warning(ex);
                    }
                } else {
                    Log.info("No challenge exams found.");
                }
            }

            if (errors.length() > 0) {
                final File errFile = new File("/opt/zircon/reports/challenge_fee_errors.txt");

                try (final FileWriter fw = new FileWriter(errFile, StandardCharsets.UTF_8)) {
                    fw.write(errors.toString());
                    Log.info("Error report written to: ", errFile.getAbsolutePath());
                } catch (final IOException ex) {
                    Log.warning(ex);
                }
            }
        } else {
            throw new SQLException("Unable to locate directory in which to write reports.");
        }
    }

    /**
     * Loads parameters from the database and validates.
     *
     * @param cache  the data cache
     * @param errors a {@code HtmlBuilder} to which to append error messages or warnings
     * @return the parameters; {@code null} if unable to query
     * @throws SQLException if there is an error accessing the database
     */
    private static RawParameters gatherParams(final Cache cache, final HtmlBuilder errors) throws SQLException {

        // Gather parameters
        // pgm_name = 'CHALLENGEFEE'
        // parm1 = 'B' = mode ('B' for batch)
        // parm2 = 30 = last batch number
        // parm3 = 4392 = Subcode
        // parm4 = 130088 = Account
        // parm5 = 20 = fee
        // parm10 = Date of last run

        RawParameters params = RawParametersLogic.query(cache, PGM_NAME);

        if (params != null) {
            if (params.parm2 == null) {
                errors.addln("RUN ABORTED: invalid value in parm2 for last batch number");
                params = null;
            } else if (params.parm3 == null) {
                errors.addln("RUN ABORTED: invalid value in parm3 for subcode account number");
                params = null;
            } else if (params.parm3.length() != 4) {
                errors.addln("RUN ABORTED: invalid value in parm3 for subcode account number (len != 4)");
                params = null;
            } else if (params.parm4 == null) {
                errors.addln("RUN ABORTED: invalid value in parm4 for account number");
                params = null;
            } else if (params.parm4.length() != 6) {
                errors.addln("RUN ABORTED: invalid value in parm4 for account number (len != 6)");
                params = null;
            } else if (params.parm5 == null) {
                errors.addln("RUN ABORTED: invalid value in parm5 for exam fee (enter as '##'");
                params = null;
            } else if (params.parm10 == null) {
                errors.addln("RUN ABORTED: invalid value in parm10 for exam date to be billed");
                params = null;
            }
        } else {
            errors.addln("RUN ABORTED: unable to locate ", PGM_NAME, " row in parameters table");
        }

        return params;
    }

    /**
     * Creates the report.
     *
     * @param cache  the data cache
     * @param params parameters
     * @param report the report accumulator
     * @param upload the upload file accumulator
     * @param errors the error message accumulator
     * @return the number of exams billed
     * @throws SQLException if there is an error accessing the database
     */
    private static int runJob(final Cache cache, final RawParameters params, final HtmlBuilder report,
                              final HtmlBuilder upload, final HtmlBuilder errors) throws SQLException {

        final LocalDate today = LocalDate.now();
        final LocalDate earliest = params.parm10.minusDays(1L);

        final List<RawStchallenge> stchallengeRows = RawStchallengeLogic.queryOnOrAfter(cache, earliest);
        Collections.sort(stchallengeRows); // Orders by student ID
        int numExams = 0;

        for (final RawStchallenge record : stchallengeRows) {
            final String pass = record.passed;

            if (("Y".equals(pass) || "N".equals(pass))
                && billIfNotAlreadyBilled(cache, record.stuId, record.course, record.examDt, params,
                    report, upload, errors)) {
                ++numExams;
            }
        }

        // Update the date in parameters table for the next cycle
        if (!DEBUG) {
            RawParametersLogic.updateParm10(cache, PGM_NAME, today);

            try {
                final int batch = Integer.parseInt(params.parm2) + 1;
                RawParametersLogic.updateParm2(cache, PGM_NAME, Integer.toString(batch));
            } catch (final NumberFormatException ex) {
                Log.warning("Unable to increment batch number '", params.parm2, "'", ex);
            }
        }

        // Trailer for the hardcopy report

        report.addln();
        report.addln();
        report.addln();
        report.addln("             # of exams:   ", Integer.valueOf(numExams));
        report.addln();
        report.addln("               Exam fee:  $", params.parm5);

        try {
            final int fee = Integer.parseInt(params.parm5);
            final int total = fee * numExams;
            report.addln();
            report.addln("           Total billed:  $", Integer.toString(total));
        } catch (final NumberFormatException ex) {
            Log.warning("Unable to interpret fee '", params.parm5, "' as integer", ex);
        }

        return numExams;
    }

    /**
     * Check whether a student has already been billed for the challenge fee, and if not,adds a record of the billing to
     * the database, and adds the student to the report and upload files.
     *
     * @param cache  the data cache
     * @param stuId  the student ID
     * @param course the course ID
     * @param examDt the date the challenge exam occurred
     * @param params parameters
     * @param report the report accumulator
     * @param upload the upload file accumulator
     * @param errors the error message accumulator
     * @return true if billed; false if not
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean billIfNotAlreadyBilled(final Cache cache, final String stuId, final String course,
                                                  final LocalDate examDt, final RawParameters params,
                                                  final HtmlBuilder report, final HtmlBuilder upload,
                                                  final HtmlBuilder errors)
            throws SQLException {

        final RawChallengeFee existing = RawChallengeFeeLogic.queryByStudentCourse(cache, stuId, course);
        final RawStudent stu = RawStudentLogic.query(cache, stuId, false);
        final boolean billed;

        if (stu == null) {
            errors.addln("ERROR: Unable to look up student ", stuId);
            billed = false;
        } else if (existing == null) {
            // Insert record into 'challenge_fee' table
            recordChallengeFee(cache, stuId, course, examDt);

            // Write line to "upload" file
            upload.addln(buildUploadRow(stuId, params));

            // Write line to "hardcopy" file
            report.addln("                   ", stuId, "       ", stu.lastName, ", ", stu.firstName);

            billed = true;
        } else {
            billed = false;
        }

        return billed;
    }

    /**
     * Inserts a row into the plc_fee table to indicate the student has been billed the challenge exam fee.
     *
     * @param cache  the data cache
     * @param stuId  the student ID
     * @param course the course
     * @param examDt the date the challenge exam occurred
     */
    private static void recordChallengeFee(final Cache cache, final String stuId, final String course,
                                           final LocalDate examDt) {

        final LocalDate today = LocalDate.now();

        if (DEBUG) {
            Log.info("Inserting into 'challenge_fee' {", stuId, ", ", course, ", ",
                    TemporalUtils.FMT_MDY.format(examDt), ", ", TemporalUtils.FMT_MDY.format(today));
        } else {
            final RawChallengeFee rec = new RawChallengeFee(stuId, course, examDt, today);

            try {
                RawChallengeFeeLogic.insert(cache, rec);
            } catch (final SQLException ex) {
                Log.warning("Failed to insert 'challenge_fee' record", ex);
            }
        }
    }

    /**
     * Creates the contents of a row in the upload file.
     *
     * @param stuId  the student ID
     * @param params the parameters
     * @return the upload file row
     */
    private static String buildUploadRow(final String stuId, final RawParameters params) {

        final StringBuilder sb = new StringBuilder(80);

        sb.append(stuId);
        sb.append("      MT01");
        sb.append(params.parm4); // acct
        sb.append(params.parm3); // subcode
        sb.append('C');
        if (params.parm5.length() == 1) {
            sb.append("00000000");
            sb.append(params.parm5);
        } else if (params.parm5.length() == 2) {
            sb.append("0000000");
            sb.append(params.parm5);
        } else if (params.parm5.length() == 3) {
            sb.append("000000");
            sb.append(params.parm5);
        }
        sb.append(".00");
        sb.append("                                                                 "
                  + "                                                                     "
                  + "                                                                   ");

        return sb.toString();
    }

    /**
     * Appends the report header to the report accumulator.
     *
     * @param params parameters
     * @param report the accumulator
     * @param now    the report date/time
     */
    private static void writeReportHeader(final RawParameters params, final HtmlBuilder report,
                                          final ChronoLocalDateTime<LocalDate> now) {

        report.addln("                          MATH CHALLENGE EXAM FEE");
        report.addln("                         DEPARTMENT OF MATHEMATICS");
        report.addln("                         COLORADO STATE UNIVERSITY");
        report.addln("                          BATCH DATE:  ", TemporalUtils.FMT_MDY.format(now.toLocalDate()));
        report.addln();
        report.addln();

        report.addln("         Account:  ", params.parm4);
        report.addln("         Subcode:  ", params.parm3);
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
            execute(cache);
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }
}
