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
import dev.mathops.db.rec.term.StandardsCourseGradingSystemRec;
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
 * Tests for the {@code StandardsCourseGradingSystemLogic} class.
 */
final class TestStandardsCourseGradingSystemLogic {

    /** A raw test record. */
    private static final StandardsCourseGradingSystemRec RAW1 =
            new StandardsCourseGradingSystemRec("SYS001", Integer.valueOf(24), Integer.valueOf(18), Integer.valueOf(6),
                    Integer.valueOf(5), Integer.valueOf(0), Integer.valueOf(5), Integer.valueOf(4),
                    Integer.valueOf(108), Integer.valueOf(96), Integer.valueOf(84), Integer.valueOf(72),
                    Integer.valueOf(12), Integer.valueOf(3));

    /** A raw test record. */
    private static final StandardsCourseGradingSystemRec RAW2 =
            new StandardsCourseGradingSystemRec("SYS002", Integer.valueOf(48), Integer.valueOf(36), Integer.valueOf(12),
                    Integer.valueOf(10), Integer.valueOf(1), Integer.valueOf(10), Integer.valueOf(8),
                    Integer.valueOf(216), Integer.valueOf(192), Integer.valueOf(168), Integer.valueOf(144),
                    Integer.valueOf(24), Integer.valueOf(6));

    /** A raw test record. */
    private static final StandardsCourseGradingSystemRec RAW3 =
            new StandardsCourseGradingSystemRec("SYS003", Integer.valueOf(12), Integer.valueOf(9), Integer.valueOf(3),
                    Integer.valueOf(2), Integer.valueOf(10), Integer.valueOf(100), Integer.valueOf(80),
                    Integer.valueOf(1000), Integer.valueOf(900), Integer.valueOf(800), Integer.valueOf(700),
                    Integer.valueOf(200), Integer.valueOf(2));

    /** A raw test record. */
    private static final StandardsCourseGradingSystemRec UPD3 =
            new StandardsCourseGradingSystemRec("SYS003", Integer.valueOf(13), Integer.valueOf(10), Integer.valueOf(4),
                    Integer.valueOf(3), Integer.valueOf(11), Integer.valueOf(101), Integer.valueOf(81),
                    Integer.valueOf(1001), Integer.valueOf(901), Integer.valueOf(801), Integer.valueOf(701),
                    Integer.valueOf(201), Integer.valueOf(3));

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StandardsCourseGradingSystemRec r) {

        Log.warning("Unexpected grading system ID ", r.gradingSystemId);
        Log.warning("Unexpected number of standards ", r.nbrStandards);
        Log.warning("Unexpected minimum standards ", r.minStandards);
        Log.warning("Unexpected number of essential standards ", r.nbrEssentialStandards);
        Log.warning("Unexpected minimum essential standards ", r.minEssentialStandards);
        Log.warning("Unexpected homework points ", r.homeworkPts);
        Log.warning("Unexpected on-time mastery points ", r.onTimeMasteryPts);
        Log.warning("Unexpected mastery points ", r.lateMasteryPts);
        Log.warning("Unexpected a min score ", r.aMinScore);
        Log.warning("Unexpected b min score ", r.bMinScore);
        Log.warning("Unexpected c min score ", r.cMinScore);
        Log.warning("Unexpected d min score ", r.dMinScore);
        Log.warning("Unexpected u min score ", r.uMinScore);
        Log.warning("Unexpected min standards for inc ", r.minStandardsForInc);
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
                stmt.executeUpdate("DELETE FROM " + prefix + ".standards_course_grading_system");
            }
            conn.commit();

            assertTrue(StandardsCourseGradingSystemLogic.INSTANCE.insert(cache, RAW1),
                    "Failed to insert standards course GradingSystem");
            assertTrue(StandardsCourseGradingSystemLogic.INSTANCE.insert(cache, RAW2),
                    "Failed to insert standards course GradingSystem");
            assertTrue(StandardsCourseGradingSystemLogic.INSTANCE.insert(cache, RAW3),
                    "Failed to insert standards course GradingSystem");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'standards_course_grading_system' table: " + ex.getMessage());
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
            final List<StandardsCourseGradingSystemRec> all = StandardsCourseGradingSystemLogic.INSTANCE.queryAll(
                    cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final StandardsCourseGradingSystemRec r : all) {
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

            assertTrue(found1, "standards_course_grading_system 1 not found");
            assertTrue(found2, "standards_course_grading_system 2 not found");
            assertTrue(found3, "standards_course_grading_system 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'standards_course_grading_system' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final StandardsCourseGradingSystemRec r = StandardsCourseGradingSystemLogic.INSTANCE.query(cache,
                    RAW1.gradingSystemId);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying standards_course_grading_system: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            if (StandardsCourseGradingSystemLogic.INSTANCE.update(cache, UPD3)) {
                final StandardsCourseGradingSystemRec r = StandardsCourseGradingSystemLogic.INSTANCE.query(cache,
                        UPD3.gradingSystemId);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD3.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of standards_course_grading_system");
                }
            } else {
                fail("Failed to update standards_course_grading_system row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating standards_course_grading_system: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = StandardsCourseGradingSystemLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<StandardsCourseGradingSystemRec> all = StandardsCourseGradingSystemLogic.INSTANCE.queryAll(
                    cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final StandardsCourseGradingSystemRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (UPD3.equals(r)) {
                    found3 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "standards_course_grading_system 1 not found");
            assertTrue(found3, "standards_course_grading_system 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting standards_course_grading_systems: " + ex.getMessage());
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
                            throw new IllegalArgumentException(
                                    TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                        }
                    } else {
                        throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                    }
                }

                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM " + prefix + ".standards_course_grading_system");
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
