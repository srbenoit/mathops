package dev.mathops.db.reclogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbUse;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.cfg.Facet;
import dev.mathops.db.rec.MasteryAttemptQaRec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code MasteryAttemptQaLogic} class.
 */
final class TestMasteryAttemptQaLogic {

    /** A raw test record. */
    private static final MasteryAttemptQaRec RAW1 = new MasteryAttemptQaRec(Integer.valueOf(11111), "EXAM1",
            Integer.valueOf(1), "A");

    /** A raw test record. */
    private static final MasteryAttemptQaRec RAW2 = new MasteryAttemptQaRec(Integer.valueOf(11111), "EXAM1",
            Integer.valueOf(2), "B");

    /** A raw test record. */
    private static final MasteryAttemptQaRec RAW3 = new MasteryAttemptQaRec(Integer.valueOf(11111), "EXAM2",
            Integer.valueOf(1), "C");

    /** A raw test record. */
    private static final MasteryAttemptQaRec RAW4 = new MasteryAttemptQaRec(Integer.valueOf(22222), "EXAM3",
            Integer.valueOf(1), "D");

    /** A raw test record. */
    private static final MasteryAttemptQaRec RAW1UPD = new MasteryAttemptQaRec(Integer.valueOf(11111), "EXAM1",
            Integer.valueOf(1), "Z");

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final MasteryAttemptQaRec r) {

        Log.warning("Unexpected serialNbr ", r.serialNbr);
        Log.warning("Unexpected examId ", r.examId);
        Log.warning("Unexpected questionNbr ", r.questionNbr);
        Log.warning("Unexpected correct ", r.correct);
    }

    /**
     * Tests for the {@code MasteryAttemptQaLogic} class.
     */
    @Nested
    final class Informix {

        /** The Informix database profile. */
        static Profile informixProfile;

        /** The Informix database context. */
        static Login informixLogin;

        /** Initialize the test class. */
        @BeforeAll
        static void initTests() {

            final DatabaseConfig config = DatabaseConfig.getDefault();
            informixProfile = config.getCodeProfile(Contexts.INFORMIX_TEST_PATH);
            if (informixProfile == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_IFXTEST_PROFILE));
            }
            informixLogin = informixProfile.getLogin(ESchema.LEGACY);
            if (informixLogin == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGPRIMARY_CONTEXT));
            }

            // Make sure the Informix connection is accessing the TEST database
            final Facet facet = informixProfile.getFacet(ESchema.LEGACY);
            if (facet.data.use != EDbUse.TEST) {
                throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, facet.data.use));
            }

            final DbConnection conn = informixLogin.checkOutConnection();
            final Cache cache = new Cache(informixProfile);

            try {
                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery("SELECT descr FROM which_db")) {

                    if (rs.next()) {
                        final String which = rs.getString(1);
                        if (which == null || !"TEST".equals(which.trim())) {
                            throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                        }
                    } else {
                        throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                    }
                }

                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM mastery_attempt_qa");
                }
                conn.commit();

                final MasteryAttemptQaLogic logic = MasteryAttemptQaLogic.get(cache);

                assertTrue(logic.insert(cache, RAW1), "Failed to insert Informix mastery_attempt_qa");
                assertTrue(logic.insert(cache, RAW2), "Failed to insert Informix mastery_attempt_qa");
                assertTrue(logic.insert(cache, RAW3), "Failed to insert Informix mastery_attempt_qa");
                assertTrue(logic.insert(cache, RAW4), "Failed to insert Informix mastery_attempt_qa");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Informix 'mastery_attempt_qa' table: " + ex.getMessage());
                throw new IllegalArgumentException(ex);
            } finally {
                informixLogin.checkInConnection(conn);
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryAll results")
        void test0003() {

            final Cache cache = new Cache(informixProfile);
            final MasteryAttemptQaLogic logic = MasteryAttemptQaLogic.get(cache);

            try {
                final List<MasteryAttemptQaRec> all = logic.queryAll(cache);

                assertEquals(4, all.size(), "Incorrect record count from Informix queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final MasteryAttemptQaRec r : all) {
                    if (RAW1.equals(r)) {
                        found1 = true;
                    } else if (RAW2.equals(r)) {
                        found2 = true;
                    } else if (RAW3.equals(r)) {
                        found3 = true;
                    } else if (RAW4.equals(r)) {
                        found4 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "Informix mastery_attempt_qa 1 not found");
                assertTrue(found2, "Informix mastery_attempt_qa 2 not found");
                assertTrue(found3, "Informix mastery_attempt_qa 3 not found");
                assertTrue(found4, "Informix mastery_attempt_qa 4 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'mastery_attempt_qa' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByAttempt results")
        void test0004() {

            final Cache cache = new Cache(informixProfile);
            final MasteryAttemptQaLogic logic = MasteryAttemptQaLogic.get(cache);

            try {
                final List<MasteryAttemptQaRec> all = logic.queryByAttempt(cache, Integer.valueOf(11111), "EXAM1");

                assertEquals(2, all.size(), "Incorrect record count from Informix queryByAttempt");

                boolean found1 = false;
                boolean found2 = false;

                for (final MasteryAttemptQaRec r : all) {
                    if (RAW1.equals(r)) {
                        found1 = true;
                    } else if (RAW2.equals(r)) {
                        found2 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "Informix mastery_attempt_qa 1 not found");
                assertTrue(found2, "Informix mastery_attempt_qa 2 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'mastery_attempt_qa' rows by attempt: "
                     + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("query results")
        void test0005() {

            final Cache cache = new Cache(informixProfile);
            final MasteryAttemptQaLogic logic = MasteryAttemptQaLogic.get(cache);

            try {
                final MasteryAttemptQaRec r = logic.query(cache, Integer.valueOf(11111), "EXAM1",
                        Integer.valueOf(1));

                assertNotNull(r, "query returned null");

                if (!RAW1.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying mastery_attempt_qa by attempt and question: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("updateCorrect results")
        void test0006() {

            final Cache cache = new Cache(informixProfile);
            final MasteryAttemptQaLogic logic = MasteryAttemptQaLogic.get(cache);

            try {
                assertTrue(logic.updateCorrect(cache, RAW1, "Z"), "updateCorrect returned false");

                final MasteryAttemptQaRec r = logic.query(cache, Integer.valueOf(11111), "EXAM1",
                        Integer.valueOf(1));

                assertNotNull(r, "query returned null");

                if (!RAW1UPD.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying mastery_attempt_qa after update: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("delete results")
        void test0007() {

            final Cache cache = new Cache(informixProfile);
            final MasteryAttemptQaLogic logic = MasteryAttemptQaLogic.get(cache);

            try {
                final boolean result = logic.delete(cache, RAW3);
                assertTrue(result, "delete returned false");

                final List<MasteryAttemptQaRec> all = logic.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count after delete");

                boolean found1 = false;
                boolean found2 = false;
                boolean found4 = false;

                for (final MasteryAttemptQaRec r : all) {
                    if (RAW1UPD.equals(r)) {
                        found1 = true;
                    } else if (RAW2.equals(r)) {
                        found2 = true;
                    } else if (RAW4.equals(r)) {
                        found4 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "Informix mastery_attempt_qa 1 not found");
                assertTrue(found2, "Informix mastery_attempt_qa 2 not found");
                assertTrue(found4, "Informix mastery_attempt_qa 4 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting mastery_attempt_qa: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            final DbConnection conn = informixLogin.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM mastery_attempt");
                }

                conn.commit();
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while cleaning tables: " + ex.getMessage());
            } finally {
                informixLogin.checkInConnection(conn);
            }
        }
    }
}
