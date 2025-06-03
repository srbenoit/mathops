package dev.mathops.web.host.course.lti;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.rec.main.LtiRegistrationRec;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;
import dev.mathops.text.internet.RFC7517;
import dev.mathops.text.internet.RFC8017KeyPairGenerator;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.BasicCss;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.course.lti.canvascourse.LTICallback;
import dev.mathops.web.host.course.lti.canvascourse.LTIJWKS;
import dev.mathops.web.host.course.lti.canvascourse.LTITarget;
import dev.mathops.web.host.course.lti.canvascourse.LTIDynamicRegistration;
import dev.mathops.web.host.course.lti.canvascourse.LTILaunch;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A site to deliver Precalculus exams through a Canvas LTI quiz.
 */
public final class LtiSite extends AbstractSite {

    /** The expiration date/time for LTI key sets. */
    private static final int LTI_KEY_EXPIRY_MINUTES = 180;

    /** The number of minutes a redirect can wait before expiring. */
    private static final long REDIRECT_EXPIRY_MINUTES = 5L;

    /** A map from LTI registration to its JSON Web Key Set. */
    private final Map<LtiRegistrationRec, JWKS> ltiKeys;

    /** A map from "nonce" string to the pending redirect. */
    private final Map<String, PendingTargetRedirect> redirects;

    /**
     * Constructs a new {@code LtiSite}.
     *
     * @param theSite     the site profile under which this site is accessed
     * @param theSessions the singleton user session repository
     */
    public LtiSite(final Site theSite, final ISessionManager theSessions) {

        super(theSite, theSessions);

        this.ltiKeys = new HashMap<>(10);
        this.redirects = new HashMap<>(20);
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
    public void doGet(final Cache cache, final String subpath, final ESiteType type,
                      final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        if (subpath.startsWith("images/")) {
            serveImage(subpath.substring(7), req, resp);
        } else if ("favicon.ico".equals(subpath) || "secure/favicon.ico".equals(subpath)
                   || "lti_logo.png".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else if ("basestyle.css".equals(subpath) || "secure/basestyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
        } else if ("style.css".equals(subpath) || "secure/tyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(getClass(), "style.css", true));
        } else {
            Log.info("GET ", subpath);

            switch (subpath) {
                // This page is called when the Canvas administrator creates a new Developer Key using "LTI
                // Registration". It presents a form that POSTS to the same URL if the user accepts the registration.
                case "lti13_dynamic_registration.html" -> LTIDynamicRegistration.doGet(this, req, resp);
                // This is called by Canvas to initiate an LTI Launch - it responds with a redirect to the
                // authorization endpoint for the LTI registration
                case "lti13_launch" -> LTILaunch.doLaunch(cache, this, req, resp);
                // A callback from the LMS after an LTI launch redirect.
                case "lti13_callback" -> LTICallback.doCallback(cache, this, req, resp);
                // The target URI for requests for LTI content.
                case "lti13_target" -> LTITarget.doTarget(cache, this, req, resp);
                // The target URI for requests for LTI content.
                case "lti13_jwks" -> LTIJWKS.doGet(cache, this, req, resp);

                case "course.css" -> BasicCss.getInstance().serveCss(req, resp);

                case CoreConstants.EMPTY -> PageIndex.showPage(req, resp);
                case "index.html" -> PageIndex.showPage(req, resp);

                // This is linked from the admin website
                case "onlineproctor.htm" -> PageOnlineProctor.showPage(req, resp, null, null);
                // This is linked from the admin website
                case "onlineproctorchallenge.htm" -> PageOnlineProctorChallenge.showPage(req, resp, null, null);

                case "home.html" -> PageHome.showPage(cache, this, req, resp);
                case "challenge.html" -> PageChallenge.showPage(cache, this, req, resp);
                case "course.html" -> PageFinished.showPage(req, resp);

                case null, default -> {
                    Log.info("GET request to unrecognized URL: ", subpath);

                    final Enumeration<String> e1 = req.getParameterNames();
                    while (e1.hasMoreElements()) {
                        final String name = e1.nextElement();
                        Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
                    }

                    Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        }
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
                       final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        Log.info("POST ", subpath);

        // TODO: Honor maintenance mode.

        switch (subpath) {
            // This is called by the form shown when doing an "LTI Registration" to accept the registration of
            // the tool
            case "lti13_dynamic_registration.html" -> LTIDynamicRegistration.doPost(cache, this, req, resp);
            // This is called by Canvas to initiate an LTI Launch - it responds with a redirect to the
            // authorization endpoint for the LTI registration
            case "lti13_launch" -> LTILaunch.doLaunch(cache, this, req, resp);
            // A callback from the LMS after an LTI launch redirect.
            case "lti13_callback" -> LTICallback.doCallback(cache, this, req, resp);
            // The target URI for requests for LTI content.
            case "lti13_target" -> LTITarget.doTarget(cache, this, req, resp);

            // THe next three are used by the online Teams proctoring process
            case "gainaccess.html" -> PageIndex.processAccessCode(cache, this, req, resp);
            case "beginproctor.html" -> PageOnlineProctor.processBeginProctor(req, resp);
            case "beginproctorchallenge.html" -> PageOnlineProctorChallenge.processBeginProctor(cache, req, resp);
            case "home.html" -> PageHome.showPage(cache, this, req, resp);
            case "challenge.html" -> PageChallenge.showPage(cache, this, req, resp);
            case "update_unit_exam.html" -> PageHome.updateUnitExam(cache, req, resp);
            case "update_challenge_exam.html" -> PageChallenge.updateChallengeExam(cache, req, resp);
            case null, default -> {
                Log.info("POST request to unrecognized URL: ", subpath);

                final Enumeration<String> e1 = req.getParameterNames();
                while (e1.hasMoreElements()) {
                    final String name = e1.nextElement();
                    Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
                }

                Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    /**
     * Validates the user session. If the session is invalid, an error is logged and the user is redirected to the
     * index.html page.
     *
     * @param req      the request
     * @param resp     the response
     * @param failPage the page to which to redirect the user on a failed validation
     * @return the {@code ImmutableSessionInfo} if the session is valid; {@code null} if not
     * @throws IOException if there is an error writing the response
     */
    @Override
    public ImmutableSessionInfo validateSession(final HttpServletRequest req,
                                                final HttpServletResponse resp, final String failPage) throws IOException {

        final String sess = extractSessionId(req);

        final SessionResult session = SessionManager.getInstance().validate(sess);
        final ImmutableSessionInfo result = session.session;

        if (result == null) {
            if (sess != null) {
                Log.warning("Session validation error: ", session.error);

                // Tell the client to delete the cookie that provided the session ID
                final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, sess);
                cook.setDomain(req.getServerName());
                cook.setPath(CoreConstants.SLASH);
                cook.setMaxAge(0);
                resp.addCookie(cook);
            }

            if (failPage != null) {
                final String path = this.site.path;
                resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) ? failPage
                        : CoreConstants.SLASH + failPage));
            }
        }

        return result;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "LTI";
    }

    /**
     * Creates the key store for the client.
     *
     * @param clientId the client IDs
     */
    public static void createKeyStore(final String clientId) {

        // Create encryption and signing key pairs and store
        final File baseDir = PathList.getInstance().get(EPath.BASE_DIR);
        final File keysDir = new File(baseDir, "keys");
        if (keysDir.exists() && keysDir.isDirectory()) {
            final File clientKeyDir = new File(keysDir, clientId);
            if (clientKeyDir.exists() || clientKeyDir.mkdirs()) {
                final long timestamp = System.currentTimeMillis();
                final String kid1 = Long.toString(timestamp);
                final String kid2 = Long.toString(timestamp + 1);
                final RFC8017KeyPairGenerator.KeyPair1 sigKeys = RFC8017KeyPairGenerator.generateKeyPair();
                final RFC8017KeyPairGenerator.KeyPair1 encKeys = RFC8017KeyPairGenerator.generateKeyPair();

                final JSONObject publicSig = RFC7517.generateSigningJWK(sigKeys.pub(), kid1);
                final JSONObject publicEnc = RFC7517.generateEncryptingJWK(encKeys.pub(), kid2);
                final JSONObject publicKeySet = RFC7517.generateKeySet(publicSig, publicEnc);

                final File publicSetFile = new File(clientKeyDir, "public.json");
                final String publicSetJson = publicKeySet.toJSONCompact();
                try (final FileWriter w = new FileWriter(publicSetFile)) {
                    w.write(publicSetJson);
                } catch (final IOException ex) {
                    Log.warning("Failed to write public key store.");
                }

                final JSONObject privateSig = RFC7517.generatePrivate(sigKeys.priv(), kid1);
                final JSONObject privateEnc = RFC7517.generatePrivate(encKeys.priv(), kid2);
                final JSONObject privateKeySet = RFC7517.generateKeySet(privateSig, privateEnc);

                final File privateSetFile = new File(clientKeyDir, "private.json");
                final String privateSetJson = privateKeySet.toJSONCompact();
                try (final FileWriter w = new FileWriter(privateSetFile)) {
                    w.write(privateSetJson);
                } catch (final IOException ex) {
                    Log.warning("Failed to write private key store.");
                }
            } else {
                Log.warning("Unable to create client directory in which to store key pairs.");
            }
        } else {
            Log.warning("Unable to find secure 'keys' directory in which to store key pairs.");
        }
    }

    /**
     * Retrieves the JSON Web Key Set for an LTI registration.
     *
     * @param registration the LTI registration
     * @return the key set
     */
    public JWKS getJWKS(final LtiRegistrationRec registration) {

        JWKS result;

        synchronized (this.ltiKeys) {
            result = this.ltiKeys.get(registration);
        }

        final LocalDateTime now = LocalDateTime.now();
        if (result != null && result.expiration.isBefore(now)) {
            result = null;
        }

        if (result == null) {
            if (registration.jwksUri != null) {
                try {
                    final URI uri = new URI(registration.jwksUri);
                    final URL url = uri.toURL();
                    final URLConnection conn = url.openConnection();
                    final InputStream input = conn.getInputStream();
                    final BufferedReader buf = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                    String inputLine;
                    final StringBuilder buffer = new StringBuilder(1000);
                    while ((inputLine = buf.readLine()) != null) {
                        buffer.append(inputLine);
                    }
                    buf.close();
                    final String responseStr = buffer.toString();

                    try {
                        final Object parsed = JSONParser.parseJSON(responseStr);
                        if (parsed instanceof final JSONObject json) {
                            final LocalDateTime expiry = now.plusMinutes(LTI_KEY_EXPIRY_MINUTES);
                            result = new JWKS(json, expiry);
                            synchronized (this.ltiKeys) {
                                this.ltiKeys.put(registration, result);
                            }
                        } else {
                            Log.warning("Unable to parse data from JWKS endpoint");
                        }
                    } catch (final ParsingException ex) {
                        Log.warning("Unable to parse data from JWKS endpoint", ex);
                    }
                } catch (final URISyntaxException | MalformedURLException ex) {
                    Log.warning("Invalid JWKS endpoint: ", registration.jwksUri, ex);
                } catch (final IOException ex) {
                    Log.warning("Unable to connect to JWKS endpoint: ", registration.jwksUri, ex);
                }
            } else {
                Log.warning("LTI registration did not include JWKS endpoint.");
            }
        }

        return result;
    }

    /**
     * Creates a redirect.
     *
     * @param registration   the LTI registration
     * @param idTokenPayload the ID token payload (verified)
     * @return the Nonce to use for the redirect
     */
    public String createRedirect(final LtiRegistrationRec registration,
                                 JSONObject idTokenPayload) {

        synchronized (this.redirects) {
            String nonce = CoreConstants.newId(24);
            while (this.redirects.containsKey(nonce)) {
                nonce = CoreConstants.newId(24);
            }
            final LocalDateTime now = LocalDateTime.now();
            final LocalDateTime expiry = now.plusMinutes(REDIRECT_EXPIRY_MINUTES);

            final PendingTargetRedirect redirect = new PendingTargetRedirect(nonce, registration, idTokenPayload,
                    expiry);
            this.redirects.put(nonce, redirect);

            return nonce;
        }
    }

    /**
     * Retrieves (and deletes) the redirect associated with a "nonce".
     *
     * @param nonce the nonce value
     * @return the associated redirect, null if none found
     */
    public PendingTargetRedirect getRedirect(final String nonce) {

        synchronized (this.redirects) {
            final PendingTargetRedirect result = this.redirects.remove(nonce);

            if (!this.redirects.isEmpty()) {
                // Check for expired and remove them
                final LocalDateTime now = LocalDateTime.now();

                final Set<Map.Entry<String, PendingTargetRedirect>> entrySet = this.redirects.entrySet();
                final Iterator<Map.Entry<String, PendingTargetRedirect>> iter = entrySet.iterator();
                while (iter.hasNext()) {
                    final Map.Entry<String, PendingTargetRedirect> entry = iter.next();
                    final PendingTargetRedirect value = entry.getValue();
                    final LocalDateTime expiry = value.expiry();
                    if (expiry.isBefore(now)) {
                        iter.remove();
                    }
                }
            }

            return result;
        }
    }

    /**
     * Data for a pending launch.  When the "callback" URI is accessed with a Token ID and a state, the state used to
     * look up the pending launch and that is used to validate the issuer and "nonce", and to obtain the client ID.
     *
     * @param nonce          the nonce
     * @param registration   the LTI registration
     * @param idTokenPayload the target link URI
     * @param expiry         the date/time the redirect will expire
     */
    public record PendingTargetRedirect(String nonce, LtiRegistrationRec registration, JSONObject idTokenPayload,
                                        LocalDateTime expiry) {
    }
}
