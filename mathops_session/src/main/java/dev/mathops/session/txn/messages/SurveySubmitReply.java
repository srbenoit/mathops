package dev.mathops.session.txn.messages;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A reply to a logout request.
 */
public final class SurveySubmitReply extends AbstractReplyBase {

    /**
     * Constructs a new {@code SurveySubmitReply}.
     */
    public SurveySubmitReply() {

        super();
    }

    /**
     * Constructs a new {@code SurveySubmitReply}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    SurveySubmitReply(final char[] xml) throws IllegalArgumentException {

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

        return "survey-submit-reply";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<survey-submit-reply>");
        printError(builder);
        builder.addln("</survey-submit-reply>");

        return builder.toString();
    }
}
