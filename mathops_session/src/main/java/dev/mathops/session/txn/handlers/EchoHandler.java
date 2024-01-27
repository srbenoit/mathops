package dev.mathops.session.txn.handlers;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.EchoReply;
import dev.mathops.session.txn.messages.EchoRequest;

/**
 * A handler for echo requests.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class EchoHandler extends AbstractHandlerBase {

    /**
     * Constructs a new {@code EchoHandler}.
     *
     * @param theDbProfile the database profile under which the handler is being accessed
     */
    public EchoHandler(final DbProfile theDbProfile) {

        super(theDbProfile);
    }

    /**
     * Processes a message from the client.
     *
     * @param cache   the data cache
     * @param message the message received from the client
     * @return the reply to be sent to the client, or null if the connection should be closed
     */
    @Override
    public String process(final Cache cache, final AbstractRequestBase message) {

        setMachineId(message);

        final EchoReply reply = new EchoReply();

        // Validate the type of request
        if (!(message instanceof EchoRequest)) {
            Log.info("EchoHandler called with ", message.getClass().getName());
            reply.error = "Invalid request type for echo request";
        }

        return reply.toXml();
    }
}
