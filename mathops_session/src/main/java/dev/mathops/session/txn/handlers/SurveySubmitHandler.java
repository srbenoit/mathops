package dev.mathops.session.txn.handlers;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.old.rawlogic.RawStsurveyqaLogic;
import dev.mathops.db.old.rawrecord.RawStsurveyqa;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.SurveySubmitReply;
import dev.mathops.session.txn.messages.SurveySubmitRequest;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * A handler for submissions of answers to survey questions attached to a particular version of an exam.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class SurveySubmitHandler extends AbstractHandlerBase {

    /**
     * Construct a new {@code SurveySubmitHandler}.
     */
    public SurveySubmitHandler() {

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

        final String result;

        if (message instanceof final SurveySubmitRequest request) {
            result = processRequest(cache, request);
        } else {
            final String clsName = message.getClass().getName();
            Log.info("SurveySubmitHandler called with ", clsName);

            final SurveySubmitReply reply = new SurveySubmitReply();
            reply.error = "Invalid request type for survey submission request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a request from the client.
     *
     * @param cache   the data cache
     * @param request the {@code SurveySubmitRequest} received from the client
     * @return the generated reply XML to send to the client
     */
    private String processRequest(final Cache cache, final SurveySubmitRequest request) {

        final SurveySubmitReply reply = new SurveySubmitReply();

        // Validate that all required fields are present, and see that we are not inhibiting
        // communications.
        if (request.version == null) {
            reply.error = "Version not included in survey submit request.";
        } else if (request.answers == null) {
            reply.error = "Answers not included in survey submit request.";
        } else {
            try {
                if (loadStudentInfo(cache, request.studentId, reply)) {
                    LogBase.setSessionInfo("TXN", request.studentId);

                    final StudentData studentData = getStudentData();
                    final String stuId = studentData.getStudentId();

                    storeAnswers(cache, request.version, request.answers, reply, stuId);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                reply.error = "Answers not included in survey submit request.";
            }
        }

        return reply.toXml();
    }

    /**
     * Look up the list of survey questions for the given exam version for the current term, and store the results in
     * the reply, in ascending order of question number.
     *
     * @param cache     the data cache
     * @param version   the exam version
     * @param answers   the list of submitted answers
     * @param studentId the ID of the student for which to update answers
     * @param reply     the reply message that will be sent back to the client
     * @throws SQLException if there is an error accessing the database
     */
    private static void storeAnswers(final Cache cache, final String version, final String[] answers,
                                     final SurveySubmitReply reply, final String studentId) throws SQLException {

        final LocalDateTime now = LocalDateTime.now();
        final HtmlBuilder htm = new HtmlBuilder(100);

        final int numAns = answers.length;
        for (int i = 0; i < numAns; ++i) {
            final String answer = answers[i];

            // Process the new answer submitted by the student
            if (answer != null && !answer.isEmpty()) {

                // A non-null answer was supplied, so validate it
                if (answer.length() > 5) {
                    htm.add("Submitted answer is too long: ", answer, ". ");
                    continue;
                }

                // Record the answer
                final RawStsurveyqa ans = new RawStsurveyqa(studentId, version, now.toLocalDate(),
                        Integer.valueOf(i + 1), answer, Integer.valueOf(TemporalUtils.minuteOfDay(now)));

                RawStsurveyqaLogic.insert(cache, ans);
            }
        }

        // store any error messages in the reply
        reply.error = htm.toString();

        if (reply.error.isEmpty()) {
            reply.error = null;
        }
    }
}
