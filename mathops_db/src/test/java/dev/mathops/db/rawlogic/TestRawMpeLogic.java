package dev.mathops.db.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.rawrecord.RawMpe;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Tests for the {@code RawMpeLogic} class.
 */
final class TestRawMpeLogic {

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
                    stmt.executeUpdate("DELETE FROM mpe");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawMpe raw1 = new RawMpe("MPTTC", Integer.valueOf(0), Integer.valueOf(2));
                final RawMpe raw2 = new RawMpe("MPTUN", Integer.valueOf(1), Integer.valueOf(0));
                final RawMpe raw3 = new RawMpe("MPTPU", Integer.valueOf(0), Integer.valueOf(1));

                assertTrue(RawMpeLogic.INSTANCE.insert(cache, raw1), "Failed to insert mpe 1");
                assertTrue(RawMpeLogic.INSTANCE.insert(cache, raw2), "Failed to insert mpe 2");
                assertTrue(RawMpeLogic.INSTANCE.insert(cache, raw3), "Failed to insert mpe 3");
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
                final List<RawMpe> all = RawMpeLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawMpe test : all) {

                    if ("MPTTC".equals(test.version)
                            && Integer.valueOf(0).equals(test.maxOnlineAtmpts)
                            && Integer.valueOf(2).equals(test.maxProctoredAtmpts)) {

                        found1 = true;
                    } else if ("MPTUN".equals(test.version)
                            && Integer.valueOf(1).equals(test.maxOnlineAtmpts)
                            && Integer.valueOf(0).equals(test.maxProctoredAtmpts)) {

                        found2 = true;
                    } else if ("MPTPU".equals(test.version)
                            && Integer.valueOf(0).equals(test.maxOnlineAtmpts)
                            && Integer.valueOf(1).equals(test.maxProctoredAtmpts)) {

                        found3 = true;

                    } else {
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected maxOnlineAtmpts ", test.maxOnlineAtmpts);
                        Log.warning("Unexpected maxProctoredAtmpts ", test.maxProctoredAtmpts);
                    }
                }

                assertTrue(found1, "mpe 1 not found");
                assertTrue(found2, "mpe 2 not found");
                assertTrue(found3, "mpe 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all mpe rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawMpe raw2 = new RawMpe("MPTUN", Integer.valueOf(1), Integer.valueOf(0));

                final boolean result = RawMpeLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawMpe> all = RawMpeLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawMpe test : all) {

                    if ("MPTTC".equals(test.version)
                            && Integer.valueOf(0).equals(test.maxOnlineAtmpts)
                            && Integer.valueOf(2).equals(test.maxProctoredAtmpts)) {

                        found1 = true;
                    } else if ("MPTPU".equals(test.version)
                            && Integer.valueOf(0).equals(test.maxOnlineAtmpts)
                            && Integer.valueOf(1).equals(test.maxProctoredAtmpts)) {

                        found3 = true;

                    } else {
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected maxOnlineAtmpts ", test.maxOnlineAtmpts);
                        Log.warning("Unexpected maxProctoredAtmpts ", test.maxProctoredAtmpts);
                    }
                }

                assertTrue(found1, "mpe 1 not found");
                assertTrue(found3, "mpe 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting mpe: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM mpe");
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
