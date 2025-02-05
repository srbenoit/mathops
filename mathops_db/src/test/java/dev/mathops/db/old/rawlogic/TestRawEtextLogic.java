package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawEtext;
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
 * Tests for the {@code RawEtextLogic} class.
 */
final class TestRawEtextLogic {

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
                stmt.executeUpdate("DELETE FROM etext");
            }
            conn.commit();

            final RawEtext raw1 = new RawEtext("ID1", "Y", "URL1", Integer.valueOf(10), "Y", "N", "Button 1");
            final RawEtext raw2 = new RawEtext("ID2", "C", "URL2", Integer.valueOf(2), "N", "Y", "Button 2");

            assertTrue(RawEtextLogic.insert(cache, raw1), "Failed to insert etext");
            assertTrue(RawEtextLogic.insert(cache, raw2), "Failed to insert etext");
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
            final List<RawEtext> all = RawEtextLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawEtext r : all) {

                if ("ID1".equals(r.etextId)
                    && "Y".equals(r.retention)
                    && "URL1".equals(r.purchaseUrl)
                    && Integer.valueOf(10).equals(r.refundPeriod)
                    && "Y".equals(r.keyEntry)
                    && "N".equals(r.active)
                    && "Button 1".equals(r.buttonLabel)) {

                    found1 = true;

                } else if ("ID2".equals(r.etextId)
                           && "C".equals(r.retention)
                           && "URL2".equals(r.purchaseUrl)
                           && Integer.valueOf(2).equals(r.refundPeriod)
                           && "N".equals(r.keyEntry)
                           && "Y".equals(r.active)
                           && "Button 2".equals(r.buttonLabel)) {

                    found2 = true;
                } else {
                    Log.warning("Unexpected etextId ", r.etextId);
                    Log.warning("Unexpected retention ", r.retention);
                    Log.warning("Unexpected purchaseUrl ", r.purchaseUrl);
                    Log.warning("Unexpected refundPeriod ", r.refundPeriod);
                    Log.warning("Unexpected keyEntry ", r.keyEntry);
                    Log.warning("Unexpected active ", r.active);
                    Log.warning("Unexpected buttonLabel ", r.buttonLabel);
                }
            }

            assertTrue(found1, "etext 1 not found");
            assertTrue(found2, "etext 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all etext rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final RawEtext r = RawEtextLogic.query(cache, "ID1");

            assertNotNull(r, "No row returned by query");

            final boolean found1 = "ID1".equals(r.etextId)
                                   && "Y".equals(r.retention)
                                   && "URL1".equals(r.purchaseUrl)
                                   && Integer.valueOf(10).equals(r.refundPeriod)
                                   && "Y".equals(r.keyEntry)
                                   && "N".equals(r.active)
                                   && "Button 1".equals(r.buttonLabel);

            assertTrue(found1, "Etext 1 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying etext row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final RawEtext raw2 = new RawEtext("ID2", "C", "URL2", Integer.valueOf(2), "N", "Y", "Button 2");

            final boolean result = RawEtextLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawEtext> all = RawEtextLogic.queryAll(cache);

            assertEquals(1, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;

            for (final RawEtext r : all) {

                if ("ID1".equals(r.etextId)
                    && "Y".equals(r.retention)
                    && "URL1".equals(r.purchaseUrl)
                    && Integer.valueOf(10).equals(r.refundPeriod)
                    && "Y".equals(r.keyEntry)
                    && "N".equals(r.active)
                    && "Button 1".equals(r.buttonLabel)) {

                    found1 = true;
                } else {
                    Log.warning("Unexpected etextId ", r.etextId);
                    Log.warning("Unexpected retention ", r.retention);
                    Log.warning("Unexpected purchaseUrl ", r.purchaseUrl);
                    Log.warning("Unexpected refundPeriod ", r.refundPeriod);
                    Log.warning("Unexpected keyEntry ", r.keyEntry);
                    Log.warning("Unexpected active ", r.active);
                    Log.warning("Unexpected buttonLabel ", r.buttonLabel);
                }
            }

            assertTrue(found1, "etext 1 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting etext: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM etext");
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
