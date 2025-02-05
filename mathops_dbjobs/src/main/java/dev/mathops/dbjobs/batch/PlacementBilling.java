package dev.mathops.dbjobs.batch;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawParametersLogic;
import dev.mathops.db.old.rawlogic.RawPlcFeeLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawParameters;
import dev.mathops.db.old.rawrecord.RawPlcFee;
import dev.mathops.db.old.rawrecord.RawStmpe;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * A port of the "placement_fee.4gl" batch from Informix ($DEV/src/mpe).
 *
 * <p>
 * Finds all billable placement activities for which the student has not been billed, and creates a report and upload
 * for the TWARBUS system to bill the students.
 *
 * <p>
 * Billable activities include:
 * <ul>
 * <li>STMPE rows with placed in {'Y', 'N'} and version in {'P????', 'MPT??'}
 * </ul>
 */
public enum PlacementBilling {
    ;

    /** Debug flag - true to skip (but print) updates; false to actually perform updates. */
    private static final boolean DEBUG = false;

    /** Program name in parameters table. */
    private static final String PGM_NAME = "PLCFEE";

    /**
     * Counts the number of unbilled placement activities, but does not perform billing.
     *
     * @param cache the data cache
     * @return the number of unbilled placement activities
     * @throws SQLException if there is an error accessing the database
     */
    public static int countUnbilled(final Cache cache) throws SQLException {

        final HtmlBuilder errors = new HtmlBuilder(100);
        final RawParameters params = gatherParams(cache, errors);

        final LocalDate earliest = params.parm10.minusDays(1L);

        final List<RawStmpe> stmpeRows = RawStmpeLogic.queryOnOrAfter(cache, earliest);
        Collections.sort(stmpeRows); // Orders by student ID

        final Collection<String> studentIds = new HashSet<>(10);
        for (final RawStmpe record : stmpeRows) {

            final String plc = record.placed;
            final String ver = record.version;

            if (("Y".equals(plc) || "N".equals(plc)) && ver != null
                && (!ver.isEmpty() && ver.charAt(0) == 'P' || ver.startsWith("MPT"))) {

                final RawPlcFee existing = RawPlcFeeLogic.queryByStudent(cache, record.stuId);
                if (existing == null) {
                    studentIds.add(record.stuId);
                }
            }
        }

        return studentIds.size();
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
                    final File hardcopyFile = new File(dir, "plc_fee_hardcopy.txt");

                    if (hardcopyFile.exists()) {
                        boolean hit = true;
                        File bakFile = null;
                        int bak = 1;
                        while (hit) {
                            final String bakName = "plc_fee_hardcopy.bak" + bak;
                            bakFile = new File(dir, bakName);
                            hit = bakFile.exists();
                            ++bak;
                        }
                        if (!hardcopyFile.renameTo(bakFile)) {
                            Log.warning("Failed to rename hardcopy backup file");
                        }
                    }

                    try (final FileWriter fw = new FileWriter(hardcopyFile, StandardCharsets.UTF_8)) {
                        fw.write(report.toString());
                        Log.info("Hardcopy report complete, written to: ", hardcopyFile.getAbsolutePath());
                    } catch (final IOException ex) {
                        Log.warning(ex);
                    }

                    final File uploadFile = new File(dir, "plc_fee_upload.txt");

                    if (uploadFile.exists()) {
                        boolean hit = true;
                        File bakFile = null;
                        int bak = 1;
                        while (hit) {
                            final String bakName = "plc_fee_upload.bak" + bak;
                            bakFile = new File(dir, bakName);
                            hit = bakFile.exists();
                            ++bak;
                        }
                        if (!uploadFile.renameTo(bakFile)) {
                            Log.warning("Failed to rename upload backup file");
                        }
                    }

                    try (final FileWriter fw = new FileWriter(uploadFile, StandardCharsets.UTF_8)) {
                        fw.write(upload.toString());
                        Log.info("Upload file complete, written to: ", uploadFile.getAbsolutePath());
                    } catch (final IOException ex) {
                        Log.warning(ex);
                    }
                }
            }

            if (errors.length() > 0) {
                final File errFile = new File(dir, "plc_fee_errors.txt");

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
        // pgm_name = 'PLCFEE'
        // parm1 = 'B' = mode ('B' for batch)
        // parm2 = 415 = last batch number
        // parm3 = 4392 = Subcode
        // parm4 = 130088 = Account
        // parm5 = 15 = fee
        // parm6 = 0101 = start
        // parm7 = 0102 = end
        // parm8 = POOOO = group
        // parm10 = 05/20/22 = Date of last run

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
            } else if (params.parm6 == null) {
                errors.addln("RUN ABORTED: invalid value in parm6 for starting CSU CONNECT date");
                params = null;
            } else if (params.parm7 == null) {
                errors.addln("RUN ABORTED: invalid value in parm6 for ending CSU CONNECT date");
                params = null;
            } else if (params.parm8 == null) {
                errors.addln("RUN ABORTED: invalid value in parm6 for group MPE vsn (enter as 'P????')");
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
     * @return the number of students billed
     * @throws SQLException if there is an error accessing the database
     */
    private static int runJob(final Cache cache, final RawParameters params, final HtmlBuilder report,
                              final HtmlBuilder upload, final HtmlBuilder errors) throws SQLException {

        final LocalDate today = LocalDate.now();

        // Determine who to bill based on the MPT --> remember exams are submitted 24x7

        final LocalDate earliest = params.parm10.minusDays(1L);

        final List<RawStmpe> stmpeRows = RawStmpeLogic.queryOnOrAfter(cache, earliest);
        Collections.sort(stmpeRows); // Orders by student ID
        int numStudents = 0;

        for (final RawStmpe record : stmpeRows) {

            final String plc = record.placed;
            final String ver = record.version;

            if (("Y".equals(plc) || "N".equals(plc)) && ver != null
                && (!ver.isEmpty() && ver.charAt(0) == 'P' || ver.startsWith("MPT"))) {

                if (billIfNotAlreadyBilled(cache, record.stuId, record.examDt, params, report,
                        upload, errors)) {
                    ++numStudents;
                }
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
        report.addln("          # of students:   ", Integer.valueOf(numStudents));
        report.addln();
        report.addln("               Exam fee:  $", params.parm5);

        try {
            final int fee = Integer.parseInt(params.parm5);
            final int total = fee * numStudents;
            report.addln();
            report.addln("           Total billed:  $", Integer.toString(total));
        } catch (final NumberFormatException ex) {
            Log.warning("Unable to interpret fee '", params.parm5, "' as integer", ex);
        }

        return numStudents;
    }

    /**
     * Check whether a student has already been billed for the placement fee, and if not,adds a record of the billing to
     * the database, and adds the student to the report and upload files.
     *
     * @param cache  the data cache
     * @param stuId  the student ID
     * @param examDt the date the placement activity occurred
     * @param params parameters
     * @param report the report accumulator
     * @param upload the upload file accumulator
     * @param errors the error message accumulator
     * @return true if billed; false if not
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean billIfNotAlreadyBilled(final Cache cache, final String stuId, final LocalDate examDt,
                                                  final RawParameters params, final HtmlBuilder report,
                                                  final HtmlBuilder upload, final HtmlBuilder errors)
            throws SQLException {

        final RawPlcFee existing = RawPlcFeeLogic.queryByStudent(cache, stuId);
        final RawStudent stu = RawStudentLogic.query(cache, stuId, false);
        final boolean billed;

        if (stu == null) {
            errors.addln("ERROR: Unable to look up student ", stuId);
            billed = false;
        } else if (existing == null) {
            // Insert record into 'plc_fee' table
            recordPlcFee(cache, stuId, examDt);

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
     * Inserts a row into the plc_fee table to indicate the student has been billed the one-time math placement fee.
     *
     * @param cache  the data cache
     * @param stuId  the student ID
     * @param examDt the date the placement activity occurred
     */
    private static void recordPlcFee(final Cache cache, final String stuId, final LocalDate examDt) {

        final LocalDate today = LocalDate.now();

        if (DEBUG) {
            Log.info("Inserting into 'plc_fee' {", stuId, ", ", "M 100P", ", ", TemporalUtils.FMT_MDY.format(examDt),
                    ", ", TemporalUtils.FMT_MDY.format(today));
        } else {
            final RawPlcFee rec = new RawPlcFee(stuId, "M 100P", examDt, today);

            try {
                RawPlcFeeLogic.insert(cache, rec);
            } catch (final SQLException ex) {
                Log.warning("Failed to insert 'plc_fee' record", ex);
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

        report.addln("                          MATH PLACEMENT EXAM FEE");
        report.addln("                         DEPARTMENT OF MATHEMATICS");
        report.addln("                         COLORADO STATE UNIVERSITY");
        report.addln("                          BATCH DATE:  ",
                TemporalUtils.FMT_MDY.format(now.toLocalDate()));
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

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);

        try {
            final Cache cache = new Cache(profile);

            execute(cache);
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

    }
}
