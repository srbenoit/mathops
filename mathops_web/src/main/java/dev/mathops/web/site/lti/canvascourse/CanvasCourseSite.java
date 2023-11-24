package dev.mathops.web.site.lti.canvascourse;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.course.CourseSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * An LTI site to integrate Precalculus courses into Canvas.
 */
public final class CanvasCourseSite extends CourseSite {

    /** LMS host running Canvas. */
    static final String LMS_HOST = "frobenius.natsci.colostate.edu";

    /** Host - change to toggle between DEV and PROD. */
    static final String HOST = "coursedev.math.colostate.edu";

    // /** Consumer key. */
    // static final String CONSUMER_KEY = "c4w0958w30jraodsf";

    /** Shared secret. */
    static final String SHARED_SECRET = "sDar_otmGeolh~SADSge";

    /** Developer Key. */
    static final String DEV_KEY = "c2bNHX2Lh33RVp4ADfvTjJllpwghuLVV9kQb0YfeRV2WHUITj7h8Bcnc7sOgC7I2";

    /** Shared secret. */
    static final String CLIENT_ID = "10000000000008";

    // Name: CSU Math Refresher
    // URL: https://coursedev.math.colostate.edu/csu_math_course_mgr/cartridge_basiclti_link.xml
    // Redirect: https://coursedev.math.colostate.edu/csu_math_course_mgr/oauth2response.html
    // Icon: https://coursedev.math.colostate.edu/www/images/favicon.ico

    /**
     * Constructs a new {@code CanvasCourseSite}.
     *
     * @param theSiteProfile the site profile under which this site is accessed
     * @param theSessions    the singleton user session repository
     */
    public CanvasCourseSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);
    }

    /**
     * Indicates whether this site should do live queries to update student registration data.
     *
     * @return true to do live registration queries; false to skip
     */
    @Override
    public boolean doLiveRegQueries() {

        return false;
    }

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache    the data cache
     * @param subpath  the portion of the path beyond that which was used to select this site
     * @param type the site type
     * @param req      the request
     * @param resp     the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doGet(final Cache cache, final String subpath, final ESiteType type,
                      final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        Log.info("GET ", subpath);

        if ("basestyle.css".equals(subpath) || "secure/basestyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
        } else if ("style.css".equals(subpath) || "secure/style.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(getClass(), "style.css", true));
        } else if ("favicon.ico".equals(subpath) || "secure/favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else if (subpath.startsWith("images/")) {
            serveImage(subpath.substring(7), req, resp);
        } else if ("cartridge_basiclti_link.xml".equals(subpath)) {
            doGetCartridgeBasicLTILink(req, resp);
        } else if ("oauth2response.html".equals(subpath)) {
            OAuth2Response.doGet(req, resp);
        } else if ("launch.html".equals(subpath)) {
            doGetLaunch(req, resp);
        } else {
            Log.info("GET request to unrecognized URL: ", subpath);

            final Enumeration<String> e1 = req.getParameterNames();
            while (e1.hasMoreElements()) {
                final String name = e1.nextElement();
                Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
            }

            Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Processes a POST request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache    the data cache
     * @param subpath  the portion of the path beyond that which was used to select this site
     * @param type the site type
     * @param req      the request
     * @param resp     the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doPost(final Cache cache, final String subpath, final ESiteType type,
                       final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        Log.info("POST ", subpath);

        // TODO: Honor maintenance mode.

        if ("course_content.html".equals(subpath)) {
            PageCourseContent.doGet(req, resp);
        } else if ("course_config.html".equals(subpath)) {
            PageCourseConfig.doPost(req, resp);
        } else if ("course_admin.html".equals(subpath)) {
            PageCourseAdmin.doPost(req, resp);
        } else {
            Log.info("POST request to unrecognized URL: ", subpath);

            final Enumeration<String> e1 = req.getParameterNames();
            while (e1.hasMoreElements()) {
                final String name = e1.nextElement();
                Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
            }

            Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "LTI";
    }

    /**
     * Responds to a GET of "cartridge_basiclti_link.xml".
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void doGetCartridgeBasicLTILink(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

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

        htm.addln("<blti:title>CSU Math Course Manager</blti:title>");

        htm.addln("<blti:description>",
                "Integration of CSU Math courses with Canvas.</blti:description>");

        htm.addln("<blti:icon>https://", HOST,
                "/www/images/favicon24.png</blti:icon>");

        htm.addln("<blti:secure_launch_url>https://", HOST,
                "/csu_math_course_mgr/</blti:secure_launch_url>");

        htm.addln("<blti:extensions platform=\"canvas.instructure.com\">");
        htm.addln("<lticm:property name=\"tool_id\">CSU_Math_Course_Manager</lticm:property>");
        htm.addln("<lticm:property name=\"privacy_level\">public</lticm:property>");
        htm.addln("<lticm:property name=\"domain\">math.colostate.edu</lticm:property>");
        htm.addln("<lticm:property name=\"selection_height\">500</lticm:property>");
        htm.addln("<lticm:property name=\"selection_width\">500</lticm:property>");
        htm.addln("<lticm:property name=\"text\">",
                "Integration of CSU Math courses with Canvas.</lticm:property>");

        // *** COURSE HOME SUBNAVIGATION - This appears in the right-hand stack of buttons within
        // each course (for the instructor, this appears below "Import existing content", and
        // above "Choose Home Page", and for the student, it appears at the top, above "View Course
        // Stream")

        htm.addln("<lticm:options name=\"course_home_sub_navigation\">");
        htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        htm.addln("<lticm:property name=\"text\">Configure CSU Course</lticm:property>");
        htm.addln("<lticm:property name=\"url\">https://", HOST,
                "/csu_math_course_mgr/course_config.html</lticm:property>");
        htm.addln("</lticm:options>");

        //

        // *** COURSE NAVIGATION - this link appears in the left-hand course menu, along with
        // "Assignments", "Modules", "Pages", etc.

        htm.addln("<lticm:options name=\"course_navigation\">");
        htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        htm.addln("<lticm:property name=\"text\">course_navivation Text</lticm:property>");
        htm.addln("<lticm:property name=\"url\">https://", HOST,
                "/csu_math_course_mgr/course_content.html</lticm:property>");
        htm.addln("<lticm:options name=\"labels\">");
        htm.addln("<lticm:property name=\"en\">CSU Math Refresher</lticm:property>");
        htm.addln("</lticm:options>");
        htm.addln("</lticm:options>");

        // *** TOOL CONFIGURATION - called when the installer of the tool clicks "Configure"

        htm.addln("<lticm:options name=\"tool_configuration\">");
        htm.addln("<lticm:property name=\"enabled\">true</lticm:property>");
        htm.addln("<lticm:property name=\"text\">Administration</lticm:property>");
        htm.addln("<lticm:property name=\"url\">https://", HOST,
                "/csu_math_course_mgr/course_admin.html</lticm:property>");
        htm.addln("</lticm:options>");

        htm.addln("</blti:extensions>");

        htm.addln("<cartridge_bundle identifierref=\"BLTI001_Bundle\"/>");
        htm.addln("<cartridge_icon   identifierref=\"BLTI001_Icon\"/>");
        htm.addln("</cartridge_basiclti_link>");

        sendReply(req, resp, MIME_TEXT_XML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Responds to a GET of "launch.html".
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void doGetLaunch(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        Log.info("GET request to LTI launch URL:");

        final Enumeration<String> e1 = req.getParameterNames();
        while (e1.hasMoreElements()) {
            final String name = e1.nextElement();
            Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
