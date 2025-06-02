package dev.mathops.web.host.testing.txn;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.messages.AbstractMessageBase;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.MessageFactory;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * A base class for sites that will process some subset of the requests received by the main servlet, based on the
 * leading part of the request path.
 */
public final class TxnSite extends AbstractSite {

    /**
     * Constructs a new {@code Site}.
     *
     * @param theSite     the website context
     * @param theSessions the singleton user session repository
     */
    public TxnSite(final dev.mathops.db.cfg.Site theSite, final ISessionManager theSessions) {

        super(theSite, theSessions);
    }

    /**
     * Tests the live refresh policy for this site.
     *
     * @return the live refresh policy
     */
    @Override
    protected ELiveRefreshes getLiveRefreshes() {

        return ELiveRefreshes.NONE;
    }

    /**
     * Generates the site title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "Precalculus Program";
    }

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doGet(final Cache cache, final String subpath, final ESiteType type, final HttpServletRequest req,
                      final HttpServletResponse resp) throws IOException {

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    /**
     * Processes a POST request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doPost(final Cache cache, final String subpath, final ESiteType type, final HttpServletRequest req,
                       final HttpServletResponse resp) throws IOException {

        final int len = req.getContentLength();

        if (len > 0) {
            final byte[] bytes = new byte[len];

            try (final InputStream in = req.getInputStream()) {
                int total = 0;
                final long timeout = System.currentTimeMillis() + 5000L;

                while (total < len) {
                    final int count = in.read(bytes, total, len - total);

                    if (count > 0) {
                        total += count;
                    } else if (System.currentTimeMillis() > timeout) {
                        Log.warning("Timeout while reading POST request");
                        throw new IOException("Failed to read POST request: content-len=" + len + ", read " + total);
                    } else {
                        try {
                            Thread.sleep(10L);
                        } catch (final InterruptedException ex) {
                            Log.warning("Interrupted while reading POST request");
                            throw new IOException("Failed to read POST request: content-len=" + len + ", read "
                                                  + total);
                        }
                    }
                }
            }

            final String reqStr = new String(bytes, StandardCharsets.UTF_8);
//                Log.info("REQ = ", reqStr);

            final byte[] reply;

            try {
                final char[] reqChars = reqStr.toCharArray();
                reply = processRequest(cache, reqChars);
            } catch (final RuntimeException ex) {
                Log.warning(ex);
                throw new IOException("Exception processing request", ex);
            }
//                Log.info("REPLY = ", new String(reply));

            if (reply == null) {
                Log.warning("Processing generated null reply: ", reqStr);
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                resp.setContentLength(reply.length);
                resp.setContentType("text/xml");
                resp.setHeader("Cache-Control", "no-cache");

                try (final OutputStream out = resp.getOutputStream()) {
                    out.write(reply);
                }
            }
        } else {
            Log.warning("Bad request: length 0");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Processes a request by parsing the XML request into a message, executing the handler associated with the message,
     * and returning the response generated by the handler.
     *
     * @param cache   the data cache
     * @param request the request
     * @return the response; {@code null} on any error
     */
    private byte[] processRequest(final Cache cache, final char[] request) {

        byte[] reply = null;

        // Parse the message XML into a message object
        final AbstractMessageBase msg = MessageFactory.parseMessage(request);

        if (msg == null) {
            Log.info("Unparseable message from client: ", new String(request));
        } else {
            String name = msg.getClass().getName();
            final int pos = name.lastIndexOf('.');

            if (pos != -1) {
                name = name.substring(pos + 1);
            }

            if (msg instanceof final AbstractRequestBase reqMsg) {
                if (!name.startsWith("Testing")) {
                    Log.info("Processing ", name);
                }

                final AbstractHandlerBase handler = reqMsg.createHandler();
                if (handler == null) {
                    Log.info("Unable to create handler for ", name);
                } else {
                    try {
                        final String chars = handler.process(cache, reqMsg);
                        if (chars == null) {
                            Log.info("Null response from handler");
                        } else {
                            reply = chars.getBytes(StandardCharsets.UTF_8);
                        }
                    } catch (final SQLException ex) {
                        Log.warning(ex);
                    }
                }
            } else {
                Log.info("Non-request message sent to server: ", name);
            }
        }

        return reply;
    }
}
