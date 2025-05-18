package dev.mathops.dbjobs.eos.rollover;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawEtextKey;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawMpeLog;
import dev.mathops.db.old.rawrecord.RawMpecrDenied;
import dev.mathops.db.old.rawrecord.RawMpscorequeue;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStchallengeqa;
import dev.mathops.db.old.rawrecord.RawStetext;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStmpeqa;
import dev.mathops.db.old.rawrecord.RawStqa;
import dev.mathops.db.old.rawrecord.RawStsurveyqa;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * At the end of a semester, during final grading, the PROD database can be copied to DEV, and the placement and
 * tutorial websites can be directed to use DEV to keep those sites open during the rollover process. This requires
 * extending the dates (in DEV) of the ending term and the "csection" rows for tutorials so testing can still take
 * place.
 *
 * <p>
 * Once rollover is complete and PROD is ready for the following term, all websites can be turned off, and this batch
 * run to copy all placement and tutorial records (and any Math Plan submissions) that were submitted to DEV over to
 * PROD. At the same time, the websites should be redirected to PROD, and the websites restarted.
 *
 * <p>
 * One should ensure there are no placement/tutorial exams in progress when the copy back to PROD is initiated, because
 * those sessions would be lost and not recoverable.
 *
 * <p>
 * The following database tables are transferred:
 * <ul>
 * <li>stexam and corresponding stqa rows (should only be tutorial exams)
 * <li>stmpe and corresponding stmpeqa rows
 * <li>stchallenge and corresponding stchallengeqa rows
 * <li>mpe_credit rows
 * <li>mpecr_denied rows
 * <li>mpe_log rows
 * <li>mpscorequeue rows
 * <li>stsurveyqa rows (surveys at start of placement tool)
 * <li>stmathplan rows (math plan submissions)
 * <li>etext_key rows with activation date after copy date (update activation date in PROD)
 * <li>stetext with activation date after copy date
 * </ul>
 *
 * <p>
 * It is safe to run this batch twice - it will test for existence of rows in PROD before copying.
 * In fact, running the batch twice is healthy to ensure it reports 0 rows needing to be transferred
 * on the second run.
 */
final class TransferPlacementDevtoProd {

    // SP25: Switch to DEV happened on May 14 at 7:30 AM
    private static final LocalDateTime CUTOVER_DATETIME = LocalDateTime.of(2025, 5, 14, 7, 30);

    /** Debug flag - true to print but do no actual transfer of data; false to transfer data. */
    private static final boolean DEBUG = true;

    /** A commonly used string. */
    private static final String INDENT4 = "    ";

    /** A commonly used string. */
    private static final String INDENT8 = "        ";

    /** The database profile to use. */
    private Profile prodProfile;

    /** The database profile to use. */
    private Profile devProfile;

    /**
     * The date/time after which to transfer data (this is the date/time that PROD was coped to DEV at the start of the
     * end-of-term rollover process).
     */
    private final LocalDateTime since;

    /** The date after which to transfer data. */
    private final Date sinceDate;

    /** The time of day (minutes past midnight) after which to transfer data. */
    private final int sinceTime;

    /**
     * Constructs a new {@code TransferPlacementDevtoProd}.
     *
     * @param theSince the date/time PROD was coped to DEV at the start of the process
     */
    private TransferPlacementDevtoProd(final LocalDateTime theSince) {

        final DatabaseConfig databaseConfig = DatabaseConfig.getDefault();
        this.prodProfile = databaseConfig.getCodeProfile(Contexts.ADMPROD_PATH);
        this.devProfile = databaseConfig.getCodeProfile(Contexts.ADMDEV_PATH);

        this.since = theSince;
        this.sinceDate = Date.valueOf(theSince.toLocalDate());
        final LocalTime time = theSince.toLocalTime();
        this.sinceTime = time.getHour() * 60 + time.getMinute();
    }

    /**
     * Executes the job.
     *
     * @return the report
     */
    private String execute() {

        final Collection<String> report = new ArrayList<>(100);

        report.add("Transfer of Placement, ELM, Precalculus Tutorial Data (DEV to PROD) -- "
                   + TemporalUtils.FMT_MDY_AT_HM_A.format(LocalDateTime.now()));
        report.add("-----------------------------------------------------------------"
                   + "---------------------------");
        report.add("    Transferring all data recorded in DEV since "
                   + TemporalUtils.FMT_MDY_AT_HM_A.format(this.since));

        if (this.prodProfile == null) {
            report.add(CoreConstants.EMPTY);
            report.add("*** ERROR: Unable to create production context.");
        } else {
            // TODO: Make this interactive - pre-query and build a list of what needs to be
            //  transfered, show that to the user, and request confirmation to perform changes.
            //  For now, we just have a global DEBUG flag (if true, no data is moved, we just print
            //  what would have been done)

            final Cache prodCache = new Cache(this.prodProfile);
            final Cache devCache = new Cache(this.devProfile);

            try {
                final DbConnection prodConn = prodCache.checkOutConnection(ESchema.LEGACY);

                try {
                    final DbConnection devConn = devCache.checkOutConnection(ESchema.LEGACY);

                    try {
                        doStexam(prodConn, devConn, report);
                        doStmpe(prodConn, devConn, report);
                        doStchallenge(prodConn, devConn, report);
                        doMpeCredit(prodConn, devConn, report);
                        doMpecrDenied(prodConn, devConn, report);
                        doMpeLog(prodConn, devConn, report);
                        doMpescorequeue(prodConn, devConn, report);
                        doStsurveyqa(prodConn, devConn, report);
                        doStmathplan(prodConn, devConn, report);
                        doEtextKey(prodConn, devConn, report);
                        doStetext(prodConn, devConn, report);
                    } finally {
                        Cache.checkInConnection(devConn);
                    }
                } finally {
                    Cache.checkInConnection(prodConn);
                }
            } catch (final SQLException ex) {
                report.add(CoreConstants.EMPTY);
                report.add("*** ERROR: Exception: " + ex.getMessage());
                Throwable th = ex;
                while (th != null) {
                    for (final StackTraceElement st : th.getStackTrace()) {
                        report.add(INDENT8 + st.toString());
                    }
                    th = th.getCause();
                }
            }
        }

        final HtmlBuilder htm = new HtmlBuilder(1000);
        htm.addln("<pre>");
        for (final String rep : report) {
            htm.addln(rep);
        }
        htm.addln("</pre>");

        return htm.toString();
    }

    /**
     * Transfers "stexam" records and corresponding "stqa" records.
     *
     * @param prodConn a connection to the production (destination) database
     * @param devConn  a connection to the development (source) database
     * @param report   list to which to add report lines
     * @throws SQLException if there is an error accessing the database
     */
    private void doStexam(final DbConnection prodConn, final DbConnection devConn,
                          final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("* Copying 'stexam' and corresponding 'stqa' records...");
        report.add(CoreConstants.EMPTY);

        final Map<RawStexam, List<RawStqa>> stexamOnDev = new HashMap<>(10);
        final Map<String, Integer> courses = new TreeMap<>();

        // Gather all "stexam" rows since the start time

        final String sql1 = "SELECT * FROM stexam"
                            + " WHERE exam_dt > ?"
                            + " OR (exam_dt = ? AND finish_time >= ?)";

        try (final PreparedStatement ps = devConn.prepareStatement(sql1)) {
            ps.setDate(1, this.sinceDate);
            ps.setDate(2, this.sinceDate);
            ps.setInt(3, this.sinceTime);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final RawStexam exam = RawStexam.fromResultSet(rs);
                    final List<RawStqa> qalist = new ArrayList<>(24);
                    stexamOnDev.put(exam, qalist);

                    final Integer count = courses.get(exam.course);
                    if (count == null) {
                        courses.put(exam.course, Integer.valueOf(1));
                    } else {
                        courses.put(exam.course, Integer.valueOf(count.intValue() + 1));
                    }
                }
            }
        }

        if (stexamOnDev.isEmpty()) {
            report.add(INDENT4 + "There are no stexam records to copy.");
        } else {
            report.add(INDENT4 + "Found " + stexamOnDev.size() + " stexam records to copy.");
            report.add(INDENT4 + "The following courses are represented:");
            for (final Map.Entry<String, Integer> course : courses.entrySet()) {
                if (course.getValue().intValue() == 1) {
                    report.add(INDENT8 + course.getKey() + " (1 exam)");
                } else {
                    report.add(INDENT8 + course.getKey() + " (" + course.getValue() + " exams)");
                }
            }

            // Gather all "stqa" rows associated with each "stexam" row

            final String sql2 = "SELECT * FROM stqa"
                                + " WHERE serial_nbr =?";

            try (final PreparedStatement ps = devConn.prepareStatement(sql2)) {
                for (final Map.Entry<RawStexam, List<RawStqa>> entry : stexamOnDev.entrySet()) {

                    final Long serial = entry.getKey().serialNbr;
                    ps.setLong(1, serial.longValue());

                    try (final ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            entry.getValue().add(RawStqa.fromResultSet(rs));
                        }
                    }

                    if (entry.getValue().size() < 10) {
                        report.add(INDENT4 + "*** WARNING: stexam record with serial number "
                                   + serial + " has only " + entry.getValue().size() + " stqa records.");
                    }
                }
            }

            // Sweep through each exam, check for its existence on PROD, and if found, remove from
            // consideration

            final Iterator<Map.Entry<RawStexam, List<RawStqa>>> entryIter = stexamOnDev.entrySet().iterator();

            while (entryIter.hasNext()) {
                final Map.Entry<RawStexam, List<RawStqa>> entry = entryIter.next();
                final RawStexam stexam = entry.getKey();

                final String sql3 = "SELECT * FROM stexam"
                                    + " WHERE serial_nbr=?"
                                    + " AND version=?"
                                    + " AND stu_id=?"
                                    + " AND exam_dt=?"
                                    + " AND finish_time=?";

                try (final PreparedStatement ps = prodConn.prepareStatement(sql3)) {

                    if (stexam.serialNbr == null) {
                        ps.setNull(1, Types.INTEGER);
                    } else {
                        ps.setLong(1, stexam.serialNbr.longValue());
                    }
                    ps.setString(2, stexam.version);
                    ps.setString(3, stexam.stuId);
                    ps.setDate(4, Date.valueOf(stexam.examDt));
                    if (stexam.finishTime == null) {
                        ps.setNull(5, Types.INTEGER);
                    } else {
                        ps.setInt(5, stexam.finishTime.intValue());
                    }

                    try (final ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Record exists in PROD already - skip
                            entryIter.remove();
                        }
                    }
                }
            }

            if (stexamOnDev.isEmpty()) {
                report.add(INDENT4 + "All stexam records already exist in PROD.");
            } else {
                report.add(INDENT4 + stexamOnDev.size() + " stexam records do not exist in PROD.");

                // Insert in PROD
                if (!DEBUG) {
                    int stexamInsertCount = 0;
                    int stqaInsertCount = 0;

                    try {
                        final String sql4 = "INSERT INTO stexam (serial_nbr,version,"
                                            + "stu_id,exam_dt,exam_score,start_time,finish_time,"
                                            + "time_ok,passed,seq_nbr,course,unit,exam_type,"
                                            + "is_first_passed,exam_source,calc_nbr) "
                                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql4)) {

                            for (final Map.Entry<RawStexam, List<RawStqa>> entry : stexamOnDev.entrySet()) {
                                final RawStexam stexam = entry.getKey();

                                if (stexam.serialNbr == null) {
                                    ps.setNull(1, Types.INTEGER);
                                } else {
                                    ps.setLong(1, stexam.serialNbr.longValue());
                                }
                                ps.setString(2, stexam.version);
                                ps.setString(3, stexam.stuId);
                                ps.setDate(4, Date.valueOf(stexam.examDt));
                                if (stexam.examScore == null) {
                                    ps.setNull(5, Types.INTEGER);
                                } else {
                                    ps.setInt(5, stexam.examScore.intValue());
                                }
                                if (stexam.startTime == null) {
                                    ps.setNull(6, Types.INTEGER);
                                } else {
                                    ps.setInt(6, stexam.startTime.intValue());
                                }
                                if (stexam.finishTime == null) {
                                    ps.setNull(7, Types.INTEGER);
                                } else {
                                    ps.setInt(7, stexam.finishTime.intValue());
                                }
                                ps.setString(8, stexam.timeOk);
                                ps.setString(9, stexam.passed);
                                if (stexam.seqNbr == null) {
                                    ps.setNull(10, Types.INTEGER);
                                } else {
                                    ps.setInt(10, stexam.seqNbr.intValue());
                                }
                                ps.setString(11, stexam.course);
                                if (stexam.unit == null) {
                                    ps.setNull(12, Types.INTEGER);
                                } else {
                                    ps.setInt(12, stexam.unit.intValue());
                                }
                                ps.setString(13, stexam.examType);
                                ps.setString(14, stexam.isFirstPassed);
                                ps.setString(15, stexam.examSource);
                                ps.setString(16, stexam.calcNbr);

                                ps.executeUpdate();
                                ++stexamInsertCount;
                            }
                        }

                        final String sql5 = "INSERT INTO stqa (serial_nbr,"
                                            + "question_nbr,answer_nbr,objective,stu_answer,stu_id,"
                                            + "version,ans_correct,exam_dt,subtest,finish_time) "
                                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql5)) {

                            for (final List<RawStqa> list : stexamOnDev.values()) {
                                for (final RawStqa stqa : list) {

                                    if (stqa.serialNbr == null) {
                                        ps.setNull(1, Types.INTEGER);
                                    } else {
                                        ps.setLong(1, stqa.serialNbr.longValue());
                                    }
                                    if (stqa.questionNbr == null) {
                                        ps.setNull(2, Types.INTEGER);
                                    } else {
                                        ps.setInt(2, stqa.questionNbr.intValue());
                                    }
                                    if (stqa.answerNbr == null) {
                                        ps.setNull(3, Types.INTEGER);
                                    } else {
                                        ps.setInt(3, stqa.answerNbr.intValue());
                                    }
                                    ps.setString(4, stqa.objective);
                                    ps.setString(5, stqa.stuAnswer);
                                    ps.setString(6, stqa.stuId);
                                    ps.setString(7, stqa.version);
                                    ps.setString(8, stqa.ansCorrect);
                                    ps.setDate(9, Date.valueOf(stqa.examDt));
                                    ps.setString(10, stqa.subtest);
                                    if (stqa.finishTime == null) {
                                        ps.setNull(11, Types.INTEGER);
                                    } else {
                                        ps.setInt(11, stqa.finishTime.intValue());
                                    }

                                    ps.executeUpdate();
                                    ++stqaInsertCount;
                                }
                            }
                        }

                        prodConn.commit();

                        report.add(INDENT4 + "Copied " + stexamInsertCount + " stexam records to PROD.");
                        report.add(INDENT4 + "Copied " + stqaInsertCount + " stqa records to PROD.");
                    } catch (final SQLException ex) {
                        report.add(INDENT4 + "*** ERROR: SQLException: " + ex.getMessage());
                        Throwable th = ex;
                        while (th != null) {
                            for (final StackTraceElement st : th.getStackTrace()) {
                                report.add(INDENT8 + st.toString());
                            }
                            th = th.getCause();
                        }
                        prodConn.rollback();
                        throw ex;
                    }
                }
            }
        }
    }

    /**
     * Transfers "stmpe" records and corresponding "stmpeqa" records.
     *
     * @param prodConn a connection to the production (destination) database
     * @param devConn  a connection to the development (source) database
     * @param report   list to which to add report lines
     * @throws SQLException if there is an error accessing the database
     */
    private void doStmpe(final DbConnection prodConn, final DbConnection devConn,
                         final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("* Copying 'stmpe' and corresponding 'stmpeqa' records...");
        report.add(CoreConstants.EMPTY);

        final Map<RawStmpe, List<RawStmpeqa>> stmpeOnDev = new HashMap<>(10);

        // Gather all "stmpe" rows since the start time

        final String sql1 = "SELECT * FROM stmpe"
                            + " WHERE exam_dt > ?"
                            + " OR (exam_dt = ? AND finish_time >= ?)";

        try (final PreparedStatement ps = devConn.prepareStatement(sql1)) {
            ps.setDate(1, this.sinceDate);
            ps.setDate(2, this.sinceDate);
            ps.setInt(3, this.sinceTime);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final RawStmpe stmpe = RawStmpe.fromResultSet(rs);
                    final List<RawStmpeqa> qalist = new ArrayList<>(24);
                    stmpeOnDev.put(stmpe, qalist);
                }
            }
        }

        if (stmpeOnDev.isEmpty()) {
            report.add(INDENT4 + "There are no stmpe records to copy.");
        } else {
            report.add(INDENT4 + "Found " + stmpeOnDev.size() + " stmpe records to copy.");

            // Gather all "stmpeqa" rows associated with each "stmpe" row

            final String sql2 = "SELECT * FROM stmpeqa"
                                + " WHERE stu_id=? AND version=? AND exam_dt=? AND finish_time=?";

            try (final PreparedStatement ps = devConn.prepareStatement(sql2)) {
                for (final Map.Entry<RawStmpe, List<RawStmpeqa>> entry : stmpeOnDev.entrySet()) {
                    final RawStmpe stmpe = entry.getKey();

                    ps.setString(1, stmpe.stuId);
                    ps.setString(2, stmpe.version);
                    ps.setDate(3, Date.valueOf(stmpe.examDt));
                    if (stmpe.finishTime == null) {
                        ps.setNull(4, Types.INTEGER);
                    } else {
                        ps.setInt(4, stmpe.finishTime.intValue());
                    }

                    try (final ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            entry.getValue().add(RawStmpeqa.fromResultSet(rs));
                        }
                    }

                    if (entry.getValue().size() < 50) {
                        report.add(INDENT4 + "*** WARNING: stmpe record for student " + stmpe.stuId
                                   + " has only " + entry.getValue().size() + " stmpeqa records.");
                    }
                }
            }

            // Sweep through each exam, check for its existence on PROD, and if found, remove from
            // consideration

            final Iterator<Map.Entry<RawStmpe, List<RawStmpeqa>>> entryIter =
                    stmpeOnDev.entrySet().iterator();

            while (entryIter.hasNext()) {

                final Map.Entry<RawStmpe, List<RawStmpeqa>> entry = entryIter.next();
                final RawStmpe stmpe = entry.getKey();

                final String sql3 = "SELECT * FROM stmpe"
                                    + " WHERE stu_id=?"
                                    + " AND version=?"
                                    + " AND exam_dt=?"
                                    + " AND finish_time=?";

                try (final PreparedStatement ps = prodConn.prepareStatement(sql3)) {

                    ps.setString(1, stmpe.stuId);
                    ps.setString(2, stmpe.version);
                    ps.setDate(3, Date.valueOf(stmpe.examDt));
                    if (stmpe.finishTime == null) {
                        ps.setNull(4, Types.INTEGER);
                    } else {
                        ps.setInt(4, stmpe.finishTime.intValue());
                    }

                    try (final ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Record exists in PROD already - skip
                            entryIter.remove();
                        }
                    }
                }
            }

            if (stmpeOnDev.isEmpty()) {
                report.add(INDENT4 + "All stmpe records already exist in PROD.");
            } else {
                report.add(INDENT4 + stmpeOnDev.size() + " stmpe records do not exist in PROD.");

                // Insert in PROD
                if (!DEBUG) {
                    int stmpeInsertCount = 0;
                    int stmpeqaInsertCount = 0;

                    try {
                        final String sql4 = "INSERT INTO stmpe (stu_id,version,"
                                            + "academic_yr,exam_dt,start_time,finish_time,last_name,"
                                            + "first_name,middle_initial,seq_nbr,serial_nbr,sts_a,"
                                            + "sts_117,sts_118,sts_124,sts_125,sts_126,placed,"
                                            + "how_validated) "
                                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql4)) {

                            for (final Map.Entry<RawStmpe, List<RawStmpeqa>> entry : stmpeOnDev
                                    .entrySet()) {
                                final RawStmpe stmpe = entry.getKey();

                                ps.setString(1, stmpe.stuId);
                                ps.setString(2, stmpe.version);
                                ps.setString(3, stmpe.academicYr);
                                ps.setDate(4, Date.valueOf(stmpe.examDt));
                                if (stmpe.startTime == null) {
                                    ps.setNull(5, Types.INTEGER);
                                } else {
                                    ps.setInt(5, stmpe.startTime.intValue());
                                }
                                if (stmpe.finishTime == null) {
                                    ps.setNull(6, Types.INTEGER);
                                } else {
                                    ps.setInt(6, stmpe.finishTime.intValue());
                                }
                                ps.setString(7, stmpe.lastName);
                                ps.setString(8, stmpe.firstName);
                                ps.setString(9, stmpe.middleInitial);
                                if (stmpe.seqNbr == null) {
                                    ps.setNull(10, Types.INTEGER);
                                } else {
                                    ps.setInt(10, stmpe.seqNbr.intValue());
                                }
                                if (stmpe.serialNbr == null) {
                                    ps.setNull(11, Types.INTEGER);
                                } else {
                                    ps.setLong(11, stmpe.serialNbr.longValue());
                                }
                                if (stmpe.stsA == null) {
                                    ps.setNull(12, Types.INTEGER);
                                } else {
                                    ps.setInt(12, stmpe.stsA.intValue());
                                }
                                if (stmpe.sts117 == null) {
                                    ps.setNull(13, Types.INTEGER);
                                } else {
                                    ps.setInt(13, stmpe.sts117.intValue());
                                }
                                if (stmpe.sts118 == null) {
                                    ps.setNull(14, Types.INTEGER);
                                } else {
                                    ps.setInt(14, stmpe.sts118.intValue());
                                }
                                if (stmpe.sts124 == null) {
                                    ps.setNull(15, Types.INTEGER);
                                } else {
                                    ps.setInt(15, stmpe.sts124.intValue());
                                }
                                if (stmpe.sts125 == null) {
                                    ps.setNull(16, Types.INTEGER);
                                } else {
                                    ps.setInt(16, stmpe.sts125.intValue());
                                }
                                if (stmpe.sts126 == null) {
                                    ps.setNull(17, Types.INTEGER);
                                } else {
                                    ps.setInt(17, stmpe.sts126.intValue());
                                }
                                ps.setString(18, stmpe.placed);
                                ps.setString(19, stmpe.howValidated);

                                ps.executeUpdate();
                                ++stmpeInsertCount;
                            }
                        }

                        final String sql5 = "INSERT INTO stmpeqa (stu_id,"
                                            + "version,exam_dt,finish_time,question_nbr,stu_answer,"
                                            + "ans_correct,subtest,tree_ref) "
                                            + "VALUES (?,?,?,?,?,?,?,?,?)";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql5)) {

                            for (final List<RawStmpeqa> list : stmpeOnDev.values()) {
                                for (final RawStmpeqa stmpeqa : list) {

                                    ps.setString(1, stmpeqa.stuId);
                                    ps.setString(2, stmpeqa.version);
                                    ps.setDate(3, Date.valueOf(stmpeqa.examDt));
                                    if (stmpeqa.finishTime == null) {
                                        ps.setNull(4, Types.INTEGER);
                                    } else {
                                        ps.setInt(4, stmpeqa.finishTime.intValue());
                                    }
                                    if (stmpeqa.questionNbr == null) {
                                        ps.setNull(5, Types.INTEGER);
                                    } else {
                                        ps.setInt(5, stmpeqa.questionNbr.intValue());
                                    }
                                    ps.setString(6, stmpeqa.stuAnswer);
                                    ps.setString(7, stmpeqa.ansCorrect);
                                    ps.setString(8, stmpeqa.subtest);
                                    ps.setString(9, stmpeqa.treeRef);

                                    ps.executeUpdate();
                                    ++stmpeqaInsertCount;
                                }
                            }
                        }

                        prodConn.commit();

                        report.add(INDENT4 + "Copied " + stmpeInsertCount + " stmpe records to PROD.");
                        report.add(INDENT4 + "Copied " + stmpeqaInsertCount + " stmpeqa records to PROD.");

                    } catch (final SQLException ex) {
                        report.add(INDENT4 + "*** ERROR: SQLException: " + ex.getMessage());
                        Throwable th = ex;
                        while (th != null) {
                            for (final StackTraceElement st : th.getStackTrace()) {
                                report.add(INDENT8 + st.toString());
                            }
                            th = th.getCause();
                        }
                        prodConn.rollback();
                        throw ex;
                    }
                }
            }
        }
    }

    /**
     * Transfers "stchallenge" records and corresponding "stchallengeqa" records.
     *
     * @param prodConn a connection to the production (destination) database
     * @param devConn  a connection to the development (source) database
     * @param report   list to which to add report lines
     * @throws SQLException if there is an error accessing the database
     */
    private void doStchallenge(final DbConnection prodConn, final DbConnection devConn,
                               final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("* Copying 'stchallenge' and corresponding 'stchallengeqa' records...");
        report.add(CoreConstants.EMPTY);

        final Map<RawStchallenge, List<RawStchallengeqa>> stchallengeOnDev = new HashMap<>(10);

        // Gather all "stchallenge" rows since the start time

        final String sql1 = "SELECT * FROM stchallenge"
                            + " WHERE exam_dt > ?"
                            + " OR (exam_dt = ? AND finish_time >= ?)";

        try (final PreparedStatement ps = devConn.prepareStatement(sql1)) {
            ps.setDate(1, this.sinceDate);
            ps.setDate(2, this.sinceDate);
            ps.setInt(3, this.sinceTime);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final RawStchallenge stchallenge = RawStchallenge.fromResultSet(rs);
                    final List<RawStchallengeqa> qalist = new ArrayList<>(24);
                    stchallengeOnDev.put(stchallenge, qalist);
                }
            }
        }

        if (stchallengeOnDev.isEmpty()) {
            report.add(INDENT4 + "There are no stchallenge records to copy.");
        } else {
            report.add(INDENT4 + "Found " + stchallengeOnDev.size() + " stchallenge records to copy.");

            // Gather all "stchallengeqa" rows associated with each "stchallenge" row

            final String sql2 = "SELECT * FROM stchallengeqa"
                                + " WHERE stu_id=? AND course=? AND version=?"
                                + " AND exam_dt=? AND finish_time=?";

            try (final PreparedStatement ps = devConn.prepareStatement(sql2)) {
                for (final Map.Entry<RawStchallenge, List<RawStchallengeqa>> entry : stchallengeOnDev.entrySet()) {
                    final RawStchallenge stchallenge = entry.getKey();

                    ps.setString(1, stchallenge.stuId);
                    ps.setString(2, stchallenge.course);
                    ps.setString(3, stchallenge.version);
                    ps.setDate(4, Date.valueOf(stchallenge.examDt));
                    if (stchallenge.finishTime == null) {
                        ps.setNull(5, Types.INTEGER);
                    } else {
                        ps.setInt(5, stchallenge.finishTime.intValue());
                    }

                    try (final ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            entry.getValue().add(RawStchallengeqa.fromResultSet(rs));
                        }
                    }

                    if (entry.getValue().size() < 20) {
                        report.add(INDENT4 + "*** WARNING: stchallenge record for student "
                                   + stchallenge.stuId + " has only " + entry.getValue().size()
                                   + " stchallengeqa records.");
                    }
                }
            }

            // Sweep through each exam, check for its existence on PROD, and if found, remove from
            // consideration

            final Iterator<Map.Entry<RawStchallenge, List<RawStchallengeqa>>> entryIter =
                    stchallengeOnDev.entrySet().iterator();

            while (entryIter.hasNext()) {

                final Map.Entry<RawStchallenge, List<RawStchallengeqa>> entry = entryIter.next();
                final RawStchallenge stchallenge = entry.getKey();

                final String sql3 = "SELECT * FROM stchallenge"
                                    + " WHERE stu_id=?"
                                    + " AND course=?"
                                    + " AND version=?"
                                    + " AND exam_dt=?"
                                    + " AND finish_time=?";

                try (final PreparedStatement ps = prodConn.prepareStatement(sql3)) {

                    ps.setString(1, stchallenge.stuId);
                    ps.setString(2, stchallenge.course);
                    ps.setString(3, stchallenge.version);
                    ps.setDate(4, Date.valueOf(stchallenge.examDt));
                    if (stchallenge.finishTime == null) {
                        ps.setNull(5, Types.INTEGER);
                    } else {
                        ps.setInt(5, stchallenge.finishTime.intValue());
                    }

                    try (final ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Record exists in PROD already - skip
                            entryIter.remove();
                        }
                    }
                }
            }

            if (stchallengeOnDev.isEmpty()) {
                report.add(INDENT4 + "All stchallenge records already exist in PROD.");
            } else {
                report.add(INDENT4 + stchallengeOnDev.size()
                           + " stchallenge records do not exist in PROD.");

                // Insert in PROD
                if (!DEBUG) {
                    int stchallengeInsCt = 0;
                    int stchallengeqaInsCt = 0;

                    try {
                        final String sql4 = "INSERT INTO stchallenge (stu_id,course,"
                                            + "version,academic_yr,exam_dt,start_time,finish_time,"
                                            + "last_name,first_name,middle_initial,seq_nbr,"
                                            + "serial_nbr,score,passed,how_validated) "
                                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql4)) {

                            for (final Map.Entry<RawStchallenge, List<RawStchallengeqa>> entry :
                                    stchallengeOnDev.entrySet()) {
                                final RawStchallenge stchallenge = entry.getKey();

                                ps.setString(1, stchallenge.stuId);
                                ps.setString(2, stchallenge.course);
                                ps.setString(3, stchallenge.version);
                                ps.setString(4, stchallenge.academicYr);
                                ps.setDate(5, Date.valueOf(stchallenge.examDt));
                                if (stchallenge.startTime == null) {
                                    ps.setNull(6, Types.INTEGER);
                                } else {
                                    ps.setInt(6, stchallenge.startTime.intValue());
                                }
                                if (stchallenge.finishTime == null) {
                                    ps.setNull(7, Types.INTEGER);
                                } else {
                                    ps.setInt(7, stchallenge.finishTime.intValue());
                                }
                                ps.setString(8, stchallenge.lastName);
                                ps.setString(9, stchallenge.firstName);
                                ps.setString(10, stchallenge.middleInitial);
                                if (stchallenge.seqNbr == null) {
                                    ps.setNull(11, Types.INTEGER);
                                } else {
                                    ps.setInt(11, stchallenge.seqNbr.intValue());
                                }
                                if (stchallenge.serialNbr == null) {
                                    ps.setNull(12, Types.INTEGER);
                                } else {
                                    ps.setLong(12, stchallenge.serialNbr.longValue());
                                }
                                if (stchallenge.score == null) {
                                    ps.setNull(13, Types.INTEGER);
                                } else {
                                    ps.setInt(13, stchallenge.score.intValue());
                                }
                                ps.setString(14, stchallenge.passed);
                                ps.setString(15, stchallenge.howValidated);

                                ps.executeUpdate();
                                ++stchallengeInsCt;
                            }
                        }

                        final String sql5 = "INSERT INTO stchallengeqa (stu_id,"
                                            + "course,version,exam_dt,finish_time,question_nbr,"
                                            + "stu_answer,ans_correct) VALUES (?,?,?,?,?,?,?,?)";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql5)) {

                            for (final List<RawStchallengeqa> list : stchallengeOnDev.values()) {
                                for (final RawStchallengeqa stmpeqa : list) {

                                    ps.setString(1, stmpeqa.stuId);
                                    ps.setString(2, stmpeqa.course);
                                    ps.setString(3, stmpeqa.version);
                                    ps.setDate(4, Date.valueOf(stmpeqa.examDt));
                                    if (stmpeqa.finishTime == null) {
                                        ps.setNull(5, Types.INTEGER);
                                    } else {
                                        ps.setInt(5, stmpeqa.finishTime.intValue());
                                    }
                                    if (stmpeqa.questionNbr == null) {
                                        ps.setNull(6, Types.INTEGER);
                                    } else {
                                        ps.setInt(6, stmpeqa.questionNbr.intValue());
                                    }
                                    ps.setString(7, stmpeqa.stuAnswer);
                                    ps.setString(8, stmpeqa.ansCorrect);

                                    ps.executeUpdate();
                                    ++stchallengeqaInsCt;
                                }
                            }
                        }

                        prodConn.commit();

                        report.add(INDENT4 + "Copied " + stchallengeInsCt + " stchallenge records to PROD.");
                        report.add(INDENT4 + "Copied " + stchallengeqaInsCt + " stchallengeqa records to PROD.");

                    } catch (final SQLException ex) {
                        report.add(INDENT4 + "*** ERROR: SQLException: " + ex.getMessage());
                        Throwable th = ex;
                        while (th != null) {
                            for (final StackTraceElement st : th.getStackTrace()) {
                                report.add(INDENT8 + st.toString());
                            }
                            th = th.getCause();
                        }
                        prodConn.rollback();
                        throw ex;
                    }
                }
            }
        }
    }

    /**
     * Transfers "mpe_credit" records.
     *
     * @param prodConn a connection to the production (destination) database
     * @param devConn  a connection to the development (source) database
     * @param report   list to which to add report lines
     * @throws SQLException if there is an error accessing the database
     */
    private void doMpeCredit(final DbConnection prodConn, final DbConnection devConn,
                             final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("* Copying 'mpe_credit' records...");
        report.add(CoreConstants.EMPTY);

        final List<RawMpeCredit> mpeCreditOnDev = new ArrayList<>(10);

        // Gather all "mpe_credit" rows since the start time

        final String sql1 = "SELECT * FROM mpe_credit"
                            + " WHERE exam_dt >= ?";

        try (final PreparedStatement ps = devConn.prepareStatement(sql1)) {
            ps.setDate(1, this.sinceDate);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mpeCreditOnDev.add(RawMpeCredit.fromResultSet(rs));
                }
            }
        }

        if (mpeCreditOnDev.isEmpty()) {
            report.add(INDENT4 + "There are no mpe_credit records to copy.");
        } else {
            report.add(INDENT4 + "Found " + mpeCreditOnDev.size() + " mpe_credit records to copy.");

            // Sweep through each row, check for its existence on PROD, and if found, remove from
            // consideration

            final Iterator<RawMpeCredit> iter = mpeCreditOnDev.iterator();

            while (iter.hasNext()) {
                final RawMpeCredit mpeCredit = iter.next();

                final String sql3 = "SELECT * FROM mpe_credit"
                                    + " WHERE stu_id=?"
                                    + " AND course=?"
                                    + " AND version=?"
                                    + " AND serial_nbr=?"
                                    + " AND exam_dt=?";

                try (final PreparedStatement ps = prodConn.prepareStatement(sql3)) {

                    ps.setString(1, mpeCredit.stuId);
                    ps.setString(2, mpeCredit.course);
                    ps.setString(3, mpeCredit.version);
                    if (mpeCredit.serialNbr == null) {
                        ps.setNull(4, Types.INTEGER);
                    } else {
                        ps.setLong(4, mpeCredit.serialNbr.longValue());
                    }
                    ps.setDate(5, Date.valueOf(mpeCredit.examDt));

                    try (final ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Record exists in PROD already - skip
                            iter.remove();
                        }
                    }
                }
            }

            if (mpeCreditOnDev.isEmpty()) {
                report.add(INDENT4 + "All mpe_credit records already exist in PROD.");
            } else {
                report.add(INDENT4 + mpeCreditOnDev.size() + " mpe_credit records do not exist in PROD.");

                // Insert in PROD
                if (!DEBUG) {
                    int mpeCreditInsertCount = 0;

                    try {
                        final String sql4 = "INSERT INTO mpe_credit (stu_id,course,"
                                            + "exam_placed,exam_dt,dt_cr_refused,serial_nbr,version,"
                                            + "exam_source) VALUES (?,?,?,?,?,?,?,?)";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql4)) {

                            for (final RawMpeCredit mpeCredit : mpeCreditOnDev) {

                                ps.setString(1, mpeCredit.stuId);
                                ps.setString(2, mpeCredit.course);
                                ps.setString(3, mpeCredit.examPlaced);
                                ps.setDate(4, Date.valueOf(mpeCredit.examDt));
                                if (mpeCredit.dtCrRefused == null) {
                                    ps.setNull(5, Types.DATE);
                                } else {
                                    ps.setDate(5, Date.valueOf(mpeCredit.dtCrRefused));
                                }
                                if (mpeCredit.serialNbr == null) {
                                    ps.setNull(6, Types.INTEGER);
                                } else {
                                    ps.setLong(6, mpeCredit.serialNbr.longValue());
                                }
                                ps.setString(7, mpeCredit.version);
                                ps.setString(8, mpeCredit.examSource);

                                ps.executeUpdate();
                                ++mpeCreditInsertCount;
                            }
                        }

                        prodConn.commit();

                        report.add(INDENT4 + "Copied " + mpeCreditInsertCount + " mpe_credit records to PROD.");

                    } catch (final SQLException ex) {
                        report.add(INDENT4 + "*** ERROR: SQLException: " + ex.getMessage());
                        Throwable th = ex;
                        while (th != null) {
                            for (final StackTraceElement st : th.getStackTrace()) {
                                report.add(INDENT8 + st.toString());
                            }
                            th = th.getCause();
                        }
                        prodConn.rollback();
                        throw ex;
                    }
                }
            }
        }
    }

    /**
     * Transfers "mpecr_denied" records.
     *
     * @param prodConn a connection to the production (destination) database
     * @param devConn  a connection to the development (source) database
     * @param report   list to which to add report lines
     * @throws SQLException if there is an error accessing the database
     */
    private void doMpecrDenied(final DbConnection prodConn, final DbConnection devConn,
                               final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("* Copying 'mpecr_denied' records...");
        report.add(CoreConstants.EMPTY);

        final List<RawMpecrDenied> mpeCrDeniedOnDev = new ArrayList<>(100);

        // Gather all "mpecr_denied" rows since the start time

        final String sql1 = "SELECT * FROM mpecr_denied"
                            + " WHERE exam_dt >= ?";

        try (final PreparedStatement ps = devConn.prepareStatement(sql1)) {
            ps.setDate(1, this.sinceDate);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mpeCrDeniedOnDev.add(RawMpecrDenied.fromResultSet(rs));
                }
            }
        }

        if (mpeCrDeniedOnDev.isEmpty()) {
            report.add(INDENT4 + "There are no mpecr_denied records to copy.");
        } else {
            report.add(INDENT4 + "Found " + mpeCrDeniedOnDev.size() + " mpecr_denied records to copy.");

            // Sweep through each row, check for its existence on PROD, and if found, remove from
            // consideration

            final Iterator<RawMpecrDenied> iter = mpeCrDeniedOnDev.iterator();

            while (iter.hasNext()) {
                final RawMpecrDenied mpeCrDenied = iter.next();

                final String sql3 = "SELECT * FROM mpecr_denied"
                                    + " WHERE stu_id=?"
                                    + " AND course=?"
                                    + " AND version=?"
                                    + " AND serial_nbr=?"
                                    + " AND exam_dt=?";

                try (final PreparedStatement ps = prodConn.prepareStatement(sql3)) {

                    ps.setString(1, mpeCrDenied.stuId);
                    ps.setString(2, mpeCrDenied.course);
                    ps.setString(3, mpeCrDenied.version);
                    if (mpeCrDenied.serialNbr == null) {
                        ps.setNull(4, Types.INTEGER);
                    } else {
                        ps.setLong(4, mpeCrDenied.serialNbr.longValue());
                    }
                    ps.setDate(5, Date.valueOf(mpeCrDenied.examDt));

                    try (final ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Record exists in PROD already - skip
                            iter.remove();
                        }
                    }
                }
            }

            if (mpeCrDeniedOnDev.isEmpty()) {
                report.add(INDENT4 + "All mpecr_denied records already exist in PROD.");
            } else {
                report.add(INDENT4 + mpeCrDeniedOnDev.size() + " mpecr_denied records do not exist in PROD.");

                // Insert in PROD
                if (!DEBUG) {
                    int mpeCrDeniedInsCt = 0;

                    try {
                        final String sql4 = "INSERT INTO mpecr_denied (stu_id,course,"
                                            + "exam_placed,exam_dt,why_denied,serial_nbr,version,"
                                            + "exam_source) VALUES (?,?,?,?,?,?,?,?)";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql4)) {

                            for (final RawMpecrDenied mpeCredit : mpeCrDeniedOnDev) {

                                ps.setString(1, mpeCredit.stuId);
                                ps.setString(2, mpeCredit.course);
                                ps.setString(3, mpeCredit.examPlaced);
                                ps.setDate(4, Date.valueOf(mpeCredit.examDt));
                                ps.setString(5, mpeCredit.whyDenied);
                                if (mpeCredit.serialNbr == null) {
                                    ps.setNull(6, Types.INTEGER);
                                } else {
                                    ps.setLong(6, mpeCredit.serialNbr.longValue());
                                }
                                ps.setString(7, mpeCredit.version);
                                ps.setString(8, mpeCredit.examSource);

                                ps.executeUpdate();
                                ++mpeCrDeniedInsCt;
                            }
                        }

                        prodConn.commit();

                        report.add(INDENT4 + "Copied " + mpeCrDeniedInsCt + " mpecr_denied records to PROD.");
                    } catch (final SQLException ex) {
                        report.add(INDENT4 + "*** ERROR: SQLException: " + ex.getMessage());
                        Throwable th = ex;
                        while (th != null) {
                            for (final StackTraceElement st : th.getStackTrace()) {
                                report.add(INDENT8 + st.toString());
                            }
                            th = th.getCause();
                        }
                        prodConn.rollback();
                        throw ex;
                    }
                }
            }
        }
    }

    /**
     * Transfers "mpe_log" records.
     *
     * @param prodConn a connection to the production (destination) database
     * @param devConn  a connection to the development (source) database
     * @param report   list to which to add report lines
     * @throws SQLException if there is an error accessing the database
     */
    private void doMpeLog(final DbConnection prodConn, final DbConnection devConn,
                          final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("* Copying 'mpe_log' records...");
        report.add(CoreConstants.EMPTY);

        final List<RawMpeLog> mpeLogOnDev = new ArrayList<>(10);

        // Gather all "mpe_log" rows since the start time

        final String sql1 = "SELECT * FROM mpe_log"
                            + " WHERE exam_dt >= ?";

        try (final PreparedStatement ps = devConn.prepareStatement(sql1)) {
            ps.setDate(1, this.sinceDate);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mpeLogOnDev.add(RawMpeLog.fromResultSet(rs));
                }
            }
        }

        if (mpeLogOnDev.isEmpty()) {
            report.add(INDENT4 + "There are no mpe_log records to copy.");
        } else {
            report.add(INDENT4 + "Found " + mpeLogOnDev.size() + " mpe_log records to copy.");

            // Sweep through each row, check for its existence on PROD, and if found, remove from
            // consideration

            final Iterator<RawMpeLog> iter = mpeLogOnDev.iterator();

            while (iter.hasNext()) {
                final RawMpeLog mpeLog = iter.next();

                final String sql3 = "SELECT * FROM mpe_log"
                                    + " WHERE stu_id=?"
                                    + " AND course=?"
                                    + " AND version=?"
                                    + " AND serial_nbr=?"
                                    + " AND start_dt=?"
                                    + " AND start_time=?";

                try (final PreparedStatement ps = prodConn.prepareStatement(sql3)) {

                    ps.setString(1, mpeLog.stuId);
                    ps.setString(2, mpeLog.course);
                    ps.setString(3, mpeLog.version);
                    if (mpeLog.serialNbr == null) {
                        ps.setNull(4, Types.INTEGER);
                    } else {
                        ps.setLong(4, mpeLog.serialNbr.longValue());
                    }
                    ps.setDate(5, Date.valueOf(mpeLog.startDt));
                    if (mpeLog.serialNbr == null) {
                        ps.setNull(6, Types.INTEGER);
                    } else {
                        ps.setInt(6, mpeLog.startTime.intValue());
                    }

                    try (final ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Record exists in PROD already - skip
                            iter.remove();
                        }
                    }
                }
            }

            if (mpeLogOnDev.isEmpty()) {
                report.add(INDENT4 + "All mpe_log records already exist in PROD.");
            } else {
                report.add(INDENT4 + mpeLogOnDev.size() + " mpe_log records do not exist in PROD.");

                // Insert in PROD
                if (!DEBUG) {
                    int mpeLogInsertCount = 0;

                    try {
                        final String sql4 = "INSERT INTO mpe_log (stu_id,academic_yr,"
                                            + "course,version,start_dt,exam_dt,recover_dt,serial_nbr,"
                                            + "start_time,calc_nbr) VALUES (?,?,?,?,?,?,?,?,?,?)";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql4)) {

                            for (final RawMpeLog mpeLog : mpeLogOnDev) {

                                ps.setString(1, mpeLog.stuId);
                                ps.setString(2, mpeLog.academicYr);
                                ps.setString(3, mpeLog.course);
                                ps.setString(4, mpeLog.version);
                                ps.setDate(5, Date.valueOf(mpeLog.startDt));
                                ps.setDate(6, Date.valueOf(mpeLog.examDt));
                                if (mpeLog.recoverDt == null) {
                                    ps.setNull(7, Types.DATE);
                                } else {
                                    ps.setDate(7, Date.valueOf(mpeLog.recoverDt));
                                }
                                if (mpeLog.serialNbr == null) {
                                    ps.setNull(8, Types.INTEGER);
                                } else {
                                    ps.setLong(8, mpeLog.serialNbr.longValue());
                                }
                                if (mpeLog.startTime == null) {
                                    ps.setNull(9, Types.INTEGER);
                                } else {
                                    ps.setLong(9, mpeLog.startTime.longValue());
                                }
                                ps.setString(10, mpeLog.calcNbr);

                                ps.executeUpdate();
                                ++mpeLogInsertCount;
                            }
                        }

                        prodConn.commit();

                        report.add(INDENT4 + "Copied " + mpeLogInsertCount + " mpe_log records to PROD.");
                    } catch (final SQLException ex) {
                        report.add(INDENT4 + "*** ERROR: SQLException: " + ex.getMessage());
                        Throwable th = ex;
                        while (th != null) {
                            for (final StackTraceElement st : th.getStackTrace()) {
                                report.add(INDENT8 + st.toString());
                            }
                            th = th.getCause();
                        }
                        prodConn.rollback();
                        throw ex;
                    }
                }
            }
        }
    }

    /**
     * Transfers "mpscorequeue" records.
     *
     * @param prodConn a connection to the production (destination) database
     * @param devConn  a connection to the development (source) database
     * @param report   list to which to add report lines
     * @throws SQLException if there is an error accessing the database
     */
    private void doMpescorequeue(final DbConnection prodConn, final DbConnection devConn,
                                 final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("* Copying 'mpscorequeue' records...");
        report.add(CoreConstants.EMPTY);

        final List<RawMpscorequeue> mpscorequeueOnDev = new ArrayList<>(10);

        // Gather all "mpscorequeue" rows since the start time

        final String sql1 = "SELECT * FROM mpscorequeue"
                            + " WHERE date(test_date) >= ?";

        try (final PreparedStatement ps = devConn.prepareStatement(sql1)) {
            ps.setDate(1, this.sinceDate);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mpscorequeueOnDev.add(RawMpscorequeue.fromResultSet(rs));
                }
            }
        }

        if (mpscorequeueOnDev.isEmpty()) {
            report.add(INDENT4 + "There are no mpscorequeue records to copy.");
        } else {
            report.add(INDENT4 + "Found " + mpscorequeueOnDev.size() + " mpscorequeue records to copy.");

            // Sweep through each row, check for its existence on PROD, and if found, remove from
            // consideration

            final Iterator<RawMpscorequeue> iter = mpscorequeueOnDev.iterator();

            while (iter.hasNext()) {
                final RawMpscorequeue mpscorequeue = iter.next();

                final String sql3 = "SELECT * FROM mpscorequeue"
                                    + " WHERE pidm=?"
                                    + " AND test_code=?"
                                    + " AND test_date=?"
                                    + " AND test_score=?";

                final Timestamp ts = mpscorequeue.testDate == null ? null : Timestamp.valueOf(mpscorequeue.testDate);

                try (final PreparedStatement ps = prodConn.prepareStatement(sql3)) {

                    if (mpscorequeue.pidm == null) {
                        ps.setNull(1, Types.INTEGER);
                    } else {
                        ps.setInt(1, mpscorequeue.pidm.intValue());
                    }
                    ps.setString(2, mpscorequeue.testCode);
                    ps.setTimestamp(3, ts);
                    ps.setString(4, mpscorequeue.testScore);

                    try (final ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Record exists in PROD already - skip
                            iter.remove();
                        }
                    }
                }
            }

            if (mpscorequeueOnDev.isEmpty()) {
                report.add(INDENT4 + "All mpscorequeue records already exist in PROD.");
            } else {
                report.add(INDENT4 + mpscorequeueOnDev.size() + " mpscorequeue records do not exist in PROD.");

                // Insert in PROD
                if (!DEBUG) {
                    int stmathplanInsCt = 0;

                    try {
                        final String sql4 = "INSERT INTO mpscorequeue (pidm,"
                                            + "test_code,test_date,test_score) VALUES (?,?,?,?)";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql4)) {

                            for (final RawMpscorequeue mpscorequeue : mpscorequeueOnDev) {
                                final Timestamp ts = mpscorequeue.testDate == null ? null
                                        : Timestamp.valueOf(mpscorequeue.testDate);

                                if (mpscorequeue.pidm == null) {
                                    ps.setNull(1, Types.INTEGER);
                                } else {
                                    ps.setInt(1, mpscorequeue.pidm.intValue());
                                }
                                ps.setString(2, mpscorequeue.testCode);
                                ps.setTimestamp(3, ts);
                                ps.setString(4, mpscorequeue.testScore);

                                ps.executeUpdate();
                                ++stmathplanInsCt;
                            }
                        }

                        prodConn.commit();

                        report.add(INDENT4 + "Copied " + stmathplanInsCt + " mpscorequeue records to PROD.");
                    } catch (final SQLException ex) {
                        report.add(INDENT4 + "*** ERROR: SQLException: " + ex.getMessage());
                        Throwable th = ex;
                        while (th != null) {
                            for (final StackTraceElement st : th.getStackTrace()) {
                                report.add(INDENT8 + st.toString());
                            }
                            th = th.getCause();
                        }
                        prodConn.rollback();
                        throw ex;
                    }
                }
            }
        }
    }

    /**
     * Transfers "stsurveyqa" records.
     *
     * @param prodConn a connection to the production (destination) database
     * @param devConn  a connection to the development (source) database
     * @param report   list to which to add report lines
     * @throws SQLException if there is an error accessing the database
     */
    private void doStsurveyqa(final DbConnection prodConn, final DbConnection devConn,
                              final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("* Copying 'stsurveyqa' records...");
        report.add(CoreConstants.EMPTY);

        final List<RawStsurveyqa> stsurveyqaOnDev = new ArrayList<>(10);

        // Gather all "stsurveyqa" rows since the start time

        final String sql1 = "SELECT * FROM stsurveyqa"
                            + " WHERE exam_dt > ?"
                            + " OR (exam_dt = ? AND finish_time >= ?)";

        try (final PreparedStatement ps = devConn.prepareStatement(sql1)) {
            ps.setDate(1, this.sinceDate);
            ps.setDate(2, this.sinceDate);
            ps.setInt(3, this.sinceTime);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stsurveyqaOnDev.add(RawStsurveyqa.fromResultSet(rs));
                }
            }
        }

        if (stsurveyqaOnDev.isEmpty()) {
            report.add(INDENT4 + "There are no stsurveyqa records to copy.");
        } else {
            report.add(INDENT4 + "Found " + stsurveyqaOnDev.size() + " stsurveyqa records to copy.");

            // Sweep through each row, check for its existence on PROD, and if found, remove from
            // consideration

            final Iterator<RawStsurveyqa> iter = stsurveyqaOnDev.iterator();

            while (iter.hasNext()) {
                final RawStsurveyqa stsurveyqa = iter.next();

                final String sql3 = "SELECT * FROM stsurveyqa"
                                    + " WHERE stu_id=?"
                                    + " AND version=?"
                                    + " AND exam_dt=?"
                                    + " AND finish_time=?"
                                    + " AND survey_nbr=?";

                try (final PreparedStatement ps = prodConn.prepareStatement(sql3)) {

                    ps.setString(1, stsurveyqa.stuId);
                    ps.setString(2, stsurveyqa.version);
                    ps.setDate(3, Date.valueOf(stsurveyqa.examDt));
                    if (stsurveyqa.finishTime == null) {
                        ps.setNull(4, Types.INTEGER);
                    } else {
                        ps.setInt(4, stsurveyqa.finishTime.intValue());
                    }
                    if (stsurveyqa.surveyNbr == null) {
                        ps.setNull(5, Types.INTEGER);
                    } else {
                        ps.setInt(5, stsurveyqa.surveyNbr.intValue());
                    }

                    try (final ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Record exists in PROD already - skip
                            iter.remove();
                        }
                    }
                }
            }

            if (stsurveyqaOnDev.isEmpty()) {
                report.add(INDENT4 + "All stsurveyqa records already exist in PROD.");
            } else {
                report.add(INDENT4 + stsurveyqaOnDev.size() + " stsurveyqa records do not exist in PROD.");

                // Insert in PROD
                if (!DEBUG) {
                    int stsurveyqaInsCt = 0;

                    try {
                        final String sql4 = "INSERT INTO stsurveyqa (stu_id,version,"
                                            + "exam_dt,survey_nbr,stu_answer,finish_time) "
                                            + "VALUES (?,?,?,?,?,?)";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql4)) {

                            for (final RawStsurveyqa stsurveyqa : stsurveyqaOnDev) {

                                ps.setString(1, stsurveyqa.stuId);
                                ps.setString(2, stsurveyqa.version);
                                ps.setDate(3, Date.valueOf(stsurveyqa.examDt));
                                if (stsurveyqa.surveyNbr == null) {
                                    ps.setNull(4, Types.INTEGER);
                                } else {
                                    ps.setInt(4, stsurveyqa.surveyNbr.intValue());
                                }
                                ps.setString(5, stsurveyqa.stuAnswer);
                                if (stsurveyqa.finishTime == null) {
                                    ps.setNull(6, Types.INTEGER);
                                } else {
                                    ps.setInt(6, stsurveyqa.finishTime.intValue());
                                }

                                ps.executeUpdate();
                                ++stsurveyqaInsCt;
                            }
                        }

                        prodConn.commit();

                        report.add(INDENT4 + "Copied " + stsurveyqaInsCt + " stsurveyqa records to PROD.");
                    } catch (final SQLException ex) {
                        report.add(INDENT4 + "*** ERROR: SQLException: " + ex.getMessage());
                        Throwable th = ex;
                        while (th != null) {
                            for (final StackTraceElement st : th.getStackTrace()) {
                                report.add(INDENT8 + st.toString());
                            }
                            th = th.getCause();
                        }
                        prodConn.rollback();
                        throw ex;
                    }
                }
            }
        }
    }

    /**
     * Transfers "stmathplan" records.
     *
     * @param prodConn a connection to the production (destination) database
     * @param devConn  a connection to the development (source) database
     * @param report   list to which to add report lines
     * @throws SQLException if there is an error accessing the database
     */
    private void doStmathplan(final DbConnection prodConn, final DbConnection devConn,
                              final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("* Copying 'stmathplan' records...");
        report.add(CoreConstants.EMPTY);

        final List<RawStmathplan> stmathplanOnDev = new ArrayList<>(10);

        // Gather all "stmathplan" rows since the start time

        final String sql1 = "SELECT * FROM stmathplan"
                            + " WHERE exam_dt >= ?";

        try (final PreparedStatement ps = devConn.prepareStatement(sql1)) {
            ps.setDate(1, this.sinceDate);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stmathplanOnDev.add(RawStmathplan.fromResultSet(rs));
                }
            }
        }

        if (stmathplanOnDev.isEmpty()) {
            report.add(INDENT4 + "There are no stmathplan records to copy.");
        } else {
            report.add(INDENT4 + "Found " + stmathplanOnDev.size() + " stmathplan records to copy.");

            // Sweep through each row, check for its existence on PROD, and if found, remove from consideration

            final Iterator<RawStmathplan> iter = stmathplanOnDev.iterator();

            while (iter.hasNext()) {
                final RawStmathplan stmathplan = iter.next();

                final String sql3 = "SELECT * FROM stmathplan"
                                    + " WHERE stu_id=?"
                                    + " AND version=?"
                                    + " AND exam_dt=?"
                                    + " AND finish_time=?"
                                    + " AND survey_nbr=?";

                try (final PreparedStatement ps = prodConn.prepareStatement(sql3)) {

                    ps.setString(1, stmathplan.stuId);
                    ps.setString(2, stmathplan.version);
                    ps.setDate(3, Date.valueOf(stmathplan.examDt));
                    if (stmathplan.finishTime == null) {
                        ps.setNull(4, Types.INTEGER);
                    } else {
                        ps.setInt(4, stmathplan.finishTime.intValue());
                    }
                    if (stmathplan.surveyNbr == null) {
                        ps.setNull(5, Types.INTEGER);
                    } else {
                        ps.setInt(5, stmathplan.surveyNbr.intValue());
                    }

                    try (final ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Record exists in PROD already - skip
                            iter.remove();
                        }
                    }
                }
            }

            if (stmathplanOnDev.isEmpty()) {
                report.add(INDENT4 + "All stmathplan records already exist in PROD.");
            } else {
                report.add(INDENT4 + stmathplanOnDev.size() + " stmathplan records do not exist in PROD.");

                // Insert in PROD
                if (!DEBUG) {
                    int stmathplanInsCt = 0;

                    try {
                        final String sql4 = "INSERT INTO stmathplan (stu_id,pidm,apln_term,version,exam_dt,"
                                            + "survey_nbr,stu_answer,finish_time,session) VALUES (?,?,?,?,?,?,?,?,?)";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql4)) {

                            for (final RawStmathplan stmathplan : stmathplanOnDev) {

                                ps.setString(1, stmathplan.stuId);
                                if (stmathplan.pidm == null) {
                                    ps.setNull(2, Types.INTEGER);
                                } else {
                                    ps.setInt(2, stmathplan.pidm.intValue());
                                }
                                ps.setString(3, stmathplan.aplnTerm);
                                ps.setString(4, stmathplan.version);
                                ps.setDate(5, Date.valueOf(stmathplan.examDt));

                                if (stmathplan.surveyNbr == null) {
                                    ps.setNull(6, Types.INTEGER);
                                } else {
                                    ps.setLong(6, stmathplan.surveyNbr.longValue());
                                }
                                ps.setString(7, stmathplan.stuAnswer);
                                if (stmathplan.finishTime == null) {
                                    ps.setNull(8, Types.INTEGER);
                                } else {
                                    ps.setInt(8, stmathplan.finishTime.intValue());
                                }
                                if (stmathplan.session == null) {
                                    ps.setNull(9, Types.INTEGER);
                                } else {
                                    ps.setLong(9, stmathplan.session.longValue());
                                }

                                ps.executeUpdate();
                                ++stmathplanInsCt;
                            }
                        }

                        prodConn.commit();

                        report.add(INDENT4 + "Copied " + stmathplanInsCt + " stmathplan records to PROD.");
                    } catch (final SQLException ex) {
                        report.add(INDENT4 + "*** ERROR: SQLException: " + ex.getMessage());
                        Throwable th = ex;
                        while (th != null) {
                            for (final StackTraceElement st : th.getStackTrace()) {
                                report.add(INDENT8 + st.toString());
                            }
                            th = th.getCause();
                        }
                        prodConn.rollback();
                        throw ex;
                    }
                }
            }
        }
    }

    /**
     * Transfers "etext_key" records.
     *
     * @param prodConn a connection to the production (destination) database
     * @param devConn  a connection to the development (source) database
     * @param report   list to which to add report lines
     * @throws SQLException if there is an error accessing the database
     */
    private void doEtextKey(final DbConnection prodConn, final DbConnection devConn,
                            final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("* Copying 'etext_key' records...");
        report.add(CoreConstants.EMPTY);

        final List<RawEtextKey> etextKeyOnDev = new ArrayList<>(10);

        // Gather all "etext_key" rows since the start time

        final String sql1 = "SELECT * FROM etext_key WHERE active_dt >= ?";

        try (final PreparedStatement ps = devConn.prepareStatement(sql1)) {
            ps.setDate(1, this.sinceDate);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    etextKeyOnDev.add(RawEtextKey.fromResultSet(rs));
                }
            }
        }

        if (etextKeyOnDev.isEmpty()) {
            report.add(INDENT4 + "There are no etext_key records to copy.");
        } else {
            report.add(INDENT4 + "Found " + etextKeyOnDev.size() + " etext_key records to copy.");

            // Sweep through each row, check for its existence on PROD, and if found, remove from
            // consideration

            final Iterator<RawEtextKey> iter = etextKeyOnDev.iterator();

            while (iter.hasNext()) {
                final RawEtextKey etextKey = iter.next();

                final String sql3 = "SELECT * FROM etext_key"
                                    + " WHERE etext_id=?"
                                    + " AND etext_key=?"
                                    + " AND active_dt=?";

                try (final PreparedStatement ps = prodConn.prepareStatement(sql3)) {

                    ps.setString(1, etextKey.etextId);
                    ps.setString(2, etextKey.etextKey);
                    ps.setTimestamp(3, Timestamp.valueOf(etextKey.activeDt));

                    try (final ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Record exists in PROD already - skip
                            iter.remove();
                        }
                    }
                }
            }

            if (etextKeyOnDev.isEmpty()) {
                report.add(INDENT4 + "All etext_key records already exist in PROD.");
            } else {
                report.add(INDENT4 + etextKeyOnDev.size() + " etext_key records do not exist in PROD.");

                // Insert in PROD
                if (!DEBUG) {
                    int etextKeyInsertCount = 0;

                    // The record should already exist on PROD, but its active_dt field does not
                    // match - update rather than insert

                    try {
                        final String sql4 = "UPDATE etext_key set active_dt=?"
                                            + " WHERE etext_id=? AND etext_key=?";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql4)) {

                            for (final RawEtextKey etextKey : etextKeyOnDev) {

                                ps.setTimestamp(1, Timestamp.valueOf(etextKey.activeDt));
                                ps.setString(2, etextKey.etextId);
                                ps.setString(3, etextKey.etextKey);

                                ps.executeUpdate();
                                ++etextKeyInsertCount;
                            }
                        }

                        prodConn.commit();

                        report.add(INDENT4 + "Copied " + etextKeyInsertCount + " etext_key records to PROD.");
                    } catch (final SQLException ex) {
                        report.add(INDENT4 + "*** ERROR: SQLException: " + ex.getMessage());
                        Throwable th = ex;
                        while (th != null) {
                            for (final StackTraceElement st : th.getStackTrace()) {
                                report.add(INDENT8 + st.toString());
                            }
                            th = th.getCause();
                        }
                        prodConn.rollback();
                        throw ex;
                    }
                }
            }
        }
    }

    /**
     * Transfers "stetext" records.
     *
     * @param prodConn a connection to the production (destination) database
     * @param devConn  a connection to the development (source) database
     * @param report   list to which to add report lines
     * @throws SQLException if there is an error accessing the database
     */
    private void doStetext(final DbConnection prodConn, final DbConnection devConn,
                           final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("* Copying 'stetext' records...");
        report.add(CoreConstants.EMPTY);

        final List<RawStetext> stetextOnDev = new ArrayList<>(10);

        // Gather all "stetext" rows since the start time

        final String sql1 = "SELECT * FROM stetext"
                            + " WHERE active_dt >= ?";

        try (final PreparedStatement ps = devConn.prepareStatement(sql1)) {
            ps.setDate(1, this.sinceDate);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stetextOnDev.add(RawStetext.fromResultSet(rs));
                }
            }
        }

        if (stetextOnDev.isEmpty()) {
            report.add(INDENT4 + "There are no stetext records to copy.");
        } else {
            report.add(INDENT4 + "Found " + stetextOnDev.size() + " stetext records to copy.");

            // Sweep through each row, check for its existence on PROD, and if found, remove from
            // consideration

            final Iterator<RawStetext> iter = stetextOnDev.iterator();

            while (iter.hasNext()) {
                final RawStetext stetext = iter.next();

                final String sql3 = "SELECT * FROM stetext"
                                    + " WHERE stu_id=?"
                                    + " AND etext_id=?"
                                    + " AND active_dt=?";

                try (final PreparedStatement ps = prodConn.prepareStatement(sql3)) {

                    ps.setString(1, stetext.stuId);
                    ps.setString(2, stetext.etextId);
                    ps.setDate(3, Date.valueOf(stetext.activeDt));

                    try (final ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Record exists in PROD already - skip
                            iter.remove();
                        }
                    }
                }
            }

            if (stetextOnDev.isEmpty()) {
                report.add(INDENT4 + "All stetext records already exist in PROD.");
            } else {
                report.add(INDENT4 + stetextOnDev.size() + " stetext records do not exist in PROD.");

                // Insert in PROD
                if (!DEBUG) {
                    int stetextInsertCount = 0;

                    try {
                        final String sql4 = "INSERT INTO stetext (stu_id,etext_id,"
                                            + "active_dt,etext_key,expiration_dt,refund_deadline_dt,"
                                            + "refund_dt,refund_reason) VALUES (?,?,?,?,?,?,?,?)";

                        try (final PreparedStatement ps = prodConn.prepareStatement(sql4)) {

                            for (final RawStetext stetext : stetextOnDev) {

                                ps.setString(1, stetext.stuId);
                                ps.setString(2, stetext.etextId);
                                ps.setDate(3, Date.valueOf(stetext.activeDt));
                                ps.setString(4, stetext.etextKey);

                                if (stetext.expirationDt == null) {
                                    ps.setNull(5, Types.DATE);
                                } else {
                                    ps.setDate(5, Date.valueOf(stetext.expirationDt));
                                }

                                if (stetext.refundDeadlineDt == null) {
                                    ps.setNull(6, Types.DATE);
                                } else {
                                    ps.setDate(6, Date.valueOf(stetext.refundDeadlineDt));
                                }

                                if (stetext.refundDt == null) {
                                    ps.setNull(7, Types.DATE);
                                } else {
                                    ps.setDate(7, Date.valueOf(stetext.refundDt));
                                }
                                ps.setString(8, stetext.refundReason);

                                ps.executeUpdate();
                                ++stetextInsertCount;
                            }
                        }

                        prodConn.commit();

                        report.add(INDENT4 + "Copied " + stetextInsertCount + " stetext records to PROD.");
                    } catch (final SQLException ex) {
                        report.add(INDENT4 + "*** ERROR: SQLException: " + ex.getMessage());
                        Throwable th = ex;
                        while (th != null) {
                            for (final StackTraceElement st : th.getStackTrace()) {
                                report.add(INDENT8 + st.toString());
                            }
                            th = th.getCause();
                        }
                        prodConn.rollback();
                        throw ex;
                    }
                }
            }
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final TransferPlacementDevtoProd job = new TransferPlacementDevtoProd(CUTOVER_DATETIME);

        Log.fine(job.execute());
    }
}
