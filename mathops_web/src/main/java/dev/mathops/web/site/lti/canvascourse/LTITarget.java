package dev.mathops.web.site.lti.canvascourse;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.xml.XmlEscaper;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.lti.LtiSite;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
            if ("assignment_edit".equals(placement)) {
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
            } else if ("homework_submission".equals(placement)) {
                PageHomeworkSubmission.showPage(req, resp, redirect);
            } else if ("link_selection".equals(placement)) {
                PageLinkSelection.showPage(req, resp, redirect);
            } else if ("migration_selection".equals(placement)) {
                PageMigrationSelection.showPage(req, resp, redirect);
            } else if ("module_group_menu".equals(placement)) {
                PageModuleGroupMenu.showPage(req, resp, redirect);
            } else if ("module_index_menu_modal".equals(placement)) {
                PageModuleIndexMenuModal.showPage(req, resp, redirect);
            } else if ("module_index_menu".equals(placement)) {
                PageModuleIndexMenu.showPage(req, resp, redirect);
            } else if ("module_menu_modal".equals(placement)) {
                PageModuleMenuModal.showPage(req, resp, redirect);
            } else if ("module_menu".equals(placement)) {
                PageModuleMenu.showPage(req, resp, redirect);
            } else if ("wiki_index_menu".equals(placement)) {
                PageWikiIndexMenu.showPage(req, resp, redirect);
            } else if ("wiki_page_menu".equals(placement)) {
                PageWikiPageMenu.showPage(req, resp, redirect);
            } else if ("quiz_index_menu".equals(placement)) {
                PageQuizIndexMenu.showPage(req, resp, redirect);
            } else if ("quiz_menu".equals(placement)) {
                PageQuizMenu.showPage(req, resp, redirect);
            } else if ("student_context_card".equals(placement)) {
                PageStudentContextCard.showPage(req, resp, redirect);
            } else if ("submission_type_selection".equals(placement)) {
                PageSubmissionTypeSelection.showPage(req, resp, redirect);
            } else if ("post_grades".equals(placement)) {
                PagePostGrades.showPage(req, resp, redirect);
            } else if ("tool_configuration".equals(placement)) {
                PageToolConfiguration.showPage(req, resp, redirect);
            } else if ("top_navigation".equals(placement)) {
                PageTopNavigation.showPage(req, resp, redirect);
            } else if ("user_navigation".equals(placement)) {
                PageUserNavigation.showPage(req, resp, redirect);
            } else {
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
                htm.sH(2).add("LTI Target").eH(2);

                htm.sDiv("indent");
                htm.sP().addln("<pre>");
                htm.add(payload.toJSONFriendly(0));
                htm.addln("</pre>").eP();
                htm.eDiv();

                final Object pres = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/launch_presentation");
                if (pres instanceof JSONObject presObject) {
                    final String returnUrl = presObject.getStringProperty("return_url");
                    htm.addln("<form action='", XmlEscaper.escape(returnUrl), "' method='GET'>");
                    htm.addln("  <input type='submit' value='Ok.'/>");
                    htm.addln("</form>");
                }

                htm.addln("</body></html>");

                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
            }
        }
    }

    /**
     * Fetches content from a URL.
     *
     * @param urlStr the URL whose content to fetch
     * @param accept the format to accept
     * @return the content; null if fetch failed
     */
    private static String fetchFromUrl(final String urlStr, final String accept) {

        String result = null;

        try {
            final URI uri = new URI(urlStr);
            final URL url = uri.toURL();

            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", accept);

            final InputStream inputStream = con.getInputStream();
            final BufferedReader in = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String inputLine;
            final StringBuilder buffer = new StringBuilder(1000);
            while ((inputLine = in.readLine()) != null) {
                buffer.append(inputLine);
            }
            in.close();
            con.disconnect();
            result = buffer.toString();
        } catch (final IOException | URISyntaxException ex) {
            Log.warning(ex);
        }

        return result;
    }
}
