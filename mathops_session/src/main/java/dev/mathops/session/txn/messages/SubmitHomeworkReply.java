package dev.mathops.session.txn.messages;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A network message that gives results of submitting a homework assignment.
 */
public final class SubmitHomeworkReply extends AbstractReplyBase {

    /** The results of the homework submission. */
    public String result;

    /**
     * Constructs a new {@code SubmitHomeworkReply}.
     */
    public SubmitHomeworkReply() {

        super();
    }

    /**
     * Constructs a new {@code SubmitHomeworkReply}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    public SubmitHomeworkReply(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.error = extractField(message, "error");
        this.result = extractField(message, "result");
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "submit-homework-reply";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<submit-homework-reply>");
        printError(builder);

        if (this.result != null) {
            builder.addln(" <result>", this.result, "</result>");
        }

        builder.addln("</submit-homework-reply>");

        return builder.toString();
    }
}
