package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawTestingCenter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawTestingCenterLogic} class.
 */
final class TestRawTestingCenterLogic {

    /** A date/time used in test records. */
    private static final LocalDateTime dateTime1 = LocalDateTime.of(2020, 1, 2, 12, 34, 56);

    /** A date/time used in test records. */
    private static final LocalDateTime dateTime2 = LocalDateTime.of(2021, 2, 3, 1, 3, 5);

    /** A date/time used in test records. */
    private static final LocalDateTime dateTime3 = LocalDateTime.of(2020, 2, 3, 4, 5, 6);

    /** A date/time used in test records. */
    private static final LocalDateTime dateTime4 = LocalDateTime.of(2021, 3, 4, 5, 6, 7);

    /** A date/time used in test records. */
    private static final LocalDateTime dateTime5 = LocalDateTime.of(2022, 4, 5, 6, 7, 8);

    /** A date/time used in test records. */
    private static final LocalDateTime dateTime6 = LocalDateTime.of(2023, 5, 6, 7, 8, 9);

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
                stmt.executeUpdate("DELETE FROM testing_centers");
            }
            conn.commit();

            final RawTestingCenter raw1 = new RawTestingCenter("CENTER 1", "Center One", "1 First Street",
                    "Apartment A", null, "Fort Collins", "CO", "80535", "Y", dateTime1, dateTime2, null, null,
                    "N", "Y");

            final RawTestingCenter raw2 = new RawTestingCenter("CENTER 2", "Center Two", "2 Second Street",
                    "Apartment B", "Closet 2", "Denver", "CO", "12345", "Y", dateTime3, dateTime4, dateTime5,
                    dateTime6, "Y", "N");

            assertTrue(RawTestingCenterLogic.insert(cache, raw1), "Failed to insert testing_centers");
            assertTrue(RawTestingCenterLogic.insert(cache, raw2), "Failed to insert testing_centers");
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
            final List<RawTestingCenter> all = RawTestingCenterLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawTestingCenter test : all) {

                if ("CENTER 1".equals(test.testingCenterId)
                    && "Center One".equals(test.tcName)
                    && "1 First Street".equals(test.addres1)
                    && "Apartment A".equals(test.addres2)
                    && test.addres3 == null
                    && "Fort Collins".equals(test.city)
                    && "CO".equals(test.state)
                    && "80535".equals(test.zipCode)
                    && "Y".equals(test.active)
                    && dateTime1.equals(test.dtimeCreated)
                    && dateTime2.equals(test.dtimeApproved)
                    && test.dtimeDenied == null
                    && test.dtimeRevoked == null
                    && "N".equals(test.isRemote)
                    && "Y".equals(test.isProctored)) {
                    found1 = true;
                } else if ("CENTER 2".equals(test.testingCenterId)
                           && "Center Two".equals(test.tcName)
                           && "2 Second Street".equals(test.addres1)
                           && "Apartment B".equals(test.addres2)
                           && "Closet 2".equals(test.addres3)
                           && "Denver".equals(test.city)
                           && "CO".equals(test.state)
                           && "12345".equals(test.zipCode)
                           && "Y".equals(test.active)
                           && dateTime3.equals(test.dtimeCreated)
                           && dateTime4.equals(test.dtimeApproved)
                           && dateTime5.equals(test.dtimeDenied)
                           && dateTime6.equals(test.dtimeRevoked)
                           && "Y".equals(test.isRemote)
                           && "N".equals(test.isProctored)) {
                    found2 = true;
                } else {
                    Log.warning("Bad testingCenterId ", test.testingCenterId);
                    Log.warning("Bad tcName ", test.tcName);
                    Log.warning("Bad addres1 ", test.addres1);
                    Log.warning("Bad addres2 ", test.addres2);
                    Log.warning("Bad addres3 ", test.addres3);
                    Log.warning("Bad city ", test.city);
                    Log.warning("Bad state ", test.state);
                    Log.warning("Bad zipCode ", test.zipCode);
                    Log.warning("Bad active ", test.active);
                    Log.warning("Bad dtimeCreated ", test.dtimeCreated);
                    Log.warning("Bad dtimeApproved ", test.dtimeApproved);
                    Log.warning("Bad dtimeDenied ", test.dtimeDenied);
                    Log.warning("Bad dtimeRevoked ", test.dtimeRevoked);
                    Log.warning("Bad isRemote ", test.isRemote);
                    Log.warning("Bad isProctored ", test.isProctored);
                }
            }

            assertTrue(found1, "Testing Center 1 not found");
            assertTrue(found2, "Testing Center 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all testing_centers rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final RawTestingCenter center1 = RawTestingCenterLogic.query(cache, "CENTER 1");

            assertNotNull(center1, "Null result from query");

            assertEquals("CENTER 1", center1.testingCenterId, "Bad testing center ID");
            assertEquals("Center One", center1.tcName, "Bad testing center name");
            assertEquals("1 First Street", center1.addres1, "Bad address 1");
            assertEquals("Apartment A", center1.addres2, "Bad address 2");
            assertNull(center1.addres3, "Bad address 3");
            assertEquals("Fort Collins", center1.city, "Bad city");
            assertEquals("CO", center1.state, "Bad state");
            assertEquals("80535", center1.zipCode, "Bad zip code");
            assertEquals("Y", center1.active, "Bad active flag");
            assertEquals(dateTime1, center1.dtimeCreated, "Bad created date/time");
            assertEquals(dateTime2, center1.dtimeApproved, "Bad approved datetime");
            assertNull(center1.dtimeDenied, "Bad denied datetime");
            assertNull(center1.dtimeRevoked, "Bad revoked datetime");
            assertEquals("N", center1.isRemote, "Bad remote flag");
            assertEquals("Y", center1.isProctored, "Bad proctored flag");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying testing_centers rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final RawTestingCenter raw2 = new RawTestingCenter("CENTER 2", "Center Two", "2 Second Street",
                    "Apartment B", "Closet 2", "Denver", "CO", "12345", "Y", dateTime3, dateTime4,
                    dateTime5, dateTime6, "Y", "N");

            final boolean result = RawTestingCenterLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawTestingCenter> all = RawTestingCenterLogic.queryAll(cache);

            assertEquals(1, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;

            for (final RawTestingCenter test : all) {

                if ("CENTER 1".equals(test.testingCenterId)
                    && "Center One".equals(test.tcName)
                    && "1 First Street".equals(test.addres1)
                    && "Apartment A".equals(test.addres2)
                    && test.addres3 == null
                    && "Fort Collins".equals(test.city)
                    && "CO".equals(test.state)
                    && "80535".equals(test.zipCode)
                    && "Y".equals(test.active)
                    && dateTime1.equals(test.dtimeCreated)
                    && dateTime2.equals(test.dtimeApproved)
                    && test.dtimeDenied == null
                    && test.dtimeRevoked == null
                    && "N".equals(test.isRemote)
                    && "Y".equals(test.isProctored)) {
                    found1 = true;
                } else {
                    Log.warning("Bad testingCenterId ", test.testingCenterId);
                    Log.warning("Bad tcName ", test.tcName);
                    Log.warning("Bad addres1 ", test.addres1);
                    Log.warning("Bad addres2 ", test.addres2);
                    Log.warning("Bad addres3 ", test.addres3);
                    Log.warning("Bad city ", test.city);
                    Log.warning("Bad state ", test.state);
                    Log.warning("Bad zipCode ", test.zipCode);
                    Log.warning("Bad active ", test.active);
                    Log.warning("Bad dtimeCreated ", test.dtimeCreated);
                    Log.warning("Bad dtimeApproved ", test.dtimeApproved);
                    Log.warning("Bad dtimeDenied ", test.dtimeDenied);
                    Log.warning("Bad dtimeRevoked ", test.dtimeRevoked);
                    Log.warning("Bad isRemote ", test.isRemote);
                    Log.warning("Bad isProctored ", test.isProctored);
                }
            }

            assertTrue(found1, "Testing Center 1 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting testing_centers: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM testing_centers");
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
