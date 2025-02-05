package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawTreePath;
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
 * Tests for the {@code RawTreePathLogic} class.
 */
final class TestRawTreePathLogic {

    /** The database profile. */
    private static Profile profile = null;

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        profile = config.getCodeProfile(Contexts.INFORMIX_TEST_PATH);
        if (profile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_TEST_PROFILE));
        }

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        // Make sure we're in the TEST database
        try {
            try (final Statement stmt = conn.createStatement();
                 final ResultSet rs = stmt.executeQuery("SELECT descr FROM which_db")) {

                if (rs.next()) {
                    final String which = rs.getString(1);
                    if (which != null && !"TEST".equals(which.trim())) {
                        throw new IllegalArgumentException(
                                TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                    }
                } else {
                    throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                }
            }

            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM tree_path");
            }
            conn.commit();

            final Cache cache = new Cache(profile);

            final RawTreePath raw1 = new RawTreePath("iso", CoreConstants.SLASH, Integer.valueOf(0),
                    Integer.valueOf(1), "ISO");
            final RawTreePath raw2 = new RawTreePath("org", "iso", Integer.valueOf(1), Integer.valueOf(3),
                    "IDENTIFIED-ORGANIZATION");
            final RawTreePath raw3 = new RawTreePath("nist", "org", Integer.valueOf(2), Integer.valueOf(5), "NIST");
            final RawTreePath raw4 = new RawTreePath("dod", "org", Integer.valueOf(2), Integer.valueOf(6), "DOD");
            final RawTreePath raw5 = new RawTreePath("internet", "dod", Integer.valueOf(3), Integer.valueOf(1),
                    "INTERNET");
            final RawTreePath raw6 = new RawTreePath("private", "internet", Integer.valueOf(4), Integer.valueOf(4),
                    "PRIVATE");

            assertTrue(RawTreePathLogic.insert(cache, raw1), "Failed to insert tree_path");
            assertTrue(RawTreePathLogic.insert(cache, raw2), "Failed to insert tree_path");
            assertTrue(RawTreePathLogic.insert(cache, raw3), "Failed to insert tree_path");
            assertTrue(RawTreePathLogic.insert(cache, raw4), "Failed to insert tree_path");
            assertTrue(RawTreePathLogic.insert(cache, raw5), "Failed to insert tree_path");
            assertTrue(RawTreePathLogic.insert(cache, raw6), "Failed to insert tree_path");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing tables: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0003() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawTreePath> all = RawTreePathLogic.queryAll(cache);

            assertEquals(6, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;
            boolean found6 = false;

            for (final RawTreePath r : all) {

                if ("iso".equals(r.ident)
                    && CoreConstants.SLASH.equals(r.parentIdent)
                    && Integer.valueOf(0).equals(r.depth)
                    && Integer.valueOf(1).equals(r.sortOrder)
                    && "ISO".equals(r.label)) {

                    found1 = true;

                } else if ("org".equals(r.ident)
                           && "iso".equals(r.parentIdent)
                           && Integer.valueOf(1).equals(r.depth)
                           && Integer.valueOf(3).equals(r.sortOrder)
                           && "IDENTIFIED-ORGANIZATION".equals(r.label)) {

                    found2 = true;

                } else if ("nist".equals(r.ident)
                           && "org".equals(r.parentIdent)
                           && Integer.valueOf(2).equals(r.depth)
                           && Integer.valueOf(5).equals(r.sortOrder)
                           && "NIST".equals(r.label)) {

                    found3 = true;

                } else if ("dod".equals(r.ident)
                           && "org".equals(r.parentIdent)
                           && Integer.valueOf(2).equals(r.depth)
                           && Integer.valueOf(6).equals(r.sortOrder)
                           && "DOD".equals(r.label)) {

                    found4 = true;

                } else if ("internet".equals(r.ident)
                           && "dod".equals(r.parentIdent)
                           && Integer.valueOf(3).equals(r.depth)
                           && Integer.valueOf(1).equals(r.sortOrder)
                           && "INTERNET".equals(r.label)) {

                    found5 = true;

                } else if ("private".equals(r.ident)
                           && "internet".equals(r.parentIdent)
                           && Integer.valueOf(4).equals(r.depth)
                           && Integer.valueOf(4).equals(r.sortOrder)
                           && "PRIVATE".equals(r.label)) {

                    found6 = true;

                } else {
                    Log.warning("Unexpected ident ", r.ident);
                    Log.warning("Unexpected parentIdent ", r.parentIdent);
                    Log.warning("Unexpected depth ", r.depth);
                    Log.warning("Unexpected sortOrder ", r.sortOrder);
                    Log.warning("Unexpected label ", r.label);
                }
            }

            assertTrue(found1, "tree_path 1 not found");
            assertTrue(found2, "tree_path 2 not found");
            assertTrue(found3, "tree_path 3 not found");
            assertTrue(found4, "tree_path 4 not found");
            assertTrue(found5, "tree_path 5 not found");
            assertTrue(found6, "tree_path 6 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all tree_path rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByDepthAndParent results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawTreePath> all = RawTreePathLogic.queryByDepthAndParent(cache, 2, "org");

            assertEquals(2, all.size(), "Incorrect record count from queryAll");

            boolean found3 = false;
            boolean found4 = false;

            for (final RawTreePath r : all) {

                if ("nist".equals(r.ident)
                    && "org".equals(r.parentIdent)
                    && Integer.valueOf(2).equals(r.depth)
                    && Integer.valueOf(5).equals(r.sortOrder)
                    && "NIST".equals(r.label)) {

                    found3 = true;

                } else if ("dod".equals(r.ident)
                           && "org".equals(r.parentIdent)
                           && Integer.valueOf(2).equals(r.depth)
                           && Integer.valueOf(6).equals(r.sortOrder)
                           && "DOD".equals(r.label)) {

                    found4 = true;

                } else {
                    Log.warning("Unexpected ident ", r.ident);
                    Log.warning("Unexpected parentIdent ", r.parentIdent);
                    Log.warning("Unexpected depth ", r.depth);
                    Log.warning("Unexpected sortOrder ", r.sortOrder);
                    Log.warning("Unexpected label ", r.label);
                }
            }

            assertTrue(found3, "tree_path 3 not found");
            assertTrue(found4, "tree_path 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying tree_path rows by depth, parent: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByCourse results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final RawTreePath rec = RawTreePathLogic.query(cache, "dod", 2, "org");

            assertNotNull(rec, "Null result from query");

            final boolean found = "dod".equals(rec.ident)
                                  && "org".equals(rec.parentIdent)
                                  && Integer.valueOf(2).equals(rec.depth)
                                  && Integer.valueOf(6).equals(rec.sortOrder)
                                  && "DOD".equals(rec.label);

            assertTrue(found, "tree_path 1 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all tree_path rows for course: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateSortOrder")
    void test0006() {

        final Cache cache = new Cache(profile);

        final RawTreePath toUpdate = new RawTreePath("dod", "org", Integer.valueOf(2), Integer.valueOf(60), null);

        try {
            assertTrue(RawTreePathLogic.updateSortOrder(cache, toUpdate, Integer.valueOf(60)),
                    "False result from updateSortOrder");

            final RawTreePath rec = RawTreePathLogic.query(cache, "dod", 2, "org");

            assertNotNull(rec, "Null result from query");

            final boolean found = "dod".equals(rec.ident)
                                  && "org".equals(rec.parentIdent)
                                  && Integer.valueOf(2).equals(rec.depth)
                                  && Integer.valueOf(60).equals(rec.sortOrder)
                                  && "DOD".equals(rec.label);

            assertTrue(found, "tree_path not valid after update sort order");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating sort order" + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateLabel")
    void test0007() {

        final Cache cache = new Cache(profile);

        final RawTreePath toUpdate = new RawTreePath("dod", "org", Integer.valueOf(2), Integer.valueOf(60), null);

        try {
            assertTrue(RawTreePathLogic.updateLabel(cache, toUpdate, "DoD"), "False result from update label");

            final RawTreePath rec = RawTreePathLogic.query(cache, "dod", 2, "org");

            assertNotNull(rec, "Null result from query");

            final boolean found = "dod".equals(rec.ident)
                                  && "org".equals(rec.parentIdent)
                                  && Integer.valueOf(2).equals(rec.depth)
                                  && Integer.valueOf(60).equals(rec.sortOrder)
                                  && "DoD".equals(rec.label);

            assertTrue(found, "tree_path not valid after update label");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating label" + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0008() {

        final Cache cache = new Cache(profile);

        try {
            final RawTreePath raw2 = new RawTreePath("org", "iso", Integer.valueOf(1), Integer.valueOf(3),
                    "IDENTIFIED-ORGANIZATION");

            final boolean result = RawTreePathLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawTreePath> all = RawTreePathLogic.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;
            boolean found6 = false;

            for (final RawTreePath r : all) {

                if ("iso".equals(r.ident)
                    && CoreConstants.SLASH.equals(r.parentIdent)
                    && Integer.valueOf(0).equals(r.depth)
                    && Integer.valueOf(1).equals(r.sortOrder)
                    && "ISO".equals(r.label)) {

                    found1 = true;

                } else if ("nist".equals(r.ident)
                           && "org".equals(r.parentIdent)
                           && Integer.valueOf(2).equals(r.depth)
                           && Integer.valueOf(5).equals(r.sortOrder)
                           && "NIST".equals(r.label)) {

                    found3 = true;

                } else if ("dod".equals(r.ident)
                           && "org".equals(r.parentIdent)
                           && Integer.valueOf(2).equals(r.depth)
                           && Integer.valueOf(60).equals(r.sortOrder)
                           && "DoD".equals(r.label)) {

                    found4 = true;

                } else if ("internet".equals(r.ident)
                           && "dod".equals(r.parentIdent)
                           && Integer.valueOf(3).equals(r.depth)
                           && Integer.valueOf(1).equals(r.sortOrder)
                           && "INTERNET".equals(r.label)) {

                    found5 = true;

                } else if ("private".equals(r.ident)
                           && "internet".equals(r.parentIdent)
                           && Integer.valueOf(4).equals(r.depth)
                           && Integer.valueOf(4).equals(r.sortOrder)
                           && "PRIVATE".equals(r.label)) {

                    found6 = true;

                } else {
                    Log.warning("Unexpected ident ", r.ident);
                    Log.warning("Unexpected parentIdent ", r.parentIdent);
                    Log.warning("Unexpected depth ", r.depth);
                    Log.warning("Unexpected sortOrder ", r.sortOrder);
                    Log.warning("Unexpected label ", r.label);
                }
            }

            assertTrue(found1, "tree_path 1 not found");
            assertTrue(found3, "tree_path 3 not found");
            assertTrue(found4, "tree_path 4 not found");
            assertTrue(found5, "tree_path 5 not found");
            assertTrue(found6, "tree_path 6 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting users: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM tree_path");
            }

            conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }
}
