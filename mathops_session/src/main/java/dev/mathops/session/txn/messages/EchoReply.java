package dev.mathops.session.txn.messages;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A network message that confirms receipt of an EchoRequest message.
 */
public final class EchoReply extends AbstractReplyBase {

    /**
     * Constructs a new {@code EchoReply}.
     */
    public EchoReply() {

        super();
    }

    /**
     * Constructs a new {@code EchoReply}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    EchoReply(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.error = extractField(message, "error");
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "echo-reply";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<echo-reply>");
        printError(builder);
        builder.addln("</echo-reply>");

        return builder.toString();
    }
}
