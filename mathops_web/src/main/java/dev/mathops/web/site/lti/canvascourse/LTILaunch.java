package dev.mathops.web.site.lti.canvascourse;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rec.main.LtiRegistrationRec;
import dev.mathops.db.reclogic.main.LtiRegistrationLogic;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.lti.LtiSite;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The page that handles an "LTI Launch", as defined in
 * <a href='https://www.imsglobal.org/spec/security/v1p0/'>https://www.imsglobal.org/spec/security/v1p0/</a>,
 * <a href='https://www.imsglobal.org/spec/security/v1p0/'>https://www.imsglobal.org/spec/security/v1p0/</a>, and
 * <a href='https://openid.net/specs/openid-connect-core-1_0.html'>
 * https://openid.net/specs/openid-connect-core-1_0.html</a>.
 *
 * <p>
 * The process followed here is documented at <a
 * href='https://www.imsglobal.org/spec/lti-dr/v1p0#overview'>https://www.imsglobal.org/spec/lti-dr/v1p0#overview</a>.
 */
public enum LTILaunch {
    ;

    /** The number of minutes a launch can wait before occurring. */
    private static final long LAUNCH_EXPIRY_MINUTES = 10L;

    /** A map from "state" string to the pending launch. */
    private static final Map<String, PendingLaunch> LAUNCHES = new HashMap<>(20);

    /**
     * Searches for a pending launch with a particular state.
     *
     * @param state the state string
     * @return the pending launch data, if found; null if not
     */
    static PendingLaunch getPendingLaunch(final String state) {

        synchronized (LAUNCHES) {
            final LocalDateTime now = LocalDateTime.now();
            final Set<Map.Entry<String, PendingLaunch>> entrySet = LAUNCHES.entrySet();
            final Iterator<Map.Entry<String, PendingLaunch>> iter = entrySet.iterator();
            while (iter.hasNext()) {
                final Map.Entry<String, PendingLaunch> entry = iter.next();
                final PendingLaunch pending = entry.getValue();
                final LocalDateTime expiry = pending.expiry();
                if (expiry.isBefore(now)) {
                    iter.remove();
                }
            }

            return LAUNCHES.remove(state);
        }
    }

    /**
     * Responds to a GET or POST to "lti13_launch".  This redirects the client browser (which is embedded in the LMS) to
     * the authorization server for that LMS (as defined during LTI tool registration).
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException if there is an error writing the response
     */
    public static void doLaunch(final Cache cache, final LtiSite site, final HttpServletRequest req,
                                final HttpServletResponse resp) throws IOException {

        // final Enumeration<String> paramNames = req.getParameterNames();
        // while (paramNames.hasMoreElements()) {
        //     final String name = paramNames.nextElement();
        //     final String value = req.getParameter(name);
        //     Log.info("Launch param '", name, "' = ", value);
        // }
        // 'iss' = ...
        // 'login_hint' = ...
        // 'client_id' = ...
        // 'lti_deployment_id' = ...
        // 'target_link_uri' = ...
        // 'lti_message_hint' = ...
        // 'canvas_environment' = ...
        // 'canvas_region' = ...
        // 'deployment_id' = ...
        // 'lti_storage_target' = ...

        final String clientId = req.getParameter("client_id");
        final String issuer = req.getParameter("iss");
        final String loginHint = req.getParameter("login_hint");
        final String ltiMessageHint = req.getParameter("lti_message_hint");
        final String targetLinkUri = req.getParameter("target_link_uri");

        LtiRegistrationRec registration = null;

        try {
            registration = LtiRegistrationLogic.INSTANCE.query(cache, clientId, issuer);
        } catch (final SQLException ex) {
            Log.warning("Failed to query for LTI registration.", ex);
        }

        if (registration == null) {
            PageError.showErrorPage(req, resp, "LTI Launch",
                    "Unable to look up LTI tool registration.  LTI tool may need to be re-installed.");
        } else {
            // We need to do a redirect within the browser to the authorization endpoint with
            final String state = CoreConstants.newId(20);
            final String nonce = CoreConstants.newId(20);

            final LocalDateTime now = LocalDateTime.now();
            final LocalDateTime expires = now.plusMinutes(LAUNCH_EXPIRY_MINUTES);
            synchronized (LAUNCHES) {
                LAUNCHES.put(state, new PendingLaunch(nonce, registration, targetLinkUri, expires));
            }

            final HtmlBuilder htm = new HtmlBuilder(1000);

            htm.add(registration.authEndpoint, "?scope=openid&response_type=id_token&client_id=", clientId,
                    "&redirect_uri=", registration.redirectUri, "&login_hint=", loginHint, "&lti_message_hint=",
                    ltiMessageHint, "&state=", state, "&response_mode=form_post&nonce=", nonce, "&prompt=none");

            final String location = htm.toString();

            resp.sendRedirect(location, 302);
        }
    }

    /**
     * Data for a pending launch.  When the "callback" URI is accessed with a Token ID and a state, the state used to
     * look up the pending launch and that is used to validate the issuer and "nonce", and to obtain the client ID.
     *
     * @param nonce         the nonce
     * @param registration  the LTI registration
     * @param targetLinkUri the target link URI
     * @param expiry        the date/time the login will expire
     */
    public record PendingLaunch(String nonce, LtiRegistrationRec registration, String targetLinkUri,
                                LocalDateTime expiry) {
    }
}
