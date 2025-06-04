package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.xml.XmlEscaper;
import dev.mathops.web.host.course.lti.LtiSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
     * @param cache    the data cache
     * @param req      the request
     * @param resp     the response
     * @param redirect the redirect
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                      final LtiSite.PendingTargetRedirect redirect) throws IOException, SQLException {

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
                .addln(" <title>", LtiSite.TOOL_NAME, "</title>");
        htm.addln("</head>");
        htm.addln("<body style='background:white; padding:20px;'>");

        htm.sH(1).add(LtiSite.TOOL_NAME).eH(1);

        if (isInstructor || isAdmin) {

            // TODO: See if the course is already configured.  If so, this page should present settings rather than
            //  the option to link the course

            emitLinkCourseForm(cache, htm, returnUrl);
        } else {
            // A user with only student permission accessed this page.  This should not be possible, but...
            htm.sP().add("Configuration settings for ", LtiSite.TOOL_NAME,
                    " are available only to instructors and administrators.").eP();

            htm.div("vgap");
            htm.addln("<a class='btn' href='", XmlEscaper.escape(returnUrl), "'>Close</a>");
        }

        htm.addln("</body></html>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Processes a POST from this page.
     *
     * @param cache the data cache
     * @param req   the request
     * @param resp  the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final ServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        PageError.showErrorPage(req, resp, "Link Course", "POST not yet implemented...");
    }

    /**
     * Emits the form to link the course to an institutional course and section.
     *
     * @param cache     the data cache
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param returnUrl the return URL
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitLinkCourseForm(final Cache cache, final HtmlBuilder htm, final String returnUrl)
            throws SQLException {

        htm.sH(2).add("Link Course").eH(2);
        htm.hr().sDiv("indent");

        htm.sP().add("Before this LTI tool can provide course content and assignments within Canvas, it must be ",
                "linked to one or more institutional course sections.").eP();

        htm.sP().add("Please select the institutional course section(s) to link to this Canvas course.").eP();
        htm.sP().add("If this Canvas course includes multiple cross-listed section, select all of the ",
                "corresponding institutional sections.").eP();
        htm.div("vgap");

        final List<RawCsection> sections = getCourseSections(cache, htm);

        if (!sections.isEmpty()) {
            htm.addln("<form class='indent' method='POST'>");
            htm.add("<input type='hidden' id='plc' name='plc' value='course_settings_sub_navigation'/>");
            String priorCourse = "foo";
            final int size = sections.size();
            for (int i = 0; i < size; ++i) {
                final RawCsection sect = sections.get(i);
                final String key = sect.course + "_" + sect.sect;
                final String dispCourse = sect.course.replace("M ", "MATH ");
                if (i == size - 1) {
                    if (priorCourse.equals(sect.course)) {
                        htm.sP(null, "style='border-bottom: solid 1px gray;'");
                    } else {
                        htm.sP(null, "style='border-top: solid 1px gray; border-bottom: solid 1px gray;'");
                    }
                } else {
                    if (priorCourse.equals(sect.course)) {
                        htm.sP();
                    } else {
                        htm.sP(null, "style='border-top: solid 1px gray;'");
                    }
                }
                priorCourse = sect.course;
                htm.add("<input type='checkbox' id='", key, "' name='", key, "'/>");
                htm.addln(" <label for='", key, "'>", dispCourse, " section ", sect.sect, "</label>");
                htm.eP();
            }

            htm.div("vgap");
            htm.addln("<input class='btn' type='submit' value='Submit'/> &nbsp; ");
            htm.addln("<a class='btn' href='", XmlEscaper.escape(returnUrl), "'>Cancel</a>");
            htm.addln("</form>");

            htm.eDiv(); // indent
        }
    }

    /**
     * Gathers the list of course/section records that are applicable to this tool.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append error messages
     * @return the list of applicable sections
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawCsection> getCourseSections(final Cache cache, final HtmlBuilder htm) throws SQLException {

        final SystemData systemData = cache.getSystemData();
        final TermRec active = systemData.getActiveTerm();

        List<RawCsection> result = null;

        if (active == null) {
            htm.sP("error").add("Error: Unable to determine the active term.").eP();
        } else {
            final List<RawCsection> sections = RawCsectionLogic.queryByTerm(cache, active.term);

            if (sections.isEmpty()) {
                htm.sP("error").add("Error: No institutional sections found.").eP();
            } else {
                result = new ArrayList<>(sections.size());
                for (final RawCsection sect : sections) {
                    final String crs = sect.course;
                    if ("M 115".equals(crs) || "MATH 115".equals(crs)
                        || "M 116".equals(crs) || "MATH 116".equals(crs)
                        || "M 117".equals(crs) || "MATH 117".equals(crs)
                        || "M 118".equals(crs) || "MATH 118".equals(crs)
                        || "M 124".equals(crs) || "MATH 124".equals(crs)
                        || "M 125".equals(crs) || "MATH 125".equals(crs)
                        || "M 126".equals(crs) || "MATH 126".equals(crs)) {

                        // Omit placement credit sections
                        if (!"OT".equals(sect.instrnType)) {
                            result.add(sect);
                        }
                    }
                }
                result.sort(null);
            }
        }

        return result;
    }
}
