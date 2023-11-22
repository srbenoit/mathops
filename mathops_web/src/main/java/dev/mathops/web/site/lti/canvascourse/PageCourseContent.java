package dev.mathops.web.site.lti.canvascourse;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.web.site.lti.EOAuthRequestVerifyResult;
import dev.mathops.web.site.lti.LtiPage;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main student-facing page for the "Math Course Manager" system. This is accessed from a "CSU Math Refresher"
 * button in the main canvas menu.
 */
public enum PageCourseContent {
    ;

    /**
     * Generates the page.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    public static void doGet(final HttpServletRequest req, final ServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        LtiPage.startPage(htm, "CSU Math Course Manager");

        final Map<String, List<String>> params = new HashMap<>(40);
        final EOAuthRequestVerifyResult verifyResult = OAuth1.verifyRequest(req, params);

        if (verifyResult == EOAuthRequestVerifyResult.VERIFIED) {
            htm.sH(2).add("CSU Math Course Manager").eH(2);

            final List<String> courseId = params.get("context_label");
            if (courseId != null && courseId.size() == 1) {
                htm.sP().add("Course ID = ", courseId.get(0)).eP();
            }

            final List<String> givenName = params.get("lis_person_name_given");
            if (givenName != null && givenName.size() == 1) {
                htm.sP().add("Given Name = ", givenName.get(0)).eP();
            }

            final List<String> familyName = params.get("lis_person_name_family");
            if (familyName != null && familyName.size() == 1) {
                htm.sP().add("Family Name = ", familyName.get(0)).eP();
            }

            final List<String> canvasUserId = params.get("custom_canvas_user_id");
            if (canvasUserId != null && canvasUserId.size() == 1) {
                htm.sP().add("Canvas user ID = ", canvasUserId.get(0)).eP();
            }

            final List<String> canvasLoginId = params.get("custom_canvas_user_login_id");
            if (canvasLoginId != null && canvasLoginId.size() == 1) {
                htm.sP().add("Canvas login ID = ", canvasLoginId.get(0)).eP();
            }

            // htm.sP().add("Under construction...").eP();

            // htm.addln("<ul>");
            // for (final Map.Entry<String, List<String>> entry : params.entrySet()) {
            // for (final String value : entry.getValue()) {
            //
            // htm.addln("<li>", entry.getKey(), "=",
            // value, "</li>");
            // }
            // }
            // htm.addln("</ul>");
        } else {
            htm.sP().add("Request was not verified: ", verifyResult.name()).eP();
        }

        LtiPage.endPage(htm);
        LtiPage.sendReply(req, resp, LtiPage.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}
