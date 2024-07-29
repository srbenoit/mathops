package dev.mathops.db.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.EDbUse;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

/**
 * Tests for the {@code TermLogic} class.
 */
final class TestTermLogic {

    /** A raw test record. */
    private static final TermRec RAW1 = new TermRec(new TermKey(202190), //
            LocalDate.of(2021, 9, 1), LocalDate.of(2021, 10, 1), "2122",
            Integer.valueOf(-2), LocalDate.of(2021, 9, 11), LocalDate.of(2021, 9, 21));

    /** A raw test record. */
    private static final TermRec RAW2 = new TermRec(new TermKey(202210), //
            LocalDate.of(2022, 1, 1), LocalDate.of(2022, 2, 1), "2122",
            Integer.valueOf(-1), LocalDate.of(2022, 1, 11), LocalDate.of(2022, 1, 21));

    /** A raw test record. */
    private static final TermRec RAW3 = new TermRec(new TermKey(202260), //
            LocalDate.of(2022, 6, 1), LocalDate.of(2022, 7, 1), "2223",
            Integer.valueOf(0), LocalDate.of(2022, 6, 11), LocalDate.of(2022, 6, 21));

    /** A raw test record. */
    private static final TermRec RAW4 = new TermRec(new TermKey(202290), //
            LocalDate.of(2022, 9, 2), LocalDate.of(2022, 10, 2), "2223",
            Integer.valueOf(1), LocalDate.of(2022, 9, 12), LocalDate.of(2022, 9, 22));

    /** A raw test record. */
    private static final TermRec RAW5 = new TermRec(new TermKey(202310), //
            LocalDate.of(2023, 1, 2), LocalDate.of(2023, 2, 2), "2223",
            Integer.valueOf(2), LocalDate.of(2023, 1, 12), LocalDate.of(2023, 1, 22));

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final TermRec r) {

        Log.warning("Unexpected term ", r.term == null ? "null" : r.term.serializedString());
        Log.warning("Unexpected startDate ", r.startDate);
        Log.warning("Unexpected endDate ", r.endDate);
        Log.warning("Unexpected academicYear ", r.academicYear);
        Log.warning("Unexpected activeIndex ", r.activeIndex);
        Log.warning("Unexpected dropDeadline ", r.dropDeadline);
        Log.warning("Unexpected withdrawDeadline ", r.withdrawDeadline);
    }

    /**
     * Tests for the {@code TermLogic} class.
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
                        stmt.executeUpdate("DELETE FROM term");
                    }
                    conn.commit();

                    final Cache cache = new Cache(informixProfile, conn);

                    assertTrue(TermLogic.Informix.INSTANCE.insert(cache, RAW1), "Failed to insert Informix term");
                    assertTrue(TermLogic.Informix.INSTANCE.insert(cache, RAW2), "Failed to insert Informix term");
                    assertTrue(TermLogic.Informix.INSTANCE.insert(cache, RAW3), "Failed to insert Informix term");
                    assertTrue(TermLogic.Informix.INSTANCE.insert(cache, RAW4), "Failed to insert Informix term");
                    assertTrue(TermLogic.Informix.INSTANCE.insert(cache, RAW5), "Failed to insert Informix term");
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Informix 'term' table: " + ex.getMessage());
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
                    final List<TermRec> all = TermLogic.Informix.INSTANCE.queryAll(cache);

                    assertEquals(5, all.size(), "Incorrect record count from Informix queryAll");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found5 = false;

                    for (final TermRec r : all) {
                        if (RAW1.equals(r)) {
                            found1 = true;
                        } else if (RAW2.equals(r)) {
                            found2 = true;
                        } else if (RAW3.equals(r)) {
                            found3 = true;
                        } else if (RAW4.equals(r)) {
                            found4 = true;
                        } else if (RAW5.equals(r)) {
                            found5 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found1, "Informix term 1 not found");
                    assertTrue(found2, "Informix term 2 not found");
                    assertTrue(found3, "Informix term 3 not found");
                    assertTrue(found4, "Informix term 4 not found");
                    assertTrue(found5, "Informix term 5 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'term' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByIndex results")
        void test0004() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final TermRec index2 = TermLogic.Informix.INSTANCE.queryByIndex(cache, 2);

                    assertNotNull(index2, "Informix queryByIndex returned null");
                    assertEquals(index2, RAW5, "Informix term index 2 not found");
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'term' rows by index: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix getFutureTerms results")
        void test0005() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<TermRec> all = TermLogic.Informix.INSTANCE.getFutureTerms(cache);

                    assertEquals(2, all.size(), "Incorrect record count from Informix getFutureTerms");

                    boolean found4 = false;
                    boolean found5 = false;

                    for (final TermRec r : all) {
                        if (RAW4.equals(r)) {
                            found4 = true;
                        } else if (RAW5.equals(r)) {
                            found5 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found4, "Informix term 4 not found");
                    assertTrue(found5, "Informix term 5 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix future 'term' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryActive results")
        void test0006() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final TermRec active = TermLogic.Informix.INSTANCE.queryActive(cache);

                    assertNotNull(active, "Informix queryActive returned null");
                    assertEquals(active, RAW3, "Informix active term not found");
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix active 'term' row: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryNext results")
        void test0007() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final TermRec active = TermLogic.Informix.INSTANCE.queryNext(cache);

                    assertNotNull(active, "Informix queryNext returned null");
                    assertEquals(active, RAW4, "Informix next term not found");
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix next 'term' row: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryPrior results")
        void test0008() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final TermRec active = TermLogic.Informix.INSTANCE.queryPrior(cache);

                    assertNotNull(active, "Informix queryPrior returned null");
                    assertEquals(active, RAW2, "Informix prior term not found");
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix prior 'term' row: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix query results")
        void test0009() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final TermRec active = TermLogic.Informix.INSTANCE.query(cache, RAW1.term);

                    assertNotNull(active, "Informix query returned null");
                    assertEquals(active, RAW1, "Informix term not found");
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'term' row: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM term");
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
     * Tests for the {@code TermLogic} class.
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
                        stmt.executeUpdate("DELETE FROM main_t.term");
                    }
                    conn.commit();

                    final Cache cache = new Cache(postgresProfile, conn);

                    assertTrue(TermLogic.Postgres.INSTANCE.insert(cache, RAW1), "Failed to insert PostgreSQL term");
                    assertTrue(TermLogic.Postgres.INSTANCE.insert(cache, RAW2), "Failed to insert PostgreSQL term");
                    assertTrue(TermLogic.Postgres.INSTANCE.insert(cache, RAW3), "Failed to insert PostgreSQL term");
                    assertTrue(TermLogic.Postgres.INSTANCE.insert(cache, RAW4), "Failed to insert PostgreSQL term");
                    assertTrue(TermLogic.Postgres.INSTANCE.insert(cache, RAW5), "Failed to insert PostgreSQL term");
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Postgres 'main_t.term' table: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("PostgreSQL queryAll results")
        void test0003() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<TermRec> all = TermLogic.Postgres.INSTANCE.queryAll(cache);

                    assertEquals(5, all.size(), "Incorrect record count from PostgreSQL queryAll");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found5 = false;

                    for (final TermRec r : all) {
                        if (RAW1.equals(r)) {
                            found1 = true;
                        } else if (RAW2.equals(r)) {
                            found2 = true;
                        } else if (RAW3.equals(r)) {
                            found3 = true;
                        } else if (RAW4.equals(r)) {
                            found4 = true;
                        } else if (RAW5.equals(r)) {
                            found5 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found1, "PostgreSQL term 1 not found");
                    assertTrue(found2, "PostgreSQL term 2 not found");
                    assertTrue(found3, "PostgreSQL term 3 not found");
                    assertTrue(found4, "PostgreSQL term 4 not found");
                    assertTrue(found5, "PostgreSQL term 5 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all PostgreSQL 'term' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("PostgreSQL queryByIndex results")
        void test0004() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final TermRec index2 = TermLogic.Postgres.INSTANCE.queryByIndex(cache, 2);

                    assertNotNull(index2, "PostgreSQL queryByIndex returned null");
                    assertEquals(index2, RAW5, "PostgreSQL term index 2 not found");
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying PostgreSQL 'term' rows by index: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("PostgreSQL getFutureTerms results")
        void test0005() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<TermRec> all = TermLogic.Postgres.INSTANCE.getFutureTerms(cache);

                    assertEquals(2, all.size(), "Incorrect record count from PostgreSQL getFutureTerms");

                    boolean found4 = false;
                    boolean found5 = false;

                    for (final TermRec r : all) {
                        if (RAW4.equals(r)) {
                            found4 = true;
                        } else if (RAW5.equals(r)) {
                            found5 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found4, "PostgreSQL term 4 not found");
                    assertTrue(found5, "PostgreSQL term 5 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying PostgreSQL future 'term' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("PostgreSQL queryActive results")
        void test0006() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final TermRec active = TermLogic.Postgres.INSTANCE.queryActive(cache);

                    assertNotNull(active, "PostgreSQL queryActive returned null");
                    assertEquals(active, RAW3, "PostgreSQL active term not found");
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying PostgreSQL active 'term' row: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("PostgreSQL queryNext results")
        void test0007() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final TermRec active = TermLogic.Postgres.INSTANCE.queryNext(cache);

                    assertNotNull(active, "PostgreSQL queryNext returned null");
                    assertEquals(active, RAW4, "PostgreSQL next term not found");
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying PostgreSQL next 'term' row: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("PostgreSQL queryPrior results")
        void test0008() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final TermRec active = TermLogic.Postgres.INSTANCE.queryPrior(cache);

                    assertNotNull(active, "PostgreSQL queryPrior returned null");
                    assertEquals(active, RAW2, "PostgreSQL prior term not found");
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying PostgreSQL prior 'term' row: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("PostgreSQL query results")
        void test0009() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final TermRec active = TermLogic.Postgres.INSTANCE.query(cache, RAW1.term);

                    assertNotNull(active, "PostgreSQL query returned null");
                    assertEquals(active, RAW1, "Postgres term not found");
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying PostgreSQL 'term' row: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM main_t.term");
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
