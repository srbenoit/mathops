package dev.mathops.web.site.lti.canvascourse;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rec.main.LtiRegistrationRec;
import dev.mathops.db.reclogic.main.LtiRegistrationLogic;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;
import dev.mathops.text.parser.xml.XmlEscaper;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.lti.LtiSite;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.net.ssl.HttpsURLConnection;
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
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;

/**
 * The page that manages the CSU Precalculus Program as an LTI 1.3 tool.
 *
 * <p>
 * The process followed here is documented at <a
 * href='https://www.imsglobal.org/spec/lti-dr/v1p0#overview'>https://www.imsglobal.org/spec/lti-dr/v1p0#overview</a>.
 */
public enum PageDynamicRegistration {
    ;

    /** The subtitle for error displays. */
    private static final String SUBTITLE = "LTI Dynamic Registration";

    /**
     * Responds to a GET of "lti13_dynamic_registration.html".  This generates a registration page that will be shown in
     * an IFRAME in the Canvas site to show the user information about the tool and to confirm the registration. The
     * request for this page will contain a registration token that will need to be used once the registration is
     * approved, so that token is embedded in the form as a hidden control.
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    public static void doGet(final LtiSite site, final HttpServletRequest req,
                             final HttpServletResponse resp) throws IOException {

        final String openIdUrlFromServer = req.getParameter("openid_configuration");
        final String registrationToken = req.getParameter("registration_token");

        if (openIdUrlFromServer == null) {
            final String msg = "Request did not include required 'openid_configuration' parameter.";
            PageError.showErrorPage(req, resp, SUBTITLE, msg);
        } else if (registrationToken == null) {
            final String msg = "Request did not include required 'registration_token' parameter.";
            PageError.showErrorPage(req, resp, SUBTITLE, msg);
        } else {
            final String openIdValue = XmlEscaper.escape(openIdUrlFromServer);
            final String tokenValue = XmlEscaper.escape(registrationToken);
            String portStr = CoreConstants.EMPTY;

            // If we have a non-standard port, we need to include that in the form.  We find the nonstandard port in the
            // "referer" header in the request, such as "Referer: https://domino.math.colostate.edu:20443/"
            final String referer = req.getHeader("referer");
            if (referer != null) {
                // Referer with a nonstandard port should be of the form "https://host.domain:####/"
                final int firstColon = referer.indexOf("://");
                final int lastColon = referer.lastIndexOf(":");
                if (lastColon > firstColon) {
                    portStr = referer.substring(lastColon);
                    if (portStr.endsWith("/")) {
                        final int portLen = portStr.length();
                        portStr = portStr.substring(0, portLen - 1);
                    }
                    Log.info("Detected non-standard server port number: ", portStr);
                }
            }

            final HtmlBuilder htm = new HtmlBuilder(1000);

            htm.addln("<!DOCTYPE html>").addln("<html>").addln("<head>");
            htm.addln(" <meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>")
                    .addln(" <link rel='stylesheet' href='basestyle.css' type='text/css'/>")
                    .addln(" <link rel='stylesheet' href='style.css' type='text/css'/>");
            htm.addln("</head>");
            htm.addln("<body style='background:white; padding:20px;'>");

            htm.sP("center").add("<img src='/images/lti_logo.png' alt='CSU Ram logo'/>").eP();

            htm.sH(1, "center").add("CSU Mathematics Program").eH(1);

            htm.sP().add("This is an LTI integration of the course delivery and assessment platform used for ",
                    "Mathematics courses at Colorado State University.").eP();

            htm.sP().add("Please confirm that you wish to register this LTI tool with your LMS.").eP();

            htm.div("vgap");
            htm.sDiv("center");
            htm.addln("<form action='lti13_dynamic_registration.html' method='POST'>");
            htm.addln("  <input type='hidden' name='openid_configuration' value='", openIdValue, "'/>");
            htm.addln("  <input type='hidden' name='registration_token' value='", tokenValue, "'/>");
            htm.addln("  <input type='hidden' name='port' value='", portStr, "'/>");
            htm.addln("  <input class='btn' type='submit' value='Complete the registration.'/>");
            htm.addln("</form>");
            htm.eDiv();

            htm.addln("</body></html>");

            resp.setHeader("Access-Control-Allow-Origin", "*");
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Responds to a POST to "lti13_dynamic_registration.html".  This retrieves the OpenID parameters from the OpenID
     * URL provided in a parameter, then performs the OpenID Client Registration process.  Finally, it returns a web
     * page that closes its containing window, as requested by Canvas.
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    public static void doPost(final Cache cache, final LtiSite site, final HttpServletRequest req,
                              final HttpServletResponse resp) throws IOException {

        final String openIdUrlFromServer = req.getParameter("openid_configuration");
        final String registrationToken = req.getParameter("registration_token");
        final String portStr = req.getParameter("port");

        if (openIdUrlFromServer == null) {
            final String msg = "Form submission did not include required 'openid_configuration' parameter.";
            PageError.showErrorPage(req, resp, SUBTITLE, msg);
        } else if (registrationToken == null) {
            final String msg = "Form submission did not include required 'registration_token' parameter.";
            PageError.showErrorPage(req, resp, SUBTITLE, msg);
        } else {
            String host = null;
            final Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                final String name = headerNames.nextElement();
                if ("host".equals(name)) {
                    host = req.getHeader(name);
                }
            }
            final String path = site.getSite().path;

            String openIdUrl = openIdUrlFromServer;
            if (!portStr.isEmpty()) {
                final int hostStart = openIdUrlFromServer.indexOf("://");
                final int hostEnd = openIdUrlFromServer.indexOf("/", hostStart + 4);
                if (hostEnd > 0) {
                    openIdUrl = openIdUrlFromServer.substring(0, hostEnd) + portStr
                                + openIdUrlFromServer.substring(hostEnd);
                }
            }

            // Fetch the OpenID configuration from Canvas
            Log.info("Fetching OpenID configuration from ", openIdUrl);
            final String openIdConfigContent = fetchContent(openIdUrl, registrationToken);

            if (openIdConfigContent == null) {
                final String msg = "Unable to retrieve OpenID configuration from LTI platform.";
                PageError.showErrorPage(req, resp, SUBTITLE, msg);
            } else {
                // Log.fine(openIdConfigContent);
                try {
                    final Object parsed = JSONParser.parseJSON(openIdConfigContent);
                    if (parsed instanceof final JSONObject json) {
                        final OpenIdConfiguration openIdConfig = new OpenIdConfiguration(json);

                        final String issuer = openIdConfig.getIssuer();

                        if (issuer == null) {
                            final String msg = "OpenID configuration from LTI platform does not specify issuer.";
                            PageError.showErrorPage(req, resp, SUBTITLE, msg);
                        } else if (openIdUrl.startsWith(issuer)) {
                            performClientRegistration(cache, host, path, req, resp, openIdConfig, portStr,
                                    registrationToken);
                        } else {
                            final String msg = "OpenID configuration from LTI platform has invalid issuer.";
                            PageError.showErrorPage(req, resp, SUBTITLE, msg);
                        }
                    } else {
                        final String msg = "Unable to interpret OpenID configuration from LTI platform.";
                        PageError.showErrorPage(req, resp, SUBTITLE, msg);
                    }
                } catch (final ParsingException ex) {
                    final String msg = "Unable to parse OpenID configuration from LTI platform.";
                    PageError.showErrorPage(req, resp, SUBTITLE, msg);
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
     * @param cache             the data cache
     * @param host              the host name
     * @param path              the site path
     * @param req               the request
     * @param resp              the response
     * @param openIdConfig      the OpenID configuration
     * @param portStr           the port string, with leading colon, like ":20443" (empty string for standard port)
     * @param registrationToken the registration token from the registration initiation request
     * @throws IOException if there is an error writing the response
     */
    private static void performClientRegistration(final Cache cache, final String host, final String path,
                                                  final HttpServletRequest req, final HttpServletResponse resp,
                                                  final OpenIdConfiguration openIdConfig, final String portStr,
                                                  final String registrationToken) throws IOException {

        Log.info("Performing client registration.");

        // Send a "Client Registration Request" to the authorization endpoint in the JSON object
        final String regEndpoint = openIdConfig.getRegistrationEndpoint();
        if (regEndpoint == null) {
            final String msg = "OpenID configuration did not include registration_endpoint.";
            PageError.showErrorPage(req, resp, SUBTITLE, msg);
        } else {
            final JSONObject response = doClientRegistrationExchange(host, path, openIdConfig, portStr,
                    registrationToken);

            if (response == null) {
                final String msg = "OpenID Client Registration exchange was unsuccessful.";
                PageError.showErrorPage(req, resp, SUBTITLE, msg);
            } else {
                // Client registration succeeded!  Store information about the registration in a database table.
                final String clientId = response.getStringProperty("client_id");
                final String issuer = openIdConfig.getIssuer();
                final String authEndpoint = openIdConfig.getAuthorizationEndpoint();
                String redirectUri = null;
                final Object o = response.getProperty("redirect_uris");
                if (o instanceof Object[] arr && arr.length > 0) {
                    if (arr[0] instanceof String s) {
                        redirectUri = s;
                    }
                }

                if (clientId == null || issuer == null || authEndpoint == null || redirectUri == null) {
                    final String msg = "LTI Tool registration failed - incomplete registration response.";
                    PageError.showErrorPage(req, resp, SUBTITLE, msg);
                } else {
                    final String jwksUri = openIdConfig.getJWKSUri();
                    try {
                        final LtiRegistrationRec rec = new LtiRegistrationRec(clientId, issuer, portStr, redirectUri,
                                authEndpoint, regEndpoint, jwksUri);
                        LtiRegistrationLogic.INSTANCE.insert(cache, rec);

                        final HtmlBuilder htm = new HtmlBuilder(2000);
                        htm.addln("<!DOCTYPE html>").addln("<html>").addln("<head></head><body>");
                        htm.addln("<script>")
                                .addln("(window.opener || window.parent).postMessage(",
                                        "{subject:'org.imsglobal.lti.close'}, '*');")
                                .addln("</script>");
                        htm.addln("</body></html>");

                        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
                    } catch (final SQLException ex) {
                        final String msg = "LTI Tool registration failed - database error.";
                        PageError.showErrorPage(req, resp, SUBTITLE, msg);
                    }
                }
            }
        }
    }

    /**
     * Assembles and sends the Client Registration request as documented in
     * <a href='https://www.imsglobal.org/spec/lti-dr/v1p0#client-registration-request'>
     * https://www.imsglobal.org/spec/lti-dr/v1p0#client-registration-request</a> and
     * <a href='https://openid.net/specs/openid-connect-registration-1_0.html#ClientRegistration'>
     * https://openid.net/specs/openid-connect-registration-1_0.html#ClientRegistration</a>, and receives the response
     *
     * @param host              the host name of this host
     * @param path              the path of the LTI application on this host
     * @param openIdConfig      the OpenID configuration
     * @param portStr           the port on which the LMS host listens (empty string to use the default port)
     * @param registrationToken the registration token
     * @return the parsed response object if successful; null if unsuccessful
     * @throws IOException if there is an error writing the request or reading the response
     */
    private static JSONObject doClientRegistrationExchange(final String host, final String path,
                                                           final OpenIdConfiguration openIdConfig,
                                                           final String portStr, final String registrationToken)
            throws IOException {

        final String redirect = "https://" + host + path + "/lti13_callback";
        final String initiate = "https://" + host + path + "/lti13_launch";
        final String jwks = "https://" + host + path + "/lti13_jwks";
        final String logo = "https://" + host + path + "/lti_logo.png";
        final String client = "https://" + host + path + "/lti13_client";
        final String policy = "https://" + host + path + "/lti13_policy";
        final String tos = "https://" + host + path + "/lti13_tos";
        final String target = "https://" + host + path + "/lti13_target";

        final HtmlBuilder requestJson = new HtmlBuilder(500);
        requestJson.addln("{");
        requestJson.addln("  \"application_type\": \"web\",");
        requestJson.addln("  \"response_types\": [\"id_token\"],");
        requestJson.addln("  \"grant_types\": [\"implicit\", \"client_credentials\"],");
        requestJson.addln("  \"initiate_login_uri\": \"", initiate, "\",");
        requestJson.addln("  \"redirect_uris\": [\"", redirect, "\"],");
        requestJson.addln("  \"jwks_uri\": \"", jwks, "\",");
        requestJson.addln("  \"logo_uri\": \"", logo, "\",");
        requestJson.addln("  \"client_name\":\"CSU Mathematics Program\",");
        requestJson.addln("  \"client_uri\": \"", client, "\",");
        requestJson.addln("  \"policy_uri\": \"", policy, "\",");
        requestJson.addln("  \"tos_uri\": \"", tos, "\",");
        requestJson.addln("  \"token_endpoint_auth_method\": \"private_key_jwt\",");
        requestJson.addln("  \"contacts\": [\"steve.benoit@colostate.edu\"],");

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

        requestJson.add("  \"scope\": \"");
        boolean space = false;
        for (final Object scope : openIdConfig.getScopesSupported()) {
            if ("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem".equals(scope)
                || "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly".equals(scope)
                || "https://purl.imsglobal.org/spec/lti-ags/scope/score".equals(scope)
                || "https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly".equals(scope)
                || "https://canvas.instructure.com/lti/account_lookup/scope/show".equals(scope)) {
                if (space) {
                    requestJson.add(CoreConstants.SPC);
                }
                requestJson.add(scope);
                space = true;
            }
        }
        requestJson.addln("\",");

        requestJson.addln("  \"https://purl.imsglobal.org/spec/lti-tool-configuration\": {");
        requestJson.addln("    \"domain\": \"", host, "\",");
        requestJson.addln("    \"description\": \"Integration of Mathematics courses and assessments into Canvas.\",");
        requestJson.addln("    \"target_link_uri\": \"", target, "\",");
        requestJson.add("    \"claims\": [");
        boolean comma = false;
        for (final Object claim : openIdConfig.getClaimsSupported()) {
            if ("sub".equals(claim) || "name".equals(claim) || "locale".equals(claim)) {
                if (comma) {
                    requestJson.add(CoreConstants.COMMA);
                }
                requestJson.add("\"", claim, "\"");
                comma = true;
            }
        }
        requestJson.addln("],");
        requestJson.addln("    \"messages\": [");
        comma = false;
        final List<String> resourceLinkPlacements = openIdConfig.getResourceLinkPlacements();
        for (final String res : resourceLinkPlacements) {
            if ("https://canvas.instructure.com/lti/assignment_edit".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Assignment Edit.", res, null);
            } else if ("https://canvas.instructure.com/lti/assignment_group_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Assignment Group Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/assignment_index_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Assignment Index Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/assignment_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Assignment Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/assignment_selection".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Assignment Selection.", res, null);
            } else if ("https://canvas.instructure.com/lti/assignment_view".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Assignment View.", res, null);
            } else if ("https://canvas.instructure.com/lti/collaboration".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Collaboration.", res, null);
            } else if ("https://canvas.instructure.com/lti/conference_selection".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Conference Selection.", res, null);
            } else if ("https://canvas.instructure.com/lti/course_assignments_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Course Assignments Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/course_home_sub_navigation".equals(res)) {
                final String iconUri = "https://" + host + "/www/images/plus.svg";
                comma = addLinkResource(requestJson, comma, "Generate Course Content", res, iconUri);
            } else if ("https://canvas.instructure.com/lti/course_navigation".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Course Navigation.", res, null);
            } else if ("https://canvas.instructure.com/lti/course_settings_sub_navigation".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Settings Sub Navigation.", res, null);
            } else if ("https://canvas.instructure.com/lti/discussion_topic_index_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Discussion Topic Index Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/discussion_topic_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Discussion Topic Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/file_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU File Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/homework_submission".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Homework Submission.", res, null);
            } else if ("https://canvas.instructure.com/lti/link_selection".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Link Selection.", res, null);
            } else if ("https://canvas.instructure.com/lti/migration_selection".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Migration Selection.", res, null);
            } else if ("https://canvas.instructure.com/lti/module_group_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Module Group Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/module_index_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Module Index Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/module_index_menu_modal".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Module Index Menu Modal.", res, null);
            } else if ("https://canvas.instructure.com/lti/module_menu_modal".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Module Menu Modal.", res, null);
            } else if ("https://canvas.instructure.com/lti/module_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Module Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/post_grades".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Post Grades.", res, null);
            } else if ("https://canvas.instructure.com/lti/quiz_index_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Quiz Index Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/quiz_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Quiz Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/similarity_detection".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Similarity Detection Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/student_context_card".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Student Context Card.", res, null);
            } else if ("https://canvas.instructure.com/lti/submission_type_selection".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Submission Type Selection.", res, null);
            } else if ("https://canvas.instructure.com/lti/tool_configuration".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Tool Configuration.", res, null);
            } else if ("https://canvas.instructure.com/lti/top_navigation".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Top Navigation.", res, null);
            } else if ("https://canvas.instructure.com/lti/wiki_index_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Wiki Index Menu.", res, null);
            } else if ("https://canvas.instructure.com/lti/wiki_page_menu".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Wiki Page Menu.", res, null);
            } else if ("ContentArea".equals(res)) {
                comma = addLinkResource(requestJson, comma, "CSU Content Area.", res, null);
            }
        }
        final List<String> deepLinkingPlacements = openIdConfig.getDeepLinkingPlacements();
        for (final String res : deepLinkingPlacements) {
            if ("https://canvas.instructure.com/lti/assignment_selection".equals(res)) {
                comma = addDeepLinking(requestJson, comma, "CSU Deep Assignment Selection.", res);
            } else if ("https://canvas.instructure.com/lti/collaboration".equals(res)) {
                comma = addDeepLinking(requestJson, comma, "CSU Deep Collaboration.", res);
            } else if ("https://canvas.instructure.com/lti/conference_selection".equals(res)) {
                comma = addDeepLinking(requestJson, comma, "CSU Deep Conference Selection.", res);
            } else if ("https://canvas.instructure.com/lti/course_assignments_menu".equals(res)) {
                comma = addDeepLinking(requestJson, comma, "CSU Deep Course Assignments Menu.", res);
            } else if ("https://canvas.instructure.com/lti/editor_button".equals(res)) {
                comma = addDeepLinking(requestJson, comma, "CSU Deep Editor Button.", res);
            } else if ("https://canvas.instructure.com/lti/homework_submission".equals(res)) {
                comma = addDeepLinking(requestJson, comma, "CSU Deep Homework Submission.", res);
            } else if ("https://canvas.instructure.com/lti/link_selection".equals(res)) {
                comma = addDeepLinking(requestJson, comma, "CSU Deep Link Selection.", res);
            } else if ("https://canvas.instructure.com/lti/migration_selection".equals(res)) {
                comma = addDeepLinking(requestJson, comma, "CSU Deep Migration Selection.", res);
            } else if ("https://canvas.instructure.com/lti/module_index_menu_modal".equals(res)) {
                comma = addDeepLinking(requestJson, comma, "CSU Deep Module Index Menu Modal.", res);
            } else if ("https://canvas.instructure.com/lti/module_menu_modal".equals(res)) {
                comma = addDeepLinking(requestJson, comma, "CSU Deep Module Menu Modal.", res);
            } else if ("https://canvas.instructure.com/lti/submission_type_selection".equals(res)) {
                comma = addDeepLinking(requestJson, comma, "CSU Deep Submission Type Selection.", res);
            } else if ("ContentArea".equals(res)) {
                comma = addDeepLinking(requestJson, comma, "CSU Deep Content Area.", res);
            } else if ("RichTextEditor".equals(res)) {
                comma = addDeepLinking(requestJson, comma, "CSU Deep Rich Text Area.", res);
            }
        }
        if (comma) {
            requestJson.addln();
        }
        requestJson.addln("    ]");
        requestJson.addln("  }");
        requestJson.addln("}");

        final String requestStr = requestJson.toString();
        Log.fine(requestStr);

        final byte[] requestBytes = requestStr.getBytes(StandardCharsets.UTF_8);
        final int contentLen = requestBytes.length;
        final String contentLenStr = Integer.toString(contentLen);

        final String issuer = openIdConfig.getIssuer();
        final int slash = issuer.indexOf("://");
        final String issuerHost = slash == -1 ? issuer : issuer.substring(slash + 3);

        final String regEndpoint = openIdConfig.getRegistrationEndpoint();
        final URL url = new URL(regEndpoint);
        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Host", issuerHost + portStr);
        connection.setRequestProperty("Authorization", "Bearer " + registrationToken);
        connection.setRequestProperty("Content-Length", contentLenStr);
        connection.setUseCaches(false);
        connection.setDoOutput(true);

        try (final OutputStream out = connection.getOutputStream()) {
            out.write(requestBytes);
        }

        JSONObject result = null;

        final int status = connection.getResponseCode();

        if (status >= 400) {
            final InputStream error = connection.getErrorStream();
            final BufferedReader buf = new BufferedReader(new InputStreamReader(error, StandardCharsets.UTF_8));
            String inputLine;
            final StringBuilder buffer = new StringBuilder(1000);
            while ((inputLine = buf.readLine()) != null) {
                buffer.append(inputLine);
            }
            buf.close();
            Log.info("Error response to Client Registration request:");
            Log.fine(buffer);
        } else {
            // Get Response
            final InputStream input = connection.getInputStream();
            final BufferedReader buf = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            String inputLine;
            final StringBuilder buffer = new StringBuilder(1000);
            while ((inputLine = buf.readLine()) != null) {
                buffer.append(inputLine);
            }
            buf.close();
            final String responseStr = buffer.toString();

            Log.info("Client Registration Response:");
            Log.fine(responseStr);

            try {
                final Object parsed = JSONParser.parseJSON(responseStr);
                if (parsed instanceof final JSONObject json) {
                    result = json;
                }
            } catch (final ParsingException ex) {
                Log.warning("Unable to parse Client Registration Response", ex);
            }
        }

        return result;
    }

    /**
     * Adds a resource link request placement message entry.
     *
     * @param requestJson the {@code HtmlBuilder} to which to append
     * @param comma       true to begin with a comma and linefeed
     * @param label       the label
     * @param placement   the placement
     * @param iconUri     if not null, the URI of an icon for the placement
     * @return true (the new "comma" setting)
     */
    private static boolean addLinkResource(final HtmlBuilder requestJson, final boolean comma, final String label,
                                           final String placement, final String iconUri) {

        if (comma) {
            requestJson.addln(",");
        }
        requestJson.addln("      {");
        requestJson.addln("        \"type\": \"LtiResourceLinkRequest\",");
        requestJson.addln("        \"label\": \"", label, "\",");
        if (iconUri != null) {
            requestJson.addln("        \"icon_uri\": \"", iconUri, "\",");
        }
        requestJson.addln("        \"placements\": [\"", placement, "\"]");
        requestJson.add("      }");

        return true;
    }

    /**
     * Adds a resource deep linking placement message entry.
     *
     * @param requestJson the {@code HtmlBuilder} to which to append
     * @param comma       true to begin with a comma and linefeed
     * @param label       the label
     * @param placement   the placement
     * @return true (the new "comma" setting)
     */
    private static boolean addDeepLinking(final HtmlBuilder requestJson, final boolean comma, final String label,
                                          final String placement) {

        if (comma) {
            requestJson.addln(",");
        }
        requestJson.addln("      {");
        requestJson.addln("        \"type\": \"LtiDeepLinkingRequest\",");
        requestJson.addln("        \"label\": \"", label, "\",");
        requestJson.addln("        \"placements\": [\"", placement, "\"]");
        requestJson.add("      }");

        return true;
    }

    /**
     * Fetches content.
     *
     * @param urlStr the URL whose content to fetch
     * @param token  the bearer token
     * @return the content; null if fetch failed
     */
    private static String fetchContent(final String urlStr, final String token) {

        String result = null;
        try {
            final URI uri = new URI(urlStr);
            final URL url = uri.toURL();
//            Log.info(url);

            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + token);
            final int code = con.getResponseCode();

            final InputStream inputStream = con.getInputStream();
            final BufferedReader in = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));
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
