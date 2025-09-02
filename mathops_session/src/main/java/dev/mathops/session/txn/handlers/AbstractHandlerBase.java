package dev.mathops.session.txn.handlers;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.schema.legacy.impl.RawClientPcLogic;
import dev.mathops.db.schema.legacy.rec.RawClientPc;
import dev.mathops.db.schema.legacy.rec.RawTestingCenter;
import dev.mathops.session.txn.messages.AbstractReplyBase;
import dev.mathops.session.txn.messages.AbstractRequestBase;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * The base class for handlers of messages sent by clients.
 */
public abstract class AbstractHandlerBase {

    /** An object on which to synchronize access to statics. */
    private static final Object SYNCH = new Object();

    /** A static counter to ensure unique, monotonic serial numbers. */
    private static Long lastSerial = Long.valueOf(0L);

    /** The ID of the machine from which the request came. */
    private String machineId = null;

    /** The client computer from which the request came. */
    private RawClientPc client = null;

    /** The testing center that the client computer is registered in. */
    private RawTestingCenter testingCenter = null;

    /** The data for the logged in student. */
    private StudentData student = null;

    /**
     * Construct a new {@code AbstractHandlerBase}.
     */
    AbstractHandlerBase() {

        // No action
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
    public abstract String process(Cache cache, AbstractRequestBase message) throws SQLException;

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
     * Gets the data on the current student.
     *
     * @return the student
     */
    final StudentData getStudentData() {

        return this.student;
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
    final boolean loadStudentInfo(final Cache cache, final String studentId, final AbstractReplyBase reply)
            throws SQLException {

        final boolean ok;

        final SystemData systemData = cache.getSystemData();

        if (this.machineId != null) {
            this.client = systemData.getClientPc(this.machineId);
            if (this.client == null) {
                reply.error = "Computer " + this.machineId + " is not in the database.";
                Log.info(reply.error);
                ok = false;
            } else {
                this.testingCenter = systemData.getTestingCenter(this.client.testingCenterId);
                if (this.testingCenter == null) {
                    reply.error = "Computer " + this.machineId + " is not in a valid testing center";
                    Log.info(reply.error);
                    ok = false;
                } else {
                    final String stuId = studentId == null ? this.client.currentStuId : studentId;
                    this.student = cache.setLoggedInUser(stuId);

                    if (this.student == null) {
                        reply.error = "Failed to obtain student info.";
                        Log.info(reply.error);
                        ok = false;
                    } else {
                        ok = true;
                    }
                }
            }
        } else {
            // "Testing center" is 0 (Public Internet)
            this.client = null;

            this.testingCenter = systemData.getTestingCenter("0");
            if (this.testingCenter == null) {
                reply.error = "Unable to query public internet site data";
                Log.info(reply.error);
                ok = false;
            } else if ("GUEST".equals(studentId) || "AACTUTOR".equals(studentId) || "ETEXT".equals(studentId)) {
                this.student = cache.setLoggedInUser("GUEST");
                ok = true;
            } else {
                this.student = cache.setLoggedInUser(studentId);
                if (this.student == null) {
                    reply.error = "Unable to look up your student info.";
                    Log.info(reply.error);
                    ok = false;
                } else {
                    ok = true;
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

        final LocalDateTime now = LocalDateTime.now();

        long ser = (long) (now.getYear() - 2000) % 20L * 100000000L
                + (long) (now.getDayOfYear() * 100000)
                + (long) (now.getHour() * 3600)
                + (long) (now.getMinute() * 60)
                + (long) now.getSecond();

        synchronized (SYNCH) {
            if (ser <= lastSerial.longValue()) {
                ser = lastSerial.longValue() + 1L;
            }

            lastSerial = Long.valueOf(ser);
        }

        return isPractice ? -ser : ser;
    }
}
