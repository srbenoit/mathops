package dev.mathops.db.old.reclogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.logic.DbConnection;
import dev.mathops.db.logic.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.EDbUse;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rec.MasteryAttemptQaRec;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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
        public static DbProfile informixProfile;

        /** The Informix database context. */
        public static DbContext informixCtx;

        /** Initialize the test class. */
        @BeforeAll
        static void initTests() {

            informixProfile = ContextMap.getDefaultInstance().getCodeProfile(Contexts.INFORMIX_TEST_PATH);
            if (informixProfile == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_IFXTEST_PROFILE));
            }

            informixCtx = informixProfile.getDbContext(ESchemaUse.PRIMARY);
            if (informixCtx == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_IFXPRIMARY_CONTEXT));
            }

            // Make sure the Informix connection is accessing the TEST database
            try {
                final DbConnection conn = informixCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement();
                         final ResultSet rs = stmt.executeQuery("SELECT descr FROM which_db")) {

                        if (rs.next()) {
                            final String which = rs.getString(1);
                            if (which == null || !"TEST".equals(which.trim())) {
                                throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST,
                                        which));
                            }
                        } else {
                            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                        }
                    }
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                throw new IllegalArgumentException(ex);
            }

            try {
                final DbConnection conn = informixCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM mastery_attempt_qa");
                    }
                    conn.commit();

                    final Cache cache = new Cache(informixProfile, conn);

                    assertTrue(MasteryAttemptQaLogic.INFORMIX.insert(cache, RAW1),
                            "Failed to insert Informix mastery_attempt_qa");

                    assertTrue(MasteryAttemptQaLogic.INFORMIX.insert(cache, RAW2),
                            "Failed to insert Informix mastery_attempt_qa");

                    assertTrue(MasteryAttemptQaLogic.INFORMIX.insert(cache, RAW3),
                            "Failed to insert Informix mastery_attempt_qa");

                    assertTrue(MasteryAttemptQaLogic.INFORMIX.insert(cache, RAW4),
                            "Failed to insert Informix mastery_attempt_qa");
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Informix 'mastery_attempt_qa' table: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryAll results")
        void test0003() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<MasteryAttemptQaRec> all = MasteryAttemptQaLogic.INFORMIX.queryAll(cache);

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

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'mastery_attempt_qa' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByAttempt results")
        void test0004() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<MasteryAttemptQaRec> all =
                            MasteryAttemptQaLogic.INFORMIX.queryByAttempt(cache, Integer.valueOf(11111), "EXAM1");

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

                } finally {
                    informixCtx.checkInConnection(conn);
                }
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

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final MasteryAttemptQaRec r = MasteryAttemptQaLogic.INFORMIX.query(cache,
                            Integer.valueOf(11111), "EXAM1", Integer.valueOf(1));

                    assertNotNull(r, "query returned null");

                    if (!RAW1.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    informixCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    assertTrue(MasteryAttemptQaLogic.INFORMIX.updateCorrect(cache, RAW1, "Z"),
                            "updateCorrect returned false");

                    final MasteryAttemptQaRec r = MasteryAttemptQaLogic.INFORMIX.query(cache,
                            Integer.valueOf(11111), "EXAM1", Integer.valueOf(1));

                    assertNotNull(r, "query returned null");

                    if (!RAW1UPD.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    informixCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final boolean result = MasteryAttemptQaLogic.INFORMIX.delete(cache, RAW3);
                    assertTrue(result, "delete returned false");

                    final List<MasteryAttemptQaRec> all = MasteryAttemptQaLogic.INFORMIX.queryAll(cache);

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

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting mastery_attempt_qa: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM mastery_attempt");
                    }

                    conn.commit();

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while cleaning tables: " + ex.getMessage());
            }
        }
    }

    /**
     * Tests for the {@code MasteryAttemptQaLogic} class.
     */
    @Nested
    final class Postgres {

        /** The PostgreSQL database profile. */
        public static DbProfile postgresProfile;

        /** The PostgreSQL database context. */
        public static DbContext postgresCtx;

        /** Initialize the test class. */
        @BeforeAll
        static void initTests() {

            postgresProfile = ContextMap.getDefaultInstance().getCodeProfile(Contexts.POSTGRES_TEST_PATH);
            if (postgresProfile == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGTEST_PROFILE));
            }

            postgresCtx = postgresProfile.getDbContext(ESchemaUse.PRIMARY);
            if (postgresCtx == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGPRIMARY_CONTEXT));
            }

            // Make sure the PostgreSQL connection is using a TEST schema
            if (postgresCtx.loginConfig.db.use != EDbUse.TEST) {
                throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST,
                        postgresCtx.loginConfig.db.use));
            }
            try {
                final DbConnection conn = postgresCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM term_t.mastery_attempt_qa");
                    }
                    conn.commit();

                    final Cache cache = new Cache(postgresProfile, conn);

                    assertTrue(MasteryAttemptQaLogic.POSTGRES.insert(cache, RAW1),
                            "Failed to insert Postgres mastery_attempt_qa");

                    assertTrue(MasteryAttemptQaLogic.POSTGRES.insert(cache, RAW2),
                            "Failed to insert Postgres mastery_attempt_qa");

                    assertTrue(MasteryAttemptQaLogic.POSTGRES.insert(cache, RAW3),
                            "Failed to insert Postgres mastery_attempt_qa");

                    assertTrue(MasteryAttemptQaLogic.POSTGRES.insert(cache, RAW4),
                            "Failed to insert Postgres mastery_attempt_qa");
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Postgres 'term_t.mastery_attempt_qa' table: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres queryAll results")
        void test0003() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<MasteryAttemptQaRec> all = MasteryAttemptQaLogic.POSTGRES.queryAll(cache);

                    assertEquals(4, all.size(), "Incorrect record count from Postgres queryAll");

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

                    assertTrue(found1, "Postgres mastery_attempt_qa 1 not found");
                    assertTrue(found2, "Postgres mastery_attempt_qa 2 not found");
                    assertTrue(found3, "Postgres mastery_attempt_qa 3 not found");
                    assertTrue(found4, "Postgres mastery_attempt_qa 4 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Postgres 'mastery_attempt_qa' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres queryByAttempt results")
        void test0004() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<MasteryAttemptQaRec> all =
                            MasteryAttemptQaLogic.POSTGRES.queryByAttempt(cache, Integer.valueOf(11111), "EXAM1");

                    assertEquals(2, all.size(), "Incorrect record count from Postgres queryByAttempt");

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

                    assertTrue(found1, "Postgres mastery_attempt_qa 1 not found");
                    assertTrue(found2, "Postgres mastery_attempt_qa 2 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Postgres 'mastery_attempt_qa' rows by attempt: "
                        + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("query results")
        void test0005() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final MasteryAttemptQaRec r = MasteryAttemptQaLogic.POSTGRES.query(cache,
                            Integer.valueOf(11111), "EXAM1", Integer.valueOf(1));

                    assertNotNull(r, "query returned null");

                    if (!RAW1.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    postgresCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    assertTrue(MasteryAttemptQaLogic.POSTGRES.updateCorrect(cache, RAW1, "Z"),
                            "updateCorrect returned false");

                    final MasteryAttemptQaRec r = MasteryAttemptQaLogic.POSTGRES.query(cache,
                            Integer.valueOf(11111), "EXAM1", Integer.valueOf(1));

                    assertNotNull(r, "query returned null");

                    if (!RAW1UPD.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    postgresCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final boolean result = MasteryAttemptQaLogic.POSTGRES.delete(cache, RAW3);
                    assertTrue(result, "delete returned false");

                    final List<MasteryAttemptQaRec> all = MasteryAttemptQaLogic.POSTGRES.queryAll(cache);

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

                    assertTrue(found1, "Postgres mastery_attempt_qa 1 not found");
                    assertTrue(found2, "Postgres mastery_attempt_qa 2 not found");
                    assertTrue(found4, "Postgres mastery_attempt_qa 4 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting mastery_attempts: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM term_t.mastery_attempt_qa");
                    }

                    conn.commit();

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while cleaning tables: " + ex.getMessage());
            }
        }
    }
}
