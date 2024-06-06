package dev.mathops.session.txn.handlers;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.PlacementStatusReply;
import dev.mathops.session.txn.messages.PlacementStatusRequest;

import java.sql.SQLException;
import java.util.List;

/**
 * A handler for requests for a student's placement status.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class PlacementStatusHandler extends AbstractHandlerBase {

    /**
     * Construct a new {@code PlacementStatusHandler}.
     *
     * @param theDbProfile the database profile under which the handler is being accessed
     */
    public PlacementStatusHandler(final DbProfile theDbProfile) {

        super(theDbProfile);
    }

    /**
     * Processes a message from the client.
     *
     * @param cache   the data cache
     * @param message the message received from the client
     * @return the reply to be sent to the client, or null if the connection should be closed
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public String process(final Cache cache, final AbstractRequestBase message) throws SQLException {

        setMachineId(message);
        touch(cache);

        final String result;

        // Validate the type of request
        if (message instanceof final PlacementStatusRequest request) {
            result = processRequest(cache, request);
        } else {
            Log.info("PlacementStatusHandler called with ", message.getClass().getName());

            final PlacementStatusReply reply = new PlacementStatusReply();
            reply.error = "Invalid request type for placement status request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a request from the client.
     *
     * @param cache   the data cache
     * @param request the {@code PlacementStatusRequest} received from the client
     * @return the generated reply XML to send to the client
     * @throws SQLException if there is an error accessing the database
     */
    private String processRequest(final Cache cache, final PlacementStatusRequest request) throws SQLException {

        final PlacementStatusReply reply = new PlacementStatusReply();

        if (loadStudentInfo(cache, request.studentId, reply)) {
            LogBase.setSessionInfo("TXN", request.studentId);

            populatePlacementStatus(cache, reply);
        }

        return reply.toXml();
    }

    /**
     * Look up the student's placement results and store in the reply.
     *
     * @param cache the data cache
     * @param reply the reply message that will be sent back to the client
     * @throws SQLException if there is an error accessing the database
     */
    private void populatePlacementStatus(final Cache cache, final PlacementStatusReply reply) throws SQLException {

        final List<RawMpeCredit> credits = RawMpeCreditLogic.queryByStudent(cache, getStudent().stuId);
        final int numCredits = credits.size();

        // Determine the number that have "C" or "P" results.
        int count = 0;
        for (int i = 0; i < numCredits; ++i) {
            if ("C".equals(credits.get(i).examPlaced) || "P".equals(credits.get(i).examPlaced)) {
                ++count;
            } else {
                credits.set(i, null);
            }
        }

        // Convert the results into array format expected in reply
        reply.courses = new String[count];
        reply.status = new char[count];
        count = 0;
        for (final RawMpeCredit credit : credits) {
            if (credit != null) {
                reply.courses[count] = credit.course;
                reply.status[count] = credit.examPlaced.charAt(0);
                ++count;
            }
        }
    }
}
