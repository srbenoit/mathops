package dev.mathops.web.site.lti;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.cfg.Site;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * The page that manages the CSU Precalculus Program as an LTI 1.3 tool.
 *
 * <p>
 * The process followed here is documented at <a
 * href='https://www.imsglobal.org/spec/lti-dr/v1p0#overview'>https://www.imsglobal.org/spec/lti-dr/v1p0#overview</a>.
 */
enum PageDynamicRegistration {
    ;

    /**
     * Responds to a GET of "lti13_dynamic_registration.html".
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doDynamicRegistration(final LtiSite site, final HttpServletRequest req,
                                      final HttpServletResponse resp) throws IOException {

        Log.info("GET request to LTI 1.3 Dynamic Registration");

        String registrationToken = null;
        String host = null;
        final String path = site.getSite().path;

        final Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            final String name = paramNames.nextElement();
            final String[] values = req.getParameterValues(name);
            Log.info("  Parameter {", name, "} = ", Arrays.toString(values));
            if ("registration_token".equals(name)) {
                registrationToken = values[0];
            }
        }
        final Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String name = headerNames.nextElement();
            final String value = req.getHeader(name);
            Log.info("  Header {", name, "} = ", value);
            if ("host".equals(name)) {
                host = value;
            }
        }

        final String openIdUrlFromServer = req.getParameter("openid_configuration");

        if (openIdUrlFromServer == null) {
            final String msg = "Request did not include required 'openid_configuration' parameter.";
            Log.warning(msg);
            showErrorPage(req, resp, msg);
        } else {
            if (registrationToken == null) {
                final int pos = openIdUrlFromServer.indexOf("registration_token=");
                if (pos > 0) {
                    registrationToken = openIdUrlFromServer.substring(pos + 19);
                }
            }

            if (registrationToken == null) {
                final String msg = "No registration token in registration initiating request.";
                Log.warning(msg);
                showErrorPage(req, resp, msg);
            } else {
                String openIdUrl = openIdUrlFromServer;

                // openIdUrl format: "https://domino.math.colostate.edu/api/lt...

                // If we have a non-standard port, we need to add that to the URL.  We find the nonstandard port in the
                // "referer" header in the request, such as "Referer: https://domino.math.colostate.edu:20443/"
                final String referer = req.getHeader("referer");
                if (referer != null) {
                    // Referer with a nonstandard port should be of the form "https://host.domain:####/"
                    final int firstColon = referer.indexOf("://");
                    final int lastColon = referer.lastIndexOf(":");
                    if (lastColon > firstColon) {
                        String portStr = referer.substring(lastColon);
                        if (portStr.endsWith("/")) {
                            final int portLen = portStr.length();
                            portStr = portStr.substring(0, portLen - 1);
                        }
                        Log.info("Detected non-standard server port number: ", portStr);

                        final int hostStart = openIdUrlFromServer.indexOf("://");
                        final int hostEnd = openIdUrlFromServer.indexOf("/", hostStart + 4);
                        if (hostEnd > 0) {
                            openIdUrl = openIdUrlFromServer.substring(0, hostEnd) + portStr
                                        + openIdUrlFromServer.substring(hostEnd);
                        }
                    }
                }

                // Fetch the OpenID configuration from Canvas
                Log.info("Fetching OpenID configuration from ", openIdUrl);
                final String openIdConfigContent = fetchContent(openIdUrl);

                if (openIdConfigContent == null) {
                    final String msg = "Unable to retrieve OpenID configuration from LTI platform.";
                    Log.warning(msg);
                    showErrorPage(req, resp, msg);
                } else {
                    Log.fine(openIdConfigContent);

                    try {
                        final Object parsed = JSONParser.parseJSON(openIdConfigContent);
                        if (parsed instanceof final JSONObject json) {
                            final String issuer = json.getStringProperty("issuer");

                            if (issuer == null) {
                                final String msg = "OpenID configuration from LTI platform does not specify issuer.";
                                Log.warning(msg);
                                showErrorPage(req, resp, msg);
                            } else if (openIdUrl.startsWith(issuer)) {
                                performClientRegistration(host, path, req, resp, issuer, registrationToken, json);
                            } else {
                                final String msg = "OpenID configuration from LTI platform has invalid issuer.";
                                Log.warning(msg);
                                showErrorPage(req, resp, msg);
                            }
                        } else {
                            final String msg = "Unable to interpret OpenID configuration from LTI platform.";
                            Log.warning(msg);
                            showErrorPage(req, resp, msg);
                        }
                    } catch (final ParsingException ex) {
                        final String msg = "Unable to parse OpenID configuration from LTI platform.";
                        Log.warning(msg, ex);
                        showErrorPage(req, resp, msg);
                    }
                }
            }
        }
    }

    /**
     * Performs the client registration.
     *
     * <p>
     * This portion of the process is documented in
     * <a href='https://openid.net/specs/openid-connect-registration-1_0.html#ClientRegistration'>
     * https://openid.net/specs/openid-connect-registration-1_0.html#ClientRegistration</a>.
     *
     * <p>
     * The OAuth2 portion of the process is defined in <a
     * href='https://datatracker.ietf.org/doc/html/rfc6749#section-1.2'>
     * https://datatracker.ietf.org/doc/html/rfc6749#section-1.2</a>
     * </p>
     *
     * @param host              the host name
     * @param path              the site path
     * @param req               the request
     * @param resp              the response
     * @param issuer            the issuer
     * @param registrationToken the registration token from the registration initiation request
     * @param json              the parsed JSON OpenID configuration response
     * @throws IOException if there is an error writing the response
     */
    private static void performClientRegistration(final String host, final String path, final HttpServletRequest req,
                                                  final HttpServletResponse resp,
                                                  final String issuer, final String registrationToken,
                                                  final JSONObject json) throws IOException {

        Log.info("Performing client registration.");

        // Send a "Client Registration Request" to the authorization endpoint in the JSON object
        final String regEndpoint = json.getStringProperty("registration_endpoint");
        if (regEndpoint == null) {
            final String msg = "OpenID configuration did not include registration_endpoint.";
            Log.warning(msg);
            showErrorPage(req, resp, msg);
        } else {
            final Object scopes = json.getProperty("scopes_supported");
            if (scopes instanceof Object[] scopesArray) {
                final Object claims = json.getProperty("claims_supported");
                if (claims instanceof Object[] claimsArray) {
                    // Create the Client Registration Request:
                    foo(host, path, issuer, registrationToken, regEndpoint, scopesArray, claimsArray);
                } else {
                    final String msg = "OpenID configuration did not include claims_supported array.";
                    Log.warning(msg);
                    showErrorPage(req, resp, msg);
                }
            } else {
                final String msg = "OpenID configuration did not include scopes_supported array.";
                Log.warning(msg);
                showErrorPage(req, resp, msg);
            }
        }
    }

    private static void foo(final String host, final String path, final String issuer, final String registrationToken,
                            final String regEndpoint, final Object[] scopesSupported,
                            final Object[] claimsSupported) throws IOException {

        final String redirect1 = "https://" + host + path + "/lti13_registration_callback1.json";
        final String redirect2 = "https://" + host + path + "/lti13_registration_callback2.json";
        final String initiate = "https://" + host + path + "/lti13_launch.json";
        final String jwks = "https://" + host + path + "/lti13_jwks.json";
        final String target = "https://" + host + path + "/lti13_target.json";

        final HtmlBuilder contentJson = new HtmlBuilder(500);
        contentJson.addln("{");
        contentJson.addln("  \"application_type\":\"web\",");
        contentJson.addln("  \"response_types\":[\"id_token\"],");
        contentJson.addln("  \"grant_types\":[\"implict\", \"client_credentials\"],");
        contentJson.addln("  \"redirect_uris\":[\"", redirect1, "\",\"", redirect2, "\"],");
        contentJson.addln("  \"initiate_login_uri\":\"", initiate, "\",");
        contentJson.addln("  \"jwks_uri\":\"", jwks, "\",");
        contentJson.addln("  \"client_name\":\"CSU Mathematics Program\",");
        contentJson.addln("  \"token_endpoint_auth_method\":\"private_key_jwt\",");

        // Generate  list of requested scopes as a subset of the list provided by Canvas
        // This tool can use these scopes:
        // 1. Create and view assignment data in the gradebook associated with the tool
        //    "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem"
        // 2. View submission data for assignments associated with the tool.
        //    "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly"
        // 3. Create and update submission results for assignments associated with the tool
        //    "https://purl.imsglobal.org/spec/lti-ags/scope/score"
        // 4. Retrieve user data associated with the context the tool is installed in
        //    "https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly",
        // 5. Lookup Account information
        //    "https://canvas.instructure.com/lti/account_lookup/scope/show"

        contentJson.add("  \"scope\":\"");
        boolean space = false;
        for (final Object scope : scopesSupported) {
            if ("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem".equals(scope)
                || "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly".equals(scope)
                || "https://purl.imsglobal.org/spec/lti-ags/scope/score".equals(scope)
                || "https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly".equals(scope)
                || "https://canvas.instructure.com/lti/account_lookup/scope/show".equals(scope)) {
                if (space) {
                    contentJson.add(CoreConstants.SPC);
                }
                contentJson.add(scope);
                space = true;
            }
        }
        contentJson.addln("\",");

        contentJson.addln("  \"https://purl.imsglobal.org/spec/lti-tool-configuration\":{");
        contentJson.addln("    \"domain\":\"math.colostate.edu\",");
        contentJson.addln("    \"description\":\"Integration of Mathematics courses and assessments into Canvas.\",");
        contentJson.addln("    \"target_link_uri\":\"", target, "\",");
        contentJson.add("    \"claims\":[");
        boolean comma = false;
        for (final Object claim : claimsSupported) {
            if ("sub".equals(claim) || "name".equals(claim) || "locale".equals(claim)) {
                if (comma) {
                    contentJson.add(CoreConstants.COMMA);
                }
                contentJson.add("\"", claim, "\"");
                comma = true;
            }
        }
        contentJson.addln("],");
        contentJson.addln("    \"messages\":[");
        contentJson.addln("      {");
        contentJson.addln("        \"type\":\"LtiDeepLinkingRequest\",");
        contentJson.addln("        \"label\":\"Just Stand There.\",");
        contentJson.addln("        \"placements\":[\"ContentArea\"],");
        contentJson.addln("        \"supported_types\":[\"ltiResourceLink\"]");
        contentJson.addln("      },");
        contentJson.addln("      {");
        contentJson.addln("        \"type\":\"LtiDeepLinkingRequest\",");
        contentJson.addln("        \"label\":\"Do Something!\",");
        contentJson.addln("        \"placements\":[\"RichTextEditor\"],");
        contentJson.addln("        \"roles\":[\"http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper\", ",
                "\"http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor\"],");
        contentJson.addln("        \"supported_types\":[\"file\"],");
        contentJson.addln("        \"supported_media_types\":[\"image/*\"]");
        contentJson.addln("      }");
        contentJson.addln("    ]");
        contentJson.addln("  }");
        contentJson.addln("}");

        final String contentStr = contentJson.toString();
        Log.fine(contentStr);
        final byte[] contentBytes = contentStr.getBytes(StandardCharsets.UTF_8);
        final int contentLen = contentBytes.length;
        final String contentLenStr = Integer.toString(contentLen);

        final int slash = issuer.indexOf("://");
        final String issuerHost = slash == -1 ? issuer : issuer.substring(slash + 3);

        final URL url = new URL(regEndpoint);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Host", issuerHost);
        connection.setRequestProperty("Authorization", "Bearer " + registrationToken);
        connection.setRequestProperty("Content-Length", contentLenStr);

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        try (final OutputStream out = connection.getOutputStream()) {
            out.write(contentBytes);
        }

        final int status = connection.getResponseCode();
        final String msg = connection.getResponseMessage();
        Log.info("Response Status is " + status + ": " + msg);

        final Map<String, List<String>> map = connection.getHeaderFields();
        for (final Map.Entry<String, List<String>> entry : map.entrySet()) {
            Log.warning("    ", entry.getKey(), ": ", entry.getValue());
        }

//        // Get Response
        final Object content = connection.getContent();

        Log.info("Response to Client Registration request:");
        Log.fine(content.toString());
    }

    /**
     * Shows a page that displays an error message.
     *
     * @param req  the request
     * @param resp the response
     * @param msg  the error message
     */
    private static void showErrorPage(final ServletRequest req, final HttpServletResponse resp,
                                      final String msg) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(2000);

        Page.startNofooterPage(htm, "CSU Precalculus Program - Dynamic Registration", null, false, 0, null, false,
                true);

        htm.sH(1).add("Precalculus Program").eH(1);
        htm.sH(2).add("LTI 1.3 Developer Key - Dynamic Registration").eH(2);

        htm.sP().add("An error has occurred:").eP();
        htm.sP().add(msg).eP();

        Page.endNoFooterPage(htm, true);

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Fetches content.
     *
     * @param urlStr the URL whose content to fetch
     * @return the content; null if fetch failed
     */
    private static String fetchContent(final String urlStr) {

        String result = null;
        try {
            final URI uri = new URI(urlStr);
            final URL url = uri.toURL();
//            Log.info(url);

            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", "Bearer 1122334455");
            con.getResponseCode();

            final InputStream inputStream = con.getInputStream();
            final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String inputLine;
            final StringBuilder buffer = new StringBuilder(1000);
            while ((inputLine = in.readLine()) != null) {
                buffer.append(inputLine);
            }
            in.close();
            con.disconnect();
            result = buffer.toString();
        } catch (final IOException | URISyntaxException ex) {
            Log.warning(ex);
        }

        return result;
    }
}
