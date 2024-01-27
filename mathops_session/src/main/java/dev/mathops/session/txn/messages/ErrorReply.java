package dev.mathops.session.txn.messages;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A reply containing only an error message.
 */
public final class ErrorReply extends AbstractReplyBase {

    /**
     * Constructs a new {@code ErrorReply}.
     */
    public ErrorReply() {

        super();
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<error-reply>");
        printError(builder);
        builder.addln("</error-reply>");

        return builder.toString();
    }
}
