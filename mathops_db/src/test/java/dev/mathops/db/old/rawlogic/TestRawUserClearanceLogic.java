package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawUserClearance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawUserClearanceLogic} class.
 */
final class TestRawUserClearanceLogic {

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
                stmt.executeUpdate("DELETE FROM user_clearance");
            }
            conn.commit();

            final RawUserClearance raw1 = new RawUserClearance("user01", "fxn001", Integer.valueOf(1), "pwd01");
            final RawUserClearance raw2 = new RawUserClearance("user01", "fxn002", Integer.valueOf(2), "pwd02");
            final RawUserClearance raw3 = new RawUserClearance("user02", "fxn003", Integer.valueOf(3), "pwd03");

            assertTrue(RawUserClearanceLogic.insert(cache, raw1), "Failed to insert user_clearance 1");
            assertTrue(RawUserClearanceLogic.insert(cache, raw2), "Failed to insert user_clearance 2");
            assertTrue(RawUserClearanceLogic.insert(cache, raw3), "Failed to insert user_clearance 3");
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
            final List<RawUserClearance> all = RawUserClearanceLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawUserClearance test : all) {

                if ("user01".equals(test.login) && "fxn001".equals(test.clearFunction)
                    && Integer.valueOf(1).equals(test.clearType) && "pwd01".equals(test.clearPasswd)) {

                    found1 = true;

                } else if ("user01".equals(test.login) && "fxn002".equals(test.clearFunction)
                           && Integer.valueOf(2).equals(test.clearType) && "pwd02".equals(test.clearPasswd)) {

                    found2 = true;

                } else if ("user02".equals(test.login) && "fxn003".equals(test.clearFunction)
                           && Integer.valueOf(3).equals(test.clearType) && "pwd03".equals(test.clearPasswd)) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected login ", test.login);
                    Log.warning("Unexpected clearFunction ", test.clearFunction);
                    Log.warning("Unexpected clearType ", test.clearType);
                    Log.warning("Unexpected clearPasswd ", test.clearPasswd);
                }
            }

            assertTrue(found1, "user clearance 1 not found");
            assertTrue(found2, "user clearance 2 not found");
            assertTrue(found3, "user clearance 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all user_clearance rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAllForLogin results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawUserClearance> all = RawUserClearanceLogic.queryAllForLogin(cache, "user01");

            assertEquals(2, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawUserClearance test : all) {

                if ("user01".equals(test.login) && "fxn001".equals(test.clearFunction)
                    && Integer.valueOf(1).equals(test.clearType) && "pwd01".equals(test.clearPasswd)) {

                    found1 = true;

                } else if ("user01".equals(test.login) && "fxn002".equals(test.clearFunction)
                           && Integer.valueOf(2).equals(test.clearType) && "pwd02".equals(test.clearPasswd)) {

                    found2 = true;

                } else {
                    Log.warning("Unexpected login ", test.login);
                    Log.warning("Unexpected clearFunction ", test.clearFunction);
                    Log.warning("Unexpected clearType ", test.clearType);
                    Log.warning("Unexpected clearPasswd ", test.clearPasswd);
                }
            }

            assertTrue(found1, "user clearance 1 not found");
            assertTrue(found2, "user clearance 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all user_clearance rows for login: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final RawUserClearance raw2 = new RawUserClearance("user01", "fxn002", Integer.valueOf(2), "pwd02");

            final boolean result = RawUserClearanceLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawUserClearance> all = RawUserClearanceLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final RawUserClearance test : all) {

                if ("user01".equals(test.login) && "fxn001".equals(test.clearFunction)
                    && Integer.valueOf(1).equals(test.clearType) && "pwd01".equals(test.clearPasswd)) {

                    found1 = true;

                } else if ("user02".equals(test.login) && "fxn003".equals(test.clearFunction)
                           && Integer.valueOf(3).equals(test.clearType) && "pwd03".equals(test.clearPasswd)) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected login ", test.login);
                    Log.warning("Unexpected clearFunction ", test.clearFunction);
                    Log.warning("Unexpected clearType ", test.clearType);
                    Log.warning("Unexpected clearPasswd ", test.clearPasswd);
                }
            }

            assertTrue(found1, "user clearance 1 not found");
            assertTrue(found3, "user clearance 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting user_clearance: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM user_clearance");
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
