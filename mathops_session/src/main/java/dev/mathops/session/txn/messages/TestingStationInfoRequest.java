package dev.mathops.session.txn.messages;

import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.TestingStationInfoHandler;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * A request for verification that this machine is a valid testing station, and to get the testing center name and
 * station number.
 */
public final class TestingStationInfoRequest extends AbstractRequestBase {

    /**
     * Constructs a new {@code TestingStationInfoRequest}.
     */
    public TestingStationInfoRequest() {

        super();
    }

    /**
     * Constructs a new {@code TestingStationInfoRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    TestingStationInfoRequest(final char[] xml) throws IllegalArgumentException {

        super();

        final String tag = xmlTag();
        final String message = extractMessage(xml, tag);

        this.machineId = extractField(message, "machine-id");
    }

    /**
     * Get the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    public static String xmlTag() {

        return "testing-station-info-request";
    }

    /**
     * Generate the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<testing-station-info-request>");
        printMachineId(builder);
        builder.addln("</testing-station-info-request>");

        return builder.toString();
    }

    /**
     * Generates a handler that can process this message.
     *
     * @return a handler that can process the message
     */
    @Override
    public AbstractHandlerBase createHandler() {

        return new TestingStationInfoHandler();
    }
}
