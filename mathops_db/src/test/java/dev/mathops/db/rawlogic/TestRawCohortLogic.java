package dev.mathops.db.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.rawrecord.RawCohort;

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
                    stmt.executeUpdate("DELETE FROM cohort");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawCohort raw1 = new RawCohort("ABCDEFGH", Integer.valueOf(100), "Alice");
                final RawCohort raw2 = new RawCohort("IJKLMNOP", Integer.valueOf(101), "Bob");

                assertTrue(RawCohortLogic.INSTANCE.insert(cache, raw1), "Failed to insert cohort");
                assertTrue(RawCohortLogic.INSTANCE.insert(cache, raw2), "Failed to insert cohort");
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
                final List<RawCohort> all = RawCohortLogic.INSTANCE.queryAll(cache);

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

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all cohort rows: " + ex.getMessage());
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
                final RawCohort rec = RawCohortLogic.query(cache, "ABCDEFGH");

                assertNotNull(rec, "No record returned after update");

                assertEquals("ABCDEFGH", rec.cohort, "Invalid cohort");

                assertEquals(Integer.valueOf(100), rec.size, "Invalid size");

                assertEquals("Alice", rec.instructor, "Invalid instructor");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all cohort rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results after updateCohortSize")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                RawCohortLogic.updateCohortSize(cache, "ABCDEFGH", Integer.valueOf(200));
                conn.commit();

                final RawCohort updated = RawCohortLogic.query(cache, "ABCDEFGH");

                assertNotNull(updated, "No record returned after update");

                assertEquals("ABCDEFGH", updated.cohort, "Invalid cohort");

                assertEquals(Integer.valueOf(200), updated.size, "Invalid size");

                assertEquals("Alice", updated.instructor, "Invalid instructor");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all cohort rows: " + ex.getMessage());
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
                final RawCohort raw2 = new RawCohort("IJKLMNOP", Integer.valueOf(101), "Bob");

                final boolean result = RawCohortLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawCohort> all = RawCohortLogic.INSTANCE.queryAll(cache);

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

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting cohort: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM cohort");
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
