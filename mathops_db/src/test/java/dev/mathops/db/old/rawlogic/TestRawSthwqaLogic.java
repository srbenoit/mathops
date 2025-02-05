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
import dev.mathops.db.old.rawrecord.RawSthwqa;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawSthwqaLogic} class.
 */
final class TestRawSthwqaLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 1, 5);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2022, 1, 5);

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
                stmt.executeUpdate("DELETE FROM sthwqa");
            }
            conn.commit();

            final RawSthomework raw1 = new RawSthomework(Long.valueOf(1000L), "UUUUU", "111111111",
                    LocalDate.of(2021, 1, 1), Integer.valueOf(2), Integer.valueOf(59), Integer.valueOf(89), "Y",
                    "Y", "HW", RawRecordConstants.M100U, "1", Integer.valueOf(0), Integer.valueOf(0), "Y",
                    LocalDate.of(2021, 2, 2), Long.valueOf(100L));

            final RawSthomework raw5 = new RawSthomework(Long.valueOf(1004L), "1711H", "222222222",
                    date2, Integer.valueOf(1), Integer.valueOf(55), Integer.valueOf(85), "Y",
                    "N", "HW", RawRecordConstants.M117, "001", Integer.valueOf(1), Integer.valueOf(1), "N", null,
                    null);

            final RawSthwqa qa1 = new RawSthwqa(Long.valueOf(1001L), Integer.valueOf(1), Integer.valueOf(1), "1.1",
                    "A", "111111111", "1711H", "Y", date1, Integer.valueOf(77));

            final RawSthwqa qa2 = new RawSthwqa(Long.valueOf(1001L), Integer.valueOf(2), Integer.valueOf(1), "1.2",
                    "B", "111111111", "1711H", "N", date1, Integer.valueOf(78));

            final RawSthwqa qa3 = new RawSthwqa(Long.valueOf(1001L), Integer.valueOf(2), Integer.valueOf(2), "1.3",
                    "C", "111111111", "1711H", "N", date1, Integer.valueOf(79));

            final RawSthwqa qa4 = new RawSthwqa(Long.valueOf(1004L), Integer.valueOf(5), Integer.valueOf(1), "1.1",
                    "X", "222222222", "1711H", "Y", date3, Integer.valueOf(80));

            assertTrue(RawSthomeworkLogic.insert(cache, raw1), "Failed to insert sthomework 1");
            assertTrue(RawSthomeworkLogic.insert(cache, raw5), "Failed to insert sthomework 5");

            assertTrue(RawSthwqaLogic.insert(cache, qa1), "Failed to insert sthwqa 1");
            assertTrue(RawSthwqaLogic.insert(cache, qa2), "Failed to insert sthwqa 2");
            assertTrue(RawSthwqaLogic.insert(cache, qa3), "Failed to insert sthwqa 3");
            assertTrue(RawSthwqaLogic.insert(cache, qa4), "Failed to insert sthwqa 4");
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
            final List<RawSthwqa> all = RawSthwqaLogic.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawSthwqa test : all) {

                if (Long.valueOf(1001L).equals(test.serialNbr)
                    && Integer.valueOf(1).equals(test.questionNbr)
                    && Integer.valueOf(1).equals(test.answerNbr)
                    && "1.1".equals(test.objective)
                    && "A".equals(test.stuAnswer)
                    && "111111111".equals(test.stuId)
                    && "1711H".equals(test.version)
                    && "Y".equals(test.ansCorrect)
                    && date1.equals(test.hwDt)
                    && Integer.valueOf(77).equals(test.finishTime)) {

                    found1 = true;

                } else if (Long.valueOf(1001L).equals(test.serialNbr)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && Integer.valueOf(1).equals(test.answerNbr)
                           && "1.2".equals(test.objective)
                           && "B".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "1711H".equals(test.version)
                           && "N".equals(test.ansCorrect)
                           && date1.equals(test.hwDt)
                           && Integer.valueOf(78).equals(test.finishTime)) {

                    found2 = true;

                } else if (Long.valueOf(1001L).equals(test.serialNbr)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && Integer.valueOf(2).equals(test.answerNbr)
                           && "1.3".equals(test.objective)
                           && "C".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "1711H".equals(test.version)
                           && "N".equals(test.ansCorrect)
                           && date1.equals(test.hwDt)
                           && Integer.valueOf(79).equals(test.finishTime)) {

                    found3 = true;

                } else if (Long.valueOf(1004L).equals(test.serialNbr)
                           && Integer.valueOf(5).equals(test.questionNbr)
                           && Integer.valueOf(1).equals(test.answerNbr)
                           && "1.1".equals(test.objective)
                           && "X".equals(test.stuAnswer)
                           && "222222222".equals(test.stuId)
                           && "1711H".equals(test.version)
                           && "Y".equals(test.ansCorrect)
                           && date3.equals(test.hwDt)
                           && Integer.valueOf(80).equals(test.finishTime)) {

                    found4 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected questionNbr ", test.questionNbr);
                    Log.warning("Unexpected answerNbr ", test.answerNbr);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                }
            }

            assertTrue(found1, "Sthwqa 1 not found");
            assertTrue(found2, "Sthwqa 2 not found");
            assertTrue(found3, "Sthwqa 3 not found");
            assertTrue(found4, "Sthwqa 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthwqa rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSthwqa> all = RawSthwqaLogic.queryByStudent(cache, "111111111");

            assertEquals(3, all.size(), "Incorrect record count from queryByStudent");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawSthwqa test : all) {

                if (Long.valueOf(1001L).equals(test.serialNbr)
                    && Integer.valueOf(1).equals(test.questionNbr)
                    && Integer.valueOf(1).equals(test.answerNbr)
                    && "1.1".equals(test.objective)
                    && "A".equals(test.stuAnswer)
                    && "111111111".equals(test.stuId)
                    && "1711H".equals(test.version)
                    && "Y".equals(test.ansCorrect)
                    && date1.equals(test.hwDt)
                    && Integer.valueOf(77).equals(test.finishTime)) {

                    found1 = true;

                } else if (Long.valueOf(1001L).equals(test.serialNbr)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && Integer.valueOf(1).equals(test.answerNbr)
                           && "1.2".equals(test.objective)
                           && "B".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "1711H".equals(test.version)
                           && "N".equals(test.ansCorrect)
                           && date1.equals(test.hwDt)
                           && Integer.valueOf(78).equals(test.finishTime)) {

                    found2 = true;

                } else if (Long.valueOf(1001L).equals(test.serialNbr)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && Integer.valueOf(2).equals(test.answerNbr)
                           && "1.3".equals(test.objective)
                           && "C".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "1711H".equals(test.version)
                           && "N".equals(test.ansCorrect)
                           && date1.equals(test.hwDt)
                           && Integer.valueOf(79).equals(test.finishTime)) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected questionNbr ", test.questionNbr);
                    Log.warning("Unexpected answerNbr ", test.answerNbr);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                }
            }

            assertTrue(found1, "Sthwqa 1 not found");
            assertTrue(found2, "Sthwqa 2 not found");
            assertTrue(found3, "Sthwqa 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthwqa by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryBySerial results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSthwqa> all = RawSthwqaLogic.queryBySerial(cache, Long.valueOf(1001L));

            assertEquals(3, all.size(), "Incorrect record count from queryBySerial");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawSthwqa test : all) {

                if (Long.valueOf(1001L).equals(test.serialNbr)
                    && Integer.valueOf(1).equals(test.questionNbr)
                    && Integer.valueOf(1).equals(test.answerNbr)
                    && "1.1".equals(test.objective)
                    && "A".equals(test.stuAnswer)
                    && "111111111".equals(test.stuId)
                    && "1711H".equals(test.version)
                    && "Y".equals(test.ansCorrect)
                    && date1.equals(test.hwDt)
                    && Integer.valueOf(77).equals(test.finishTime)) {

                    found1 = true;

                } else if (Long.valueOf(1001L).equals(test.serialNbr)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && Integer.valueOf(1).equals(test.answerNbr)
                           && "1.2".equals(test.objective)
                           && "B".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "1711H".equals(test.version)
                           && "N".equals(test.ansCorrect)
                           && date1.equals(test.hwDt)
                           && Integer.valueOf(78).equals(test.finishTime)) {

                    found2 = true;

                } else if (Long.valueOf(1001L).equals(test.serialNbr)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && Integer.valueOf(2).equals(test.answerNbr)
                           && "1.3".equals(test.objective)
                           && "C".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "1711H".equals(test.version)
                           && "N".equals(test.ansCorrect)
                           && date1.equals(test.hwDt)
                           && Integer.valueOf(79).equals(test.finishTime)) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected questionNbr ", test.questionNbr);
                    Log.warning("Unexpected answerNbr ", test.answerNbr);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                }
            }

            assertTrue(found1, "Sthwqa 1 not found");
            assertTrue(found2, "Sthwqa 2 not found");
            assertTrue(found3, "Sthwqa 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthwqa by serial: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudentCourse results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final RawSthomework attempt = new RawSthomework(Long.valueOf(1004L), "1711H", "222222222",
                    date2, Integer.valueOf(1), Integer.valueOf(55), Integer.valueOf(85), "Y",
                    "N", "HW", RawRecordConstants.M117, "001", Integer.valueOf(1), Integer.valueOf(1), "N", null,
                    null);

            final boolean result = RawSthwqaLogic.deleteAllForAttempt(cache, attempt);

            assertTrue(result, "deleteAllForAttempt returned false");

            final List<RawSthwqa> all = RawSthwqaLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count after deleteAllForAttempt");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawSthwqa test : all) {

                if (Long.valueOf(1001L).equals(test.serialNbr)
                    && Integer.valueOf(1).equals(test.questionNbr)
                    && Integer.valueOf(1).equals(test.answerNbr)
                    && "1.1".equals(test.objective)
                    && "A".equals(test.stuAnswer)
                    && "111111111".equals(test.stuId)
                    && "1711H".equals(test.version)
                    && "Y".equals(test.ansCorrect)
                    && date1.equals(test.hwDt)
                    && Integer.valueOf(77).equals(test.finishTime)) {

                    found1 = true;

                } else if (Long.valueOf(1001L).equals(test.serialNbr)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && Integer.valueOf(1).equals(test.answerNbr)
                           && "1.2".equals(test.objective)
                           && "B".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "1711H".equals(test.version)
                           && "N".equals(test.ansCorrect)
                           && date1.equals(test.hwDt)
                           && Integer.valueOf(78).equals(test.finishTime)) {

                    found2 = true;

                } else if (Long.valueOf(1001L).equals(test.serialNbr)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && Integer.valueOf(2).equals(test.answerNbr)
                           && "1.3".equals(test.objective)
                           && "C".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "1711H".equals(test.version)
                           && "N".equals(test.ansCorrect)
                           && date1.equals(test.hwDt)
                           && Integer.valueOf(79).equals(test.finishTime)) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected questionNbr ", test.questionNbr);
                    Log.warning("Unexpected answerNbr ", test.answerNbr);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                }
            }

            assertTrue(found1, "Sthwqa 1 not found");
            assertTrue(found2, "Sthwqa 2 not found");
            assertTrue(found3, "Sthwqa 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework by student/course: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {

            final RawSthwqa qa2 = new RawSthwqa(Long.valueOf(1001L), Integer.valueOf(2), Integer.valueOf(1), "1.2",
                    "B", "111111111", "1711H", "N", date1, Integer.valueOf(78));

            final boolean result = RawSthwqaLogic.delete(cache, qa2);
            assertTrue(result, "delete returned false");

            final List<RawSthwqa> all = RawSthwqaLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final RawSthwqa test : all) {

                if (Long.valueOf(1001L).equals(test.serialNbr)
                    && Integer.valueOf(1).equals(test.questionNbr)
                    && Integer.valueOf(1).equals(test.answerNbr)
                    && "1.1".equals(test.objective)
                    && "A".equals(test.stuAnswer)
                    && "111111111".equals(test.stuId)
                    && "1711H".equals(test.version)
                    && "Y".equals(test.ansCorrect)
                    && date1.equals(test.hwDt)
                    && Integer.valueOf(77).equals(test.finishTime)) {

                    found1 = true;

                } else if (Long.valueOf(1001L).equals(test.serialNbr)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && Integer.valueOf(2).equals(test.answerNbr)
                           && "1.3".equals(test.objective)
                           && "C".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "1711H".equals(test.version)
                           && "N".equals(test.ansCorrect)
                           && date1.equals(test.hwDt)
                           && Integer.valueOf(79).equals(test.finishTime)) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected questionNbr ", test.questionNbr);
                    Log.warning("Unexpected answerNbr ", test.answerNbr);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                    Log.warning("Unexpected hwDt ", test.hwDt);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                }
            }

            assertTrue(found1, "users 1 not found");
            assertTrue(found3, "users 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting users: " + ex.getMessage());
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
                stmt.executeUpdate("DELETE FROM sthwqa");
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
