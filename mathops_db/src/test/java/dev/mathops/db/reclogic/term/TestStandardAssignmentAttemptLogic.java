package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbUse;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Facet;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.rec.term.StandardAssignmentAttemptRec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code StandardAssignmentAttemptLogic} class.
 */
final class TestStandardAssignmentAttemptLogic {

    /** A raw test record. */
    private static final StandardAssignmentAttemptRec RAW1 =
            new StandardAssignmentAttemptRec(Integer.valueOf(111222331), "876543111", "HW-117-6.3",
                    LocalDate.of(2025, Month.MARCH, 4), Integer.valueOf(43200), "MATH 117", Integer.valueOf(6),
                    Integer.valueOf(3), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(4), "N");

    /** A raw test record. */
    private static final StandardAssignmentAttemptRec RAW2 =
            new StandardAssignmentAttemptRec(Integer.valueOf(111222332), "876543111", "HW-117-6.3",
                    LocalDate.of(2025, Month.MARCH, 5), Integer.valueOf(44200), "MATH 117", Integer.valueOf(6),
                    Integer.valueOf(3), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(9), "Y");

    /** A raw test record. */
    private static final StandardAssignmentAttemptRec RAW3 =
            new StandardAssignmentAttemptRec(Integer.valueOf(111222333), "876543222", "MA-118-4.2",
                    LocalDate.of(2025, Month.MARCH, 6), Integer.valueOf(43300), "MATH 118", Integer.valueOf(4),
                    Integer.valueOf(2), Integer.valueOf(15), Integer.valueOf(10), Integer.valueOf(12), "Y");

    /** A raw test record. */
    private static final StandardAssignmentAttemptRec UPD3 =
            new StandardAssignmentAttemptRec(Integer.valueOf(111222333), "876543333", "MA-125-5.3",
                    LocalDate.of(2025, Month.MARCH, 7), Integer.valueOf(43400), "MATH 125", Integer.valueOf(5),
                    Integer.valueOf(3), Integer.valueOf(25), Integer.valueOf(20), Integer.valueOf(22), "P");

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StandardAssignmentAttemptRec r) {

        Log.warning("Unexpected serial number ", r.serialNbr);
        Log.warning("Unexpected student ID ", r.studentId);
        Log.warning("Unexpected assignment iD ", r.assignmentId);
        Log.warning("Unexpected attempt date ", r.attemptDate);
        Log.warning("Unexpected attempt time sec. ", r.attemptTimeSec);
        Log.warning("Unexpected course ID ", r.courseId);
        Log.warning("Unexpected module number ", r.moduleNbr);
        Log.warning("Unexpected standard number ", r.standardNbr);
        Log.warning("Unexpected pts possible ", r.ptsPossible);
        Log.warning("Unexpected min passing score ", r.minPassingScore);
        Log.warning("Unexpected score ", r.score);
        Log.warning("Unexpected passed ", r.passed);
    }

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        profile = config.getCodeProfile(Contexts.POSTGRES_TEST_PATH);
        if (profile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGTEST_PROFILE));
        }
        login = profile.getLogin(ESchema.LEGACY);
        if (login == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGPRIMARY_CONTEXT));
        }

        // Make sure the connection is accessing the TEST database
        final Facet facet = profile.getFacet(ESchema.LEGACY);
        if (facet.data.use != EDbUse.TEST) {
            throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, facet.data.use));
        }

        final Cache cache = new Cache(profile);
        final String prefix = cache.getSchemaPrefix(ESchema.TERM);
        if (prefix == null) {
            fail(TestRes.get(TestRes.ERR_NO_TERM_PREFIX));
        }

        final DbConnection conn = login.checkOutConnection();

        final String sql = "SELECT descr FROM " + prefix + ".which_db";

        try {
            try (final Statement stmt = conn.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {

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
                stmt.executeUpdate("DELETE FROM " + prefix + ".standard_assignment_attempt");
            }
            conn.commit();

            assertTrue(StandardAssignmentAttemptLogic.INSTANCE.insert(cache, RAW1),
                    "Failed to insert standard assignment attempt");
            assertTrue(StandardAssignmentAttemptLogic.INSTANCE.insert(cache, RAW2),
                    "Failed to insert standard assignment attempt");
            assertTrue(StandardAssignmentAttemptLogic.INSTANCE.insert(cache, RAW3),
                    "Failed to insert standard assignment attempt");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'standard_assignment_attempt' table: " + ex.getMessage());
            throw new IllegalArgumentException(ex);
        } finally {
            login.checkInConnection(conn);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0001() {
        final Cache cache = new Cache(profile);

        try {
            final List<StandardAssignmentAttemptRec> all = StandardAssignmentAttemptLogic.INSTANCE.queryAll(
                    cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final StandardAssignmentAttemptRec r : all) {
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

            assertTrue(found1, "standard_assignment_attempt 1 not found");
            assertTrue(found2, "standard_assignment_attempt 2 not found");
            assertTrue(found3, "standard_assignment_attempt 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'standard_assignment_attempt' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final StandardAssignmentAttemptRec r = StandardAssignmentAttemptLogic.INSTANCE.query(cache, RAW1.serialNbr);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying standard_assignment_attempt: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            final List<StandardAssignmentAttemptRec> all = StandardAssignmentAttemptLogic.INSTANCE.queryByStudent(cache,
                    RAW1.studentId);

            assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

            boolean found1 = false;
            boolean found2 = false;

            for (final StandardAssignmentAttemptRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (RAW2.equals(r)) {
                    found2 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "standard_assignment_attempt 1 not found");
            assertTrue(found2, "standard_assignment_attempt 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying standard_assignment_attempt: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            if (StandardAssignmentAttemptLogic.INSTANCE.update(cache, UPD3)) {
                final StandardAssignmentAttemptRec r = StandardAssignmentAttemptLogic.INSTANCE.query(cache,
                        UPD3.serialNbr);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD3.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of standard_assignment_attempt");
                }
            } else {
                fail("Failed to update standard_assignment_attempt row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating standard_assignment_attempt: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = StandardAssignmentAttemptLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<StandardAssignmentAttemptRec> all = StandardAssignmentAttemptLogic.INSTANCE.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final StandardAssignmentAttemptRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (UPD3.equals(r)) {
                    found3 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "standard_assignment_attempt 1 not found");
            assertTrue(found3, "standard_assignment_attempt 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting standard_assignment_attempts: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Cache cache = new Cache(profile);
        final String prefix = cache.getSchemaPrefix(ESchema.TERM);
        if (prefix == null) {
            fail(TestRes.get(TestRes.ERR_NO_TERM_PREFIX));
        }

        try {
            final DbConnection conn = login.checkOutConnection();

            final String sql = "SELECT descr FROM " + prefix + ".which_db";

            try {
                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery(sql)) {

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
                    stmt.executeUpdate("DELETE FROM " + prefix + ".standard_assignment_attempt");
                }

                conn.commit();
            } finally {
                login.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        }
    }
}
