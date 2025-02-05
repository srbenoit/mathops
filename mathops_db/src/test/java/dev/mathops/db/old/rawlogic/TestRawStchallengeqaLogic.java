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
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStchallengeqa;
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
 * Tests for the {@code RawStchallengeqaLogic} class.
 */
final class TestRawStchallengeqaLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 5, 6);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2019, 12, 31);

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
                stmt.executeUpdate("DELETE FROM stchallenge");
                stmt.executeUpdate("DELETE FROM stchallengeqa");
            }
            conn.commit();

            final RawStchallenge raw1 = new RawStchallenge("111111111", RawRecordConstants.M117, "117CH", "2021",
                    date1, Integer.valueOf(100), Integer.valueOf(200), "Smith", "Alice", "B",
                    Integer.valueOf(123), Long.valueOf(100100100L), Integer.valueOf(3), "Y", "P");

            final RawStchallengeqa qa11 = new RawStchallengeqa("111111111", RawRecordConstants.M117, "117CH",
                    date1, Integer.valueOf(200), Integer.valueOf(1), "ANS1", "Y");

            final RawStchallengeqa qa12 = new RawStchallengeqa("111111111", RawRecordConstants.M117, "117CH",
                    date1, Integer.valueOf(200), Integer.valueOf(2), "ANS2", "Y");

            final RawStchallengeqa qa13 = new RawStchallengeqa("111111111", RawRecordConstants.M117, "117CH",
                    date1, Integer.valueOf(200), Integer.valueOf(3), "ANS3", "Y");

            final RawStchallenge raw2 = new RawStchallenge("222222222", RawRecordConstants.M118, "118CH", "2021",
                    date1, Integer.valueOf(101), Integer.valueOf(201), "Jones", "Bob", "C",
                    Integer.valueOf(456), Long.valueOf(200200200L), Integer.valueOf(2), "N", "Q");

            final RawStchallengeqa qa21 = new RawStchallengeqa("222222222", RawRecordConstants.M118, "118CH",
                    date2, Integer.valueOf(201), Integer.valueOf(1), "A1", "Y");

            final RawStchallengeqa qa22 = new RawStchallengeqa("222222222", RawRecordConstants.M118, "118CH",
                    date2, Integer.valueOf(201), Integer.valueOf(2), "A2", "Y");

            final RawStchallengeqa qa23 = new RawStchallengeqa("222222222", RawRecordConstants.M118, "118CH",
                    date2, Integer.valueOf(201), Integer.valueOf(3), "A3", "N");

            final RawStchallenge raw3 = new RawStchallenge("222222222", RawRecordConstants.M124, "124CH", "2021",
                    date3, Integer.valueOf(102), Integer.valueOf(202), "Shafer", "Carol", "D",
                    Integer.valueOf(789), Long.valueOf(300300300L), Integer.valueOf(1), "G", "R");

            final RawStchallengeqa qa31 = new RawStchallengeqa("222222222", RawRecordConstants.M124, "124CH",
                    date3, Integer.valueOf(202), Integer.valueOf(1), "X1", "Y");

            final RawStchallengeqa qa32 = new RawStchallengeqa("222222222", RawRecordConstants.M124, "124CH",
                    date3, Integer.valueOf(202), Integer.valueOf(2), "X2", "G");

            final RawStchallengeqa qa33 = new RawStchallengeqa("222222222", RawRecordConstants.M124, "124CH",
                    date3, Integer.valueOf(202), Integer.valueOf(3), "X3", "N");

            assertTrue(RawStchallengeLogic.insert(cache, raw1), "Failed to insert stchallenge");
            assertTrue(RawStchallengeLogic.insert(cache, raw2), "Failed to insert stchallenge");
            assertTrue(RawStchallengeLogic.insert(cache, raw3), "Failed to insert stchallenge");
            assertTrue(RawStchallengeqaLogic.insert(cache, qa11), "Failed to insert stchallengeqa");
            assertTrue(RawStchallengeqaLogic.insert(cache, qa12), "Failed to insert stchallengeqa");
            assertTrue(RawStchallengeqaLogic.insert(cache, qa13), "Failed to insert stchallengeqa");
            assertTrue(RawStchallengeqaLogic.insert(cache, qa21), "Failed to insert stchallengeqa");
            assertTrue(RawStchallengeqaLogic.insert(cache, qa22), "Failed to insert stchallengeqa");
            assertTrue(RawStchallengeqaLogic.insert(cache, qa23), "Failed to insert stchallengeqa");
            assertTrue(RawStchallengeqaLogic.insert(cache, qa31), "Failed to insert stchallengeqa");
            assertTrue(RawStchallengeqaLogic.insert(cache, qa32), "Failed to insert stchallengeqa");
            assertTrue(RawStchallengeqaLogic.insert(cache, qa33), "Failed to insert stchallengeqa");
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
            final List<RawStchallengeqa> all = RawStchallengeqaLogic.queryAll(cache);

            assertEquals(9, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;
            boolean found6 = false;
            boolean found7 = false;
            boolean found8 = false;
            boolean found9 = false;

            for (final RawStchallengeqa test : all) {

                if ("111111111".equals(test.stuId)
                    && RawRecordConstants.M117.equals(test.course)
                    && "117CH".equals(test.version)
                    && date1.equals(test.examDt)
                    && Integer.valueOf(200).equals(test.finishTime)
                    && Integer.valueOf(1).equals(test.questionNbr)
                    && "ANS1".equals(test.stuAnswer)
                    && "Y".equals(test.ansCorrect)) {

                    found1 = true;

                } else if ("111111111".equals(test.stuId)
                           && RawRecordConstants.M117.equals(test.course)
                           && "117CH".equals(test.version)
                           && date1.equals(test.examDt)
                           && Integer.valueOf(200).equals(test.finishTime)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && "ANS2".equals(test.stuAnswer)
                           && "Y".equals(test.ansCorrect)) {

                    found2 = true;

                } else if ("111111111".equals(test.stuId)
                           && RawRecordConstants.M117.equals(test.course)
                           && "117CH".equals(test.version)
                           && date1.equals(test.examDt)
                           && Integer.valueOf(200).equals(test.finishTime)
                           && Integer.valueOf(3).equals(test.questionNbr)
                           && "ANS3".equals(test.stuAnswer)
                           && "Y".equals(test.ansCorrect)) {

                    found3 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M118.equals(test.course)
                           && "118CH".equals(test.version)
                           && date2.equals(test.examDt)
                           && Integer.valueOf(201).equals(test.finishTime)
                           && Integer.valueOf(1).equals(test.questionNbr)
                           && "A1".equals(test.stuAnswer)
                           && "Y".equals(test.ansCorrect)) {

                    found4 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M118.equals(test.course)
                           && "118CH".equals(test.version)
                           && date2.equals(test.examDt)
                           && Integer.valueOf(201).equals(test.finishTime)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && "A2".equals(test.stuAnswer)
                           && "Y".equals(test.ansCorrect)) {

                    found5 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M118.equals(test.course)
                           && "118CH".equals(test.version)
                           && date2.equals(test.examDt)
                           && Integer.valueOf(201).equals(test.finishTime)
                           && Integer.valueOf(3).equals(test.questionNbr)
                           && "A3".equals(test.stuAnswer)
                           && "N".equals(test.ansCorrect)) {

                    found6 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M124.equals(test.course)
                           && "124CH".equals(test.version)
                           && date3.equals(test.examDt)
                           && Integer.valueOf(202).equals(test.finishTime)
                           && Integer.valueOf(1).equals(test.questionNbr)
                           && "X1".equals(test.stuAnswer)
                           && "Y".equals(test.ansCorrect)) {

                    found7 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M124.equals(test.course)
                           && "124CH".equals(test.version)
                           && date3.equals(test.examDt)
                           && Integer.valueOf(202).equals(test.finishTime)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && "X2".equals(test.stuAnswer)
                           && "G".equals(test.ansCorrect)) {

                    found8 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M124.equals(test.course)
                           && "124CH".equals(test.version)
                           && date3.equals(test.examDt)
                           && Integer.valueOf(202).equals(test.finishTime)
                           && Integer.valueOf(3).equals(test.questionNbr)
                           && "X3".equals(test.stuAnswer)
                           && "N".equals(test.ansCorrect)) {

                    found9 = true;

                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected examDt ", test.examDt);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected questionNbr ", test.questionNbr);
                    Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                    Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                }
            }

            assertTrue(found1, "Stchallengeqa 1 not found");
            assertTrue(found2, "Stchallengeqa 2 not found");
            assertTrue(found3, "Stchallengeqa 3 not found");
            assertTrue(found4, "Stchallengeqa 4 not found");
            assertTrue(found5, "Stchallengeqa 5 not found");
            assertTrue(found6, "Stchallengeqa 6 not found");
            assertTrue(found7, "Stchallengeqa 7 not found");
            assertTrue(found8, "Stchallengeqa 8 not found");
            assertTrue(found9, "Stchallengeqa 9 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stchallengeqa rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("deleteAllForAttempt results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final RawStchallenge raw3 = new RawStchallenge("222222222", RawRecordConstants.M124, "124CH", "2021",
                    date3, Integer.valueOf(102), Integer.valueOf(202), "Shafer", "Carol", "D",
                    Integer.valueOf(789), Long.valueOf(300300300L), Integer.valueOf(1), "G", "R");

            final boolean result = RawStchallengeqaLogic.deleteAllForAttempt(cache, raw3);

            assertTrue(result, "deleteAllForAttempt returned false");

            final List<RawStchallengeqa> all = RawStchallengeqaLogic.queryAll(cache);

            assertEquals(6, all.size(), "Incorrect record count from queryAll after deleteAllForAttempt");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;
            boolean found6 = false;

            for (final RawStchallengeqa test : all) {

                if ("111111111".equals(test.stuId)
                    && RawRecordConstants.M117.equals(test.course)
                    && "117CH".equals(test.version)
                    && date1.equals(test.examDt)
                    && Integer.valueOf(200).equals(test.finishTime)
                    && Integer.valueOf(1).equals(test.questionNbr)
                    && "ANS1".equals(test.stuAnswer)
                    && "Y".equals(test.ansCorrect)) {

                    found1 = true;

                } else if ("111111111".equals(test.stuId)
                           && RawRecordConstants.M117.equals(test.course)
                           && "117CH".equals(test.version)
                           && date1.equals(test.examDt)
                           && Integer.valueOf(200).equals(test.finishTime)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && "ANS2".equals(test.stuAnswer)
                           && "Y".equals(test.ansCorrect)) {

                    found2 = true;

                } else if ("111111111".equals(test.stuId)
                           && RawRecordConstants.M117.equals(test.course)
                           && "117CH".equals(test.version)
                           && date1.equals(test.examDt)
                           && Integer.valueOf(200).equals(test.finishTime)
                           && Integer.valueOf(3).equals(test.questionNbr)
                           && "ANS3".equals(test.stuAnswer)
                           && "Y".equals(test.ansCorrect)) {

                    found3 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M118.equals(test.course)
                           && "118CH".equals(test.version)
                           && date2.equals(test.examDt)
                           && Integer.valueOf(201).equals(test.finishTime)
                           && Integer.valueOf(1).equals(test.questionNbr)
                           && "A1".equals(test.stuAnswer)
                           && "Y".equals(test.ansCorrect)) {

                    found4 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M118.equals(test.course)
                           && "118CH".equals(test.version)
                           && date2.equals(test.examDt)
                           && Integer.valueOf(201).equals(test.finishTime)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && "A2".equals(test.stuAnswer)
                           && "Y".equals(test.ansCorrect)) {

                    found5 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M118.equals(test.course)
                           && "118CH".equals(test.version)
                           && date2.equals(test.examDt)
                           && Integer.valueOf(201).equals(test.finishTime)
                           && Integer.valueOf(3).equals(test.questionNbr)
                           && "A3".equals(test.stuAnswer)
                           && "N".equals(test.ansCorrect)) {

                    found6 = true;

                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected examDt ", test.examDt);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected questionNbr ", test.questionNbr);
                    Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                    Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                }
            }

            assertTrue(found1, "Stchallengeqa 1 not found");
            assertTrue(found2, "Stchallengeqa 2 not found");
            assertTrue(found3, "Stchallengeqa 3 not found");
            assertTrue(found4, "Stchallengeqa 4 not found");
            assertTrue(found5, "Stchallengeqa 5 not found");
            assertTrue(found6, "Stchallengeqa 6 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stchallengeqa rows after deleteAllForAttempt: "
                 + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final RawStchallengeqa qa23 = new RawStchallengeqa("222222222", RawRecordConstants.M118, "118CH",
                    date2, Integer.valueOf(201), Integer.valueOf(3), "A3", "N");

            final boolean result = RawStchallengeqaLogic.delete(cache, qa23);

            assertTrue(result, "delete returned false");

            final List<RawStchallengeqa> all = RawStchallengeqaLogic.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawStchallengeqa test : all) {

                if ("111111111".equals(test.stuId)
                    && RawRecordConstants.M117.equals(test.course)
                    && "117CH".equals(test.version)
                    && date1.equals(test.examDt)
                    && Integer.valueOf(200).equals(test.finishTime)
                    && Integer.valueOf(1).equals(test.questionNbr)
                    && "ANS1".equals(test.stuAnswer)
                    && "Y".equals(test.ansCorrect)) {

                    found1 = true;

                } else if ("111111111".equals(test.stuId)
                           && RawRecordConstants.M117.equals(test.course)
                           && "117CH".equals(test.version)
                           && date1.equals(test.examDt)
                           && Integer.valueOf(200).equals(test.finishTime)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && "ANS2".equals(test.stuAnswer)
                           && "Y".equals(test.ansCorrect)) {

                    found2 = true;

                } else if ("111111111".equals(test.stuId)
                           && RawRecordConstants.M117.equals(test.course)
                           && "117CH".equals(test.version)
                           && date1.equals(test.examDt)
                           && Integer.valueOf(200).equals(test.finishTime)
                           && Integer.valueOf(3).equals(test.questionNbr)
                           && "ANS3".equals(test.stuAnswer)
                           && "Y".equals(test.ansCorrect)) {

                    found3 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M118.equals(test.course)
                           && "118CH".equals(test.version)
                           && date2.equals(test.examDt)
                           && Integer.valueOf(201).equals(test.finishTime)
                           && Integer.valueOf(1).equals(test.questionNbr)
                           && "A1".equals(test.stuAnswer)
                           && "Y".equals(test.ansCorrect)) {

                    found4 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M118.equals(test.course)
                           && "118CH".equals(test.version)
                           && date2.equals(test.examDt)
                           && Integer.valueOf(201).equals(test.finishTime)
                           && Integer.valueOf(2).equals(test.questionNbr)
                           && "A2".equals(test.stuAnswer)
                           && "Y".equals(test.ansCorrect)) {

                    found5 = true;

                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected examDt ", test.examDt);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected questionNbr ", test.questionNbr);
                    Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                    Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                }
            }

            assertTrue(found1, "Stchallengeqa 1 not found");
            assertTrue(found2, "Stchallengeqa 2 not found");
            assertTrue(found3, "Stchallengeqa 3 not found");
            assertTrue(found4, "Stchallengeqa 4 not found");
            assertTrue(found5, "Stchallengeqa 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stchallengeqa rows after delete: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM stchallenge");
                stmt.executeUpdate("DELETE FROM stchallengeqa");
            }

            conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }
}
