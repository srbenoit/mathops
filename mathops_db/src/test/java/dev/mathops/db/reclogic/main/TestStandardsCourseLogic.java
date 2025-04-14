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
import dev.mathops.db.rec.main.StandardsCourseRec;
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
 * Tests for the {@code StandardsCourseLogic} class.
 */
final class TestStandardsCourseLogic {

    /** A raw test record. */
    private static final StandardsCourseRec RAW1 =
            new StandardsCourseRec("MATH 117", "College Algebra I", Integer.valueOf(8), Integer.valueOf(1),
                    Integer.valueOf(99), "02_alg/MATH_117.metadata");

    /** A raw test record. */
    private static final StandardsCourseRec RAW2 =
            new StandardsCourseRec("MATH 101", "Math in the Social Sciences", Integer.valueOf(12), Integer.valueOf(3),
                    Integer.valueOf(45), "01_gen/MATH_101.metadata");

    /** A raw test record. */
    private static final StandardsCourseRec RAW3 =
            new StandardsCourseRec("MATH 160", "Calculus I", Integer.valueOf(16), Integer.valueOf(4),
                    Integer.valueOf(22), "06_calc/MATH_160.metadata");

    /** A raw test record. */
    private static final StandardsCourseRec UPD3 =
            new StandardsCourseRec("MATH 160", "Calculus for Physical Scientists I", Integer.valueOf(15),
                    Integer.valueOf(5), Integer.valueOf(23), "07_calc/MATH_160.metadata");

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StandardsCourseRec r) {

        Log.warning("Unexpected course ID ", r.courseId);
        Log.warning("Unexpected course title ", r.courseTitle);
        Log.warning("Unexpected number of modules ", r.nbrModules);
        Log.warning("Unexpected number of credits ", r.nbrCredits);
        Log.warning("Unexpected allowed lends ", r.allowLend);
        Log.warning("Unexpected metadata path ", r.metadataPath);
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
                stmt.executeUpdate("DELETE FROM " + prefix + ".standards_course");
            }
            conn.commit();

            assertTrue(StandardsCourseLogic.INSTANCE.insert(cache, RAW1), "Failed to insert standards course");
            assertTrue(StandardsCourseLogic.INSTANCE.insert(cache, RAW2), "Failed to insert standards course");
            assertTrue(StandardsCourseLogic.INSTANCE.insert(cache, RAW3), "Failed to insert standards course");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'standards_course' table: " + ex.getMessage());
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
            final List<StandardsCourseRec> all = StandardsCourseLogic.INSTANCE.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final StandardsCourseRec r : all) {
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

            assertTrue(found1, "standards_course 1 not found");
            assertTrue(found2, "standards_course 2 not found");
            assertTrue(found3, "standards_course 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'standards_course' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final StandardsCourseRec r = StandardsCourseLogic.INSTANCE.query(cache, RAW1.courseId);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying standards_course: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            if (StandardsCourseLogic.INSTANCE.update(cache, UPD3)) {
                final StandardsCourseRec r = StandardsCourseLogic.INSTANCE.query(cache, UPD3.courseId);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD3.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of standards_course");
                }
            } else {
                fail("Failed to update standards_course row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating standards_course: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = StandardsCourseLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<StandardsCourseRec> all = StandardsCourseLogic.INSTANCE.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final StandardsCourseRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (UPD3.equals(r)) {
                    found3 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "standards_course 1 not found");
            assertTrue(found3, "standards_course 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting standards_courses: " + ex.getMessage());
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
                    stmt.executeUpdate("DELETE FROM " + prefix + ".standards_course");
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
