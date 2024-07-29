package dev.mathops.session.txn.messages;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.GetPastExamHandler;

/**
 * A network message to request an exam that a student has taken in the past.
 */
public final class GetPastExamRequest extends AbstractRequestBase {

    /** The path of the exam XML file, relative to the student's data path. */
    public final String xmlPath;

    /** The path of the exam update file relative to the student's data path. */
    public final String updPath;

    /**
     * Constructs a new {@code GetPastExamRequest}.
     *
     * @param theXmlPath the path of the exam XML file, relative to the student's data path
     * @param theUpdPath the path of the exam update file, relative to the student's data path
     */
    public GetPastExamRequest(final String theXmlPath, final String theUpdPath) {

        super();

        this.xmlPath = theXmlPath;
        this.updPath = theUpdPath;
    }

    /**
     * Constructs a new {@code GetExamRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException If the XML stream is not valid
     */
    GetPastExamRequest(final char[] xml) throws IllegalArgumentException {

        super();

        final String tag = xmlTag();
        final String message = extractMessage(xml, tag);

        this.machineId = extractField(message, "machine-id");
        this.xmlPath = extractField(message, "xml");
        this.updPath = extractField(message, "update");
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "get-past-exam-request";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<get-past-exam-request>");

        if (this.xmlPath != null) {
            builder.addln(" <xml>", this.xmlPath, "</xml>");
        }

        if (this.updPath != null) {
            builder.addln(" <update>", this.updPath, "</update>");
        }

        printMachineId(builder);
        builder.addln("</get-past-exam-request>");

        return builder.toString();
    }

    /**
     * Generates a handler that can process this message.
     *
     * @return a handler that can process the message
     */
    @Override
    public AbstractHandlerBase createHandler() {

        return new GetPastExamHandler();
    }
}
