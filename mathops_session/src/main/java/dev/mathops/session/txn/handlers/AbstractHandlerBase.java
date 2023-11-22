package dev.mathops.session.txn.handlers;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.rawlogic.RawClientPcLogic;
import dev.mathops.db.rawlogic.RawStudentLogic;
import dev.mathops.db.rawlogic.RawTestingCenterLogic;
import dev.mathops.db.rawrecord.RawClientPc;
import dev.mathops.db.rawrecord.RawStudent;
import dev.mathops.db.rawrecord.RawTestingCenter;
import dev.mathops.session.txn.messages.AbstractReplyBase;
import dev.mathops.session.txn.messages.AbstractRequestBase;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Calendar;

/**
 * The base class for handlers of messages sent by clients.
 */
public abstract class AbstractHandlerBase {

    /** An object on which to synchronize access to statics. */
    private static final Object SYNCH = new Object();

    /** A static counter to ensure unique, monotonic serial numbers. */
    private static Long lastSerial = Long.valueOf(0L);

    /** The ID of the machine from which the request came. */
    private String machineId;

    /** The client computer from which the request came. */
    private RawClientPc client;

    /** The testing center that the client computer is registered in. */
    private RawTestingCenter testingCenter;

    /** The logged in student. */
    private RawStudent student;

    /** The database profile of the request. */
    final DbProfile dbProfile;

    /**
     * Construct a new {@code HandlerBase}.
     *
     * @param theDbProfile the database profile under which the handler is being accessed
     */
    AbstractHandlerBase(final DbProfile theDbProfile) {

        if (theDbProfile == null) {
            throw new IllegalArgumentException("Context may not be null");
        }

        this.dbProfile = theDbProfile;
    }

    /**
     * Process a message from the client. Subclasses override this method to implement their particular processing
     * logic.
     *
     * @param cache   the data cache
     * @param message the message received from the client
     * @return the reply to be sent to the client, or null if the connection should be closed
     * @throws SQLException if there is an error accessing the database
     */
    public abstract String process(final Cache cache, AbstractRequestBase message) throws SQLException;

    /**
     * Gets the machine ID.
     *
     * @return the machine ID
     */
    final String getMachineId() {

        return this.machineId;
    }

    /**
     * Gets the client.
     *
     * @return the client
     */
    final RawClientPc getClient() {

        return this.client;
    }

    /**
     * Gets the testing center.
     *
     * @return the testing center
     */
    final RawTestingCenter getTestingCenter() {

        return this.testingCenter;
    }

    /**
     * Sets the testing center.
     *
     * @param theTestingCenter the new testing center
     */
    final void setTestingCenter(final RawTestingCenter theTestingCenter) {

        this.testingCenter = theTestingCenter;
    }

    /**
     * Gets the student.
     *
     * @return the student
     */
    final RawStudent getStudent() {

        return this.student;
    }

    /**
     * Sets the student.
     *
     * @param theStudent the new student
     */
    final void setStudent(final RawStudent theStudent) {

        this.student = theStudent;
    }

    /**
     * Extracts the machine ID from a request and stores it.
     *
     * @param message the request received from the client.
     */
    final void setMachineId(final AbstractRequestBase message) {

        this.machineId = message.machineId;
    }

    /**
     * Performs a "touch" on the ClientPC record, updating its last ping time.
     *
     * @param cache the data cache
     */
    final void touch(final Cache cache) {

        final int pingTime = TemporalUtils.secondOfDay(LocalTime.now());
        try {
            RawClientPcLogic.updateLastPing(cache, this.machineId, Integer.valueOf(pingTime));
        } catch (final SQLException ex) {
            Log.warning("Failed to update 'last_ping' on testing station", ex);
        }
    }

    /**
     * Load the student information.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @param reply     the reply to populate with errors if unsuccessful
     * @return true if request completed successfully; false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    final boolean loadStudentInfo(final Cache cache, final String studentId,
                                  final AbstractReplyBase reply) throws SQLException {

        boolean ok;

        if (this.machineId != null) {
            this.client = RawClientPcLogic.query(cache, this.machineId);
            if (this.client == null) {
                reply.error = "Computer " + this.machineId + " is not in the database.";
                Log.info(reply.error);
                return false;
            }

            this.testingCenter = RawTestingCenterLogic.query(cache, this.client.testingCenterId);
            if (this.testingCenter == null) {
                reply.error = "Computer " + this.machineId + " is not in a valid testing center";
                Log.info(reply.error);
                return false;
            }

            final String stuId = studentId == null ? this.client.currentStuId : studentId;

            this.student = RawStudentLogic.query(cache, stuId, true);

            if (this.student == null) {
                reply.error = "Failed to obtain student info.";
                Log.info(reply.error);
                return false;
            }

            ok = true;
        } else {
            // "Testing center" is 0 (Public Internet)
            this.client = null;
            this.testingCenter = RawTestingCenterLogic.query(cache, "0");
            if (this.testingCenter == null) {
                reply.error = "Unable to query public internet site data";
                Log.info(reply.error);
                return false;
            }
            ok = true;

            if ("GUEST".equals(studentId) || "AACTUTOR".equals(studentId) || "ETEXT".equals(studentId)) {
                this.student = RawStudentLogic.makeFakeStudent("GUEST", CoreConstants.EMPTY, "GUEST");
            } else {
                this.student = RawStudentLogic.query(cache, studentId, true);
                if (this.student == null) {
                    reply.error = "Unable to look up your student info.";
                    Log.info(reply.error);
                    ok = false;
                }
            }
        }

        return ok;
    }

    /**
     * Generate a unique serial number for records of events. Serial numbers are guaranteed to be monotonically
     * increasing in order of generation (practice serial numbers are negative and monotonically decreasing). The format
     * of a serial number is YYDDDSSSSS where YY is the 2-digit year modulo 20, DDD is the day of the year (1-366), and
     * SSSSS is a seconds counter, which will try to be accurate to the extent this does not violate uniqueness and
     * monotonicity.
     *
     * @param isPractice {@code true} if the serial number is being generated for a practice exam
     * @return the serial number
     */
    public static long generateSerialNumber(final boolean isPractice) {

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        long ser = (long) ((cal.get(Calendar.YEAR) - 2000) % 20 * 100000000
                + cal.get(Calendar.DAY_OF_YEAR) * 100000 + cal.get(Calendar.HOUR_OF_DAY) * 3600
                + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND));

        synchronized (SYNCH) {
            if (ser <= lastSerial.longValue()) {
                ser = lastSerial.longValue() + 1L;
            }

            lastSerial = Long.valueOf(ser);
        }

        return isPractice ? -ser : ser;
    }

    ///**
    // * Generates a date/time string that corresponds to a serial number.
    // *
    // * @param theSerial the serial number
    // * @return the resulting date/time string
    // */
    // public static String serialToDateString(final long theSerial) {
    //
    // final long serial = Math.abs(theSerial);
    //
    // final long longYear = serial / 100000000;
    // int remains = (int) (serial - longYear * 100000000);
    // final int year = (int) (longYear + 2000);
    //
    // final int dayOfYear = remains / 100000;
    // remains -= dayOfYear * 100000;
    //
    // final int hourOfDay = remains / 3600;
    // remains -= hourOfDay * 3600;
    //
    // final int minute = remains / 60;
    // remains -= minute * 60;
    // final int second = remains;
    //
    // return TemporalUtils.FMT_MDY_AT_HM_A.format(LocalDateTime
    // .of(LocalDate.ofYearDay(year, dayOfYear), LocalTime.of(hourOfDay, minute, second)));
    // }
}
