package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
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
     * @throws IOException if there is an error writing the response
     */
    public static void doTarget(final Cache cache, final LtiSite site, final HttpServletRequest req,
                                final HttpServletResponse resp) throws IOException {

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
                PageLinkSelection.showPage(req, resp, redirect);
            } else if ("assignment_edit".equals(placement)) {
                PageAssignmentEdit.showPage(req, resp, redirect);
            } else if ("assignment_group_menu".equals(placement)) {
                PageAssignmentGroupMenu.showPage(req, resp, redirect);
            } else if ("assignment_index_menu".equals(placement)) {
                PageAssignmentIndexMenu.showPage(req, resp, redirect);
            } else if ("assignment_menu".equals(placement)) {
                PageAssignmentMenu.showPage(req, resp, redirect);
            } else if ("assignment_selection".equals(placement)) {
                PageAssignmentSelection.showPage(req, resp, redirect);
            } else if ("assignment_view".equals(placement)) {
                PageAssignmentView.showPage(req, resp, redirect);
            } else if ("collaboration".equals(placement)) {
                PageCollaboration.showPage(req, resp, redirect);
            } else if ("conference_selection.".equals(placement)) {
                PageConferenceSelection.showPage(req, resp, redirect);
            } else if ("course_assignments_menu".equals(placement)) {
                PageCourseAssignmentsMenu.showPage(req, resp, redirect);
            } else if ("course_home_sub_navigation".equals(placement)) {
                PageCourseHomeSubNavigation.showPage(req, resp, redirect);
            } else if ("course_navigation".equals(placement)) {
                PageCourseNavigation.showPage(req, resp, redirect);
            } else if ("course_settings_sub_navigation".equals(placement)) {
                PageCourseSettingsSubNavigation.showPage(req, resp, redirect);
            } else if ("discussion_topic_index_menu".equals(placement)) {
                PageDiscussionTopicIndexMenu.showPage(req, resp, redirect);
            } else if ("discussion_topic_menu".equals(placement)) {
                PageDiscussionTopicMenu.showPage(req, resp, redirect);
            } else if ("editor_button".equals(placement)) {
                PageEditorButton.showPage(req, resp, redirect);
            } else if ("file_index_menu".equals(placement)) {
                PageFileIndexMenu.showPage(req, resp, redirect);
            } else if ("file_menu".equals(placement)) {
                PageFileMenu.showPage(req, resp, redirect);
            } else if ("CSU Homework Submission.".equals(placement)) {
                PageHomeworkSubmission.showPage(req, resp, redirect);
            } else if ("migration_selection".equals(placement)) {
                PageMigrationSelection.showPage(req, resp, redirect);
            } else if ("module_group_menu".equals(placement)) {
                PageModuleGroupMenu.showPage(req, resp, redirect);
            } else if ("module_index_menu".equals(placement)) {
                PageModuleIndexMenu.showPage(req, resp, redirect);
            } else if ("module_index_menu_modal".equals(placement)) {
                PageModuleIndexMenuModal.showPage(req, resp, redirect);
            } else if ("module_menu_modal".equals(placement)) {
                PageModuleMenuModal.showPage(req, resp, redirect);
            } else if ("module_menu".equals(placement)) {
                PageModuleMenu.showPage(req, resp, redirect);
            } else if ("post_grades".equals(placement)) {
                PagePostGrades.showPage(req, resp, redirect);
            } else if ("quiz_index_menu".equals(placement)) {
                PageQuizIndexMenu.showPage(req, resp, redirect);
            } else if ("quiz_menu".equals(placement)) {
                PageQuizMenu.showPage(req, resp, redirect);
            } else if ("student_context_card".equals(placement)) {
                PageStudentContextCard.showPage(req, resp, redirect);
            } else if ("tool_configuration".equals(placement)) {
                PageToolConfiguration.showPage(req, resp, redirect);
            } else if ("user_navigation".equals(placement)) {
                PageUserNavigation.showPage(req, resp, redirect);
            } else if ("wiki_index_menu".equals(placement)) {
                PageWikiIndexMenu.showPage(req, resp, redirect);
            } else if ("wiki_page_menu".equals(placement)) {
                PageWikiPageMenu.showPage(req, resp, redirect);
            } else if ("ContentArea".equals(placement)) {
                PageContentArea.showPage(req, resp, redirect);
            } else {
                Log.warning("Unrecognized placement: '", placement, "'");
                showDefault(payload, req, resp, null);
            }
        }
    }

    /**
     * A handler for GET requests through LTI to a particular target.
     *
     * @param payload the verified payload
     * @param req     the request
     * @param resp    the response
     * @param title   The title for the page; null if none
     * @throws IOException if there is an error writing the response
     */
    public static void showDefault(final JSONObject payload, final ServletRequest req,
                                   final HttpServletResponse resp, final String title) throws IOException {

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

        if (title == null) {
            htm.sH(1).add("CSU Mathematics Program").eH(1);
            htm.sH(2).add("LTI Target").eH(2);
        } else {
            htm.sH(1).add(title).eH(1);
        }

        htm.sDiv("indent");

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
