package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStexam;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@code RawStexamLogic} class.
 */
final class TestRawStexamLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 1);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 1, 3);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2022, 1, 4);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2022, 1, 5);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2022, 1, 6);

    /** A date used in test records. */
    private static final LocalDate date7 = LocalDate.of(2022, 1, 7);

    /** A date used in test records. */
    private static final LocalDate date8 = LocalDate.of(2021, 1, 8);

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
                    stmt.executeUpdate("DELETE FROM stexam");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawStexam raw1 = new RawStexam(Long.valueOf(1000L), "UUUUU", "111111111",
                        date1, Integer.valueOf(20), Integer.valueOf(14), Integer.valueOf(59),
                        Integer.valueOf(89), "Y", "Y", Integer.valueOf(0), RawRecordConstants.M100U,
                        Integer.valueOf(0), "Q", "Y", null, null);

                final RawStexam raw2 = new RawStexam(Long.valueOf(1001L), "171RE", "111111111",
                        date2, Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(60),
                        Integer.valueOf(90), "Y", "Y", Integer.valueOf(1), RawRecordConstants.M117,
                        Integer.valueOf(1), "R", "Y", null, null);

                final RawStexam raw3 = new RawStexam(Long.valueOf(1002L), "171UE", "111111111",
                        date3, Integer.valueOf(6), Integer.valueOf(8), Integer.valueOf(61),
                        Integer.valueOf(91), "Y", "N", Integer.valueOf(2), RawRecordConstants.M117,
                        Integer.valueOf(1), "U", "N", "TC", "123");

                final RawStexam raw4 = new RawStexam(Long.valueOf(1003L), "174RE", "222222222",
                        date4, Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(62),
                        Integer.valueOf(92), "Y", "Y", Integer.valueOf(3), RawRecordConstants.M117,
                        Integer.valueOf(4), "R", "Y", null, null);

                final RawStexam raw5 = new RawStexam(Long.valueOf(1004L), "18FIN", "333333333",
                        date6, Integer.valueOf(13), Integer.valueOf(16), Integer.valueOf(1430),
                        Integer.valueOf(9), "Y", "N", Integer.valueOf(4), RawRecordConstants.M118, Integer.valueOf(5),
                        "F", "N", "RE", "987");

                final RawStexam raw6 = new RawStexam(Long.valueOf(1005L), "18FIN", "333333333",
                        date7, Integer.valueOf(19), Integer.valueOf(16), Integer.valueOf(1),
                        Integer.valueOf(10), "Y", "Y", Integer.valueOf(5), RawRecordConstants.M118,
                        Integer.valueOf(5), "F", "Y", "PU", null);

                final RawStexam raw7 = new RawStexam(Long.valueOf(1006L), "172RE", "111111111",
                        date8, Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(63),
                        Integer.valueOf(83), "Y", "G", Integer.valueOf(6), RawRecordConstants.M117,
                        Integer.valueOf(2), "R", "N", null, null);

                assertTrue(RawStexamLogic.INSTANCE.insert(cache, raw1), "Failed to insert stexam 1");
                assertTrue(RawStexamLogic.INSTANCE.insert(cache, raw2), "Failed to insert stexam 2");
                assertTrue(RawStexamLogic.INSTANCE.insert(cache, raw3), "Failed to insert stexam 3");
                assertTrue(RawStexamLogic.INSTANCE.insert(cache, raw4), "Failed to insert stexam 4");
                assertTrue(RawStexamLogic.INSTANCE.insert(cache, raw5), "Failed to insert stexam 5");
                assertTrue(RawStexamLogic.INSTANCE.insert(cache, raw6), "Failed to insert stexam 6");
                assertTrue(RawStexamLogic.INSTANCE.insert(cache, raw7), "Failed to insert stexam 7");
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
                final List<RawStexam> all = RawStexamLogic.INSTANCE.queryAll(cache);

                assertEquals(7, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;
                boolean found7 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1000L).equals(test.serialNbr)
                            && "UUUUU".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(20).equals(test.examScore)
                            && Integer.valueOf(14).equals(test.masteryScore)
                            && Integer.valueOf(59).equals(test.startTime)
                            && Integer.valueOf(89).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(0).equals(test.seqNbr)
                            && RawRecordConstants.M100U.equals(test.course)
                            && Integer.valueOf(0).equals(test.unit)
                            && "Q".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found1 = true;

                    } else if (Long.valueOf(1001L).equals(test.serialNbr)
                            && "171RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(10).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(60).equals(test.startTime)
                            && Integer.valueOf(90).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(1).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found2 = true;

                    } else if (Long.valueOf(1002L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(6).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(61).equals(test.startTime)
                            && Integer.valueOf(91).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(2).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "U".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && "TC".equals(test.examSource)
                            && "123".equals(test.calcNbr)) {

                        found3 = true;

                    } else if (Long.valueOf(1003L).equals(test.serialNbr)
                            && "174RE".equals(test.version)
                            && "222222222".equals(test.stuId)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(9).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(62).equals(test.startTime)
                            && Integer.valueOf(92).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(3).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(4).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found4 = true;

                    } else if (Long.valueOf(1004L).equals(test.serialNbr)
                            && "18FIN".equals(test.version)
                            && "333333333".equals(test.stuId)
                            && date5.equals(test.examDt)
                            && Integer.valueOf(13).equals(test.examScore)
                            && Integer.valueOf(16).equals(test.masteryScore)
                            && Integer.valueOf(1430).equals(test.startTime)
                            && Integer.valueOf(1449).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(4).equals(test.seqNbr)
                            && RawRecordConstants.M118.equals(test.course)
                            && Integer.valueOf(5).equals(test.unit)
                            && "F".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && "RE".equals(test.examSource)
                            && "987".equals(test.calcNbr)) {

                        found5 = true;

                    } else if (Long.valueOf(1005L).equals(test.serialNbr)
                            && "18FIN".equals(test.version)
                            && "333333333".equals(test.stuId)
                            && date6.equals(test.examDt)
                            && Integer.valueOf(19).equals(test.examScore)
                            && Integer.valueOf(16).equals(test.masteryScore)
                            && Integer.valueOf(1441).equals(test.startTime)
                            && Integer.valueOf(1450).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(5).equals(test.seqNbr)
                            && RawRecordConstants.M118.equals(test.course)
                            && Integer.valueOf(5).equals(test.unit)
                            && "F".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && "PU".equals(test.examSource)
                            && test.calcNbr == null) {

                        found6 = true;
                    } else if (Long.valueOf(1006L).equals(test.serialNbr)
                            && "172RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date8.equals(test.examDt)
                            && Integer.valueOf(8).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(63).equals(test.startTime)
                            && Integer.valueOf(83).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "G".equals(test.passed)
                            && Integer.valueOf(6).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(2).equals(test.unit)
                            && "R".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found7 = true;
                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found1, "Stexam 1 not found");
                assertTrue(found2, "Stexam 2 not found");
                assertTrue(found3, "Stexam 3 not found");
                assertTrue(found4, "Stexam 4 not found");
                assertTrue(found5, "Stexam 5 not found");
                assertTrue(found6, "Stexam 6 not found");
                assertTrue(found7, "Stexam 7 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam rows: " + ex.getMessage());
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
                final List<RawStexam> all = RawStexamLogic.queryByStudent(cache, "111111111", true);

                assertEquals(4, all.size(), "Incorrect record count from queryByStudent");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found7 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1000L).equals(test.serialNbr)
                            && "UUUUU".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(20).equals(test.examScore)
                            && Integer.valueOf(14).equals(test.masteryScore)
                            && Integer.valueOf(59).equals(test.startTime)
                            && Integer.valueOf(89).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(0).equals(test.seqNbr)
                            && RawRecordConstants.M100U.equals(test.course)
                            && Integer.valueOf(0).equals(test.unit)
                            && "Q".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found1 = true;

                    } else if (Long.valueOf(1001L).equals(test.serialNbr)
                            && "171RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(10).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(60).equals(test.startTime)
                            && Integer.valueOf(90).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(1).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found2 = true;

                    } else if (Long.valueOf(1002L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(6).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(61).equals(test.startTime)
                            && Integer.valueOf(91).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(2).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "U".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && "TC".equals(test.examSource)
                            && "123".equals(test.calcNbr)) {

                        found3 = true;
                    } else if (Long.valueOf(1006L).equals(test.serialNbr)
                            && "172RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date8.equals(test.examDt)
                            && Integer.valueOf(8).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(63).equals(test.startTime)
                            && Integer.valueOf(83).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "G".equals(test.passed)
                            && Integer.valueOf(6).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(2).equals(test.unit)
                            && "R".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found7 = true;
                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found1, "Stexam 1 not found");
                assertTrue(found2, "Stexam 2 not found");
                assertTrue(found3, "Stexam 3 not found");
                assertTrue(found7, "Stexam 7 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.queryByStudent(cache, "111111111", false);

                assertEquals(3, all.size(), "Incorrect record count from queryByStudent");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1000L).equals(test.serialNbr)
                            && "UUUUU".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(20).equals(test.examScore)
                            && Integer.valueOf(14).equals(test.masteryScore)
                            && Integer.valueOf(59).equals(test.startTime)
                            && Integer.valueOf(89).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(0).equals(test.seqNbr)
                            && RawRecordConstants.M100U.equals(test.course)
                            && Integer.valueOf(0).equals(test.unit)
                            && "Q".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found1 = true;

                    } else if (Long.valueOf(1001L).equals(test.serialNbr)
                            && "171RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(10).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(60).equals(test.startTime)
                            && Integer.valueOf(90).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(1).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found2 = true;

                    } else if (Long.valueOf(1002L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(6).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(61).equals(test.startTime)
                            && Integer.valueOf(91).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(2).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "U".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && "TC".equals(test.examSource)
                            && "123".equals(test.calcNbr)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found1, "Stexam 1 not found");
                assertTrue(found2, "Stexam 2 not found");
                assertTrue(found3, "Stexam 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryOutstanding results")
    void test0006() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.queryByStudentCourse(cache, "111111111",
                        RawRecordConstants.M117, true);

                assertEquals(3, all.size(), "Incorrect record count from queryByStudent");

                boolean found2 = false;
                boolean found3 = false;
                boolean found7 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1001L).equals(test.serialNbr)
                            && "171RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(10).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(60).equals(test.startTime)
                            && Integer.valueOf(90).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(1).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found2 = true;

                    } else if (Long.valueOf(1002L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(6).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(61).equals(test.startTime)
                            && Integer.valueOf(91).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(2).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "U".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && "TC".equals(test.examSource)
                            && "123".equals(test.calcNbr)) {

                        found3 = true;
                    } else if (Long.valueOf(1006L).equals(test.serialNbr)
                            && "172RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date8.equals(test.examDt)
                            && Integer.valueOf(8).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(63).equals(test.startTime)
                            && Integer.valueOf(83).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "G".equals(test.passed)
                            && Integer.valueOf(6).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(2).equals(test.unit)
                            && "R".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found7 = true;
                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found2, "Stexam 2 not found");
                assertTrue(found3, "Stexam 3 not found");
                assertTrue(found7, "Stexam 7 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying outstanding row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryOutstanding results")
    void test0007() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.queryByStudentCourse(cache, "111111111",
                        RawRecordConstants.M117, false);

                assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

                boolean found2 = false;
                boolean found3 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1001L).equals(test.serialNbr)
                            && "171RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(10).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(60).equals(test.startTime)
                            && Integer.valueOf(90).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(1).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found2 = true;

                    } else if (Long.valueOf(1002L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(6).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(61).equals(test.startTime)
                            && Integer.valueOf(91).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(2).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "U".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && "TC".equals(test.examSource)
                            && "123".equals(test.calcNbr)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found2, "Stexam 2 not found");
                assertTrue(found3, "Stexam 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying outstanding row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0008() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.queryByCourse(cache, RawRecordConstants.M117, true);

                assertEquals(4, all.size(), "Incorrect record count from queryAll");

                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found7 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1001L).equals(test.serialNbr)
                            && "171RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(10).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(60).equals(test.startTime)
                            && Integer.valueOf(90).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(1).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found2 = true;

                    } else if (Long.valueOf(1002L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(6).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(61).equals(test.startTime)
                            && Integer.valueOf(91).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(2).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "U".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && "TC".equals(test.examSource)
                            && "123".equals(test.calcNbr)) {

                        found3 = true;

                    } else if (Long.valueOf(1003L).equals(test.serialNbr)
                            && "174RE".equals(test.version)
                            && "222222222".equals(test.stuId)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(9).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(62).equals(test.startTime)
                            && Integer.valueOf(92).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(3).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(4).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found4 = true;
                    } else if (Long.valueOf(1006L).equals(test.serialNbr)
                            && "172RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date8.equals(test.examDt)
                            && Integer.valueOf(8).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(63).equals(test.startTime)
                            && Integer.valueOf(83).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "G".equals(test.passed)
                            && Integer.valueOf(6).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(2).equals(test.unit)
                            && "R".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found7 = true;
                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found2, "Stexam 2 not found");
                assertTrue(found3, "Stexam 3 not found");
                assertTrue(found4, "Stexam 4 not found");
                assertTrue(found7, "Stexam 7 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0009() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.queryByCourse(cache, RawRecordConstants.M117, false);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1001L).equals(test.serialNbr)
                            && "171RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(10).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(60).equals(test.startTime)
                            && Integer.valueOf(90).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(1).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found2 = true;

                    } else if (Long.valueOf(1002L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(6).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(61).equals(test.startTime)
                            && Integer.valueOf(91).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(2).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "U".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && "TC".equals(test.examSource)
                            && "123".equals(test.calcNbr)) {

                        found3 = true;

                    } else if (Long.valueOf(1003L).equals(test.serialNbr)
                            && "174RE".equals(test.version)
                            && "222222222".equals(test.stuId)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(9).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(62).equals(test.startTime)
                            && Integer.valueOf(92).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(3).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(4).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found4 = true;

                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found2, "Stexam 2 not found");
                assertTrue(found3, "Stexam 3 not found");
                assertTrue(found4, "Stexam 4 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0010() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<List<RawStexam>> history = new ArrayList<>(10);
                final LocalDate today = LocalDate.of(2021, 1, 10);

                RawStexamLogic.getHistory(cache, history, 10, today, RawRecordConstants.M117);

                assertEquals(10, history.size(), "Incorrect result list count from getHistory");

                final List<RawStexam> jan1 = history.get(0);
                final List<RawStexam> jan2 = history.get(1);
                final List<RawStexam> jan3 = history.get(2);
                final List<RawStexam> jan4 = history.get(3);
                final List<RawStexam> jan5 = history.get(4);
                final List<RawStexam> jan6 = history.get(5);
                final List<RawStexam> jan7 = history.get(6);
                final List<RawStexam> jan8 = history.get(7);
                final List<RawStexam> jan9 = history.get(8);
                final List<RawStexam> jan10 = history.get(9);

                assertEquals(0, jan1.size(), "Incorrect count for Jan 1 from getHistory");
                assertEquals(1, jan2.size(), "Incorrect count for Jan 2 from getHistory");
                assertEquals(1, jan3.size(), "Incorrect count for Jan 3 from getHistory");
                assertEquals(0, jan4.size(), "Incorrect count for Jan 4 from getHistory");
                assertEquals(0, jan5.size(), "Incorrect count for Jan 5 from getHistory");
                assertEquals(0, jan6.size(), "Incorrect count for Jan 6 from getHistory");
                assertEquals(0, jan7.size(), "Incorrect count for Jan 7 from getHistory");
                assertEquals(1, jan8.size(), "Incorrect count for Jan 8 from getHistory");
                assertEquals(0, jan9.size(), "Incorrect count for Jan 9 from getHistory");
                assertEquals(0, jan10.size(), "Incorrect count for Jan 10 from getHistory");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getExams results")
    void test0011() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.getExams(cache, RawRecordConstants.M117, false, "R", "U");

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1001L).equals(test.serialNbr)
                            && "171RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(10).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(60).equals(test.startTime)
                            && Integer.valueOf(90).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(1).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null //
                            && test.calcNbr == null) {

                        found2 = true;

                    } else if (Long.valueOf(1002L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(6).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(61).equals(test.startTime)
                            && Integer.valueOf(91).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(2).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "U".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && "TC".equals(test.examSource)
                            && "123".equals(test.calcNbr)) {

                        found3 = true;

                    } else if (Long.valueOf(1003L).equals(test.serialNbr)
                            && "174RE".equals(test.version)
                            && "222222222".equals(test.stuId)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(9).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(62).equals(test.startTime)
                            && Integer.valueOf(92).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(3).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(4).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found4 = true;
                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found2, "Stexam 2 not found");
                assertTrue(found3, "Stexam 3 not found");
                assertTrue(found4, "Stexam 4 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getExams results")
    void test0012() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.getExams(cache, RawRecordConstants.M117, true, "R", "U");

                assertEquals(2, all.size(), "Incorrect record count from queryAll");

                boolean found2 = false;
                boolean found4 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1001L).equals(test.serialNbr)
                            && "171RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(10).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(60).equals(test.startTime)
                            && Integer.valueOf(90).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(1).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found2 = true;

                    } else if (Long.valueOf(1003L).equals(test.serialNbr)
                            && "174RE".equals(test.version)
                            && "222222222".equals(test.stuId)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(9).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(62).equals(test.startTime)
                            && Integer.valueOf(92).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(3).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(4).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found4 = true;
                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found2, "Stexam 2 not found");
                assertTrue(found4, "Stexam 4 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getExams results")
    void test0013() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.getExams(cache, RawRecordConstants.M117, true, "U");

                assertEquals(0, all.size(), "Incorrect record count from queryAll");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getExams results")
    void test0014() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.getExams(cache, "111111111", RawRecordConstants.M117,
                        false, "R", "U");

                assertEquals(2, all.size(), "Incorrect record count from queryAll");

                boolean found2 = false;
                boolean found3 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1001L).equals(test.serialNbr)
                            && "171RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(10).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(60).equals(test.startTime)
                            && Integer.valueOf(90).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(1).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found2 = true;

                    } else if (Long.valueOf(1002L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(6).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(61).equals(test.startTime)
                            && Integer.valueOf(91).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(2).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "U".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && "TC".equals(test.examSource)
                            && "123".equals(test.calcNbr)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found2, "Stexam 2 not found");
                assertTrue(found3, "Stexam 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getExams results")
    void test0015() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.getExams(cache, "111111111", RawRecordConstants.M117, true,
                        "R", "U");

                assertEquals(1, all.size(), "Incorrect record count from queryAll");

                boolean found2 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1001L).equals(test.serialNbr)
                            && "171RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(10).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(60).equals(test.startTime)
                            && Integer.valueOf(90).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(1).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found2 = true;
                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found2, "Stexam 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getExams results")
    void test0016() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.getExams(cache, "111111111", RawRecordConstants.M117, true,
                        "U");

                assertEquals(0, all.size(), "Incorrect record count from queryAll");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getExams results")
    void test0017() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.getExams(cache, "111111111", RawRecordConstants.M117,
                        Integer.valueOf(1), false, "R", "U");

                assertEquals(2, all.size(), "Incorrect record count from queryAll");

                boolean found2 = false;
                boolean found3 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1001L).equals(test.serialNbr)
                            && "171RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(10).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(60).equals(test.startTime)
                            && Integer.valueOf(90).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(1).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found2 = true;

                    } else if (Long.valueOf(1002L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(6).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(61).equals(test.startTime)
                            && Integer.valueOf(91).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(2).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "U".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && "TC".equals(test.examSource)
                            && "123".equals(test.calcNbr)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found2, "Stexam 2 not found");
                assertTrue(found3, "Stexam 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getExams results")
    void test0018() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.getExams(cache, "111111111", RawRecordConstants.M117,
                        Integer.valueOf(1), true, "R", "U");

                assertEquals(1, all.size(), "Incorrect record count from queryAll");

                boolean found2 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1001L).equals(test.serialNbr)
                            && "171RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(10).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(60).equals(test.startTime)
                            && Integer.valueOf(90).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(1).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found2 = true;
                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found2, "Stexam 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getExams results")
    void test0019() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStexam> all = RawStexamLogic.getExams(cache, "111111111", RawRecordConstants.M117,
                        Integer.valueOf(1), true, "U");

                assertEquals(0, all.size(), "Incorrect record count from queryAll");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stexam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateScore results")
    void test0022() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                List<RawStexam> all = RawStexamLogic.queryByStudentCourse(cache, "111111111", RawRecordConstants.M100U,
                        true);

                assertEquals(1, all.size(), "Incorrect record count from queryByStudentCourse");

                assertTrue(RawStexamLogic.updatePassed(cache, all.getFirst(), "N"),
                        "Incorrect result from updatePassed");

                RawStexamLogic.recalculateFirstPassed(cache, "111111111", RawRecordConstants.M100U,
                        Integer.valueOf(0), "Q");

                all = RawStexamLogic.queryByStudentCourse(cache, "111111111", RawRecordConstants.M100U, true);

                assertEquals(1, all.size(), "Incorrect record count from queryByStudentCourse");

                final RawStexam test = all.getFirst();

                final boolean found = Long.valueOf(1000L).equals(test.serialNbr)
                        && "UUUUU".equals(test.version)
                        && "111111111".equals(test.stuId)
                        && date1.equals(test.examDt)
                        && Integer.valueOf(20).equals(test.examScore)
                        && Integer.valueOf(14).equals(test.masteryScore)
                        && Integer.valueOf(59).equals(test.startTime)
                        && Integer.valueOf(89).equals(test.finishTime)
                        && "Y".equals(test.timeOk)
                        && "N".equals(test.passed)
                        && Integer.valueOf(0).equals(test.seqNbr)
                        && RawRecordConstants.M100U.equals(test.course)
                        && Integer.valueOf(0).equals(test.unit)
                        && "Q".equals(test.examType)
                        && "N".equals(test.isFirstPassed)
                        && test.examSource == null
                        && test.calcNbr == null;

                assertTrue(found, "Updated Stexam is incorrect");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating stexam : " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateScoreAndPassed results")
    void test0023() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                List<RawStexam> all = RawStexamLogic.queryByStudentCourse(cache, "111111111", RawRecordConstants.M100U,
                        true);

                assertEquals(1, all.size(), "Incorrect record count from queryByStudentCourse");

                assertTrue(RawStexamLogic.updateScoreAndPassed(cache, all.getFirst(), Integer.valueOf(16), "Y"),
                        "Incorrect result from updateScoreAndPassed");

                RawStexamLogic.recalculateFirstPassed(cache, "111111111", RawRecordConstants.M100U,
                        Integer.valueOf(0), "Q");

                all = RawStexamLogic.queryByStudentCourse(cache, "111111111", RawRecordConstants.M100U, true);

                assertEquals(1, all.size(), "Incorrect record count from queryByStudentCourse");

                final RawStexam test = all.getFirst();

                final boolean found = Long.valueOf(1000L).equals(test.serialNbr)
                        && "UUUUU".equals(test.version)
                        && "111111111".equals(test.stuId)
                        && date1.equals(test.examDt)
                        && Integer.valueOf(16).equals(test.examScore)
                        && Integer.valueOf(14).equals(test.masteryScore)
                        && Integer.valueOf(59).equals(test.startTime)
                        && Integer.valueOf(89).equals(test.finishTime)
                        && "Y".equals(test.timeOk)
                        && "Y".equals(test.passed)
                        && Integer.valueOf(0).equals(test.seqNbr)
                        && RawRecordConstants.M100U.equals(test.course)
                        && Integer.valueOf(0).equals(test.unit)
                        && "Q".equals(test.examType)
                        && "Y".equals(test.isFirstPassed)
                        && test.examSource == null
                        && test.calcNbr == null;

                assertTrue(found, "Updated Stexam is incorrect");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating stexam : " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateWhenFinished results")
    void test0024() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                List<RawStexam> all = RawStexamLogic.queryByStudentCourse(cache, "111111111", RawRecordConstants.M100U,
                        true);

                assertEquals(1, all.size(), "Incorrect record count from queryByStudentCourse");

                assertTrue(RawStexamLogic.updateWhenFinished(cache, all.getFirst(), date2,
                        Integer.valueOf(100)), "Incorrect result from updateWhenFinished");

                RawStexamLogic.recalculateFirstPassed(cache, "111111111", RawRecordConstants.M100U,
                        Integer.valueOf(0), "Q");

                all = RawStexamLogic.queryByStudentCourse(cache, "111111111", RawRecordConstants.M100U, true);

                assertEquals(1, all.size(), "Incorrect record count from queryByStudentCourse");

                final RawStexam test = all.getFirst();

                final boolean found = Long.valueOf(1000L).equals(test.serialNbr)
                        && "UUUUU".equals(test.version)
                        && "111111111".equals(test.stuId)
                        && date2.equals(test.examDt)
                        && Integer.valueOf(16).equals(test.examScore)
                        && Integer.valueOf(14).equals(test.masteryScore)
                        && Integer.valueOf(59).equals(test.startTime)
                        && Integer.valueOf(100).equals(test.finishTime)
                        && "Y".equals(test.timeOk)
                        && "Y".equals(test.passed)
                        && Integer.valueOf(0).equals(test.seqNbr)
                        && RawRecordConstants.M100U.equals(test.course)
                        && Integer.valueOf(0).equals(test.unit)
                        && "Q".equals(test.examType)
                        && "Y".equals(test.isFirstPassed)
                        && test.examSource == null
                        && test.calcNbr == null;

                assertTrue(found, "Updated Stexam is incorrect");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating stexam : " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0025() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawStexam raw2 = new RawStexam(Long.valueOf(1001L), "171RE", "111111111",
                        date2, Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(60),
                        Integer.valueOf(90), "Y", "Y", Integer.valueOf(1), RawRecordConstants.M117,
                        Integer.valueOf(1), "R", "Y", null, null);

                final boolean result = RawStexamLogic.INSTANCE.delete(cache, raw2);

                assertTrue(result, "delete returned false");

                final List<RawStexam> all = RawStexamLogic.INSTANCE.queryAll(cache);

                assertEquals(6, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;
                boolean found7 = false;

                for (final RawStexam test : all) {

                    if (Long.valueOf(1000L).equals(test.serialNbr)
                            && "UUUUU".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(16).equals(test.examScore)
                            && Integer.valueOf(14).equals(test.masteryScore)
                            && Integer.valueOf(59).equals(test.startTime)
                            && Integer.valueOf(100).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(0).equals(test.seqNbr)
                            && RawRecordConstants.M100U.equals(test.course)
                            && Integer.valueOf(0).equals(test.unit)
                            && "Q".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found1 = true;

                    } else if (Long.valueOf(1002L).equals(test.serialNbr)
                            && "171UE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(6).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(61).equals(test.startTime)
                            && Integer.valueOf(91).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(2).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "U".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && "TC".equals(test.examSource)
                            && "123".equals(test.calcNbr)) {

                        found3 = true;

                    } else if (Long.valueOf(1003L).equals(test.serialNbr)
                            && "174RE".equals(test.version)
                            && "222222222".equals(test.stuId)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(9).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(62).equals(test.startTime)
                            && Integer.valueOf(92).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(3).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(4).equals(test.unit)
                            && "R".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found4 = true;

                    } else if (Long.valueOf(1004L).equals(test.serialNbr)
                            && "18FIN".equals(test.version)
                            && "333333333".equals(test.stuId)
                            && date5.equals(test.examDt)
                            && Integer.valueOf(13).equals(test.examScore)
                            && Integer.valueOf(16).equals(test.masteryScore)
                            && Integer.valueOf(1430).equals(test.startTime)
                            && Integer.valueOf(1449).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "N".equals(test.passed)
                            && Integer.valueOf(4).equals(test.seqNbr)
                            && RawRecordConstants.M118.equals(test.course)
                            && Integer.valueOf(5).equals(test.unit)
                            && "F".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && "RE".equals(test.examSource)
                            && "987".equals(test.calcNbr)) {

                        found5 = true;

                    } else if (Long.valueOf(1005L).equals(test.serialNbr)
                            && "18FIN".equals(test.version)
                            && "333333333".equals(test.stuId)
                            && date6.equals(test.examDt)
                            && Integer.valueOf(19).equals(test.examScore)
                            && Integer.valueOf(16).equals(test.masteryScore)
                            && Integer.valueOf(1441).equals(test.startTime)
                            && Integer.valueOf(1450).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "Y".equals(test.passed)
                            && Integer.valueOf(5).equals(test.seqNbr)
                            && RawRecordConstants.M118.equals(test.course)
                            && Integer.valueOf(5).equals(test.unit)
                            && "F".equals(test.examType)
                            && "Y".equals(test.isFirstPassed)
                            && "PU".equals(test.examSource)
                            && test.calcNbr == null) {

                        found6 = true;
                    } else if (Long.valueOf(1006L).equals(test.serialNbr)
                            && "172RE".equals(test.version)
                            && "111111111".equals(test.stuId)
                            && date8.equals(test.examDt)
                            && Integer.valueOf(8).equals(test.examScore)
                            && Integer.valueOf(8).equals(test.masteryScore)
                            && Integer.valueOf(63).equals(test.startTime)
                            && Integer.valueOf(83).equals(test.finishTime)
                            && "Y".equals(test.timeOk)
                            && "G".equals(test.passed)
                            && Integer.valueOf(6).equals(test.seqNbr)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(2).equals(test.unit)
                            && "R".equals(test.examType)
                            && "N".equals(test.isFirstPassed)
                            && test.examSource == null
                            && test.calcNbr == null) {

                        found7 = true;
                    } else {
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected masteryScore ", test.masteryScore);
                        Log.warning("Unexpected startTime ", test.startTime);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected timeOk ", test.timeOk);
                        Log.warning("Unexpected passed ", test.passed);
                        Log.warning("Unexpected seqNbr ", test.seqNbr);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected examType ", test.examType);
                        Log.warning("Unexpected isFirstPassed ", test.isFirstPassed);
                        Log.warning("Unexpected examSource ", test.examSource);
                        Log.warning("Unexpected calcNbr ", test.calcNbr);
                    }
                }

                assertTrue(found1, "Stexam 1 not found");
                assertTrue(found3, "Stexam 3 not found");
                assertTrue(found4, "Stexam 4 not found");
                assertTrue(found5, "Stexam 5 not found");
                assertTrue(found6, "Stexam 6 not found");
                assertTrue(found7, "Stexam 7 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework rows afer delete: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM stexam");
                }

                conn.commit();

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning table: " + ex.getMessage());
        }
    }
}
