package dev.mathops.web.site.course;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.old.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.BasicCss;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
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

        // Log.info("GET ", subpath);

        if (CoreConstants.EMPTY.equals(subpath)) {
            final String path = this.siteProfile.path;
            resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH) ? "index.html" : "/index.html"));
        } else if ("basestyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
        } else if ("style.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(CourseSite.class, "style.css", true));
        } else if ("course.css".equals(subpath)) {
            Log.info("***GET course.css in CourseSite");
            BasicCss.getInstance().serveCss(req, resp);
        } else if ("lesson.css".equals(subpath)) {
            Log.info("***GET lesson.css in CourseSite");
            LessonCss.getInstance().serveCss(req, resp);
        } else if (subpath.startsWith("lessons/")) {
            Log.info("***GET a lesson in CourseSite");
            serveLesson(subpath.substring(8), req, resp);
        } else if (subpath.startsWith("images/")) {
            serveImage(subpath.substring(7), req, resp);
        } else if (subpath.endsWith(".vtt")) {
            serveVtt(subpath, req, resp);
        } else if ("favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else {
            final String maintMsg = isMaintenance(this.siteProfile);

            if (maintMsg == null) {
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
//                        case "orientation.html" -> PageOrientation.doGet(cache, this, req, resp, null, null);
                        case "secure/shibboleth.html" -> doShibbolethLogin(cache, req, resp, null, "home.html");
                        default -> {
                            Log.warning("Unrecognized GET request path: ", subpath);
                            resp.sendRedirect("/index.html");
                        }
                    }
                } else {
                    LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

                    // TODO: Replace all logic with this

                    final CourseSiteLogic logic = new CourseSiteLogic(cache, this.siteProfile, session);
                    logic.gatherData();

                    switch (subpath) {
                        case "index.html", "home.html" -> PageHome.doGet(cache, this, req, resp, session, logic);
                        case "secure/shibboleth.html" -> doShibbolethLogin(cache, req, resp, session, "home.html");
//                        case "orientation.html" -> PageOrientation.doGet(cache, this, req, resp, session, logic);
                        case "schedule.html" -> PageSchedule.doGet(cache, this, req, resp, session, logic);
                        case "calendar.html" -> PageCalendar.doGet(cache, this, req, resp, session, logic);
                        case "calendar_print.html" -> PageCalendar.doGetPrintable(cache, this, req, resp, logic);
                        case "onlinehelp.html" -> PageGettingHelp.doGet(cache, this, req, resp, session, logic);
                        case "users_exam.html" -> PageUsersExam.doGet(cache, this, req, resp, session, logic);
                        case "etexts.html" -> PageETexts.doETextsPage(cache, this, req, resp, session, logic);
                        case "start_course.html" -> PageStartCourse.doGet(cache, this, req, resp, session, logic);
                        case "course.html" -> PageOutline.doGet(cache, type, this, req, resp, session, logic);

                        case "course_media.html" -> PageStdsTextMedia.doGet(cache, this, req, resp, session, logic);
                        case "course_status.html" -> PageCourseStatus.doGet(cache, this, req, resp, session, logic);
                        case "course_text.html" -> PageStdsText.doGet(cache, this, req, resp, session, logic);
                        case "course_text_module.html" ->
                                PageStdsTextModule.doGet(cache, this, req, resp, session, logic);

                        case "skills_review.html" ->
                                PageSkillsReview.doGet(cache, type, this, req, resp, session, logic);
                        case "lesson.html" -> PageLesson.doGet(cache, this, req, resp, session, logic);
                        case "placement_report.html" ->
                                PagePlacementReport.doGet(cache, this, req, resp, session, logic);
                        case "proctor_login.html" -> PageProctorLogin.doGet(cache, this, req, resp, session);
                        case "video.html" -> PageVideo.doGet(cache, this, req, resp, session, logic);
                        case "video_example.html" -> PageVideoExample.doGet(this, req, resp, session);
                        case "run_homework.html" ->
                                PageHtmlHomework.startHomework(cache, this, req, resp, session, logic);
                        case "run_lta.html" -> PageHtmlLta.startLta(cache, this, req, resp, session, logic);
                        case "run_review.html" ->
                                PageHtmlReviewExam.startReviewExam(cache, this, req, resp, session, logic);
                        case "update_homework.html" ->
                                PageHtmlHomework.updateHomework(cache, this, req, resp, session, logic);
                        case "update_lta.html" -> PageHtmlLta.updateLta(cache, this, req, resp, session, logic);
                        case "update_review_exam.html" ->
                                PageHtmlReviewExam.updateReviewExam(cache, this, req, resp, session, logic);
                        case "update_past_exam.html" ->
                                PageHtmlPastExam.updatePastExam(cache, this, req, resp, session, logic);
                        case "update_past_lta.html" ->
                                PageHtmlPastLta.updatePastLta(cache, this, req, resp, session, logic);
                        case "update_unit_exam.html" ->
                                PageHtmlUnitExam.updateUnitExam(cache, this, req, resp, session, logic);
                        case "run_unit.html" -> PageHtmlUnitExam.startUnitExam(cache, this, req, resp, session, logic);
                        case "see_past_exam.html" ->
                                PageHtmlPastExam.startPastExam(cache, this, req, resp, session, logic);
                        case "see_past_lta.html" ->
                                PageHtmlPastLta.startPastLta(cache, this, req, resp, session, logic);

                        default -> {
                            Log.warning("Unrecognized GET request path: ", subpath);
                            final String path = this.siteProfile.path;
                            resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH)
                                    ? "index.html" : "/index.html"));
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

        final String maintMsg = isMaintenance(this.siteProfile);

        if (maintMsg == null) {
            final ImmutableSessionInfo session = validateSession(req, resp, "index.html");

            if (session != null) {
                LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

                final CourseSiteLogic logic = new CourseSiteLogic(cache, this.siteProfile, session);
                logic.gatherData();

                switch (subpath) {
                    case "rolecontrol.html" -> processRoleControls(cache, req, resp, session);
                    case "home.html" -> PageHome.doGet(cache, this, req, resp, session, logic);
                    case "media_feedback.html" -> PageVideo.doMediaFeedback(cache, this, req, resp, session, logic);
                    case "example_feedback.html" -> PageVideoExample.doExampleFeedback(this, req, resp, session);
                    case "process_proctor_login.html" -> doProcessProctorLogin(req, resp);
                    case "process_honorlock_login.html" -> doProcessHonorlockLogin(req, resp);
                    case "set_course_schedule.html" -> PageSchedule.doSetCourseOrder(cache, req, resp, logic);

                    case "request_accom_extension.html" -> PageOutline.doRequestAccomExtension(cache, type, this, req,
                            resp, session, logic);
                    case "request_free_extension.html" -> PageOutline.doRequestFreeExtension(cache, type, this, req,
                            resp, session, logic);

                    case "update_homework.html" ->
                            PageHtmlHomework.updateHomework(cache, this, req, resp, session, logic);
                    case "update_lta.html" -> PageHtmlLta.updateLta(cache, this, req, resp, session, logic);
                    case "update_review_exam.html" ->
                            PageHtmlReviewExam.updateReviewExam(cache, this, req, resp, session, logic);
                    case "update_past_exam.html" ->
                            PageHtmlPastExam.updatePastExam(cache, this, req, resp, session, logic);
                    case "update_past_lta.html" ->
                            PageHtmlPastLta.updatePastLta(cache, this, req, resp, session, logic);
                    case "update_unit_exam.html" ->
                            PageHtmlUnitExam.updateUnitExam(cache, this, req, resp, session, logic);
                    case null, default -> {
                        Log.warning("Unrecognized POST request path: ", subpath);
                        final String path = this.siteProfile.path;
                        resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH)
                                ? "index.html" : "/index.html"));
                    }
                }
            }
        } else {
            PageMaintenance.doGet(cache, this, req, resp, maintMsg);
        }
    }

    /**
     * Checks whether the student has "credit" for a course from the perspective of testing prerequisites.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @param courseId  the course to test
     * @return true if student has the course
     * @throws SQLException if there was an error accessing the database
     */
    static boolean hasCourseAsPrereq(final Cache cache, final String studentId,
                                     final String courseId) throws SQLException {

        boolean hasCourse = false;

        // See if student has completed the course at any time in the past

        final List<RawStcourse> complete = RawStcourseLogic.getAllPriorCompleted(cache, studentId);
        for (final RawStcourse test : complete) {
            if (courseId.equals(test.course)) {
                hasCourse = true;
                break;
            }
        }

        if (!hasCourse) {
            // See if there are transfer credits satisfying the prerequisite
            final List<RawFfrTrns> trans = RawFfrTrnsLogic.queryByStudent(cache, studentId);
            for (final RawFfrTrns test : trans) {
                if (courseId.equals(test.course)) {
                    hasCourse = true;
                    break;
                }
            }
        }

        if (!hasCourse) {
            // See if there is a placement result satisfying prerequisite

            final List<RawMpeCredit> placeCred = RawMpeCreditLogic.queryByStudent(cache, studentId);
            for (final RawMpeCredit test : placeCred) {
                if (courseId.equals(test.course)) {
                    hasCourse = true;
                    break;
                }
            }
        }

        return hasCourse;
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

        if (isParamInvalid(course) || isParamInvalid(exam) || isParamInvalid(password)) {
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

        if (isParamInvalid(course) || isParamInvalid(exam) || isParamInvalid(password)) {
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
     * Tests the live refresh policy for this site.
     *
     * @return the live refresh policy
     */
    @Override
    protected ELiveRefreshes getLiveRefreshes() {

        return ELiveRefreshes.ALL;
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
