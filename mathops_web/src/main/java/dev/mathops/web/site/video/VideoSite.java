package dev.mathops.web.site.video;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.session.ISessionManager;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * A site that serves video files without a session requirement. Used by online course web pages, Precalculus course web
 * pages, or ad-hoc pages as needed.
 */
public final class VideoSite extends AbstractSite {

    /** Streaming server. */
    private static final String STREAM = "https://nibbler.math.colostate.edu/media/";

    /**
     * Constructs a new {@code VideoSite}.
     *
     * @param theSite     the site profile under which this site is accessed
     * @param theSessions the singleton user session repository
     */
    public VideoSite(final Site theSite, final ISessionManager theSessions) {

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
     * Generates the site title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "Precalculus Program";
    }

    /**
     * Processes a GET request.
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
                      final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        if ("favicon.ico".equals(subpath) || "secure/favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else if ("video.html".equals(subpath)) {
            // Used by MATH 160 and 161 course videos
            doVideo(req, resp);
        } else if (subpath.startsWith("lessons/")) {
            final String filename = subpath.substring(8);
            serveLesson(filename, req, resp);
        } else {
            final String msg = Res.fmt(Res.UNRECOGNIZED_PATH, subpath);
            Log.warning(msg);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Processes a POST request.
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
                       final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        final String msg = Res.fmt(Res.UNRECOGNIZED_PATH, subpath);
        Log.warning(msg);
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
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
    private static void doVideo(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        final String mediaId = req.getParameter("media-id");
        final String course = req.getParameter("course");
        final String dir = req.getParameter("dir");
        final String id = req.getParameter("id");
        final String width = req.getParameter("width");

        if (isParamInvalid(dir) || isParamInvalid(id) || isParamInvalid(width) || isParamInvalid(course)
            || isParamInvalid(mediaId)) {
            final String msg = Res.get(Res.BAD_PARAMETERS);
            Log.warning(msg);
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

            htm.addln("""
                    <!DOCTYPE html>")
                    <html>
                    <head>
                     <meta charset='utf-8'>
                     <meta http-equiv='X-UA-Compatible' content='IE=edge'/>
                     <meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>
                     <title>Department of Mathematics - Colorado State University</title>
                    </head>
                    <body style='padding:0;margin:0;'>
                     <div style='width:100vw;height:100vh;'>""");
            htm.addln(" <video style='", style, "' controls='controls' poster='", STREAM, dir, "/poster/", id, ".png'>")
                    .addln(" <source src='", STREAM, dir, "/mp4/", id, ".mp4' type='video/mp4'/>")
                    .addln(" <source src='", STREAM, dir, "/webm/", id, ".webm' type='video/webm'/>")
                    .addln(" <source src='", STREAM, dir, "/ogv/", id, ".ogv' type='video/ogg'/>")
                    .addln(" <track  src='", STREAM, dir, "/vtt/", id,
                            ".vtt' kind='subtitles' srclang='en' label='English' default/>")
                    .addln(Res.get(Res.VIDEO_NOT_SUPP))
                    .addln("""
                            </video>
                            </div>
                            </body>
                            </html>""");

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

            htm.addln("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                     <meta charset='utf-8'>
                     <meta http-equiv='X-UA-Compatible' content='IE=edge'/>
                     <meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>
                     <title>Department of Mathematics - Colorado State University</title>
                    </head>
                    <body style='padding:0;margin:0;'>""");

            if (mediaId == null || actualCourse == null) {
                htm.sDiv("indent11");
                htm.addln("Invalid video request.");
                htm.eDiv();
            } else {
                htm.addln("""
                        <script>
                        function showReportError() {
                         document.getElementById('error_rpt_link').className='hidden';
                         document.getElementById('error_rpt').className='visible';
                        }
                        </script>""");

                htm.sDiv("indent11");

                final boolean is160 = "M160".equals(actualCourse);

                htm.addln("<video ", (is160 ? "width='1024' height='768'" : "width='640' height='480'"),
                        " controls='controls' autoplay='autoplay'>");
                htm.addln(" <source src='", STREAM, direct, "/mp4/", mediaId, ".mp4' type='video/mp4'/>");
                htm.addln(" <source src='", STREAM, direct, "/ogv/", mediaId, ".ogv' type='video/ogg'/>");
                htm.addln(" <track src='/www/math/", direct, "/vtt/", mediaId, ".vtt' kind='subtitles' srclang='en' ",
                        "label='English' default='default'/>");
                htm.addln(" Your browser does not support inline video.");
                htm.addln("</video>");

                htm.addln("<div><a href='/math/", direct, "/transcripts/", mediaId,
                        ".pdf'>Access a plain-text transcript for screen-readers (Adobe PDF).</a></div>");

                htm.sDiv().add("<a href='", STREAM, direct, "/pdf/",
                        mediaId, ".pdf'>Access a static (Adobe PDF) version.</a>").eDiv();
            }

            htm.addln("""
                    </body>
                    </html>""");

            sendReply(req, resp, MIME_TEXT_HTML, htm);
        }
    }
}
