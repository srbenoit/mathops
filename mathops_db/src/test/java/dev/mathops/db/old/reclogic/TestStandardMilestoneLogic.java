package dev.mathops.db.old.reclogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.EDbUse;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rec.StandardMilestoneRec;

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
import java.time.LocalDate;
import java.util.List;

/**
 * Tests for the {@code StandardMilestoneLogic} class.
 */
final class TestStandardMilestoneLogic {

    /** A raw test record. */
    private static final StandardMilestoneRec RAW1 = new StandardMilestoneRec("A", Integer.valueOf(5),
            Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "OP", LocalDate.of(2023, 1, 1));

    /** A raw test record. */
    private static final StandardMilestoneRec RAW2 = new StandardMilestoneRec("A", Integer.valueOf(5),
            Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "MA", LocalDate.of(2023, 1, 2));

    /** A raw test record. */
    private static final StandardMilestoneRec RAW3 = new StandardMilestoneRec("A", Integer.valueOf(5),
            Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(2), "OP", LocalDate.of(2023, 1, 3));

    /** A raw test record. */
    private static final StandardMilestoneRec RAW4 = new StandardMilestoneRec("A", Integer.valueOf(5),
            Integer.valueOf(4), Integer.valueOf(7), Integer.valueOf(1), "EX", LocalDate.of(2023, 1, 4));

    /** A raw test record. */
    private static final StandardMilestoneRec RAW5 = new StandardMilestoneRec("A", Integer.valueOf(4),
            Integer.valueOf(3), Integer.valueOf(6), Integer.valueOf(2), "CD", LocalDate.of(2023, 1, 5));

    /** A raw test record. */
    private static final StandardMilestoneRec RAW6 = new StandardMilestoneRec("B", Integer.valueOf(5),
            Integer.valueOf(1), Integer.valueOf(5), Integer.valueOf(3), "MA", LocalDate.of(2023, 1, 6));

    /** A raw test record. */
    private static final StandardMilestoneRec RAW1NEWDATE = new StandardMilestoneRec("A", Integer.valueOf(5),
            Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "OP", LocalDate.of(2023, 12, 10));

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StandardMilestoneRec r) {

        Log.warning("Unexpected paceTrack ", r.paceTrack);
        Log.warning("Unexpected pace ", r.pace);
        Log.warning("Unexpected paceIndex ", r.paceIndex);
        Log.warning("Unexpected unit ", r.unit);
        Log.warning("Unexpected objective ", r.objective);
        Log.warning("Unexpected msType ", r.msType);
        Log.warning("Unexpected msDate ", r.msDate);
    }

    /**
     * Tests for the {@code StandardMilestoneLogic} class.
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
                        stmt.executeUpdate("DELETE FROM std_milestone");
                    }
                    conn.commit();

                    final Cache cache = new Cache(informixProfile, conn);

                    assertTrue(StandardMilestoneLogic.INFORMIX.insert(cache, RAW1),
                            "Failed to insert Informix std_milestone");

                    assertTrue(StandardMilestoneLogic.INFORMIX.insert(cache, RAW2),
                            "Failed to insert Informix std_milestone");

                    assertTrue(StandardMilestoneLogic.INFORMIX.insert(cache, RAW3),
                            "Failed to insert Informix std_milestone");

                    assertTrue(StandardMilestoneLogic.INFORMIX.insert(cache, RAW4),
                            "Failed to insert Informix std_milestone");

                    assertTrue(StandardMilestoneLogic.INFORMIX.insert(cache, RAW5),
                            "Failed to insert Informix std_milestone");

                    assertTrue(StandardMilestoneLogic.INFORMIX.insert(cache, RAW6),
                            "Failed to insert Informix std_milestone");
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Informix 'std_milestone' table: " + ex.getMessage());
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
                    final List<StandardMilestoneRec> all = StandardMilestoneLogic.INFORMIX.queryAll(cache);

                    assertEquals(6, all.size(), "Incorrect record count from Informix queryAll");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found5 = false;
                    boolean found6 = false;

                    for (final StandardMilestoneRec r : all) {
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
                        } else if (RAW6.equals(r)) {
                            found6 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found1, "Informix std_milestone 1 not found");
                    assertTrue(found2, "Informix std_milestone 2 not found");
                    assertTrue(found3, "Informix std_milestone 3 not found");
                    assertTrue(found4, "Informix std_milestone 4 not found");
                    assertTrue(found5, "Informix std_milestone 5 not found");
                    assertTrue(found6, "Informix std_milestone 6 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'std_milestone' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByPaceTrackPace results")
        void test0004() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<StandardMilestoneRec> all = StandardMilestoneLogic.INFORMIX
                            .queryByPaceTrackPace(cache, "A", Integer.valueOf(5));

                    assertEquals(4, all.size(), "Incorrect record count from Informix queryByPaceTrackPace");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;

                    for (final StandardMilestoneRec r : all) {
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

                    assertTrue(found1, "Informix std_milestone 1 not found");
                    assertTrue(found2, "Informix std_milestone 2 not found");
                    assertTrue(found3, "Informix std_milestone 3 not found");
                    assertTrue(found4, "Informix std_milestone 4 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'std_milestone' rows by track and pace: "
                        + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByPaceTrackPaceIndex results")
        void test0005() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<StandardMilestoneRec> all = StandardMilestoneLogic.INFORMIX
                            .queryByPaceTrackPaceIndex(cache, "A", Integer.valueOf(5), Integer.valueOf(3));
                    assertEquals(3, all.size(), "Incorrect record count from Informix queryByPaceTrackPaceIndex");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;

                    for (final StandardMilestoneRec r : all) {
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

                    assertTrue(found1, "Informix std_milestone 1 not found");
                    assertTrue(found2, "Informix std_milestone 2 not found");
                    assertTrue(found3, "Informix std_milestone 3 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'std_milestone' rows by track, pace, index: "
                        + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("query results")
        void test0006() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final StandardMilestoneRec r = StandardMilestoneLogic.INFORMIX.query(cache, "A", Integer.valueOf(5),
                            Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "OP");

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying std_milestone: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("updateDate results")
        void test0007() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final boolean result = StandardMilestoneLogic.INFORMIX.updateDate(cache, RAW1, RAW1NEWDATE.msDate);
                    assertTrue(result, "updateDate returned false");

                    final StandardMilestoneRec r = StandardMilestoneLogic.INFORMIX.query(cache, "A", Integer.valueOf(5),
                            Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "OP");

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWDATE.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while updating date: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("delete results")
        void test0008() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final boolean result = StandardMilestoneLogic.INFORMIX.delete(cache, RAW5);
                    assertTrue(result, "delete returned false");

                    final List<StandardMilestoneRec> all = StandardMilestoneLogic.INFORMIX.queryAll(cache);

                    assertEquals(5, all.size(), "Incorrect record count from queryAll after delete");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found6 = false;

                    for (final StandardMilestoneRec r : all) {
                        if (RAW1NEWDATE.equals(r)) {
                            found1 = true;
                        } else if (RAW2.equals(r)) {
                            found2 = true;
                        } else if (RAW3.equals(r)) {
                            found3 = true;
                        } else if (RAW4.equals(r)) {
                            found4 = true;
                        } else if (RAW6.equals(r)) {
                            found6 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found1, "std_milestone 1 not found");
                    assertTrue(found2, "std_milestone 2 not found");
                    assertTrue(found3, "std_milestone 3 not found");
                    assertTrue(found4, "std_milestone 4 not found");
                    assertTrue(found6, "std_milestone 6 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting std_milestones: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM std_milestone");
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
     * Tests for the {@code StandardMilestoneLogic} class.
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
                        stmt.executeUpdate("DELETE FROM term_t.std_milestone");
                    }
                    conn.commit();

                    final Cache cache = new Cache(postgresProfile, conn);

                    assertTrue(StandardMilestoneLogic.POSTGRES.insert(cache, RAW1),
                            "Failed to insert Postgres std_milestone");

                    assertTrue(StandardMilestoneLogic.POSTGRES.insert(cache, RAW2),
                            "Failed to insert Postgres std_milestone");

                    assertTrue(StandardMilestoneLogic.POSTGRES.insert(cache, RAW3),
                            "Failed to insert Postgres std_milestone");

                    assertTrue(StandardMilestoneLogic.POSTGRES.insert(cache, RAW4),
                            "Failed to insert Postgres std_milestone");

                    assertTrue(StandardMilestoneLogic.POSTGRES.insert(cache, RAW5),
                            "Failed to insert Postgres std_milestone");

                    assertTrue(StandardMilestoneLogic.POSTGRES.insert(cache, RAW6),
                            "Failed to insert Postgres std_milestone");
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Postgres 'term_t.std_milestone' table: " + ex.getMessage());
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
                    final List<StandardMilestoneRec> all = StandardMilestoneLogic.POSTGRES.queryAll(cache);

                    assertEquals(6, all.size(), "Incorrect record count from Postgres queryAll");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found5 = false;
                    boolean found6 = false;

                    for (final StandardMilestoneRec r : all) {
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
                        } else if (RAW6.equals(r)) {
                            found6 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found1, "Postgres std_milestone 1 not found");
                    assertTrue(found2, "Postgres std_milestone 2 not found");
                    assertTrue(found3, "Postgres std_milestone 3 not found");
                    assertTrue(found4, "Postgres std_milestone 4 not found");
                    assertTrue(found5, "Postgres std_milestone 5 not found");
                    assertTrue(found6, "Postgres std_milestone 6 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Postgres 'std_milestone' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres queryByPaceTrackPace results")
        void test0004() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<StandardMilestoneRec> all = StandardMilestoneLogic.POSTGRES
                            .queryByPaceTrackPace(cache, "A", Integer.valueOf(5));

                    assertEquals(4, all.size(), "Incorrect record count from Postgres queryByPaceTrackPace");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;

                    for (final StandardMilestoneRec r : all) {
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

                    assertTrue(found1, "Postgres std_milestone 1 not found");
                    assertTrue(found2, "Postgres std_milestone 2 not found");
                    assertTrue(found3, "Postgres std_milestone 3 not found");
                    assertTrue(found4, "Postgres std_milestone 4 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Postgres 'std_milestone' rows by track and pace: "
                        + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres queryByPaceTrackPaceIndex results")
        void test0005() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<StandardMilestoneRec> all = StandardMilestoneLogic.POSTGRES
                            .queryByPaceTrackPaceIndex(cache, "A", Integer.valueOf(5), Integer.valueOf(3));

                    assertEquals(3, all.size(), "Incorrect record count from Postgres queryByPaceTrackPaceIndex");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;

                    for (final StandardMilestoneRec r : all) {
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

                    assertTrue(found1, "Postgres std_milestone 1 not found");
                    assertTrue(found2, "Postgres std_milestone 2 not found");
                    assertTrue(found3, "Postgres std_milestone 3 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Postgres 'std_milestone' rows by track, pace, index: "
                        + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("query results")
        void test0006() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final StandardMilestoneRec r = StandardMilestoneLogic.POSTGRES.query(cache, "A", Integer.valueOf(5),
                            Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "OP");

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying std_milestone: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("updateDate results")
        void test0007() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final boolean result = StandardMilestoneLogic.POSTGRES.updateDate(cache, RAW1, RAW1NEWDATE.msDate);
                    assertTrue(result, "updateDate returned false");

                    final StandardMilestoneRec r = StandardMilestoneLogic.POSTGRES.query(cache, "A", Integer.valueOf(5),
                            Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "OP");

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWDATE.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while updating date: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("delete results")
        void test0008() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final boolean result = StandardMilestoneLogic.POSTGRES.delete(cache, RAW5);
                    assertTrue(result, "delete returned false");

                    final List<StandardMilestoneRec> all = StandardMilestoneLogic.POSTGRES.queryAll(cache);

                    assertEquals(5, all.size(), "Incorrect record count from queryAll after delete");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found6 = false;

                    for (final StandardMilestoneRec r : all) {
                        if (RAW1NEWDATE.equals(r)) {
                            found1 = true;
                        } else if (RAW2.equals(r)) {
                            found2 = true;
                        } else if (RAW3.equals(r)) {
                            found3 = true;
                        } else if (RAW4.equals(r)) {
                            found4 = true;
                        } else if (RAW6.equals(r)) {
                            found6 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found1, "std_milestone 1 not found");
                    assertTrue(found2, "std_milestone 2 not found");
                    assertTrue(found3, "std_milestone 3 not found");
                    assertTrue(found4, "std_milestone 4 not found");
                    assertTrue(found6, "std_milestone 6 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting std_milestones: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM term_t.std_milestone");
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
