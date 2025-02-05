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
 * Tests for the {@code RawStchallengeLogic} class.
 */
final class TestRawStchallengeLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 1, 3);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 1, 5);

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
            }
            conn.commit();

            final RawStchallenge raw1 = new RawStchallenge("111111111", RawRecordConstants.M117, "117CH", "2021",
                    date1, Integer.valueOf(100), Integer.valueOf(200), "Smith", "Alice", "B",
                    Integer.valueOf(123), Long.valueOf(100100100L), Integer.valueOf(3), "Y", "P");

            final RawStchallenge raw2 = new RawStchallenge("222222222", RawRecordConstants.M118, "118CH", "2021",
                    date1, Integer.valueOf(101), Integer.valueOf(201), "Jones", "Bob", "C",
                    Integer.valueOf(456), Long.valueOf(200200200L), Integer.valueOf(2), "N", "Q");

            final RawStchallenge raw3 = new RawStchallenge("222222222", RawRecordConstants.M124, "124CH", "2021",
                    date2, Integer.valueOf(102), Integer.valueOf(202), "Shafer", "Carol", "D",
                    Integer.valueOf(789), Long.valueOf(300300300L), Integer.valueOf(1), "G", "R");

            assertTrue(RawStchallengeLogic.insert(cache, raw1), "Failed to insert stchallenge");
            assertTrue(RawStchallengeLogic.insert(cache, raw2), "Failed to insert stchallenge");
            assertTrue(RawStchallengeLogic.insert(cache, raw3), "Failed to insert stchallenge");
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
            final List<RawStchallenge> all = RawStchallengeLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawStchallenge test : all) {

                if ("111111111".equals(test.stuId)
                    && RawRecordConstants.M117.equals(test.course)
                    && "117CH".equals(test.version)
                    && "2021".equals(test.academicYr)
                    && date1.equals(test.examDt)
                    && Integer.valueOf(100).equals(test.startTime)
                    && Integer.valueOf(200).equals(test.finishTime)
                    && "Smith".equals(test.lastName)
                    && "Alice".equals(test.firstName)
                    && "B".equals(test.middleInitial)
                    && Integer.valueOf(123).equals(test.seqNbr)
                    && Long.valueOf(100100100L).equals(test.serialNbr)
                    && Integer.valueOf(3).equals(test.score)
                    && "Y".equals(test.passed)
                    && "P".equals(test.howValidated)) {

                    found1 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M118.equals(test.course)
                           && "118CH".equals(test.version)
                           && "2021".equals(test.academicYr)
                           && date1.equals(test.examDt)
                           && Integer.valueOf(101).equals(test.startTime)
                           && Integer.valueOf(201).equals(test.finishTime)
                           && "Jones".equals(test.lastName)
                           && "Bob".equals(test.firstName)
                           && "C".equals(test.middleInitial)
                           && Integer.valueOf(456).equals(test.seqNbr)
                           && Long.valueOf(200200200L).equals(test.serialNbr)
                           && Integer.valueOf(2).equals(test.score)
                           && "N".equals(test.passed)
                           && "Q".equals(test.howValidated)) {

                    found2 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M124.equals(test.course)
                           && "124CH".equals(test.version)
                           && "2021".equals(test.academicYr)
                           && date2.equals(test.examDt)
                           && Integer.valueOf(102).equals(test.startTime)
                           && Integer.valueOf(202).equals(test.finishTime)
                           && "Shafer".equals(test.lastName)
                           && "Carol".equals(test.firstName)
                           && "D".equals(test.middleInitial)
                           && Integer.valueOf(789).equals(test.seqNbr)
                           && Long.valueOf(300300300L).equals(test.serialNbr)
                           && Integer.valueOf(1).equals(test.score)
                           && "G".equals(test.passed)
                           && "R".equals(test.howValidated)) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected academicYr ", test.academicYr);
                    Log.warning("Unexpected examDt ", test.examDt);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected lastName ", test.lastName);
                    Log.warning("Unexpected firstName ", test.firstName);
                    Log.warning("Unexpected middleInitial ", test.middleInitial);
                    Log.warning("Unexpected seqNbr ", test.seqNbr);
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected score ", test.score);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected howValidated ", test.howValidated);
                }
            }

            assertTrue(found1, "Stchallenge 1 not found");
            assertTrue(found2, "Stchallenge 2 not found");
            assertTrue(found3, "Stchallenge 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stchallenge rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawStchallenge> all = RawStchallengeLogic.queryByStudent(cache, "222222222");

            assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawStchallenge test : all) {
                if ("222222222".equals(test.stuId)
                    && RawRecordConstants.M118.equals(test.course)
                    && "118CH".equals(test.version)
                    && "2021".equals(test.academicYr)
                    && date1.equals(test.examDt)
                    && Integer.valueOf(101).equals(test.startTime)
                    && Integer.valueOf(201).equals(test.finishTime)
                    && "Jones".equals(test.lastName)
                    && "Bob".equals(test.firstName)
                    && "C".equals(test.middleInitial)
                    && Integer.valueOf(456).equals(test.seqNbr)
                    && Long.valueOf(200200200L).equals(test.serialNbr)
                    && Integer.valueOf(2).equals(test.score)
                    && "N".equals(test.passed)
                    && "Q".equals(test.howValidated)) {

                    found1 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M124.equals(test.course)
                           && "124CH".equals(test.version)
                           && "2021".equals(test.academicYr)
                           && date2.equals(test.examDt)
                           && Integer.valueOf(102).equals(test.startTime)
                           && Integer.valueOf(202).equals(test.finishTime)
                           && "Shafer".equals(test.lastName)
                           && "Carol".equals(test.firstName)
                           && "D".equals(test.middleInitial)
                           && Integer.valueOf(789).equals(test.seqNbr)
                           && Long.valueOf(300300300L).equals(test.serialNbr)
                           && Integer.valueOf(1).equals(test.score)
                           && "G".equals(test.passed)
                           && "R".equals(test.howValidated)) {

                    found2 = true;
                }
            }

            assertTrue(found1, "Stchallenge 1 not found");
            assertTrue(found2, "Stchallenge 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stchallenge rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudentCourse results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawStchallenge> all = RawStchallengeLogic.queryByStudentCourse(cache, "222222222",
                    RawRecordConstants.M124);

            assertEquals(1, all.size(), "Incorrect record count from queryByStudentCourse");

            boolean found = false;

            for (final RawStchallenge test : all) {
                if ("222222222".equals(test.stuId)
                    && RawRecordConstants.M124.equals(test.course)
                    && "124CH".equals(test.version)
                    && "2021".equals(test.academicYr)
                    && date2.equals(test.examDt)
                    && Integer.valueOf(102).equals(test.startTime)
                    && Integer.valueOf(202).equals(test.finishTime)
                    && "Shafer".equals(test.lastName)
                    && "Carol".equals(test.firstName)
                    && "D".equals(test.middleInitial)
                    && Integer.valueOf(789).equals(test.seqNbr)
                    && Long.valueOf(300300300L).equals(test.serialNbr)
                    && Integer.valueOf(1).equals(test.score)
                    && "G".equals(test.passed)
                    && "R".equals(test.howValidated)) {

                    found = true;
                    break;
                }
            }

            assertTrue(found, "Stchallenge not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stchallenge rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("countLegalAttempts results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final int count1 = RawStchallengeLogic.countLegalAttempts(cache, "111111111", RawRecordConstants.M117);

            assertEquals(1, count1, "Incorrect count from countLegalAttempts (111, M 117)");

            final int count2 = RawStchallengeLogic.countLegalAttempts(cache, "111111111", RawRecordConstants.M118);

            assertEquals(0, count2, "Incorrect count from countLegalAttempts (111, M 118)");

            final int count3 = RawStchallengeLogic.countLegalAttempts(cache, "222222222", RawRecordConstants.M118);

            assertEquals(1, count3, "Incorrect count from countLegalAttempts (222, M 118)");

            final int count4 = RawStchallengeLogic.countLegalAttempts(cache, "222222222", RawRecordConstants.M124);

            assertEquals(0, count4, "Incorrect count from countLegalAttempts (222, M 124)");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while counting legal attempts: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("countLegalAttempts results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final List<List<RawStchallenge>> history = new ArrayList<>(10);

            RawStchallengeLogic.getHistory(cache, history, 10, date3);

            assertEquals(10, history.size(), "Incorrect number of entries in history");

            assertEquals(0, history.get(0).size(), "Incorrect number of entries in history[0]");
            assertEquals(0, history.get(1).size(), "Incorrect number of entries in history[1]");
            assertEquals(0, history.get(2).size(), "Incorrect number of entries in history[2]");
            assertEquals(0, history.get(3).size(), "Incorrect number of entries in history[3]");
            assertEquals(0, history.get(4).size(), "Incorrect number of entries in history[4]");
            assertEquals(0, history.get(5).size(), "Incorrect number of entries in history[5]");
            assertEquals(2, history.get(6).size(), "Incorrect number of entries in history[6]");
            assertEquals(1, history.get(7).size(), "Incorrect number of entries in history[7]");
            assertEquals(0, history.get(8).size(), "Incorrect number of entries in history[8]");
            assertEquals(0, history.get(9).size(), "Incorrect number of entries in history[9]");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while counting legal attempts: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryOnOrAfter results")
    void test0008() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawStchallenge> all = RawStchallengeLogic.queryOnOrAfter(cache, date2);

            assertEquals(1, all.size(), "Incorrect record count from queryOnOrAfter");

            boolean found3 = false;

            for (final RawStchallenge test : all) {

                if ("222222222".equals(test.stuId)
                    && RawRecordConstants.M124.equals(test.course)
                    && "124CH".equals(test.version)
                    && "2021".equals(test.academicYr)
                    && date2.equals(test.examDt)
                    && Integer.valueOf(102).equals(test.startTime)
                    && Integer.valueOf(202).equals(test.finishTime)
                    && "Shafer".equals(test.lastName)
                    && "Carol".equals(test.firstName)
                    && "D".equals(test.middleInitial)
                    && Integer.valueOf(789).equals(test.seqNbr)
                    && Long.valueOf(300300300L).equals(test.serialNbr)
                    && Integer.valueOf(1).equals(test.score)
                    && "G".equals(test.passed)
                    && "R".equals(test.howValidated)) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected academicYr ", test.academicYr);
                    Log.warning("Unexpected examDt ", test.examDt);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected lastName ", test.lastName);
                    Log.warning("Unexpected firstName ", test.firstName);
                    Log.warning("Unexpected middleInitial ", test.middleInitial);
                    Log.warning("Unexpected seqNbr ", test.seqNbr);
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected score ", test.score);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected howValidated ", test.howValidated);
                }
            }

            assertTrue(found3, "Stchallenge 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stchallenge rows on or after date: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0009() {

        final Cache cache = new Cache(profile);

        try {
            final RawStchallenge raw2 = new RawStchallenge("222222222", RawRecordConstants.M118, "118CH", "2021",
                    date1, Integer.valueOf(101), Integer.valueOf(201), "Jones", "Bob", "C",
                    Integer.valueOf(456), Long.valueOf(200200200L), Integer.valueOf(2), "N", "Q");

            final boolean result = RawStchallengeLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawStchallenge> all = RawStchallengeLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final RawStchallenge test : all) {

                if ("111111111".equals(test.stuId)
                    && RawRecordConstants.M117.equals(test.course)
                    && "117CH".equals(test.version)
                    && "2021".equals(test.academicYr)
                    && date1.equals(test.examDt)
                    && Integer.valueOf(100).equals(test.startTime)
                    && Integer.valueOf(200).equals(test.finishTime)
                    && "Smith".equals(test.lastName)
                    && "Alice".equals(test.firstName)
                    && "B".equals(test.middleInitial)
                    && Integer.valueOf(123).equals(test.seqNbr)
                    && Long.valueOf(100100100L).equals(test.serialNbr)
                    && Integer.valueOf(3).equals(test.score)
                    && "Y".equals(test.passed)
                    && "P".equals(test.howValidated)) {

                    found1 = true;

                } else if ("222222222".equals(test.stuId)
                           && RawRecordConstants.M124.equals(test.course)
                           && "124CH".equals(test.version)
                           && "2021".equals(test.academicYr)
                           && date2.equals(test.examDt)
                           && Integer.valueOf(102).equals(test.startTime)
                           && Integer.valueOf(202).equals(test.finishTime)
                           && "Shafer".equals(test.lastName)
                           && "Carol".equals(test.firstName)
                           && "D".equals(test.middleInitial)
                           && Integer.valueOf(789).equals(test.seqNbr)
                           && Long.valueOf(300300300L).equals(test.serialNbr)
                           && Integer.valueOf(1).equals(test.score)
                           && "G".equals(test.passed)
                           && "R".equals(test.howValidated)) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected academicYr ", test.academicYr);
                    Log.warning("Unexpected examDt ", test.examDt);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected lastName ", test.lastName);
                    Log.warning("Unexpected firstName ", test.firstName);
                    Log.warning("Unexpected middleInitial ", test.middleInitial);
                    Log.warning("Unexpected seqNbr ", test.seqNbr);
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected score ", test.score);
                    Log.warning("Unexpected passed ", test.passed);
                    Log.warning("Unexpected howValidated ", test.howValidated);
                }
            }

            assertTrue(found1, "Stchallenge 1 not found");
            assertTrue(found3, "Stchallenge 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting stchallenge: " + ex.getMessage());
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
