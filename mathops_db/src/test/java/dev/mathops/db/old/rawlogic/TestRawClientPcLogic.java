package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawClientPcLogic} class.
 */
final class TestRawClientPcLogic {

    /** A date/time used in test records. */
    private static final LocalDateTime date1 = LocalDateTime.of(2020, 1, 2, 13, 14, 15);

    /** A date/time used in test records. */
    private static final LocalDateTime date2 = LocalDateTime.of(2021, 2, 3, 14, 15, 16);

    /** A date/time used in test records. */
    private static final LocalDateTime date3 = LocalDateTime.of(2020, 1, 2, 3, 4, 5);

    /** A date/time used in test records. */
    private static final LocalDateTime date4 = LocalDateTime.of(2021, 2, 3, 4, 5, 6);

    /** A date/time used in test records. */
    private static final LocalDateTime date5 = LocalDateTime.of(1990, 10, 9, 8, 7, 6);

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
                stmt.executeUpdate("DELETE FROM client_pc");
            }
            conn.commit();

            final RawClientPc raw1 = new RawClientPc("Computer 1", "CENTER 1", "Station A", "A good computer",
                    Integer.valueOf(100), Integer.valueOf(200), "P", Integer.valueOf(1), date1, date2,
                    "001122334455", RawClientPc.POWER_OFF, Integer.valueOf(50), Integer.valueOf(51), "888888888",
                    RawRecordConstants.M117, Integer.valueOf(1), "171UE");

            final RawClientPc raw2 = new RawClientPc("Computer 2", "CENTER 2", "Station B", "A lousy computer",
                    Integer.valueOf(150), Integer.valueOf(250), "O", Integer.valueOf(2), date3, date4,
                    "66778899AABB", RawClientPc.POWER_TURNING_ON, Integer.valueOf(60), Integer.valueOf(61),
                    "888888889", RawRecordConstants.M126, Integer.valueOf(5), "26FIN");

            final RawClientPc raw3 = new RawClientPc("Computer 3", "CENTER 2", "Station C", "A mediocre computer",
                    Integer.valueOf(50), Integer.valueOf(50), "Q", Integer.valueOf(3), date5, null,
                    "0123456789AB", RawClientPc.POWER_REPORTING_ON, Integer.valueOf(70), Integer.valueOf(71),
                    null, null, null, null);

            assertTrue(RawClientPcLogic.insert(cache, raw1), "Failed to insert client_pc");
            assertTrue(RawClientPcLogic.insert(cache, raw2), "Failed to insert client_pc");
            assertTrue(RawClientPcLogic.insert(cache, raw3), "Failed to insert client_pc");
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
            final List<RawClientPc> all = RawClientPcLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawClientPc r : all) {

                if ("Computer 1".equals(r.computerId)
                    && "CENTER 1".equals(r.testingCenterId)
                    && "Station A".equals(r.stationNbr)
                    && "A good computer".equals(r.computerDesc)
                    && Integer.valueOf(100).equals(r.iconX)
                    && Integer.valueOf(200).equals(r.iconY)
                    && "P".equals(r.pcUsage)
                    && Integer.valueOf(1).equals(r.currentStatus)
                    && date1.equals(r.dtimeCreated)
                    && date2.equals(r.dtimeApproved)
                    && "001122334455".equals(r.macAddress)
                    && RawClientPc.POWER_OFF.equals(r.powerStatus)
                    && Integer.valueOf(50).equals(r.powerOnDue)
                    && Integer.valueOf(51).equals(r.lastPing)
                    && "888888888".equals(r.currentStuId)
                    && RawRecordConstants.M117.equals(r.currentCourse)
                    && Integer.valueOf(1).equals(r.currentUnit)
                    && "171UE".equals(r.currentVersion)) {

                    found1 = true;

                } else if ("Computer 2".equals(r.computerId)
                           && "CENTER 2".equals(r.testingCenterId)
                           && "Station B".equals(r.stationNbr)
                           && "A lousy computer".equals(r.computerDesc)
                           && Integer.valueOf(150).equals(r.iconX)
                           && Integer.valueOf(250).equals(r.iconY)
                           && "O".equals(r.pcUsage)
                           && Integer.valueOf(2).equals(r.currentStatus)
                           && date3.equals(r.dtimeCreated)
                           && date4.equals(r.dtimeApproved)
                           && "66778899AABB".equals(r.macAddress)
                           && RawClientPc.POWER_TURNING_ON.equals(r.powerStatus)
                           && Integer.valueOf(60).equals(r.powerOnDue)
                           && Integer.valueOf(61).equals(r.lastPing)
                           && "888888889".equals(r.currentStuId)
                           && RawRecordConstants.M126.equals(r.currentCourse)
                           && Integer.valueOf(5).equals(r.currentUnit)
                           && "26FIN".equals(r.currentVersion)) {
                    found2 = true;

                } else if ("Computer 3".equals(r.computerId)
                           && "CENTER 2".equals(r.testingCenterId)
                           && "Station C".equals(r.stationNbr)
                           && "A mediocre computer".equals(r.computerDesc)
                           && Integer.valueOf(50).equals(r.iconX)
                           && Integer.valueOf(50).equals(r.iconY)
                           && "Q".equals(r.pcUsage)
                           && Integer.valueOf(3).equals(r.currentStatus)
                           && date5.equals(r.dtimeCreated)
                           && r.dtimeApproved == null
                           && "0123456789AB".equals(r.macAddress)
                           && RawClientPc.POWER_REPORTING_ON.equals(r.powerStatus)
                           && Integer.valueOf(70).equals(r.powerOnDue)
                           && Integer.valueOf(71).equals(r.lastPing)
                           && r.currentStuId == null
                           && r.currentCourse == null
                           && r.currentUnit == null
                           && r.currentVersion == null) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected computerId ", r.computerId);
                    Log.warning("Unexpected testingCenterId ", r.testingCenterId);
                    Log.warning("Unexpected stationNbr ", r.stationNbr);
                    Log.warning("Unexpected computerDesc ", r.computerDesc);
                    Log.warning("Unexpected iconX ", r.iconX);
                    Log.warning("Unexpected iconY ", r.iconY);
                    Log.warning("Unexpected pcUsage ", r.pcUsage);
                    Log.warning("Unexpected currentStatus ", r.currentStatus);
                    Log.warning("Unexpected dtimeCreated ", r.dtimeCreated);
                    Log.warning("Unexpected dtimeApproved ", r.dtimeApproved);
                    Log.warning("Unexpected macAddress ", r.macAddress);
                    Log.warning("Unexpected powerStatus ", r.powerStatus);
                    Log.warning("Unexpected currentStuId ", r.currentStuId);
                    Log.warning("Unexpected currentCourse ", r.currentCourse);
                    Log.warning("Unexpected currentUnit ", r.currentUnit);
                    Log.warning("Unexpected currentVersion ", r.currentVersion);
                }
            }

            assertTrue(found1, "client_pc 1 not found");
            assertTrue(found2, "client_pc 2 not found");
            assertTrue(found3, "client_pc 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all client_pc rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByTestingCenter results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawClientPc> tc2 = RawClientPcLogic.queryByTestingCenter(cache, "CENTER 2");

            assertEquals(2, tc2.size(), "Incorrect record count from queryByTestingCenter");

            boolean found2 = false;
            boolean found3 = false;

            for (final RawClientPc r : tc2) {

                if ("Computer 2".equals(r.computerId)
                    && "CENTER 2".equals(r.testingCenterId)
                    && "Station B".equals(r.stationNbr)
                    && "A lousy computer".equals(r.computerDesc)
                    && Integer.valueOf(150).equals(r.iconX)
                    && Integer.valueOf(250).equals(r.iconY)
                    && "O".equals(r.pcUsage)
                    && Integer.valueOf(2).equals(r.currentStatus)
                    && date3.equals(r.dtimeCreated)
                    && date4.equals(r.dtimeApproved)
                    && "66778899AABB".equals(r.macAddress)
                    && RawClientPc.POWER_TURNING_ON.equals(r.powerStatus)
                    && Integer.valueOf(60).equals(r.powerOnDue)
                    && Integer.valueOf(61).equals(r.lastPing)
                    && "888888889".equals(r.currentStuId)
                    && RawRecordConstants.M126.equals(r.currentCourse)
                    && Integer.valueOf(5).equals(r.currentUnit)
                    && "26FIN".equals(r.currentVersion)) {
                    found2 = true;

                } else if ("Computer 3".equals(r.computerId)
                           && "CENTER 2".equals(r.testingCenterId)
                           && "Station C".equals(r.stationNbr)
                           && "A mediocre computer".equals(r.computerDesc)
                           && Integer.valueOf(50).equals(r.iconX)
                           && Integer.valueOf(50).equals(r.iconY)
                           && "Q".equals(r.pcUsage)
                           && Integer.valueOf(3).equals(r.currentStatus)
                           && date5.equals(r.dtimeCreated)
                           && r.dtimeApproved == null
                           && "0123456789AB".equals(r.macAddress)
                           && RawClientPc.POWER_REPORTING_ON.equals(r.powerStatus)
                           && Integer.valueOf(70).equals(r.powerOnDue)
                           && Integer.valueOf(71).equals(r.lastPing)
                           && r.currentStuId == null
                           && r.currentCourse == null
                           && r.currentUnit == null
                           && r.currentVersion == null) {
                    found3 = true;

                } else {
                    Log.warning("Unexpected computerId ", r.computerId);
                    Log.warning("Unexpected testingCenterId ", r.testingCenterId);
                    Log.warning("Unexpected stationNbr ", r.stationNbr);
                    Log.warning("Unexpected computerDesc ", r.computerDesc);
                    Log.warning("Unexpected iconX ", r.iconX);
                    Log.warning("Unexpected iconY ", r.iconY);
                    Log.warning("Unexpected pcUsage ", r.pcUsage);
                    Log.warning("Unexpected currentStatus ", r.currentStatus);
                    Log.warning("Unexpected dtimeCreated ", r.dtimeCreated);
                    Log.warning("Unexpected dtimeApproved ", r.dtimeApproved);
                    Log.warning("Unexpected macAddress ", r.macAddress);
                    Log.warning("Unexpected powerStatus ", r.powerStatus);
                    Log.warning("Unexpected currentStuId ", r.currentStuId);
                    Log.warning("Unexpected currentCourse ", r.currentCourse);
                    Log.warning("Unexpected currentUnit ", r.currentUnit);
                    Log.warning("Unexpected currentVersion ", r.currentVersion);
                }
            }

            assertTrue(found2, "client_pc 2 not found");
            assertTrue(found3, "client_pc 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying client_pc by testing center: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final RawClientPc r = RawClientPcLogic.query(cache, "Computer 1");

            assertNotNull(r, "No row returned by query");

            assertEquals("Computer 1", r.computerId, "Bad computer");
            assertEquals("CENTER 1", r.testingCenterId, "Bad center ID");
            assertEquals("Station A", r.stationNbr, "Bad station number");
            assertEquals("A good computer", r.computerDesc, "Bad description");
            assertEquals(Integer.valueOf(100), r.iconX, "Bad icon X");
            assertEquals(Integer.valueOf(200), r.iconY, "Bad icon Y");
            assertEquals("P", r.pcUsage, "Bad usage");
            assertEquals(Integer.valueOf(1), r.currentStatus, "Bad current status");
            assertEquals(date1, r.dtimeCreated, "Bad create date");
            assertEquals(date2, r.dtimeApproved, "Bad approval date");
            assertEquals("001122334455", r.macAddress, "Bad MAC address");
            assertEquals(RawClientPc.POWER_OFF, r.powerStatus, "Bad power status");
            assertEquals("888888888", r.currentStuId, "Bad current student");
            assertEquals(RawRecordConstants.M117, r.currentCourse, "Bad current course");
            assertEquals(Integer.valueOf(1), r.currentUnit, "Bad current unit");
            assertEquals("171UE", r.currentVersion, "Bad current version");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying client_pc row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateCurrentStatus results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawClientPcLogic.updateCurrentStatus(cache, "Computer 1", RawClientPc.STATUS_ERROR),
                    "updateCurrentStatus returned false");

            final RawClientPc r = RawClientPcLogic.query(cache, "Computer 1");

            assertNotNull(r, "No row returned by query");

            assertEquals(RawClientPc.STATUS_ERROR, r.currentStatus, "Bad current status after update");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateCurrentStatus: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateAllCurrent results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawClientPcLogic.updateAllCurrent(cache, "Computer 1", RawClientPc.STATUS_AWAIT_STUDENT,
                            "111111111", "M 999", Integer.valueOf(9), "VVVVV"),
                    "updateCurrentStatus returned false");

            final RawClientPc r = RawClientPcLogic.query(cache, "Computer 1");

            assertNotNull(r, "No row returned by query");

            assertEquals(RawClientPc.STATUS_AWAIT_STUDENT, r.currentStatus, "Bad current status after update");
            assertEquals("111111111", r.currentStuId, "Bad current student");
            assertEquals("M 999", r.currentCourse, "Bad current course");
            assertEquals(Integer.valueOf(9), r.currentUnit, "Bad current unit");
            assertEquals("VVVVV", r.currentVersion, "Bad current version");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateAllCurrent: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updatePcUsage results")
    void test0008() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawClientPcLogic.updatePcUsage(cache, "Computer 1", "Z"), "updtePcUsage returned false");

            final RawClientPc r = RawClientPcLogic.query(cache, "Computer 1");

            assertNotNull(r, "No row returned by query");

            assertEquals("Z", r.pcUsage, "Bad usage after update");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updatePcUsage: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updatePowerStatus results")
    void test0009() {

        final Cache cache = new Cache(profile);
        try {
            assertTrue(RawClientPcLogic.updatePowerStatus(cache, "Computer 1", "9"),
                    "updatePowerStatus returned false");

            final RawClientPc r = RawClientPcLogic.query(cache, "Computer 1");

            assertNotNull(r, "No row returned by query");

            assertEquals("9", r.powerStatus, "Bad power status after update");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updatePowerStatus: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updatePowerOnDue results")
    void test0010() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawClientPcLogic.updatePowerOnDue(cache, "Computer 1", Integer.valueOf(101)),
                    "updatePowerOnDue returned false");

            final RawClientPc r = RawClientPcLogic.query(cache, "Computer 1");

            assertNotNull(r, "No row returned by query");

            assertEquals(Integer.valueOf(101), r.powerOnDue, "Bad power due time after update");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updatePowerOnDue: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateLastPing results")
    void test0011() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawClientPcLogic.updateLastPing(cache, "Computer 1", Integer.valueOf(102)),
                    "updateLastPing returned false");

            final RawClientPc r = RawClientPcLogic.query(cache, "Computer 1");

            assertNotNull(r, "No row returned by query");

            assertEquals(Integer.valueOf(102), r.lastPing, "Bad last ping time after update");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateLastPing: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0012() {

        final Cache cache = new Cache(profile);

        try {
            final RawClientPc raw1 = new RawClientPc("Computer 1", "CENTER 1", "Station A", "A good computer",
                    Integer.valueOf(100), Integer.valueOf(200), "P", Integer.valueOf(1), date1, date2,
                    "001122334455", RawClientPc.POWER_OFF, null, null, "888888888", RawRecordConstants.M117,
                    Integer.valueOf(1), "171UE");

            final boolean result = RawClientPcLogic.delete(cache, raw1);
            assertTrue(result, "delete returned false");

            final List<RawClientPc> all = RawClientPcLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found2 = false;
            boolean found3 = false;

            for (final RawClientPc r : all) {

                if ("Computer 2".equals(r.computerId)
                    && "CENTER 2".equals(r.testingCenterId)
                    && "Station B".equals(r.stationNbr)
                    && "A lousy computer".equals(r.computerDesc)
                    && Integer.valueOf(150).equals(r.iconX)
                    && Integer.valueOf(250).equals(r.iconY)
                    && "O".equals(r.pcUsage)
                    && Integer.valueOf(2).equals(r.currentStatus)
                    && date3.equals(r.dtimeCreated)
                    && date4.equals(r.dtimeApproved)
                    && "66778899AABB".equals(r.macAddress)
                    && RawClientPc.POWER_TURNING_ON.equals(r.powerStatus)
                    && Integer.valueOf(60).equals(r.powerOnDue)
                    && Integer.valueOf(61).equals(r.lastPing)
                    && "888888889".equals(r.currentStuId)
                    && RawRecordConstants.M126.equals(r.currentCourse)
                    && Integer.valueOf(5).equals(r.currentUnit)
                    && "26FIN".equals(r.currentVersion)) {
                    found2 = true;

                } else if ("Computer 3".equals(r.computerId)
                           && "CENTER 2".equals(r.testingCenterId)
                           && "Station C".equals(r.stationNbr)
                           && "A mediocre computer".equals(r.computerDesc)
                           && Integer.valueOf(50).equals(r.iconX)
                           && Integer.valueOf(50).equals(r.iconY)
                           && "Q".equals(r.pcUsage)
                           && Integer.valueOf(3).equals(r.currentStatus)
                           && date5.equals(r.dtimeCreated)
                           && r.dtimeApproved == null
                           && "0123456789AB".equals(r.macAddress)
                           && RawClientPc.POWER_REPORTING_ON.equals(r.powerStatus)
                           && Integer.valueOf(70).equals(r.powerOnDue)
                           && Integer.valueOf(71).equals(r.lastPing)
                           && r.currentStuId == null
                           && r.currentCourse == null
                           && r.currentUnit == null
                           && r.currentVersion == null) {
                    found3 = true;

                } else {
                    Log.warning("Unexpected computerId ", r.computerId);
                    Log.warning("Unexpected testingCenterId ", r.testingCenterId);
                    Log.warning("Unexpected stationNbr ", r.stationNbr);
                    Log.warning("Unexpected computerDesc ", r.computerDesc);
                    Log.warning("Unexpected iconX ", r.iconX);
                    Log.warning("Unexpected iconY ", r.iconY);
                    Log.warning("Unexpected pcUsage ", r.pcUsage);
                    Log.warning("Unexpected currentStatus ", r.currentStatus);
                    Log.warning("Unexpected dtimeCreated ", r.dtimeCreated);
                    Log.warning("Unexpected dtimeApproved ", r.dtimeApproved);
                    Log.warning("Unexpected macAddress ", r.macAddress);
                    Log.warning("Unexpected powerStatus ", r.powerStatus);
                    Log.warning("Unexpected currentStuId ", r.currentStuId);
                    Log.warning("Unexpected currentCourse ", r.currentCourse);
                    Log.warning("Unexpected currentUnit ", r.currentUnit);
                    Log.warning("Unexpected currentVersion ", r.currentVersion);
                }
            }

            assertTrue(found2, "client_pc 2 not found");
            assertTrue(found3, "client_pc 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting client_pc: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM client_pc");
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
