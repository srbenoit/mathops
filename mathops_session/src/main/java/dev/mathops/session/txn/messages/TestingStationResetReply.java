package dev.mathops.session.txn.messages;

import dev.mathops.text.builder.HtmlBuilder;

/**
 * A reply to a testing station reset request.
 */
public final class TestingStationResetReply extends AbstractReplyBase {

    /**
     * Constructs a new {@code TestingStationResetReply}.
     */
    public TestingStationResetReply() {

        super();
    }

    /**
     * Constructs a new {@code TestingStationResetReply}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    TestingStationResetReply(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.error = extractField(message, "error");
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    public static String xmlTag() {

        return "testing-station-reset-reply";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<testing-station-reset-reply>");
        printError(builder);
        builder.addln("</testing-station-reset-reply>");

        return builder.toString();
    }
}
