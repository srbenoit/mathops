package dev.mathops.web.site.admin.genadmin.serveradmin;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdmSubtopic;
import dev.mathops.web.site.admin.genadmin.EAdminTopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;
import dev.mathops.web.site.html.EForceTerminateState;
import dev.mathops.web.site.html.hw.HomeworkSession;
import dev.mathops.web.site.html.hw.HomeworkSessionStore;
import dev.mathops.web.site.html.pastexam.EPastExamState;
import dev.mathops.web.site.html.pastexam.PastExamSession;
import dev.mathops.web.site.html.pastexam.PastExamSessionStore;
import dev.mathops.web.site.html.placementexam.EPlacementExamState;
import dev.mathops.web.site.html.placementexam.PlacementExamSession;
import dev.mathops.web.site.html.placementexam.PlacementExamSessionStore;
import dev.mathops.web.site.html.reviewexam.EReviewExamState;
import dev.mathops.web.site.html.reviewexam.ReviewExamSession;
import dev.mathops.web.site.html.reviewexam.ReviewExamSessionStore;
import dev.mathops.web.site.html.unitexam.EUnitExamState;
import dev.mathops.web.site.html.unitexam.UnitExamSession;
import dev.mathops.web.site.html.unitexam.UnitExamSessionStore;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The "Sessions" sub-page of the Server Administration page.
 */
public final class PageServerAdminSessions {

    /**
     * Private constructor to prevent instantiation.
     */
    private PageServerAdminSessions() {

        super();
    }

    /**
     * Generates the server administration page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.SERVER_ADMIN, htm);

        PageServerAdmin.emitNavMenu(htm, EAdmSubtopic.SRV_SESSIONS);
        doPageContent(htm, session);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Appends page content to an {@code HtmlBuilder}.
     *
     * @param htm     the {@code HtmlBuilder} to which to write
     * @param session the session under which the page is being displayed
     */
    private static void doPageContent(final HtmlBuilder htm, final ImmutableSessionInfo session) {

        emitLoginSessions(htm, session);
        emitHtmlPlacementExamSessions(htm);
        emitHtmlUnitExamSessions(htm);
        emitHtmlReviewExamSessions(htm);
        emitHtmlHomeworkSessions(htm);
        emitHtmlPastExamSessions(htm);
    }

    /**
     * Appends a table of active login sessions to an {@code HtmlBuilder}.
     *
     * @param htm     the {@code HtmlBuilder} to which to write
     * @param session the session under which the page is being displayed
     */
    private static void emitLoginSessions(final HtmlBuilder htm, final ImmutableSessionInfo session) {

        final List<ImmutableSessionInfo> activeSessions =
                SessionManager.getInstance().listActiveSessions(session.loginSessionId);

        // Create a new sorted map whose keys are based on screen name.
        final Map<String, ImmutableSessionInfo> newmap = new TreeMap<>();
        for (final ImmutableSessionInfo sess : activeSessions) {
            final String name = sess.screenName + CoreConstants.SPC + sess.userId + CoreConstants.SPC
                    + sess.loginSessionTag;
            newmap.put(name, sess);
        }

        htm.div("vgap0").hr().div("vgap0");

        htm.sH(2).add("Active Login Sessions (", Integer.toString(newmap.size() - 1), ")").eH(2);

        htm.sTable("report");
        htm.sTr();
        htm.sTh().add("Session ID").eTh();
        htm.sTh().add("Student ID").eTh();
        htm.sTh().add("Name").eTh();
        htm.sTh().add("Role").eTh();
        htm.sTh().add("Acting As").eTh();
        htm.sTh().add("Idle Time").eTh();
        htm.sTh().add("Until purge").eTh();
        htm.sTh().add("Action").eTh();
        htm.eTr();

        final Instant now = Instant.now();
        for (final ImmutableSessionInfo sess : newmap.values()) {
            if (sess.userId == null) {
                // Skip anonymous session (used for non-login tutorials like Calc review)
                continue;
            }

            htm.sTr();
            htm.sTd().add(sess.loginSessionId).eTd();
            htm.sTd().add(sess.userId).eTd();
            htm.sTd().add(sess.screenName).eTd();
            htm.sTd().add(sess.role.abbrev).eTd();
            htm.sTd().add(sess.actAsUserId == null ? CoreConstants.EMPTY : sess.actAsUserId).eTd();
            htm.sTd().add(GenAdminPage.formatMsDuration(Duration.between(sess.lastActivity, now).toMillis())).eTd();
            htm.sTd().add(GenAdminPage.formatMsDuration(sess.getTimeUntilPurge())).eTd();
            htm.sTd();
            // htm.addln("<form action='srvadm_sessions.html' method='post'>");
            // htm.addln(" <input type='hidden' name='action' value='terminate'>");
            // htm.addln(" <input type='hidden' name='type' value='login'>");
            // htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId,
            // "'>");
            // htm.addln(" <input type='submit' value='Terminate'>");
            // htm.add("</form>");
            htm.eTd();
            htm.eTr();
        }
        htm.eTable();
    }

    /**
     * Appends a table of active HTML placement exam sessions to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to write
     */
    private static void emitHtmlPlacementExamSessions(final HtmlBuilder htm) {

        htm.div("vgap0").hr().div("vgap0");

        final Map<String, PlacementExamSession> map =
                PlacementExamSessionStore.getInstance().getPlacementExamSessions();
        Log.info("There are " + map.size() + " placement sessions.");

        // Create a new sorted map whose keys are based on student name (last, first)
        final Map<String, PlacementExamSession> newmap = new TreeMap<>();
        for (final PlacementExamSession sess : map.values()) {

            final RawStudent stu = sess.getStudent();
            if (stu == null) {
                Log.warning("Placement session had null student: ", sess.studentId);
            } else {
                // Sort by name, but add student ID to key in case of duplicate names
                final String name = stu.lastName + CoreConstants.SPC + stu.firstName + CoreConstants.SPC + stu.stuId;
                newmap.put(name, sess);
            }
        }

        htm.sH(2).add("HTML Placement Exam Sessions (" + newmap.size() + ")").eH(2);

        htm.sTable("report");
        htm.sTr();
        htm.sTh().add("Session ID").eTh();
        htm.sTh().add("Student ID").eTh();
        htm.sTh().add("Name").eTh();
        htm.sTh().add("Exam").eTh();
        htm.sTh().add("Proctor").eTh();
        htm.sTh().add("State").eTh();
        htm.sTh().add("Started").eTh();
        htm.sTh().add("Remaining").eTh();
        htm.sTh().add("Until purge").eTh();
        htm.sTh().add("Error").eTh();
        htm.sTh().add("Action").eTh();
        htm.eTr();

        for (final PlacementExamSession sess : newmap.values()) {

            htm.addln("<tr id='placement_session_", sess.sessionId, "'>");
            htm.sTd().add(sess.sessionId).eTd();
            htm.sTd().add(sess.studentId).eTd();
            htm.sTd().add(sess.getStudent().getScreenName()).eTd();
            htm.sTd().add(sess.version).eTd();
            htm.sTd().add(sess.proctored ? "Yes" : "No").eTd();
            if (sess.getState() == EPlacementExamState.ITEM_NN) {
                htm.sTd().add(sess.getState().name(), CoreConstants.DOT,
                        Integer.toString(sess.getCurrentSect()), CoreConstants.COMMA,
                        Integer.toString(sess.getCurrentItem())).eTd();
            } else {
                htm.sTd().add(sess.getState().name()).eTd();
            }
            htm.sTd().add(sess.isStarted() ? "Yes" : "No").eTd();
            htm.sTd().add(GenAdminPage.formatMsDuration(sess.getTimeRemaining())).eTd();
            htm.sTd().add(GenAdminPage.formatMsDuration(sess.getTimeUntilPurge())).eTd();
            htm.sTd().add(sess.getError()).eTd();

            final EForceTerminateState force = sess.getForceTerminate();
            if (force == EForceTerminateState.NONE) {
                htm.sTd();
                htm.addln("<form action='srvadm_sessions.html#placement_session_",
                        sess.sessionId, "' method='post' style='display:inline;'>");
                htm.addln(" <input type='hidden' name='action' value='abort'>");
                htm.addln(" <input type='hidden' name='type' value='placement_exam'>");
                htm.addln(" <input type='hidden' name='student_id' value='", sess.studentId, "'>");
                htm.addln(" <input type='submit' value='Abort'>");
                htm.add("</form>");
                htm.addln("<form action='srvadm_sessions.html#placement_session_",
                        sess.sessionId, "' method='post' style='display:inline;'>");
                htm.addln(" <input type='hidden' name='action' value='submit'>");
                htm.addln(" <input type='hidden' name='type' value='placement_exam'>");
                htm.addln(" <input type='hidden' name='student_id' value='", sess.studentId, "'>");
                htm.addln(" <input type='submit' value='Submit'>");
                htm.add("</form>");
                htm.eTd();
            } else if (force == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {
                htm.sTd("red");
                htm.add("Abort exam without scoring?: ");
                htm.addln("<form action='srvadm_sessions.html#placement_session_",
                        sess.sessionId, "' method='post' style='padding:3px;'>");
                htm.addln(" <input type='hidden' name='action' value='abortconfirm'>");
                htm.addln(" <input type='hidden' name='type' value='placement_exam'>");
                htm.addln(" <input type='hidden' name='student_id' value='", sess.studentId, "'>");
                htm.addln(" <input type='submit' value='Yes, Abort Exam'>");
                htm.add("</form>");
                htm.addln("<form action='srvadm_sessions.html#placement_session_",
                        sess.sessionId, "' method='post' style='padding:3px;'>");
                htm.addln(" <input type='hidden' name='action' value='abortcancel'>");
                htm.addln(" <input type='hidden' name='type' value='placement_exam'>");
                htm.addln(" <input type='hidden' name='student_id' value='", sess.studentId, "'>");
                htm.addln(" <input type='submit' value='No, Leave Exam Active'>");
                htm.add("</form>");
                htm.eTd();
            } else if (force == EForceTerminateState.SUBMIT_AND_SCORE_REQUESTED) {
                htm.sTd("red");
                htm.add("Force submission and scoring of exam?: ");
                htm.addln("<form action='srvadm_sessions.html#placement_session_",
                        sess.sessionId, "' method='post' style='padding:3px;'>");
                htm.addln(" <input type='hidden' name='action' value='submitconfirm'>");
                htm.addln(" <input type='hidden' name='type' value='placement_exam'>");
                htm.addln(" <input type='hidden' name='student_id' value='", sess.studentId, "'>");
                htm.addln(" <input type='submit' value='Yes, Submit Exam'>");
                htm.add("</form>");
                htm.addln("<form action='srvadm_sessions.html#placement_session_",
                        sess.sessionId, "' method='post' style='padding:3px;'>");
                htm.addln(" <input type='hidden' name='action' value='submitcancel'>");
                htm.addln(" <input type='hidden' name='type' value='placement_exam'>");
                htm.addln(" <input type='hidden' name='student_id' value='", sess.studentId, "'>");
                htm.addln(" <input type='submit' value='No, Leave Exam Active'>");
                htm.add("</form>");
                htm.eTd();
            }
            htm.eTr();
        }

        htm.eTable();
    }

    /**
     * Appends a table of active HTML unit exam sessions to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to write
     */
    private static void emitHtmlUnitExamSessions(final HtmlBuilder htm) {

        final Map<String, Map<String, UnitExamSession>> map =
                UnitExamSessionStore.getInstance().getUnitExamSessions();
        Log.info("There are " + map.size() + " sessions with unit exams in progress.");

        // Create a new sorted map whose keys are based on student name (last, first)
        final Map<String, UnitExamSession> newmap = new TreeMap<>();
        for (final Map<String, UnitExamSession> inner : map.values()) {

            for (final UnitExamSession sess : inner.values()) {

                final RawStudent stu = sess.getStudent();
                if (stu == null) {
                    Log.warning("Unit exam session had null student: ", sess.studentId);
                } else {
                    // Sort by name, but add session and exam IDs to key in case of duplicate names
                    final String name = stu.lastName + CoreConstants.SPC + stu.firstName
                            + CoreConstants.SPC + sess.sessionId + CoreConstants.SPC + sess.version;
                    newmap.put(name, sess);
                }
            }
        }

        htm.div("vgap0").hr().div("vgap0");
        htm.sH(2).add("HTML Unit Exam Sessions (" + newmap.size() + ")").eH(2);

        htm.sTable("report");
        htm.sTr();
        htm.sTh().add("Session ID").eTh();
        htm.sTh().add("Student ID").eTh();
        htm.sTh().add("Name").eTh();
        htm.sTh().add("Exam").eTh();
        htm.sTh().add("State").eTh();
        htm.sTh().add("Started").eTh();
        htm.sTh().add("Remaining").eTh();
        htm.sTh().add("Redirect").eTh();
        htm.sTh().add("Action").eTh();
        htm.eTr();

        for (final UnitExamSession sess : newmap.values()) {

            htm.addln("<tr id='unit_session_", sess.sessionId, "'>");
            htm.sTd().add(sess.sessionId).eTd();
            htm.sTd().add(sess.studentId).eTd();
            htm.sTd().add(sess.getStudent().getScreenName()).eTd();
            htm.sTd().add(sess.version).eTd();
            if ((sess.getState() == EUnitExamState.ITEM_NN)
                    || (sess.getState() == EUnitExamState.SOLUTION_NN)) {
                htm.sTd().add(sess.getState().name() + CoreConstants.DOT + sess.getCurrentItem()).eTd();
            } else {
                htm.sTd().add(sess.getState().name()).eTd();
            }
            htm.sTd().add(sess.isStarted() ? "Yes" : "No").eTd();
            htm.sTd().add(GenAdminPage.formatMsDuration(sess.getTimeRemaining())).eTd();
            htm.sTd().add(sess.redirectOnEnd).eTd();

            final EForceTerminateState force = sess.getForceTerminate();
            if (force == EForceTerminateState.NONE) {
                htm.sTd();
                htm.addln("<form action='srvadm_sessions.html#unit_session_",
                        sess.sessionId, "' method='post' style='display:inline;'>");
                htm.addln(" <input type='hidden' name='action' value='abort'>");
                htm.addln(" <input type='hidden' name='type' value='unit_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                htm.addln(" <input type='submit' value='Abort'>");
                htm.add("</form>");
                htm.addln("<form action='srvadm_sessions.html#unit_session_",
                        sess.sessionId, "' method='post' style='display:inline;'>");
                htm.addln(" <input type='hidden' name='action' value='submit'>");
                htm.addln(" <input type='hidden' name='type' value='unit_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                htm.addln(" <input type='submit' value='Submit'>");
                htm.add("</form>");
                htm.eTd();
            } else if (force == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {
                htm.sTd("red");
                htm.add("Abort exam without scoring?: ");
                htm.addln("<form action='srvadm_sessions.html#unit_session_",
                        sess.sessionId, "' method='post' style='padding:3px;'>");
                htm.addln(" <input type='hidden' name='action' value='abortconfirm'>");
                htm.addln(" <input type='hidden' name='type' value='unit_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                htm.addln(" <input type='submit' value='Yes, Abort Exam'>");
                htm.add("</form>");
                htm.addln("<form action='srvadm_sessions.html#unit_session_",
                        sess.sessionId, "' method='post' style='padding:3px;'>");
                htm.addln(" <input type='hidden' name='action' value='abortcancel'>");
                htm.addln(" <input type='hidden' name='type' value='unit_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                htm.addln(" <input type='submit' value='No, Leave Exam Active'>");
                htm.add("</form>");
                htm.eTd();
            } else if (force == EForceTerminateState.SUBMIT_AND_SCORE_REQUESTED) {
                htm.sTd("red");
                htm.add("Force submission and scoring of exam?: ");
                htm.addln("<form action='srvadm_sessions.html#unit_session_",
                        sess.sessionId, "' method='post' style='padding:3px;'>");
                htm.addln(" <input type='hidden' name='action' value='submitconfirm'>");
                htm.addln(" <input type='hidden' name='type' value='unit_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                htm.addln(" <input type='submit' value='Yes, Submit Exam'>");
                htm.add("</form>");
                htm.addln("<form action='srvadm_sessions.html#unit_session_",
                        sess.sessionId, "' method='post' style='padding:3px;'>");
                htm.addln(" <input type='hidden' name='action' value='submitcancel'>");
                htm.addln(" <input type='hidden' name='type' value='unit_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                htm.addln(" <input type='submit' value='No, Leave Exam Active'>");
                htm.add("</form>");
                htm.eTd();
            }
            htm.eTr();
        }

        htm.eTable();
    }

    /**
     * Appends a table of active HTML review exam sessions to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to write
     */
    private static void emitHtmlReviewExamSessions(final HtmlBuilder htm) {

        final Map<String, Map<String, ReviewExamSession>> map =
                ReviewExamSessionStore.getInstance().getReviewExamSessions();

        final Map<String, ReviewExamSession> newmap = new TreeMap<>();

        synchronized (map) {
            Log.info("There are " + map.size()
                    + " sessions with review exams in progress.");

            // Create a new sorted map whose keys are based on student name (last, first)
            for (final Map<String, ReviewExamSession> inner : map.values()) {
                for (final ReviewExamSession sess : inner.values()) {
                    final String name = sess.studentId + CoreConstants.SPC + sess.sessionId
                            + CoreConstants.SPC + sess.version;
                    newmap.put(name, sess);
                }
            }
        }

        htm.div("vgap0").hr().div("vgap0");
        htm.sH(2).add("HTML Review Exam Sessions (" + newmap.size() + ")").eH(2);

        htm.sTable("report");
        htm.sTr();
        htm.sTh().add("Session ID").eTh();
        htm.sTh().add("Student ID").eTh();
        htm.sTh().add("Exam").eTh();
        htm.sTh().add("State").eTh();
        htm.sTh().add("Practice").eTh();
        htm.sTh().add("Started").eTh();
        htm.sTh().add("Remaining").eTh();
        htm.sTh().add("Redirect").eTh();
        htm.sTh().add("Action").eTh();
        htm.eTr();

        for (final ReviewExamSession sess : newmap.values()) {

            htm.addln("<tr id='review_session_", sess.sessionId,
                    "'>");
            htm.sTd().add(sess.sessionId).eTd();
            htm.sTd().add(sess.studentId).eTd();
            htm.sTd().add(sess.version).eTd();
            if ((sess.getState() == EReviewExamState.ITEM_NN)
                    || (sess.getState() == EReviewExamState.SOLUTION_NN)) {
                htm.sTd().add(sess.getState().name() + CoreConstants.DOT + sess.getItem()).eTd();
            } else {
                htm.sTd().add(sess.getState().name()).eTd();
            }
            htm.sTd().add(sess.practice ? "Yes" : "No").eTd();
            htm.sTd().add(sess.isStarted() ? "Yes" : "No").eTd();
            htm.sTd().add(GenAdminPage.formatMsDuration(sess.getTimeRemaining())).eTd();
            htm.sTd().add(sess.redirectOnEnd).eTd();

            final EForceTerminateState force = sess.getForceTerminate();
            if (force == EForceTerminateState.NONE) {
                htm.sTd();
                htm.addln("<form style='display:inline;' action='srvadm_sessions.html#review_session_",
                        sess.sessionId, "' method='post'>");
                htm.addln(" <input type='hidden' name='action' value='abort'>");
                htm.addln(" <input type='hidden' name='type' value='review_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                htm.addln(" <input type='submit' value='Abort'>");
                htm.add("</form>");
                htm.addln("<form style='display:inline;' action='srvadm_sessions.html#review_session_",
                        sess.sessionId, "' method='post'>");
                htm.addln(" <input type='hidden' name='action' value='submit'>");
                htm.addln(" <input type='hidden' name='type' value='review_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                htm.addln(" <input type='submit' value='Submit'>");
                htm.add("</form>");
                htm.eTd();
            } else if (force == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {
                htm.sTd("red");
                htm.add("Abort exam without scoring?: ");
                htm.addln("<form style='padding:3px;' action='srvadm_sessions.html#review_session_",
                        sess.sessionId, "' method='post'>");
                htm.addln(" <input type='hidden' name='action' value='abortconfirm'>");
                htm.addln(" <input type='hidden' name='type' value='review_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                htm.addln(" <input type='submit' value='Yes, Abort Exam'>");
                htm.add("</form>");
                htm.addln("<form style='padding:3px;' action='srvadm_sessions.html#review_session_",
                        sess.sessionId, "' method='post'>");
                htm.addln(" <input type='hidden' name='action' value='abortcancel'>");
                htm.addln(" <input type='hidden' name='type' value='review_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                htm.addln(" <input type='submit' value='No, Leave Exam Active'>");
                htm.add("</form>");
                htm.eTd();
            } else if (force == EForceTerminateState.SUBMIT_AND_SCORE_REQUESTED) {
                htm.sTd("red");
                htm.add("Force submission and scoring of exam?: ");
                htm.addln("<form style='padding:3px;' action='srvadm_sessions.html#review_session_",
                        sess.sessionId, "' method='post'>");
                htm.addln(" <input type='hidden' name='action' value='submitconfirm'>");
                htm.addln(" <input type='hidden' name='type' value='review_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                htm.addln(" <input type='submit' value='Yes, Submit Exam'>");
                htm.add("</form>");
                htm.addln("<form style='padding:3px;' action='srvadm_sessions.html#review_session_",
                        sess.sessionId, "' method='post'>");
                htm.addln(" <input type='hidden' name='action' value='submitcancel'>");
                htm.addln(" <input type='hidden' name='type' value='review_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                htm.addln(" <input type='submit' value='No, Leave Exam Active'>");
                htm.add("</form>");
                htm.eTd();
            }
            htm.eTr();
        }

        htm.eTable();
    }

    /**
     * Appends a table of active HTML homework sessions to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to write
     */
    private static void emitHtmlHomeworkSessions(final HtmlBuilder htm) {

        final HashMap<String, HashMap<String, HomeworkSession>> map =
                HomeworkSessionStore.getInstance().getHomeworkSessions();
        Log.info("There are " + map.size() + " sessions with homework in progress.");

        // Create a new sorted map whose keys are based on student name (last, first)
        final Map<String, HomeworkSession> newmap = new TreeMap<>();
        for (final Map<String, HomeworkSession> inner : map.values()) {

            for (final HomeworkSession sess : inner.values()) {
                final String name = sess.studentId + CoreConstants.SPC + sess.sessionId
                        + CoreConstants.SPC + sess.version;
                newmap.put(name, sess);
            }
        }

        htm.div("vgap0").hr().div("vgap0");
        htm.sH(2).add("HTML Homework Sessions (" + newmap.size() + ")").eH(2);

        htm.sTable("report");
        htm.sTr();
        htm.sTh().add("Session ID").eTh();
        htm.sTh().add("Student ID").eTh();
        htm.sTh().add("Assignment").eTh();
        htm.sTh().add("State").eTh();
        htm.sTh().add("Practice").eTh();
        htm.sTh().add("Remaining").eTh();
        htm.sTh().add("Redirect").eTh();
        htm.sTh().add("Action").eTh();
        htm.eTr();

        for (final HomeworkSession sess : newmap.values()) {

            synchronized (sess) {
                htm.addln("<tr id='homework_session_", sess.sessionId,
                        "'>");
                htm.sTd().add(sess.sessionId).eTd();
                htm.sTd().add(sess.studentId).eTd();
                htm.sTd().add(sess.version).eTd();
                htm.sTd().add(sess.getState().name()).eTd();
                htm.sTd().add(sess.practice ? "Yes" : "No").eTd();
                htm.sTd().add(GenAdminPage.formatMsDuration(sess.getTimeRemaining())).eTd();
                htm.sTd().add(sess.redirectOnEnd).eTd();

                final EForceTerminateState force = sess.getForceTerminate();
                if (force == EForceTerminateState.NONE) {
                    htm.sTd();
                    htm.addln("<form style='display:inline;' action='srvadm_sessions.html#homework_session_",
                            sess.sessionId, "' method='post'>");
                    htm.addln(" <input type='hidden' name='action' value='terminate'>");
                    htm.addln(" <input type='hidden' name='type' value='homework'>");
                    htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                    htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                    htm.addln(" <input type='submit' value='Abort'>");
                    htm.add("</form>");
                    htm.eTd();
                } else if (force == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {
                    htm.sTd("red");
                    htm.add("Abort exam without scoring?: ");
                    htm.addln("<form style='padding:3px;' action='srvadm_sessions.html#homework_session_",
                            sess.sessionId, "' method='post'>");
                    htm.addln(" <input type='hidden' name='action' value='terminateconfirm'>");
                    htm.addln(" <input type='hidden' name='type' value='homework'>");
                    htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                    htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                    htm.addln(" <input type='submit' value='Yes, Abort Homework'>");
                    htm.add("</form>");
                    htm.addln("<form style='padding:3px;' action='srvadm_sessions.html#homework_session_",
                            sess.sessionId, "' method='post'>");
                    htm.addln(" <input type='hidden' name='action' value='terminatecancel'>");
                    htm.addln(" <input type='hidden' name='type' value='homework'>");
                    htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                    htm.addln(" <input type='hidden' name='exam_id' value='", sess.version, "'>");
                    htm.addln(" <input type='submit' value='No, Leave Homework Active'>");
                    htm.add("</form>");
                    htm.eTd();
                }
                htm.eTr();
            }
        }

        htm.eTable();
    }

    /**
     * Appends a table of active HTML past exam sessions to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to write
     */
    private static void emitHtmlPastExamSessions(final HtmlBuilder htm) {

        final Map<String, Map<String, PastExamSession>> map =
                PastExamSessionStore.getInstance().getPastExamSessions();
        Log.info("There are " + map.size() + " sessions viewing past exams.");

        // Create a new sorted map whose keys are based on student name (last, first)
        final Map<String, PastExamSession> newmap = new TreeMap<>();
        for (final Map<String, PastExamSession> inner : map.values()) {

            for (final PastExamSession sess : inner.values()) {
                final String name = sess.studentId + CoreConstants.SPC + sess.sessionId
                        + CoreConstants.SPC + sess.xmlFilename;
                newmap.put(name, sess);
            }
        }

        htm.div("vgap0").hr().div("vgap0");
        htm.sH(2).add("HTML Past Exam Sessions (" + newmap.size() + ")").eH(2);

        htm.sTable("report");
        htm.sTr();
        htm.sTh().add("Session ID").eTh();
        htm.sTh().add("XML").eTh();
        htm.sTh().add("Student ID").eTh();
        htm.sTh().add("Exam ID").eTh();
        htm.sTh().add("State").eTh();
        htm.sTh().add("Remaining").eTh();
        htm.sTh().add("Redirect").eTh();
        htm.sTh().add("Action").eTh();
        htm.eTr();

        for (final PastExamSession sess : newmap.values()) {

            htm.addln("<tr id='past_exam_session_", sess.sessionId, "'>");
            htm.sTd().add(sess.sessionId).eTd();
            htm.sTd().add(sess.xmlFilename).eTd();
            htm.sTd().add(sess.studentId).eTd();
            htm.sTd().add(sess.getExam() == null ? "(null)" : sess.getExam().examVersion).eTd();
            if (sess.getState() == EPastExamState.ITEM_NN) {
                htm.sTd().add(sess.getState().name() + CoreConstants.DOT + sess.getItem()).eTd();
            } else {
                htm.sTd().add(sess.getState().name()).eTd();
            }
            htm.sTd().add(GenAdminPage.formatMsDuration(sess.getTimeRemaining())).eTd();
            htm.sTd().add(sess.redirectOnEnd).eTd();

            final EForceTerminateState force = sess.getForceTerminate();
            if (force == EForceTerminateState.NONE) {
                htm.sTd();
                htm.addln("<form style='display:inline;' action='srvadm_sessions.html#past_exam_session_",
                        sess.sessionId, "' method='post'>");
                htm.addln(" <input type='hidden' name='action' value='terminate'>");
                htm.addln(" <input type='hidden' name='type' value='past_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='xml' value='", sess.xmlFilename, "'>");
                htm.addln(" <input type='submit' value='Abort'>");
                htm.add("</form>");
                htm.eTd();
            } else if (force == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {
                htm.sTd("red");
                htm.add("Abort exam without scoring?: ");
                htm.addln("<form style='padding:3px;' action='srvadm_sessions.html#past_exam_session_",
                        sess.sessionId, "' method='post'>");
                htm.addln(" <input type='hidden' name='action' value='terminateconfirm'>");
                htm.addln(" <input type='hidden' name='type' value='past_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='xml' value='", sess.xmlFilename, "'>");
                htm.addln(" <input type='submit' value='Yes, Abort Session'>");
                htm.add("</form>");
                htm.addln("<form style='padding:3px;' action='srvadm_sessions.html#past_exam_session_",
                        sess.sessionId, "' method='post'>");
                htm.addln(" <input type='hidden' name='action' value='terminatecancel'>");
                htm.addln(" <input type='hidden' name='type' value='past_exam'>");
                htm.addln(" <input type='hidden' name='session_id' value='", sess.sessionId, "'>");
                htm.addln(" <input type='hidden' name='xml' value='", sess.xmlFilename, "'>");
                htm.addln(" <input type='submit' value='No, Leave Session Active'>");
                htm.add("</form>");
                htm.eTd();
            }

            htm.eTr();
        }

        htm.eTable();
    }

    /**
     * Handles a POST request to the sessions page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doPost(final Cache cache, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String action = req.getParameter("action");

        if ("abort".equals(action)) {

            final String type = req.getParameter("type");

            if ("placement_exam".equals(type)) {
                final String studentId = req.getParameter("student_id");
                final PlacementExamSession sess = PlacementExamSessionStore.getInstance()
                        .getPlacementExamSessionForStudent(studentId);
                if (sess == null) {
                    Log.warning("Unrecognized student ID for placement session: ", studentId);
                } else if (sess.getForceTerminate() == EForceTerminateState.NONE) {
                    sess.setForceTerminate(EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED);
                } else {
                    Log.warning("'abort' request for placement session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("unit_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final UnitExamSession sess = UnitExamSessionStore.getInstance().getUnitExamSession(sessionId, examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for unit session: ", sessionId, ", ", examId);
                } else if (sess.getForceTerminate() == EForceTerminateState.NONE) {
                    sess.setForceTerminate(EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED);
                } else {
                    Log.warning("'abort' request for unit session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("review_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final ReviewExamSession sess = ReviewExamSessionStore.getInstance().getReviewExamSession(sessionId,
                        examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for review session: ", sessionId, ", ", examId);
                } else if (sess.getForceTerminate() == EForceTerminateState.NONE) {
                    sess.setForceTerminate(EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED);
                } else {
                    Log.warning("'abort' request for review session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("past_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String xml = req.getParameter("xml");
                final PastExamSession sess = PastExamSessionStore.getInstance().getPastExamSession(sessionId, xml);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/XML for past exam session: ", sessionId, ", ", xml);
                } else if (sess.getForceTerminate() == EForceTerminateState.NONE) {
                    sess.setForceTerminate(EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED);
                } else {
                    Log.warning("'abort' request for past exam session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else {
                Log.warning("Unrecognized type for 'abort' action: ", type);
            }
        } else if ("abortconfirm".equals(action)) {

            final String type = req.getParameter("type");

            if ("placement_exam".equals(type)) {
                final String studentId = req.getParameter("student_id");
                final PlacementExamSession sess = PlacementExamSessionStore.getInstance()
                        .getPlacementExamSessionForStudent(studentId);
                if (sess == null) {
                    Log.warning("Unrecognized student ID for placement session: ", studentId);
                } else if (sess
                        .getForceTerminate() == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {
                    Log.warning("Forced abort of placement exam for student ", studentId);
                    sess.forceAbort(cache, session);
                } else {
                    Log.warning("'abortconfirm' request for placement session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("unit_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final UnitExamSession sess = UnitExamSessionStore.getInstance().getUnitExamSession(sessionId, examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for unit session: ", sessionId, ", ", examId);
                } else if (sess
                        .getForceTerminate() == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {
                    Log.warning("Forced abort of unit exam for student ", sess.studentId);
                    sess.forceAbort(cache, session);
                } else {
                    Log.warning("'abortconfirm' request for unit session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("review_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final ReviewExamSession sess =
                        ReviewExamSessionStore.getInstance().getReviewExamSession(sessionId, examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for review session: ", sessionId, ", ", examId);
                } else if (sess
                        .getForceTerminate() == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {
                    Log.warning("Forced abort of review exam for student ", sess.studentId);
                    sess.forceAbort(cache, session);
                } else {
                    Log.warning("'abortconfirm' request for review session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("past_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String xml = req.getParameter("xml");
                final PastExamSession sess = PastExamSessionStore.getInstance().getPastExamSession(sessionId, xml);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/XML for past exam session: ", sessionId, ", ", xml);
                } else if (sess
                        .getForceTerminate() == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {
                    Log.warning("Forced abort of past exam exam for student ", sess.studentId);
                    sess.forceAbort(session);
                } else {
                    Log.warning("'abortconfirm' request for past exam session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else {
                Log.warning("Unrecognized type for 'abortconfirm' action: ", type);
            }
        } else if ("abortcancel".equals(action)) {

            final String type = req.getParameter("type");

            if ("placement_exam".equals(type)) {
                final String studentId = req.getParameter("student_id");
                final PlacementExamSession sess = PlacementExamSessionStore.getInstance()
                        .getPlacementExamSessionForStudent(studentId);
                if (sess == null) {
                    Log.warning("Unrecognized student ID for placement session: ", studentId);
                } else {
                    if (sess.getForceTerminate() == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {

                        // TODO:
                    } else {
                        Log.warning("'abortcancel' request for placement session in termination state: ",
                                sess.getForceTerminate().name());
                    }
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("unit_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final UnitExamSession sess = UnitExamSessionStore.getInstance().getUnitExamSession(sessionId, examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for unit session: ", sessionId, ", ", examId);
                } else {
                    if (sess.getForceTerminate() == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {

                        // TODO:
                    } else {
                        Log.warning("'abortcancel' request for unit session in termination state: ",
                                sess.getForceTerminate().name());
                    }
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("review_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final ReviewExamSession sess =
                        ReviewExamSessionStore.getInstance().getReviewExamSession(sessionId, examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for review session: ", sessionId, ", ", examId);
                } else {
                    if (sess.getForceTerminate() == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {

                        // TODO:
                    } else {
                        Log.warning("'abortcancel' request for review session in termination state: ",
                                sess.getForceTerminate().name());
                    }
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else {
                Log.warning("Unrecognized type for 'abortcancel' action: ", type);
            }

        } else if ("submit".equals(action)) {

            final String type = req.getParameter("type");

            if ("placement_exam".equals(type)) {
                final String studentId = req.getParameter("student_id");
                final PlacementExamSession sess = PlacementExamSessionStore.getInstance()
                        .getPlacementExamSessionForStudent(studentId);
                if (sess == null) {
                    Log.warning("Unrecognized student ID for placement session: ", studentId);
                } else if (sess.getForceTerminate() == EForceTerminateState.NONE) {
                    sess.setForceTerminate(EForceTerminateState.SUBMIT_AND_SCORE_REQUESTED);
                } else {
                    Log.warning("'submit' request for placement session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("unit_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final UnitExamSession sess = UnitExamSessionStore.getInstance().getUnitExamSession(sessionId, examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for unit session: ", sessionId, ", ", examId);
                } else if (sess.getForceTerminate() == EForceTerminateState.NONE) {
                    sess.setForceTerminate(EForceTerminateState.SUBMIT_AND_SCORE_REQUESTED);
                } else {
                    Log.warning("'submit' request for unit session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("review_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final ReviewExamSession sess = ReviewExamSessionStore.getInstance().getReviewExamSession(sessionId,
                        examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for review session: ", sessionId, ", ", examId);
                } else if (sess.getForceTerminate() == EForceTerminateState.NONE) {
                    sess.setForceTerminate(EForceTerminateState.SUBMIT_AND_SCORE_REQUESTED);
                } else {
                    Log.warning("'submit' request for review session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else {
                Log.warning("Unrecognized type for 'submit' action: ", type);
            }
        } else if ("submitconfirm".equals(action)) {

            final String type = req.getParameter("type");

            if ("placement_exam".equals(type)) {
                final String studentId = req.getParameter("student_id");
                final PlacementExamSession sess = PlacementExamSessionStore.getInstance()
                        .getPlacementExamSessionForStudent(studentId);
                if (sess == null) {
                    Log.warning("Unrecognized student ID for placement session: ", studentId);
                } else if (sess.getForceTerminate() == EForceTerminateState.SUBMIT_AND_SCORE_REQUESTED) {
                    Log.warning("Forced submit of placement exam for student ", studentId);
                    sess.forceSubmit(cache, session);
                } else {
                    Log.warning("'submitconfirm' request for placement session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("unit_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final UnitExamSession sess = UnitExamSessionStore.getInstance().getUnitExamSession(sessionId, examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for unit session: ", sessionId, ", ", examId);
                } else if (sess.getForceTerminate() == EForceTerminateState.SUBMIT_AND_SCORE_REQUESTED) {
                    Log.warning("Forced submit of unit exam for student ", sess.studentId);
                    sess.forceSubmit(cache, session);
                } else {
                    Log.warning("'submitconfirm' request for unit session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("review_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final ReviewExamSession sess =
                        ReviewExamSessionStore.getInstance().getReviewExamSession(sessionId, examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for review session: ", sessionId, ", ", examId);
                } else if (sess.getForceTerminate() == EForceTerminateState.SUBMIT_AND_SCORE_REQUESTED) {
                    Log.warning("Forced submit of review exam for student ", sess.studentId);
                    sess.forceSubmit(cache, session);
                } else {
                    Log.warning("'submitconfirm' request for review session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else {
                Log.warning("Unrecognized type for 'submitconfirm' action: ", type);
            }
        } else if ("submitcancel".equals(action)) {

            final String type = req.getParameter("type");

            if ("placement_exam".equals(type)) {
                final String studentId = req.getParameter("student_id");
                final PlacementExamSession sess = PlacementExamSessionStore.getInstance()
                        .getPlacementExamSessionForStudent(studentId);
                if (sess == null) {
                    Log.warning("Unrecognized student ID for placement session: ", studentId);
                } else {
                    if (sess.getForceTerminate() == EForceTerminateState.SUBMIT_AND_SCORE_REQUESTED) {

                        // TODO:
                    } else {
                        Log.warning("'submitcancel' request for placement session in termination state: ",
                                sess.getForceTerminate().name());
                    }
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("unit_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final UnitExamSession sess = UnitExamSessionStore.getInstance().getUnitExamSession(sessionId, examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for unit session: ", sessionId, ", ", examId);
                } else {
                    if (sess.getForceTerminate() == EForceTerminateState.SUBMIT_AND_SCORE_REQUESTED) {

                        // TODO:
                    } else {
                        Log.warning("'submitcancel' request for unit session in termination state: ",
                                sess.getForceTerminate().name());
                    }
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else if ("review_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final ReviewExamSession sess = ReviewExamSessionStore.getInstance().getReviewExamSession(sessionId,
                        examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for review session: ", sessionId, ", ", examId);
                } else {
                    if (sess.getForceTerminate() == EForceTerminateState.SUBMIT_AND_SCORE_REQUESTED) {

                        // TODO:
                    } else {
                        Log.warning("'submitcancel' request for review session in termination state: ",
                                sess.getForceTerminate().name());
                    }
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else {
                Log.warning("Unrecognized type for 'submitcancel' action: ", type);
            }

        } else if ("terminate".equals(action)) {

            final String type = req.getParameter("type");

            if ("login".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final ImmutableSessionInfo sess = SessionManager.getInstance().getUserSession(sessionId);
                if (sess == null) {
                    Log.warning("Unrecognized login session ID: ", sessionId);
                } else {
                    // TODO:
                    Log.warning("Unimplemented: terminate login session");
                }
            } else if ("homework".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final HomeworkSession sess = HomeworkSessionStore.getInstance().getHomeworkSession(sessionId, examId);

                if (sess == null) {
                    Log.warning("Unrecognized homework session/exam ID: ", sessionId, ", ", examId);
                } else {
                    synchronized (sess) {
                        if (sess.getForceTerminate() == EForceTerminateState.NONE) {
                            sess.setForceTerminate(EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED);
                        } else {
                            Log.warning("'terminate' request for homework in termination state: ",
                                    sess.getForceTerminate().name());
                            sess.setForceTerminate(EForceTerminateState.NONE);
                        }
                    }
                }

            } else if ("past_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String xml = req.getParameter("xml");
                final PastExamSession sess = PastExamSessionStore.getInstance().getPastExamSession(sessionId, xml);
                if (sess == null) {
                    Log.warning("Unrecognized past exam session/XML path: ", sessionId, ", ", xml);
                } else if (sess.getForceTerminate() == EForceTerminateState.NONE) {
                    sess.setForceTerminate(EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED);
                } else {
                    Log.warning("'terminate' request for past exam in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else {
                Log.warning("Unrecognized type: ", type);
            }

        } else if ("terminateconfirm".equals(action)) {

            final String type = req.getParameter("type");

            if ("homework".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final HomeworkSession sess = HomeworkSessionStore.getInstance().getHomeworkSession(sessionId, examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for homework session: ",
                            sessionId, ", ", examId);
                } else {
                    synchronized (sess) {
                        if (sess.getForceTerminate() == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {
                            Log.warning("Forced abort of homework for student ", sess.studentId);
                            sess.forceAbort(session);
                        } else {
                            Log.warning("'terminateconfirm' request for homework session in termination state: ",
                                    sess.getForceTerminate().name());
                            sess.setForceTerminate(EForceTerminateState.NONE);
                        }
                    }
                }
            } else if ("past_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String xml = req.getParameter("xml");
                final PastExamSession sess = PastExamSessionStore.getInstance().getPastExamSession(sessionId, xml);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/XML for past exam session: ", sessionId, ", ", xml);
                } else if (sess.getForceTerminate() == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {
                    Log.warning("Forced abort of past exam for student ", sess.studentId);
                    sess.forceAbort(session);
                } else {
                    Log.warning("'terminateconfirm' request for past exam session in termination state: ",
                            sess.getForceTerminate().name());
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else {
                Log.warning("Unrecognized type for 'terminateconfirm' action: ", type);
            }
        } else if ("terminatecancel".equals(action)) {

            final String type = req.getParameter("type");

            if ("homework".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String examId = req.getParameter("exam_id");
                final HomeworkSession sess = HomeworkSessionStore.getInstance().getHomeworkSession(sessionId, examId);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/exam for homework session: ", sessionId, ", ", examId);
                } else {
                    synchronized (sess) {
                        if (sess.getForceTerminate() == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {

                            // TODO:
                        } else {
                            Log.warning("'terminatecancel' request for homework session in termination state: ",
                                    sess.getForceTerminate().name());
                        }
                        sess.setForceTerminate(EForceTerminateState.NONE);
                    }
                }
            } else if ("past_exam".equals(type)) {
                final String sessionId = req.getParameter("session_id");
                final String xml = req.getParameter("xml");
                final PastExamSession sess = PastExamSessionStore.getInstance().getPastExamSession(sessionId, xml);
                if (sess == null) {
                    Log.warning("Unrecognized session ID/XML for past exam session: ", sessionId, ", ", xml);
                } else {
                    if (sess.getForceTerminate() == EForceTerminateState.ABORT_WITHOUT_SCORING_REQUESTED) {

                        // TODO:

                    } else {
                        Log.warning("'terminatecancel' request for past exam session in termination state: ",
                                sess.getForceTerminate().name());
                    }
                    sess.setForceTerminate(EForceTerminateState.NONE);
                }
            } else {
                Log.warning("Unrecognized type for 'terminatecancel' action: ", type);
            }

        } else {
            Log.warning("Unrecognized action: ", action);
        }

        doGet(cache, site, req, resp, session);
    }
}
