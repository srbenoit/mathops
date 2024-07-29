package dev.mathops.session.txn.messages;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;

/**
 * The base class for all request messages.
 */
public abstract class AbstractRequestBase extends AbstractMessageBase {

    /** The ID of the machine sending the message. */
    public String machineId = null;

    /**
     * Constructs a new {@code AbstractRequestBase}.
     */
    protected AbstractRequestBase() {

        super();
    }

    /**
     * Writes the machine ID to an XML output {@code HtmlBuilder}.
     *
     * @param builder the {@code HtmlBuilder} to which to write the XML
     */
    final void printMachineId(final HtmlBuilder builder) {

        if (this.machineId != null) {
            builder.addln(" <machine-id>", this.machineId, "</machine-id>");
        }
    }

    /**
     * Generates a handler that can process this message.
     *
     * @return a handler that can process the message
     */
    public abstract AbstractHandlerBase createHandler();
}
