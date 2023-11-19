package dev.mathops.db.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.rawrecord.RawEtextKey;
import dev.mathops.db.rawrecord.RawRecordConstants;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tests for the {@code RawEtextKeyLogic} class.
 */
final class TestRawEtextKeyLogic {

    /** A date/time used in test records. */
    private static final LocalDateTime datetime1 = LocalDateTime.of(2021, 1, 2, 3, 4, 5);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime2 = LocalDateTime.of(2020, 2, 3, 4, 5, 6);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime3 = LocalDateTime.of(2021, 11, 12, 13, 14, 15);

    /** The database profile. */
    private static DbProfile dbProfile = null;

    /** The database context. */
    private static DbContext ctx = null;

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        dbProfile = ContextMap.getDefaultInstance().getCodeProfile(Contexts.INFORMIX_TEST_PATH);
        if (dbProfile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_TEST_PROFILE));
        }

        ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);
        if (ctx == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PRIMARY_CONTEXT));
        }

        // Make sure we're in the TEST database
        try {
            final DbConnection conn = ctx.checkOutConnection();

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
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            throw new IllegalArgumentException(ex);
        }

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM etext_key");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawEtextKey raw1 = new RawEtextKey("PACE", "123456", datetime1);
                final RawEtextKey raw2 = new RawEtextKey("PACE", "789012", null);
                final RawEtextKey raw3 = new RawEtextKey(RawRecordConstants.M117, "ABC", datetime2);

                assertTrue(RawEtextKeyLogic.INSTANCE.insert(cache, raw1), "Failed to insert etext_key");
                assertTrue(RawEtextKeyLogic.INSTANCE.insert(cache, raw2), "Failed to insert etext_key");
                assertTrue(RawEtextKeyLogic.INSTANCE.insert(cache, raw3), "Failed to insert etext_key");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing tables: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0003() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawEtextKey> all = RawEtextKeyLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawEtextKey r : all) {

                    if ("PACE".equals(r.etextId) && "123456".equals(r.etextKey) && datetime1.equals(r.activeDt)) {

                        found1 = true;

                    } else if ("PACE".equals(r.etextId) && "789012".equals(r.etextKey) && r.activeDt == null) {

                        found2 = true;

                    } else if (RawRecordConstants.M117.equals(r.etextId) && "ABC".equals(r.etextKey)
                            && datetime2.equals(r.activeDt)) {

                        found3 = true;

                    } else {
                        Log.warning("Unexpected etextId ", r.etextId);
                        Log.warning("Unexpected etextKey ", r.etextKey);
                        Log.warning("Unexpected activeDt ", r.activeDt);
                    }
                }

                assertTrue(found1, "etext_key 1 not found");
                assertTrue(found2, "etext_key 2 not found");
                assertTrue(found3, "etext_key 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all etext_key rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawEtextKey r = RawEtextKeyLogic.query(cache, "ABC");

                assertNotNull(r, "No row returned by query");

                assertEquals(RawRecordConstants.M117, r.etextId, "Bad etextId");
                assertEquals("ABC", r.etextKey, "Bad etextKey");
                assertEquals(datetime2, r.activeDt, "Bad activeDt");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying etext_key row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getEtextKeyLabel results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawEtextKeyLogic.updateActiveDt(cache, "789012", datetime3), "Update activeDt failed");
                conn.commit();

                final RawEtextKey r = RawEtextKeyLogic.query(cache, "789012");

                assertNotNull(r, "No row returned by query");

                assertEquals("PACE", r.etextId, "Bad etextId");
                assertEquals("789012", r.etextKey, "Bad etextKey");
                assertEquals(datetime3, r.activeDt, "Bad activeDt");

            } finally {
                ctx.checkInConnection(conn);
            }

        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying etext_key label: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0006() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawEtextKey raw2 = new RawEtextKey("PACE", "789012", null);

                final boolean result = RawEtextKeyLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawEtextKey> all = RawEtextKeyLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawEtextKey r : all) {

                    if ("PACE".equals(r.etextId)
                            && "123456".equals(r.etextKey)
                            && datetime1.equals(r.activeDt)) {

                        found1 = true;

                    } else if (RawRecordConstants.M117.equals(r.etextId)
                            && "ABC".equals(r.etextKey)
                            && datetime2.equals(r.activeDt)) {

                        found3 = true;

                    } else {
                        Log.warning("Unexpected etextId ", r.etextId);
                        Log.warning("Unexpected etextKey ", r.etextKey);
                        Log.warning("Unexpected activeDt ", r.activeDt);
                    }
                }

                assertTrue(found1, "etext_key 1 not found");
                assertTrue(found3, "etext_key 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting etext_key: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM etext_key");
                }

                conn.commit();

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        }
    }
}
