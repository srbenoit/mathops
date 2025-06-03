package dev.mathops.web.host.testing.adminsys.genadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.host.testing.adminsys.AbstractSubsite;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import dev.mathops.web.host.testing.adminsys.genadmin.dbadmin.PageDbAdmin;
import dev.mathops.web.host.testing.adminsys.genadmin.dbadmin.PageDbAdminBatch;
import dev.mathops.web.host.testing.adminsys.genadmin.dbadmin.PageDbAdminBatchRun;
import dev.mathops.web.host.testing.adminsys.genadmin.dbadmin.PageDbAdminContexts;
import dev.mathops.web.host.testing.adminsys.genadmin.dbadmin.PageDbAdminContextsMetadata;
import dev.mathops.web.host.testing.adminsys.genadmin.dbadmin.PageDbAdminContextsProdViews;
import dev.mathops.web.host.testing.adminsys.genadmin.dbadmin.PageDbAdminContextsServer;
import dev.mathops.web.host.testing.adminsys.genadmin.dbadmin.PageDbAdminContextsTableMetadata;
import dev.mathops.web.host.testing.adminsys.genadmin.dbadmin.PageDbAdminQueries;
import dev.mathops.web.host.testing.adminsys.genadmin.dbadmin.PageDbAdminReport;
import dev.mathops.web.host.testing.adminsys.genadmin.dbadmin.PageDbAdminReports;
import dev.mathops.web.host.testing.adminsys.genadmin.logic.PageLogicCalendar;
import dev.mathops.web.host.testing.adminsys.genadmin.logic.PageLogicMilestones;
import dev.mathops.web.host.testing.adminsys.genadmin.logic.PageLogicPrerequisites;
import dev.mathops.web.host.testing.adminsys.genadmin.logic.PageLogicRegistrations;
import dev.mathops.web.host.testing.adminsys.genadmin.logic.PageLogicTesting;
import dev.mathops.web.host.testing.adminsys.genadmin.reports.PageCourseExamsReport;
import dev.mathops.web.host.testing.adminsys.genadmin.reports.PageCourseHomeworkReport;
import dev.mathops.web.host.testing.adminsys.genadmin.reports.PageElmReport;
import dev.mathops.web.host.testing.adminsys.genadmin.reports.PageMathPlanReport;
import dev.mathops.web.host.testing.adminsys.genadmin.reports.PagePlacementReport;
import dev.mathops.web.host.testing.adminsys.genadmin.reports.PagePrecalcReport;
import dev.mathops.web.host.testing.adminsys.genadmin.reports.PageReports;
import dev.mathops.web.host.testing.adminsys.genadmin.serveradmin.PageServerAdmin;
import dev.mathops.web.host.testing.adminsys.genadmin.serveradmin.PageServerAdminControl;
import dev.mathops.web.host.testing.adminsys.genadmin.serveradmin.PageServerAdminDiagnostics;
import dev.mathops.web.host.testing.adminsys.genadmin.serveradmin.PageServerAdminMaintenance;
import dev.mathops.web.host.testing.adminsys.genadmin.serveradmin.PageServerAdminSessions;
import dev.mathops.web.host.testing.adminsys.genadmin.siteadmin.PageSiteAdmin;
import dev.mathops.web.host.testing.adminsys.genadmin.student.PagePopulationPick;
import dev.mathops.web.host.testing.adminsys.genadmin.student.PageStudent;
import dev.mathops.web.host.testing.adminsys.genadmin.student.PageStudentCourseActivity;
import dev.mathops.web.host.testing.adminsys.genadmin.student.PageStudentCourseStatus;
import dev.mathops.web.host.testing.adminsys.genadmin.student.PageStudentInfo;
import dev.mathops.web.host.testing.adminsys.genadmin.student.PageStudentMathPlan;
import dev.mathops.web.host.testing.adminsys.genadmin.student.PageStudentPastExam;
import dev.mathops.web.host.testing.adminsys.genadmin.student.PageStudentPick;
import dev.mathops.web.host.testing.adminsys.genadmin.student.PageStudentPlacement;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * The root of the administrative site. Provides user login and buttons to enter subordinate administrative sites.
 * <p>
 * This servlet requires that all connections be secured (SSL/TLS), and uses the SSL session ID as the unique identifier
 * for the session. This prevents session hijacking as can be done in a URL-rewriting scheme.
 */
public final class GenAdminSubsite extends AbstractSubsite {

    /** Map from session ID to map from driver name to connection. */
    private static final Map<String, Map<String, Connection>> CONNECTIONS = new HashMap<>(10);

    /**
     * Constructs a new {@code GenAdminSubsite}.
     *
     * @param theSite the owning site
     */
    public GenAdminSubsite(final AdminSite theSite) {

        super(theSite);
    }

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param session the login session (known not to be null)
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void subsiteGet(final Cache cache, final String subpath,
                           final ImmutableSessionInfo session, final HttpServletRequest req,
                           final HttpServletResponse resp) throws IOException, SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            switch (subpath) {
                case "home.html" -> PageHome.doGet(cache, this.site, req, resp, session);
                case "student.html" -> PageStudent.doGet(cache, this.site, req, resp, session, null);
                case "test_student.html" -> PageTestStudent.doTestStudentsPage(cache, this.site, req, resp, session);
                case "utilities.html" -> PageUtilities.doUtilitiesPage(cache, this.site, req, resp, session);
                case "server_admin.html" -> PageServerAdmin.doServerAdminPage(cache, this.site, req, resp, session);
                case "srvadm_sessions.html" -> PageServerAdminSessions.doGet(cache, this.site, req, resp, session);
                case "srvadm_maintenance.html" ->
                        PageServerAdminMaintenance.doGet(cache, this.site, req, resp, session);
                case "srvadm_control.html" -> PageServerAdminControl.doGet(cache, this.site, req, resp, session);
                case "srvadm_diagnostics.html" ->
                        PageServerAdminDiagnostics.doGet(cache, this.site, req, resp, session);
                case "db_admin.html" -> PageDbAdmin.doDbAdminPage(cache, this.site, req, resp, session);
                case "dbadm_contexts.html" -> PageDbAdminContexts.doGet(cache, this.site, req, resp, session);
                case "dbadm_batch.html" -> PageDbAdminBatch.doGet(cache, this.site, req, resp, session);
                case "dbadm_batch_run.html" -> PageDbAdminBatchRun.doGet(cache, this.site, req, resp, session);
                case "dbadm_reports.html" -> PageDbAdminReports.doGet(cache, this.site, req, resp, session);
                case "dbadm_report.html" -> PageDbAdminReport.doGet(cache, this.site, req, resp, session);
                case "dbadm_queries.html" -> PageDbAdminQueries.doGet(cache, this.site, req, resp, session);
                case "db_admin_server.html" ->
                        PageDbAdminContextsServer.doGet(cache, this.site, req, resp, session, null);
                case "db_admin_server_logout.html" ->
                        PageDbAdminContextsServer.doGet(cache, this.site, req, resp, session, null);
                case "dbadm_metadata.html" -> PageDbAdminContextsMetadata.doGet(cache, this.site, req, resp, session);
                case "dbadm_cache_metadata.html" ->
                        PageDbAdminContextsTableMetadata.doGet(cache, this.site, req, resp, session);
                case "dbadm_prod_views.html" ->
                        PageDbAdminContextsProdViews.doGet(cache, this.site, req, resp, session);
                case "site_admin.html" -> PageSiteAdmin.doGet(cache, this.site, req, resp, session);
                case "monitor.html" -> PageReports.doGet(cache, this.site, req, resp, session);

                case "logic_testing.html" -> PageLogicTesting.doGet(cache, this.site, req, resp, session);
                case "logic_registrations.html" -> PageLogicRegistrations.doGet(cache, this.site, req, resp, session);
                case "logic_prerequisites.html" -> PageLogicPrerequisites.doGet(cache, this.site, req, resp, session);
                case "logic_calendar.html" -> PageLogicCalendar.doGet(cache, this.site, req, resp, session);
                case "logic_milestones.html" -> PageLogicMilestones.doGet(cache, this.site, req, resp, session);

                case "report_mathplan.html" -> PageMathPlanReport.doGet(cache, this.site, req, resp, session);
                case "report_placement.html" -> PagePlacementReport.doGet(cache, this.site, req, resp, session);
                case "report_elm.html" -> PageElmReport.doGet(cache, this.site, req, resp, session);
                case "report_precalc.html" -> PagePrecalcReport.doGet(cache, this.site, req, resp, session);
                case "report_course_exams.html" -> PageCourseExamsReport.doGet(cache, this.site, req, resp, session);
                case "report_course_homework.html" ->
                        PageCourseHomeworkReport.doGet(cache, this.site, req, resp, session);
                case "student_info.html" -> PageStudentInfo.doGet(cache, this.site, req, resp, session);
                case "student_placement.html" -> PageStudentPlacement.doGet(cache, this.site, req, resp, session);
                case "student_course_status.html" ->
                        PageStudentCourseStatus.doGet(cache, this.site, req, resp, session);
                case "student_course_activity.html" ->
                        PageStudentCourseActivity.doGet(cache, this.site, req, resp, session);
                case "student_math_plan.html" -> PageStudentMathPlan.doGet(cache, this.site, req, resp, session);
                case "xmlauthor.jnlp" -> PageUtilities.doXmlAuthor(this.site, req, resp);
                case "problemtester.jnlp" -> PageUtilities.doProblemTester(this.site, req, resp);
                case "examtester.jnlp" -> PageUtilities.doExamTester(this.site, req, resp);
                case "examprinter.jnlp" -> PageUtilities.doExamPrinter(this.site, req, resp);
                case "instructiontester.jnlp" -> PageUtilities.doInstructionTester(this.site, req, resp);
                case "glyphviewer.jnlp" -> PageUtilities.doGlyphViewer(this.site, req, resp);
                case "keyconfig.jnlp" -> PageUtilities.doKeyConfig(this.site, req, resp);
                case "pwdhash.jnlp" -> PageUtilities.doPasswordHash(this.site, req, resp);
                case "renamedirs.jnlp" -> PageUtilities.doRenameDirs(this.site, req, resp);
                case "jwabbit.jnlp" -> PageUtilities.doJWabbit(this.site, req, resp);
                case "see_past_exam.jnlp" -> doSeePastExam(req, resp);
                case null, default -> {
                    Log.warning("GET: unknown path '", subpath, "'");
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        } else {
            Log.warning("GET: invalid role");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * Processes a POST request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param session the login session (known not to be null)
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void subsitePost(final Cache cache, final String subpath, final ImmutableSessionInfo session,
                            final HttpServletRequest req, final HttpServletResponse resp) throws IOException,
            SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

            switch (subpath) {
                case "student_pick.html" -> PageStudentPick.doPost(cache, this.site, req, resp, session);
                case "population_pick.html" -> PagePopulationPick.doPost(cache, this.site, req, resp, session);

                case "student_info.html" -> PageStudentInfo.doGet(cache, this.site, req, resp, session);
                case "student_placement.html" -> PageStudentPlacement.doGet(cache, this.site, req, resp, session);
                case "student_course_status.html" ->
                        PageStudentCourseStatus.doGet(cache, this.site, req, resp, session);
                case "student_course_activity.html" ->
                        PageStudentCourseActivity.doGet(cache, this.site, req, resp, session);
                case "student_math_plan.html" -> PageStudentMathPlan.doGet(cache, this.site, req, resp, session);
                case "student_view_past_exam.html" ->
                        PageStudentPastExam.startPastExam(cache, this.site, req, resp, session);
                case "student_update_past_exam.html" ->
                        PageStudentPastExam.updatePastExam(cache, this.site, req, resp, session);

                case "maint_mode_update.html" -> PageServerAdminMaintenance.doMaintenanceModeUpdate(req, resp);

                case "teststu_update_student.html" -> PageTestStudent.updateStudent(cache, req, resp);
                case "teststu_update_special.html" -> PageTestStudent.updateSpecial(cache, req, resp);
                case "teststu_update_placement.html" -> PageTestStudent.updatePlacement(cache, this.site, req, resp);
                case "teststu_update_tutorial.html" -> PageTestStudent.updateTutorials(cache, req, resp);
                case "teststu_update_etext.html" -> PageTestStudent.updateETexts(cache, req, resp);
                case "teststu_update_reg.html" -> PageTestStudent.updateRegistrations(cache, req, resp);

                case "dbadm_update_banner.html" -> PageDbAdminContexts.doPost(cache, this.site, req, resp, session);
                case "dbadm_batch_run.html" -> PageDbAdminBatchRun.doPost(cache, this.site, req, resp, session);

                case "srvadm_sessions.html" -> PageServerAdminSessions.doPost(cache, this.site, req, resp, session);
                case "srvadm_control.html" -> PageServerAdminControl.doPost(cache, this.site, req, resp, session);
                case "srvadm_diagnostics.html" ->
                        PageServerAdminDiagnostics.doPost(cache, this.site, req, resp, session);

                case "logic_registrations.html" -> PageLogicRegistrations.doPost(cache, this.site, req, resp, session);
                case "logic_prerequisites.html" -> PageLogicPrerequisites.doPost(cache, this.site, req, resp, session);
                case "logic_milestones.html" -> PageLogicMilestones.doPost(cache, this.site, req, resp, session);

                case null, default -> {
                    Log.warning("POST: unknown path '", subpath, "'");
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        } else {
            Log.warning("POST: invalid role");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * Sends the JavaWebStart launch descriptor to view a past exam.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private void doSeePastExam(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        final String xml = req.getParameter("xml");
        final String upd = req.getParameter("upd");

        if (AbstractSite.isParamInvalid(xml) || AbstractSite.isParamInvalid(upd)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  xml='", xml, "'");
            Log.warning("  upd='", upd, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(300);

            final String scheme = req.getScheme();
            final String host = req.getServerName();
            final String port = Integer.toString(req.getServerPort());

            htm.addln("<?xml version='1.0' encoding='utf-8'?>");

            htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://", host, CoreConstants.COLON, port,
                    this.site.site.path, "'>");

            htm.addln("  <information>");
            htm.addln("    <title>Exam Review Tool</title>");
            htm.addln("    <vendor>Colorado State University</vendor>");
            htm.addln("    <homepage href='", this.site.site.path, "/index.html'/>");
            htm.addln("    <description>Exam Review Tool.</description>");
            htm.addln("  </information>");

            htm.addln("  <security>");
            htm.addln("    <all-permissions/>");
            htm.addln("  </security>");

            htm.addln("  <update check='always' policy='always'/>");

            htm.addln("  <resources>");
            htm.addln("    <j2se version='1.8+'/>");
            htm.addln("    <jar href='/www/jars/bls8.jar'/>");
            htm.addln("    <jar href='/www/jars/minfonts3.jar'/>");
            htm.addln("    <jar href='/www/jars/jwabbit.jar'/>");
            htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
            htm.addln("  </resources>");

            htm.addln("  <application-desc main-class='edu.colostate.math.app.examviewer.ExamViewerApp'>");
            htm.addln("    <argument>", scheme, "</argument>");
            htm.addln("    <argument>", host, "</argument>");
            htm.addln("    <argument>", port, "</argument>");
            htm.addln("    <argument>", xml, "</argument>");
            htm.addln("    <argument>", upd, "</argument>");
            htm.addln("  </application-desc>");
            htm.addln("</jnlp>");

            final long millis = System.currentTimeMillis();
            resp.setDateHeader("Expires", millis);
            resp.setDateHeader("Last-Modified", millis);

            AbstractSite.sendReply(req, resp, "application/x-java-jnlp-file", htm);
        }
    }

    /**
     * Gets the connections for a session and driver name.
     *
     * @param sessionId  the session ID
     * @param driverName the driver name
     * @return the connection, if any
     */
    public static Connection getConnection(final String sessionId, final String driverName) {

        synchronized (CONNECTIONS) {
            final Map<String, Connection> map = CONNECTIONS.get(sessionId);

            return map == null ? null : map.get(driverName);
        }
    }

    /**
     * Stores a connection that can be retrieved laster with {@code getConnection}.
     *
     * @param sessionId  the session ID
     * @param driverName the driver name
     * @param connection the connection to add
     */
    public static void addConnection(final String sessionId,
                                     final String driverName, final Connection connection) {

        synchronized (CONNECTIONS) {
            final Map<String, Connection> map = CONNECTIONS.computeIfAbsent(sessionId, s -> new HashMap<>(10));

            final Connection old = map.get(driverName);
            if (old != null) {
                try {
                    old.close();
                } catch (final SQLException ex) {
                    Log.warning(ex);
                }
            }

            map.put(driverName, connection);
        }
    }

    /**
     * Removes a connection that was stored with {@code addConnection}.
     *
     * @param sessionId  the session ID
     * @param driverName the driver name
     */
    public static void removeConnection(final String sessionId, final String driverName) {

        synchronized (CONNECTIONS) {
            final Map<String, Connection> map = CONNECTIONS.get(sessionId);

            if (map != null) {

                final Connection connection = map.get(driverName);
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (final SQLException ex) {
                        Log.warning(ex);
                    }
                    map.remove(driverName);

                    if (map.isEmpty()) {
                        CONNECTIONS.remove(sessionId);
                    }
                }
            }
        }
    }

//    /**
//     * Removes all connections associated with a session ID.
//     *
//     * @param sessionId the session ID
//     */
//     public void removeConnections(final String sessionId) {
//
//     synchronized (this.connections) {
//     Map<String, Connection> map = this.connections.get(sessionId);
//
//     if (map != null) {
//     for (final Connection connection : map.values()) {
//     try {
//     connection.close();
//     } catch (SQLException ex) {
//     Log.warning(ex);
//     }
//     }
//
//     map.clear();
//     this.connections.remove(sessionId);
//     }
//     }
//     }
}
