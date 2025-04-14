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
import dev.mathops.db.rec.main.CourseSurveyItemRec;
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
 * Tests for the {@code CourseSurveyItemLogic} class.
 */
final class TestCourseSurveyItemLogic {

    /** A raw test record. */
    private static final CourseSurveyItemRec RAW1 =
            new CourseSurveyItemRec("SURV-1", Integer.valueOf(1), Integer.valueOf(2), "Prompt 1");

    /** A raw test record. */
    private static final CourseSurveyItemRec RAW2 =
            new CourseSurveyItemRec("SURV-1", Integer.valueOf(2), Integer.valueOf(3), "Prompt 2");

    /** A raw test record. */
    private static final CourseSurveyItemRec RAW3 =
            new CourseSurveyItemRec("SURV-2", Integer.valueOf(3), Integer.valueOf(4), "Prompt 3");

    /** A raw test record. */
    private static final CourseSurveyItemRec UPD3 =
            new CourseSurveyItemRec("SURV-2", Integer.valueOf(3), Integer.valueOf(5), "Prompt 4");

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final CourseSurveyItemRec r) {

        Log.warning("Unexpected survey ID ", r.surveyId);
        Log.warning("Unexpected item number ", r.itemNbr);
        Log.warning("Unexpected item type ", r.itemType);
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
                stmt.executeUpdate("DELETE FROM " + prefix + ".course_survey_item");
            }
            conn.commit();

            assertTrue(CourseSurveyItemLogic.INSTANCE.insert(cache, RAW1), "Failed to insert course survey item");
            assertTrue(CourseSurveyItemLogic.INSTANCE.insert(cache, RAW2), "Failed to insert course survey item");
            assertTrue(CourseSurveyItemLogic.INSTANCE.insert(cache, RAW3), "Failed to insert course survey item");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'course_survey_item' table: " + ex.getMessage());
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
            final List<CourseSurveyItemRec> all = CourseSurveyItemLogic.INSTANCE.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final CourseSurveyItemRec r : all) {
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

            assertTrue(found1, "course_survey_item 1 not found");
            assertTrue(found2, "course_survey_item 2 not found");
            assertTrue(found3, "course_survey_item 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'course_survey_item' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final CourseSurveyItemRec r = CourseSurveyItemLogic.INSTANCE.query(cache, RAW1.surveyId, RAW1.itemNbr);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying course_survey_item: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryBySurvey results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            final List<CourseSurveyItemRec> all = CourseSurveyItemLogic.INSTANCE.queryBySurveyId(cache, RAW1.surveyId);

            assertEquals(2, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;

            for (final CourseSurveyItemRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (RAW2.equals(r)) {
                    found2 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "course_survey_item 1 not found");
            assertTrue(found2, "course_survey_item 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'course_survey_item' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            if (CourseSurveyItemLogic.INSTANCE.update(cache, UPD3)) {
                final CourseSurveyItemRec r = CourseSurveyItemLogic.INSTANCE.query(cache, UPD3.surveyId, UPD3.itemNbr);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD3.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of course_survey_item");
                }
            } else {
                fail("Failed to update course_survey_item row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating course_survey_item: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = CourseSurveyItemLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<CourseSurveyItemRec> all = CourseSurveyItemLogic.INSTANCE.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final CourseSurveyItemRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (UPD3.equals(r)) {
                    found3 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "course_survey_item 1 not found");
            assertTrue(found3, "course_survey_item 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting course_survey_items: " + ex.getMessage());
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
                    stmt.executeUpdate("DELETE FROM " + prefix + ".course_survey_item");
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
