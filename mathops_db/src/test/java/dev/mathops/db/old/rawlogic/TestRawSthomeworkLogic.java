package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSthomework;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawSthomeworkLogic} class.
 */
final class TestRawSthomeworkLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 1);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 1, 3);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2021, 1, 4);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2021, 1, 5);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2021, 2, 2);

    /** The database profile. */
    private static Profile profile = null;

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        profile = config.getCodeProfile(Contexts.INFORMIX_TEST_PATH);
        if (profile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_TEST_PROFILE));
        }

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();
        final Cache cache = new Cache(profile);

        // Make sure we're in the TEST database
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

            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM sthomework");
            }
            conn.commit();

            final RawSthomework raw1 = new RawSthomework(Long.valueOf(1000L), "UUUUU", "111111111",
                    date1, Integer.valueOf(2), Integer.valueOf(59), Integer.valueOf(89), "Y",
                    "Y", "HW", RawRecordConstants.M100U, "1", Integer.valueOf(0), Integer.valueOf(0), "Y",
                    date6, Long.valueOf(100L));

            final RawSthomework raw2 = new RawSthomework(Long.valueOf(1001L), "1711H", "111111111",
                    date2, Integer.valueOf(3), Integer.valueOf(58), Integer.valueOf(88), "Y",
                    "Y", "HW", RawRecordConstants.M117, "001", Integer.valueOf(1), Integer.valueOf(1), "N", null,
                    null);

            final RawSthomework raw3 = new RawSthomework(Long.valueOf(1002L), "1721H", "111111111",
                    date3, Integer.valueOf(0), Integer.valueOf(57), Integer.valueOf(87), "Y",
                    "N", "LB", RawRecordConstants.M117, "001", Integer.valueOf(2), Integer.valueOf(1), "N", null,
                    null);

            final RawSthomework raw4 = new RawSthomework(Long.valueOf(1003L), "1812H", "111111111",
                    date4, Integer.valueOf(2), Integer.valueOf(56), Integer.valueOf(86),
                    "Y", "N", "HW", RawRecordConstants.M118, "002", Integer.valueOf(1), Integer.valueOf(2), "N",
                    null, null);

            final RawSthomework raw5 = new RawSthomework(Long.valueOf(1004L), "1711H", "222222222",
                    date5, Integer.valueOf(1), Integer.valueOf(55), Integer.valueOf(85), "Y",
                    "N", "HW", RawRecordConstants.M117, "001", Integer.valueOf(1), Integer.valueOf(1), "N", null,
                    null);

            assertTrue(RawSthomeworkLogic.insert(cache, raw1), "Failed to insert sthomework 1");
            assertTrue(RawSthomeworkLogic.insert(cache, raw2), "Failed to insert sthomework 2");
            assertTrue(RawSthomeworkLogic.insert(cache, raw3), "Failed to insert sthomework 3");
            assertTrue(RawSthomeworkLogic.insert(cache, raw4), "Failed to insert sthomework 4");
            assertTrue(RawSthomeworkLogic.insert(cache, raw5), "Failed to insert sthomework 5");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing tables: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0003() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSthomework> all = RawSthomeworkLogic.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawSthomework test : all) {

                if (Long.valueOf(1000L).equals(test.serialNbr)
                    && "UUUUU".equals(test.version)
                    && "111111111".equals(test.stuId)
                    && date1.equals(test.hwDt)
                    && Integer.valueOf(2).equals(test.hwScore)
                    && Integer.valueOf(59).equals(test.startTime)
                    && Integer.valueOf(89).equals(test.finishTime)
                    && "Y".equals(test.timeOk)
                    && "Y".equals(test.passed)
                    && "HW".equals(test.hwType)
                    && RawRecordConstants.M100U.equals(test.course)
                    && "1".equals(test.sect)
                    && Integer.valueOf(0).equals(test.unit)
                    && Integer.valueOf(0).equals(test.objective)
                    && "Y".equals(test.hwCoupon)
                    && date6.equals(test.usedDt)
                    && Long.valueOf(100L).equals(test.usedSerialNbr)) {

                    found1 = true;

                } else if (Long.valueOf(1001L).equals(test.serialNbr)
                           && "1711H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date2.equals(test.hwDt)
                           && Integer.valueOf(3).equals(test.hwScore)
                           && Integer.valueOf(58).equals(test.startTime)
                           && Integer.valueOf(88).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "Y".equals(test.passed)
                           && "HW".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(1).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found2 = true;

                } else if (Long.valueOf(1002L).equals(test.serialNbr)
                           && "1721H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date3.equals(test.hwDt)
                           && Integer.valueOf(0).equals(test.hwScore)
                           && Integer.valueOf(57).equals(test.startTime)
                           && Integer.valueOf(87).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "LB".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(2).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found3 = true;

                } else if (Long.valueOf(1003L).equals(test.serialNbr)
                           && "1812H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date4.equals(test.hwDt)
                           && Integer.valueOf(2).equals(test.hwScore)
                           && Integer.valueOf(56).equals(test.startTime)
                           && Integer.valueOf(86).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "HW".equals(test.hwType)
                           && RawRecordConstants.M118.equals(test.course)
                           && "002".equals(test.sect)
                           && Integer.valueOf(1).equals(test.unit)
                           && Integer.valueOf(2).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found4 = true;

                } else if (Long.valueOf(1004L).equals(test.serialNbr)
                           && "1711H".equals(test.version)
                           && "222222222".equals(test.stuId)
                           && date5.equals(test.hwDt)
                           && Integer.valueOf(1).equals(test.hwScore)
                           && Integer.valueOf(55).equals(test.startTime)
                           && Integer.valueOf(85).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "HW".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(1).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found5 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected hwScore ", test.hwScore);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timeOk ", test.timeOk);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected hwType ", test.hwType);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected sect ", test.sect);
                    Log.warning("Unexpected unit ", test.unit);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected hwCoupon ", test.hwCoupon);
                    Log.warning("Unexpected usedDt ", test.usedDt);
                    Log.warning("Unexpected usedSerialNbr ", test.usedSerialNbr);
                }
            }

            assertTrue(found1, "Sthomework 1 not found");
            assertTrue(found2, "Sthomework 2 not found");
            assertTrue(found3, "Sthomework 3 not found");
            assertTrue(found4, "Sthomework 4 not found");
            assertTrue(found5, "Sthomework 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSthomework> all = RawSthomeworkLogic.queryByStudent(cache, "111111111", true);

            assertEquals(4, all.size(), "Incorrect record count from queryByStudent");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawSthomework test : all) {

                if (Long.valueOf(1000L).equals(test.serialNbr)
                    && "UUUUU".equals(test.version)
                    && "111111111".equals(test.stuId)
                    && date1.equals(test.hwDt)
                    && Integer.valueOf(2).equals(test.hwScore)
                    && Integer.valueOf(59).equals(test.startTime)
                    && Integer.valueOf(89).equals(test.finishTime)
                    && "Y".equals(test.timeOk)
                    && "Y".equals(test.passed)
                    && "HW".equals(test.hwType)
                    && RawRecordConstants.M100U.equals(test.course)
                    && "1".equals(test.sect)
                    && Integer.valueOf(0).equals(test.unit)
                    && Integer.valueOf(0).equals(test.objective)
                    && "Y".equals(test.hwCoupon)
                    && date6.equals(test.usedDt)
                    && Long.valueOf(100L).equals(test.usedSerialNbr)) {

                    found1 = true;

                } else if (Long.valueOf(1001L).equals(test.serialNbr)
                           && "1711H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date2.equals(test.hwDt)
                           && Integer.valueOf(3).equals(test.hwScore)
                           && Integer.valueOf(58).equals(test.startTime)
                           && Integer.valueOf(88).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "Y".equals(test.passed)
                           && "HW".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(1).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found2 = true;

                } else if (Long.valueOf(1002L).equals(test.serialNbr)
                           && "1721H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date3.equals(test.hwDt)
                           && Integer.valueOf(0).equals(test.hwScore)
                           && Integer.valueOf(57).equals(test.startTime)
                           && Integer.valueOf(87).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "LB".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(2).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found3 = true;

                } else if (Long.valueOf(1003L).equals(test.serialNbr)
                           && "1812H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date4.equals(test.hwDt)
                           && Integer.valueOf(2).equals(test.hwScore)
                           && Integer.valueOf(56).equals(test.startTime)
                           && Integer.valueOf(86).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "HW".equals(test.hwType)
                           && RawRecordConstants.M118.equals(test.course)
                           && "002".equals(test.sect)
                           && Integer.valueOf(1).equals(test.unit)
                           && Integer.valueOf(2).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found4 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected hwScore ", test.hwScore);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timeOk ", test.timeOk);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected hwType ", test.hwType);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected sect ", test.sect);
                    Log.warning("Unexpected unit ", test.unit);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected hwCoupon ", test.hwCoupon);
                    Log.warning("Unexpected usedDt ", test.usedDt);
                    Log.warning("Unexpected usedSerialNbr ", test.usedSerialNbr);
                }
            }

            assertTrue(found1, "Sthomework 1 not found");
            assertTrue(found2, "Sthomework 2 not found");
            assertTrue(found3, "Sthomework 3 not found");
            assertTrue(found4, "Sthomework 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSthomework> all = RawSthomeworkLogic.queryByStudent(cache, "111111111", false);

            assertEquals(4, all.size(), "Incorrect record count from queryByStudent");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawSthomework test : all) {

                if (Long.valueOf(1000L).equals(test.serialNbr)
                    && "UUUUU".equals(test.version)
                    && "111111111".equals(test.stuId)
                    && date1.equals(test.hwDt)
                    && Integer.valueOf(2).equals(test.hwScore)
                    && Integer.valueOf(59).equals(test.startTime)
                    && Integer.valueOf(89).equals(test.finishTime)
                    && "Y".equals(test.timeOk)
                    && "Y".equals(test.passed)
                    && "HW".equals(test.hwType)
                    && RawRecordConstants.M100U.equals(test.course)
                    && "1".equals(test.sect)
                    && Integer.valueOf(0).equals(test.unit)
                    && Integer.valueOf(0).equals(test.objective)
                    && "Y".equals(test.hwCoupon)
                    && date6.equals(test.usedDt)
                    && Long.valueOf(100L).equals(test.usedSerialNbr)) {

                    found1 = true;

                } else if (Long.valueOf(1001L).equals(test.serialNbr)
                           && "1711H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date2.equals(test.hwDt)
                           && Integer.valueOf(3).equals(test.hwScore)
                           && Integer.valueOf(58).equals(test.startTime)
                           && Integer.valueOf(88).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "Y".equals(test.passed)
                           && "HW".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(1).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found2 = true;

                } else if (Long.valueOf(1002L).equals(test.serialNbr)
                           && "1721H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date3.equals(test.hwDt)
                           && Integer.valueOf(0).equals(test.hwScore)
                           && Integer.valueOf(57).equals(test.startTime)
                           && Integer.valueOf(87).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "LB".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(2).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found3 = true;

                } else if (Long.valueOf(1003L).equals(test.serialNbr)
                           && "1812H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date4.equals(test.hwDt)
                           && Integer.valueOf(2).equals(test.hwScore)
                           && Integer.valueOf(56).equals(test.startTime)
                           && Integer.valueOf(86).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "HW".equals(test.hwType)
                           && RawRecordConstants.M118.equals(test.course)
                           && "002".equals(test.sect)
                           && Integer.valueOf(1).equals(test.unit)
                           && Integer.valueOf(2).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found4 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected hwScore ", test.hwScore);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timeOk ", test.timeOk);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected hwType ", test.hwType);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected sect ", test.sect);
                    Log.warning("Unexpected unit ", test.unit);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected hwCoupon ", test.hwCoupon);
                    Log.warning("Unexpected usedDt ", test.usedDt);
                    Log.warning("Unexpected usedSerialNbr ", test.usedSerialNbr);
                }
            }

            assertTrue(found1, "Sthomework 1 not found");
            assertTrue(found2, "Sthomework 2 not found");
            assertTrue(found3, "Sthomework 3 not found");
            assertTrue(found4, "Sthomework 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudentCourse results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSthomework> all = RawSthomeworkLogic.queryByStudentCourse(cache, "111111111",
                    RawRecordConstants.M117, true);

            assertEquals(2, all.size(), "Incorrect record count from queryByStudentCourse");

            boolean found2 = false;
            boolean found3 = false;

            for (final RawSthomework test : all) {

                if (Long.valueOf(1001L).equals(test.serialNbr)
                    && "1711H".equals(test.version)
                    && "111111111".equals(test.stuId)
                    && date2.equals(test.hwDt)
                    && Integer.valueOf(3).equals(test.hwScore)
                    && Integer.valueOf(58).equals(test.startTime)
                    && Integer.valueOf(88).equals(test.finishTime)
                    && "Y".equals(test.timeOk)
                    && "Y".equals(test.passed)
                    && "HW".equals(test.hwType)
                    && RawRecordConstants.M117.equals(test.course)
                    && "001".equals(test.sect)
                    && Integer.valueOf(1).equals(test.unit)
                    && Integer.valueOf(1).equals(test.objective)
                    && "N".equals(test.hwCoupon)
                    && test.usedDt == null
                    && test.usedSerialNbr == null) {

                    found2 = true;

                } else if (Long.valueOf(1002L).equals(test.serialNbr)
                           && "1721H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date3.equals(test.hwDt)
                           && Integer.valueOf(0).equals(test.hwScore)
                           && Integer.valueOf(57).equals(test.startTime)
                           && Integer.valueOf(87).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "LB".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(2).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected hwScore ", test.hwScore);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timeOk ", test.timeOk);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected hwType ", test.hwType);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected sect ", test.sect);
                    Log.warning("Unexpected unit ", test.unit);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected hwCoupon ", test.hwCoupon);
                    Log.warning("Unexpected usedDt ", test.usedDt);
                    Log.warning("Unexpected usedSerialNbr ", test.usedSerialNbr);
                }
            }

            assertTrue(found2, "Sthomework 2 not found");
            assertTrue(found3, "Sthomework 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework by student/course: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudentCourse results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSthomework> all = RawSthomeworkLogic.queryByStudentCourse(cache, "111111111",
                    RawRecordConstants.M117, false);

            assertEquals(2, all.size(), "Incorrect record count from queryByStudentCourse");

            boolean found2 = false;
            boolean found3 = false;

            for (final RawSthomework test : all) {

                if (Long.valueOf(1001L).equals(test.serialNbr)
                    && "1711H".equals(test.version)
                    && "111111111".equals(test.stuId)
                    && date2.equals(test.hwDt)
                    && Integer.valueOf(3).equals(test.hwScore)
                    && Integer.valueOf(58).equals(test.startTime)
                    && Integer.valueOf(88).equals(test.finishTime)
                    && "Y".equals(test.timeOk)
                    && "Y".equals(test.passed)
                    && "HW".equals(test.hwType)
                    && RawRecordConstants.M117.equals(test.course)
                    && "001".equals(test.sect)
                    && Integer.valueOf(1).equals(test.unit)
                    && Integer.valueOf(1).equals(test.objective)
                    && "N".equals(test.hwCoupon)
                    && test.usedDt == null
                    && test.usedSerialNbr == null) {

                    found2 = true;

                } else if (Long.valueOf(1002L).equals(test.serialNbr)
                           && "1721H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date3.equals(test.hwDt)
                           && Integer.valueOf(0).equals(test.hwScore)
                           && Integer.valueOf(57).equals(test.startTime)
                           && Integer.valueOf(87).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "LB".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(2).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected hwScore ", test.hwScore);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timeOk ", test.timeOk);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected hwType ", test.hwType);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected sect ", test.sect);
                    Log.warning("Unexpected unit ", test.unit);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected hwCoupon ", test.hwCoupon);
                    Log.warning("Unexpected usedDt ", test.usedDt);
                    Log.warning("Unexpected usedSerialNbr ", test.usedSerialNbr);
                }
            }

            assertTrue(found2, "Sthomework 2 not found");
            assertTrue(found3, "Sthomework 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework by student/course: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudentCourseUnit results")
    void test0008() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSthomework> all = RawSthomeworkLogic.queryByStudentCourseUnit(cache, "111111111",
                    RawRecordConstants.M117, Integer.valueOf(1), true);

            assertEquals(1, all.size(), "Incorrect record count from queryByStudentCourseUnit");

            boolean found2 = false;

            for (final RawSthomework test : all) {

                if (Long.valueOf(1001L).equals(test.serialNbr)
                    && "1711H".equals(test.version)
                    && "111111111".equals(test.stuId)
                    && date2.equals(test.hwDt)
                    && Integer.valueOf(3).equals(test.hwScore)
                    && Integer.valueOf(58).equals(test.startTime)
                    && Integer.valueOf(88).equals(test.finishTime)
                    && "Y".equals(test.timeOk)
                    && "Y".equals(test.passed)
                    && "HW".equals(test.hwType)
                    && RawRecordConstants.M117.equals(test.course)
                    && "001".equals(test.sect)
                    && Integer.valueOf(1).equals(test.unit)
                    && Integer.valueOf(1).equals(test.objective)
                    && "N".equals(test.hwCoupon)
                    && test.usedDt == null
                    && test.usedSerialNbr == null) {

                    found2 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected hwScore ", test.hwScore);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timeOk ", test.timeOk);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected hwType ", test.hwType);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected sect ", test.sect);
                    Log.warning("Unexpected unit ", test.unit);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected hwCoupon ", test.hwCoupon);
                    Log.warning("Unexpected usedDt ", test.usedDt);
                    Log.warning("Unexpected usedSerialNbr ", test.usedSerialNbr);
                }
            }

            assertTrue(found2, "Sthomework 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework by student/course/unit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudentCourseUnit results")
    void test0009() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSthomework> all = RawSthomeworkLogic.queryByStudentCourseUnit(cache, "111111111",
                    RawRecordConstants.M117, Integer.valueOf(1), false);

            assertEquals(1, all.size(), "Incorrect record count from queryByStudentCourseUnit");

            boolean found2 = false;

            for (final RawSthomework test : all) {

                if (Long.valueOf(1001L).equals(test.serialNbr)
                    && "1711H".equals(test.version)
                    && "111111111".equals(test.stuId)
                    && date2.equals(test.hwDt)
                    && Integer.valueOf(3).equals(test.hwScore)
                    && Integer.valueOf(58).equals(test.startTime)
                    && Integer.valueOf(88).equals(test.finishTime)
                    && "Y".equals(test.timeOk)
                    && "Y".equals(test.passed)
                    && "HW".equals(test.hwType)
                    && RawRecordConstants.M117.equals(test.course)
                    && "001".equals(test.sect)
                    && Integer.valueOf(1).equals(test.unit)
                    && Integer.valueOf(1).equals(test.objective)
                    && "N".equals(test.hwCoupon)
                    && test.usedDt == null
                    && test.usedSerialNbr == null) {

                    found2 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected hwScore ", test.hwScore);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timeOk ", test.timeOk);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected hwType ", test.hwType);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected sect ", test.sect);
                    Log.warning("Unexpected unit ", test.unit);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected hwCoupon ", test.hwCoupon);
                    Log.warning("Unexpected usedDt ", test.usedDt);
                    Log.warning("Unexpected usedSerialNbr ", test.usedSerialNbr);
                }
            }

            assertTrue(found2, "Sthomework 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework by student/course/unit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0010() {

        final Cache cache = new Cache(profile);

        try {
            final List<List<RawSthomework>> history = new ArrayList<>(10);
            final LocalDate today = LocalDate.of(2021, 1, 10);

            RawSthomeworkLogic.getHistory(cache, history, 10, today, RawRecordConstants.M117);

            assertEquals(10, history.size(), "Incorrect result list count from getHistory");

            final List<RawSthomework> jan1 = history.get(0);
            final List<RawSthomework> jan2 = history.get(1);
            final List<RawSthomework> jan3 = history.get(2);
            final List<RawSthomework> jan4 = history.get(3);
            final List<RawSthomework> jan5 = history.get(4);
            final List<RawSthomework> jan6 = history.get(5);
            final List<RawSthomework> jan7 = history.get(6);
            final List<RawSthomework> jan8 = history.get(7);
            final List<RawSthomework> jan9 = history.get(8);
            final List<RawSthomework> jan10 = history.get(9);

            assertEquals(0, jan1.size(), "Incorrect count for Jan 1 from getHistory");
            assertEquals(1, jan2.size(), "Incorrect count for Jan 2 from getHistory");
            assertEquals(1, jan3.size(), "Incorrect count for Jan 3 from getHistory");
            assertEquals(0, jan4.size(), "Incorrect count for Jan 4 from getHistory");
            assertEquals(1, jan5.size(), "Incorrect count for Jan 5 from getHistory");
            assertEquals(0, jan6.size(), "Incorrect count for Jan 6 from getHistory");
            assertEquals(0, jan7.size(), "Incorrect count for Jan 7 from getHistory");
            assertEquals(0, jan8.size(), "Incorrect count for Jan 8 from getHistory");
            assertEquals(0, jan9.size(), "Incorrect count for Jan 9 from getHistory");
            assertEquals(0, jan10.size(), "Incorrect count for Jan 10 from getHistory");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getExams results")
    void test0011() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSthomework> all = RawSthomeworkLogic.getHomeworks(cache, "111111111",
                    RawRecordConstants.M117, false, "HW", "LB");

            assertEquals(2, all.size(), "Incorrect record count from getHomeworks");

            boolean found2 = false;
            boolean found3 = false;

            for (final RawSthomework test : all) {

                if (Long.valueOf(1001L).equals(test.serialNbr)
                    && "1711H".equals(test.version)
                    && "111111111".equals(test.stuId)
                    && date2.equals(test.hwDt)
                    && Integer.valueOf(3).equals(test.hwScore)
                    && Integer.valueOf(58).equals(test.startTime)
                    && Integer.valueOf(88).equals(test.finishTime)
                    && "Y".equals(test.timeOk)
                    && "Y".equals(test.passed)
                    && "HW".equals(test.hwType)
                    && RawRecordConstants.M117.equals(test.course)
                    && "001".equals(test.sect)
                    && Integer.valueOf(1).equals(test.unit)
                    && Integer.valueOf(1).equals(test.objective)
                    && "N".equals(test.hwCoupon)
                    && test.usedDt == null
                    && test.usedSerialNbr == null) {

                    found2 = true;

                } else if (Long.valueOf(1002L).equals(test.serialNbr)
                           && "1721H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date3.equals(test.hwDt)
                           && Integer.valueOf(0).equals(test.hwScore)
                           && Integer.valueOf(57).equals(test.startTime)
                           && Integer.valueOf(87).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "LB".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(2).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected hwScore ", test.hwScore);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timeOk ", test.timeOk);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected hwType ", test.hwType);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected sect ", test.sect);
                    Log.warning("Unexpected unit ", test.unit);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected hwCoupon ", test.hwCoupon);
                    Log.warning("Unexpected usedDt ", test.usedDt);
                    Log.warning("Unexpected usedSerialNbr ", test.usedSerialNbr);
                }
            }

            assertTrue(found2, "Sthomework 2 not found");
            assertTrue(found3, "Sthomework 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getExams results")
    void test0012() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSthomework> all = RawSthomeworkLogic.getHomeworks(cache, "111111111",
                    RawRecordConstants.M117, true, "HW", "LB");

            assertEquals(1, all.size(), "Incorrect record count from getHomeworks");

            boolean found2 = false;

            for (final RawSthomework test : all) {

                if (Long.valueOf(1001L).equals(test.serialNbr)
                    && "1711H".equals(test.version)
                    && "111111111".equals(test.stuId)
                    && date2.equals(test.hwDt)
                    && Integer.valueOf(3).equals(test.hwScore)
                    && Integer.valueOf(58).equals(test.startTime)
                    && Integer.valueOf(88).equals(test.finishTime)
                    && "Y".equals(test.timeOk)
                    && "Y".equals(test.passed)
                    && "HW".equals(test.hwType)
                    && RawRecordConstants.M117.equals(test.course)
                    && "001".equals(test.sect)
                    && Integer.valueOf(1).equals(test.unit)
                    && Integer.valueOf(1).equals(test.objective)
                    && "N".equals(test.hwCoupon)
                    && test.usedDt == null
                    && test.usedSerialNbr == null) {

                    found2 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected hwScore ", test.hwScore);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timeOk ", test.timeOk);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected hwType ", test.hwType);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected sect ", test.sect);
                    Log.warning("Unexpected unit ", test.unit);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected hwCoupon ", test.hwCoupon);
                    Log.warning("Unexpected usedDt ", test.usedDt);
                    Log.warning("Unexpected usedSerialNbr ", test.usedSerialNbr);
                }
            }

            assertTrue(found2, "Sthomework 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getExams results")
    void test0013() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSthomework> all = RawSthomeworkLogic.getHomeworks(cache, "111111111",
                    RawRecordConstants.M117, Integer.valueOf(2), false, "HW", "LB");

            assertEquals(1, all.size(), "Incorrect record count from getHomeworks");

            boolean found3 = false;

            for (final RawSthomework test : all) {

                if (Long.valueOf(1002L).equals(test.serialNbr)
                    && "1721H".equals(test.version)
                    && "111111111".equals(test.stuId)
                    && date3.equals(test.hwDt)
                    && Integer.valueOf(0).equals(test.hwScore)
                    && Integer.valueOf(57).equals(test.startTime)
                    && Integer.valueOf(87).equals(test.finishTime)
                    && "Y".equals(test.timeOk)
                    && "N".equals(test.passed)
                    && "LB".equals(test.hwType)
                    && RawRecordConstants.M117.equals(test.course)
                    && "001".equals(test.sect)
                    && Integer.valueOf(2).equals(test.unit)
                    && Integer.valueOf(1).equals(test.objective)
                    && "N".equals(test.hwCoupon)
                    && test.usedDt == null
                    && test.usedSerialNbr == null) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected hwScore ", test.hwScore);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timeOk ", test.timeOk);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected hwType ", test.hwType);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected sect ", test.sect);
                    Log.warning("Unexpected unit ", test.unit);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected hwCoupon ", test.hwCoupon);
                    Log.warning("Unexpected usedDt ", test.usedDt);
                    Log.warning("Unexpected usedSerialNbr ", test.usedSerialNbr);
                }
            }

            assertTrue(found3, "Sthomework 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getExams results")
    void test0014() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSthomework> all = RawSthomeworkLogic.getHomeworks(cache, "111111111",
                    RawRecordConstants.M117, Integer.valueOf(2), true, "HW", "LB");

            assertEquals(0, all.size(), "Incorrect record count from getHomeworks");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateFinishTimeScore results")
    void test0015() {

        final Cache cache = new Cache(profile);

        try {
            final boolean result = RawSthomeworkLogic.updateFinishTimeScore(cache, Long.valueOf(1000L), "UUUUU",
                    "111111111", 27, 28, "Z");

            assertTrue(result, "updateFinishTimeScore returned false");

            final List<RawSthomework> all = RawSthomeworkLogic.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll after updateFinishTimeScore");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawSthomework test : all) {

                if (Long.valueOf(1000L).equals(test.serialNbr)
                    && "UUUUU".equals(test.version)
                    && "111111111".equals(test.stuId)
                    && date1.equals(test.hwDt)
                    && Integer.valueOf(28).equals(test.hwScore)
                    && Integer.valueOf(59).equals(test.startTime)
                    && Integer.valueOf(27).equals(test.finishTime)
                    && "Y".equals(test.timeOk)
                    && "Z".equals(test.passed)
                    && "HW".equals(test.hwType)
                    && RawRecordConstants.M100U.equals(test.course)
                    && "1".equals(test.sect)
                    && Integer.valueOf(0).equals(test.unit)
                    && Integer.valueOf(0).equals(test.objective)
                    && "Y".equals(test.hwCoupon)
                    && date6.equals(test.usedDt)
                    && Long.valueOf(100L).equals(test.usedSerialNbr)) {

                    found1 = true;

                } else if (Long.valueOf(1001L).equals(test.serialNbr)
                           && "1711H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date2.equals(test.hwDt)
                           && Integer.valueOf(3).equals(test.hwScore)
                           && Integer.valueOf(58).equals(test.startTime)
                           && Integer.valueOf(88).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "Y".equals(test.passed)
                           && "HW".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(1).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found2 = true;

                } else if (Long.valueOf(1002L).equals(test.serialNbr)
                           && "1721H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date3.equals(test.hwDt)
                           && Integer.valueOf(0).equals(test.hwScore)
                           && Integer.valueOf(57).equals(test.startTime)
                           && Integer.valueOf(87).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "LB".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(2).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found3 = true;

                } else if (Long.valueOf(1003L).equals(test.serialNbr)
                           && "1812H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date4.equals(test.hwDt)
                           && Integer.valueOf(2).equals(test.hwScore)
                           && Integer.valueOf(56).equals(test.startTime)
                           && Integer.valueOf(86).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "HW".equals(test.hwType)
                           && RawRecordConstants.M118.equals(test.course)
                           && "002".equals(test.sect)
                           && Integer.valueOf(1).equals(test.unit)
                           && Integer.valueOf(2).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found4 = true;

                } else if (Long.valueOf(1004L).equals(test.serialNbr)
                           && "1711H".equals(test.version)
                           && "222222222".equals(test.stuId)
                           && date5.equals(test.hwDt)
                           && Integer.valueOf(1).equals(test.hwScore)
                           && Integer.valueOf(55).equals(test.startTime)
                           && Integer.valueOf(85).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "HW".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(1).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found5 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected hwScore ", test.hwScore);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timeOk ", test.timeOk);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected hwType ", test.hwType);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected sect ", test.sect);
                    Log.warning("Unexpected unit ", test.unit);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected hwCoupon ", test.hwCoupon);
                    Log.warning("Unexpected usedDt ", test.usedDt);
                    Log.warning("Unexpected usedSerialNbr ", test.usedSerialNbr);
                }
            }

            assertTrue(found1, "Sthomework 1 not found");
            assertTrue(found2, "Sthomework 2 not found");
            assertTrue(found3, "Sthomework 3 not found");
            assertTrue(found4, "Sthomework 4 not found");
            assertTrue(found5, "Sthomework 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework rows after updateFinishTimeScore: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0016() {

        final Cache cache = new Cache(profile);

        try {
            final RawSthomework raw2 = new RawSthomework(Long.valueOf(1001L), "1711H", "111111111",
                    date2, Integer.valueOf(3), Integer.valueOf(58), Integer.valueOf(88), "Y",
                    "Y", "HW", RawRecordConstants.M117, "001", Integer.valueOf(1), Integer.valueOf(1), "N", null,
                    null);

            final boolean result = RawSthomeworkLogic.delete(cache, raw2);

            assertTrue(result, "delete returned false");

            final List<RawSthomework> all = RawSthomeworkLogic.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawSthomework test : all) {

                if (Long.valueOf(1000L).equals(test.serialNbr)
                    && "UUUUU".equals(test.version)
                    && "111111111".equals(test.stuId)
                    && date1.equals(test.hwDt)
                    && Integer.valueOf(28).equals(test.hwScore)
                    && Integer.valueOf(59).equals(test.startTime)
                    && Integer.valueOf(27).equals(test.finishTime)
                    && "Y".equals(test.timeOk)
                    && "Z".equals(test.passed)
                    && "HW".equals(test.hwType)
                    && RawRecordConstants.M100U.equals(test.course)
                    && "1".equals(test.sect)
                    && Integer.valueOf(0).equals(test.unit)
                    && Integer.valueOf(0).equals(test.objective)
                    && "Y".equals(test.hwCoupon)
                    && date6.equals(test.usedDt)
                    && Long.valueOf(100L).equals(test.usedSerialNbr)) {

                    found1 = true;

                } else if (Long.valueOf(1002L).equals(test.serialNbr)
                           && "1721H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date3.equals(test.hwDt)
                           && Integer.valueOf(0).equals(test.hwScore)
                           && Integer.valueOf(57).equals(test.startTime)
                           && Integer.valueOf(87).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "LB".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(2).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found3 = true;

                } else if (Long.valueOf(1003L).equals(test.serialNbr)
                           && "1812H".equals(test.version)
                           && "111111111".equals(test.stuId)
                           && date4.equals(test.hwDt)
                           && Integer.valueOf(2).equals(test.hwScore)
                           && Integer.valueOf(56).equals(test.startTime)
                           && Integer.valueOf(86).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "HW".equals(test.hwType)
                           && RawRecordConstants.M118.equals(test.course)
                           && "002".equals(test.sect)
                           && Integer.valueOf(1).equals(test.unit)
                           && Integer.valueOf(2).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found4 = true;

                } else if (Long.valueOf(1004L).equals(test.serialNbr)
                           && "1711H".equals(test.version)
                           && "222222222".equals(test.stuId)
                           && date5.equals(test.hwDt)
                           && Integer.valueOf(1).equals(test.hwScore)
                           && Integer.valueOf(55).equals(test.startTime)
                           && Integer.valueOf(85).equals(test.finishTime)
                           && "Y".equals(test.timeOk)
                           && "N".equals(test.passed)
                           && "HW".equals(test.hwType)
                           && RawRecordConstants.M117.equals(test.course)
                           && "001".equals(test.sect)
                           && Integer.valueOf(1).equals(test.unit)
                           && Integer.valueOf(1).equals(test.objective)
                           && "N".equals(test.hwCoupon)
                           && test.usedDt == null
                           && test.usedSerialNbr == null) {

                    found5 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected hwScore ", test.hwScore);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timeOk ", test.timeOk);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected hwType ", test.hwType);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected sect ", test.sect);
                    Log.warning("Unexpected unit ", test.unit);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected hwCoupon ", test.hwCoupon);
                    Log.warning("Unexpected usedDt ", test.usedDt);
                    Log.warning("Unexpected usedSerialNbr ", test.usedSerialNbr);
                }
            }

            assertTrue(found1, "Sthomework 1 not found");
            assertTrue(found3, "Sthomework 3 not found");
            assertTrue(found4, "Sthomework 4 not found");
            assertTrue(found5, "Sthomework 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework rows afer delete: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM sthomework");
            }

            conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning table: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }
}
