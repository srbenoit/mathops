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
import dev.mathops.db.rec.main.FacilityClosureRec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code FacilityClosureLogic} class.
 */
final class TestFacilityClosureLogic {

    /** A closure date. */
    private static final LocalDate HOLIDAY_1 = LocalDate.of(2025, 1, 20);

    /** A closure date. */
    private static final LocalDate SNOW = LocalDate.of(2025, 2, 28);

    /** A closure date. */
    private static final LocalDate BREAK_1_START = LocalDate.of(2025, 3, 16);

    /** A closure date. */
    private static final LocalDate BREAK_1_END = LocalDate.of(2025, 3, 23);

    /** A start date. */
    private static final LocalTime START_1 = LocalTime.of(12, 30);

    /** A start date. */
    private static final LocalTime END_1 = LocalTime.of(17, 30);

    /** A raw test record. */
    private static final FacilityClosureRec RAW1 =
            new FacilityClosureRec("PRECALC_TC", HOLIDAY_1, HOLIDAY_1, FacilityClosureRec.HOLIDAY, null, null);

//    public FacilityClosureRec(final String theFacility, final LocalDate theClosureDt, final String theClosureType,
//                              final LocalTime theStartTime, final LocalTime theEndTime) {

    /** A raw test record. */
    private static final FacilityClosureRec RAW2 =
            new FacilityClosureRec("PRECALC_LC", SNOW, SNOW, FacilityClosureRec.WEATHER, START_1, END_1);

    /** A raw test record. */
    private static final FacilityClosureRec RAW3 =
            new FacilityClosureRec("PRECALC_LC", BREAK_1_START, BREAK_1_END, FacilityClosureRec.SP_BREAK, null, null);

    /** A raw test record. */
    private static final FacilityClosureRec UPD3 =
            new FacilityClosureRec("PRECALC_LC", BREAK_1_START, BREAK_1_END, FacilityClosureRec.EVENT, START_1, END_1);

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final FacilityClosureRec r) {

        Log.warning("Unexpected facility ", r.facilityId);
        Log.warning("Unexpected startDate ", r.startDate);
        Log.warning("Unexpected endDate ", r.endDate);
        Log.warning("Unexpected closureType ", r.closureType);
        Log.warning("Unexpected startTime ", r.startTime);
        Log.warning("Unexpected endTime ", r.endTime);
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
                stmt.executeUpdate("DELETE FROM " + prefix + ".facility_closure");
            }
            conn.commit();

            assertTrue(FacilityClosureLogic.INSTANCE.insert(cache, RAW1), "Failed to insert facility_closure");
            assertTrue(FacilityClosureLogic.INSTANCE.insert(cache, RAW2), "Failed to insert facility_closure");
            assertTrue(FacilityClosureLogic.INSTANCE.insert(cache, RAW3), "Failed to insert facility_closure");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'facility_closure' table: " + ex.getMessage());
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
            final List<FacilityClosureRec> all = FacilityClosureLogic.INSTANCE.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final FacilityClosureRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (RAW2.equals(r)) {
                    found2 = true;
                } else if (RAW3.equals(r)) {
                    found3 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "facility_closure 1 not found");
            assertTrue(found2, "facility_closure 2 not found");
            assertTrue(found3, "facility_closure 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'facility_closure' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final FacilityClosureRec r = FacilityClosureLogic.INSTANCE.query(cache, RAW2.facilityId, RAW2.startDate);

            assertNotNull(r, "No record returned by query");
            assertEquals(RAW2, r, "Incorrect record returned by query");

        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying facility_closure: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByFacility results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            final List<FacilityClosureRec> all = FacilityClosureLogic.INSTANCE.queryByFacility(cache, "PRECALC_LC");

            assertEquals(2, all.size(), "Incorrect record count from queryByFacility");

            boolean found2 = false;
            boolean found3 = false;

            for (final FacilityClosureRec r : all) {
                if (RAW2.equals(r)) {
                    found2 = true;
                } else if (RAW3.equals(r)) {
                    found3 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found2, "facility_closure 2 not found");
            assertTrue(found3, "facility_closure 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying facility_closure by facility: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            if (FacilityClosureLogic.INSTANCE.update(cache, UPD3)) {
                final FacilityClosureRec r = FacilityClosureLogic.INSTANCE.query(cache, UPD3.facilityId,
                        UPD3.startDate);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD3.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of facility_closure");
                }
            } else {
                fail("Failed to update facility_closure row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating facility_closure: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = FacilityClosureLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<FacilityClosureRec> all = FacilityClosureLogic.INSTANCE.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final FacilityClosureRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (UPD3.equals(r)) {
                    found3 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "facility_closure 1 not found");
            assertTrue(found3, "facility_closure 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting facility_closure: " + ex.getMessage());
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
                    stmt.executeUpdate("DELETE FROM " + prefix + ".facility_closure");
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
