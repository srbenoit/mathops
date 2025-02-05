package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStqa;
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
 * Tests for the {@code RawStqaLogic} class.
 */
final class TestRawStqaLogic {

    /** A date used in test records. */
    private static final LocalDate date12 = LocalDate.of(2020, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date23 = LocalDate.of(2021, 2, 3);

    /** A date used in test records. */
    private static final LocalDate date34 = LocalDate.of(2022, 3, 4);

    /** A date used in test records. */
    private static final LocalDate date45 = LocalDate.of(2003, 4, 5);

    /** A date used in test records. */
    private static final LocalDate date56 = LocalDate.of(2004, 5, 6);

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
                stmt.executeUpdate("DELETE FROM stqa");
            }
            conn.commit();

            final RawStqa raw1 = new RawStqa(Long.valueOf(10101010L), Integer.valueOf(11), Integer.valueOf(1),
                    "5.5.1", "Agree", "111111111", "17UE1", "Y", date12, "a", Integer.valueOf(80000));

            final RawStqa raw2 = new RawStqa(Long.valueOf(10101010L), Integer.valueOf(22), Integer.valueOf(2),
                    "5.5.2", "Disagree", "111111111", "17UE1", "N", date23, "b", Integer.valueOf(80001));

            final RawStqa raw3 = new RawStqa(Long.valueOf(20202020L), Integer.valueOf(33), Integer.valueOf(3),
                    "5.5.3", "True", "111111111", "17UE3", "Z", date34, "c", Integer.valueOf(80002));

            final RawStqa raw4 = new RawStqa(Long.valueOf(30303030L), Integer.valueOf(44), Integer.valueOf(4),
                    "5.5.4", "AA", "222222222", "18UE2", "P", date45, "d", Integer.valueOf(80003));

            final RawStqa raw5 = new RawStqa(Long.valueOf(40404040L), Integer.valueOf(55), Integer.valueOf(5),
                    "5.5.5", "BB", "333333333", "18UE3", "Q", date56, "e", Integer.valueOf(80004));

            assertTrue(RawStqaLogic.insert(cache, raw1), "Failed to insert stqa 1");
            assertTrue(RawStqaLogic.insert(cache, raw2), "Failed to insert stqa 2");
            assertTrue(RawStqaLogic.insert(cache, raw3), "Failed to insert stqa 3");
            assertTrue(RawStqaLogic.insert(cache, raw4), "Failed to insert stqa 4");
            assertTrue(RawStqaLogic.insert(cache, raw5), "Failed to insert stqa 5");
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
            final List<RawStqa> all = RawStqaLogic.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawStqa test : all) {
                if (Long.valueOf(10101010L).equals(test.serialNbr)
                    && Integer.valueOf(11).equals(test.questionNbr)
                    && Integer.valueOf(1).equals(test.answerNbr)
                    && "5.5.1".equals(test.objective)
                    && "Agree".equals(test.stuAnswer)
                    && "111111111".equals(test.stuId)
                    && "17UE1".equals(test.version)
                    && "Y".equals(test.ansCorrect)
                    && date12.equals(test.examDt)
                    && "a".equals(test.subtest)
                    && Integer.valueOf(80000).equals(test.finishTime)) {

                    found1 = true;
                } else if (Long.valueOf(10101010L).equals(test.serialNbr)
                           && Integer.valueOf(22).equals(test.questionNbr)
                           && Integer.valueOf(2).equals(test.answerNbr)
                           && "5.5.2".equals(test.objective)
                           && "Disagree".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "17UE1".equals(test.version)
                           && "N".equals(test.ansCorrect)
                           && date23.equals(test.examDt)
                           && "b".equals(test.subtest)
                           && Integer.valueOf(80001).equals(test.finishTime)) {

                    found2 = true;
                } else if (Long.valueOf(20202020L).equals(test.serialNbr)
                           && Integer.valueOf(33).equals(test.questionNbr)
                           && Integer.valueOf(3).equals(test.answerNbr)
                           && "5.5.3".equals(test.objective)
                           && "True".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "17UE3".equals(test.version)
                           && "Z".equals(test.ansCorrect)
                           && date34.equals(test.examDt)
                           && "c".equals(test.subtest)
                           && Integer.valueOf(80002).equals(test.finishTime)) {

                    found3 = true;

                } else if (Long.valueOf(30303030L).equals(test.serialNbr)
                           && Integer.valueOf(44).equals(test.questionNbr)
                           && Integer.valueOf(4).equals(test.answerNbr)
                           && "5.5.4".equals(test.objective)
                           && "AA".equals(test.stuAnswer)
                           && "222222222".equals(test.stuId)
                           && "18UE2".equals(test.version)
                           && "P".equals(test.ansCorrect)
                           && date45.equals(test.examDt)
                           && "d".equals(test.subtest)
                           && Integer.valueOf(80003).equals(test.finishTime)) {

                    found4 = true;

                } else if (Long.valueOf(40404040L).equals(test.serialNbr)
                           && Integer.valueOf(55).equals(test.questionNbr)
                           && Integer.valueOf(5).equals(test.answerNbr)
                           && "5.5.5".equals(test.objective)
                           && "BB".equals(test.stuAnswer)
                           && "333333333".equals(test.stuId)
                           && "18UE3".equals(test.version)
                           && "Q".equals(test.ansCorrect)
                           && date56.equals(test.examDt)
                           && "e".equals(test.subtest)
                           && Integer.valueOf(80004).equals(test.finishTime)) {

                    found5 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected questionNbr ", test.questionNbr);
                    Log.warning("Unexpected answerNbr ", test.answerNbr);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                    Log.warning("Unexpected examDt ", test.examDt);
                    Log.warning("Unexpected subtest ", test.subtest);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                }
            }

            assertTrue(found1, "Stqa 1 not found");
            assertTrue(found2, "Stqa 2 not found");
            assertTrue(found3, "Stqa 3 not found");
            assertTrue(found4, "Stqa 4 not found");
            assertTrue(found5, "Stqa 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stqa rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawStqa> all = RawStqaLogic.queryByStudent(cache, "111111111");

            assertEquals(3, all.size(), "Incorrect record count from queryByStudent");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawStqa test : all) {
                if (Long.valueOf(10101010L).equals(test.serialNbr)
                    && Integer.valueOf(11).equals(test.questionNbr)
                    && Integer.valueOf(1).equals(test.answerNbr)
                    && "5.5.1".equals(test.objective)
                    && "Agree".equals(test.stuAnswer)
                    && "111111111".equals(test.stuId)
                    && "17UE1".equals(test.version)
                    && "Y".equals(test.ansCorrect)
                    && date12.equals(test.examDt)
                    && "a".equals(test.subtest)
                    && Integer.valueOf(80000).equals(test.finishTime)) {

                    found1 = true;
                } else if (Long.valueOf(10101010L).equals(test.serialNbr)
                           && Integer.valueOf(22).equals(test.questionNbr)
                           && Integer.valueOf(2).equals(test.answerNbr)
                           && "5.5.2".equals(test.objective)
                           && "Disagree".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "17UE1".equals(test.version)
                           && "N".equals(test.ansCorrect)
                           && date23.equals(test.examDt)
                           && "b".equals(test.subtest)
                           && Integer.valueOf(80001).equals(test.finishTime)) {

                    found2 = true;
                } else if (Long.valueOf(20202020L).equals(test.serialNbr)
                           && Integer.valueOf(33).equals(test.questionNbr)
                           && Integer.valueOf(3).equals(test.answerNbr)
                           && "5.5.3".equals(test.objective)
                           && "True".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "17UE3".equals(test.version)
                           && "Z".equals(test.ansCorrect)
                           && date34.equals(test.examDt)
                           && "c".equals(test.subtest)
                           && Integer.valueOf(80002).equals(test.finishTime)) {

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
                    Log.warning("Unexpected examDt ", test.examDt);
                    Log.warning("Unexpected subtest ", test.subtest);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                }
            }

            assertTrue(found1, "Stqa 1 not found");
            assertTrue(found2, "Stqa 2 not found");
            assertTrue(found3, "Stqa 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stqa rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryBySerial results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawStqa> all = RawStqaLogic.queryBySerial(cache, Long.valueOf(10101010L));

            assertEquals(2, all.size(), "Incorrect record count from queryBySerial");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawStqa test : all) {
                if (Long.valueOf(10101010L).equals(test.serialNbr)
                    && Integer.valueOf(11).equals(test.questionNbr)
                    && Integer.valueOf(1).equals(test.answerNbr)
                    && "5.5.1".equals(test.objective)
                    && "Agree".equals(test.stuAnswer)
                    && "111111111".equals(test.stuId)
                    && "17UE1".equals(test.version)
                    && "Y".equals(test.ansCorrect)
                    && date12.equals(test.examDt)
                    && "a".equals(test.subtest)
                    && Integer.valueOf(80000).equals(test.finishTime)) {

                    found1 = true;
                } else if (Long.valueOf(10101010L).equals(test.serialNbr)
                           && Integer.valueOf(22).equals(test.questionNbr)
                           && Integer.valueOf(2).equals(test.answerNbr)
                           && "5.5.2".equals(test.objective)
                           && "Disagree".equals(test.stuAnswer)
                           && "111111111".equals(test.stuId)
                           && "17UE1".equals(test.version)
                           && "N".equals(test.ansCorrect)
                           && date23.equals(test.examDt)
                           && "b".equals(test.subtest)
                           && Integer.valueOf(80001).equals(test.finishTime)) {

                    found2 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected questionNbr ", test.questionNbr);
                    Log.warning("Unexpected answerNbr ", test.answerNbr);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                    Log.warning("Unexpected examDt ", test.examDt);
                    Log.warning("Unexpected subtest ", test.subtest);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                }
            }

            assertTrue(found1, "Stqa 1 not found");
            assertTrue(found2, "Stqa 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stqa rows by serial number: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateAnsCorrect results")
    void test0006() {

        final RawStqa raw5 = new RawStqa(Long.valueOf(40404040L), Integer.valueOf(55), Integer.valueOf(5),
                "5.5.5", "BB", "333333333", "18UE3", "Q", date56, "e", Integer.valueOf(80004));

        final Cache cache = new Cache(profile);

        try {
            final boolean result = RawStqaLogic.updateAnsCorrect(cache, raw5, "A");

            assertTrue(result, "updateAnsCorrect returned false");

            final List<RawStqa> all = RawStqaLogic.queryBySerial(cache, Long.valueOf(40404040L));

            assertEquals(1, all.size(), "Incorrect record count from queryBySerial");

            boolean found5 = false;

            for (final RawStqa test : all) {
                if (Long.valueOf(40404040L).equals(test.serialNbr)
                    && Integer.valueOf(55).equals(test.questionNbr)
                    && Integer.valueOf(5).equals(test.answerNbr)
                    && "5.5.5".equals(test.objective)
                    && "BB".equals(test.stuAnswer)
                    && "333333333".equals(test.stuId)
                    && "18UE3".equals(test.version)
                    && "A".equals(test.ansCorrect)
                    && date56.equals(test.examDt)
                    && "e".equals(test.subtest)
                    && Integer.valueOf(80004).equals(test.finishTime)) {

                    found5 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected questionNbr ", test.questionNbr);
                    Log.warning("Unexpected answerNbr ", test.answerNbr);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                    Log.warning("Unexpected examDt ", test.examDt);
                    Log.warning("Unexpected subtest ", test.subtest);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                }
            }

            assertTrue(found5, "Stqa 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateAnsCorrect: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("deleteAllForAttempt results")
    void test0007() {

        final RawStexam stexam = new RawStexam(Long.valueOf(10101010L), "17UE1", "111111111", date12,
                Integer.valueOf(1), Integer.valueOf(8), Integer.valueOf(10000), Integer.valueOf(12000), "Y", "N",
                null, "M 117", Integer.valueOf(1), "UE", "N", "TC", null);

        final Cache cache = new Cache(profile);

        try {
            final boolean result = RawStqaLogic.deleteAllForAttempt(cache, stexam);

            assertTrue(result, "deleteAllForAttempt returned false");

            final List<RawStqa> all = RawStqaLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll after deleteAllForAttempt");

            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawStqa test : all) {
                if (Long.valueOf(20202020L).equals(test.serialNbr)
                    && Integer.valueOf(33).equals(test.questionNbr)
                    && Integer.valueOf(3).equals(test.answerNbr)
                    && "5.5.3".equals(test.objective)
                    && "True".equals(test.stuAnswer)
                    && "111111111".equals(test.stuId)
                    && "17UE3".equals(test.version)
                    && "Z".equals(test.ansCorrect)
                    && date34.equals(test.examDt)
                    && "c".equals(test.subtest)
                    && Integer.valueOf(80002).equals(test.finishTime)) {

                    found3 = true;

                } else if (Long.valueOf(30303030L).equals(test.serialNbr)
                           && Integer.valueOf(44).equals(test.questionNbr)
                           && Integer.valueOf(4).equals(test.answerNbr)
                           && "5.5.4".equals(test.objective)
                           && "AA".equals(test.stuAnswer)
                           && "222222222".equals(test.stuId)
                           && "18UE2".equals(test.version)
                           && "P".equals(test.ansCorrect)
                           && date45.equals(test.examDt)
                           && "d".equals(test.subtest)
                           && Integer.valueOf(80003).equals(test.finishTime)) {

                    found4 = true;

                } else if (Long.valueOf(40404040L).equals(test.serialNbr)
                           && Integer.valueOf(55).equals(test.questionNbr)
                           && Integer.valueOf(5).equals(test.answerNbr)
                           && "5.5.5".equals(test.objective)
                           && "BB".equals(test.stuAnswer)
                           && "333333333".equals(test.stuId)
                           && "18UE3".equals(test.version)
                           && "A".equals(test.ansCorrect)
                           && date56.equals(test.examDt)
                           && "e".equals(test.subtest)
                           && Integer.valueOf(80004).equals(test.finishTime)) {

                    found5 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected questionNbr ", test.questionNbr);
                    Log.warning("Unexpected answerNbr ", test.answerNbr);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                    Log.warning("Unexpected examDt ", test.examDt);
                    Log.warning("Unexpected subtest ", test.subtest);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                }
            }

            assertTrue(found3, "Stqa 3 not found");
            assertTrue(found4, "Stqa 4 not found");
            assertTrue(found5, "Stqa 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stqa rows after deleteAllForAttempt: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0008() {

        final RawStqa raw3 = new RawStqa(Long.valueOf(20202020L), Integer.valueOf(33), Integer.valueOf(3),
                "5.5.3", "True", "111111111", "17UE3", "Z", date34, "c", Integer.valueOf(80002));

        final Cache cache = new Cache(profile);

        try {
            final boolean result = RawStqaLogic.delete(cache, raw3);

            assertTrue(result, "delete returned false");

            final List<RawStqa> all = RawStqaLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found4 = false;
            boolean found5 = false;

            for (final RawStqa test : all) {
                if (Long.valueOf(30303030L).equals(test.serialNbr)
                    && Integer.valueOf(44).equals(test.questionNbr)
                    && Integer.valueOf(4).equals(test.answerNbr)
                    && "5.5.4".equals(test.objective)
                    && "AA".equals(test.stuAnswer)
                    && "222222222".equals(test.stuId)
                    && "18UE2".equals(test.version)
                    && "P".equals(test.ansCorrect)
                    && date45.equals(test.examDt)
                    && "d".equals(test.subtest)
                    && Integer.valueOf(80003).equals(test.finishTime)) {

                    found4 = true;

                } else if (Long.valueOf(40404040L).equals(test.serialNbr)
                           && Integer.valueOf(55).equals(test.questionNbr)
                           && Integer.valueOf(5).equals(test.answerNbr)
                           && "5.5.5".equals(test.objective)
                           && "BB".equals(test.stuAnswer)
                           && "333333333".equals(test.stuId)
                           && "18UE3".equals(test.version)
                           && "A".equals(test.ansCorrect)
                           && date56.equals(test.examDt)
                           && "e".equals(test.subtest)
                           && Integer.valueOf(80004).equals(test.finishTime)) {

                    found5 = true;

                } else {
                    Log.warning("Unexpected serialNbr ", test.serialNbr);
                    Log.warning("Unexpected questionNbr ", test.questionNbr);
                    Log.warning("Unexpected answerNbr ", test.answerNbr);
                    Log.warning("Unexpected objective ", test.objective);
                    Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                    Log.warning("Unexpected examDt ", test.examDt);
                    Log.warning("Unexpected subtest ", test.subtest);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                }
            }

            assertTrue(found4, "Stqa 4 not found");
            assertTrue(found5, "Stqa 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stqa rows after delete: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM stqa");
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
