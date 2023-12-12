package dev.mathops.session.txn.handlers;

import dev.mathops.core.EPath;
import dev.mathops.core.PathList;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.ExceptionSubmissionReply;
import dev.mathops.session.txn.messages.ExceptionSubmissionRequest;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;

/**
 * A handler for requests to submit homework assignments.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class ExceptionSubmissionHandler extends AbstractHandlerBase {

    /**
     * Construct a new {@code ExceptionSubmissionHandler}.
     *
     * @param theDbProfile the database profile under which the handler is being accessed
     */
    public ExceptionSubmissionHandler(final DbProfile theDbProfile) {

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

        final String result;

        // Validate the type of request
        if (message instanceof final ExceptionSubmissionRequest request) {
            result = processRequest(request);
        } else {
            Log.info("ExceptionSubmissionHandler called with ", message.getClass().getName());

            final ExceptionSubmissionReply reply = new ExceptionSubmissionReply();
            reply.error = "Invalid request type for exception submission request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a request from the client.
     *
     * @param request the {@code ExceptionSubmissionRequest} received from the client
     * @return the generated reply XML to send to the client
     */
    private static String processRequest(final ExceptionSubmissionRequest request) {

        final ExceptionSubmissionReply reply = new ExceptionSubmissionReply();

        final File excDir = PathList.getInstance().get(EPath.EXCEPTION_PATH);

        if (excDir != null) {

            if (!excDir.exists() && !excDir.mkdirs()) {
                Log.warning("Failed to create directory ", excDir.getAbsolutePath());
            }

            final String station = request.testingStationId;
            final File excFile = new File(excDir, (station == null) //
                    ? "null" : station);

            try (final FileWriter fw = new FileWriter(excFile, true)) {
                fw.write(TemporalUtils.FMT_MDY_AT_HMS_A.format(LocalDateTime.now()));
                fw.write("\n*** Exception Submission:\n");
                fw.write(request.exceptionMessage);

                if (request.exception != null) {
                    fw.write("\nException:\n  ");
                    fw.write(request.exception);
                }

                fw.write("\n\n");
            } catch (final Exception e) {
                Log.warning(e.getMessage());
            }
        }

        return reply.toXml();
    }
}
