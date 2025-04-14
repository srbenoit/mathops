package dev.mathops.db.reclogic.main;

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
import dev.mathops.db.rec.main.FacilityRec;
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
 * Tests for the {@code FacilityLogic} class.
 */
final class TestFacilityLogic {

    /** A raw test record. */
    private static final FacilityRec RAW1 =
            new FacilityRec("PRECALC_TC", "Precalculus Testing Center", "Weber", "138");

    /** A raw test record. */
    private static final FacilityRec RAW2 =
            new FacilityRec("PRECALC_LC", "Precalculus Learning Center", "Weber", "136");

    /** A raw test record. */
    private static final FacilityRec RAW3 =
            new FacilityRec("PRECALC_OF", "Precalculus Center Office", "Weber", "137");

    /** A raw test record. */
    private static final FacilityRec RAW4 =
            new FacilityRec("HELP_TEAMS", "Online Help (MS Teams)", null, null);

    /** A raw test record. */
    private static final FacilityRec RAW5 =
            new FacilityRec("HELP_ALVS", "In-Person Help (Adult Learner and Veteran Services Office)", "LSC", "282");

    /** A raw test record. */
    private static final FacilityRec UPD5 =
            new FacilityRec("HELP_ALVS", "In-Person Help (Durrell Center)", "DUR", "100");

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final FacilityRec r) {

        Log.warning("Unexpected facility ", r.facilityId);
        Log.warning("Unexpected name ", r.facilityName);
        Log.warning("Unexpected building ", r.buildingName);
        Log.warning("Unexpected room ", r.roomNbr);
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
        final String prefix = cache.getSchemaPrefix(ESchema.MAIN);
        if (prefix == null) {
            fail(TestRes.get(TestRes.ERR_NO_MAIN_PREFIX));
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
                stmt.executeUpdate("DELETE FROM " + prefix + ".facility");
            }
            conn.commit();

            assertTrue(FacilityLogic.INSTANCE.insert(cache, RAW1), "Failed to insert facility");
            assertTrue(FacilityLogic.INSTANCE.insert(cache, RAW2), "Failed to insert facility");
            assertTrue(FacilityLogic.INSTANCE.insert(cache, RAW3), "Failed to insert facility");
            assertTrue(FacilityLogic.INSTANCE.insert(cache, RAW4), "Failed to insert facility");
            assertTrue(FacilityLogic.INSTANCE.insert(cache, RAW5), "Failed to insert facility");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'facility' table: " + ex.getMessage());
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
            final List<FacilityRec> all = FacilityLogic.INSTANCE.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final FacilityRec r : all) {
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

            assertTrue(found1, "facility 1 not found");
            assertTrue(found2, "facility 2 not found");
            assertTrue(found3, "facility 3 not found");
            assertTrue(found4, "facility 4 not found");
            assertTrue(found5, "facility 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'facility' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final FacilityRec r = FacilityLogic.INSTANCE.query(cache, RAW1.facilityId);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying facility: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            if (FacilityLogic.INSTANCE.update(cache, UPD5)) {
                final FacilityRec r = FacilityLogic.INSTANCE.query(cache, UPD5.facilityId);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD5.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of facility");
                }
            } else {
                fail("Failed to update facility row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating facility: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = FacilityLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<FacilityRec> all = FacilityLogic.INSTANCE.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final FacilityRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (RAW3.equals(r)) {
                    found3 = true;
                } else if (RAW4.equals(r)) {
                    found4 = true;
                } else if (UPD5.equals(r)) {
                    found5 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "facility 1 not found");
            assertTrue(found3, "facility 3 not found");
            assertTrue(found4, "facility 4 not found");
            assertTrue(found5, "facility 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting facilities: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Cache cache = new Cache(profile);
        final String prefix = cache.getSchemaPrefix(ESchema.MAIN);
        if (prefix == null) {
            fail(TestRes.get(TestRes.ERR_NO_MAIN_PREFIX));
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
                    stmt.executeUpdate("DELETE FROM " + prefix + ".facility");
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
