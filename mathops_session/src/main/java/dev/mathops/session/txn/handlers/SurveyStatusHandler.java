package dev.mathops.session.txn.handlers;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.rawlogic.RawStsurveyqaLogic;
import dev.mathops.db.rawlogic.RawSurveyqaLogic;
import dev.mathops.db.svc.term.TermLogic;
import dev.mathops.db.svc.term.TermRec;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.SurveyStatusReply;
import dev.mathops.session.txn.messages.SurveyStatusRequest;

import java.sql.SQLException;

/**
 * A handler for requests for a student's status regarding the survey questions attached to a particular version of an
 * exam. The current list of survey questions and the student's current answers are sent in reply.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class SurveyStatusHandler extends AbstractHandlerBase {

    /**
     * Construct a new {@code SurveyStatusHandler}.
     *
     * @param theDbProfile the database profile under which the handler is being accessed
     */
    public SurveyStatusHandler(final DbProfile theDbProfile) {

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
    public String process(final Cache cache, final AbstractRequestBase message)
            throws SQLException {

        setMachineId(message);
        touch(cache);

        final String result;

        // Validate the type of request
        if (message instanceof final SurveyStatusRequest request) {
            result = processRequest(cache, request);
        } else {
            Log.info("SurveyStatusHandler called with ",
                    message.getClass().getName());

            final SurveyStatusReply reply = new SurveyStatusReply();
            reply.error = "Invalid request type for survey status request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a request from the client.
     *
     * @param cache   the data cache
     * @param request the {@code SurveyStatusRequest} received from the client
     * @return the generated reply XML to send to the client
     * @throws SQLException if there is an error accessing the database
     */
    private String processRequest(final Cache cache, final SurveyStatusRequest request)
            throws SQLException {

        final SurveyStatusReply reply = new SurveyStatusReply();

        if (request.version == null) {
            reply.error = "Version not included in survey status request.";
        } else if (loadStudentInfo(cache, request.studentId, reply)) {

            if (getStudent().stuId == null) {
                reply.error = "Invalid session ID";
            } else if (populateSurveyQuestions(cache, request.version, reply)) {
                reply.answers = RawStsurveyqaLogic.queryLatestByStudentProfile(cache, getStudent().stuId,
                        request.version);
            }
        }

        return reply.toXml();
    }

    /**
     * Look up the list of survey questions for the given exam version for the current term, and store the results in
     * the reply, in ascending order of question number.
     *
     * @param cache   the data cache
     * @param version the exam version for which to retrieve questions
     * @param reply   the reply message that will be sent back to the client
     * @return true if successful; false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean populateSurveyQuestions(final Cache cache, final String version,
                                                   final SurveyStatusReply reply) throws SQLException {

        final boolean ok;

        final TermRec active = TermLogic.get(cache).queryActive(cache);
        if (active == null) {
            reply.error = "Unable to query active term";
            ok = false;
        } else {
            reply.questions = RawSurveyqaLogic.queryUniqueQuestionsByVersion(cache, version);

            if (reply.questions.isEmpty()) {
                Log.warning("No survey questions found for ", version);
            }
            ok = true;
        }

        return ok;
    }
}
