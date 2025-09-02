package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.db.Cache;
import dev.mathops.db.schema.main.rec.LtiRegistrationRec;
import dev.mathops.db.schema.term.impl.LtiContextCourseSectionLogic;
import dev.mathops.db.schema.term.impl.LtiContextLogic;
import dev.mathops.db.schema.term.rec.LtiContextCourseSectionRec;
import dev.mathops.db.schema.term.rec.LtiContextRec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.web.host.course.lti.LtiSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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
     * @return the {@code LtiContextData}; null if the LMS course has not yet been connected to an institution course
     *         section
     * @throws SQLException if there is an error accessing the database
     */
    static LtiContextData lookupLtiContext(final Cache cache, final LtiSite.PendingTargetRedirect redirect)
            throws SQLException {

        final JSONObject payload = redirect.idTokenPayload();

        final LtiRegistrationRec registration = redirect.registration();
        final String deployment = payload.getStringProperty("https://purl.imsglobal.org/spec/lti/claim/deployment_id");
        String contextId = null;
        final Object context = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/context");
        if (context instanceof final JSONObject contextObj) {
            contextId = contextObj.getStringProperty("id");
        }

        LtiContextData result = null;

        if (deployment != null && contextId != null) {
            final LtiContextRec ctx = LtiContextLogic.INSTANCE.query(cache, registration.clientId, registration.issuer,
                    deployment, contextId);
            if (ctx != null) {
                final List<LtiContextCourseSectionRec> sects = LtiContextCourseSectionLogic.INSTANCE.queryForContext(
                        cache, registration.clientId, registration.issuer, deployment, contextId);
                result = new LtiContextData(ctx, sects);
            }
        }

        return result;
    }

    /**
     * A container for data associated with an LTI context.
     *
     * @param context        the context
     * @param courseSections the set of course/sections associated with the context
     * @return
     */
    public record LtiContextData(LtiContextRec context, List<LtiContextCourseSectionRec> courseSections) {
    }

    /**
     * Shows the page that indicates this course has not yet been configured with the LTI tool.
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
                .addln(" <link rel='stylesheet' href='ltistyle.css' type='text/css'>")
                .addln(" <title>", LtiSite.TOOL_NAME, "</title>");
        htm.addln("</head>");
        htm.addln("<body style='background:white; padding:20px;'>");

        htm.sH(1).add(LtiSite.TOOL_NAME).eH(1);

        htm.sDiv("indent");
        htm.sP().addln("This course has not yet been linked to the ", LtiSite.TOOL_NAME, " tool.").eP();
        htm.sP().addln("Please to into the <strong>[Settings]</strong> menu for the course, and select ",
                "<strong>[", LtiSite.TOOL_NAME, "]</strong> from the menu on the right-hand side to ",
                "link this course.").eP();
        htm.eDiv();

        htm.addln("</body></html>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
