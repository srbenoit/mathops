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
import dev.mathops.db.rec.main.CourseSurveyRec;
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
 * Tests for the {@code CourseSurveyLogic} class.
 */
final class TestCourseSurveyLogic {

    /** A raw test record. */
    private static final CourseSurveyRec RAW1 =
            new CourseSurveyRec("SURV-1", "Title 1", "Prompt 1");

    /** A raw test record. */
    private static final CourseSurveyRec RAW2 =
            new CourseSurveyRec("SURV-2", "Title 2", "Prompt 2");

    /** A raw test record. */
    private static final CourseSurveyRec RAW3 =
            new CourseSurveyRec("SURV-3", "Title 3", "Prompt 3");

    /** A raw test record. */
    private static final CourseSurveyRec UPD3 =
            new CourseSurveyRec("SURV-3", "Title 3 Revised", "Prompt 3 Revised");

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final CourseSurveyRec r) {

        Log.warning("Unexpected survey ID ", r.surveyId);
        Log.warning("Unexpected survey title ", r.surveyTitle);
        Log.warning("Unexpected prompt HTML ", r.promptHtml);
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
                stmt.executeUpdate("DELETE FROM " + prefix + ".course_survey");
            }
            conn.commit();

            assertTrue(CourseSurveyLogic.INSTANCE.insert(cache, RAW1), "Failed to insert course survey");
            assertTrue(CourseSurveyLogic.INSTANCE.insert(cache, RAW2), "Failed to insert course survey");
            assertTrue(CourseSurveyLogic.INSTANCE.insert(cache, RAW3), "Failed to insert course survey");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'course_survey' table: " + ex.getMessage());
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
            final List<CourseSurveyRec> all = CourseSurveyLogic.INSTANCE.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final CourseSurveyRec r : all) {
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

            assertTrue(found1, "course_survey 1 not found");
            assertTrue(found2, "course_survey 2 not found");
            assertTrue(found3, "course_survey 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'course_survey' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final CourseSurveyRec r = CourseSurveyLogic.INSTANCE.query(cache, RAW1.surveyId);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying course_survey: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            if (CourseSurveyLogic.INSTANCE.update(cache, UPD3)) {
                final CourseSurveyRec r = CourseSurveyLogic.INSTANCE.query(cache, UPD3.surveyId);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD3.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of course_survey");
                }
            } else {
                fail("Failed to update course_survey row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating course_survey: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = CourseSurveyLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<CourseSurveyRec> all = CourseSurveyLogic.INSTANCE.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final CourseSurveyRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (UPD3.equals(r)) {
                    found3 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "course_survey 1 not found");
            assertTrue(found3, "course_survey 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting course_surveys: " + ex.getMessage());
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
                    stmt.executeUpdate("DELETE FROM " + prefix + ".course_survey");
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
