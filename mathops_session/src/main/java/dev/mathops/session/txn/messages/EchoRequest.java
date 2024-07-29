package dev.mathops.session.txn.messages;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.EchoHandler;

/**
 * A network message to test connectivity.
 */
public final class EchoRequest extends AbstractRequestBase {

    /**
     * Constructs a new {@code EchoRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    EchoRequest(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.machineId = extractField(message, "machine-id");
    }

    /**
     * Get the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "echo-request";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(200);

        builder.addln("<echo-request>");
        printMachineId(builder);
        builder.addln("</echo-request>");

        return builder.toString();
    }

    /**
     * Generates a handler that can process this message.
     *
     * @return a handler that can process the message
     */
    @Override
    public AbstractHandlerBase createHandler() {

        return new EchoHandler();
    }
}
