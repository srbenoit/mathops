package dev.mathops.session.txn.handlers;

import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.TestingStationResetReply;
import dev.mathops.session.txn.messages.TestingStationResetRequest;

import java.sql.SQLException;

/**
 * A handler for requests to reset a testing station to its ground state.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class TestingStationResetHandler extends AbstractHandlerBase {

    /**
     * Construct a new {@code TestingStationResetHandler}.
     *
     * @param theDbProfile the database profile under which the handler is being accessed
     */
    public TestingStationResetHandler(final DbProfile theDbProfile) {

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

        // Validate the type of request
        if (message instanceof TestingStationResetRequest) {
            try {
                result = processRequest(cache);
            } catch (final SQLException ex) {
                Log.warning(ex);

                final TestingStationResetReply reply = new TestingStationResetReply();
                reply.error = "Error processing testing station reset request";
                result = reply.toXml();
            }
        } else {
            Log.warning("TestingStationResetHandler called with ",
                    message.getClass().getName());

            final TestingStationResetReply reply = new TestingStationResetReply();
            reply.error = "Invalid request type for testing station reset request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a message from the client.
     *
     * @param cache the data cache
     * @return the generated reply XML to send to the client
     * @throws SQLException if there is an error accessing the database
     */
    private String processRequest(final Cache cache) throws SQLException {

        final TestingStationResetReply reply = new TestingStationResetReply();

        // Query client computer to obtain the information
        final RawClientPc pc = RawClientPcLogic.query(cache, getMachineId());

        if (pc != null) {
            if (!RawClientPcLogic.updateAllCurrent(cache, pc.computerId, RawClientPc.STATUS_LOCKED,
                    null, null, null, null)) {
                reply.error = "Failed to reset testing station.";
            }
        } else {
            reply.error = "Unable to query testing station to perform a reset";
            Log.info(reply.error);
        }

        return reply.toXml();
    }
}
