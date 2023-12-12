package dev.mathops.session.txn.handlers;

import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawlogic.RawPendingExamLogic;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.ExamStartResultReply;
import dev.mathops.session.txn.messages.ExamStartResultRequest;

import java.sql.SQLException;

/**
 * A handler for indication of the results of attempting to start an exam. If this is a testing station, that station's
 * status is updated appropriately.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class ExamStartResultHandler extends AbstractHandlerBase {

    /**
     * Construct a new {@code ExamStartResultHandler}.
     *
     * @param theDbProfile the database profile under which the handler is being accessed
     */
    public ExamStartResultHandler(final DbProfile theDbProfile) {

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
        if (message instanceof final ExamStartResultRequest request) {
            try {
                result = processRequest(cache, request);
            } catch (final SQLException ex) {
                Log.warning(ex);

                final ExamStartResultReply reply = new ExamStartResultReply();
                reply.error = "Error processing exam start result request";
                result = reply.toXml();
            }
        } else {
            Log.warning("ExamStartResultHandler called with ", message.getClass().getName());

            final ExamStartResultReply reply = new ExamStartResultReply();
            reply.error = "Invalid request type for exam start result request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a request from the client.
     *
     * @param cache   the data cache
     * @param request the {@code ExamStartResultRequest} received from the client
     * @return the generated reply XML to send to the client
     * @throws SQLException if there was an error accessing the database
     */
    private String processRequest(final Cache cache, final ExamStartResultRequest request)
            throws SQLException {

        final ExamStartResultReply reply = new ExamStartResultReply();

        if (loadStudentInfo(cache, null, reply)) {

            if (getMachineId() != null) {

                final boolean started = request.result != null
                        && request.result.intValue() == ExamStartResultRequest.EXAM_STARTED;

                final Integer state;
                final String student;
                final String course;
                final Integer unit;
                final String version;
                if (started) {
                    state = RawClientPc.STATUS_TAKING_EXAM;
                    student = getClient().currentStuId;
                    course = getClient().currentCourse;
                    unit = getClient().currentUnit;
                    version = getClient().currentVersion;
                } else {
                    state = RawClientPc.STATUS_LOCKED;
                    student = null;
                    course = null;
                    unit = null;
                    version = null;

                    if (request.serialNumber == null) {
                        Log.warning("ExamStartResultRequest without serial number ",
                                request.toXml());
                    } else {
                        RawPendingExamLogic.delete(cache, request.serialNumber, getStudent().stuId);
                    }
                }

                RawClientPcLogic.updateAllCurrent(cache, getClient().computerId, state, student,
                        course, unit, version);
            } else {
                RawPendingExamLogic.delete(cache, request.serialNumber, getStudent().stuId);
            }
        }

        return reply.toXml();
    }
}
