package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A raw "client_pc" record.
 */
public final class RawClientPc extends RecBase implements Comparable<RawClientPc> {

    /** The table name. */
    public static final String TABLE_NAME = "client_pc";

    /** A field name. */
    private static final String FLD_COMPUTER_ID = "computer_id";

    /** A field name. */
    private static final String FLD_TESTING_CENTER_ID = "testing_center_id";

    /** A field name. */
    private static final String FLD_STATION_NBR = "station_nbr";

    /** A field name. */
    private static final String FLD_COMPUTER_DESC = "computer_desc";

    /** A field name. */
    private static final String FLD_ICON_X = "icon_x";

    /** A field name. */
    private static final String FLD_ICON_Y = "icon_y";

    /** A field name. */
    private static final String FLD_PC_USAGE = "pc_usage";

    /** A field name. */
    private static final String FLD_CURRENT_STATUS = "current_status";

    /** A field name. */
    private static final String FLD_DTIME_CREATED = "dtime_created";

    /** A field name. */
    private static final String FLD_DTIME_APPROVED = "dtime_approved";

    /** A field name. */
    private static final String FLD_MAC_ADDRESS = "mac_address";

    /** A field name. */
    private static final String FLD_POWER_STATUS = "power_status";

    /** A field name. */
    private static final String FLD_POWER_ON_DUE = "power_on_due";

    /** A field name. */
    private static final String FLD_LAST_PING = "last_ping";

    /** A field name. */
    private static final String FLD_CURRENT_STU_ID = "current_stu_id";

    /** A field name. */
    private static final String FLD_CURRENT_COURSE = "current_course";

    /** A field name. */
    private static final String FLD_CURRENT_UNIT = "current_unit";

    /** A field name. */
    private static final String FLD_CURRENT_VERSION = "current_version";

    /** A value for "pc_usage". */
    public static final String USAGE_ONLINE = "O";

    /** A value for "pc_usage". */
    public static final String USAGE_PAPER = "P";

    /** A value for "pc_usage". */
    public static final String USAGE_BOTH = "B";

    /** A value for "pc_usage". */
    public static final String USAGE_WHEELCHAIR = "W";

    /** A value for "current_status". */
    public static final Integer STATUS_ERROR = Integer.valueOf(-1);

    /** A value for "current_status". */
    public static final Integer STATUS_UNINITIALIZED = Integer.valueOf(0);

    /** A value for "current_status". */
    public static final Integer STATUS_LOCKED = Integer.valueOf(4);

    /** A value for "current_status". */
    public static final Integer STATUS_PAPER_ONLY = Integer.valueOf(5);

    /** A value for "current_status". */
    public static final Integer STATUS_AWAIT_STUDENT = Integer.valueOf(6);

    /** A value for "current_status". */
    public static final Integer STATUS_TAKING_EXAM = Integer.valueOf(7);

    /** A value for "current_status". */
    public static final Integer STATUS_EXAM_RESULTS = Integer.valueOf(8);

    /** A value for "current_status". */
    public static final Integer STATUS_FORCE_SUBMIT = Integer.valueOf(9);

    /** A value for "current_status". */
    public static final Integer STATUS_CANCEL_EXAM = Integer.valueOf(12);

    /** A value for "current_status". */
    public static final Integer STATUS_LOGIN_NOCHECK = Integer.valueOf(13);

    /** A value for "power_status". */
    public static final String POWER_OFF = "0";

    /** A value for "power_status". */
    public static final String POWER_TURNING_ON = "1";

    /** A value for "power_status". */
    public static final String POWER_REPORTING_ON = "2";

    /** The 'computer_id' field value. */
    public String computerId;

    /** The 'testing_center_id' field value. */
    public String testingCenterId;

    /** The 'station_nbr' field value. */
    public String stationNbr;

    /** The 'computer_desc' field value. */
    public String computerDesc;

    /** The 'icon_x' field value. */
    public Integer iconX;

    /** The 'icon_y' field value. */
    public Integer iconY;

    /** The 'pc_usage' field value. */
    public String pcUsage;

    /** The 'current_status' field value. */
    public Integer currentStatus;

    /** The 'dtime_created' field value. */
    public LocalDateTime dtimeCreated;

    /** The 'dtime_approved' field value. */
    public LocalDateTime dtimeApproved;

    /** The 'mac_address' field value. */
    public String macAddress;

    /** The 'power_status' field value. */
    public String powerStatus;

    /** The 'power_on_due' field value. */
    public Integer powerOnDue;

    /** The 'past_ping' field value. */
    public Integer lastPing;

    /** The 'current_stu_id' field value. */
    public String currentStuId;

    /** The 'current_course' field value. */
    public String currentCourse;

    /** The 'current_unit' field value. */
    public Integer currentUnit;

    /** The 'current_version' field value. */
    public String currentVersion;

    /**
     * Constructs a new {@code RawClientPc}.
     */
    private RawClientPc() {

        super();
    }

    /**
     * Constructs a new {@code RawClientPc}.
     *
     * @param theComputerId      the computer ID
     * @param theTestingCenterId the testing center ID
     * @param theStationNbr      the station number
     * @param theComputerDesc    the computer description
     * @param theIconX           the X coordinate of the icon on the map
     * @param theIconY           the Y coordinate of the icon on the map
     * @param thePcUsage         the PC usage code
     * @param theCurrentStatus   the current machine status
     * @param theDtimeCreated    the date/time the record was created
     * @param theDtimeApproved   the date/time the record was approved
     * @param theMacAddress      the MAC address, in hex, 12 characters long
     * @param thePowerStatus     the current power status of the station
     * @param thePowerOnDue      the time (second of day) when station is due to power on
     * @param theLastPing        the last time (second of day) when traffic was received from the station
     * @param theCurrentStuId    the student currently assigned to the station
     * @param theCurrentCourse   the course ID of the exam currently assigned to the station
     * @param theCurrentUnit     the unit of the exam currently assigned to the station
     * @param theCurrentVersion  the version of the exam currently assigned to the station
     */
    public RawClientPc(final String theComputerId, final String theTestingCenterId,
                       final String theStationNbr, final String theComputerDesc, final Integer theIconX,
                       final Integer theIconY, final String thePcUsage, final Integer theCurrentStatus,
                       final LocalDateTime theDtimeCreated, final LocalDateTime theDtimeApproved,
                       final String theMacAddress, final String thePowerStatus, final Integer thePowerOnDue,
                       final Integer theLastPing, final String theCurrentStuId, final String theCurrentCourse,
                       final Integer theCurrentUnit, final String theCurrentVersion) {
        super();

        this.computerId = theComputerId;
        this.testingCenterId = theTestingCenterId;
        this.stationNbr = theStationNbr;
        this.computerDesc = theComputerDesc;
        this.iconX = theIconX;
        this.iconY = theIconY;
        this.pcUsage = thePcUsage;
        this.currentStatus = theCurrentStatus;
        this.dtimeCreated = theDtimeCreated;
        this.dtimeApproved = theDtimeApproved;
        this.macAddress = theMacAddress;
        this.powerStatus = thePowerStatus;
        this.powerOnDue = thePowerOnDue;
        this.lastPing = theLastPing;
        this.currentStuId = theCurrentStuId;
        this.currentCourse = theCurrentCourse;
        this.currentUnit = theCurrentUnit;
        this.currentVersion = theCurrentVersion;
    }

    /**
     * Extracts a "testing_centers" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawClientPc fromResultSet(final ResultSet rs) throws SQLException {

        final RawClientPc result = new RawClientPc();

        result.computerId = getStringField(rs, FLD_COMPUTER_ID);
        result.testingCenterId = getStringField(rs, FLD_TESTING_CENTER_ID);
        result.stationNbr = getStringField(rs, FLD_STATION_NBR);
        result.computerDesc = getStringField(rs, FLD_COMPUTER_DESC);
        result.iconX = getIntegerField(rs, FLD_ICON_X);
        result.iconY = getIntegerField(rs, FLD_ICON_Y);
        result.pcUsage = getStringField(rs, FLD_PC_USAGE);
        result.currentStatus = getIntegerField(rs, FLD_CURRENT_STATUS);
        result.dtimeCreated = getDateTimeField(rs, FLD_DTIME_CREATED);
        result.dtimeApproved = getDateTimeField(rs, FLD_DTIME_APPROVED);
        result.macAddress = getStringField(rs, FLD_MAC_ADDRESS);
        result.powerStatus = getStringField(rs, FLD_POWER_STATUS);
        result.powerOnDue = getIntegerField(rs, FLD_POWER_ON_DUE);
        result.lastPing = getIntegerField(rs, FLD_LAST_PING);
        result.currentStuId = getStringField(rs, FLD_CURRENT_STU_ID);
        result.currentCourse = getStringField(rs, FLD_CURRENT_COURSE);
        result.currentUnit = getIntegerField(rs, FLD_CURRENT_UNIT);
        result.currentVersion = getStringField(rs, FLD_CURRENT_VERSION);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to which to compare
     * @return a negative value, 0, or a positive value as this object is less than, equal to, or greater than
     *         {@code o}.
     */
    @Override
    public int compareTo(final RawClientPc o) {

        int result = compareAllowingNull(this.testingCenterId, o.testingCenterId);

        if (result == 0) {
            result = compareAllowingNull(this.stationNbr, o.stationNbr);
            if (result == 0) {
                result = compareAllowingNull(this.dtimeCreated, o.dtimeCreated);
            }
        }

        return result;
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(40);

        appendField(htm, FLD_COMPUTER_ID, this.computerId);
        htm.add(DIVIDER);
        appendField(htm, FLD_TESTING_CENTER_ID, this.testingCenterId);
        htm.add(DIVIDER);
        appendField(htm, FLD_STATION_NBR, this.stationNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_COMPUTER_DESC, this.computerDesc);
        htm.add(DIVIDER);
        appendField(htm, FLD_ICON_X, this.iconX);
        htm.add(DIVIDER);
        appendField(htm, FLD_ICON_Y, this.iconY);
        htm.add(DIVIDER);
        appendField(htm, FLD_PC_USAGE, this.pcUsage);
        htm.add(DIVIDER);
        appendField(htm, FLD_CURRENT_STATUS, this.currentStatus);
        htm.add(DIVIDER);
        appendField(htm, FLD_DTIME_CREATED, this.dtimeCreated);
        htm.add(DIVIDER);
        appendField(htm, FLD_DTIME_APPROVED, this.dtimeApproved);
        htm.add(DIVIDER);
        appendField(htm, FLD_MAC_ADDRESS, this.macAddress);
        htm.add(DIVIDER);
        appendField(htm, FLD_POWER_STATUS, this.powerStatus);
        htm.add(DIVIDER);
        appendField(htm, FLD_POWER_ON_DUE, this.powerOnDue);
        htm.add(DIVIDER);
        appendField(htm, FLD_LAST_PING, this.lastPing);
        htm.add(DIVIDER);
        appendField(htm, FLD_CURRENT_STU_ID, this.currentStuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_CURRENT_COURSE, this.currentCourse);
        htm.add(DIVIDER);
        appendField(htm, FLD_CURRENT_UNIT, this.currentUnit);
        htm.add(DIVIDER);
        appendField(htm, FLD_CURRENT_VERSION, this.currentVersion);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.computerId)
                + Objects.hashCode(this.testingCenterId)
                + Objects.hashCode(this.stationNbr)
                + Objects.hashCode(this.computerDesc)
                + Objects.hashCode(this.iconX)
                + Objects.hashCode(this.iconY)
                + Objects.hashCode(this.pcUsage)
                + Objects.hashCode(this.currentStatus)
                + Objects.hashCode(this.dtimeCreated)
                + Objects.hashCode(this.dtimeApproved)
                + Objects.hashCode(this.macAddress)
                + Objects.hashCode(this.powerStatus)
                + Objects.hashCode(this.powerOnDue)
                + Objects.hashCode(this.lastPing)
                + Objects.hashCode(this.currentStuId)
                + Objects.hashCode(this.currentCourse)
                + Objects.hashCode(this.currentUnit)
                + Objects.hashCode(this.currentVersion);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final RawClientPc rec) {
            equal = Objects.equals(this.computerId, rec.computerId)
                    && Objects.equals(this.testingCenterId, rec.testingCenterId)
                    && Objects.equals(this.stationNbr, rec.stationNbr)
                    && Objects.equals(this.computerDesc, rec.computerDesc)
                    && Objects.equals(this.iconX, rec.iconX)
                    && Objects.equals(this.iconY, rec.iconY)
                    && Objects.equals(this.pcUsage, rec.pcUsage)
                    && Objects.equals(this.currentStatus, rec.currentStatus)
                    && Objects.equals(this.dtimeCreated, rec.dtimeCreated)
                    && Objects.equals(this.dtimeApproved, rec.dtimeApproved)
                    && Objects.equals(this.macAddress, rec.macAddress)
                    && Objects.equals(this.powerStatus, rec.powerStatus)
                    && Objects.equals(this.powerOnDue, rec.powerOnDue)
                    && Objects.equals(this.lastPing, rec.lastPing)
                    && Objects.equals(this.currentStuId, rec.currentStuId)
                    && Objects.equals(this.currentCourse, rec.currentCourse)
                    && Objects.equals(this.currentUnit, rec.currentUnit)
                    && Objects.equals(this.currentVersion, rec.currentVersion);
        } else {
            equal = false;
        }

        return equal;
    }
}
