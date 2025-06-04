package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.db.Cache;
import dev.mathops.db.rec.main.LtiRegistrationRec;
import dev.mathops.db.rec.term.LtiCourseRec;
import dev.mathops.db.reclogic.term.LtiCourseLogic;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.web.host.course.lti.LtiSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Utilities used by LTI tool pages.
 */
enum PageUtils {
    ;

    /**
     * Looks up an {@code LtiCourseRec} based on the information in a {@code PendingTargetRedirect}.
     *
     * @param cache    the data cache
     * @param redirect the redirect information
     * @return the {@code LtiCourseRec}; null if the LMS course has not yet been connected to an institution course
     *         section
     * @throws SQLException if there is an error accessing the database
     */
    static LtiCourseRec lookupLtiCourse(final Cache cache, final LtiSite.PendingTargetRedirect redirect)
            throws SQLException {

        final JSONObject payload = redirect.idTokenPayload();

        final LtiRegistrationRec registration = redirect.registration();
        final String deployment = payload.getStringProperty("https://purl.imsglobal.org/spec/lti/claim/deployment_id");
        String contextId = null;
        final Object context = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/context");
        if (context instanceof final JSONObject contextObj) {
            contextId = contextObj.getStringProperty("id");
        }

        LtiCourseRec result;

        if (deployment == null || contextId == null) {
            result = null;
        } else {
            result = LtiCourseLogic.INSTANCE.query(cache, registration.clientId, registration.issuer,
                    deployment, contextId);
        }

        return result;
    }

    /**
     * Shows the page that indicates this course has not yet been configured with the CSU Mathematics LTI tool.
     *
     * @param req  the request
     * @param resp the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showCourseNotConfigured(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        htm.addln("<!DOCTYPE html>").addln("<html>").addln("<head>");
        htm.addln(" <meta name=\"robots\" content=\"noindex\">");
        htm.addln(" <meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>")
                .addln(" <meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>")
                .addln(" <link rel='stylesheet' href='basestyle.css' type='text/css'>")
                .addln(" <link rel='stylesheet' href='style.css' type='text/css'>")
                .addln(" <title>CSU Mathematics Program</title>");
        htm.addln("</head>");
        htm.addln("<body style='background:white; padding:20px;'>");

        htm.sH(1).add(LtiSite.TOOL_NAME).eH(1);

        htm.sDiv("indent");
        htm.sP().addln("This course has not yet been configured in the CSU Math Tool.").eP();
        htm.sP().addln("Please to into the <strong>[Settings]</strong> menu for the course, and select ",
                "<strong>[Configure CSU Math Tool]</strong> from the menu on the right-hand side to ",
                "configure this course.").eP();
        htm.eDiv();

        htm.addln("</body></html>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
