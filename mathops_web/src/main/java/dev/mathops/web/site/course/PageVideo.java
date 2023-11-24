package dev.mathops.web.site.course;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.PathList;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.xml.XmlEscaper;
import dev.mathops.db.Cache;
import dev.mathops.db.rawlogic.RawStcuobjectiveLogic;
import dev.mathops.db.rawrecord.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
     * @param session the user's login session information (could be null)
     * @param logic   the course site logic (null if session is null)
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final CourseSiteLogic logic) throws IOException, SQLException {

        final String mediaId = req.getParameter("media-id");
        final String course = req.getParameter("course");
        final String unitStr = req.getParameter("unit");
        final String lessonStr = req.getParameter("lesson");
        final String mode = req.getParameter("mode");
        final String srcourse = req.getParameter("srcourse");

        if (AbstractSite.isParamInvalid(mediaId) || AbstractSite.isParamInvalid(course)
                || AbstractSite.isParamInvalid(unitStr) || AbstractSite.isParamInvalid(lessonStr)
                || AbstractSite.isParamInvalid(mode) || AbstractSite.isParamInvalid(srcourse)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  media-id='", mediaId, "'");
            Log.warning("  course='", course, "'");
            Log.warning("  unit='", unitStr, "'");
            Log.warning("  lesson='", lessonStr, "'");
            Log.warning("  mode='", mode, "'");
            Log.warning("  srcourse='", srcourse, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            // For the tutorial courses, we don't want to force duplication of the video files in a
            // new directory, so map those course numbers to the corresponding non-tutorial courses
            final String actualCourse;
            if (RawRecordConstants.M1170.equals(course)) {
                actualCourse = RawRecordConstants.M117;
            } else if (RawRecordConstants.M1180.equals(course)) {
                actualCourse = RawRecordConstants.M118;
            } else if (RawRecordConstants.M1240.equals(course)) {
                actualCourse = RawRecordConstants.M124;
            } else if (RawRecordConstants.M1250.equals(course)) {
                actualCourse = RawRecordConstants.M125;
            } else if (RawRecordConstants.M1260.equals(course)) {
                actualCourse = RawRecordConstants.M126;
            } else {
                actualCourse = course;
            }

            final String dir = actualCourse == null ? null
                    : actualCourse.replace(CoreConstants.SPC, CoreConstants.EMPTY);

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, null, null,
                    Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            if (session != null && logic != null) {
                htm.sDiv("menupanelu");
                CourseMenu.buildMenu(cache, site, session, logic, htm);
                htm.sDiv("panelu");

                // If this is an instructor lecture, record that the student has viewed it.
                final String studentId = session.getEffectiveUserId();
                if (studentId != null && mediaId != null && actualCourse != null && unitStr != null
                        && lessonStr != null && mediaId.endsWith("OV")) {

                    try {
                        RawStcuobjectiveLogic.recordLectureView(cache, studentId, actualCourse,
                                Integer.valueOf(unitStr), Integer.valueOf(lessonStr), session.getNow());
                    } catch (final NumberFormatException ex) {
                        Log.warning("Failed to record lecture view for student ", studentId, " media ID ", mediaId);
                    }
                }
            }

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
                if (unitStr != null && lessonStr != null && mode != null) {
                    htm.sP();
                    htm.add("<a href='lesson.html?course=", course,
                            "&unit=", unitStr, "&lesson=", lessonStr,
                            "&mode=", mode);
                    if (srcourse != null) {
                        htm.add("&srcourse=", srcourse);
                    }
                    htm.addln("#", mediaId, "'>Return to lesson</a>").br().eP();
                }

                htm.addln("<video ", "M160".equals(actualCourse)
                                ? "width='1024' height='768'"
                                : "width='640' height='480'",
                        " controls='controls' autoplay='autoplay'>");
                htm.addln(" <source src='", STREAM, dir, "/mp4/",
                        mediaId, ".mp4' type='video/mp4'/>");
                htm.addln(" <source src='", STREAM, dir, "/ogv/",
                        mediaId, ".ogv' type='video/ogg'/>");
                htm.addln(" <track src='/www/math/", dir, "/vtt/",
                        mediaId, ".vtt' kind='subtitles' srclang='en' ",
                        "label='English' default='default'/>");
                htm.addln(" Your browser does not support inline video.");
                htm.addln("</video>");

                htm.addln("<div><a href='/math/", dir,
                        "/transcripts/", mediaId, ".pdf'>",
                        "Access a plain-text transcript for screen-readers (Adobe PDF).", //
                        "</a></div>");

                htm.sDiv().add("<a href='", STREAM, dir, "/pdf/",
                        mediaId, ".pdf'>Access a static (Adobe PDF) version.</a>").eDiv();

                if (unitStr != null && lessonStr != null && mode != null) {

                    final String stuId = session == null ? "anon"
                            : session.getEffectiveUserId();
                    final File file = feedbackFile(course, unitStr, lessonStr, mediaId, stuId);

                    htm.sDiv("visible", "id='error_rpt_link'");
                    htm.addln("<a href='#' onClick='showReportError();'>",
                            session != null && file.exists()
                                    ? "Edit your existing error report or recommendation..."
                                    : "Report an error or recommend an improvement...",
                            "</a>").eDiv();

                    htm.sP();
                    htm.addln("<form class='hidden' id='error_rpt' ",
                            "action='media_feedback.html' method='post'>");
                    htm.addln(" <input type='hidden' name='course' value='",
                            course, "'/>");
                    htm.addln(" <input type='hidden' name='unit' value='",
                            unitStr, "'/>");
                    htm.addln(" <input type='hidden' name='lesson' value='",
                            lessonStr, "'/>");
                    htm.addln(" <input type='hidden' name='media' value='",
                            mediaId, "'/>");
                    htm.addln(" <input type='hidden' name='mode' value='",
                            mode, "'/>");
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
            }

            if (session != null && logic != null) {
                htm.eDiv(); // panelu
                htm.eDiv(); // menupanelu
            }

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                    htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Handles submission of feedback on a media object.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doMediaFeedback(final Cache cache, final CourseSite site,
                                final ServletRequest req, final HttpServletResponse resp,
                                final ImmutableSessionInfo session, final CourseSiteLogic logic)
            throws IOException, SQLException {

        final String courseId = req.getParameter("course");
        final String unit = req.getParameter("unit");
        final String lessonId = req.getParameter("lesson");
        final String mediaId = req.getParameter("media");
        final String mode = req.getParameter("mode");

        if (AbstractSite.isParamInvalid(courseId) || AbstractSite.isParamInvalid(unit)
                || AbstractSite.isParamInvalid(lessonId) || AbstractSite.isParamInvalid(mediaId)
                || AbstractSite.isParamInvalid(mode)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", courseId, "'");
            Log.warning("  unit='", unit, "'");
            Log.warning("  lesson='", lessonId, "'");
            Log.warning("  srcourse='", mediaId, "'");
            Log.warning("  mode='", mode, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, null, null,
                    Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            if (session != null && logic != null) {
                htm.sDiv("menupanelu");
                CourseMenu.buildMenu(cache, site, session, logic, htm);
                htm.sDiv("panelu");
            }

            final File file = feedbackFile(courseId, unit, lessonId, mediaId,
                    session == null ? "anon" : session.getEffectiveUserId());

            try (final FileWriter wri = new FileWriter(file, StandardCharsets.UTF_8)) {
                wri.write(req.getParameter("comments"));

                htm.sP().br().add("Thank you for your feedback.  Your comments have been sent to ",
                        "our course development team.").eP();
            } catch (final IOException ex) {
                Log.warning("Error posting feedback", ex);
                htm.sP().br().add("There was an error sending your feedback to the development team.").eP();
            }

            htm.sP();
            htm.addln(" <a href='lesson.html?course=", courseId, "&unit=", unit, "&lesson=", lessonId,
                    "&mode=", mode, "#", mediaId, "'>Return to lesson</a>").br();
            htm.eP();

            if (session != null && logic != null) {
                htm.eDiv(); // panelu
                htm.eDiv(); // menupanelu
            }

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                    htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Generates a file that represents student feedback on a video.
     *
     * @param courseId the ID of the course
     * @param unit     the ID of the unit
     * @param lessonId the ID of the lesson
     * @param mediaId  the ID of the media object
     * @param userId   the ID of the user providing feedback
     * @return the file where feedback is stored
     */
    private static File feedbackFile(final String courseId, final String unit,
                                     final String lessonId, final String mediaId, final String userId) {

        final File baseDir = PathList.getInstance().baseDir;
        final File dir = new File(baseDir, "feedback");

        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Log.warning("Failed to create feedback directory ", dir.getAbsolutePath());
            }
        }

        return new File(dir, courseId + "_" + unit + "_" + lessonId + "_" + mediaId + "_" + userId + ".txt");
    }
}
