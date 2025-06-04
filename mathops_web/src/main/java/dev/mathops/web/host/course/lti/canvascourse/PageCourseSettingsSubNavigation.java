package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.xml.XmlEscaper;
import dev.mathops.web.host.course.lti.LtiSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * A page with the "course_settings_sub_navigation" placement.
 *
 * <p>
 * This page allows the teacher to associate a CSU course/section with this Canvas course and configure this tool with
 * respect to that section.  This must be done before other configurations can be done.
 * </p>
 */
enum PageCourseSettingsSubNavigation {
    ;

    /**
     * Shows the page.
     *
     * @param req      the request
     * @param resp     the response
     * @param redirect the redirect
     */
    static void showPage(final ServletRequest req, final HttpServletResponse resp,
                         final LtiSite.PendingTargetRedirect redirect) throws IOException {

        final JSONObject payload = redirect.idTokenPayload();

        final String locale = payload.getStringProperty("locale");
        final String deployment = payload.getStringProperty("https://purl.imsglobal.org/spec/lti/claim/deployment_id");
        String returnUrl = null;
        String contextId = null;
        String contextTitle = null;
        String canvasCourseId = null;
        boolean isAdmin = false;
        boolean isInstructor = false;

        final Object pres = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/launch_presentation");
        if (pres instanceof final JSONObject presObject) {
            returnUrl = presObject.getStringProperty("return_url");
        }

        final Object context = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/context");
        if (context instanceof final JSONObject contextObj) {
            contextId = contextObj.getStringProperty("id");
            contextTitle = contextObj.getStringProperty("title");
            if (contextTitle == null || contextTitle.isBlank()) {
                contextTitle = contextObj.getStringProperty("label");
            }
        }

        final Object roles = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/roles");
        if (roles instanceof final Object[] rolesArray) {
            for (final Object role : rolesArray) {
                if ("http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator".equals(role)
                    || "http://purl.imsglobal.org/vocab/lis/v2/system/person#SysAdmin".equals(role)) {
                    isAdmin = true;
                } else if ("http://purl.imsglobal.org/vocab/lis/v2/institution/person#Instructor".equals(role)
                           || "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor".equals(role)) {
                    isInstructor = true;
                }
            }
        }

        final Object custom = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/custom");
        if (custom instanceof final JSONObject customObj) {
            String test = customObj.getStringProperty("canvas_course_id");
            if (!(test == null || test.isBlank() || "$Canvas.course.id".equals(test))) {
                canvasCourseId = test;
            }
        }

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

        htm.sH(1).add("CSU Mathematics Program").eH(1);
        htm.sH(2).add("Connect Canvas Course to CSU Course").eH(2);

        htm.hr().sDiv("indent");

        // TODO: See if the course is already configured.

        htm.sP().add("Before this LTI tool can provide course content and assignments within Canvas, it must be ",
                "linked to a course definition in the CSU system (Banner).").eP();

        htm.sP().add("Information found:").eP();

        htm.addln("<ul>");
        htm.addln("  <li>issuer = ", redirect.registration().issuer, "</li>");
        htm.addln("  <li>client_id = ", redirect.registration().clientId, "</li>");
        htm.addln("  <li>locale = ", locale, "</li>");
        htm.addln("  <li>deployment = ", deployment, "</li>");
        htm.addln("  <li>returnUrl = ", returnUrl, "</li>");
        htm.addln("  <li>contextId = ", contextId, "</li>");
        htm.addln("  <li>contextTitle = ", contextTitle, "</li>");
        htm.addln("  <li>canvasCourseId = ", canvasCourseId, "</li>");
        htm.addln("  <li>Admin role = ", (isAdmin ? "yes" : "no"), "</li>");
        htm.addln("  <li>Instructor role = ", (isInstructor ? "yes" : "no"), "</li>");
        htm.addln("</ul>");

        htm.div("vgap");
        htm.addln("<a href='", XmlEscaper.escape(returnUrl), "'>Finished</a>");

        htm.eDiv(); // indent

        htm.addln("</body></html>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
