package dev.mathops.session.txn.handlers;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.TestingStationStatusReply;
import dev.mathops.session.txn.messages.TestingStationStatusRequest;

import java.sql.SQLException;

/**
 * A handler for requests for a testing station's current status. These are sent periodically by testing stations.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class TestingStationStatusHandler extends AbstractHandlerBase {

    /**
     * Construct a new {@code TestingStationStatusHandler}.
     *
     * @param theDbProfile the database profile under which the handler is being accessed
     */
    public TestingStationStatusHandler(final DbProfile theDbProfile) {

        super(theDbProfile);
    }

    /**
     * Processes a message from the client.
     *
     * @param cache   the data cache
     * @param message the message received from the client
     * @return the reply to be sent to the client, or null if the connection should be closed
     */
    @Override
    public String process(final Cache cache, final AbstractRequestBase message) {

        setMachineId(message);
        touch(cache);

        String result;

        if (message instanceof TestingStationStatusRequest) {
            try {
                result = processRequest(cache);
            } catch (final SQLException ex) {
                Log.warning(ex);

                final TestingStationStatusReply reply = new TestingStationStatusReply();
                reply.error = "Error processing testing station status request";
                result = reply.toXml();
            }
        } else {
            Log.warning("TestingStationStatusHandler called with ",
                    message.getClass().getName());

            final TestingStationStatusReply reply = new TestingStationStatusReply();
            reply.error = "Invalid request type for testing station status request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a request from the client.
     *
     * @param cache the data cache
     * @return the generated reply XML to send to the client
     * @throws SQLException if there was an error accessing the database
     */
    private String processRequest(final Cache cache) throws SQLException {

        final TestingStationStatusReply reply = new TestingStationStatusReply();

        // Query client computer to obtain the status information
        final RawClientPc pc = RawClientPcLogic.query(cache, getMachineId());

        if (pc == null) {
            reply.error = "Unable to query testing station status.";
        } else {
            reply.stationUsage = pc.pcUsage;
            reply.status = pc.currentStatus;
            reply.course = pc.currentCourse;
            reply.unit = pc.currentUnit;
            reply.version = pc.currentVersion;

            // Query student only if one is assigned to the station
            if ("GROUP".equalsIgnoreCase(pc.currentStuId)) {
                reply.studentId = "GROUP";
            } else if (pc.currentStuId != null) {

                setStudent(RawStudentLogic.query(cache, pc.currentStuId, false));

                if (getStudent() == null) {
                    reply.error = "Unable to query student information.";
                    setStudent(null);
                } else {
                    reply.studentId = pc.currentStuId;
                    reply.studentName =
                            getStudent().prefName + CoreConstants.SPC + getStudent().lastName;
                }
            }

            if (RawClientPc.POWER_TURNING_ON.equals(pc.powerStatus)) {
                RawClientPcLogic.updatePowerStatus(cache, getMachineId(),
                        RawClientPc.POWER_REPORTING_ON);
            }
        }

        return reply.toXml();
    }
}
