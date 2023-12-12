package dev.mathops.session.txn.messages;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.TestingStationStatusHandler;

/**
 * A request for the current status of a testing station.
 */
public final class TestingStationStatusRequest extends AbstractRequestBase {

    /**
     * Constructs a new {@code TestingStationStatusRequest}.
     */
    public TestingStationStatusRequest() {

        super();
    }

    /**
     * Constructs a new {@code TestingStationStatusRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    TestingStationStatusRequest(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.machineId = extractField(message, "machine-id");
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    public static String xmlTag() {

        return "testing-station-status-request";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<testing-station-status-request>");
        printMachineId(builder);
        builder.addln("</testing-station-status-request>");

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

        return new TestingStationStatusHandler(dbProfile);
    }
}
