package dev.mathops.web.site.tutorial.precalc;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.PathList;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.BasicCss;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * A course site for "placement.math.colostate.edu/precalc-tutorial".
 */
public final class PrecalcTutorialSite extends AbstractPageSite {

    /** Correct passwords - note that ProctorU does not allow tildes. */
    private static final String[] CORRECT = {"AfumwaviLiz7", "UpqoujziAki6", "EvjiosgoAwoq"};

    /**
     * Constructs a new {@code PrecalcTutorialSite}.
     *
     * @param theSiteProfile the website profile
     * @param theSessions    the singleton user session repository
     */
    public PrecalcTutorialSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);
    }

    /**
     * Tests the live refresh policy for this site.
     *
     * @return the live refresh policy
     */
    @Override
    protected ELiveRefreshes getLiveRefreshes() {

        return ELiveRefreshes.ALL;
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
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public void doGet(final Cache cache, final String subpath, final ESiteType type,
                      final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        if (CoreConstants.EMPTY.equals(subpath)) {
            final String path = this.siteProfile.path;
            resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH) ? "index.html" : "/index.html"));
        } else if ("basestyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
        } else if ("style.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(PrecalcTutorialSite.class, "style.css", true));
        } else if ("course.css".equals(subpath)) {
            BasicCss.getInstance().serveCss(req, resp);
        } else if ("lesson.css".equals(subpath)) {
            LessonCss.getInstance().serveCss(req, resp);
        } else if (subpath.startsWith("lessons/")) {
            serveLesson(subpath.substring(8), req, resp);
        } else if (subpath.startsWith("images/")) {
            serveImage(subpath.substring(7), req, resp);
        } else if (subpath.endsWith(".vtt")) {
            serveVtt(subpath, req, resp);
        } else if ("favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else {
            final String maintenanceMsg = isMaintenance(this.siteProfile);

            if (maintenanceMsg == null) {
                if ("index.html".equals(subpath) || "login.html".equals(subpath)) {
                    PageLogin.doGet(cache, this, req, resp);
                } else {
                    // The pages that follow require the user to be logged in
                    final ImmutableSessionInfo session = validateSession(req, resp, null);

                    if (session == null) {
                        if ("secure/shibboleth.html".equals(subpath)) {
                            doShibbolethLogin(cache, req, resp, null, "home.html");
                        } else {
                            Log.warning("Unrecognized GET request path: ", subpath);
                            final String path = this.siteProfile.path;
                            resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH)
                                    ? "index.html" : "/index.html"));
                        }
                    } else {
                        LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

                        final PrecalcTutorialSiteLogic logic = new PrecalcTutorialSiteLogic(session, cache);

                        switch (subpath) {
                            case "logout.html" -> doLogout(req, resp, session);
                            case "secure/shibboleth.html" -> doShibbolethLogin(cache, req, resp, session, "home.html");
                            case "home.html" -> PageHome.doGet(cache, this, req, resp, session, logic);
                            case "onlinehelp.html" ->
                                    PageOnlineHelp.doOnlineHelpPage(cache, this, req, resp, session, logic);
                            case "course.html" -> PageCourseOutline.doGet(cache, this, req, resp, session, logic);
                            case "lesson.html" -> PageCourseLesson.doGet(cache, this, req, resp, session, logic);
                            case "elm_lesson.html" -> PageElmUnit.doGet(cache, this, req, resp, session, logic);
                            case "course_status.html" -> PageCourseStatus.doGet(cache, this, req, resp, session, logic);
                            case "placement_report.html" ->
                                    PagePlacementReport.doGet(cache, this, req, resp, session, logic);
                            case "proctor_login.html" -> PageProctorLogin.doGet(cache, this, req, resp, session);
                            case "video.html" -> PageVideo.doGet(cache, this, req, resp, session, logic);
                            case "run_homework.html" -> PageHtmlHomework.startHomework(cache, this, req, resp, session);
                            case "run_review.html" ->
                                    PageHtmlReviewExam.startReviewExam(cache, this, req, resp, session);
                            case "update_homework.html" -> {
                                Log.warning("GET of update_homework.html");
                                PageHtmlHomework.updateHomework(cache, this, req, resp, session);
                            }
                            case "update_review_exam.html" -> {
                                Log.warning("GET of update_review_exam.html");
                                PageHtmlReviewExam.updateReviewExam(cache, this, req, resp, session);
                            }
                            case "update_past_exam.html" -> {
                                Log.warning("GET of update_past_exam.html");
                                PageHtmlPastExam.updatePastExam(cache, this, req, resp, session);
                            }
                            case "update_unit_exam.html" -> {
                                Log.warning("GET of update_unit_exam.html");
                                PageHtmlUnitExam.updateUnitExam(cache, this, req, resp, session);
                            }
                            case "run_unit.html" -> PageHtmlUnitExam.startUnitExam(cache, this, req, resp, session);
                            case "see_past_exam.html" ->
                                    PageHtmlPastExam.startPastExam(cache, this, req, resp, session);
                            case "taking_exam_precalc.html" ->
                                    PageTakingExamPrecalc.doGet(cache, this, req, resp, session);
                            case "instructions_precalc_pu.html" ->
                                    PageInstructionsPrecalcPu.doGet(cache, this, req, resp, session, logic);
                            case "instructions_precalc_tc.html" ->
                                    PageInstructionsPrecalcTc.doGet(cache, this, req, resp, session, logic);
                            case "schedule_precalc_pu.html" ->
                                    PageSchedulePrecalcPu.doGet(cache, this, req, resp, session, logic);
                            default -> {
                                Log.warning("Unrecognized GET request path: ", subpath);
                                final String path = this.siteProfile.path;
                                resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH)
                                        ? "index.html" : "/index.html"));
                            }
                        }
                    }
                }
            } else {
                PageMaintenance.doMaintenancePage(cache, this, req, resp, maintenanceMsg);
            }
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
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public void doPost(final Cache cache, final String subpath, final ESiteType type, final HttpServletRequest req,
                       final HttpServletResponse resp) throws IOException, SQLException {

        final String maintenanceMsg = isMaintenance(this.siteProfile);

        if (maintenanceMsg == null) {
            if ("login.html".equals(subpath)) {
                PageLogin.doGet(cache, this, req, resp);
            } else {
                // The pages that follow require the user to be logged in
                final ImmutableSessionInfo session = validateSession(req, resp, "index.html");

                if (session != null) {
                    LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

                    final PrecalcTutorialSiteLogic logic = new PrecalcTutorialSiteLogic(session, cache);

                    switch (subpath) {
                        case "rolecontrol.html" -> processRoleControls(cache, req, resp, session);
                        case "home.html" -> PageHome.doGet(cache, this, req, resp, session, logic);
                        case "media_feedback.html" -> doMediaFeedback(cache, req, resp, session, logic);
                        case "update_homework.html" -> PageHtmlHomework.updateHomework(cache, this, req, resp, session);
                        case "update_review_exam.html" ->
                                PageHtmlReviewExam.updateReviewExam(cache, this, req, resp, session);
                        case "update_past_exam.html" ->
                                PageHtmlPastExam.updatePastExam(cache, this, req, resp, session);
                        case "update_unit_exam.html" ->
                                PageHtmlUnitExam.updateUnitExam(cache, this, req, resp, session);
                        case "process_proctor_login.html" -> doProcessProctorLogin(req, resp);
                        case null, default -> {
                            Log.warning("Unrecognized POST request path: ", subpath);
                            final String path = this.siteProfile.path;
                            resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH) ? "index.html" : "/index.html"));
                        }
                    }

                    LogBase.setSessionInfo(session.loginSessionId, null);
                }
            }
        } else {
            PageMaintenance.doMaintenancePage(cache, this, req, resp, maintenanceMsg);
        }
    }

    /**
     * Generates the site title based on the context.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "Precalculus Tutorial";
    }

    /**
     * Tests whether the password entered by the proctor is correct.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void doProcessProctorLogin(final ServletRequest req,
                                              final HttpServletResponse resp) throws IOException {

        final String course = req.getParameter("course");
        final String exam = req.getParameter("exam");
        final String password = req.getParameter("drowssap");

        boolean match = false;

        for (final String test : CORRECT) {
            if (test.equals(password)) {
                match = true;
                break;
            }
        }

        if (match) {
            resp.sendRedirect("taking_exam_precalc.html");
        } else {
            resp.sendRedirect("proctor_login.html?course=" + course + "&exam=" + exam + "&error=Invalid%20password.");
        }
    }

    /**
     * Handles submission of feedback on a media object.
     *
     * @param cache   the data cache
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void doMediaFeedback(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                                 final ImmutableSessionInfo session, final PrecalcTutorialSiteLogic logic)
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
            Page.startOrdinaryPage(htm, getTitle(), session, false, "Precalculus Tutorial",
                    "/precalc-tutorial/home.html", Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            htm.sDiv("menupanel");
            TutorialMenu.buildMenu(session, logic, htm);
            htm.sDiv("panel");

            final File file = feedbackFile(courseId, unit, lessonId, mediaId, session.getEffectiveUserId());

            try (final FileWriter wri = new FileWriter(file, StandardCharsets.UTF_8)) {
                wri.write(req.getParameter("comments"));

                htm.sP().br().add("Thank you for your feedback.  Your comments have been sent to ",
                        "our course development team.").eP();
            } catch (final IOException ex) {
                Log.warning("Error posting feedback", ex);
                htm.sP().br().add("There was an error sending your feedback to the development team.").eP();
            }

            htm.sP();
            htm.addln(" <a href='lesson.html?course=", courseId, "&unit=", unit, "&lesson=", lessonId, "&mode=", mode,
                    "#", mediaId, "'>Return to lesson</a>").br();
            htm.eP();

            htm.eDiv(); // (end "panel" div)
            htm.eDiv(); // (end "menupanel" div)

            Page.endOrdinaryPage(cache, this, htm, true);

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
    private static File feedbackFile(final String courseId, final String unit, final String lessonId,
                                     final String mediaId, final String userId) {

        final File baseDir = PathList.getInstance().baseDir;
        final File dir = new File(baseDir, "feedback");

        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Log.warning("Failed to create feedback directory.");
            }
        }

        return new File(dir, courseId + "_" + unit + "_" + lessonId + "_" + mediaId + "_" + userId + ".txt");
    }
}
