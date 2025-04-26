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
import dev.mathops.db.rec.main.StandardsCourseStandardRec;
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
 * Tests for the {@code StandardsCourseStandardLogic} class.
 */
final class TestStandardsCourseStandardLogic {

    /** A raw test record. */
    private static final StandardsCourseStandardRec RAW1 =
            new StandardsCourseStandardRec("MATH 117", Integer.valueOf(1), Integer.valueOf(3), "I can A.",
                    "Y");

    /** A raw test record. */
    private static final StandardsCourseStandardRec RAW2 =
            new StandardsCourseStandardRec("MATH 101", Integer.valueOf(2), Integer.valueOf(5), "I can B.",
                    "N");

    /** A raw test record. */
    private static final StandardsCourseStandardRec RAW3 =
            new StandardsCourseStandardRec("MATH 160", Integer.valueOf(3), Integer.valueOf(4), "I can C.",
                    "A");

    /** A raw test record. */
    private static final StandardsCourseStandardRec RAW4 =
            new StandardsCourseStandardRec("MATH 160", Integer.valueOf(4), Integer.valueOf(6), "I can D.",
                    "B");

    /** A raw test record. */
    private static final StandardsCourseStandardRec UPD4 =
            new StandardsCourseStandardRec("MATH 160", Integer.valueOf(4), Integer.valueOf(7), "I can E.",
                    "C");

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StandardsCourseStandardRec r) {

        Log.warning("Unexpected course ID ", r.courseId);
        Log.warning("Unexpected module number ", r.moduleNbr);
        Log.warning("Unexpected standard number ", r.standardNbr);
        Log.warning("Unexpected learning objective ", r.learningObjective);
        Log.warning("Unexpected essential flag ", r.isEssential);
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
                stmt.executeUpdate("DELETE FROM " + prefix + ".standards_course_standard");
            }
            conn.commit();

            assertTrue(StandardsCourseStandardLogic.INSTANCE.insert(cache, RAW1),
                    "Failed to insert standards course standard");
            assertTrue(StandardsCourseStandardLogic.INSTANCE.insert(cache, RAW2),
                    "Failed to insert standards course standard");
            assertTrue(StandardsCourseStandardLogic.INSTANCE.insert(cache, RAW3),
                    "Failed to insert standards course standard");
            assertTrue(StandardsCourseStandardLogic.INSTANCE.insert(cache, RAW4),
                    "Failed to insert standards course standard");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'standards_course_standard' table: " + ex.getMessage());
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
            final List<StandardsCourseStandardRec> all = StandardsCourseStandardLogic.INSTANCE.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final StandardsCourseStandardRec r : all) {
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

            assertTrue(found1, "standards_course_standard 1 not found");
            assertTrue(found2, "standards_course_standard 2 not found");
            assertTrue(found3, "standards_course_standard 3 not found");
            assertTrue(found4, "standards_course_standard 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'standards_course_standard' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final StandardsCourseStandardRec r = StandardsCourseStandardLogic.INSTANCE.query(cache, RAW1.courseId,
                    RAW1.moduleNbr, RAW1.standardNbr);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying standards_course_standard: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByCourse results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            final List<StandardsCourseStandardRec> all = StandardsCourseStandardLogic.INSTANCE.queryByCourse(cache,
                    RAW3.courseId);

            assertEquals(2, all.size(), "Incorrect record count from queryByCourse");

            boolean found3 = false;
            boolean found4 = false;

            for (final StandardsCourseStandardRec r : all) {
                if (RAW3.equals(r)) {
                    found3 = true;
                } else if (RAW4.equals(r)) {
                    found4 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found3, "standards_course_standard 3 not found");
            assertTrue(found4, "standards_course_standard 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying standards_course_standard: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            if (StandardsCourseStandardLogic.INSTANCE.update(cache, UPD4)) {
                final StandardsCourseStandardRec r = StandardsCourseStandardLogic.INSTANCE.query(cache, UPD4.courseId,
                        UPD4.moduleNbr, UPD4.standardNbr);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD4.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of standards_course_standard");
                }
            } else {
                fail("Failed to update standards_course_standard row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating standards_course_standard: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = StandardsCourseStandardLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<StandardsCourseStandardRec> all = StandardsCourseStandardLogic.INSTANCE.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final StandardsCourseStandardRec r : all) {
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

            assertTrue(found1, "standards_course_standard 1 not found");
            assertTrue(found3, "standards_course_standard 3 not found");
            assertTrue(found4, "standards_course_standard 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting standards_course_standards: " + ex.getMessage());
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
                    stmt.executeUpdate("DELETE FROM " + prefix + ".standards_course_standard");
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
