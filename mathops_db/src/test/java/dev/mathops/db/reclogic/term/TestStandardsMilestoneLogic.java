package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbUse;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Facet;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.rec.term.StandardsMilestoneRec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code StandardsMilestoneLogic} class.
 */
final class TestStandardsMilestoneLogic {

    /** A raw test record. */
    private static final StandardsMilestoneRec RAW1 =
            new StandardsMilestoneRec("A", Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(6), "HW",
                    LocalDate.of(2025, Month.MARCH, 1));

    /** A raw test record. */
    private static final StandardsMilestoneRec RAW2 =
            new StandardsMilestoneRec("A", Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(6), "MA",
                    LocalDate.of(2025, Month.MARCH, 2));

    /** A raw test record. */
    private static final StandardsMilestoneRec RAW3 =
            new StandardsMilestoneRec("A", Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(7), "LB",
                    LocalDate.of(2025, Month.MARCH, 3));

    /** A raw test record. */
    private static final StandardsMilestoneRec RAW4 =
            new StandardsMilestoneRec("A", Integer.valueOf(2), Integer.valueOf(2), Integer.valueOf(8), "ZY",
                    LocalDate.of(2025, Month.MARCH, 4));

    /** A raw test record. */
    private static final StandardsMilestoneRec RAW5 =
            new StandardsMilestoneRec("B", Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(3), "AA",
                    LocalDate.of(2025, Month.MARCH, 5));

    /** A raw test record. */
    private static final StandardsMilestoneRec UPD3 =
            new StandardsMilestoneRec("A", Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(7), "LB",
                    LocalDate.of(2025, Month.APRIL, 3));

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StandardsMilestoneRec r) {

        Log.warning("Unexpected pace track ", r.paceTrack);
        Log.warning("Unexpected pace ", r.pace);
        Log.warning("Unexpected pace index ", r.paceIndex);
        Log.warning("Unexpected module number ", r.moduleNbr);
        Log.warning("Unexpected milestone type ", r.msType);
        Log.warning("Unexpected milestone date ", r.msDate);
    }

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        profile = config.getCodeProfile(Contexts.POSTGRES_TEST_PATH);
        if (profile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGTEST_PROFILE));
        }
        login = profile.getLogin(ESchema.LEGACY);
        if (login == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGPRIMARY_CONTEXT));
        }

        // Make sure the connection is accessing the TEST database
        final Facet facet = profile.getFacet(ESchema.LEGACY);
        if (facet.data.use != EDbUse.TEST) {
            throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, facet.data.use));
        }

        final Cache cache = new Cache(profile);
        final String prefix = cache.getSchemaPrefix(ESchema.TERM);
        if (prefix == null) {
            fail(TestRes.get(TestRes.ERR_NO_TERM_PREFIX));
        }

        final DbConnection conn = login.checkOutConnection();

        final String sql = "SELECT descr FROM " + prefix + ".which_db";

        try {
            try (final Statement stmt = conn.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {

                if (rs.next()) {
                    final String which = rs.getString(1);
                    if (which == null || !"TEST".equals(which.trim())) {
                        throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                    }
                } else {
                    throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                }
            }

            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM " + prefix + ".standards_milestone");
            }
            conn.commit();

            assertTrue(StandardsMilestoneLogic.INSTANCE.insert(cache, RAW1),
                    "Failed to insert standards milestone");
            assertTrue(StandardsMilestoneLogic.INSTANCE.insert(cache, RAW2),
                    "Failed to insert standards milestone");
            assertTrue(StandardsMilestoneLogic.INSTANCE.insert(cache, RAW3),
                    "Failed to insert standards milestone");
            assertTrue(StandardsMilestoneLogic.INSTANCE.insert(cache, RAW4),
                    "Failed to insert standards milestone");
            assertTrue(StandardsMilestoneLogic.INSTANCE.insert(cache, RAW5),
                    "Failed to insert standards milestone");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'standards_milestone' table: " + ex.getMessage());
            throw new IllegalArgumentException(ex);
        } finally {
            login.checkInConnection(conn);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0001() {
        final Cache cache = new Cache(profile);

        try {
            final List<StandardsMilestoneRec> all = StandardsMilestoneLogic.INSTANCE.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final StandardsMilestoneRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (RAW2.equals(r)) {
                    found2 = true;
                } else if (RAW3.equals(r)) {
                    found3 = true;
                } else if (RAW4.equals(r)) {
                    found4 = true;
                } else if (RAW5.equals(r)) {
                    found5 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "standards_milestone 1 not found");
            assertTrue(found2, "standards_milestone 2 not found");
            assertTrue(found3, "standards_milestone 3 not found");
            assertTrue(found4, "standards_milestone 4 not found");
            assertTrue(found5, "standards_milestone 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'standards_milestone' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final StandardsMilestoneRec r = StandardsMilestoneLogic.INSTANCE.query(cache, RAW1.paceTrack,
                    RAW1.pace, RAW1.paceIndex, RAW1.moduleNbr, RAW1.msType);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying standards_milestone: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            if (StandardsMilestoneLogic.INSTANCE.update(cache, UPD3)) {
                final StandardsMilestoneRec r = StandardsMilestoneLogic.INSTANCE.query(cache, UPD3.paceTrack,
                        UPD3.pace, UPD3.paceIndex, UPD3.moduleNbr, UPD3.msType);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD3.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of standards_milestone");
                }
            } else {
                fail("Failed to update standards_milestone row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating standards_milestone: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = StandardsMilestoneLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<StandardsMilestoneRec> all = StandardsMilestoneLogic.INSTANCE.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final StandardsMilestoneRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (UPD3.equals(r)) {
                    found3 = true;
                } else if (RAW4.equals(r)) {
                    found4 = true;
                } else if (RAW5.equals(r)) {
                    found5 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "standards_milestone 1 not found");
            assertTrue(found3, "standards_milestone 3 not found");
            assertTrue(found4, "standards_milestone 4 not found");
            assertTrue(found5, "standards_milestone 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting standards_milestones: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Cache cache = new Cache(profile);
        final String prefix = cache.getSchemaPrefix(ESchema.TERM);
        if (prefix == null) {
            fail(TestRes.get(TestRes.ERR_NO_TERM_PREFIX));
        }

        try {
            final DbConnection conn = login.checkOutConnection();

            final String sql = "SELECT descr FROM " + prefix + ".which_db";

            try {
                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery(sql)) {

                    if (rs.next()) {
                        final String which = rs.getString(1);
                        if (which == null || !"TEST".equals(which.trim())) {
                            throw new IllegalArgumentException(
                                    TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                        }
                    } else {
                        throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                    }
                }

                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM " + prefix + ".standards_milestone");
                }

                conn.commit();
            } finally {
                login.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        }
    }
}
