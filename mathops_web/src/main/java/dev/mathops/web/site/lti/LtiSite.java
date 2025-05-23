package dev.mathops.web.site.lti;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.BasicCss;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.course.CourseSite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * A site to deliver Precalculus exams through a Canvas LTI quiz.
 */
public final class LtiSite extends CourseSite {

    /** Streaming server. */
    private static final String STREAM = "https://nibbler.math.colostate.edu/media/";

    /**
     * Constructs a new {@code LtiSite}.
     *
     * @param theSite     the site profile under which this site is accessed
     * @param theSessions the singleton user session repository
     */
    public LtiSite(final Site theSite, final ISessionManager theSessions) {

        super(theSite, theSessions);
    }

    /**
     * Tests the live refresh policy for this site.
     *
     * @return the live refresh policy
     */
    @Override
    protected ELiveRefreshes getLiveRefreshes() {

        return ELiveRefreshes.NONE;
    }

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doGet(final Cache cache, final String subpath, final ESiteType type,
                      final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        Log.info("GET ", subpath);

        if ("lti13_dynamic_registration.html".equals(subpath)) {
            PageDynamicRegistration.doDynamicRegistration(this, req, resp);

        } else if ("lti13_dev_key_configuration.json".equals(subpath)) {
            PageLTI13.doGetDevKeyConfigurationJson(req, resp);
        } else if ("lti13_registration_callback.json".equals(subpath)) {
            // TODO:
        } else if ("lti13_launch.json".equals(subpath)) {
            // TODO:
        } else if ("lti13_jwks.json".equals(subpath)) {
            // TODO:

            //
            //
            //

        } else if ("basestyle.css".equals(subpath) || "secure/basestyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
        } else if ("style.css".equals(subpath) || "secure/style.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(getClass(), "style.css", true));
        } else if ("course.css".equals(subpath)) {
            BasicCss.getInstance().serveCss(req, resp);
        } else if ("favicon.ico".equals(subpath) || "secure/favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else if (subpath.startsWith("images/")) {
            serveImage(subpath.substring(7), req, resp);
        } else if (CoreConstants.EMPTY.equals(subpath) || "index.html".equals(subpath)) {
            PageIndex.showPage(req, resp);
        } else if ("onlineproctor.html".equals(subpath)) {
            // This is linked from the admin website
            PageOnlineProctor.showPage(req, resp, null, null);
        } else if ("onlineproctorchallenge.html".equals(subpath)) {
            // This is linked from the admin website
            PageOnlineProctorChallenge.showPage(req, resp, null, null);
        } else if ("home.html".equals(subpath)) {
            PageHome.showPage(cache, this, req, resp);
        } else if ("challenge.html".equals(subpath)) {
            PageChallenge.showPage(cache, this, req, resp);
        } else if ("course.html".equals(subpath)) {
            PageFinished.showPage(req, resp);
        } else if ("cartridge_basiclti_link.xml".equals(subpath)) {
            PageLTI.doGetCartridgeBasicLTILink(req, resp);
        } else if ("launch.html".equals(subpath)) {
            PageLTI.doGetLaunch(req, resp);
        } else if ("endp.html".equals(subpath)) {
            PageLTI.doGetEndpoint(req, resp);
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
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doPost(final Cache cache, final String subpath, final ESiteType type,
                       final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        Log.info("POST ", subpath);

        // TODO: Honor maintenance mode.

        switch (subpath) {

            // THe next three are used by the online Teams proctoring process
            case "gainaccess.html" -> PageIndex.processAccessCode(cache, this, req, resp);
            case "beginproctor.html" -> PageOnlineProctor.processBeginProctor(req, resp);
            case "beginproctorchallenge.html" -> PageOnlineProctorChallenge.processBeginProctor(cache, req, resp);
            case "home.html" -> PageHome.showPage(cache, this, req, resp);
            case "challenge.html" -> PageChallenge.showPage(cache, this, req, resp);
            case "update_unit_exam.html" -> PageHome.updateUnitExam(cache, req, resp);
            case "update_challenge_exam.html" -> PageChallenge.updateChallengeExam(cache, req, resp);
            case null, default -> {

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
    }

    /**
     * Validates the user session. If the session is invalid, an error is logged and the user is redirected to the
     * index.html page.
     *
     * @param req      the request
     * @param resp     the response
     * @param failPage the page to which to redirect the user on a failed validation
     * @return the {@code ImmutableSessionInfo} if the session is valid; {@code null} if not
     * @throws IOException if there is an error writing the response
     */
    @Override
    public ImmutableSessionInfo validateSession(final HttpServletRequest req,
                                                final HttpServletResponse resp, final String failPage) throws IOException {

        final String sess = extractSessionId(req);

        final SessionResult session = SessionManager.getInstance().validate(sess);
        final ImmutableSessionInfo result = session.session;

        if (result == null) {
            if (sess != null) {
                Log.warning("Session validation error: ", session.error);

                // Tell the client to delete the cookie that provided the session ID
                final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, sess);
                cook.setDomain(req.getServerName());
                cook.setPath(CoreConstants.SLASH);
                cook.setMaxAge(0);
                resp.addCookie(cook);
            }

            if (failPage != null) {
                final String path = this.site.path;
                resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) ? failPage
                        : CoreConstants.SLASH + failPage));
            }
        }

        return result;
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
     * Displays the page that shows an embedded video for a course based on a media-id, course, unit, and lesson.
     *
     * <p>
     * The request should have the following parameters:
     * <ul>
     * <li><b>dir</b>: the directory on the streaming server (below the 'media' directory, such as
     * "M261") from which to serve content. This directory should contain 'poster', 'mp4', 'webm',
     * 'ogv', and 'vtt' subdirectories.
     * <li><b>id</b>: the video ID, such as "MC.13-Vectors.01-3DCoordinates.01.LE.01". The
     * appropriate suffix will be added to this value to obtain the filename to serve from each
     * subdirectory of <b>dir</b>
     * <li><b>width</b> (optional): a CSS width, specified in pixels, such as "640px". If present
     * (and if the value appears to be a valid CSS measurement), video width will be set as
     * indicated; if omitted, width and height will be set to 100%.
     * </ul>
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void doVideo(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final String mediaId = req.getParameter("media-id");
        final String course = req.getParameter("course");
        final String dir = req.getParameter("dir");
        final String id = req.getParameter("id");
        final String width = req.getParameter("width");

        if (isParamInvalid(dir) || isParamInvalid(id) || isParamInvalid(width) || isParamInvalid(course)
            || isParamInvalid(mediaId)) {
            Log.warning("Bad parametrs - possible attack");
            Log.warning("  dir='", dir, "'");
            Log.warning("  id='", id, "'");
            Log.warning("  width='", width, "'");
            Log.warning("  course='", course, "'");
            Log.warning("  mediaId='", mediaId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if ((course == null || course.isEmpty()) && (mediaId == null || mediaId.isEmpty())) {
            final HtmlBuilder htm = new HtmlBuilder(1000);

            final String style;
            if ((width == null || width.isEmpty()) || !width.endsWith("px")) {
                style = "width:100vw;height:100vh;object-fit:contain;";
            } else {
                style = "width:" + width;
            }

            htm.addln("<!DOCTYPE html>")
                    .addln("<html>")
                    .addln("<head>")
                    .addln(" <meta charset='utf-8'>")
                    .addln(" <meta http-equiv='X-UA-Compatible' content='IE=edge'/>")
                    .addln(" <meta http-equiv='Content-Type' ",
                            "content='text/html;charset=utf-8'/>")
                    .addln(" <title>Department of Mathematics - Colorado State University</title>")
                    .addln("</head>")
                    .addln("<body style='padding:0;margin:0;'>")
                    .addln("<div style='width:100vw;height:100vh;'>")
                    .addln("<video style='", style,
                            "' controls='controls' poster='", STREAM, dir,
                            "/poster/", id, ".png'>")
                    .addln(" <source src='", STREAM, dir, "/mp4/", id,
                            ".mp4' type='video/mp4'/>")
                    .addln(" <source src='", STREAM, dir, "/webm/", id,
                            ".webm' type='video/webm'/>")
                    .addln(" <source src='", STREAM, dir, "/ogv/", id,
                            ".ogv' type='video/ogg'/>")
                    .addln(" <track  src='", STREAM, dir, "/vtt/", id,
                            ".vtt' kind='subtitles' srclang='en' label='English' default/>")
                    .addln("Video format not supported.") //
                    .addln("</video>")
                    .addln("</div>")
                    .addln("</body>")
                    .addln("</html>");

            sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        } else {
            // For the tutorial courses, we don't want to force duplication of the video files in a
            // new directory, so map those course numbers to the corresponding non-tutorial courses
            final String actualCourse = switch (course) {
                case RawRecordConstants.M1170 -> RawRecordConstants.M117;
                case RawRecordConstants.M1180 -> RawRecordConstants.M118;
                case RawRecordConstants.M1240 -> RawRecordConstants.M124;
                case RawRecordConstants.M1250 -> RawRecordConstants.M125;
                case RawRecordConstants.M1260 -> RawRecordConstants.M126;
                case null, default -> course;
            };

            final String direct = actualCourse == null ? null
                    : actualCourse.replace(CoreConstants.SPC, CoreConstants.EMPTY);

            final HtmlBuilder htm = new HtmlBuilder(2000);

            htm.addln("<!DOCTYPE html>")
                    .addln("<html>")
                    .addln("<head>")
                    .addln(" <meta charset='utf-8'>")
                    .addln(" <meta http-equiv='X-UA-Compatible' content='IE=edge'/>")
                    .addln(" <meta http-equiv='Content-Type' ",
                            "content='text/html;charset=utf-8'/>")
                    .addln(" <title>Department of Mathematics - Colorado State University</title>")
                    .addln("</head>")
                    .addln("<body style='padding:0;margin:0;'>");

            if (mediaId == null || actualCourse == null) {
                htm.sDiv("indent11");
                htm.addln("Invalid video request.");
                htm.eDiv();
            } else {
                htm.addln("<script>");
                htm.addln(" function showReportError() {");
                htm.addln("  document.getElementById('error_rpt_link')",
                        ".className='hidden';");
                htm.addln("  document.getElementById('error_rpt')",
                        ".className='visible';");
                htm.addln(" }");
                htm.addln("</script>");

                htm.sDiv("indent11");

                htm.addln("<video ", "M160".equals(actualCourse)
                                ? "width='1024' height='768'"
                                : "width='640' height='480'",
                        " controls='controls' autoplay='autoplay'>");
                htm.addln(" <source src='", STREAM, direct, "/mp4/",
                        mediaId, ".mp4' type='video/mp4'/>");
                htm.addln(" <source src='", STREAM, direct, "/ogv/",
                        mediaId, ".ogv' type='video/ogg'/>");
                htm.addln(" <track src='/www/math/", direct, "/vtt/",
                        mediaId, ".vtt' kind='subtitles' srclang='en' ",
                        "label='English' default='default'/>");
                htm.addln(" Your browser does not support inline video.");
                htm.addln("</video>");

                htm.addln("<div><a href='/math/", direct,
                        "/transcripts/", mediaId, ".pdf'>",
                        "Access a plain-text transcript for screen-readers (Adobe PDF).", //
                        "</a></div>");

                htm.sDiv().add("<a href='", STREAM, direct, "/pdf/",
                        mediaId, ".pdf'>Access a static (Adobe PDF) version.</a>").eDiv();
            }

            htm.addln("</body>")
                    .addln("</html>");

            sendReply(req, resp, MIME_TEXT_HTML, htm);
        }
    }
}
