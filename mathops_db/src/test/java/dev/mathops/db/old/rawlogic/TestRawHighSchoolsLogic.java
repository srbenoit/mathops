package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawHighSchools;
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
 * Tests for the {@code RawHighSchoolsLogic} class.
 */
final class TestRawHighSchoolsLogic {

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
                stmt.executeUpdate("DELETE FROM high_schools");
            }
            conn.commit();

            final RawHighSchools raw1 = new RawHighSchools("HS1", "High School 1", "Addr 1", "City 1", "S1",
                    "ZIP01");

            final RawHighSchools raw2 = new RawHighSchools("HS2", "High School 2", "Addr 2", "City 2", "S2",
                    "ZIP02");

            final RawHighSchools raw3 = new RawHighSchools("HS3", "High School 3", "Addr 3", "City 3", "S3",
                    "ZIP03");

            assertTrue(RawHighSchoolsLogic.insert(cache, raw1), "Failed to insert high_schools 1");
            assertTrue(RawHighSchoolsLogic.insert(cache, raw2), "Failed to insert high_schools 2");
            assertTrue(RawHighSchoolsLogic.insert(cache, raw3), "Failed to insert high_schools 3");
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
            final List<RawHighSchools> all = RawHighSchoolsLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawHighSchools test : all) {
                if ("HS1".equals(test.hsCode)
                    && "High School 1".equals(test.hsName)
                    && "Addr 1".equals(test.addres1)
                    && "City 1".equals(test.city)
                    && "S1".equals(test.state)
                    && "ZIP01".equals(test.zipCode)) {

                    found1 = true;
                } else if ("HS2".equals(test.hsCode)
                           && "High School 2".equals(test.hsName)
                           && "Addr 2".equals(test.addres1)
                           && "City 2".equals(test.city)
                           && "S2".equals(test.state)
                           && "ZIP02".equals(test.zipCode)) {

                    found2 = true;
                } else if ("HS3".equals(test.hsCode)
                           && "High School 3".equals(test.hsName)
                           && "Addr 3".equals(test.addres1)
                           && "City 3".equals(test.city)
                           && "S3".equals(test.state)
                           && "ZIP03".equals(test.zipCode)) {

                    found3 = true;
                }
            }

            assertTrue(found1, "high_schools 1 not found");
            assertTrue(found2, "high_schools 2 not found");
            assertTrue(found3, "high_schools 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all high_schools rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final RawHighSchools raw2 = new RawHighSchools("HS2", "High School 2", "Addr 2", "City 2", "S2",
                    "ZIP02");

            final boolean result = RawHighSchoolsLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawHighSchools> all = RawHighSchoolsLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final RawHighSchools test : all) {
                if ("HS1".equals(test.hsCode)
                    && "High School 1".equals(test.hsName)
                    && "Addr 1".equals(test.addres1)
                    && "City 1".equals(test.city)
                    && "S1".equals(test.state)
                    && "ZIP01".equals(test.zipCode)) {

                    found1 = true;
                } else if ("HS3".equals(test.hsCode)
                           && "High School 3".equals(test.hsName)
                           && "Addr 3".equals(test.addres1)
                           && "City 3".equals(test.city)
                           && "S3".equals(test.state)
                           && "ZIP03".equals(test.zipCode)) {

                    found3 = true;
                }
            }

            assertTrue(found1, "high_schools 1 not found");
            assertTrue(found3, "high_schools 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting high_schools: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM high_schools");
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
