package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.schema.term.rec.LtiContextCourseSectionRec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.xml.XmlEscaper;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.course.lti.LtiSite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * A dispatcher class that examines the payload of a validate launch callback to determine the page to generate, then
 * dispatches the request to the appropriate page generator.
 */
public enum LTITarget {
    ;

    /**
     * A handler for GET requests through LTI to a particular target.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final LtiSite site, final HttpServletRequest req,
                             final HttpServletResponse resp) throws IOException, SQLException {

        final String nonce = req.getParameter("nonce");

        final LtiSite.PendingTargetRedirect redirect = site.getRedirect(nonce);
        if (redirect == null) {
            PageError.showErrorPage(req, resp, "LTI Resource Launch",
                    "The launch is invalid or has timed out.  Please try again.");
        } else {
            final JSONObject payload = redirect.idTokenPayload();

            final String placement = payload.getStringProperty("https://www.instructure.com/placement");
            Log.info("LTI Target placement is ", placement);

            if (placement == null || "link_selection".equals(placement)) {
                // The "Link Selection" placement sends a null placement value.
                PageLinkSelection.showPage(cache, req, resp, redirect);
            } else if ("assignment_edit".equals(placement)) {
                PageAssignmentEdit.showPage(cache, req, resp, redirect);
            } else if ("assignment_group_menu".equals(placement)) {
                PageAssignmentGroupMenu.showPage(cache, req, resp, redirect);
            } else if ("assignment_index_menu".equals(placement)) {
                PageAssignmentIndexMenu.showPage(cache, req, resp, redirect);
            } else if ("assignment_menu".equals(placement)) {
                PageAssignmentMenu.showPage(cache, req, resp, redirect);
            } else if ("assignment_selection".equals(placement)) {
                PageAssignmentSelection.showPage(cache, req, resp, redirect);
            } else if ("assignment_view".equals(placement)) {
                PageAssignmentView.showPage(cache, req, resp, redirect);
            } else if ("collaboration".equals(placement)) {
                PageCollaboration.showPage(cache, req, resp, redirect);
            } else if ("conference_selection.".equals(placement)) {
                PageConferenceSelection.showPage(cache, req, resp, redirect);
            } else if ("course_assignments_menu".equals(placement)) {
                PageCourseAssignmentsMenu.showPage(cache, req, resp, redirect);
            } else if ("course_home_sub_navigation".equals(placement)) {
                PageCourseHomeSubNavigation.showPage(cache, req, resp, redirect);
            } else if ("course_navigation".equals(placement)) {
                PageCourseNavigation.showPage(cache, req, resp, redirect);
            } else if ("course_settings_sub_navigation".equals(placement)) {
                PageCourseSettingsSubNavigation.doGet(cache, req, resp, redirect);
            } else if ("discussion_topic_index_menu".equals(placement)) {
                PageDiscussionTopicIndexMenu.showPage(cache, req, resp, redirect);
            } else if ("discussion_topic_menu".equals(placement)) {
                PageDiscussionTopicMenu.showPage(cache, req, resp, redirect);
            } else if ("editor_button".equals(placement)) {
                PageEditorButton.showPage(cache, req, resp, redirect);
            } else if ("file_index_menu".equals(placement)) {
                PageFileIndexMenu.showPage(cache, req, resp, redirect);
            } else if ("file_menu".equals(placement)) {
                PageFileMenu.showPage(cache, req, resp, redirect);
            } else if ("homework_submission".equals(placement)) {
                PageHomeworkSubmission.showPage(cache, req, resp, redirect);
            } else if ("migration_selection".equals(placement)) {
                PageMigrationSelection.showPage(cache, req, resp, redirect);
            } else if ("module_group_menu".equals(placement)) {
                PageModuleGroupMenu.showPage(cache, req, resp, redirect);
            } else if ("module_index_menu".equals(placement)) {
                PageModuleIndexMenu.showPage(cache, req, resp, redirect);
            } else if ("module_index_menu_modal".equals(placement)) {
                PageModuleIndexMenuModal.showPage(cache, req, resp, redirect);
            } else if ("module_menu_modal".equals(placement)) {
                PageModuleMenuModal.showPage(cache, req, resp, redirect);
            } else if ("module_menu".equals(placement)) {
                PageModuleMenu.showPage(cache, req, resp, redirect);
            } else if ("post_grades".equals(placement)) {
                PagePostGrades.showPage(cache, req, resp, redirect);
            } else if ("quiz_index_menu".equals(placement)) {
                PageQuizIndexMenu.showPage(cache, req, resp, redirect);
            } else if ("quiz_menu".equals(placement)) {
                PageQuizMenu.showPage(cache, req, resp, redirect);
            } else if ("student_context_card".equals(placement)) {
                PageStudentContextCard.showPage(cache, req, resp, redirect);
            } else if ("tool_configuration".equals(placement)) {
                PageToolConfiguration.showPage(cache, req, resp, redirect);
            } else if ("user_navigation".equals(placement)) {
                PageUserNavigation.showPage(cache, req, resp, redirect);
            } else if ("wiki_index_menu".equals(placement)) {
                PageWikiIndexMenu.showPage(cache, req, resp, redirect);
            } else if ("wiki_page_menu".equals(placement)) {
                PageWikiPageMenu.showPage(cache, req, resp, redirect);
            } else if ("ContentArea".equals(placement)) {
                PageContentArea.showPage(cache, req, resp, redirect);
            } else {
                final PageUtils.LtiContextData ltiData = PageUtils.lookupLtiContext(cache, redirect);

                if (ltiData == null) {
                    PageUtils.showCourseNotConfigured(req, resp);
                } else {
                    Log.warning("Unrecognized placement: '", placement, "'");
                    showDefault(ltiData, payload, req, resp, null);
                }
            }
        }
    }

    /**
     * A handler for POST requests through LTI to a particular target.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doPost(final Cache cache, final LtiSite site, final HttpServletRequest req,
                              final HttpServletResponse resp) throws IOException, SQLException {

        final Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            final String name = paramNames.nextElement();
            final String value = req.getParameter(name);
            Log.info("Target param '", name, "' = ", value);
        }

        final String placement = req.getParameter("plc");

        Log.info("LTI Target POST placement is ", placement);

        if ("course_settings_sub_navigation".equals(placement)) {
            PageCourseSettingsSubNavigation.doPost(cache, req, resp);
        } else {
            Log.warning("Unrecognized placement for POST: '", placement, "'");
            PageError.showErrorPage(req, resp, "Invalid POST access to ", LtiSite.TOOL_NAME);
        }
    }

    /**
     * A handler for GET requests through LTI to a particular target.
     *
     * @param ltiData the LTI data
     * @param payload the verified payload
     * @param req     the request
     * @param resp    the response
     * @param title   The title for the page; null if none
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void showDefault(final PageUtils.LtiContextData ltiData, final JSONObject payload,
                                   final ServletRequest req, final HttpServletResponse resp, final String title)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        htm.addln("<!DOCTYPE html>").addln("<html>").addln("<head>");
        htm.addln(" <meta name=\"robots\" content=\"noindex\">");
        htm.addln(" <meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>")
                .addln(" <meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>")
                .addln(" <link rel='stylesheet' href='ltistyle.css' type='text/css'>")
                .addln(" <title>", LtiSite.TOOL_NAME, "</title>");
        htm.addln("</head>");
        htm.addln("<body style='background:white; padding:20px;'>");

        if (title == null) {
            htm.sH(1).add(LtiSite.TOOL_NAME).eH(1);
            htm.sH(2).add("LTI Target").eH(2);
        } else {
            htm.sH(1).add(title).eH(1);
        }

        htm.sDiv("indent");

        if (ltiData != null) {
            htm.sP().add("This course is linked to the following institutional course sections:").eP();
            htm.sP("indent");
            for (final LtiContextCourseSectionRec rec : ltiData.courseSections()) {
                htm.addln(rec.courseId, " section ", rec.sectionNbr).br();
            }
            htm.eP();
            htm.hr();
        }

        htm.sP().addln("<pre>");
        htm.add(payload.toJSONFriendly(0));
        htm.addln("</pre>").eP();

        final Object pres = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/launch_presentation");
        if (pres instanceof final JSONObject presObject) {
            final String returnUrl = presObject.getStringProperty("return_url");
            if (returnUrl == null) {
                Log.warning("Unable to find 'return_url' in 'launch_presentation' of ID target payload.");
            } else {
                htm.div("vgap");
                htm.addln("<a href='", XmlEscaper.escape(returnUrl), "'>Finished</a>");
            }
        } else {
            Log.warning("Unable to find 'launch_presentation' in ID target payload.");
        }

        htm.eDiv(); // indent

        htm.addln("</body></html>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
