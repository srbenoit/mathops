package dev.mathops.session.txn.handlers;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.db.old.rawrecord.RawTestingCenter;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.TestingStationInfoReply;
import dev.mathops.session.txn.messages.TestingStationInfoRequest;

import java.sql.SQLException;

/**
 * A handler for requests for a student's status regarding the survey questions attached to a particular version of an
 * exam. The current list of survey questions and the student's current answers are sent in reply.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class TestingStationInfoHandler extends AbstractHandlerBase {

    /**
     * Construct a new {@code TestingStationInfoHandler}.
     */
    public TestingStationInfoHandler() {

        super();
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

        if (message instanceof TestingStationInfoRequest) {
            try {
                result = processRequest(cache);
            } catch (final SQLException ex) {
                Log.warning(ex);
                final TestingStationInfoReply reply = new TestingStationInfoReply();
                reply.error = "Error processing request";
                result = reply.toXml();
            }
        } else {
            final String clsName = message.getClass().getName();
            Log.info("TestingStationInfoHandler called with ", clsName);

            final TestingStationInfoReply reply = new TestingStationInfoReply();
            reply.error = "Invalid request type for testing station info request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a request from the client.
     *
     * @param cache the data cache
     * @return the generated reply XML to send to the client
     * @throws SQLException if there is an error accessing the database
     */
    private String processRequest(final Cache cache) throws SQLException {

        final TestingStationInfoReply reply = new TestingStationInfoReply();

        final String machineId = getMachineId();
        final SystemData systemData = cache.getSystemData();

        final RawClientPc pc = systemData.getClientPc(machineId);

        if (pc == null) {
            reply.error = "Unable to query client computer record.";
        } else if (pc.testingCenterId != null) {

            final RawTestingCenter center = systemData.getTestingCenter(pc.testingCenterId);

            if (center == null) {
                Log.warning("Unable to query for testing center ", pc.testingCenterId);
                reply.error = "This station is not in a valid testing center";
            } else {
                reply.testingCenterName = center.tcName;
                reply.stationNumber = pc.stationNbr;
                reply.stationUsage = pc.pcUsage;
                reply.status = pc.currentStatus;
            }
        } else {
            Log.warning("Test station ", reply.stationNumber, " info - station not valid");
            reply.error = "This station is not in a valid testing center";
        }

        return reply.toXml();
    }
}
