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
import dev.mathops.db.rec.main.StandardsCourseModuleRec;
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
 * Tests for the {@code StandardsCourseModuleLogic} class.
 */
final class TestStandardsCourseModuleLogic {

    /** A raw test record. */
    private static final StandardsCourseModuleRec RAW1 =
            new StandardsCourseModuleRec("MATH 117", Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(2),
                    "02_alg/01_numbers");

    /** A raw test record. */
    private static final StandardsCourseModuleRec RAW2 =
            new StandardsCourseModuleRec("MATH 101", Integer.valueOf(2), Integer.valueOf(5), Integer.valueOf(4),
                    "01_gen/02_data");

    /** A raw test record. */
    private static final StandardsCourseModuleRec RAW3 =
            new StandardsCourseModuleRec("MATH 160", Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(3),
                    "06_calc/03_deriv_apps");

    /** A raw test record. */
    private static final StandardsCourseModuleRec RAW4 =
            new StandardsCourseModuleRec("MATH 160", Integer.valueOf(4), Integer.valueOf(6), Integer.valueOf(5),
                    "06_calc/04_antidiff");

    /** A raw test record. */
    private static final StandardsCourseModuleRec UPD4 =
            new StandardsCourseModuleRec("MATH 160", Integer.valueOf(4), Integer.valueOf(7), Integer.valueOf(6),
                    "06_calc/04_integrals");

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StandardsCourseModuleRec r) {

        Log.warning("Unexpected course ID ", r.courseId);
        Log.warning("Unexpected module number ", r.moduleNbr);
        Log.warning("Unexpected number of standards ", r.nbrStandards);
        Log.warning("Unexpected number of essential standards ", r.nbrEssential);
        Log.warning("Unexpected module path ", r.modulePath);
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
                stmt.executeUpdate("DELETE FROM " + prefix + ".standards_course_module");
            }
            conn.commit();

            assertTrue(StandardsCourseModuleLogic.INSTANCE.insert(cache, RAW1),
                    "Failed to insert standards course module");
            assertTrue(StandardsCourseModuleLogic.INSTANCE.insert(cache, RAW2),
                    "Failed to insert standards course module");
            assertTrue(StandardsCourseModuleLogic.INSTANCE.insert(cache, RAW3),
                    "Failed to insert standards course module");
            assertTrue(StandardsCourseModuleLogic.INSTANCE.insert(cache, RAW4),
                    "Failed to insert standards course module");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'standards_course_module' table: " + ex.getMessage());
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
            final List<StandardsCourseModuleRec> all = StandardsCourseModuleLogic.INSTANCE.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final StandardsCourseModuleRec r : all) {
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

            assertTrue(found1, "standards_course_module 1 not found");
            assertTrue(found2, "standards_course_module 2 not found");
            assertTrue(found3, "standards_course_module 3 not found");
            assertTrue(found4, "standards_course_module 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'standards_course_module' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final StandardsCourseModuleRec r = StandardsCourseModuleLogic.INSTANCE.query(cache, RAW1.courseId,
                    RAW1.moduleNbr);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying standards_course_module: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByCourse results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            final List<StandardsCourseModuleRec> all = StandardsCourseModuleLogic.INSTANCE.queryByCourse(cache,
                    RAW3.courseId);

            assertEquals(2, all.size(), "Incorrect record count from queryByCourse");

            boolean found3 = false;
            boolean found4 = false;

            for (final StandardsCourseModuleRec r : all) {
                if (RAW3.equals(r)) {
                    found3 = true;
                } else if (RAW4.equals(r)) {
                    found4 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found3, "standards_course_module 3 not found");
            assertTrue(found4, "standards_course_module 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying standards_course_module: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            if (StandardsCourseModuleLogic.INSTANCE.update(cache, UPD4)) {
                final StandardsCourseModuleRec r = StandardsCourseModuleLogic.INSTANCE.query(cache, UPD4.courseId,
                        UPD4.moduleNbr);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD4.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of standards_course_module");
                }
            } else {
                fail("Failed to update standards_course_module row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating standards_course_module: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = StandardsCourseModuleLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<StandardsCourseModuleRec> all = StandardsCourseModuleLogic.INSTANCE.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final StandardsCourseModuleRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (RAW3.equals(r)) {
                    found3 = true;
                } else if (UPD4.equals(r)) {
                    found4 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "standards_course_module 1 not found");
            assertTrue(found3, "standards_course_module 3 not found");
            assertTrue(found4, "standards_course_module 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting standards_course_modules: " + ex.getMessage());
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
                    stmt.executeUpdate("DELETE FROM " + prefix + ".standards_course_module");
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
