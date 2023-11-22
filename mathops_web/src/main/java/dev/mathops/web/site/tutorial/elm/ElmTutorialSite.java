package dev.mathops.web.site.tutorial.elm;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.PathList;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.WebSiteProfile;
import dev.mathops.db.logic.ELMTutorialStatus;
import dev.mathops.db.logic.HoldsStatus;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;

import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * A course site for "placement.math.colostate.edu/elm-tutorial".
 */
public final class ElmTutorialSite extends AbstractPageSite {

    /** Proctor passwords (one for ProctorU, one for UTC, one for Honorlock). */
    private static final String[] CORRECT = {"AfumwaviLiz7", "UpqoujziAki6", "EvjiosgoAwoq",
            "Racibetu", "Big7oge7", "Hetebife"};

    /**
     * Constructs a new {@code ElmCourseSite}.
     *
     * @param theSiteProfile the website profile
     * @param theSessions    the singleton user session repository
     */
    public ElmTutorialSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);
    }

    /**
     * Initializes the site - called when the servlet is initialized.
     *
     * @param config the servlet context in which the servlet is being in
     */
    @Override
    public void init(final ServletConfig config) {

        // No action
    }

    /**
     * Indicates whether this site should do live queries to update student registration data.
     *
     * @return true to do live registration queries; false to skip
     */
    @Override
    public boolean doLiveRegQueries() {

        return true;
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
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public void doGet(final Cache cache, final String subpath, final ESiteType type,
                      final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        if (CoreConstants.EMPTY.equals(subpath)) {
            final String path = this.siteProfile.path;
            resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH) //
                    ? "index.html" : "/index.html"));
        } else if ("basestyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css",
                    FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
        } else if ("style.css".equals(subpath)) {
            sendReply(req, resp, "text/css",
                    FileLoader.loadFileAsBytes(ElmTutorialSite.class, "style.css", true));
        } else if (subpath.startsWith("images/")) {
            serveImage(subpath.substring(7), req, resp);
        } else if (subpath.endsWith(".vtt")) {
            serveVtt(subpath, req, resp);
        } else if ("favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else {
            final String maintMsg = isMaintenance(this.siteProfile);

            if (maintMsg == null) {
                if ("index.html".equals(subpath)
                        || "login.html".equals(subpath)) {
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
                        final String studentId = session.getEffectiveUserId();

                        LogBase.setSessionInfo(session.loginSessionId, studentId);

                        final ELMTutorialStatus status = ELMTutorialStatus.of(cache, studentId,
                                session.getNow(), HoldsStatus.of(cache, studentId));

                        switch (subpath) {
                            case "secure/shibboleth.html" -> doShibbolethLogin(cache, req, resp, session, "home.html");
                            case "home.html" -> PageHome.doGet(cache, this, req, resp, session, status);
                            case "placement_report.html" ->
                                    PagePlacementReport.doGet(cache, this, req, resp, session, status);
                            case "onlinehelp.html" -> PageOnlineHelp.doGet(cache, this, req, resp, session, status);
                            case "tutorial.html" -> PageOutline.doGet(cache, this, req, resp, session, status);
                            case "lesson.html" -> PageLesson.doGet(cache, this, req, resp, session, status);
                            case "tutorial_status.html" -> PageStatus.doGet(cache, this, req, resp, session, status);
                            case "video.html" -> PageVideo.doGet(cache, this, req, resp, session, status);
                            case "run_homework.html" ->
                                    PageHtmlPractice.startHomework(cache, this, req, resp, session, status);
                            case "update_homework.html" -> {
                                Log.warning("GET of update_homework.html");
                                PageHtmlPractice.updateHomework(cache, this, req, resp, session, status);
                            }
                            case "run_review.html" ->
                                    PageHtmlReviewExam.startReviewExam(cache, this, req, resp, session, status);
                            case "update_review_exam.html" -> {
                                Log.warning("GET of update_review_exam.html");
                                PageHtmlReviewExam.updateReviewExam(cache, this, req, resp, session, status);
                            }
                            case "see_past_exam.html" ->
                                    PageHtmlPastExam.startPastExam(cache, this, req, resp, session, status);
                            case "update_past_exam.html" -> {
                                Log.warning("GET of update_past_exam.html");
                                PageHtmlPastExam.updatePastExam(cache, this, req, resp, session, status);
                            }
                            case "run_unit.html" -> PageHtmlUnitExam.startUnitExam(cache, this, req, resp, session);
                            case "update_unit_exam.html" -> {
                                Log.warning("GET of update_unit_exam.html");
                                PageHtmlUnitExam.updateUnitExam(cache, this, req, resp, session);
                            }
                            case "instructions_elm_tc.html" ->
                                    PageInstructionsElmTC.doGet(cache, this, req, resp, session, status);
                            case "instructions_elm_pu.html" ->
                                    PageInstructionsElmPU.doGet(cache, this, req, resp, session, status);
                            case "schedule_elm_pu.html" ->
                                    PageScheduleElmPU.doGet(cache, this, req, resp, session, status);
                            case "start_elm_pu.html" -> PageStartElmPU.doGet(cache, this, req, resp, session, status);
                            case "proctor_login_elm.html" -> PageProctorLoginElm.doGet(cache, this, req, resp, session);
                            case "taking_exam_elm.html" -> PageTakingExamElm.doGet(cache, this, req, resp, session);
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
                PageMaintenance.doGet(cache, this, req, resp, maintMsg);
            }
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
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public void doPost(final Cache cache, final String subpath, final ESiteType type,
                       final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        final String maintMsg = isMaintenance(this.siteProfile);

        if (maintMsg == null) {
            if ("login.html".equals(subpath)) {
                PageLogin.doGet(cache, this, req, resp);
            } else {
                // The pages that follow require the user to be logged in
                final ImmutableSessionInfo session = validateSession(req, resp, //
                        "index.html");

                if (session != null) {
                    final String studentId = session.getEffectiveUserId();

                    LogBase.setSessionInfo(session.loginSessionId, studentId);

                    final ELMTutorialStatus status = ELMTutorialStatus.of(cache, studentId,
                            session.getNow(), HoldsStatus.of(cache, studentId));

                    if ("rolecontrol.html".equals(subpath)) {
                        processRoleControls(cache, req, resp, session);
                    } else if ("home.html".equals(subpath)) {
                        PageHome.doGet(cache, this, req, resp, session, status);
                    } else if ("media_feedback.html".equals(subpath)) {
                        doMediaFeedback(cache, req, resp, session, status);
                    } else if ("update_homework.html".equals(subpath)) {
                        PageHtmlPractice.updateHomework(cache, this, req, resp, session, status);
                    } else if ("update_review_exam.html".equals(subpath)) {
                        PageHtmlReviewExam.updateReviewExam(cache, this, req, resp, session,
                                status);
                    } else if ("update_past_exam.html".equals(subpath)) {
                        PageHtmlPastExam.updatePastExam(cache, this, req, resp, session, status);
                    } else if ("update_unit_exam.html".equals(subpath)) {
                        PageHtmlUnitExam.updateUnitExam(cache, this, req, resp, session);
                    } else if ("process_proctor_login_elm.html".equals(subpath)) {
                        doProcessProctorLoginElm(req, resp);
                    } else {
                        Log.warning("Unrecognized POST request path: ", subpath);
                        final String path = this.siteProfile.path;
                        resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH) //
                                ? "index.html" : "/index.html"));
                    }
                }
            }
        } else {
            PageMaintenance.doGet(cache, this, req, resp, maintMsg);
        }
    }

    /**
     * Handles submission of feedback on a media object.
     *
     * @param cache   the data cache
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param status  the student status with respect to the ELM Tutorial
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void doMediaFeedback(final Cache cache, final ServletRequest req,
                                 final HttpServletResponse resp, final ImmutableSessionInfo session,
                                 final ELMTutorialStatus status) throws IOException, SQLException {

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
            Page.startOrdinaryPage(htm, getTitle(), session, false, Page.ADMIN_BAR, null, false,
                    true);

            htm.sDiv("menupanel");
            TutorialMenu.buildMenu(cache, session, status, htm);
            htm.sDiv("panel");

            final File file =
                    feedbackFile(courseId, unit, lessonId, mediaId, session.getEffectiveUserId());

            try (final FileWriter wri = new FileWriter(file, StandardCharsets.UTF_8)) {
                wri.write(req.getParameter("comments"));

                htm.sP().br().add("Thank you for your feedback.  Your comments have ",
                        "been sent to our course development team.").eP();
            } catch (final IOException ex) {
                Log.warning("Error posting feedback", ex);
                htm.sP().br()
                        .add("There was an error sending your feedback to the development team.").eP();
            }

            htm.sP();
            htm.addln(" <a href='lesson.html?course=", courseId,
                    "&unit=", unit, "&lesson=", lessonId,
                    "&mode=", mode, "#", mediaId,
                    "'>Return to lesson</a>").br();
            htm.eP();

            htm.eDiv(); // (end "panel" div)
            htm.eDiv(); // (end "menupanel" div)
            Page.endOrdinaryPage(cache, this, htm, true);

            sendReply(req, resp, Page.MIME_TEXT_HTML,
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
                Log.warning("Failed to create feedback directory");
            }
        }

        return new File(dir, courseId + "_" + unit + "_" + lessonId + "_" + mediaId + "_" + userId + ".txt");
    }

    /**
     * Generates the site title based on the context.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "ELM Tutorial";
    }

    /**
     * Tests whether the password entered by the proctor is correct.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void doProcessProctorLoginElm(final ServletRequest req,
                                                 final HttpServletResponse resp) throws IOException {

        final String password = req.getParameter("drowssap");

        boolean match = false;
        for (final String test : CORRECT) {
            if (test.equals(password)) {
                match = true;
                break;
            }
        }

        if (match) {
            resp.sendRedirect("taking_exam_elm.html");
        } else {
            resp.sendRedirect("proctor_login_elm.html?error=Invalid%20password.");
        }
    }
}
