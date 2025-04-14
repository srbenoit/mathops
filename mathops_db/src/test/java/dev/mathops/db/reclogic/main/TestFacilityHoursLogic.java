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
import dev.mathops.db.rec.main.FacilityHoursRec;
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
 * Tests for the {@code FacilityHoursLogic} class.
 */
final class TestFacilityHoursLogic {

    /** A display index. */
    private static final Integer ONE = Integer.valueOf(1);

    /** A display index. */
    private static final Integer TWO = Integer.valueOf(2);

    /** A set of weekdays. */
    private static final Integer WEEDKDAYS_MTWRF = Integer.valueOf(62);

    /** A set of weekdays. */
    private static final Integer WEEDKDAYS_SMTWRF = Integer.valueOf(63);

    /** A set of weekdays. */
    private static final Integer WEEDKDAYS_S = Integer.valueOf(1);

    /** A start date. */
    private static final LocalDate START_1 = LocalDate.of(2025, 1, 21);

    /** A display index. */
    private static final LocalDate END_1 = LocalDate.of(2025, 5, 9);

    /** A start date. */
    private static final LocalDate START_2 = LocalDate.of(2025, 1, 22);

    /** A display index. */
    private static final LocalDate END_2 = LocalDate.of(2025, 5, 8);

    /** A start date. */
    private static final LocalTime OPEN_1 = LocalTime.of(10, 0);

    /** A display index. */
    private static final LocalTime CLOSE_1 = LocalTime.of(16, 0);

    /** A start date. */
    private static final LocalTime OPEN_2 = LocalTime.of(17, 30);

    /** A display index. */
    private static final LocalTime CLOSE_2 = LocalTime.of(20, 15);

    /** A start date. */
    private static final LocalTime OPEN_3 = LocalTime.of(12, 0);

    /** A display index. */
    private static final LocalTime CLOSE_3 = LocalTime.of(15, 0);

    /** A raw test record. */
    private static final FacilityHoursRec RAW1 =
            new FacilityHoursRec("PRECALC_TC", ONE, WEEDKDAYS_MTWRF, START_1, END_1, OPEN_1, CLOSE_1, OPEN_2, CLOSE_2);

    /** A raw test record. */
    private static final FacilityHoursRec RAW2 =
            new FacilityHoursRec("PRECALC_LC", ONE, WEEDKDAYS_MTWRF, START_1, END_1, OPEN_1, CLOSE_1, OPEN_2, CLOSE_2);

    /** A raw test record. */
    private static final FacilityHoursRec RAW3 =
            new FacilityHoursRec("PRECALC_LC", TWO, WEEDKDAYS_S, START_1, END_1, OPEN_3, CLOSE_3, null, null);

    /** A raw test record. */
    private static final FacilityHoursRec RAW4 =
            new FacilityHoursRec("HELP_TEAMS", ONE, WEEDKDAYS_SMTWRF, START_1, END_1, OPEN_1, CLOSE_3, null, null);

    /** A raw test record. */
    private static final FacilityHoursRec RAW5 =
            new FacilityHoursRec("HELP_TEAMS", TWO, WEEDKDAYS_MTWRF, START_1, END_1, OPEN_2, CLOSE_2, null, null);

    /** A raw test record. */
    private static final FacilityHoursRec UPD5 =
            new FacilityHoursRec("HELP_TEAMS", TWO, WEEDKDAYS_S, START_2, END_2, OPEN_3, CLOSE_3, null, null);

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final FacilityHoursRec r) {

        Log.warning("Unexpected facility ", r.facilityId);
        Log.warning("Unexpected displayIndex ", r.displayIndex);
        Log.warning("Unexpected weekdays ", r.weekdays);
        Log.warning("Unexpected startDt ", r.startDate);
        Log.warning("Unexpected endDt ", r.endDate);
        Log.warning("Unexpected openTime1 ", r.openTime1);
        Log.warning("Unexpected closeTime1 ", r.closeTime1);
        Log.warning("Unexpected openTime2 ", r.openTime2);
        Log.warning("Unexpected closeTime2 ", r.closeTime2);
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
                stmt.executeUpdate("DELETE FROM " + prefix + ".facility_hours");
            }
            conn.commit();

            assertTrue(FacilityHoursLogic.INSTANCE.insert(cache, RAW1), "Failed to insert facility_hours");
            assertTrue(FacilityHoursLogic.INSTANCE.insert(cache, RAW2), "Failed to insert facility_hours");
            assertTrue(FacilityHoursLogic.INSTANCE.insert(cache, RAW3), "Failed to insert facility_hours");
            assertTrue(FacilityHoursLogic.INSTANCE.insert(cache, RAW4), "Failed to insert facility_hours");
            assertTrue(FacilityHoursLogic.INSTANCE.insert(cache, RAW5), "Failed to insert facility_hours");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'facility_hours' table: " + ex.getMessage());
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
            final List<FacilityHoursRec> all = FacilityHoursLogic.INSTANCE.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final FacilityHoursRec r : all) {
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

            assertTrue(found1, "facility_hours 1 not found");
            assertTrue(found2, "facility_hours 2 not found");
            assertTrue(found3, "facility_hours 3 not found");
            assertTrue(found4, "facility_hours 4 not found");
            assertTrue(found5, "facility_hours 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'facility_hours' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final FacilityHoursRec r = FacilityHoursLogic.INSTANCE.query(cache, RAW3.facilityId, RAW3.displayIndex);

            assertNotNull(r, "No record returned by query");
            if (!RAW3.equals(r)) {
                printUnexpected(r);
                fail("Incorrect results after update of facility");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying facility_hours: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByFacility results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            final List<FacilityHoursRec> all = FacilityHoursLogic.INSTANCE.queryByFacility(cache, "PRECALC_LC");

            assertEquals(2, all.size(), "Incorrect record count from queryByFacility");

            boolean found2 = false;
            boolean found3 = false;

            for (final FacilityHoursRec r : all) {
                if (RAW2.equals(r)) {
                    found2 = true;
                } else if (RAW3.equals(r)) {
                    found3 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found2, "facility_hours 2 not found");
            assertTrue(found3, "facility_hours 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying facility_hours by facility: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            if (FacilityHoursLogic.INSTANCE.update(cache, UPD5)) {
                final FacilityHoursRec r = FacilityHoursLogic.INSTANCE.query(cache, UPD5.facilityId, UPD5.displayIndex);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD5.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of facility_hours");
                }
            } else {
                fail("Failed to update facility_hours row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating facility_hours: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = FacilityHoursLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<FacilityHoursRec> all = FacilityHoursLogic.INSTANCE.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final FacilityHoursRec r : all) {
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

            assertTrue(found1, "facility_hours 1 not found");
            assertTrue(found3, "facility_hours 3 not found");
            assertTrue(found4, "facility_hours 4 not found");
            assertTrue(found5, "facility_hours 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting facility_hours: " + ex.getMessage());
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
                    stmt.executeUpdate("DELETE FROM " + prefix + ".facility_hours");
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
