package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.DbUtils;
import dev.mathops.db.old.rawrecord.RawMpscorequeue;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "mpscorequeue" records.
 *
 * <pre>
 * Table:  'mpscorequeue'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * pidm                 integer                   no      PK
 * test_code            char(4)                   no      PK
 * test_date            datetime year to second   no      PK
 * test_score           char(1)                   no
 *
 * </pre>
 */
public enum RawMpscorequeueLogic {
    ;

    /** A commonly used string. */
    public static final String MC00 = "MC00";

    /** A commonly used string. */
    public static final String MC17 = "MC17";

    /** A commonly used string. */
    private static final String MC18 = "MC18";

    /** A commonly used string. */
    private static final String MC24 = "MC24";

    /** A commonly used string. */
    private static final String MC25 = "MC25";

    /** A commonly used string. */
    private static final String MC26 = "MC26";

    /** A commonly used string. */
    private static final String INSERTING_OPEN = "Inserting SORTEST test score: [";

    /** A commonly used string. */
    private static final String CLOSE = "]";

    /** A commonly used string. */
    private static final String FAILED_QUEUEING = "Failed to post score to BANNER - Queueing";

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawMpscorequeue record) throws SQLException {

        if (record.pidm == null || record.testCode == null || record.testDate == null || record.testScore == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO mpscorequeue (pidm,test_code,test_date,test_score) VALUES (",
                LogicUtils.sqlIntegerValue(record.pidm), ",",
                LogicUtils.sqlStringValue(record.testCode), ",",
                LogicUtils.sqlDateTimeValue(record.testDate), ",",
                LogicUtils.sqlStringValue(record.testScore), ")");

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean delete(final Cache cache, final RawMpscorequeue record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM mpscorequeue",
                " WHERE pidm=", LogicUtils.sqlIntegerValue(record.pidm),
                " AND test_code=", LogicUtils.sqlStringValue(record.testCode),
                " AND test_date=", LogicUtils.sqlDateTimeValue(record.testDate),
                " AND test_score=", LogicUtils.sqlStringValue(record.testScore));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawMpscorequeue> queryAll(final Cache cache) throws SQLException {

        final List<RawMpscorequeue> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM mpscorequeue")) {

            while (rs.next()) {
                result.add(RawMpscorequeue.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Queries all records with a specified PIDM.
     *
     * @param cache the data cache
     * @param pidm  the PIDM
     * @return the complete set of records in the database
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawMpscorequeue> queryByPidm(final Cache cache, final Integer pidm) throws SQLException {

        final List<RawMpscorequeue> result = new ArrayList<>(10);

        final String sql = SimpleBuilder.concat("SELECT * FROM mpscorequeue",
                " WHERE pidm=", LogicUtils.sqlIntegerValue(pidm));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawMpscorequeue.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Called when a student has taken a challenge exam and passed, earning credit in a course. This method simply
     * inserts a test result row in Banner, since this is the best result and cannot degrade an existing result. Even if
     * the student already had a "2" result, this will update the date/time in Banner.
     *
     * <p>
     * If the score cannot be inserted in BANNER, a record is inserted into the local score queue table for later
     * processing.
     *
     * <p>
     * This call commits the insert, whether it is done in the live SORTEST table or the local queue table.
     *
     * @param cache          the dat
     * @param liveConn       the LIVE database connection, checked out to this thread
     * @param pidm           the student's PIDM
     * @param courseId       the course ID in which credit was earned
     * @param examFinishTime the exam finish time
     * @throws SQLException if there is an error accessing the database
     */
    public static void postChallengeCredit(final Cache cache, final DbConnection liveConn, final Integer pidm,
                                           final String courseId, final LocalDateTime examFinishTime) throws SQLException {

        String testCode = null;
        if (RawRecordConstants.M117.equals(courseId)) {
            testCode = MC17;
        } else if (RawRecordConstants.M118.equals(courseId)) {
            testCode = MC18;
        } else if (RawRecordConstants.M124.equals(courseId)) {
            testCode = MC24;
        } else if (RawRecordConstants.M125.equals(courseId)) {
            testCode = MC25;
        } else if (RawRecordConstants.M126.equals(courseId)) {
            testCode = MC26;
        }

        if (testCode == null) {
            Log.warning("Unrecognized challenge course: ", courseId);
        } else {
            final RawMpscorequeue rec = new RawMpscorequeue(pidm, testCode, examFinishTime, "2");

            if (LogicUtils.isBannerDown()) {
                Log.warning("BANNER down - Queueing");
                insert(cache, rec);
            } else if (!insertSORTEST(liveConn, rec)) {
                Log.warning(FAILED_QUEUEING);
                insert(cache, rec);
            }
        }
    }

    /**
     * Called when a student has completed the placement tool with at least one course in which placement was earned.
     * This method attempts to query the BANNER table for all existing scores for the student, and then inserts records
     * for which the score has improved (or for which there was no existing score).
     *
     * <p>
     * If BANNER could not be queried or a score cannot be inserted in BANNER, a record is inserted into the local score
     * queue table for later processing.
     *
     * <p>
     * This call commits the insert, whether it is done in the live SORTEST table or the local queue table.
     *
     * @param cache           the datA cache
     * @param liveConn        the LIVE database connection, checked out to this thread
     * @param pidm            the student's PIDM
     * @param earnedPlacement the list of course IDs in which placement was earned
     * @param examFinishTime  the exam finish time
     * @throws SQLException if there is an error accessing the database
     */
    public static void postPlacementToolResult(final Cache cache, final DbConnection liveConn, final Integer pidm,
                                               final Iterable<String> earnedPlacement, final LocalDateTime examFinishTime)
            throws SQLException {

        List<RawMpscorequeue> existing = null;
        if (!LogicUtils.isBannerDown()) {
            try {
                existing = querySORTESTByStudent(liveConn, pidm);
            } catch (final SQLException ex) {
                Log.warning("Failed to look up SORTEST in Banner - indicating Banner is down for 15 minutes", ex);
                LogicUtils.indicateBannerDown();
            }
        }

        int mc00 = 0;
        int mc17 = 0;
        int mc18 = 0;
        int mc24 = 0;
        int mc25 = 0;
        int mc26 = 0;

        for (final String placeIn : earnedPlacement) {
            if (RawRecordConstants.M100C.equals(placeIn)) {
                mc00 = 2;
            } else if (RawRecordConstants.M117.equals(placeIn)) {
                mc17 = 1;
            } else if (RawRecordConstants.M118.equals(placeIn)) {
                mc18 = 1;
            } else if (RawRecordConstants.M124.equals(placeIn)) {
                mc24 = 1;
            } else if (RawRecordConstants.M125.equals(placeIn)) {
                mc25 = 1;
            } else if (RawRecordConstants.M126.equals(placeIn)) {
                mc26 = 1;
            }
        }

        if (LogicUtils.isBannerDown() || existing == null) {
            logActivity("BANNER down - queueing placement scores for PIDM " + pidm);

            final RawMpscorequeue rec00 = new RawMpscorequeue(pidm, MC00, examFinishTime, Integer.toString(mc00));
            final RawMpscorequeue rec17 = new RawMpscorequeue(pidm, MC17, examFinishTime, Integer.toString(mc17));
            final RawMpscorequeue rec18 = new RawMpscorequeue(pidm, MC18, examFinishTime, Integer.toString(mc18));
            final RawMpscorequeue rec24 = new RawMpscorequeue(pidm, MC24, examFinishTime, Integer.toString(mc24));
            final RawMpscorequeue rec25 = new RawMpscorequeue(pidm, MC25, examFinishTime, Integer.toString(mc25));
            final RawMpscorequeue rec26 = new RawMpscorequeue(pidm, MC26, examFinishTime, Integer.toString(mc26));

            insert(cache, rec00);
            insert(cache, rec17);
            insert(cache, rec18);
            insert(cache, rec24);
            insert(cache, rec25);
            insert(cache, rec26);
        } else {
            // Upgrade scores if existing scores are higher
            for (final RawMpscorequeue exist : existing) {
                if (MC00.equals(exist.testCode)) {
                    if ("1".equals(exist.testScore)) {
                        mc00 = Math.max(mc00, 1);
                    } else if ("2".equals(exist.testScore)) {
                        mc00 = 2;
                    }
                } else if (MC17.equals(exist.testCode)) {
                    if ("1".equals(exist.testScore)) {
                        mc17 = Math.max(mc17, 1);
                    } else if ("2".equals(exist.testScore)) {
                        mc17 = 2;
                    }
                } else if (MC18.equals(exist.testCode)) {
                    if ("1".equals(exist.testScore)) {
                        mc18 = Math.max(mc18, 1);
                    } else if ("2".equals(exist.testScore)) {
                        mc18 = 2;
                    }
                } else if (MC24.equals(exist.testCode)) {
                    if ("1".equals(exist.testScore)) {
                        mc24 = Math.max(mc24, 1);
                    } else if ("2".equals(exist.testScore)) {
                        mc24 = 2;
                    }
                } else if (MC25.equals(exist.testCode)) {
                    if ("1".equals(exist.testScore)) {
                        mc25 = Math.max(mc25, 1);
                    } else if ("2".equals(exist.testScore)) {
                        mc25 = 2;
                    }
                } else if (MC26.equals(exist.testCode)) {
                    if ("1".equals(exist.testScore)) {
                        mc26 = Math.max(mc26, 1);
                    } else if ("2".equals(exist.testScore)) {
                        mc26 = 2;
                    }
                }
            }

            final RawMpscorequeue rec00 = new RawMpscorequeue(pidm, MC00, examFinishTime, Integer.toString(mc00));
            final RawMpscorequeue rec17 = new RawMpscorequeue(pidm, MC17, examFinishTime, Integer.toString(mc17));
            final RawMpscorequeue rec18 = new RawMpscorequeue(pidm, MC18, examFinishTime, Integer.toString(mc18));
            final RawMpscorequeue rec24 = new RawMpscorequeue(pidm, MC24, examFinishTime, Integer.toString(mc24));
            final RawMpscorequeue rec25 = new RawMpscorequeue(pidm, MC25, examFinishTime, Integer.toString(mc25));
            final RawMpscorequeue rec26 = new RawMpscorequeue(pidm, MC26, examFinishTime, Integer.toString(mc26));

            // NOTE: We still insert all rows to update the test date

            Log.warning(INSERTING_OPEN, rec00, CLOSE);
            if (!insertSORTEST(liveConn, rec00)) {
                logActivity(FAILED_QUEUEING);
                insert(cache, rec00);
            }

            Log.warning(INSERTING_OPEN, rec17, CLOSE);
            if (!insertSORTEST(liveConn, rec17)) {
                logActivity(FAILED_QUEUEING);
                insert(cache, rec17);
            }

            Log.warning(INSERTING_OPEN, rec18, CLOSE);
            if (!insertSORTEST(liveConn, rec18)) {
                logActivity(FAILED_QUEUEING);
                insert(cache, rec18);
            }

            Log.warning(INSERTING_OPEN, rec24, CLOSE);
            if (!insertSORTEST(liveConn, rec24)) {
                logActivity(FAILED_QUEUEING);
                insert(cache, rec24);
            }

            Log.warning(INSERTING_OPEN, rec25, CLOSE);
            if (!insertSORTEST(liveConn, rec25)) {
                logActivity(FAILED_QUEUEING);
                insert(cache, rec25);
            }

            Log.warning(INSERTING_OPEN, rec26, CLOSE);
            if (!insertSORTEST(liveConn, rec26)) {
                logActivity(FAILED_QUEUEING);
                insert(cache, rec26);
            }
        }
    }

    /**
     * Called when a student has completed a precalculus tutorial and earned placement out of a single course. This
     * method attempts to query the BANNER table for an existing score for the student, and then inserts a record only
     * when the score has improved (or when there was no existing score).
     *
     * <p>
     * If BANNER could not be queried or a score cannot be inserted in BANNER, a record is inserted into the local score
     * queue table for later processing.
     *
     * <p>
     * This call commits the insert, whether it is done in the live SORTEST table or the local queue table.
     *
     * @param cache          the data cache
     * @param liveConn       the LIVE database connection, checked out to this thread
     * @param pidm           the student's PIDM
     * @param courseId       the course ID in which credit was earned
     * @param examFinishTime the exam finish time
     * @throws SQLException if there is an error accessing the database
     */
    public static void postPrecalcTutorialResult(final Cache cache, final DbConnection liveConn, final Integer pidm,
                                                 final String courseId, final LocalDateTime examFinishTime)
            throws SQLException {

        String testCode = null;
        if (RawRecordConstants.M117.equals(courseId)) {
            testCode = MC17;
        } else if (RawRecordConstants.M118.equals(courseId)) {
            testCode = MC18;
        } else if (RawRecordConstants.M124.equals(courseId)) {
            testCode = MC24;
        } else if (RawRecordConstants.M125.equals(courseId)) {
            testCode = MC25;
        } else if (RawRecordConstants.M126.equals(courseId)) {
            testCode = MC26;
        }

        if (testCode == null) {
            Log.warning("Unrecognized placement course: ", courseId);
        } else {
            List<RawMpscorequeue> existing = null;
            if (!LogicUtils.isBannerDown()) {
                existing = querySORTESTByStudent(liveConn, pidm);
            }

            if (existing == null) {
                logActivity("BANNER down - queueing placement scores for PIDM " + pidm);

                final RawMpscorequeue rec = new RawMpscorequeue(pidm, testCode, examFinishTime, "1");
                insert(cache, rec);
            } else {
                RawMpscorequeue bestExisting = null;

                for (final RawMpscorequeue test : existing) {
                    if (test.testCode.equals(testCode)) {
                        if (bestExisting == null) {
                            bestExisting = test;
                        } else if ("0".equals(bestExisting.testScore)) {
                            if ("1".equals(test.testScore) || "2".equals(test.testScore)) {
                                bestExisting = test;
                            }
                        } else if ("1".equals(bestExisting.testScore) && "2".equals(test.testScore)) {
                            bestExisting = test;
                        }
                    }
                }

                if (bestExisting == null || "0".equals(bestExisting.testScore)) {
                    // Score does not exist in Banner, or score improved, so insert

                    final RawMpscorequeue rec = new RawMpscorequeue(pidm, testCode, examFinishTime, "1");

                    Log.warning(INSERTING_OPEN, rec, CLOSE);
                    if (!insertSORTEST(liveConn, rec)) {
                        logActivity(FAILED_QUEUEING);
                        insert(cache, rec);
                    }
                }
            }
        }
    }

    /**
     * Called when a student has completed the ELM tutorial and earned placement into MATH 117. This method attempts to
     * query the BANNER table for existing MC00 score for the student, and then inserts a record only when the score has
     * improved (or when there was no existing score).
     *
     * <p>
     * If BANNER could not be queried or a score cannot be inserted in BANNER, a record is inserted into the local score
     * queue table for later processing.
     *
     * <p>
     * This call commits the insert, whether it is done in the live SORTEST table or the local queue table.
     *
     * @param cache          the data cache
     * @param liveConn       the LIVE database connection, checked out to this thread
     * @param pidm           the student's PIDM
     * @param examFinishTime the exam finish time
     * @throws SQLException if there is an error accessing the database
     */
    public static void postELMTutorialResult(final Cache cache, final DbConnection liveConn, final Integer pidm,
                                             final LocalDateTime examFinishTime) throws SQLException {

        List<RawMpscorequeue> existing = null;
        if (!LogicUtils.isBannerDown()) {
            existing = querySORTESTByStudent(liveConn, pidm);
        }

        final RawMpscorequeue rec = new RawMpscorequeue(pidm, MC00, examFinishTime, "2");

        if (existing == null) {
            logActivity("BANNER down - queueing placement scores for PIDM " + pidm);

            insert(cache, rec);
        } else {
            boolean needs2 = true;
            for (final RawMpscorequeue test : existing) {
                if (MC00.equals(test.testCode) && "2".equals(test.testScore)) {
                    needs2 = false;
                    break;
                }
            }

            if (needs2) {
                // Score does not exist in Banner, or score improved, so insert
                Log.warning(INSERTING_OPEN, rec, CLOSE);
                if (!insertSORTEST(liveConn, rec)) {
                    logActivity(FAILED_QUEUEING);
                    insert(cache, rec);
                }
            }
        }
    }

    /**
     * Called when a student has passed the ELM Unit 3 Review. This method attempts to insert a score "4" for the MC00
     * test score, assuming no other record exists with a nonzero score.
     *
     * <p>
     * If the score cannot be inserted in BANNER, a record is inserted into the local score queue table for later
     * processing.
     *
     * <p>
     * This call commits the insert, whether it is done in the live SORTEST table or the local queue table.
     *
     * @param cache          the data cache
     * @param liveConn       the LIVE database connection, checked out to this thread
     * @param pidm           the student's PIDM
     * @param examFinishTime the exam finish time
     * @throws SQLException if there is an error accessing the database
     */
    public static void postELMUnit3ReviewPassed(final Cache cache, final DbConnection liveConn, final Integer pidm,
                                                final LocalDateTime examFinishTime) throws SQLException {

        List<RawMpscorequeue> existing = null;
        if (!LogicUtils.isBannerDown()) {
            existing = querySORTESTByStudent(liveConn, pidm);
        }

        final RawMpscorequeue rec = new RawMpscorequeue(pidm, MC00, examFinishTime, "4");

        if (existing == null) {
            logActivity("BANNER down - queueing placement scores for PIDM " + pidm);
            insert(cache, rec);
        } else {
            boolean allZero = true;
            for (final RawMpscorequeue test : existing) {
                if (MC00.equals(test.testCode) && !"0".equals(test.testScore)) {
                    allZero = false;
                    break;
                }
            }

            if (allZero) {
                // Score does not exist in Banner, or score improved, so insert
                Log.warning(INSERTING_OPEN, rec, CLOSE);
                if (!insertSORTEST(liveConn, rec)) {
                    logActivity(FAILED_QUEUEING);
                    insert(cache, rec);
                }
            }
        }
    }

    /**
     * Queries for all SORTEST records for a single student.
     *
     * @param liveConn the LIVE database connection, checked out to this thread
     * @param pidm     the student's PIDM
     * @return the list of all records; empty if the query failed
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawMpscorequeue> querySORTESTByStudent(final DbConnection liveConn,
                                                              final Integer pidm) throws SQLException {

        final List<RawMpscorequeue> result = new ArrayList<>(10);

        if (!LogicUtils.isBannerDown()) {
            final String sql = SimpleBuilder.concat("SELECT * FROM SORTEST",
                    " WHERE SORTEST_PIDM=", pidm,
                    " AND SORTEST_TESC_CODE IN ('MPL','MC00','MC17','MC18','MC24','MC25','MC26')");

            try (final Statement stmt = liveConn.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    result.add(constructFromSORTESTResultSet(rs));
                }
            }
        }

        return result;
    }

    /**
     * Inserts a new SORTEST record. This call commits the insert.
     *
     * @param liveConn the LIVE database connection, checked out to this thread
     * @param record   the record to insert
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insertSORTEST(final DbConnection liveConn, final RawMpscorequeue record) throws SQLException {

        boolean result = false;

        if (record.pidm.intValue() == 10121250) {
            logActivity("Skipping SORTEST insert for Anita Pattison: " + record);
            result = true;
        } else if (record.pidm.intValue() == 10567708) {
            logActivity("Skipping SORTEST insert for Steve Benoit: " + record);
            result = true;
        } else if (LogicUtils.isBannerDown()) {
            logActivity("Skipping SORTEST insert because BANNER is down");
        } else {
            Log.info("Inserting '", record.testCode, "' SORTEST record for PIDM ", record.pidm);

            final Connection conn = liveConn.getConnection();

            try (final CallableStatement cs = conn.prepareCall("{ call csus_api_sortest.p_create"
                                                               + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) }")) {

                cs.setInt(1, record.pidm.intValue());
                cs.setString(2, record.testCode);
                cs.setTimestamp(3, Timestamp.valueOf(record.testDate));
                cs.setString(4, record.testScore);
                cs.setString(5, "INST");
                cs.setString(6, null);
                cs.setString(7, null);
                cs.setString(8, null);
                cs.setString(9, null);
                cs.setString(10, null);
                cs.setString(11, null);
                cs.setString(12, null);
                cs.setString(13, null);
                cs.setString(14, null);
                cs.setObject(15, null);
                cs.setString(16, null);
                cs.setString(17, null);
                cs.setString(18, "N");
                cs.setString(19, null);
                cs.setString(20, null);
                cs.setString(21, null);

                cs.registerOutParameter(22, Types.VARCHAR);

                boolean hadResults = cs.execute();

                while (hadResults) {
                    try (final ResultSet rs = cs.getResultSet()) {

                        final String outputValue = rs.getString(22); // index-based
                        Log.info("Inserted record has ID: ", outputValue);

                        hadResults = cs.getMoreResults();
                    }
                }

                // This is critical - evidently the "auto-commit" setting does not apply to calls
                conn.commit();
                result = true;
            }
        }

        return result;
    }

    /**
     * Writes a log of test scores being posted to BANNER or written to the queue, so we can audit scores for testing.
     *
     * @param message the message to log
     */
    public static void logActivity(final String message) {

        final File logPath = PathList.getInstance().get(EPath.LOG_PATH);
        final File bannerLog = new File(logPath, "BANNER.log");

        final HtmlBuilder htm = new HtmlBuilder(30 + message.length());
        htm.add(TemporalUtils.FMT_MDY_AT_HMS_A.format(LocalDateTime.now()));
        htm.add(": ");
        htm.addln(message);

        try (final FileWriter fw = new FileWriter(bannerLog, true)) {
            fw.write(htm.toString());
        } catch (final IOException ex) {
            Log.warning("Failed to write to BANNER.log", ex);
        }
    }

    /**
     * Builds a {@code RawMpscorequeue} from a {@code ResultSet} that comes from a query of the SORTEST table in the
     * Live database schema.
     *
     * @param rset the result set from which to construct the object
     * @return the {@code RawMpscorequeue} object
     * @throws SQLException if there is an error accessing the database
     */
    private static RawMpscorequeue constructFromSORTESTResultSet(final ResultSet rset) throws SQLException {

        // Want: test codes 'MPL', 'MC00', 'MC17', 'MC18', 'MC24', 'MC25', 'MC26'

        // Log.info("SORTEST Record:");
        // Log.fine(" SORTEST_PIDM = " + getInteger(rset, "SORTEST_PIDM"));
        // Log.fine(" SORTEST_TESC_CODE = " + getString(rset, "SORTEST_TESC_CODE"));
        // Log.fine(" SORTEST_TEST_DATE = " + getDateTime(rset, "SORTEST_TEST_DATE"));
        // Log.fine(" SORTEST_TEST_SCORE = " + getString(rset, "SORTEST_TEST_SCORE"));
        // Log.fine(" SORTEST_TSRC_CODE = " + getString(rset, "SORTEST_TSRC_CODE"));
        // Log.fine(" SORTEST_TADM_CODE = " + getString(rset, "SORTEST_TADM_CODE"));
        // Log.fine(" SORTEST_ACTIVITY_DATE = " + getDateTime(rset, "SORTEST_ACTIVITY_DATE"));
        // Log.fine(" SORTEST_ADMR_CODE = " + getString(rset, "SORTEST_ADMR_CODE"));
        // Log.fine(" SORTEST_TEIN_CODE = " + getString(rset, "SORTEST_TEIN_CODE"));
        // Log.fine(" SORTEST_TEFR_CODE = " + getString(rset, "SORTEST_TEFR_CODE"));
        // Log.fine(" SORTEST_TEAC_CODE = " + getString(rset, "SORTEST_TEAC_CODE"));
        // Log.fine(" SORTEST_TEPR_CODE = " + getString(rset, "SORTEST_TEPR_CODE"));
        // Log.fine(" SORTEST_RCRV_IND = " + getString(rset, "SORTEST_RCRV_IND"));
        // Log.fine(" SORTEST_SAT_ORIG_SCORE = " + getString(rset, "SORTEST_SAT_ORIG_SCORE"));
        // Log.fine(" SORTEST_TERM_CODE_ENTRY = " + getString(rset, "SORTEST_TERM_CODE_ENTRY"));
        // Log.fine(" SORTEST_APPL_NO = " + getDouble(rset, "SORTEST_APPL_NO"));
        // Log.fine(" SORTEST_INSTR_ID = " + getString(rset, "SORTEST_INSTR_ID"));
        // Log.fine(" SORTEST_RELEASE_IND = " + getString(rset, "SORTEST_RELEASE_IND"));
        // Log.fine(" SORTEST_EQUIV_IND = " + getString(rset, "SORTEST_EQUIV_IND"));
        // Log.fine(" SORTEST_SAT_ESSAY_ID = " + getString(rset, "SORTEST_SAT_ESSAY_ID"));
        // Log.fine(" SORTEST_USER_ID = " + getString(rset, "SORTEST_USER_ID"));
        // Log.fine(" SORTEST_DATA_ORIGIN = " + getString(rset, "SORTEST_DATA_ORIGIN"));
        // Log.fine(" SORTEST_SURROGATE_ID = " + getDouble(rset, "SORTEST_SURROGATE_ID"));
        // Log.fine(" SORTEST_VERSION = " + getDouble(rset, "SORTEST_VERSION"));
        // Log.fine(" SORTEST_VPDI_CODE = " + getString(rset, "SORTEST_VPDI_CODE"));
        // Log.fine(" SORTEST_GUID = " + getString(rset, "SORTEST_GUID"));

        final Integer pidm = DbUtils.getInteger(rset, "SORTEST_PIDM");
        final String code = DbUtils.getString(rset, "SORTEST_TESC_CODE");
        final LocalDateTime date = DbUtils.getDateTime(rset, "SORTEST_TEST_DATE");
        final String score = DbUtils.getString(rset, "SORTEST_TEST_SCORE");

        try {
            return new RawMpscorequeue(pidm, code, date, score);
        } catch (final IllegalArgumentException ex) {
            throw new SQLException(ex);
        }
    }
}
