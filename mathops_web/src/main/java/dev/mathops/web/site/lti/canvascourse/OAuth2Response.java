package dev.mathops.web.site.lti.canvascourse;

import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.lti.LtiPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A handler for redirects from the OAuth2 process to authenticate through Canvas and obtain an access token.
 */
enum OAuth2Response {
    ;

    /**
     * Responds to a GET of "oauth2response.html".
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doGet(final ServletRequest req, final ServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        LtiPage.startPage(htm, "CSU Math Course Manager");

        htm.sH(2).add("CSU Math Course Manager").eH(2);

        final String code = req.getParameter("code");
        Log.info("  GET request to LTI OAuth2 Response URL with code=", code);

        if (code == null) {
            final String error = req.getParameter("error");

            if (error == null) {
                // No code and no error - must be access token response
            } else {
                final String errorUri = req.getParameter("error_uri");

                htm.sP();
                if (errorUri != null) {
                    htm.add("<a href='", errorUri, "'>");
                }

                switch (error) {
                    case "invalid_request" -> htm.addln("Invalid Request");
                    case "unauthorized_client" -> htm.addln("Unauthorized Client");
                    case "access_denied" -> htm.addln("Access Denied");
                    case "unsupported_response_type" -> htm.addln("Unsupported Response Type");
                    case "invalid_scope" -> htm.addln("Invalid Scope");
                    case "server_error" -> htm.addln("Server Error");
                    case "temporarily_unavailable" -> htm.addln("Temporarily Unavailable");
                    default -> htm.addln(error);
                }

                if (errorUri != null) {
                    htm.add("</a>");
                }
                htm.eP();

                final String desc = req.getParameter("error_description");
                if (desc != null) {
                    htm.sP().addln(desc).eP();
                }
            }
        } else {
            htm.addln("<script>");
            htm.addln("  const xhr = new XMLHttpRequest();");
            htm.addln("  xhr.open('POST', 'https://",
                    CanvasCourseSite.LMS_HOST, "/login/oauth2/token', true);");
            htm.addln("  xhr.setRequestHeader('Content-Type', ",
                    "'application/x-www-form-urlencoded');");

            htm.addln("  xhr.onreadystatechange = function() {");
            htm.addln("    if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {");
            htm.addln("      alert('Hello');");
            htm.addln("    }");
            htm.addln("  }");
            htm.addln("  xhr.send('grant_type=authorization_code",
                    "&client_id=", CanvasCourseSite.CLIENT_ID,
                    "&client_secret=", CanvasCourseSite.DEV_KEY,
                    "&redirect_uri=https://", CanvasCourseSite.HOST,
                    "/csu_math_course_mgr/oauth2response.html",
                    "&code=", code, "');");
            htm.addln("</script>");

            // htm.addln("<form method='post' action='https://",
            // CanvasCourseSite.LMSHOST, "/login/oauth2/token'>");
            // htm.addln("<input type='hidden' name='grant_type' ",
            // "value='authorization_code'/>");
            // htm.addln("<input type='hidden' name='client_id' value='",
            // CanvasCourseSite.CLIENT_ID, "'/>");
            // htm.addln("<input type='hidden' name='client_secret' value='",
            // CanvasCourseSite.DEV_KEY, "'/>");
            // htm.addln("<input type='hidden' name='redirect_uri' value='https://",
            // CanvasCourseSite.HOST, "/csu_math_course_mgr/oauth2response.html'>");
            // htm.addln("<input type='hidden' name='code' value='", code,
            // "'/>");
            // htm.addln("<input type='submit' value='Acquire access token'/>");
            // htm.addln("</form>");
        }

        final Map<String, List<String>> params = new HashMap<>(40);

        // TODO: Gather params

        htm.addln("<ul>");
        for (final Map.Entry<String, List<String>> entry : params.entrySet()) {
            for (final String value : entry.getValue()) {

                htm.addln("<li>", entry.getKey(), "=", value, "</li>");
            }
        }
        htm.addln("</ul>");

        LtiPage.endPage(htm);
        LtiPage.sendReply(req, resp, LtiPage.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}
