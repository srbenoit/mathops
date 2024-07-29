package dev.mathops.db.old.reclogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.EDbUse;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rec.AssignmentRec;

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
 * Tests for the {@code AssignmentLogic} class.
 */
final class TestAssignmentLogic {

    /** A raw test record. */
    private static final AssignmentRec RAW1 = new AssignmentRec("1718H", "HW", RawRecordConstants.M117,
            Integer.valueOf(1), Integer.valueOf(8), "17.1.1.8H", "1.1.8H", LocalDateTime.of(2021, 1, 1, 0, 0, 0), null);

    /** A raw test record. */
    private static final AssignmentRec RAW2 = new AssignmentRec("1718L", "LB", RawRecordConstants.M117,
            Integer.valueOf(1), Integer.valueOf(8), "17.1.1.8L", "1.1.8L", LocalDateTime.of(2021, 1, 2, 0, 0, 0), null);

    /** A raw test record. */
    private static final AssignmentRec RAW3 = new AssignmentRec("1719H", "HW", RawRecordConstants.M117,
            Integer.valueOf(1), Integer.valueOf(9), "17.1.1.9H", "1.1.9H", LocalDateTime.of(2021, 1, 3, 0, 0, 0), null);

    /** A raw test record. */
    private static final AssignmentRec RAW4 = new AssignmentRec("1721E", "HW", RawRecordConstants.M117,
            Integer.valueOf(2), Integer.valueOf(1), "17.1.2.1H", "1.2.1H", LocalDateTime.of(2021, 1, 4, 0, 0, 0), null);

    /** A raw test record. */
    private static final AssignmentRec RAW5 = new AssignmentRec("1818H", "HW", RawRecordConstants.M118,
            Integer.valueOf(1), Integer.valueOf(8), "18.1.1.8H", "1.1.8H", LocalDateTime.of(2021, 1, 5, 0, 0, 0), null);

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final AssignmentRec r) {

        Log.warning("Unexpected assignmentId ", r.assignmentId);
        Log.warning("Unexpected courseId ", r.courseId);
        Log.warning("Unexpected unit ", r.unit);
        Log.warning("Unexpected objective ", r.objective);
        Log.warning("Unexpected title ", r.title);
        Log.warning("Unexpected treeRef ", r.treeRef);
        Log.warning("Unexpected assignmentType ", r.assignmentType);
        Log.warning("Unexpected whenActive ", r.whenActive);
        Log.warning("Unexpected whenPulled ", r.whenPulled);
    }

    /**
     * Tests for the {@code AssignmentLogic} class.
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
                        stmt.executeUpdate("DELETE FROM homework");
                    }
                    conn.commit();

                    final Cache cache = new Cache(informixProfile, conn);

                    assertTrue(AssignmentLogic.INFORMIX.insert(cache, RAW1), "Failed to insert Informix homework");
                    assertTrue(AssignmentLogic.INFORMIX.insert(cache, RAW2), "Failed to insert Informix homework");
                    assertTrue(AssignmentLogic.INFORMIX.insert(cache, RAW3), "Failed to insert Informix homework");
                    assertTrue(AssignmentLogic.INFORMIX.insert(cache, RAW4), "Failed to insert Informix homework");
                    assertTrue(AssignmentLogic.INFORMIX.insert(cache, RAW5), "Failed to insert Informix homework");
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Informix 'homework' table: " + ex.getMessage());
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
                    final List<AssignmentRec> all = AssignmentLogic.INFORMIX.queryAll(cache);

                    assertEquals(5, all.size(), "Incorrect record count from Informix queryAll");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found5 = false;

                    for (final AssignmentRec r : all) {
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

                    assertTrue(found1, "Informix homework 1 not found");
                    assertTrue(found2, "Informix homework 2 not found");
                    assertTrue(found3, "Informix homework 3 not found");
                    assertTrue(found4, "Informix homework 4 not found");
                    assertTrue(found5, "Informix homework 5 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'homework' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryActiveByCourse results")
        void test0004() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<AssignmentRec> all = AssignmentLogic.INFORMIX
                            .queryActiveByCourse(cache, RawRecordConstants.M117, null);

                    assertEquals(4, all.size(), "Incorrect record count from Informix queryActiveByCourse");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;

                    for (final AssignmentRec r : all) {
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

                    assertTrue(found1, "Informix homework 1 not found");
                    assertTrue(found2, "Informix homework 2 not found");
                    assertTrue(found3, "Informix homework 3 not found");
                    assertTrue(found4, "Informix homework 4 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'homework' rows by course: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("query results")
        void test0008() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final AssignmentRec r = AssignmentLogic.INFORMIX.query(cache, "1718H");

                    assertNotNull(r, "No record returned by Informix query");

                    if (!RAW1.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying homework by version: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("delete results")
        void test0009() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final boolean result = AssignmentLogic.INFORMIX.delete(cache, RAW2);
                    assertTrue(result, "delete returned false");

                    final List<AssignmentRec> all = AssignmentLogic.INFORMIX.queryAll(cache);

                    assertEquals(4, all.size(), "Incorrect record count from queryAll after delete");

                    boolean found1 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found5 = false;

                    for (final AssignmentRec r : all) {
                        if (RAW1.equals(r)) {
                            found1 = true;
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

                    assertTrue(found1, "homework 1 not found");
                    assertTrue(found3, "homework 3 not found");
                    assertTrue(found4, "homework 4 not found");
                    assertTrue(found5, "homework 5 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting homeworks: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM homework");
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
     * Tests for the {@code AssignmentLogic} class.
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
                        stmt.executeUpdate("DELETE FROM main_t.assignment");
                    }
                    conn.commit();

                    final Cache cache = new Cache(postgresProfile, conn);

                    assertTrue(AssignmentLogic.POSTGRES.insert(cache, RAW1), "Failed to insert PostgreSQL assignment");
                    assertTrue(AssignmentLogic.POSTGRES.insert(cache, RAW2), "Failed to insert PostgreSQL assignment");
                    assertTrue(AssignmentLogic.POSTGRES.insert(cache, RAW3), "Failed to insert PostgreSQL assignment");
                    assertTrue(AssignmentLogic.POSTGRES.insert(cache, RAW4), "Failed to insert PostgreSQL assignment");
                    assertTrue(AssignmentLogic.POSTGRES.insert(cache, RAW5), "Failed to insert PostgreSQL assignment");
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Postgres 'main_t.assignment' table: " + ex.getMessage());
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
                    final List<AssignmentRec> all = AssignmentLogic.POSTGRES.queryAll(cache);

                    assertEquals(5, all.size(), "Incorrect record count from PostgreSQL queryAll");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found5 = false;

                    for (final AssignmentRec r : all) {
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

                    assertTrue(found1, "PostgreSQL assignment 1 not found");
                    assertTrue(found2, "PostgreSQL assignment 2 not found");
                    assertTrue(found3, "PostgreSQL assignment 3 not found");
                    assertTrue(found4, "PostgreSQL assignment 4 not found");
                    assertTrue(found5, "PostgreSQL assignment 5 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all PostgreSQL 'assignment' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("PostgreSQL queryActiveByCourse results")
        void test0004() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<AssignmentRec> all = AssignmentLogic.POSTGRES
                            .queryActiveByCourse(cache, RawRecordConstants.M117, null);

                    assertEquals(4, all.size(), "Incorrect record count from PostgreSQL queryActiveByCourse");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;

                    for (final AssignmentRec r : all) {
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

                    assertTrue(found1, "PostgreSQL assignment 1 not found");
                    assertTrue(found2, "PostgreSQL assignment 2 not found");
                    assertTrue(found3, "PostgreSQL assignment 3 not found");
                    assertTrue(found4, "PostgreSQL assignment 4 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying PostgreSQL 'assignment' rows by course: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("PostgreSQL query results")
        void test0008() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final AssignmentRec r = AssignmentLogic.POSTGRES.query(cache, "1718H");

                    assertNotNull(r, "No record returned by PostgreSQL query");

                    if (!RAW1.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying PostgreSQL 'assignment' by assignmentId: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("PostgreSQL delete results")
        void test0009() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final boolean result = AssignmentLogic.POSTGRES.delete(cache, RAW2);
                    assertTrue(result, "PostgreSQL delete returned false");

                    final List<AssignmentRec> all = AssignmentLogic.POSTGRES.queryAll(cache);

                    assertEquals(4, all.size(), "Incorrect record count from queryAll after PostgreSQL delete");

                    boolean found1 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found5 = false;

                    for (final AssignmentRec r : all) {
                        if (RAW1.equals(r)) {
                            found1 = true;
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

                    assertTrue(found1, "PostgreSQL assignment 1 not found");
                    assertTrue(found3, "PostgreSQL assignment 3 not found");
                    assertTrue(found4, "PostgreSQL assignment 4 not found");
                    assertTrue(found5, "PostgreSQL assignment 5 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting PostgreSQL 'assignment' row: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM main_t.assignment");
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
