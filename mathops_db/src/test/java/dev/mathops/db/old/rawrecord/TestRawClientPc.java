package dev.mathops.db.old.rawrecord;

import dev.mathops.core.CoreConstants;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

/**
 * Tests for the {@code RawClientPc} class.
 */
final class TestRawClientPc {

    /** A test field value. */
    private static final String TEST_COMPUTER_ID = "12345678901234567890";

    /** A test field value. */
    private static final String TEST_TESTING_CENTER_ID = "20";

    /** A test field value. */
    private static final String TEST_STATION_NBR = "999";

    /** A test field value. */
    private static final String TEST_COMPUTER_DESC = "Description";

    /** A test field value. */
    private static final Integer TEST_ICON_X = Integer.valueOf(1234);

    /** A test field value. */
    private static final Integer TEST_ICON_Y = Integer.valueOf(5678);

    /** A test field value. */
    private static final String TEST_PC_USAGE = RawClientPc.USAGE_ONLINE;

    /** A test field value. */
    private static final Integer TEST_CURRENT_STATUS = RawClientPc.STATUS_AWAIT_STUDENT;

    /** A test field value. */
    private static final LocalDateTime TEST_DTIME_CREATED = LocalDateTime.of(2021, 1, 2, 3, 4, 5);

    /** A test field value. */
    private static final LocalDateTime TEST_DTIME_APPROVED = LocalDateTime.of(2022, 6, 7, 8, 9, 10);

    /** A test field value. */
    private static final String TEST_MAC_ADDRESS = "010203ABCDEF";

    /** A test field value. */
    private static final String TEST_POWER_STATUS = RawClientPc.POWER_REPORTING_ON;

    /** A test field value. */
    private static final Integer TEST_POWER_ON_DUE = Integer.valueOf(800);

    /** A test field value. */
    private static final Integer TEST_LAST_PING = Integer.valueOf(15800);

    /** A test field value. */
    private static final String TEST_CURRENT_STU_ID = "888888888";

    /** A test field value. */
    private static final String TEST_CURRENT_COURSE = "M 100P";

    /** A test field value. */
    private static final Integer TEST_CURRENT_UNIT = Integer.valueOf(1);

    /** A test field value. */
    private static final String TEST_CURRENT_VERSION = "MPTTC";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER1 = String.join(RecBase.DIVIDER,
            "computer_id=12345678901234567890",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER2 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            "testing_center_id=20",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER3 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "station_nbr=999",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER4 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "computer_desc=Description",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER5 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "icon_x=1234",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER6 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "icon_y=5678",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER7 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "pc_usage=O",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER8 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "current_status=6",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER9 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "dtime_created=2021-01-02T03:04:05",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER10 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "dtime_approved=2022-06-07T08:09:10",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER11 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "mac_address=010203ABCDEF",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER12 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "power_status=2",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER13 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "power_on_due=800",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER14 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "last_ping=15800",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER15 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "current_stu_id=888888888",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER16 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "current_course=M 100P",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER17 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "current_unit=1",
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER18 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "current_version=MPTTC");

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER19 = String.join(RecBase.DIVIDER,
            "computer_id=12345678901234567890",
            "testing_center_id=20",
            "station_nbr=999",
            "computer_desc=Description",
            "icon_x=1234",
            "icon_y=5678",
            "pc_usage=O",
            "current_status=6",
            "dtime_created=2021-01-02T03:04:05",
            "dtime_approved=2022-06-07T08:09:10",
            "mac_address=010203ABCDEF",
            "power_status=2",
            "power_on_due=800",
            "last_ping=15800",
            "current_stu_id=888888888",
            "current_course=M 100P",
            "current_unit=1",
            "current_version=MPTTC");

    /**
     * Constructs a new {@code IvtRawClientPc}.
     */
    TestRawClientPc() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawClientPc obj = new RawClientPc(TEST_COMPUTER_ID, TEST_TESTING_CENTER_ID,
                TEST_STATION_NBR, TEST_COMPUTER_DESC, TEST_ICON_X, TEST_ICON_Y, TEST_PC_USAGE,
                TEST_CURRENT_STATUS, TEST_DTIME_CREATED, TEST_DTIME_APPROVED, TEST_MAC_ADDRESS,
                TEST_POWER_STATUS, TEST_POWER_ON_DUE, TEST_LAST_PING, TEST_CURRENT_STU_ID,
                TEST_CURRENT_COURSE, TEST_CURRENT_UNIT, TEST_CURRENT_VERSION);

        assertEquals(TEST_COMPUTER_ID, obj.computerId, "Invalid computer_id value after constructor");
        assertEquals(TEST_TESTING_CENTER_ID, obj.testingCenterId, "Invalid testing_center_id value after constructor");
        assertEquals(TEST_STATION_NBR, obj.stationNbr, "Invalid station_nbr value after constructor");
        assertEquals(TEST_COMPUTER_DESC, obj.computerDesc, "Invalid computer_desc value after constructor");
        assertEquals(TEST_ICON_X, obj.iconX, "Invalid icon_x value after constructor");
        assertEquals(TEST_ICON_Y, obj.iconY, "Invalid icon_y value after constructor");
        assertEquals(TEST_PC_USAGE, obj.pcUsage, "Invalid pc_usage value after constructor");
        assertEquals(TEST_CURRENT_STATUS, obj.currentStatus, "Invalid current_status value after constructor");
        assertEquals(TEST_DTIME_CREATED, obj.dtimeCreated, "Invalid dtime_created value after constructor");
        assertEquals(TEST_DTIME_APPROVED, obj.dtimeApproved, "Invalid dtime_approved value after constructor");
        assertEquals(TEST_MAC_ADDRESS, obj.macAddress, "Invalid mac_address value after constructor");
        assertEquals(TEST_POWER_STATUS, obj.powerStatus, "Invalid power_status value after constructor");
        assertEquals(TEST_POWER_ON_DUE, obj.powerOnDue, "Invalid power_on_due value after constructor");
        assertEquals(TEST_LAST_PING, obj.lastPing, "Invalid last_ping value after constructor");
        assertEquals(TEST_CURRENT_STU_ID, obj.currentStuId, "Invalid current_stu_id value after constructor");
        assertEquals(TEST_CURRENT_COURSE, obj.currentCourse, "Invalid current_course value after constructor");
        assertEquals(TEST_CURRENT_UNIT, obj.currentUnit, "Invalid current_unit value after constructor");
        assertEquals(TEST_CURRENT_VERSION, obj.currentVersion, "Invalid current_version value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0002() {

        final RawClientPc obj = new RawClientPc(TEST_COMPUTER_ID, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER1, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0003() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER1);

        assertEquals(TEST_COMPUTER_ID, obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0004() {

        final RawClientPc obj = new RawClientPc(null, TEST_TESTING_CENTER_ID, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER2, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0005() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER2);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertEquals(TEST_TESTING_CENTER_ID, obj.testingCenterId,
                "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0006() {

        final RawClientPc obj = new RawClientPc(null, null, TEST_STATION_NBR, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER3, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0007() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER3);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertEquals(TEST_STATION_NBR, obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0008() {

        final RawClientPc obj = new RawClientPc(null, null, null, TEST_COMPUTER_DESC, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER4, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0009() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER4);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertEquals(TEST_COMPUTER_DESC, obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0010() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, TEST_ICON_X, null, null,
                null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER5, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0011() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER5);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertEquals(TEST_ICON_X, obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0012() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, null, TEST_ICON_Y, null,
                null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER6, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0013() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER6);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertEquals(TEST_ICON_Y, obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0014() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, null, null, TEST_PC_USAGE,
                null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER7, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0015() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER7);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertEquals(TEST_PC_USAGE, obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0016() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, null, null, null,
                TEST_CURRENT_STATUS, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER8, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0017() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER8);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertEquals(TEST_CURRENT_STATUS, obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0018() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, null, null, null, null,
                TEST_DTIME_CREATED, null, null, null, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER9, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0019() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER9);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertEquals(TEST_DTIME_CREATED, obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0020() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, null, null, null, null,
                null, TEST_DTIME_APPROVED, null, null, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER10, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0021() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER10);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertEquals(TEST_DTIME_APPROVED, obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0022() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, null, null, null, null,
                null, null, TEST_MAC_ADDRESS, null, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER11, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0023() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER11);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertEquals(TEST_MAC_ADDRESS, obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0024() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, null, null, null, null,
                null, null, null, TEST_POWER_STATUS, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER12, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0025() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER12);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertEquals(TEST_POWER_STATUS, obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0026() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, null, null, null, null,
                null, null, null, null, TEST_POWER_ON_DUE, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER13, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0027() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER13);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertEquals(TEST_POWER_ON_DUE, obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0028() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, null, null, null, null,
                null, null, null, null, null, TEST_LAST_PING, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER14, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0029() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER14);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertEquals(TEST_LAST_PING, obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0030() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, TEST_CURRENT_STU_ID, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER15, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0031() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER15);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertEquals(TEST_CURRENT_STU_ID, obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0032() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, TEST_CURRENT_COURSE, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER16, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0033() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER16);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertEquals(TEST_CURRENT_COURSE, obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0034() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, TEST_CURRENT_UNIT, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER17, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0035() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER17);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertEquals(TEST_CURRENT_UNIT, obj.currentUnit, "Invalid current_unit value after deserialization");
        assertNull(obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0036() {

        final RawClientPc obj = new RawClientPc(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, TEST_CURRENT_VERSION);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER18, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0037() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER18);

        assertNull(obj.computerId, "Invalid computer_id value after deserialization");
        assertNull(obj.testingCenterId, "Invalid testing_center_id value after deserialization");
        assertNull(obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertNull(obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertNull(obj.iconX, "Invalid icon_x value after deserialization");
        assertNull(obj.iconY, "Invalid icon_y value after deserialization");
        assertNull(obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertNull(obj.currentStatus, "Invalid current_status value after deserialization");
        assertNull(obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertNull(obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertNull(obj.macAddress, "Invalid mac_address value after deserialization");
        assertNull(obj.powerStatus, "Invalid power_status value after deserialization");
        assertNull(obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertNull(obj.lastPing, "Invalid last_ping value after deserialization");
        assertNull(obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertNull(obj.currentCourse, "Invalid current_course value after deserialization");
        assertNull(obj.currentUnit, "Invalid current_unit value after deserialization");
        assertEquals(TEST_CURRENT_VERSION, obj.currentVersion, "Invalid current_version value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0038() {

        final RawClientPc obj = new RawClientPc(TEST_COMPUTER_ID, TEST_TESTING_CENTER_ID,
                TEST_STATION_NBR, TEST_COMPUTER_DESC, TEST_ICON_X, TEST_ICON_Y, TEST_PC_USAGE,
                TEST_CURRENT_STATUS, TEST_DTIME_CREATED, TEST_DTIME_APPROVED, TEST_MAC_ADDRESS,
                TEST_POWER_STATUS, TEST_POWER_ON_DUE, TEST_LAST_PING, TEST_CURRENT_STU_ID,
                TEST_CURRENT_COURSE, TEST_CURRENT_UNIT, TEST_CURRENT_VERSION);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER19, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0039() {

        final RawClientPc obj = RawClientPc.parse(EXPECT_SER19);

        assertEquals(TEST_COMPUTER_ID, obj.computerId, "Invalid computer_id value after deserialization");
        assertEquals(TEST_TESTING_CENTER_ID, obj.testingCenterId, "Invalid testing_center_id value after " +
                "deserialization");
        assertEquals(TEST_STATION_NBR, obj.stationNbr, "Invalid station_nbr value after deserialization");
        assertEquals(TEST_COMPUTER_DESC, obj.computerDesc, "Invalid computer_desc value after deserialization");
        assertEquals(TEST_ICON_X, obj.iconX, "Invalid icon_x value after deserialization");
        assertEquals(TEST_ICON_Y, obj.iconY, "Invalid icon_y value after deserialization");
        assertEquals(TEST_PC_USAGE, obj.pcUsage, "Invalid pc_usage value after deserialization");
        assertEquals(TEST_CURRENT_STATUS, obj.currentStatus, "Invalid current_status value after deserialization");
        assertEquals(TEST_DTIME_CREATED, obj.dtimeCreated, "Invalid dtime_created value after deserialization");
        assertEquals(TEST_DTIME_APPROVED, obj.dtimeApproved, "Invalid dtime_approved value after deserialization");
        assertEquals(TEST_MAC_ADDRESS, obj.macAddress, "Invalid mac_address value after deserialization");
        assertEquals(TEST_POWER_STATUS, obj.powerStatus, "Invalid power_status value after deserialization");
        assertEquals(TEST_POWER_ON_DUE, obj.powerOnDue, "Invalid power_on_due value after deserialization");
        assertEquals(TEST_LAST_PING, obj.lastPing, "Invalid last_ping value after deserialization");
        assertEquals(TEST_CURRENT_STU_ID, obj.currentStuId, "Invalid current_stu_id value after deserialization");
        assertEquals(TEST_CURRENT_COURSE, obj.currentCourse, "Invalid current_course value after deserialization");
        assertEquals(TEST_CURRENT_UNIT, obj.currentUnit, "Invalid current_unit value after deserialization");
        assertEquals(TEST_CURRENT_VERSION, obj.currentVersion, "Invalid current_version value after deserialization");
    }
}
