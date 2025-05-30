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
import dev.mathops.db.rec.main.LtiRegistrationRec;
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
 * Tests for the {@code LtiRegistrationLogic} class.
 */
final class TestLtiRegistrationLogic {

    /** A raw test record. */
    private static final LtiRegistrationRec RAW1 =
            new LtiRegistrationRec("CLIENT_111", "issuer.1", "port1", "redirect.1", "auth.1", "token.1", "reg.1",
                    "jwks.1");

    /** A raw test record. */
    private static final LtiRegistrationRec RAW2 =
            new LtiRegistrationRec("CLIENT_222", "issuer.2", "port2", "redirect.2", "auth.2", "token.2", "reg.2",
                    "jwks.2");

    /** A raw test record. */
    private static final LtiRegistrationRec RAW3 =
            new LtiRegistrationRec("CLIENT_333", "issuer.3", "port3", "redirect.3", "auth.3", "token.3", "reg.3",
                    "jwks.3");

    /** A raw test record. */
    private static final LtiRegistrationRec RAW4 =
            new LtiRegistrationRec("CLIENT_444", "issuer.4", "port4", "redirect.4", "auth.4", "token.4", "reg.4",
                    "jwks.4");

    /** A raw test record. */
    private static final LtiRegistrationRec RAW5 =
            new LtiRegistrationRec("CLIENT_555", "issuer.5", "port5", "redirect.5", "auth.5", "token.5", "reg.5",
                    "jwks.5");

    /** A raw test record. */
    private static final LtiRegistrationRec UPD5 =
            new LtiRegistrationRec("CLIENT_555", "issuer.5", "port5a", "redirect.5a", "auth.5a", "token.5a", "reg.5a",
                    "jwks.5a");

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final LtiRegistrationRec r) {

        Log.warning("Unexpected clientId ", r.clientId);
        Log.warning("Unexpected issuer ", r.issuer);
        Log.warning("Unexpected issuer port ", r.issuerPort);
        Log.warning("Unexpected redirect uri ", r.redirectUri);
        Log.warning("Unexpected auth endpoint ", r.authEndpoint);
        Log.warning("Unexpected token endpoint ", r.tokenEndpoint);
        Log.warning("Unexpected reg endpoint ", r.regEndpoint);
        Log.warning("Unexpected JWKS URI ", r.jwksUri);
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
                stmt.executeUpdate("DELETE FROM " + prefix + ".lti_registration");
            }
            conn.commit();

            assertTrue(LtiRegistrationLogic.INSTANCE.insert(cache, RAW1), "Failed to insert lti_registration");
            assertTrue(LtiRegistrationLogic.INSTANCE.insert(cache, RAW2), "Failed to insert lti_registration");
            assertTrue(LtiRegistrationLogic.INSTANCE.insert(cache, RAW3), "Failed to insert lti_registration");
            assertTrue(LtiRegistrationLogic.INSTANCE.insert(cache, RAW4), "Failed to insert lti_registration");
            assertTrue(LtiRegistrationLogic.INSTANCE.insert(cache, RAW5), "Failed to insert lti_registration");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'lti_registration' table: " + ex.getMessage());
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
            final List<LtiRegistrationRec> all = LtiRegistrationLogic.INSTANCE.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final LtiRegistrationRec r : all) {
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

            assertTrue(found1, "lti_registration 1 not found");
            assertTrue(found2, "lti_registration 2 not found");
            assertTrue(found3, "lti_registration 3 not found");
            assertTrue(found4, "lti_registration 4 not found");
            assertTrue(found5, "lti_registration 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'lti_registration' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final LtiRegistrationRec r = LtiRegistrationLogic.INSTANCE.query(cache, RAW1.clientId, RAW1.issuer);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying lti_registration: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            if (LtiRegistrationLogic.INSTANCE.update(cache, UPD5)) {
                final LtiRegistrationRec r = LtiRegistrationLogic.INSTANCE.query(cache, UPD5.clientId, UPD5.issuer);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD5.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of lti_registration");
                }
            } else {
                fail("Failed to update lti_registration row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating lti_registration: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = LtiRegistrationLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<LtiRegistrationRec> all = LtiRegistrationLogic.INSTANCE.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final LtiRegistrationRec r : all) {
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

            assertTrue(found1, "lti_registration 1 not found");
            assertTrue(found3, "lti_registration 3 not found");
            assertTrue(found4, "lti_registration 4 not found");
            assertTrue(found5, "lti_registration 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting lti_registrations: " + ex.getMessage());
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
                    stmt.executeUpdate("DELETE FROM " + prefix + ".lti_registration");
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
