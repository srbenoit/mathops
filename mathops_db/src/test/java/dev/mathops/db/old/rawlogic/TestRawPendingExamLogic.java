package dev.mathops.db.old.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawPendingExam;
import dev.mathops.db.old.rawrecord.RawRecordConstants;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

/**
 * Tests for the {@code RawPendingExamLogic} class.
 */
final class TestRawPendingExamLogic {

    /** A float used in test records. */
    private static final Float ONE = Float.valueOf(1.0f);

    /** A float used in test records. */
    private static final Float THREE_HALVES = Float.valueOf(1.5f);

    /** A float used in test records. */
    private static final Float TWO = Float.valueOf(2.0f);

    /** A float used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 1);

    /** A float used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 2, 2);

    /** A float used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 3, 3);

    /** The database profile. */
    private static DbProfile dbProfile = null;

    /** The database context. */
    private static DbContext ctx = null;

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        dbProfile = ContextMap.getDefaultInstance().getCodeProfile(Contexts.INFORMIX_TEST_PATH);
        if (dbProfile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_TEST_PROFILE));
        }

        ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);
        if (ctx == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PRIMARY_CONTEXT));
        }

        // Make sure we're in the TEST database
        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery("SELECT descr FROM which_db")) {

                    if (rs.next()) {
                        final String which = rs.getString(1);
                        if (which != null && !"TEST".equals(which.trim())) {
                            throw new IllegalArgumentException(
                                    TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                        }
                    } else {
                        throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                    }
                }
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            throw new IllegalArgumentException(ex);
        }

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM pending_exam");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawPendingExam raw1 = new RawPendingExam(Long.valueOf(12345L), "171UE", "111111111", date1,
                        Integer.valueOf(2), Integer.valueOf(300), Integer.valueOf(400), "Y", "N", Integer.valueOf(3),
                        RawRecordConstants.M117, Integer.valueOf(1), "UE", THREE_HALVES, "STU");

                final RawPendingExam raw2 = new RawPendingExam(Long.valueOf(12346L), "18FIN", "111111111", date2,
                        Integer.valueOf(3), Integer.valueOf(500), Integer.valueOf(600), "A", "B", Integer.valueOf(4),
                        RawRecordConstants.M118, Integer.valueOf(5), "FE", TWO, "STU");

                final RawPendingExam raw3 = new RawPendingExam(Long.valueOf(12347L), "PPPPP", "222222222", date3,
                        Integer.valueOf(5), Integer.valueOf(700), Integer.valueOf(800), "C", "D", Integer.valueOf(5),
                        RawRecordConstants.M100P, Integer.valueOf(6), "P", ONE, "ADM");

                assertTrue(RawPendingExamLogic.INSTANCE.insert(cache, raw1), "Failed to insert pending_exam 1");
                assertTrue(RawPendingExamLogic.INSTANCE.insert(cache, raw2), "Failed to insert pending_exam 2");
                assertTrue(RawPendingExamLogic.INSTANCE.insert(cache, raw3), "Failed to insert pending_exam 3");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing tables: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0003() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawPendingExam> all = RawPendingExamLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawPendingExam test : all) {
                    if (Long.valueOf(12345L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(2).equals(test.examScore)
                            && Integer.valueOf(300).equals(test.startTime)
                            && Integer.valueOf(400).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(3).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "UE".equals(test.examType)
                            && THREE_HALVES.equals(test.timelimitFactor)
                            && "STU".equals(test.stuType)) {

                        found1 = true;
                    } else if (Long.valueOf(12346L).equals(test.serialNbr)
                            && "18FIN".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(3).equals(test.examScore)
                            && Integer.valueOf(500).equals(test.startTime)
                            && Integer.valueOf(600).equals(test.finishTime)
                            && "A".equals(test.timeOk)
                            && "B".equals(test.passed)
                            && Integer.valueOf(4).equals(test.seqNbr)
                            && RawRecordConstants.M118.equals(test.course)
                            && Integer.valueOf(5).equals(test.unit)
                            && "FE".equals(test.examType)
                            && TWO.equals(test.timelimitFactor)
                            && "STU".equals(test.stuType)) {

                        found2 = true;
                    } else if (Long.valueOf(12347L).equals(test.serialNbr)
                            && "PPPPP".equals(test.version)
                            && "222222222".equals(test.stuId)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(5).equals(test.examScore)
                            && Integer.valueOf(700).equals(test.startTime)
                            && Integer.valueOf(800).equals(test.finishTime)
                            && "C".equals(test.timeOk)
                            && "D".equals(test.passed)
                            && Integer.valueOf(5).equals(test.seqNbr)
                            && RawRecordConstants.M100P.equals(test.course)
                            && Integer.valueOf(6).equals(test.unit)
                            && "P".equals(test.examType)
                            && ONE.equals(test.timelimitFactor)
                            && "ADM".equals(test.stuType)) {

                        found3 = true;
                    }
                }

                assertTrue(found1, "pending_exam 1 not found");
                assertTrue(found2, "pending_exam 2 not found");
                assertTrue(found3, "pending_exam 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all pending_exam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawPendingExam> all = RawPendingExamLogic.queryByStudent(cache, "111111111");

                assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawPendingExam test : all) {
                    if (Long.valueOf(12345L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(2).equals(test.examScore)
                            && Integer.valueOf(300).equals(test.startTime)
                            && Integer.valueOf(400).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(3).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "UE".equals(test.examType)
                            && THREE_HALVES.equals(test.timelimitFactor)
                            && "STU".equals(test.stuType)) {

                        found1 = true;
                    } else if (Long.valueOf(12346L).equals(test.serialNbr)
                            && "18FIN".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(3).equals(test.examScore)
                            && Integer.valueOf(500).equals(test.startTime)
                            && Integer.valueOf(600).equals(test.finishTime)
                            && "A".equals(test.timeOk)
                            && "B".equals(test.passed)
                            && Integer.valueOf(4).equals(test.seqNbr)
                            && RawRecordConstants.M118.equals(test.course)
                            && Integer.valueOf(5).equals(test.unit)
                            && "FE".equals(test.examType)
                            && TWO.equals(test.timelimitFactor)
                            && "STU".equals(test.stuType)) {

                        found2 = true;
                    }
                }

                assertTrue(found1, "pending_exam 1 not found");
                assertTrue(found2, "pending_exam 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying pending_exam rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll after delete")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawPendingExamLogic.delete(cache, Long.valueOf(12347L), "222222222"), "Delete failed");

                final List<RawPendingExam> all = RawPendingExamLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawPendingExam test : all) {
                    if (Long.valueOf(12345L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(2).equals(test.examScore)
                            && Integer.valueOf(300).equals(test.startTime)
                            && Integer.valueOf(400).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(3).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "UE".equals(test.examType)
                            && THREE_HALVES.equals(test.timelimitFactor)
                            && "STU".equals(test.stuType)) {

                        found1 = true;
                    } else if (Long.valueOf(12346L).equals(test.serialNbr)
                            && "18FIN".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(3).equals(test.examScore)
                            && Integer.valueOf(500).equals(test.startTime)
                            && Integer.valueOf(600).equals(test.finishTime)
                            && "A".equals(test.timeOk)
                            && "B".equals(test.passed)
                            && Integer.valueOf(4).equals(test.seqNbr)
                            && RawRecordConstants.M118.equals(test.course)
                            && Integer.valueOf(5).equals(test.unit)
                            && "FE".equals(test.examType)
                            && TWO.equals(test.timelimitFactor)
                            && "STU".equals(test.stuType)) {

                        found2 = true;
                    }
                }

                assertTrue(found1, "pending_exam 1 not found");
                assertTrue(found2, "pending_exam 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after delete: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll after delete")
    void test0006() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawPendingExam raw2 = new RawPendingExam(Long.valueOf(12346L), "18FIN", "111111111", date2,
                        Integer.valueOf(3), Integer.valueOf(500), Integer.valueOf(600), "A", "B", Integer.valueOf(4),
                        RawRecordConstants.M118, Integer.valueOf(5), "FE", TWO, "STU");

                final boolean result = RawPendingExamLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "Delete failed");

                final List<RawPendingExam> all = RawPendingExamLogic.INSTANCE.queryAll(cache);

                assertEquals(1, all.size(), //
                        "Incorrect record count from queryAll");

                boolean found1 = false;

                for (final RawPendingExam test : all) {
                    if (Long.valueOf(12345L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(2).equals(test.examScore)
                            && Integer.valueOf(300).equals(test.startTime)
                            && Integer.valueOf(400).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(3).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "UE".equals(test.examType)
                            && THREE_HALVES.equals(test.timelimitFactor)
                            && "STU".equals(test.stuType)) {

                        found1 = true;
                        break;
                    }
                }

                assertTrue(found1, "pending_exam 1 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after delete: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM pending_exam");
                }

                conn.commit();

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        }
    }
}
