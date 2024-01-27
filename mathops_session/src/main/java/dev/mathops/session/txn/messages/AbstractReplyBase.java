package dev.mathops.session.txn.messages;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * The base class for all reply messages.
 */
public abstract class AbstractReplyBase extends AbstractMessageBase {

    /** An optional error message to be included in the reply. */
    public String error;

    /**
     * Constructs a new {@code AbstractReplyBase}.
     */
    protected AbstractReplyBase() {

        super();
    }

    /**
     * Writes the error to an XML output {@code HtmlBuilder}.
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     */
    final void printError(final HtmlBuilder builder) {

        if (this.error != null) {
            builder.addln(" <error>", this.error, "</error>");
        }
    }
}
