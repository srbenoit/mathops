package dev.mathops.web.site.lti.canvascourse;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
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
 * The main configuration page for the "Math Refresher" system - accessed through a button in the upper right of the
 * Instructor's Canvas page for the course.
 *
 * <p>
 * This page extracts the user's name, CSU ID, and Canvas course identifier from the request, then provides the
 * configuration page for that course (or a "start configuring" page if the course has never been configured).
 */
public enum PageCourseConfig {
    ;

    /**
     * Called when the "Configure Math Refresher" link in the upper-right side of the instructor's Canvas page is
     * clicked.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    public static void doPost(final HttpServletRequest req, final ServletResponse resp) throws IOException {

        Log.info("POST request to 'Configure Math Refresher' :");

        final HtmlBuilder htm = new HtmlBuilder(2000);
        LtiPage.startPage(htm, "CSU Math Course Manager");

        final Map<String, List<String>> params = new HashMap<>(40);
        final EOAuthRequestVerifyResult verifyResult = OAuth1.verifyRequest(req, params);

        if (verifyResult == EOAuthRequestVerifyResult.VERIFIED) {
            String username = null;
            String roles = null;
            String course = null;
            String email = null;
            String userId = null;

            for (final Map.Entry<String, List<String>> e1 : params.entrySet()) {
                if (e1.getValue().size() == 1) {
                    final String name = e1.getKey();
                    final String value = e1.getValue().get(0);

                    if ("lis_person_name_full".equals(name)) {
                        username = value;
                    } else if ("ext_roles".equals(name)) {
                        roles = value;
                    } else if ("context_label".equals(name)) {
                        course = value;
                    } else if ("lis_person_contact_email_primary".equals(name)) {
                        email = value;
                    } else if ("user_id".equals(name)) {
                        userId = value;
                    }

                    Log.fine("Parameter '", name, "' = '", value, "'");
                }
            }

            if (username == null) {
                htm.sP().add("ERROR: Unable to verify username").eP();
            } else if (userId == null) {
                htm.sP().add("ERROR: Unable to verify User ID number").eP();
            } else if (roles == null) {
                htm.sP().add("ERROR: Unable to verify Instructor role").eP();
            } else if (course == null) {
                htm.sP().add("ERROR: Unable to verify course").eP();
            } else if (roles.contains("Instructor")) {
                emitConfiguePage(htm, username, userId, course, email);
            } else {
                htm.sP().add("Only instructions may access Math Course Manager",
                                EOAuthRequestVerifyResult.VERIFIED.name()).eP();
            }
        } else {
            htm.sP().add("Request was not verified: ", verifyResult.name()).eP();
        }

        LtiPage.endPage(htm);
        LtiPage.sendReply(req, resp, LtiPage.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits the contents of the configuration page.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param username the user Canvas display name (like "John Doe")
     * @param userId   the Canvas user ID for the user accessing the site
     * @param course   the Canvas course identifier
     * @param email    the user's email address
     */
    private static void emitConfiguePage(final HtmlBuilder htm, final String username,
                                         final String userId, final String course, final String email) {

        htm.sH(1).add("Welcome ", username).eH(1);
        htm.sP().add("User ID ", userId).eP();
        htm.sP().add("Course: ", course).eP();
        htm.sP().add("Email: ", email).eP();

        htm.br().sP()
                .add("This page would give instructors assigned to a course the ability ",
                        "to administer or monitor that course's configuration within our system, but ",
                        "it is not clear how this page can connect to the Canvas API.").eP();

        htm.addln("<form method='get' action='https://", CanvasCourseSite.LMS_HOST, "/login/oauth2/auth'>");
        htm.addln("<input type='hidden' name='client_id' value='", CanvasCourseSite.CLIENT_ID, "'/>");
        htm.addln("<input type='hidden' name='response_type' value='code'/>");
        htm.addln("<input type='hidden' name='redirect_uri' value='https://",
                CanvasCourseSite.HOST, "/csu_math_course_mgr/oauth2response.html'/>");

        htm.addln("<input type='submit' value='Connect Tool to Canvas'/>");

        htm.addln("</form>");
    }
}
