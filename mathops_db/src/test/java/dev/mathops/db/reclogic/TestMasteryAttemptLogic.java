package dev.mathops.db.reclogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.EDbUse;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.rec.MasteryAttemptRec;

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
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tests for the {@code MasteryAttemptLogic} class.
 */
final class TestMasteryAttemptLogic {

    /** A raw test record. */
    private static final MasteryAttemptRec RAW1 = new MasteryAttemptRec(Integer.valueOf(11111), //
            "EXAM1", "STU1",
            LocalDateTime.of(2021, 1, 1, 1, 2, 3), LocalDateTime.of(2022, 2, 2, 4, 5, 6),
            Integer.valueOf(1), Integer.valueOf(2), "N", "N",
            "S1");

    /** A raw test record. */
    private static final MasteryAttemptRec RAW2 = new MasteryAttemptRec(Integer.valueOf(11111), //
            "EXAM2", "STU1",
            LocalDateTime.of(2021, 1, 2, 2, 3, 4), LocalDateTime.of(2022, 2, 3, 5, 6, 7),
            Integer.valueOf(4), Integer.valueOf(3), "Y", "N",
            "S2");

    /** A raw test record. */
    private static final MasteryAttemptRec RAW3 = new MasteryAttemptRec(Integer.valueOf(22222), //
            "EXAM1", "STU1",
            LocalDateTime.of(2021, 1, 3, 3, 4, 5), LocalDateTime.of(2022, 2, 4, 6, 7, 8),
            Integer.valueOf(2), Integer.valueOf(2), "Y", "Y",
            "S3");

    /** A raw test record. */
    private static final MasteryAttemptRec RAW4 = new MasteryAttemptRec(Integer.valueOf(33333), //
            "EXAM2", "STU2",
            LocalDateTime.of(2021, 1, 4, 4, 5, 6), LocalDateTime.of(2022, 2, 5, 7, 8, 9),
            Integer.valueOf(0), Integer.valueOf(2), "N", "N",
            "S4");

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final MasteryAttemptRec r) {

        Log.warning("Unexpected serialNbr ", r.serialNbr);
        Log.warning("Unexpected examId ", r.examId);
        Log.warning("Unexpected stuId ", r.stuId);
        Log.warning("Unexpected whenStarted ", r.whenStarted);
        Log.warning("Unexpected whenFinished ", r.whenFinished);
        Log.warning("Unexpected examScore ", r.examScore);
        Log.warning("Unexpected masteryScore ", r.masteryScore);
        Log.warning("Unexpected passed ", r.passed);
        Log.warning("Unexpected isFirstPassed ", r.isFirstPassed);
        Log.warning("Unexpected examSource ", r.examSource);
    }

    /**
     * Tests for the {@code MasteryAttemptLogic} class.
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
                        stmt.executeUpdate("DELETE FROM mastery_attempt");
                    }
                    conn.commit();

                    final Cache cache = new Cache(informixProfile, conn);

                    assertTrue(MasteryAttemptLogic.INFORMIX.insert(cache, RAW1),
                            "Failed to insert Informix mastery_attempt");

                    assertTrue(MasteryAttemptLogic.INFORMIX.insert(cache, RAW2),
                            "Failed to insert Informix mastery_attempt");

                    assertTrue(MasteryAttemptLogic.INFORMIX.insert(cache, RAW3),
                            "Failed to insert Informix mastery_attempt");

                    assertTrue(MasteryAttemptLogic.INFORMIX.insert(cache, RAW4),
                            "Failed to insert Informix mastery_attempt");
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Informix 'mastery_attempt' table: " + ex.getMessage());
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
                    final List<MasteryAttemptRec> all = MasteryAttemptLogic.INFORMIX.queryAll(cache);

                    assertEquals(4, all.size(), "Incorrect record count from Informix queryAll");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;

                    for (final MasteryAttemptRec r : all) {
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

                    assertTrue(found1, "Informix mastery_attempt 1 not found");
                    assertTrue(found2, "Informix mastery_attempt 2 not found");
                    assertTrue(found3, "Informix mastery_attempt 3 not found");
                    assertTrue(found4, "Informix mastery_attempt 4 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'mastery_attempt' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByStudent results")
        void test0004() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<MasteryAttemptRec> all =
                            MasteryAttemptLogic.INFORMIX.queryByStudent(cache, "STU1");

                    assertEquals(3, all.size(), "Incorrect record count from Informix queryByStudent");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;

                    for (final MasteryAttemptRec r : all) {
                        if (RAW1.equals(r)) {
                            found1 = true;
                        } else if (RAW2.equals(r)) {
                            found2 = true;
                        } else if (RAW3.equals(r)) {
                            found3 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found1, "Informix mastery_attempt 1 not found");
                    assertTrue(found2, "Informix mastery_attempt 2 not found");
                    assertTrue(found3, "Informix mastery_attempt 3 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'mastery_attempt' rows by student: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByExam results")
        void test0005() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<MasteryAttemptRec> all =
                            MasteryAttemptLogic.INFORMIX.queryByExam(cache, "EXAM1");

                    assertEquals(2, all.size(), "Incorrect record count from Informix queryByExam");

                    boolean found1 = false;
                    boolean found3 = false;

                    for (final MasteryAttemptRec r : all) {
                        if (RAW1.equals(r)) {
                            found1 = true;
                        } else if (RAW3.equals(r)) {
                            found3 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found1, "Informix mastery_attempt 1 not found");
                    assertTrue(found3, "Informix mastery_attempt 3 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'mastery_attempt' rows by exam: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByStudentExam(false) results")
        void test0006() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<MasteryAttemptRec> all =
                            MasteryAttemptLogic.INFORMIX.queryByStudentExam(cache, "STU1", "EXAM1", false);

                    assertEquals(2, all.size(), "Incorrect record count from Informix queryByStudentExam(false)");

                    boolean found1 = false;
                    boolean found3 = false;

                    for (final MasteryAttemptRec r : all) {
                        if (RAW1.equals(r)) {
                            found1 = true;
                        } else if (RAW3.equals(r)) {
                            found3 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found1, "Informix mastery_attempt 1 not found");
                    assertTrue(found3, "Informix mastery_attempt 3 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'mastery_attempt' rows by student and exam: "
                        + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByStudentExam(true) results")
        void test0007() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<MasteryAttemptRec> all =
                            MasteryAttemptLogic.INFORMIX.queryByStudentExam(cache, "STU1", "EXAM1", true);

                    assertEquals(1, all.size(), "Incorrect record count from Informix queryByStudentExam(true)");

                    boolean found3 = false;

                    for (final MasteryAttemptRec r : all) {
                        if (RAW3.equals(r)) {
                            found3 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found3, "Informix mastery_attempt 3 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'mastery_attempt' rows by student and exam: "
                        + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix query results")
        void test0008() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final MasteryAttemptRec r = MasteryAttemptLogic.INFORMIX.query(cache, Integer.valueOf(11111),
                            "EXAM1");

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
                fail("Exception while querying mastery_attempt by serial number and exam: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix delete results")
        void test0009() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final boolean result = MasteryAttemptLogic.INFORMIX.delete(cache, RAW3);
                    assertTrue(result, "delete returned false");

                    final List<MasteryAttemptRec> all = MasteryAttemptLogic.INFORMIX.queryAll(cache);

                    assertEquals(3, all.size(), "Incorrect record count after delete");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found4 = false;

                    for (final MasteryAttemptRec r : all) {
                        if (RAW1.equals(r)) {
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

                    assertTrue(found1, "Informix mastery_attempt 1 not found");
                    assertTrue(found2, "Informix mastery_attempt 2 not found");
                    assertTrue(found4, "Informix mastery_attempt 4 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
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
     * Tests for the {@code MasteryAttemptLogic} class.
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
                        stmt.executeUpdate("DELETE FROM term_t.mastery_attempt");
                    }
                    conn.commit();

                    final Cache cache = new Cache(postgresProfile, conn);

                    assertTrue(MasteryAttemptLogic.POSTGRES.insert(cache, RAW1),
                            "Failed to insert Postgres mastery_attempt");

                    assertTrue(MasteryAttemptLogic.POSTGRES.insert(cache, RAW2),
                            "Failed to insert Postgres mastery_attempt");

                    assertTrue(MasteryAttemptLogic.POSTGRES.insert(cache, RAW3),
                            "Failed to insert Postgres mastery_attempt");

                    assertTrue(MasteryAttemptLogic.POSTGRES.insert(cache, RAW4),
                            "Failed to insert Postgres mastery_attempt");
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Postgres 'term_t.mastery_attempt' table: " + ex.getMessage());
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
                    final List<MasteryAttemptRec> all = MasteryAttemptLogic.POSTGRES.queryAll(cache);

                    assertEquals(4, all.size(), "Incorrect record count from Postgres queryAll");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;

                    for (final MasteryAttemptRec r : all) {
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

                    assertTrue(found1, "Postgres mastery_attempt 1 not found");
                    assertTrue(found2, "Postgres mastery_attempt 2 not found");
                    assertTrue(found3, "Postgres mastery_attempt 3 not found");
                    assertTrue(found4, "Postgres mastery_attempt 4 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Postgres 'mastery_attempt' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres queryByStudent results")
        void test0004() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<MasteryAttemptRec> all =
                            MasteryAttemptLogic.POSTGRES.queryByStudent(cache, "STU1");

                    assertEquals(3, all.size(), "Incorrect record count from Postgres queryByStudent");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;

                    for (final MasteryAttemptRec r : all) {
                        if (RAW1.equals(r)) {
                            found1 = true;
                        } else if (RAW2.equals(r)) {
                            found2 = true;
                        } else if (RAW3.equals(r)) {
                            found3 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found1, "Postgres mastery_attempt 1 not found");
                    assertTrue(found2, "Postgres mastery_attempt 2 not found");
                    assertTrue(found3, "Postgres mastery_attempt 3 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Postgres 'mastery_attempt' rows by student: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres queryByExam results")
        void test0005() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<MasteryAttemptRec> all = MasteryAttemptLogic.POSTGRES.queryByExam(cache, "EXAM1");

                    assertEquals(2, all.size(), "Incorrect record count from Postgres queryByExam");

                    boolean found1 = false;
                    boolean found3 = false;

                    for (final MasteryAttemptRec r : all) {
                        if (RAW1.equals(r)) {
                            found1 = true;
                        } else if (RAW3.equals(r)) {
                            found3 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found1, "Postgres mastery_attempt 1 not found");
                    assertTrue(found3, "Postgres mastery_attempt 3 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Postgres 'mastery_attempt' rows by exam: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres queryByStudentExam(false) results")
        void test0006() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<MasteryAttemptRec> all =
                            MasteryAttemptLogic.POSTGRES.queryByStudentExam(cache, "STU1", "EXAM1", false);

                    assertEquals(2, all.size(), "Incorrect record count from Postgres queryByStudentExam(false)");

                    boolean found1 = false;
                    boolean found3 = false;

                    for (final MasteryAttemptRec r : all) {
                        if (RAW1.equals(r)) {
                            found1 = true;
                        } else if (RAW3.equals(r)) {
                            found3 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found1, "Postgres mastery_attempt 1 not found");
                    assertTrue(found3, "Postgres mastery_attempt 3 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Postgres 'mastery_attempt' rows by student and exam: "
                        + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres queryByStudentExam(true) results")
        void test0007() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<MasteryAttemptRec> all =
                            MasteryAttemptLogic.POSTGRES.queryByStudentExam(cache, "STU1", "EXAM1", true);

                    assertEquals(1, all.size(), "Incorrect record count from Postgres queryByStudentExam(true)");

                    boolean found3 = false;

                    for (final MasteryAttemptRec r : all) {
                        if (RAW3.equals(r)) {
                            found3 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found3, "Postgres mastery_attempt 3 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Postgres 'mastery_attempt' rows by student and exam: "
                        + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres query results")
        void test0008() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final MasteryAttemptRec r = MasteryAttemptLogic.POSTGRES.query(cache, Integer.valueOf(11111),
                            "EXAM1");

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
                fail("Exception while querying mastery_attempt by serial number and exam: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres delete results")
        void test0009() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final boolean result = MasteryAttemptLogic.POSTGRES.delete(cache, RAW3);
                    assertTrue(result, "delete returned false");

                    final List<MasteryAttemptRec> all = MasteryAttemptLogic.POSTGRES.queryAll(cache);

                    assertEquals(3, all.size(), "Incorrect record count after delete");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found4 = false;

                    for (final MasteryAttemptRec r : all) {
                        if (RAW1.equals(r)) {
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

                    assertTrue(found1, "Postgres mastery_attempt 1 not found");
                    assertTrue(found2, "Postgres mastery_attempt 2 not found");
                    assertTrue(found4, "Postgres mastery_attempt 4 not found");

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
                        stmt.executeUpdate("DELETE FROM term_t.mastery_attempt");
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
