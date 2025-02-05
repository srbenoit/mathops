package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawNewstu;
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
 * Tests for the {@code RawNewstuLogic} class.
 */
final class TestRawNewstuLogic {

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
                stmt.executeUpdate("DELETE FROM newstu");
            }
            conn.commit();

            final RawNewstu raw1 = new RawNewstu("111111111", "A", "B", "C");
            final RawNewstu raw2 = new RawNewstu("222222222", "D", "E", "F");

            assertTrue(RawNewstuLogic.insert(cache, raw1), "Failed to insert newstu 1");
            assertTrue(RawNewstuLogic.insert(cache, raw2), "Failed to insert newstu 2");
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
            final List<RawNewstu> all = RawNewstuLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawNewstu test : all) {
                if ("111111111".equals(test.stuId)
                    && "A".equals(test.acadLevel)
                    && "B".equals(test.regType)
                    && "C".equals(test.term)) {

                    found1 = true;
                } else if ("222222222".equals(test.stuId)
                           && "D".equals(test.acadLevel)
                           && "E".equals(test.regType)
                           && "F".equals(test.term)) {

                    found2 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected acadLevel ", test.acadLevel);
                    Log.warning("Unexpected regType ", test.regType);
                    Log.warning("Unexpected term ", test.term);
                }
            }

            assertTrue(found1, "Newstu 1 not found");
            assertTrue(found2, "Newstu 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all newstu rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {

        final Cache cache = new Cache(profile);

        final RawNewstu raw2 = new RawNewstu("222222222", "D", "E", "F");

        try {
            final boolean result = RawNewstuLogic.delete(cache, raw2);

            assertTrue(result, "delete returned false");

            final List<RawNewstu> all = RawNewstuLogic.queryAll(cache);

            assertEquals(1, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;

            for (final RawNewstu test : all) {
                if ("111111111".equals(test.stuId)
                    && "A".equals(test.acadLevel)
                    && "B".equals(test.regType)
                    && "C".equals(test.term)) {

                    found1 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected acadLevel ", test.acadLevel);
                    Log.warning("Unexpected regType ", test.regType);
                    Log.warning("Unexpected term ", test.term);
                }
            }

            assertTrue(found1, "Newstu 1 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleteing a newstu row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("deleteAll results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final int count = RawNewstuLogic.deleteAll(cache);

            assertEquals(1, count, "Incorrect record count from deleteAll");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleteing all newstu rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawNewstu> all = RawNewstuLogic.queryAll(cache);

            assertEquals(0, all.size(), "Incorrect record count from queryAll");
        } catch (
                final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all newstu rows: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM newstu");
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
