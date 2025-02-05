package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawResource;
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
 * Tests for the {@code RawResourceLogic} class.
 */
final class TestRawResourceLogic {

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
        final Cache cache = new Cache(profile);

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
                stmt.executeUpdate("DELETE FROM resource");
            }
            conn.commit();

            final RawResource raw1 = new RawResource("res1", "RC", "Calculator 1", Integer.valueOf(1),
                    Integer.valueOf(2), "01");

            final RawResource raw2 = new RawResource("res2", "RC", "Calculator 2", Integer.valueOf(2),
                    Integer.valueOf(3), "02");

            final RawResource raw3 = new RawResource("res3", "RL", "Lock 1", Integer.valueOf(3),
                    Integer.valueOf(4), "03");

            assertTrue(RawResourceLogic.insert(cache, raw1), "Failed to insert resource 1");
            assertTrue(RawResourceLogic.insert(cache, raw2), "Failed to insert resource 2");
            assertTrue(RawResourceLogic.insert(cache, raw3), "Failed to insert resource 3");
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
            final List<RawResource> all = RawResourceLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawResource test : all) {
                if ("res1".equals(test.resourceId)
                    && "RC".equals(test.resourceType)
                    && "Calculator 1".equals(test.resourceDesc)
                    && Integer.valueOf(1).equals(test.daysAllowed)
                    && Integer.valueOf(2).equals(test.holdsAllowed)
                    && "01".equals(test.holdId)) {

                    found1 = true;
                } else if ("res2".equals(test.resourceId)
                           && "RC".equals(test.resourceType)
                           && "Calculator 2".equals(test.resourceDesc)
                           && Integer.valueOf(2).equals(test.daysAllowed)
                           && Integer.valueOf(3).equals(test.holdsAllowed)
                           && "02".equals(test.holdId)) {

                    found2 = true;
                } else if ("res3".equals(test.resourceId)
                           && "RL".equals(test.resourceType)
                           && "Lock 1".equals(test.resourceDesc)
                           && Integer.valueOf(3).equals(test.daysAllowed)
                           && Integer.valueOf(4).equals(test.holdsAllowed)
                           && "03".equals(test.holdId)) {

                    found3 = true;
                } else {
                    Log.warning("Unexpected resourceId ", test.resourceId);
                    Log.warning("Unexpected resourceType ", test.resourceType);
                    Log.warning("Unexpected resourceDesc ", test.resourceDesc);
                    Log.warning("Unexpected daysAllowed ", test.daysAllowed);
                    Log.warning("Unexpected holdsAllowed ", test.holdsAllowed);
                    Log.warning("Unexpected holdId ", test.holdId);
                }
            }

            assertTrue(found1, "Resource 1 not found");
            assertTrue(found2, "Resource 2 not found");
            assertTrue(found3, "Resource 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all resource rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final RawResource test = RawResourceLogic.query(cache, "res1");

            assertNotNull(test, "query returned no record");

            final boolean found = "res1".equals(test.resourceId)
                                  && "RC".equals(test.resourceType)
                                  && "Calculator 1".equals(test.resourceDesc)
                                  && Integer.valueOf(1).equals(test.daysAllowed)
                                  && Integer.valueOf(2).equals(test.holdsAllowed)
                                  && "01".equals(test.holdId);

            assertTrue(found, "Resource not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying resource: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final RawResource raw2 = new RawResource("res2", "RC", "Calculator 2", Integer.valueOf(2),
                    Integer.valueOf(3), "02");

            final boolean result = RawResourceLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawResource> all = RawResourceLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final RawResource test : all) {
                if ("res1".equals(test.resourceId)
                    && "RC".equals(test.resourceType)
                    && "Calculator 1".equals(test.resourceDesc)
                    && Integer.valueOf(1).equals(test.daysAllowed)
                    && Integer.valueOf(2).equals(test.holdsAllowed)
                    && "01".equals(test.holdId)) {

                    found1 = true;
                } else if ("res3".equals(test.resourceId)
                           && "RL".equals(test.resourceType)
                           && "Lock 1".equals(test.resourceDesc)
                           && Integer.valueOf(3).equals(test.daysAllowed)
                           && Integer.valueOf(4).equals(test.holdsAllowed)
                           && "03".equals(test.holdId)) {

                    found3 = true;
                } else {
                    Log.warning("Unexpected resourceId ", test.resourceId);
                    Log.warning("Unexpected resourceType ", test.resourceType);
                    Log.warning("Unexpected resourceDesc ", test.resourceDesc);
                    Log.warning("Unexpected daysAllowed ", test.daysAllowed);
                    Log.warning("Unexpected holdsAllowed ", test.holdsAllowed);
                    Log.warning("Unexpected holdId ", test.holdId);
                }
            }

            assertTrue(found1, "resource 1 not found");
            assertTrue(found3, "resource 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting resource: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM resource");
            }

            conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning table: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }
}
