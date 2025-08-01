package dev.mathops.web.host.placement.tutorial.elm;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.tutorial.ELMTutorialStatus;
import dev.mathops.db.old.rawlogic.RawStcuobjectiveLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.xml.XmlEscaper;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates the content of the web page that displays the outline of a course tailored to a student's position and
 * status in the course.
 */
enum PageVideo {
    ;

    /** Streaming server. */
    private static final String STREAM = "https://nibbler.math.colostate.edu/media/";

    /**
     * Displays the page that shows an embedded video.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param status  the student status with respect to the ELM Tutorial
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final ELMTutorialStatus status) throws IOException, SQLException {

        final String mediaId = req.getParameter("media-id");
        final String unitStr = req.getParameter("unit");
        final String lessonStr = req.getParameter("lesson");

        if (AbstractSite.isParamInvalid(mediaId) || AbstractSite.isParamInvalid(unitStr)
                || AbstractSite.isParamInvalid(lessonStr)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  media-id='", mediaId, "'");
            Log.warning("  unit='", unitStr, "'");
            Log.warning("  lesson='", lessonStr, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final String dir = "M100T";

            // If this is an instructor lecture, record that the student has viewed it.
            final String studentId = session.getEffectiveUserId();
            if (studentId != null && mediaId != null && unitStr != null && lessonStr != null
                    && mediaId.endsWith("OV")) {

                try {
                    RawStcuobjectiveLogic.recordLectureView(cache, studentId, RawRecordConstants.M100T,
                            Integer.valueOf(unitStr), Integer.valueOf(lessonStr), session.getNow());
                } catch (final NumberFormatException ex) {
                    Log.warning("Failed to record lecture view for student ", studentId, " media ID ", mediaId);
                }
            }

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Entry Level Mathematics Tutorial",
                    "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

            htm.sDiv("menupanel");
            TutorialMenu.buildMenu(cache, session, status, htm);
            htm.sDiv("panel");

            if (mediaId == null) {
                htm.sDiv("indent11");
                htm.addln("Invalid video request.");
                htm.eDiv();
            } else {
                htm.addln("<script>");
                htm.addln(" function showReportError() {");
                htm.addln("  document.getElementById('error_rpt_link').className='hidden';");
                htm.addln("  document.getElementById('error_rpt').className='visible';");
                htm.addln(" }");
                htm.addln("</script>");

                htm.sDiv("indent11");
                if (unitStr != null && lessonStr != null) {
                    htm.sP().add("<a href='lesson.html?unit=", unitStr, "&lesson=", lessonStr,
                            "'>Return to lesson</a>").br().eP();
                }

                htm.addln("<video width='640' height='480' controls='controls' autoplay='autoplay'>");
                htm.addln(" <source src='", STREAM, dir, "/mp4/", mediaId, ".mp4' type='video/mp4'/>");
                htm.addln(" <source src='", STREAM, dir, "/ogv/", mediaId, ".ogv' type='video/ogg'/>");
                htm.addln(" <track src='/www/math/", dir, "/vtt/", mediaId, ".vtt' kind='subtitles' srclang='en' ",
                        "label='English' default='default'/>");
                htm.addln(" Your browser does not support inline video.");
                htm.addln("</video>");

                htm.addln("<div><a href='/www/math/", dir, "/transcripts/", mediaId, ".pdf'>",
                        "Access a plain-text transcript for screen-readers (Adobe PDF).</a></div>");

                htm.sDiv().add("<a href='", STREAM, dir, "/pdf/",
                        mediaId, ".pdf'>Access a static (Adobe PDF) version.</a>").eDiv();

                if (unitStr != null && lessonStr != null) {

                    // If the student already provided feedback, prime the field with that data
                    final File file =
                            feedbackFile(unitStr, lessonStr, mediaId, session.getEffectiveUserId());

                    htm.sDiv("visible", "id='error_rpt_link'");
                    htm.addln("<a href='#' onClick='showReportError();'>",
                            file.exists() ? "Edit your existing error report or recommendation..."
                                    : "Report an error or recommend an improvement...",
                            "</a>").eDiv();

                    htm.sP();
                    htm.addln("<form class='hidden' id='error_rpt' action='media_feedback.html' method='post'>");
                    htm.addln(" <input type='hidden' name='course' value='M 100T'/>");
                    htm.addln(" <input type='hidden' name='unit' value='", unitStr, "'/>");
                    htm.addln(" <input type='hidden' name='lesson' value='", lessonStr, "'/>");
                    htm.addln(" <input type='hidden' name='media' value='", mediaId, "'/>");
                    htm.addln(" Please describe the error or recommend an improvement:<br/>");
                    htm.addln(" <textarea rows='5' cols='40' name='comments'>");
                    if (file.exists()) {
                        htm.addln(XmlEscaper.escape(FileLoader.loadFileAsString(file, true)));
                    }
                    htm.addln(" </textarea>").br();
                    htm.addln(" <input type='submit' value='Submit'/>");
                    htm.addln("</form>").eP();
                    htm.eDiv();
                }
            }

            htm.eDiv(); // panel
            htm.eDiv(); // menupanel
            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Generates a file that represents student feedback on a video.
     *
     * @param unit     the ID of the unit
     * @param lessonId the ID of the lesson
     * @param mediaId  the ID of the media object
     * @param userId   the ID of the user providing feedback
     * @return the file where feedback is stored
     */
    private static File feedbackFile(final String unit, final String lessonId, final String mediaId,
                                     final String userId) {

        final File baseDir = PathList.getInstance().getBaseDir();
        final File dir = new File(baseDir, "feedback");

        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Log.warning("Failed to create feedback directory");
            }
        }

        return new File(dir, "M100T_" + unit + "_" + lessonId + "_" + mediaId + "_" + userId + ".txt");
    }
}
