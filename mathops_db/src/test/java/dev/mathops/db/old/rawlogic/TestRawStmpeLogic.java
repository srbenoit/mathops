package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawStmpe;
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
 * Tests for the {@code RawStmpeLogic} class.
 */
final class TestRawStmpeLogic {

    /** A date used in test records. */
    private static final LocalDate date12 = LocalDate.of(2020, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date13 = LocalDate.of(2020, 1, 3);

    /** A date used in test records. */
    private static final LocalDate date14 = LocalDate.of(2020, 1, 4);

    /** A date used in test records. */
    private static final LocalDate date15 = LocalDate.of(2020, 1, 5);

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
                stmt.executeUpdate("DELETE FROM stmpe");
            }
            conn.commit();

            final RawStmpe raw1 = new RawStmpe("111111111", "POOOO", "1920", date12,
                    Integer.valueOf(600), Integer.valueOf(700), "Doe", "Jane", "W", Integer.valueOf(1),
                    Long.valueOf(12345L), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(5),
                    Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(2), "Y", "S");

            final RawStmpe raw2 = new RawStmpe("111111111", "PPPPP", "1920", date13,
                    Integer.valueOf(601), Integer.valueOf(701), "Doe", "Jane", "W", Integer.valueOf(2),
                    Long.valueOf(67890L), Integer.valueOf(17), Integer.valueOf(16), Integer.valueOf(15),
                    Integer.valueOf(14), Integer.valueOf(13), Integer.valueOf(12), "Y", "P");

            final RawStmpe raw3 = new RawStmpe("111111111", "PPPPP", "1920", date14,
                    Integer.valueOf(609), Integer.valueOf(709), "Doe", "Jane", "W", Integer.valueOf(21),
                    Long.valueOf(88888L), Integer.valueOf(171), Integer.valueOf(161), Integer.valueOf(151),
                    Integer.valueOf(141), Integer.valueOf(131), Integer.valueOf(121), "1", "P");

            final RawStmpe raw4 = new RawStmpe("222222222", "MPTUN", "2021", date15,
                    Integer.valueOf(602), Integer.valueOf(702), "Public", "John", "Q", Integer.valueOf(3),
                    Long.valueOf(99999L), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(7),
                    Integer.valueOf(6), Integer.valueOf(5),
                    Integer.valueOf(4), "N", "R");

            assertTrue(RawStmpeLogic.insert(cache, raw1), "Failed to insert stmpe 1");
            assertTrue(RawStmpeLogic.insert(cache, raw2), "Failed to insert stmpe 2");
            assertTrue(RawStmpeLogic.insert(cache, raw3), "Failed to insert stmpe 3");
            assertTrue(RawStmpeLogic.insert(cache, raw4), "Failed to insert stmpe 4");
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
            final List<RawStmpe> all = RawStmpeLogic.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawStmpe test : all) {

                if ("111111111".equals(test.stuId)
                    && "POOOO".equals(test.version)
                    && "1920".equals(test.academicYr)
                    && date12.equals(test.examDt)
                    && Integer.valueOf(600).equals(test.startTime)
                    && Integer.valueOf(700).equals(test.finishTime)
                    && "Doe".equals(test.lastName)
                    && "Jane".equals(test.firstName)
                    && "W".equals(test.middleInitial)
                    && Integer.valueOf(1).equals(test.seqNbr)
                    && Long.valueOf(12345L).equals(test.serialNbr)
                    && Integer.valueOf(7).equals(test.stsA)
                    && Integer.valueOf(6).equals(test.sts117)
                    && Integer.valueOf(5).equals(test.sts118)
                    && Integer.valueOf(4).equals(test.sts124)
                    && Integer.valueOf(3).equals(test.sts125)
                    && Integer.valueOf(2).equals(test.sts126)
                    && "Y".equals(test.placed)
                    && "S".equals(test.howValidated)) {

                    found1 = true;

                } else if ("111111111".equals(test.stuId)
                           && "PPPPP".equals(test.version)
                           && "1920".equals(test.academicYr)
                           && date13.equals(test.examDt)
                           && Integer.valueOf(601).equals(test.startTime)
                           && Integer.valueOf(701).equals(test.finishTime)
                           && "Doe".equals(test.lastName)
                           && "Jane".equals(test.firstName)
                           && "W".equals(test.middleInitial)
                           && Integer.valueOf(2).equals(test.seqNbr)
                           && Long.valueOf(67890L).equals(test.serialNbr)
                           && Integer.valueOf(17).equals(test.stsA)
                           && Integer.valueOf(16).equals(test.sts117)
                           && Integer.valueOf(15).equals(test.sts118)
                           && Integer.valueOf(14).equals(test.sts124)
                           && Integer.valueOf(13).equals(test.sts125)
                           && Integer.valueOf(12).equals(test.sts126)
                           && "Y".equals(test.placed)
                           && "P".equals(test.howValidated)) {

                    found2 = true;

                } else if ("111111111".equals(test.stuId)
                           && "PPPPP".equals(test.version)
                           && "1920".equals(test.academicYr)
                           && date14.equals(test.examDt)
                           && Integer.valueOf(609).equals(test.startTime)
                           && Integer.valueOf(709).equals(test.finishTime)
                           && "Doe".equals(test.lastName)
                           && "Jane".equals(test.firstName)
                           && "W".equals(test.middleInitial)
                           && Integer.valueOf(21).equals(test.seqNbr)
                           && Long.valueOf(88888L).equals(test.serialNbr)
                           && Integer.valueOf(171).equals(test.stsA)
                           && Integer.valueOf(161).equals(test.sts117)
                           && Integer.valueOf(151).equals(test.sts118)
                           && Integer.valueOf(141).equals(test.sts124)
                           && Integer.valueOf(131).equals(test.sts125)
                           && Integer.valueOf(121).equals(test.sts126)
                           && "1".equals(test.placed)
                           && "P".equals(test.howValidated)) {

                    found3 = true;

                } else if ("222222222".equals(test.stuId)
                           && "MPTUN".equals(test.version)
                           && "2021".equals(test.academicYr)
                           && date15.equals(test.examDt)
                           && Integer.valueOf(602).equals(test.startTime)
                           && Integer.valueOf(702).equals(test.finishTime)
                           && "Public".equals(test.lastName)
                           && "John".equals(test.firstName)
                           && "Q".equals(test.middleInitial)
                           && Integer.valueOf(3).equals(test.seqNbr)
                           && Long.valueOf(99999L).equals(test.serialNbr)
                           && Integer.valueOf(9).equals(test.stsA)
                           && Integer.valueOf(8).equals(test.sts117)
                           && Integer.valueOf(7).equals(test.sts118)
                           && Integer.valueOf(6).equals(test.sts124)
                           && Integer.valueOf(5).equals(test.sts125)
                           && Integer.valueOf(4).equals(test.sts126)
                           && "N".equals(test.placed)
                           && "R".equals(test.howValidated)) {

                    found4 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
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
                    Log.warning("Unexpected stsA ", test.stsA);
                    Log.warning("Unexpected sts117 ", test.sts117);
                    Log.warning("Unexpected sts118 ", test.sts118);
                    Log.warning("Unexpected sts124 ", test.sts124);
                    Log.warning("Unexpected sts125 ", test.sts125);
                    Log.warning("Unexpected sts126 ", test.sts126);
                    Log.warning("Unexpected placed ", test.placed);
                    Log.warning("Unexpected howValidated ", test.howValidated);
                }
            }

            assertTrue(found1, "Stmpe 1 not found");
            assertTrue(found2, "Stmpe 2 not found");
            assertTrue(found3, "Stmpe 3 not found");
            assertTrue(found4, "Stmpe 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stmpe rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryLegalByStudent results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawStmpe> all = RawStmpeLogic.queryLegalByStudent(cache, "111111111");

            assertEquals(2, all.size(), "Incorrect record count from queryLegalByStudent");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawStmpe test : all) {

                if ("111111111".equals(test.stuId)
                    && "POOOO".equals(test.version)
                    && "1920".equals(test.academicYr)
                    && date12.equals(test.examDt)
                    && Integer.valueOf(600).equals(test.startTime)
                    && Integer.valueOf(700).equals(test.finishTime)
                    && "Doe".equals(test.lastName)
                    && "Jane".equals(test.firstName)
                    && "W".equals(test.middleInitial)
                    && Integer.valueOf(1).equals(test.seqNbr)
                    && Long.valueOf(12345L).equals(test.serialNbr)
                    && Integer.valueOf(7).equals(test.stsA)
                    && Integer.valueOf(6).equals(test.sts117)
                    && Integer.valueOf(5).equals(test.sts118)
                    && Integer.valueOf(4).equals(test.sts124)
                    && Integer.valueOf(3).equals(test.sts125)
                    && Integer.valueOf(2).equals(test.sts126)
                    && "Y".equals(test.placed)
                    && "S".equals(test.howValidated)) {

                    found1 = true;

                } else if ("111111111".equals(test.stuId)
                           && "PPPPP".equals(test.version)
                           && "1920".equals(test.academicYr)
                           && date13.equals(test.examDt)
                           && Integer.valueOf(601).equals(test.startTime)
                           && Integer.valueOf(701).equals(test.finishTime)
                           && "Doe".equals(test.lastName)
                           && "Jane".equals(test.firstName)
                           && "W".equals(test.middleInitial)
                           && Integer.valueOf(2).equals(test.seqNbr)
                           && Long.valueOf(67890L).equals(test.serialNbr)
                           && Integer.valueOf(17).equals(test.stsA)
                           && Integer.valueOf(16).equals(test.sts117)
                           && Integer.valueOf(15).equals(test.sts118)
                           && Integer.valueOf(14).equals(test.sts124)
                           && Integer.valueOf(13).equals(test.sts125)
                           && Integer.valueOf(12).equals(test.sts126)
                           && "Y".equals(test.placed)
                           && "P".equals(test.howValidated)) {

                    found2 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
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
                    Log.warning("Unexpected stsA ", test.stsA);
                    Log.warning("Unexpected sts117 ", test.sts117);
                    Log.warning("Unexpected sts118 ", test.sts118);
                    Log.warning("Unexpected sts124 ", test.sts124);
                    Log.warning("Unexpected sts125 ", test.sts125);
                    Log.warning("Unexpected sts126 ", test.sts126);
                    Log.warning("Unexpected placed ", test.placed);
                    Log.warning("Unexpected howValidated ", test.howValidated);
                }
            }

            assertTrue(found1, "Stmpe 1 not found");
            assertTrue(found2, "Stmpe 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stmpe by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("countLegalAttempts results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final int[] all = RawStmpeLogic.countLegalAttempts(cache, "111111111", "POOOO");

            assertEquals(2, all.length, "Incorrect array size from countLegalAttempts");
            assertEquals(1, all[0], "Incorrect number of unproctored from countLegalAttempts");
            assertEquals(0, all[1], "Incorrect number of proctored from countLegalAttempts");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while counting stmpe attempts: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("countLegalAttempts results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final int[] all = RawStmpeLogic.countLegalAttempts(cache, "111111111", "PPPPP");

            assertEquals(2, all.length, "Incorrect array size from countLegalAttempts");
            assertEquals(0, all[0], "Incorrect number of unproctored from countLegalAttempts");
            assertEquals(1, all[1], "Incorrect number of proctored from countLegalAttempts");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while counting stmpe attempts: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final List<List<RawStmpe>> history = new ArrayList<>(10);
            final LocalDate today = LocalDate.of(2020, 1, 10);

            RawStmpeLogic.getHistory(cache, history, 10, today);

            assertEquals(10, history.size(), "Incorrect result list count from getHistory");

            final List<RawStmpe> jan1 = history.get(0);
            final List<RawStmpe> jan2 = history.get(1);
            final List<RawStmpe> jan3 = history.get(2);
            final List<RawStmpe> jan4 = history.get(3);
            final List<RawStmpe> jan5 = history.get(4);
            final List<RawStmpe> jan6 = history.get(5);
            final List<RawStmpe> jan7 = history.get(6);
            final List<RawStmpe> jan8 = history.get(7);
            final List<RawStmpe> jan9 = history.get(8);
            final List<RawStmpe> jan10 = history.get(9);

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
            fail("Exception while querying all stmpe rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryOnOrAfter results")
    void test0008() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawStmpe> all = RawStmpeLogic.queryOnOrAfter(cache, date13);

            assertEquals(2, all.size(), "Incorrect record count from queryOnOrAfter");

            boolean found2 = false;
            boolean found4 = false;

            for (final RawStmpe test : all) {

                if ("111111111".equals(test.stuId)
                    && "PPPPP".equals(test.version)
                    && "1920".equals(test.academicYr)
                    && date13.equals(test.examDt)
                    && Integer.valueOf(601).equals(test.startTime)
                    && Integer.valueOf(701).equals(test.finishTime)
                    && "Doe".equals(test.lastName)
                    && "Jane".equals(test.firstName)
                    && "W".equals(test.middleInitial)
                    && Integer.valueOf(2).equals(test.seqNbr)
                    && Long.valueOf(67890L).equals(test.serialNbr)
                    && Integer.valueOf(17).equals(test.stsA)
                    && Integer.valueOf(16).equals(test.sts117)
                    && Integer.valueOf(15).equals(test.sts118)
                    && Integer.valueOf(14).equals(test.sts124)
                    && Integer.valueOf(13).equals(test.sts125)
                    && Integer.valueOf(12).equals(test.sts126)
                    && "Y".equals(test.placed)
                    && "P".equals(test.howValidated)) {

                    found2 = true;

                } else if ("222222222".equals(test.stuId)
                           && "MPTUN".equals(test.version)
                           && "2021".equals(test.academicYr)
                           && date15.equals(test.examDt)
                           && Integer.valueOf(602).equals(test.startTime)
                           && Integer.valueOf(702).equals(test.finishTime)
                           && "Public".equals(test.lastName)
                           && "John".equals(test.firstName)
                           && "Q".equals(test.middleInitial)
                           && Integer.valueOf(3).equals(test.seqNbr)
                           && Long.valueOf(99999L).equals(test.serialNbr)
                           && Integer.valueOf(9).equals(test.stsA)
                           && Integer.valueOf(8).equals(test.sts117)
                           && Integer.valueOf(7).equals(test.sts118)
                           && Integer.valueOf(6).equals(test.sts124)
                           && Integer.valueOf(5).equals(test.sts125)
                           && Integer.valueOf(4).equals(test.sts126)
                           && "N".equals(test.placed)
                           && "R".equals(test.howValidated)) {

                    found4 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
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
                    Log.warning("Unexpected stsA ", test.stsA);
                    Log.warning("Unexpected sts117 ", test.sts117);
                    Log.warning("Unexpected sts118 ", test.sts118);
                    Log.warning("Unexpected sts124 ", test.sts124);
                    Log.warning("Unexpected sts125 ", test.sts125);
                    Log.warning("Unexpected sts126 ", test.sts126);
                    Log.warning("Unexpected placed ", test.placed);
                    Log.warning("Unexpected howValidated ", test.howValidated);
                }
            }

            assertTrue(found2, "Stmpe 2 not found");
            assertTrue(found4, "Stmpe 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stmpe rows on or after date: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM stmpe");
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
