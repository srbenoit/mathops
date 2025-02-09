package dev.mathops.web.site.lti;

import dev.mathops.commons.log.Log;
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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * The page that manages the CSU Precalculus Program as an LTI 1.3 tool.
 */
enum PageDynamicRegistration {
    ;

    /**
     * Responds to a GET of "lti13_dynamic_registration.html".
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doDynamicRegistration(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        Log.info("GET request to LTI 1.3 Dynamic Registration");

        final String openIdUrlFromServer = req.getParameter("openid_configuration");

        if (openIdUrlFromServer == null) {
            showErrorPage(req, resp, "Request did not include required 'openid_configuration' parameter.");
        } else {
            String openIdUrl = openIdUrlFromServer;

            // If we have a non-standard port, we need to add that to the URL.  We find the nonstandard port in the
            // "referer" header in the request
            final String referer = req.getHeader("referer");
            if (referer != null) {
                // Referer with a nonstandard port should be of the form "https://host.domain:####/"
                final int firstColon = referer.indexOf(":");
                final int lastColon = referer.lastIndexOf(":");
                if (lastColon > firstColon) {
                    final String port = referer.substring(lastColon);
                    final int hostEnd = openIdUrlFromServer.indexOf("/api");
                    if (hostEnd > 0) {
                        openIdUrl = openIdUrlFromServer.substring(0, hostEnd) + port
                                    + openIdUrlFromServer.substring(hostEnd + 1);
                    }
                }
            }

            // Fetch the OpenID configuration from Canvas
            final String openIdConfigContent = fetchContent(openIdUrl);
            if (openIdConfigContent == null) {
                showErrorPage(req, resp, "Unable to retrieve OpenID configuration from LTI platform.");
            } else {
                Log.fine(openIdConfigContent);
                try {
                    final Object parsed = JSONParser.parseJSON(openIdConfigContent);
                    if (parsed instanceof final JSONObject json) {
                        final String issuer = json.getStringProperty("issuer");

                        if (issuer == null) {
                            showErrorPage(req, resp, "OpenID configuration from LTI platform does not specify issuer.");
                        } else if (referer.startsWith(issuer)) {
                            generateDynamicRegistrationPage(req, resp, json);
                        } else {
                            showErrorPage(req, resp, "OpenID configuration from LTI platform has invalid issuer.");
                        }
                    } else {
                        showErrorPage(req, resp, "Unable to interpret OpenID configuration from LTI platform.");
                    }
                } catch (final ParsingException ex) {
                    Log.warning(ex);
                    showErrorPage(req, resp, "Unable to parse OpenID configuration from LTI platform.");
                }
            }
        }
    }

    /**
     * Generates the content of the dynamic registration page.
     *
     * @param req  the request
     * @param resp the response
     * @param json the parsed JSON OpenID configuration response
     * @throws IOException if there is an error writing the response
     */
    private static void generateDynamicRegistrationPage(final HttpServletRequest req, final HttpServletResponse resp,
                                                        final JSONObject json) throws IOException {

        final Object platformConfigObj = json.getProperty("https://purl.imsglobal.org/spec/lti-platform-configuration");
        if (platformConfigObj instanceof final JSONObject platformConfig) {
            final String productFamilyCode = platformConfig.getStringProperty("product_family_code");
            final String version = platformConfig.getStringProperty("version");

            final HtmlBuilder htm = new HtmlBuilder(2000);

            Page.startNofooterPage(htm, "CSU Precalculus Program - Dynamic Registration", null, false, 0, null, false,
                    true);

            htm.sH(1).add("Precalculus Program").eH(1);
            htm.sH(2).add("LTI 1.3 Developer Key - Dynamic Registration").eH(2);

            htm.sP().add("Detected LTI platform: ", productFamilyCode, " (", version, ")").eP();

            Page.endNoFooterPage(htm, true);

            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        } else {
            showErrorPage(req, resp, "OpenID configuration from LTI platform did not contain platform configuration.");

        }
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
