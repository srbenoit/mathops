package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.schema.main.rec.LtiRegistrationRec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.internet.RFC7518;
import dev.mathops.text.internet.RFC8017PublicKey;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;
import dev.mathops.web.host.course.lti.JWKS;
import dev.mathops.web.host.course.lti.LtiSite;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;

/**
 * The page that manages the CSU Precalculus Program as an LTI 1.3 tool.
 *
 * <p>
 * The process followed here is documented at <a
 * href='https://www.imsglobal.org/spec/lti-dr/v1p0'>https://www.imsglobal.org/spec/lti-dr/v1p0#overview</a>.
 */
public enum LTIApiCallback {
    ;

    /**
     * Responds to a GET or POST to "lti13_launch_callback" with an Authentication Response as described in
     * <a href='https://www.imsglobal.org/spec/security/v1p0/'>https://www.imsglobal.org/spec/security/v1p0/</a>.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException if there is an error writing the response
     */
    public static void doCallback(final Cache cache, final LtiSite site, final HttpServletRequest req,
                                  final HttpServletResponse resp) throws IOException {

        final Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            final String name = paramNames.nextElement();
            final String value = req.getParameter(name);
            Log.info("Callback param '", name, "' = ", value);
        }

//        final String state = req.getParameter("state");
//        final LTILaunch.PendingLaunch pending = LTILaunch.getPendingLaunch(state);
//        if (pending == null) {
//            PageError.showErrorPage(req, resp, "LTI Launch Process",
//                    "No active invocation of the tool was found - possible timeout?");
//        } else {
//            // We need the public JKWS key from the Canvas instance
//            final JWKS jwks = site.getJWKS(pending.registration());
//            if (jwks == null) {
//                PageError.showErrorPage(req, resp, "LTI Launch Process",
//                        "Unable to retrieve public key from LMS to validate LTI request.");
//            } else {
//                final String idToken = req.getParameter("id_token");
//                validateIdToken(cache, site, req, resp, idToken, jwks, pending);
//            }
//        }
    }

    /**
     * Validates the "id_token" using JSON Web Signature (RFC 7515).
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param idToken the ID token, which is a JWS object in encoded form
     * @param jwks    the public key set from the server
     * @param pending the pending launch object (used to validate the "nonce")
     * @throws IOException if there is an error writing the response
     */
    static void validateIdToken(final Cache cache, final LtiSite site, final HttpServletRequest req,
                                final HttpServletResponse resp, final String idToken,
                                final JWKS jwks, final LTILaunch.PendingLaunch pending) throws IOException {

        final int dot1 = idToken.indexOf('.');
        final int dot2 = idToken.lastIndexOf('.');

        JSONObject validatedPayload = null;
        if (dot1 == -1 || dot2 == -1 || dot2 == dot1) {
            Log.warning("Failed to parse JWS in compact form.");
            // TODO: Try the JSON form?
        } else {
            final String part1 = idToken.substring(0, dot1);
            final String part2 = idToken.substring(dot1 + 1, dot2);
            final String part3 = idToken.substring(dot2 + 1);
            validatedPayload = validateCompactForm(jwks, pending, part1, part2, part3);
        }

        if (validatedPayload == null) {
            PageError.showErrorPage(req, resp, "LTI Launch Process", "Unable to validate LTI request.");
        } else {
            redirectToTarget(site, resp, pending, validatedPayload);
        }
    }

    /**
     * Validates a JWS in the compact form.
     *
     * @param jwks    the public key set from the server
     * @param pending the pending launch object (used to validate the "nonce")
     * @param part1   the part of the JWS compact form that preceded the first period character
     * @param part2   the part of the JWS compact form between the first and last period characters
     * @param part3   the part of the JWS compact form after the last period
     * @return the validate payload if it was valid; null if not
     */
    private static JSONObject validateCompactForm(final JWKS jwks, final LTILaunch.PendingLaunch pending,
                                                  final String part1, final String part2, final String part3) {

        JSONObject result = null;

        final Base64.Decoder dec = Base64.getUrlDecoder();
        try {
            final byte[] bytes1 = dec.decode(part1);
            final String str1 = new String(bytes1, StandardCharsets.UTF_8);

            try {
                if (JSONParser.parseJSON(str1) instanceof final JSONObject joseHeader) {
                    final JSONObject validHeader = validateProtectedHeader(joseHeader);

                    if (validHeader != null) {
                        final String alg = validHeader.getStringProperty("alg");
                        final String kid = validHeader.getStringProperty("kid");

                        // Look for a public key with which to validate the request
                        JWKS.JWK matchingKey = null;
                        final int numKeys = jwks.getNumKeys();
                        for (int i = 0; i < numKeys; ++i) {
                            final JWKS.JWK key = jwks.getKey(i);
                            if (kid.equals(key.kid)) {
                                matchingKey = key;
                                break;
                            }
                        }

                        if (matchingKey == null) {
                            Log.warning("No matching signature key found for '", alg, "' algorithm.");
                        } else if (matchingKey.publicKey == null) {
                            Log.warning("Matching signature key for '", alg, "' algorithm had no public key.");
                        } else {
                            final RFC8017PublicKey sigKey = matchingKey.publicKey;

                            final byte[] bytes2 = dec.decode(part2);
                            final String str2 = new String(bytes2, StandardCharsets.UTF_8);

                            try {
                                if (JSONParser.parseJSON(str2) instanceof final JSONObject payload) {
                                    final byte[] signature = dec.decode(part3);
                                    final String sigInputStr = part1 + "." + part2;
                                    final byte[] signingInput = sigInputStr.getBytes(StandardCharsets.UTF_8);

                                    if ("RS256".equals(alg)) {
                                        if (RFC7518.validateAlgRS256(signingInput, sigKey, signature)) {
                                            Log.info("RS256 signature on 'token_id' has been verified.");
                                            result = validatePayload(payload, pending);
                                        } else {
                                            Log.warning("Invalid digital signature on 'token_id'");
                                        }
                                    } else {
                                        Log.warning("Unsupported algorithm: ", alg);
                                    }
                                }
                            } catch (final ParsingException ex1) {
                                Log.warning("Failed to parse JWS payload as JSON.", ex1);
                            }
                        }
                    }
                }
            } catch (final ParsingException ex2) {
                Log.warning("Failed to parse JWS protected header as JSON.", ex2);
            }
        } catch (final IllegalArgumentException ex) {
            Log.warning("Failed to Base64 decode JWS in compact form.", ex);
        }

        return result;
    }

    /**
     * Validates the fields in the payload.
     *
     * @param payload the payload to validate
     * @param pending the pending launch object (used to validate the "nonce")
     * @return the validate payload if it was valid; null if not
     */
    private static JSONObject validatePayload(final JSONObject payload, final LTILaunch.PendingLaunch pending) {

        JSONObject result = null;

        final String payloadNonce = payload.getStringProperty("nonce");

        if (pending.nonce().equals(payloadNonce)) {
            final LtiRegistrationRec registration = pending.registration();

            final String payloadIss = payload.getStringProperty("iss");
            if (registration.issuer.equals(payloadIss)) {
                final Object payloadAud = payload.getProperty("aud");

                if (payloadAud.equals(registration.clientId)) {

                    final String payloadAzp = payload.getStringProperty("azp");
                    if (payloadAzp == null || payloadAzp.equals(registration.clientId)) {
                        final long timestamp = System.currentTimeMillis() / 1000L;
                        final Double payloadExp = payload.getNumberProperty("exp");
                        if (payloadExp == null || payloadExp.longValue() > timestamp) {
                            result = payload;
                        } else {
                            Log.warning("payload in ID token has expired");
                        }
                    } else {
                        Log.warning("'authorized party' specified in payload does not match registered client ID.");
                    }
                } else {
                    Log.warning("'audience' specified in payload does not match registered client ID.");
                }
            } else {
                Log.warning("No matching issuer within 'token_id'");
            }
        } else {
            Log.warning("No matching nonce within 'token_id'");
        }

        return result;
    }

    /**
     * Verifies that all values in a JSON object representing a protected header are recognized and supported.
     *
     * @param joseHeader the header
     * @return the header if valid; null if not
     */
    private static JSONObject validateProtectedHeader(final JSONObject joseHeader) {

        final JSONObject result;

        final String alg = joseHeader.getStringProperty("alg");
        if ("RS256".equals(alg) || "ES256".equals(alg) || "RSA1_5".equals(alg) || "RSA-OAEP".equals(alg)
            || "A128KW".equals(alg) || "A256KW".equals(alg) || "dir".equals(alg) || "ECDH-ES".equals(alg)
            || " ECDH-ES+A128KW".equals(alg) || "ECDH-ES+A256KW".equals(alg)) {

            if (joseHeader.getStringProperty("kid") == null) {
                Log.warning("Missing required 'kid' value in protected JWS header: ", alg);
                result = null;
            } else {
                result = joseHeader;
            }
        } else {
            Log.warning("Unsupported 'alg' value in protected JWS header: ", alg);
            result = null;
        }

        return result;
    }

    /**
     * Dispatches a request.
     *
     * @param site             the owning site
     * @param resp             the response
     * @param pending          the pending launch information
     * @param validatedPayload the validated payload
     * @throws IOException if there is an error writing the response
     */
    static void redirectToTarget(final LtiSite site, final HttpServletResponse resp,
                                 final LTILaunch.PendingLaunch pending, final JSONObject validatedPayload)
            throws IOException {

        final LtiRegistrationRec registration = pending.registration();
        final String nonce = site.createRedirect(registration, validatedPayload);
        final String target = pending.targetLinkUri();

        final HtmlBuilder htm = new HtmlBuilder(1000);
        htm.add(target, "?nonce=", nonce);
        final String location = htm.toString();

        resp.sendRedirect(location, 302);
    }
}
