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
import dev.mathops.db.rec.term.LtiContextRec;
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
 * Tests for the {@code LtiCourseLogic} class.
 */
final class TestLtiContextLogic {

    /** A raw test record. */
    private static final LtiContextRec RAW1 =
            new LtiContextRec("CLIENT_111", "issuer.1", "deploy.1", "context.1", "lms_course.1", "lms_title.1");

    /** A raw test record. */
    private static final LtiContextRec RAW2 =
            new LtiContextRec("CLIENT_222", "issuer.2", "deploy.2", "context.2", "lms_course.2", "lms_title.2");

    /** A raw test record. */
    private static final LtiContextRec RAW3 =
            new LtiContextRec("CLIENT_333", "issuer.3", "deploy.3", "context.3", "lms_course.3", "lms_title.3");

    /** A raw test record. */
    private static final LtiContextRec RAW4 =
            new LtiContextRec("CLIENT_444", "issuer.4", "deploy.4", "context.4", "lms_course.4", "lms_title.4");

    /** A raw test record. */
    private static final LtiContextRec RAW5 =
            new LtiContextRec("CLIENT_555", "issuer.5", "deploy.5", "context.5", "lms_course.5", "lms_title.5");

    /** A raw test record. */
    private static final LtiContextRec UPD5 =
            new LtiContextRec("CLIENT_555", "issuer.5", "deploy.5", "context.5", "lms_course.5a", "lms_title.5a");

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final LtiContextRec r) {

        Log.warning("Unexpected clientId ", r.clientId);
        Log.warning("Unexpected issuer ", r.issuer);
        Log.warning("Unexpected deployment ID ", r.deploymentId);
        Log.warning("Unexpected context ID ", r.contextId);
        Log.warning("Unexpected LMS course ID ", r.lmsCourseId);
        Log.warning("Unexpected LMS course title ", r.lmsCourseTitle);
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
                stmt.executeUpdate("DELETE FROM " + prefix + ".lti_context");
            }
            conn.commit();

            assertTrue(LtiContextLogic.INSTANCE.insert(cache, RAW1), "Failed to insert lti_context");
            assertTrue(LtiContextLogic.INSTANCE.insert(cache, RAW2), "Failed to insert lti_context");
            assertTrue(LtiContextLogic.INSTANCE.insert(cache, RAW3), "Failed to insert lti_context");
            assertTrue(LtiContextLogic.INSTANCE.insert(cache, RAW4), "Failed to insert lti_context");
            assertTrue(LtiContextLogic.INSTANCE.insert(cache, RAW5), "Failed to insert lti_context");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'lti_context' table: " + ex.getMessage());
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
            final List<LtiContextRec> all = LtiContextLogic.INSTANCE.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final LtiContextRec r : all) {
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

            assertTrue(found1, "lti_context 1 not found");
            assertTrue(found2, "lti_context 2 not found");
            assertTrue(found3, "lti_context 3 not found");
            assertTrue(found4, "lti_context 4 not found");
            assertTrue(found5, "lti_context 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'lti_context' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final LtiContextRec r = LtiContextLogic.INSTANCE.query(cache, RAW1.clientId, RAW1.issuer, RAW1.deploymentId,
                    RAW1.contextId);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying lti_context: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            if (LtiContextLogic.INSTANCE.update(cache, UPD5)) {
                final LtiContextRec r = LtiContextLogic.INSTANCE.query(cache, UPD5.clientId, UPD5.issuer,
                        UPD5.deploymentId, UPD5.contextId);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD5.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of lti_context");
                }
            } else {
                fail("Failed to update lti_context row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating lti_context: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = LtiContextLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<LtiContextRec> all = LtiContextLogic.INSTANCE.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final LtiContextRec r : all) {
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

            assertTrue(found1, "lti_context 1 not found");
            assertTrue(found3, "lti_context 3 not found");
            assertTrue(found4, "lti_context 4 not found");
            assertTrue(found5, "lti_context 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting lti_contexts: " + ex.getMessage());
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
                    stmt.executeUpdate("DELETE FROM " + prefix + ".lti_context");
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
