package dev.mathops.web.site.course;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.BasicCss;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * A course site for resident precalculus courses.
 */
public class CourseSite extends AbstractPageSite {

    /** Proctor passwords (one for ProctorU, one for UTC, one for Honorlock). */
    private static final String[] CORRECT = {"AfumwaviLiz7", "UpqoujziAki6", "EvjiosgoAwoq"};

    /**
     * Constructs a new {@code CourseSite}.
     *
     * @param theSiteProfile the website profile
     * @param theSessions    the singleton user session repository
     */
    public CourseSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

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
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param data    the web view data
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doGet(final WebViewData data, final String subpath, final ESiteType type, final HttpServletRequest req,
                      final HttpServletResponse resp) throws IOException, SQLException {

        // Log.info("GET ", subpath);

        if (CoreConstants.EMPTY.equals(subpath)) {
            final String path = this.siteProfile.path;
            resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH) ? "index.html" : "/index.html"));
        } else if ("basestyle.css".equals(subpath)) {
            final byte[] bytes = FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true);
            sendReply(req, resp, "text/css", bytes);
        } else if ("style.css".equals(subpath)) {
            final byte[] bytes = FileLoader.loadFileAsBytes(CourseSite.class, "style.css", true);
            sendReply(req, resp, "text/css", bytes);
        } else if ("course.css".equals(subpath)) {
            Log.info("***GET course.css in CourseSite");
            BasicCss.getInstance().serveCss(req, resp);
        } else if ("lesson.css".equals(subpath)) {
            Log.info("***GET lesson.css in CourseSite");
            LessonCss.getInstance().serveCss(req, resp);
        } else if (subpath.startsWith("lessons/")) {
            Log.info("***GET a lesson in CourseSite");
            final String substring = subpath.substring(8);
            serveLesson(substring, req, resp);
        } else if (subpath.startsWith("images/")) {
            final String substring = subpath.substring(7);
            serveImage(substring, req, resp);
        } else if (subpath.endsWith(".vtt")) {
            serveVtt(subpath, req, resp);
        } else if ("favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else {
            final String maintenanceMsg = isMaintenance(this.siteProfile);

            if (maintenanceMsg == null) {
                // The pages that follow require the user to be logged in
                final ImmutableSessionInfo session = validateSession(req, resp, null);

                if (session == null) {
                    switch (subpath) {
                        case "index.html", "login.html" -> {
                            // Send back to the root site
                            final String path = this.siteProfile.path;
                            resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH) ? "index.html"
                                    : "/index.html"));
                        }
                        case "video.html" -> PageVideoExample.doGet(this, req, resp, null);
                        case "orientation.html" -> PageOrientation.doGet(data, this, req, resp, null, null);
                        case "secure/shibboleth.html" -> doShibbolethLogin(data.getCache(), req, resp, null,
                                "home.html");
                        default -> {
                            Log.warning("Unrecognized GET request path: ", subpath);
                            resp.sendRedirect("/index.html");
                        }
                    }
                } else {
                    LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

                    // TODO: Replace all logic with this

                    final CourseSiteLogic logic = new CourseSiteLogic(this.siteProfile, session);
                    logic.gatherData();

                    switch (subpath) {
                        case "index.html", "home.html" -> PageHome.doGet(data, this, req, resp, session, logic);
                        case "secure/shibboleth.html" ->
                                doShibbolethLogin(data.getCache(), req, resp, session, "home.html");
                        case "orientation.html" -> PageOrientation.doGet(data, this, req, resp, session, logic);
                        case "schedule.html" -> PageSchedule.doGet(data, this, req, resp, session, logic);
                        case "calendar.html" -> PageCalendar.doGet(data, this, req, resp, session, logic);
                        case "calendar_print.html" -> PageCalendar.doGetPrintable(data, this, req, resp, logic);
                        case "onlinehelp.html" -> PageGettingHelp.doGet(data, this, req, resp, session, logic);
                        case "users_exam.html" -> PageUsersExam.doGet(data, this, req, resp, session, logic);
                        case "etexts.html" -> PageETexts.doETextsPage(data, this, req, resp, session, logic);
                        case "start_course.html" -> PageStartCourse.doGet(data, this, req, resp, session, logic);
                        case "course.html" -> PageOutline.doGet(data, type, this, req, resp, session, logic);

                        case "course_media.html" -> PageStdsTextMedia.doGet(data, this, req, resp, session, logic);
                        case "course_status.html" -> PageCourseStatus.doGet(data, this, req, resp, session, logic);
                        case "course_text.html" -> PageStdsText.doGet(data, this, req, resp, session, logic);
                        case "course_text_module.html" ->
                                PageStdsTextModule.doGet(data, this, req, resp, session, logic);

                        case "skills_review.html" ->
                                PageSkillsReview.doGet(data, type, this, req, resp, session, logic);
                        case "lesson.html" -> PageLesson.doGet(data, this, req, resp, session, logic);
                        case "placement_report.html" ->
                                PagePlacementReport.doGet(data, this, req, resp, session, logic);
                        case "proctor_login.html" -> PageProctorLogin.doGet(data, this, req, resp, session);
                        case "video.html" -> PageVideo.doGet(data, this, req, resp, session, logic);
                        case "video_example.html" -> PageVideoExample.doGet(this, req, resp, session);
                        case "run_homework.html" ->
                                PageHtmlHomework.startHomework(data, this, req, resp, session, logic);
                        case "run_lta.html" -> PageHtmlLta.startLta(data, this, req, resp, session, logic);
                        case "run_review.html" ->
                                PageHtmlReviewExam.startReviewExam(data, this, req, resp, session, logic);
                        case "update_homework.html" ->
                                PageHtmlHomework.updateHomework(data, this, req, resp, session, logic);
                        case "update_lta.html" -> PageHtmlLta.updateLta(data, this, req, resp, session, logic);
                        case "update_review_exam.html" ->
                                PageHtmlReviewExam.updateReviewExam(data, this, req, resp, session, logic);
                        case "update_past_exam.html" ->
                                PageHtmlPastExam.updatePastExam(data, this, req, resp, session, logic);
                        case "update_past_lta.html" ->
                                PageHtmlPastLta.updatePastLta(data, this, req, resp, session, logic);
                        case "update_unit_exam.html" ->
                                PageHtmlUnitExam.updateUnitExam(data, this, req, resp, session, logic);
                        case "run_unit.html" -> PageHtmlUnitExam.startUnitExam(data, this, req, resp, session, logic);
                        case "see_past_exam.html" ->
                                PageHtmlPastExam.startPastExam(data, this, req, resp, session, logic);
                        case "see_past_lta.html" -> PageHtmlPastLta.startPastLta(data, this, req, resp, session, logic);
                        default -> {
                            Log.warning("Unrecognized GET request path: ", subpath);
                            final String path = this.siteProfile.path;
                            resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH)
                                    ? "index.html" : "/index.html"));
                        }
                    }
                }
            } else {
                PageMaintenance.doGet(data, this, req, resp, maintenanceMsg);
            }
        }
    }

    /**
     * Processes a POST request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param data    the web view data
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public void doPost(final WebViewData data, final String subpath, final ESiteType type,
                       final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        final String maintMsg = isMaintenance(this.siteProfile);

        if (maintMsg == null) {
            final ImmutableSessionInfo session = validateSession(req, resp, "index.html");

            if (session != null) {
                LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

                final CourseSiteLogic logic = new CourseSiteLogic(this.siteProfile, session);
                logic.gatherData();

                if ("rolecontrol.html".equals(subpath)) {
                    processRoleControls(data, req, resp, session);
                } else if ("home.html".equals(subpath)) {
                    PageHome.doGet(data, this, req, resp, session, logic);
                } else if ("media_feedback.html".equals(subpath)) {
                    PageVideo.doMediaFeedback(data, this, req, resp, session, logic);
                } else if ("example_feedback.html".equals(subpath)) {
                    PageVideoExample.doExampleFeedback(this, req, resp, session);
                } else if ("process_proctor_login.html".equals(subpath)) {
                    doProcessProctorLogin(req, resp);
                } else if ("process_honorlock_login.html".equals(subpath)) {
                    doProcessHonorlockLogin(req, resp);
                } else if ("set_course_schedule.html".equals(subpath)) {
                    PageSchedule.doSetCourseOrder(data, req, resp, logic);
                } else if ("update_homework.html".equals(subpath)) {
                    PageHtmlHomework.updateHomework(data, this, req, resp, session, logic);
                } else if ("update_lta.html".equals(subpath)) {
                    PageHtmlLta.updateLta(data, this, req, resp, session, logic);
                } else if ("update_review_exam.html".equals(subpath)) {
                    PageHtmlReviewExam.updateReviewExam(data, this, req, resp, session, logic);
                } else if ("update_past_exam.html".equals(subpath)) {
                    PageHtmlPastExam.updatePastExam(data, this, req, resp, session, logic);
                } else if ("update_past_lta.html".equals(subpath)) {
                    PageHtmlPastLta.updatePastLta(data, this, req, resp, session, logic);
                } else if ("update_unit_exam.html".equals(subpath)) {
                    PageHtmlUnitExam.updateUnitExam(data, this, req, resp, session, logic);
                } else {
                    Log.warning("Unrecognized POST request path: ", subpath);
                    final String path = this.siteProfile.path;
                    resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH) ? "index.html" : "/index.html"));
                }
            }
        } else {
            PageMaintenance.doGet(data, this, req, resp, maintMsg);
        }
    }

    /**
     * Tests whether the password entered by the proctor is correct.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void doProcessProctorLogin(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final String course = req.getParameter("course");
        final String exam = req.getParameter("exam");
        final String password = req.getParameter("drowssap");

        if (AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(exam)
                || AbstractSite.isParamInvalid(password)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            Log.warning("  exam='", exam, "'");
            Log.warning("  password='", password, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            boolean match = false;

            for (final String test : CORRECT) {
                if (test.equals(password)) {
                    match = true;
                    break;
                }
            }

            final String url = match ? "run_unit.html?course=" + course + "&exam=" + exam :
                    "proctor_login.html?course=" + course + "&exam=" + exam + "&error=Invalid%20password.";

            resp.sendRedirect(url);
        }
    }

    /**
     * Tests whether the password entered by Honorlock is correct.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void doProcessHonorlockLogin(final ServletRequest req,
                                                final HttpServletResponse resp) throws IOException {

        final String course = req.getParameter("course");
        final String exam = req.getParameter("exam");
        final String password = req.getParameter("drowssap");

        if (AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(exam)
                || AbstractSite.isParamInvalid(password)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            Log.warning("  exam='", exam, "'");
            Log.warning("  password='", password, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            boolean match = false;

            for (final String test : CORRECT) {
                if (test.equals(password)) {
                    match = true;
                    break;
                }
            }

            final String url;
            if (match) {
                url = "run_unit.html?course=" + course + "&exam=" + exam;
            } else {
                url = "course.html?course=" + course + "&mode=course&errorExam=" + exam + "&error=Invalid%20password.";
            }

            resp.sendRedirect(url);
        }
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
     * Generates the site title based on the context.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "Precalculus Program";
    }
}
