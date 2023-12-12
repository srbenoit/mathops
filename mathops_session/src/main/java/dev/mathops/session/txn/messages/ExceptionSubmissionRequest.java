package dev.mathops.session.txn.messages;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.ExceptionSubmissionHandler;

/**
 * A submission of an exception that occurred on the client.
 */
public final class ExceptionSubmissionRequest extends AbstractRequestBase {

    /** The testing station ID generating the exception. */
    public final String testingStationId;

    /** A message associated with the exception. */
    public final String exceptionMessage;

    /** The exception stack trace. */
    public final String exception;

    /**
     * Constructs a new {@code ExceptionSubmissionRequest}.
     *
     * @param theStationId the ID of the testing station sending the event
     * @param theMessage   a message to associate with the exception
     * @param theException the exception to report
     */
    public ExceptionSubmissionRequest(final String theStationId, final String theMessage,
                                      final Throwable theException) {

        super();

        final HtmlBuilder builder = new HtmlBuilder(500);

        this.testingStationId = theStationId;
        this.exceptionMessage = theMessage;

        String exc = null;
        if (theException != null) {
            builder.addln(theException.toString());

            final StackTraceElement[] stack = theException.getStackTrace();
            for (final StackTraceElement stackTraceElement : stack) {
                builder.addln(stackTraceElement.toString());
            }

            exc = builder.toString();
        }
        this.exception = exc;
    }

    /**
     * Constructs a new {@code StackTraceElement[] stack;}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    ExceptionSubmissionRequest(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.machineId = extractField(message, "machine-id");
        this.testingStationId = extractField(message, "station-id");
        this.exceptionMessage = extractField(message, "message");
        this.exception = extractField(message, "exception");
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "exception-submission-request";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return The XML representation.
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<exception-submission-request>");

        if (this.testingStationId != null) {
            builder.addln(" <station-id>", this.testingStationId, "</station-id>");
        }

        if (this.exceptionMessage != null) {
            builder.addln(" <message>", this.exceptionMessage, "</message>");
        }

        if (this.exception != null) {
            builder.addln(" <exception>", this.exception, "</exception>");
        }

        printMachineId(builder);
        builder.addln("</exception-submission-request>");

        return builder.toString();
    }

    /**
     * Generates a handler that can process this message.
     *
     * @param dbProfile the database profile in which the handler will operate
     * @return a handler that can process the message
     */
    @Override
    public AbstractHandlerBase createHandler(final DbProfile dbProfile) {

        return new ExceptionSubmissionHandler(dbProfile);
    }
}
