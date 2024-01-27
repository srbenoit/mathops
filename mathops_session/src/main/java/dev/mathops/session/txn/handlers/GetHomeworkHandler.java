package dev.mathops.session.txn.handlers;

import dev.mathops.assessment.InstructionalCache;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rec.AssignmentRec;
import dev.mathops.db.old.reclogic.AssignmentLogic;
import dev.mathops.session.sitelogic.servlet.HomeworkEligibilityTester;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.GetHomeworkReply;
import dev.mathops.session.txn.messages.GetHomeworkRequest;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A handler for requests to retrieve a homework assignment received by the server. Requests include a login session ID
 * and the homework assignment version being requested, and replies include the homework assignment and the list of
 * problems that the assignment can draw from, or a list of errors or holds that prevented the assignment from being
 * retrieved.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class GetHomeworkHandler extends AbstractHandlerBase {

    /**
     * Construct a new {@code GetHomeworkHandler}.
     *
     * @param theDbProfile the database profile under which the handler is being accessed
     */
    public GetHomeworkHandler(final DbProfile theDbProfile) {

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
        if (message instanceof final GetHomeworkRequest request) {
            try {
                result = processRequest(cache, request);
            } catch (final SQLException ex) {
                final GetHomeworkReply reply = new GetHomeworkReply();
                reply.error = "Error accessing the database: " + ex.getMessage();
                result = reply.toXml();
            }
        } else {
            Log.info("GetHomeworkHandler called with ", message.getClass().getName());

            final GetHomeworkReply reply = new GetHomeworkReply();
            reply.error = "Invalid request type for get homework request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a request from the client.
     *
     * @param cache   the data cache
     * @param request the {@code GetHomeworkRequest} received from the client
     * @return the generated reply XML to send to the client
     * @throws SQLException if there is an error accessing the database
     */
    private String processRequest(final Cache cache, final GetHomeworkRequest request)
            throws SQLException {

        final GetHomeworkReply reply = new GetHomeworkReply();

        if (loadStudentInfo(cache, request.studentId, reply)) {

            LogBase.setSessionInfo("TXN", request.studentId);

            // If exam was requested as a homework assignment, look up the version
            final HomeworkEligibilityTester hwtest = new HomeworkEligibilityTester(getStudent().stuId);

            // See if the student is eligible to take the homework
            final AssignmentRec avail = AssignmentLogic.get(cache).query(cache, request.homeworkVersion);

            final HtmlBuilder reasons = new HtmlBuilder(100);
            final List<RawAdminHold> holds = new ArrayList<>(1);

            reply.studentId = getStudent().stuId;

            if (avail == null) {
                reply.error = "Homework not found.";
            } else {
                final ZonedDateTime now = ZonedDateTime.now();

                if (hwtest.isHomeworkEligible(cache, now, avail, reasons, holds, request.isPractice)) {
                    reply.minMoveOn = hwtest.getMinMoveOnScore();
                    reply.minMastery = hwtest.getMinMasteryScore();

                    buildAssignment(avail.treeRef, reply);

                    if (Integer.valueOf(-1).equals(reply.minMoveOn)) {
                        reply.minMoveOn = Integer.valueOf(reply.homework.getNumProblems());
                    }

                    if (Integer.valueOf(-1).equals(reply.minMastery)) {
                        reply.minMastery = Integer.valueOf(reply.homework.getNumProblems());
                    }
                } else {
                    reply.error = reasons.toString();

                    if (reply.error.isEmpty()) {
                        reply.error = null;
                    }

                    Log.info("Homework not eligible: ", reply.error);
                }
            }

            if (!holds.isEmpty()) {
                final int count = holds.size();
                reply.holds = new String[count];

                for (int i = 0; i < count; ++i) {
                    reply.holds[i] = RawAdminHoldLogic.getStudentMessage(holds.get(i).holdId);
                }
            }
        }

        return reply.toXml();
    }

    /**
     * Attempt to load a homework assignment, which consists of an unrealized exam and its associated problems. On
     * errors, the reply message errors field will be set to the cause of the error.
     *
     * @param ref   the reference to the homework assignment to be loaded
     * @param reply the reply message to populate with the homework assignment or error status
     */
    private static void buildAssignment(final String ref, final GetHomeworkReply reply) {

        reply.homework = InstructionalCache.getExam(ref);

        if (reply.homework == null) {
            Log.info("Unable to load template for ", ref);
        } else if (reply.homework.ref == null) {
            Log.info("Errors loading exam template");
            reply.homework = null;
        }
    }
}
