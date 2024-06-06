package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.logic.DbConnection;
import dev.mathops.db.logic.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawHoldType;

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
import java.util.List;

/**
 * Tests for the {@code RawHoldTypeLogic} class.
 */
final class TestRawHoldTypeLogic {

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
                    stmt.executeUpdate("DELETE FROM hold_type");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawHoldType raw1 = new RawHoldType("AA", "B", "C", "D", "E");
                final RawHoldType raw2 = new RawHoldType("FF", "G", "H", "I", "J");

                assertTrue(RawHoldTypeLogic.INSTANCE.insert(cache, raw1), "Failed to insert hold_type");
                assertTrue(RawHoldTypeLogic.INSTANCE.insert(cache, raw2), "Failed to insert hold_type");
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
                final List<RawHoldType> all = RawHoldTypeLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawHoldType r : all) {

                    if ("AA".equals(r.holdId)
                            && "B".equals(r.sevAdminHold)
                            && "C".equals(r.holdType)
                            && "D".equals(r.addHold)
                            && "E".equals(r.deleteHold)) {

                        found1 = true;

                    } else if ("FF".equals(r.holdId)
                            && "G".equals(r.sevAdminHold)
                            && "H".equals(r.holdType)
                            && "I".equals(r.addHold)
                            && "J".equals(r.deleteHold)) {

                        found2 = true;

                    } else {
                        Log.warning("Unexpected holdId ", r.holdId);
                        Log.warning("Unexpected sevAdminHold ", r.sevAdminHold);
                        Log.warning("Unexpected holdType ", r.holdType);
                        Log.warning("Unexpected addHold ", r.addHold);
                        Log.warning("Unexpected deleteHold ", r.deleteHold);
                    }
                }

                assertTrue(found1, "HoldType 1 not found");
                assertTrue(found2, "HoldType 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all hold_type rows: " + ex.getMessage());
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
                final RawHoldType rec = RawHoldTypeLogic.query(cache, "AA");

                assertNotNull(rec, "No record returned by query");

                final boolean found = "AA".equals(rec.holdId)
                        && "B".equals(rec.sevAdminHold)
                        && "C".equals(rec.holdType)
                        && "D".equals(rec.addHold)
                        && "E".equals(rec.deleteHold);

                assertTrue(found, "HoldType not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all hold_type rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawHoldType raw2 = new RawHoldType("FF", "G", "H", "I", "J");

                final boolean result = RawHoldTypeLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawHoldType> all = RawHoldTypeLogic.INSTANCE.queryAll(cache);

                assertEquals(1, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;

                for (final RawHoldType r : all) {

                    if ("AA".equals(r.holdId)
                            && "B".equals(r.sevAdminHold)
                            && "C".equals(r.holdType)
                            && "D".equals(r.addHold)
                            && "E".equals(r.deleteHold)) {

                        found1 = true;

                    } else {
                        Log.warning("Unexpected holdId ", r.holdId);
                        Log.warning("Unexpected sevAdminHold ", r.sevAdminHold);
                        Log.warning("Unexpected holdType ", r.holdType);
                        Log.warning("Unexpected addHold ", r.addHold);
                        Log.warning("Unexpected deleteHold ", r.deleteHold);
                    }
                }

                assertTrue(found1, "hold_type 1 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting hold_type: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM hold_type");
                }

                conn.commit();

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning table: " + ex.getMessage());
        }
    }
}
