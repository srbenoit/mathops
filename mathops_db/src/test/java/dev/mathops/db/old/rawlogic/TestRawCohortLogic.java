package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawCohort;

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
 * Tests for the {@code RawCohortLogic} class.
 */
final class TestRawCohortLogic {

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
                stmt.executeUpdate("DELETE FROM cohort");
            }
            conn.commit();

            final RawCohort raw1 = new RawCohort("ABCDEFGH", Integer.valueOf(100), "Alice");
            final RawCohort raw2 = new RawCohort("IJKLMNOP", Integer.valueOf(101), "Bob");

            assertTrue(RawCohortLogic.insert(cache, raw1), "Failed to insert cohort");
            assertTrue(RawCohortLogic.insert(cache, raw2), "Failed to insert cohort");
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
            final List<RawCohort> all = RawCohortLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawCohort r : all) {

                if ("ABCDEFGH".equals(r.cohort)
                    && Integer.valueOf(100).equals(r.size)
                    && "Alice".equals(r.instructor)) {

                    found1 = true;

                } else if ("IJKLMNOP".equals(r.cohort)
                           && Integer.valueOf(101).equals(r.size)
                           && "Bob".equals(r.instructor)) {

                    found2 = true;

                } else {
                    Log.warning("Unexpected cohort ", r.cohort);
                    Log.warning("Unexpected size ", r.size);
                    Log.warning("Unexpected instructor ", r.instructor);
                }
            }

            assertTrue(found1, "cohort 1 not found");
            assertTrue(found2, "cohort 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all cohort rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final RawCohort rec = RawCohortLogic.query(cache, "ABCDEFGH");

            assertNotNull(rec, "No record returned after update");

            assertEquals("ABCDEFGH", rec.cohort, "Invalid cohort");

            assertEquals(Integer.valueOf(100), rec.size, "Invalid size");

            assertEquals("Alice", rec.instructor, "Invalid instructor");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all cohort rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results after updateCohortSize")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            RawCohortLogic.updateCohortSize(cache, "ABCDEFGH", Integer.valueOf(200));

            final RawCohort updated = RawCohortLogic.query(cache, "ABCDEFGH");

            assertNotNull(updated, "No record returned after update");

            assertEquals("ABCDEFGH", updated.cohort, "Invalid cohort");

            assertEquals(Integer.valueOf(200), updated.size, "Invalid size");

            assertEquals("Alice", updated.instructor, "Invalid instructor");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all cohort rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final RawCohort raw2 = new RawCohort("IJKLMNOP", Integer.valueOf(101), "Bob");

            final boolean result = RawCohortLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawCohort> all = RawCohortLogic.queryAll(cache);

            assertEquals(1, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;

            for (final RawCohort r : all) {

                if ("ABCDEFGH".equals(r.cohort)
                    && Integer.valueOf(200).equals(r.size)
                    && "Alice".equals(r.instructor)) {

                    found1 = true;

                } else {
                    Log.warning("Unexpected cohort ", r.cohort);
                    Log.warning("Unexpected size ", r.size);
                    Log.warning("Unexpected instructor ", r.instructor);
                }
            }

            assertTrue(found1, "cohort 1 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting cohort: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM cohort");
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
