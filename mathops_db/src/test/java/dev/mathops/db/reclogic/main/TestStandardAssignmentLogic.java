package dev.mathops.db.reclogic.main;

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
import dev.mathops.db.rec.main.StandardAssignmentRec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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
 * Tests for the {@code StandardAssignmentLogic} class.
 */
final class TestStandardAssignmentLogic {

    /** A raw test record. */
    private static final StandardAssignmentRec RAW1 =
            new StandardAssignmentRec("HW-117-1.2", "HW", "MATH 117", Integer.valueOf(1), Integer.valueOf(2),
                    Integer.valueOf(100), Integer.valueOf(80), "tree.ref.hw.1.2");

    /** A raw test record. */
    private static final StandardAssignmentRec RAW2 =
            new StandardAssignmentRec("HW-117-1.3", "HW", "MATH 117", Integer.valueOf(1), Integer.valueOf(3),
                    Integer.valueOf(10), Integer.valueOf(9), "tree.ref.hw.1.3");

    /** A raw test record. */
    private static final StandardAssignmentRec RAW3 =
            new StandardAssignmentRec("HW-117-2.1", "HW", "MATH 117", Integer.valueOf(2), Integer.valueOf(1),
                    Integer.valueOf(50), Integer.valueOf(40), "tree.ref.hw.2.1");

    /** A raw test record. */
    private static final StandardAssignmentRec RAW4 =
            new StandardAssignmentRec("MA-117-2.3", "MA", "MATH 117", Integer.valueOf(2), Integer.valueOf(3),
                    Integer.valueOf(1000), Integer.valueOf(876), "tree.ref.ma.2.3");

    /** A raw test record. */
    private static final StandardAssignmentRec RAW5 =
            new StandardAssignmentRec("MA-118-2.3", "MA", "MATH 118", Integer.valueOf(2), Integer.valueOf(3),
                    Integer.valueOf(1000), Integer.valueOf(876), "tree.ref.ma.2.3");

    /** A raw test record. */
    private static final StandardAssignmentRec UPD5 =
            new StandardAssignmentRec("MA-118-2.3", "MX", "MATH 125", Integer.valueOf(3), Integer.valueOf(4),
                    Integer.valueOf(500), Integer.valueOf(438), "tree.ref.mx.3.4");

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StandardAssignmentRec r) {

        Log.warning("Unexpected assignment ID ", r.assignmentId);
        Log.warning("Unexpected assignment type ", r.assignmentType);
        Log.warning("Unexpected course ID ", r.courseId);
        Log.warning("Unexpected module number ", r.moduleNbr);
        Log.warning("Unexpected standard number ", r.standardNbr);
        Log.warning("Unexpected points possible ", r.ptsPossible);
        Log.warning("Unexpected min passing score ", r.minPassingScore);
        Log.warning("Unexpected tree ref ", r.treeRef);
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
        final String prefix = cache.getSchemaPrefix(ESchema.MAIN);
        if (prefix == null) {
            fail(TestRes.get(TestRes.ERR_NO_MAIN_PREFIX));
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
                stmt.executeUpdate("DELETE FROM " + prefix + ".standard_assignment");
            }
            conn.commit();

            assertTrue(StandardAssignmentLogic.INSTANCE.insert(cache, RAW1), "Failed to insert standard assignment");
            assertTrue(StandardAssignmentLogic.INSTANCE.insert(cache, RAW2), "Failed to insert standard assignment");
            assertTrue(StandardAssignmentLogic.INSTANCE.insert(cache, RAW3), "Failed to insert standard assignment");
            assertTrue(StandardAssignmentLogic.INSTANCE.insert(cache, RAW4), "Failed to insert standard assignment");
            assertTrue(StandardAssignmentLogic.INSTANCE.insert(cache, RAW5), "Failed to insert standard assignment");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'standard_assignment' table: " + ex.getMessage());
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
            final List<StandardAssignmentRec> all = StandardAssignmentLogic.INSTANCE.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final StandardAssignmentRec r : all) {
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

            assertTrue(found1, "standard_assignment 1 not found");
            assertTrue(found2, "standard_assignment 2 not found");
            assertTrue(found3, "standard_assignment 3 not found");
            assertTrue(found4, "standard_assignment 4 not found");
            assertTrue(found5, "standard_assignment 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'standard_assignment' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final StandardAssignmentRec r = StandardAssignmentLogic.INSTANCE.query(cache, RAW1.assignmentId);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying standard_assignment: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByCourse results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            final List<StandardAssignmentRec> all = StandardAssignmentLogic.INSTANCE.queryByCourse(cache,
                    RAW1.courseId);

            assertEquals(4, all.size(), "Incorrect record count from queryByCourse");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final StandardAssignmentRec r : all) {
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

            assertTrue(found1, "standard_assignment 1 not found");
            assertTrue(found2, "standard_assignment 2 not found");
            assertTrue(found3, "standard_assignment 3 not found");
            assertTrue(found4, "standard_assignment 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying standard_assignment: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            if (StandardAssignmentLogic.INSTANCE.update(cache, UPD5)) {
                final StandardAssignmentRec r = StandardAssignmentLogic.INSTANCE.query(cache, UPD5.assignmentId);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD5.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of standard_assignment");
                }
            } else {
                fail("Failed to update standard_assignment row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating standard_assignment: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = StandardAssignmentLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<StandardAssignmentRec> all = StandardAssignmentLogic.INSTANCE.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final StandardAssignmentRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (RAW3.equals(r)) {
                    found3 = true;
                } else if (RAW4.equals(r)) {
                    found4 = true;
                } else if (UPD5.equals(r)) {
                    found5 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "standard_assignment 1 not found");
            assertTrue(found3, "standard_assignment 3 not found");
            assertTrue(found4, "standard_assignment 4 not found");
            assertTrue(found5, "standard_assignment 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting standard_assignments: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Cache cache = new Cache(profile);
        final String prefix = cache.getSchemaPrefix(ESchema.MAIN);
        if (prefix == null) {
            fail(TestRes.get(TestRes.ERR_NO_MAIN_PREFIX));
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
                            throw new IllegalArgumentException(
                                    TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                        }
                    } else {
                        throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                    }
                }

                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM " + prefix + ".standard_assignment");
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
