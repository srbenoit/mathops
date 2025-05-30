package dev.mathops.session.txn.messages;

import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.TestingStationResetHandler;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * A request for the to reset testing station to the locked state.
 */
public final class TestingStationResetRequest extends AbstractRequestBase {

    /**
     * Constructs a new {@code TestingStationResetRequest}.
     */
    public TestingStationResetRequest() {

        super();
    }

    /**
     * Constructs a new {@code TestingStationResetRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    TestingStationResetRequest(final char[] xml) throws IllegalArgumentException {

        super();

        final String tag = xmlTag();
        final String message = extractMessage(xml, tag);

        this.machineId = extractField(message, "machine-id");
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    public static String xmlTag() {

        return "testing-station-reset-request";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<testing-station-reset-request>");
        printMachineId(builder);
        builder.addln("</testing-station-reset-request>");

        return builder.toString();
    }

    /**
     * Generates a handler that can process this message.
     *
     * @return a handler that can process the message
     */
    @Override
    public AbstractHandlerBase createHandler() {

        return new TestingStationResetHandler();
    }
}
