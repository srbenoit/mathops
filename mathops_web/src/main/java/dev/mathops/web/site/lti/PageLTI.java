package dev.mathops.web.site.lti;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * The page that handles Precalculus unit exams as an LTI tool.
 */
enum PageLTI {
    ;

    /** Host - change to toggle between DEV and PROD. */
    private static final String HOST = "coursedev.math.colostate.edu";

    /**
     * Responds to a GET of "cartridge_basiclti_link.xml".
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doGetCartridgeBasicLTILink(final ServletRequest req,
                                           final HttpServletResponse resp) throws IOException {

        Log.info("GET request to LTI cartridge basic LTI link URL:");

        final HtmlBuilder htm = new HtmlBuilder(2000);

        htm.addln("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        htm.addln("<cartridge_basiclti_link xmlns=\"http://www.imsglobal.org/xsd/imslticc_v1p0\"");
        htm.addln("  xmlns:blti = \"http://www.imsglobal.org/xsd/imsbasiclti_v1p0\"");
        htm.addln("  xmlns:lticm =\"http://www.imsglobal.org/xsd/imslticm_v1p0\"");
        htm.addln("  xmlns:lticp =\"http://www.imsglobal.org/xsd/imslticp_v1p0\"");
        htm.addln("  xmlns:xsi = \"http://www.w3.org/2001/XMLSchema-instance\"");
        htm.addln("  xsi:schemaLocation = \"http://www.imsglobal.org/xsd/imslticc_v1p0 ",
                "http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticc_v1p0.xsd");
        htm.addln("  http://www.imsglobal.org/xsd/imsbasiclti_v1p0 ",
                "http://www.imsglobal.org/xsd/lti/ltiv1p0/imsbasiclti_v1p0.xsd");
        htm.addln("  http://www.imsglobal.org/xsd/imslticm_v1p0 ",
                "http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticm_v1p0.xsd");
        htm.addln("  http://www.imsglobal.org/xsd/imslticp_v1p0 ",
                "http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticp_v1p0.xsd\">");

        htm.addln("<blti:title>CSU Math Refresher</blti:title>");

        htm.addln(
                "<blti:description>Library of Mathematics lessons to refresh skills.</blti:description>");

        htm.addln("<blti:icon>https://", HOST,
                "/images/bullet.png</blti:icon>");

        htm.addln("<blti:secure_launch_url>https://", HOST,
                "/lti/</blti:secure_launch_url>");

        htm.addln("<blti:extensions platform=\"canvas.instructure.com\">");
        htm.addln("<lticm:property name=\"tool_id\">CSU_MATH_refresher</lticm:property>");
        htm.addln("<lticm:property name=\"privacy_level\">public</lticm:property>");
        htm.addln("<lticm:property name=\"domain\">math.colostate.edu</lticm:property>");
        htm.addln("<lticm:property name=\"selection_height\">500</lticm:property>");
        htm.addln("<lticm:property name=\"selection_width\">500</lticm:property>");
        htm.addln("<lticm:property name=\"text\">",
                "Mathematics lessons to refresh skills.</lticm:property>");

        //

        // *** ACCOUNT NAVIGATION - menu item in the administrator account under which the tool
        // is installed in Canvas.

        // htm.addln("<lticm:options name=\"account_navigation\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">account_navigation Text</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/account_navigation.html</lticm:property>");
        // htm.addln("<lticm:options name=\"labels\">");
        // htm.addln("<lticm:property name=\"en\">Account Navigation Label</lticm:property>");
        // htm.addln("</lticm:options>");
        // htm.addln("</lticm:options>");

        //

        // *** ASSIGNMENT MENU - this appears in the context menu to configure an assignment,
        // along with "Delete" and "Duplicate".

        // htm.addln("<lticm:options name=\"assignment_menu\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">assignment_menu Text</lticm:property>");
        // htm.addln("<lticm:property name=\"message_type\">ContentItemSelection</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/assignment_menu.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        // *** ASSIGNMENT SELECTION - When an assignment is set to "external tool" as its submission
        // type, "Find" has this link

        // htm.addln("<lticm:options name=\"assignment_selection\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">assignment_selection Text</lticm:property>");
        // htm.addln("<lticm:property name=\"message_type\">",
        // "ContentItemSelectionRequest</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/assignment_selection.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        // *** COURSE HOME SUBNAVIGATION - This appears in the right-hand stack of buttons within
        // each course (for the instructor, this appears below "Import existing content", and
        // above "Choose Home Page", and for the student, it appears at the top, above "View Course
        // Stream")

        // htm.addln("<lticm:options name=\"course_home_sub_navigation\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">Configure CSU Math Refresher</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/config_math_refresher.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        // *** COURSE NAVIGATION - this link appears in the left-hand course menu, along with
        // "Assignments", "Modules", "Pages", etc.

        htm.addln("<lticm:options name=\"course_navigation\">");
        htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        htm.addln("<lticm:property name=\"text\">course_navivation Text</lticm:property>");
        htm.addln("<lticm:property name=\"url\">https://", HOST,
                "/lti/math_refresher.html</lticm:property>");
        htm.addln("<lticm:options name=\"labels\">");
        htm.addln("<lticm:property name=\"en\">CSU Math Refresher</lticm:property>");
        htm.addln("</lticm:options>");
        htm.addln("</lticm:options>");

        //

        // *** COURSE SETTINGS SUBNAVIGATION - this appears in the stack of buttons on the right
        // side within course settings (available to course owner), along with "Course Statistics",
        // "Course Calendar", etc.

        // htm.addln("<lticm:options name=\"course_settings_sub_navigation\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">",
        // "Math Refresher Settings</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/course_settings_sub_navigation.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        // *** Discussion topic menu - this appears in the context menu of a discussion (available
        // to the owner, not to students), along with "Pin", or "Close for comments". It also
        // appears in the context menu of an announcement.

        // htm.addln("<lticm:options name=\"discussion_topic_menu\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">discussion_topic_menu Text</lticm:property>");
        // htm.addln("<lticm:property name=\"message_type\">ContentItemSelection</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/discussion_topic_menu.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        // *** A button that appears in the content editor for an assignment, along with "Bold"
        // and "Italic" or

        // htm.addln("<lticm:options name=\"editor_button\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">editor_button Text</lticm:property>");
        // htm.addln("<lticm:property name=\"message_type\">",
        // "ContentItemSelectionRequest</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/editor_button.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        // *** FILES MENU - this appears in the context menu for a selected file, along with
        // "Download"

        // htm.addln("<lticm:options name=\"file_menu\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">file_menu Text</lticm:property>");
        // htm.addln("<lticm:property name=\"message_type\">ContentItemSelection</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/file_menu.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        // *** GLOBAL NAVIGATION - this would appear in the left-hand menu along with "Dashboard",
        // "Courses", and "Calendar" - operations not associated with any course

        // htm.addln("<lticm:options name=\"global_navigation\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">global_navigation Text</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/global_navigation.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        // *** HOMEWORK SUBMISSION - when an assignment is configured to allow file upload as a
        // submission type, the submission panel for the student will include "File Upload" and
        // this link as options.

        // htm.addln("<lticm:options name=\"homework_submission\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">homework_submission Text</lticm:property>");
        // htm.addln("<lticm:property name=\"message_type\">",
        // "ContentItemSelectionRequest</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/homework_submission.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        // *** LINK SELECTION - When adding an item to a module, if "External Tool" is chosen,
        // this appears as an option.

        // htm.addln("<lticm:options name=\"link_selection\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">link_selection Text</lticm:property>");
        // htm.addln("<lticm:property name=\"message_type\">",
        // "ContentItemSelectionRequest</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/link_selection.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        // *** MIGRATION SELECTION - Appears in the options under "Import Course content"

        // htm.addln("<lticm:options name=\"migration_selection\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">migration_selection Text</lticm:property>");
        // htm.addln("<lticm:property name=\"message_type\">",
        // "ContentItemSelectionRequest</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/migration_selection.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        // *** MODULE MENU - appears in the context menu for a module (available to instructor,
        // not to students), along with "Delete" and "Duplicate"

        // htm.addln("<lticm:options name=\"module_menu\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">module_menu Text</lticm:property>");
        // htm.addln("<lticm:property name=\"message_type\">ContentItemSelection</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/module_menu.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        // *** QUIZ MENU - appears in the context menu for a quiz, along with "Preview" and "Show
        // Student Quiz Results" (available to instructor, not to student)

        // htm.addln("<lticm:options name=\"quiz_menu\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">quiz_menu Text</lticm:property>");
        // htm.addln("<lticm:property
        // name=\"message_type\">ContentItemSelection</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/quiz_menu.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        // *** TOOL CONFIGURATION - called when the installer of the tool clicks "Configure"

        htm.addln("<lticm:options name=\"tool_configuration\">");
        htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        htm.addln("<lticm:property name=\"text\">tool_configuration Text</lticm:property>");
        htm.addln("<lticm:property name=\"url\">https://", HOST,
                "/lti/tool_configuration.html</lticm:property>");
        htm.addln("</lticm:options>");

        //

        // *** USER NAVIGATION - menu item under each user's "Account" sub-menu or under the
        // "Settings" page for the user

        // htm.addln("<lticm:options name=\"user_navigation\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">user_navigation Text</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/user_navigation.html</lticm:property>");
        // htm.addln("<lticm:options name=\"labels\">");
        // htm.addln("<lticm:property name=\"en\">User Navigation Label</lticm:property>");
        // htm.addln("</lticm:options>");
        // htm.addln("</lticm:options>");

        //

        // *** PAGE MENU - appears in the context menu for a page, along with "Use as Front Page",
        // and "View Page History"

        // htm.addln("<lticm:options name=\"wiki_page_menu\">");
        // htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        // htm.addln("<lticm:property name=\"text\">wiki_page_menu Text</lticm:property>");
        // htm.addln("<lticm:property
        // name=\"message_type\">ContentItemSelection</lticm:property>");
        // htm.addln("<lticm:property name=\"url\">https://", HOST,
        // "/lti/wiki_page_menu.html</lticm:property>");
        // htm.addln("</lticm:options>");

        //

        htm.addln("</blti:extensions>");

        htm.addln("<cartridge_bundle identifierref=\"BLTI001_Bundle\"/>");
        htm.addln("<cartridge_icon   identifierref=\"BLTI001_Icon\"/>");
        htm.addln("</cartridge_basiclti_link>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_XML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Responds to a GET of "launch.html".
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doGetLaunch(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        Log.info("GET request to LTI launch URL:");

        final Enumeration<String> e1 = req.getParameterNames();
        while (e1.hasMoreElements()) {
            final String name = e1.nextElement();
            Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

//    /**
//     * Responds to a GET of "editor_button.html".
//     *
//     * @param req  the request
//     * @param resp the response
//     * @throws IOException if there is an error writing the response
//     */
//    static void doPostEditorButton(final ServletRequest req, final HttpServletResponse resp)
//            throws IOException {
//
//        Log.info("POST request to editor_button.html");
//
//        final Enumeration<String> e1 = req.getParameterNames();
//        while (e1.hasMoreElements()) {
//            final String name = e1.nextElement();
//            Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
//        }
//
//        final String action = req.getParameter("content_item_return_url");
//
//        final String endp = "https://coursedev.math.colostate.edu/lti/endp.html";
//        final String url = "https://coursedev.math.colostate.edu/lti/index.html?exam=171UE";
//        // $NON-NLS-1$
//
//        // final String redir =
//        // action + "?return_type=iframe&url=" + URLEncoder.encode(url) + "&width=600&height=600";
//
//        try {
//            final String redir = action + "?return_type=oembed&endpoint="
//                    + URLEncoder.encode(endp, StandardCharsets.UTF_8)
//                    + "&url=" + URLEncoder.encode(url, StandardCharsets.UTF_8);
//
//            Log.info("Reddirect is ", redir);
//
//            resp.sendRedirect(redir);
//        } catch (final UnsupportedEncodingException ex) {
//            Log.warning(ex);
//        }
//    }

    /**
     * Responds to a Get to "endp.html".
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doGetEndpoint(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        Log.info("GET request to endp.html");

        final Enumeration<String> e1 = req.getParameterNames();
        while (e1.hasMoreElements()) {
            final String name = e1.nextElement();
            Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
        }

        final String url = req.getParameter("url");

        final HtmlBuilder json = new HtmlBuilder(200);

        json.addln("{");
        json.addln("    \"type\":    \"rich\",");
        json.addln("    \"version\": \"1.0\",");
        json.addln("    \"html\":    \"<div style='width:100%; height:620px;'>",
                "<iframe style='height:100%; width:100%; border:0;' src='", url, "'></iframe></div>\",");
        json.addln("    \"width\":   600,");
        json.addln("    \"height\":  600");
        json.addln(" }");

        Log.fine(json.toString());

        AbstractSite.sendReply(req, resp, Page.MIME_APP_JSON, json.toString().getBytes(StandardCharsets.UTF_8));
    }

//    /**
//     * Responds to a POST to "tool_configuration.html".
//     *
//     * @param req  the request
//     * @param resp the response
//     * @throws IOException if there is an error writing the response
//     */
//    static void doPostToolConfiguration(final HttpServletRequest req,
//                                        final HttpServletResponse resp) throws IOException {
//
//        final HtmlBuilder htm = new HtmlBuilder(2000);
//        Page.startPage(htm, "Tool Configuration", false, false);
//        htm.addln("<body style='background:white'>");
//
//        final Map<String, List<String>> params = new HashMap<>(40);
//        final EOAuthRequestVerifyResult verifyResult = OAuth1.verifyRequest(req, params);
//
//        if (verifyResult == EOAuthRequestVerifyResult.VERIFIED) {
//
//            htm.sH(2).add("CSU Math Refresher Canvas Integration").eH(2);
//
//            htm.sP().add("Under construction...").eP();
//        } else {
//            htm.sP().add("Request was not verified: ", verifyResult.name()).eP();
//        }
//
//        htm.addln("</body>");
//        htm.addln("</html>");
//
//        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
//    }

//    /**
//     * Responds to a POST to "launch.html".
//     *
//     * @param req  the request
//     * @param resp the response
//     * @throws IOException if there is an error writing the response
//     */
//    static void doPostLaunch(final ServletRequest req, final HttpServletResponse resp) throws IOException {
//
//        Log.info("POST request to LTI launch URL:");
//
//        final Enumeration<String> e1 = req.getParameterNames();
//        while (e1.hasMoreElements()) {
//            final String name = e1.nextElement();
//            Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
//        }
//
//        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
//    }
}
