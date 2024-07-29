package dev.mathops.session.txn.handlers;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawStsurveyqaLogic;
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
     */
    public SurveyStatusHandler() {

        super();
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
        if (message instanceof final SurveyStatusRequest request) {
            result = processRequest(cache, request);
        } else {
            final String clsName = message.getClass().getName();
            Log.info("SurveyStatusHandler called with ", clsName);

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
    private String processRequest(final Cache cache, final SurveyStatusRequest request) throws SQLException {

        final SurveyStatusReply reply = new SurveyStatusReply();

        if (request.version == null) {
            reply.error = "Version not included in survey status request.";
        } else if (loadStudentInfo(cache, request.studentId, reply)) {
            final StudentData studentData = getStudentData();
            final String stuId = studentData.getStudentId();

            if (stuId == null) {
                reply.error = "Invalid session ID";
            } else {
                final SystemData systemData = cache.getSystemData();
                reply.questions = systemData.getSurveyQuestions(request.version);

                if (reply.questions.isEmpty()) {
                    Log.warning("No survey questions found for ", request.version);
                }

                reply.answers = RawStsurveyqaLogic.queryLatestByStudentProfile(cache, stuId, request.version);
            }
        }

        return reply.toXml();
    }
}
