package dev.mathops.web.site.lti;

import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;

/**
 * The page that manages the CSU Precalculus Program as an LTI 1.3 tool.
 */
enum PageLTI13 {
    ;

    /** Host - change to toggle between DEV and PROD. */
    private static final String HOST = "coursedev.math.colostate.edu";

    /**
     * Responds to a GET of "lti13_dev_key_configuration.json.xml".
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doGetDevKeyConfigurationJson(final HttpServletRequest req,
                                             final HttpServletResponse resp) throws IOException {

        Log.info("GET request for LTI 1.3 configuration JSON:");

        final Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String name = headerNames.nextElement();
            final String value = req.getHeader(name);
            Log.info("   ", name, " = ", value);
        }

        final HtmlBuilder htm = new HtmlBuilder(2000);

        htm.addln("""
                {
                  "title": "CSU Precalculus Program",
                  "description": "A Canvas deep integration to provide Precalculus course content.",
                  "oidc_initiation_url": "https://coursedev.math.colostate.edu/lti/oidc_initiation.json",
                  "target_link_uri": "https://coursedev.math.colostate.edu/lti/target_link.json",
                  "scopes": [
                    "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem",
                    "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly"
                  ],
                  "extensions": [
                    {
                      "domain": "math.colostate.edu",
                      "tool_id": "csu-precalculus-program",
                      "platform": "canvas.instructure.com",
                      "privacy_level": "public",
                      "settings": {
                        "text": "Launch The CSU Precalculus Program",
                        "labels": {
                          "en": "Launch The CSU Precalculus Program"
                        },
                        "icon_url": "https://coursedev.math.colostate.edu/images/icon.png",
                        "selection_height": 800,
                        "selection_width": 800,
                        "placements": [
                          {
                            "text": "User Navigation Placement",
                            "icon_url": "https://coursedev.math.colostate.edu/lti/my_dashboard.png",
                            "placement": "user_navigation",
                            "message_type": "LtiResourceLinkRequest",
                            "target_link_uri": "https://coursedev.math.colostate.edu/lti/my_dashboard",
                            "canvas_icon_class": "icon-lti",
                            "custom_fields": {
                              "foo": "$Canvas.user.id"
                            }
                          },
                          {
                            "text": "Editor Button Placement",
                            "icon_url": "https://coursedev.math.colostate.edu/lti/editor_tool.png",
                            "placement": "editor_button",
                            "message_type": "LtiDeepLinkingRequest",
                            "target_link_uri": "https://coursedev.math.colostate.edu/lti/content_selector",
                            "selection_height": 500,
                            "selection_width": 500
                          },
                          {
                            "text": "Course Navigation Placement",
                            "icon_url": "https://coursedev.math.colostate.edu/lti/131630-200.png",
                            "placement": "course_navigation",
                            "message_type": "LtiResourceLinkRequest",
                            "target_link_uri": "https://coursedev.math.colostate.edu/lti/launch?placement=course_navigation",
                            "required_permissions": "manage_calendar",
                            "selection_height": 500,
                            "selection_width": 500
                          }
                        ]
                      }
                    }
                  ],
                  "public_jwk": {
                    "kty": "RSA",
                    "alg": "RS256",
                    "e": "AQAB",
                    "kid": "8f796169-0ac4-48a3-a202-fa4f3d814fcd",
                    "n": "nZD7QWmIwj-3N_RZ1qJjX6CdibU87y2l02yMay4KunambalP9g0fU9yZLwLX9WYJINcXZDUf6QeZ-SSbblET-h8Q4OvfSQ7iuu0WqcvBGy8M0qoZ7I-NiChw8dyybMJHgpiP_AyxpCQnp3bQ6829kb3fopbb4cAkOilwVRBYPhRLboXma0cwcllJHPLvMp1oGa7Ad8osmmJhXhM9qdFFASg_OCQdPnYVzp8gOFeOGwlXfSFEgt5vgeU25E-ycUOREcnP7BnMUk7wpwYqlE537LWGOV5z_1Dqcqc9LmN-z4HmNV7b23QZW4_mzKIOY4IqjmnUGgLU9ycFj5YGDCts7Q",
                    "use": "sig"
                  },
                  "custom_fields": {
                    "bar": "$Canvas.user.sisid"
                  }
                }
                """);

        AbstractSite.sendReply(req, resp, Page.MIME_APP_JSON, htm);
    }

    /**
     * Responds to a GET of "launch.html".
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doGetLaunch(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        Log.info("GET request to LTI launch URL:");

        final Enumeration<String> e1 = req.getParameterNames();
        while (e1.hasMoreElements()) {
            final String name = e1.nextElement();
            Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

//    /**
//     * Responds to a GET of "editor_button.html".
//     *
//     * @param req  the request
//     * @param resp the response
//     * @throws IOException if there is an error writing the response
//     */
//    static void doPostEditorButton(final ServletRequest req, final HttpServletResponse resp)
//            throws IOException {
//
//        Log.info("POST request to editor_button.html");
//
//        final Enumeration<String> e1 = req.getParameterNames();
//        while (e1.hasMoreElements()) {
//            final String name = e1.nextElement();
//            Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
//        }
//
//        final String action = req.getParameter("content_item_return_url");
//
//        final String endp = "https://coursedev.math.colostate.edu/lti/endp.html";
//        final String url = "https://coursedev.math.colostate.edu/lti/index.html?exam=171UE";
//        // $NON-NLS-1$
//
//        // final String redir =
//        // action + "?return_type=iframe&url=" + URLEncoder.encode(url) + "&width=600&height=600";
//
//        try {
//            final String redir = action + "?return_type=oembed&endpoint="
//                    + URLEncoder.encode(endp, StandardCharsets.UTF_8)
//                    + "&url=" + URLEncoder.encode(url, StandardCharsets.UTF_8);
//
//            Log.info("Reddirect is ", redir);
//
//            resp.sendRedirect(redir);
//        } catch (final UnsupportedEncodingException ex) {
//            Log.warning(ex);
//        }
//    }

    /**
     * Responds to a Get to "endp.html".
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doGetEndpoint(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        Log.info("GET request to endp.html");

        final Enumeration<String> e1 = req.getParameterNames();
        while (e1.hasMoreElements()) {
            final String name = e1.nextElement();
            Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
        }

        final String url = req.getParameter("url");

        final HtmlBuilder json = new HtmlBuilder(200);

        json.addln("{");
        json.addln("    \"type\":    \"rich\",");
        json.addln("    \"version\": \"1.0\",");
        json.addln("    \"html\":    \"<div style='width:100%; height:620px;'>",
                "<iframe style='height:100%; width:100%; border:0;' src='", url, "'></iframe></div>\",");
        json.addln("    \"width\":   600,");
        json.addln("    \"height\":  600");
        json.addln(" }");

        AbstractSite.sendReply(req, resp, Page.MIME_APP_JSON, json);
    }

//    /**
//     * Responds to a POST to "tool_configuration.html".
//     *
//     * @param req  the request
//     * @param resp the response
//     * @throws IOException if there is an error writing the response
//     */
//    static void doPostToolConfiguration(final HttpServletRequest req,
//                                        final HttpServletResponse resp) throws IOException {
//
//        final HtmlBuilder htm = new HtmlBuilder(2000);
//        Page.startPage(htm, "Tool Configuration", false, false);
//        htm.addln("<body style='background:white'>");
//
//        final Map<String, List<String>> params = new HashMap<>(40);
//        final EOAuthRequestVerifyResult verifyResult = OAuth1.verifyRequest(req, params);
//
//        if (verifyResult == EOAuthRequestVerifyResult.VERIFIED) {
//
//            htm.sH(2).add("CSU Math Refresher Canvas Integration").eH(2);
//
//            htm.sP().add("Under construction...").eP();
//        } else {
//            htm.sP().add("Request was not verified: ", verifyResult.name()).eP();
//        }
//
//        htm.addln("</body>");
//        htm.addln("</html>");
//
//        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
//    }

//    /**
//     * Responds to a POST to "launch.html".
//     *
//     * @param req  the request
//     * @param resp the response
//     * @throws IOException if there is an error writing the response
//     */
//    static void doPostLaunch(final ServletRequest req, final HttpServletResponse resp) throws IOException {
//
//        Log.info("POST request to LTI launch URL:");
//
//        final Enumeration<String> e1 = req.getParameterNames();
//        while (e1.hasMoreElements()) {
//            final String name = e1.nextElement();
//            Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
//        }
//
//        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
//    }
}
