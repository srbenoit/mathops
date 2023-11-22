package dev.mathops.web.site.course;

import dev.mathops.core.PathList;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.xml.XmlEscaper;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A page that presents a video example, along with links for the PDF version, a plain-text transcript, and a box to
 * provide feedback.
 */
enum PageVideoExample {
    ;

    /** Streaming server. */
    private static final String STREAM = "https://nibbler.math.colostate.edu/media/";

    /** VTT/TXT server. */
    private static final String VTT = "/media/";

    /**
     * Displays the page that shows an embedded video.
     *
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information (could be null)
     * @throws IOException if there is an error writing the response
     */
    static void doGet(final CourseSite site, final ServletRequest req, final HttpServletResponse resp,
                      final ImmutableSessionInfo session) throws IOException {

        final String dir = req.getParameter("dir");
        final String id = req.getParameter("id");
        final String course = req.getParameter("course");

        // final String unitStr = req.getParameter("unit");
        // final String lessonStr = req.getParameter("lesson");

        if (AbstractSite.isParamInvalid(dir) || AbstractSite.isParamInvalid(id)
                || AbstractSite.isParamInvalid(course)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  dir='", dir, "'");
            Log.warning("  id='", id, "'");
            Log.warning("  course='", course, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startEmptyPage(htm, site.getTitle(), false);

            if (id == null || dir == null) {
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
                htm.addln("<video width='960' height='540' ",
                        "style='border:1px solid gray;' controls='controls' autoplay='autoplay'>");
                htm.addln(" <source src='", STREAM, dir, "/mp4/",
                        id, ".mp4' type='video/mp4'/>");
                htm.addln(" <track src='", VTT, dir, "/vtt/",
                        id, ".vtt' kind='subtitles' srclang='en' ",
                        "label='English' default='default'/>");
                htm.addln(" Your browser does not support inline video.");
                htm.addln("</video>");

                htm.addln("<div><a href='/math/", dir,
                        "/transcripts/", id, ".txt'>",
                        "Access a plain-text transcript for screen-readers.", //
                        "</a></div>");

                final String stuId = session == null ? "anon"
                        : session.getEffectiveUserId();
                final File file = feedbackFile(course, id, stuId);

                htm.sDiv("visible", "id='error_rpt_link'");
                htm.addln("<a href='#' onClick='showReportError();'>",
                        session != null && file.exists()
                                ? "Edit your existing error report or recommendation..."
                                : "Report an error or recommend an improvement...",
                        "</a>").eDiv();

                htm.sP();
                htm.addln("<form class='hidden' id='error_rpt' ",
                        "action='example_feedback.html' method='post'>");
                htm.addln(" <input type='hidden' name='course' value='",
                        course, "'/>");
                htm.addln(" <input type='hidden' name='media' value='",
                        id, "'/>");
                htm.addln(" Please describe the error or recommend an improvement:<br/>");
                htm.addln(" <textarea rows='5' cols='40' name='comments'>");
                if (session != null && file.exists()) {
                    htm.addln(XmlEscaper.escape(FileLoader.loadFileAsString(file, true)));
                }
                htm.addln(" </textarea>").br();
                htm.addln(" <input type='submit' value='Submit'/>");
                htm.addln("</form>").eP();
                htm.eDiv();
            }

            Page.endEmptyPage(htm, false);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                    htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Handles submission of feedback on a video example.
     *
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException if there is an error writing the response
     */
    static void doExampleFeedback(final CourseSite site, final ServletRequest req,
                                  final HttpServletResponse resp, final ImmutableSessionInfo session) throws IOException {

        final String courseId = req.getParameter("course");
        final String mediaId = req.getParameter("media");

        if (AbstractSite.isParamInvalid(courseId) || AbstractSite.isParamInvalid(mediaId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", courseId, "'");
            Log.warning("  srcourse='", mediaId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startEmptyPage(htm, site.getTitle(), false);

            final File file = feedbackFile(courseId, mediaId,
                    session == null ? "anon" : session.getEffectiveUserId());

            try (final FileWriter wri = new FileWriter(file, StandardCharsets.UTF_8)) {
                wri.write(req.getParameter("comments"));

                htm.sP().br().add("Thank you for your feedback.  Your comments have been sent to ",
                        "our course development team.").eP();
            } catch (final IOException ex) {
                Log.warning("Error posting feedback", ex);
                htm.sP().br().add("There was an error sending your feedback to the development team.").eP();
            }

            Page.endEmptyPage(htm, false);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                    htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Generates a file that represents student feedback on a video.
     *
     * @param courseId the ID of the course
     * @param mediaId  the ID of the media object
     * @param userId   the ID of the user providing feedback
     * @return the file where feedback is stored
     */
    private static File feedbackFile(final String courseId, final String mediaId, final String userId) {

        final File baseDir = PathList.getInstance().baseDir;
        final File dir = new File(baseDir, "feedback");

        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Log.warning("Failed to create feedback directory ", dir.getAbsolutePath());
            }
        }

        return new File(dir, courseId + "_" + mediaId + "_" + userId + ".txt");
    }
}
