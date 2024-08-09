package dev.mathops.web.webservice;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.scramsha256.ScramServerStub;
import dev.mathops.session.scramsha256.UserCredentials;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The web service website.
 */
public final class WebServiceSite extends AbstractSite {

    /** Object on which to synchronize access to server stub. */
    private final Object sync;

    /** The server stub. */
    private ScramServerStub stub = null;

    /**
     * Constructs a new {@code WebServiceSite}.
     *
     * @param theSiteProfile the site profile under which this site is accessed
     * @param theSessions    the singleton user session repository
     */
    public WebServiceSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);

        this.sync = new Object();
    }

    /**
     * Indicates whether this site should do live queries to update student registration data.
     *
     * @return true to do live registration queries; false to skip
     */
    @Override
    public boolean doLiveRegQueries() {

        return false;
    }

    /**
     * Generates the site title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "Web Services";
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

        Log.info("GET Request to WebServiceSite, subpath=", subpath);

        synchronized (this.sync) {
            if (this.stub == null) {
                this.stub = new ScramServerStub(cache);
                Log.info("SCRAM stub created");
            }
        }

        if ("client-first.ws".equals(subpath)) {
            doScramClientFirst(req, resp);
        } else if ("client-final.ws".equals(subpath)) {
            doScramClientFinal(req, resp);
        } else {
            // All other requests require a valid token
            final String token = req.getParameter("token");
            final String reply;

            if (token == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                final UserCredentials credentials = this.stub.validateToken(token);

                if (credentials == null) {
                    reply = "!Unable to validate token.";
                } else {
                    reply = processValidatedRequest(cache, subpath, credentials, req);
                }

                final byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
                sendReply(req, resp, MIME_TEXT_PLAIN, bytes);
            }
        }
    }

    /**
     * Processes a request once the 'token' parameter value has been verified and the calling user's credentials
     * retrieved.
     *
     * @param cache       the data cache
     * @param subpath     the web service subpath
     * @param credentials the verified user's credentials
     * @param request     the request
     * @return the reply
     */
    private static String processValidatedRequest(final Cache cache, final String subpath,
                                                  final UserCredentials credentials, final ServletRequest request) {

        // https://testing.math.colostate.edu/websvc/*.ws

        final String reply;

        if ("testing-power-station-on.ws".equals(subpath)) {
            reply = TestingWebServices.powerStationOn(cache, credentials, request);
        } else if ("testing-power-station-off.ws".equals(subpath)) {
            reply = TestingWebServices.powerStationOff(cache, credentials, request);
        } else {
            reply = "!Unrecognized web service.";
        }

        return reply;
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
    public void doPost(final Cache cache, final String subpath, final ESiteType type,
                       final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        // Log.info("POST Request to WebServiceSite, subpath=", subpath);

        final String requestURI = req.getRequestURI();
        Log.warning(requestURI, " (", subpath, ") not found");

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Processes a "client-first" message of the SCRAM-SHA-256 protocol. The response is a "text/plain" body with either
     * the hex encoding of a server-first message, or "!" followed by an error message.
     *
     * @param request  the request (which must have a 'first' parameter with the encoded client-first message)
     * @param response the response
     * @throws IOException if there is an error writing the response
     */
    private void doScramClientFirst(final ServletRequest request,
                                    final HttpServletResponse response) throws IOException {

        final String reply;

        Log.info("SCRAM client first");

        final String firstData = request.getParameter("first");

        if (firstData == null) {
            reply = "!Request did not contain a 'first' parameter with request data.";
        } else {
            reply = this.stub.handleClientFirst(firstData);
        }

        Log.info("SCRAM client first reply is ", reply);

        final byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
        sendReply(request, response, MIME_TEXT_PLAIN, bytes);
    }

    /**
     * Processes a "client-final" message of the SCRAM-SHA-256 protocol. The response is a "text/plain" body with either
     * the hex encoding of a server-final message, or "!" followed by an error message.
     *
     * @param request  the request (which must have a 'final' parameter with the encoded client-final message)
     * @param response the response
     * @throws IOException if there is an error writing the response
     */
    private void doScramClientFinal(final ServletRequest request,
                                    final HttpServletResponse response) throws IOException {

        Log.info("SCRAM client final");

        final String finalData = request.getParameter("final");
        final String reply;

        if (finalData == null) {
            reply = "!Request did not contain a 'final' parameter with request data.";
        } else {
            reply = this.stub.handleClientFinal(finalData);
        }

        Log.info("SCRAM client final reply is ", reply);

        final byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
        sendReply(request, response, MIME_TEXT_PLAIN, bytes);
    }
}
