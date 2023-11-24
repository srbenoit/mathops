package dev.mathops.web.site.lti.canvascourse;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.web.site.lti.EOAuthRequestVerifyResult;
import dev.mathops.web.site.lti.LtiPage;

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main configuration page for the "CSU Math Course Manager" system - accessed through a button in the upper right
 * of the Instructor's Canvas page for the course.
 *
 * <p>
 * This page extracts the user's name, CSU ID, and Canvas course identifier from the request, then provides the
 * configuration page for that course (or a "start configuring" page if the course has never been configured).
 */
public enum PageCourseAdmin {
    ;

    /**
     * Responds to a POST to "course_admin.html".
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    public static void doPost(final HttpServletRequest req, final ServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        LtiPage.startPage(htm, "CSU Math Course Manager");

        final Map<String, List<String>> params = new HashMap<>(40);
        final EOAuthRequestVerifyResult verifyResult = OAuth2.verifyRequest(req, params);

        if (verifyResult == EOAuthRequestVerifyResult.VERIFIED) {

            htm.sH(2).add("CSU Math Course Manager").eH(2);

            htm.addln("<form method='get' action='https://", CanvasCourseSite.LMS_HOST, "/login/oauth2/auth'>");
            htm.addln("<input type='hidden' name='client_id' value='", CanvasCourseSite.CLIENT_ID, "'/>");
            htm.addln("<input type='hidden' name='response_type' value='code'/>");
            htm.addln("<input type='hidden' name='redirect_uri' value='https://",
                    CanvasCourseSite.HOST, "/csu_math_course_mgr/oauth2response.html'/>");

            htm.addln("<input type='submit' value='Connect Tool to Canvas'/>");

            htm.addln("</form>");
        } else {
            htm.sP().add("Request was not verified: ", verifyResult.name()).eP();
        }

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
