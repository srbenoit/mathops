package dev.mathops.web.site.admin.genadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.admin.AbstractSubsite;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.automation.PageAutomation;
import dev.mathops.web.site.admin.genadmin.automation.PageAutomationBot;
import dev.mathops.web.site.admin.genadmin.dbadmin.PageDbAdmin;
import dev.mathops.web.site.admin.genadmin.dbadmin.PageDbAdminBatch;
import dev.mathops.web.site.admin.genadmin.dbadmin.PageDbAdminBatchRun;
import dev.mathops.web.site.admin.genadmin.dbadmin.PageDbAdminContexts;
import dev.mathops.web.site.admin.genadmin.dbadmin.PageDbAdminContextsMetadata;
import dev.mathops.web.site.admin.genadmin.dbadmin.PageDbAdminContextsProdViews;
import dev.mathops.web.site.admin.genadmin.dbadmin.PageDbAdminContextsServer;
import dev.mathops.web.site.admin.genadmin.dbadmin.PageDbAdminContextsTableMetadata;
import dev.mathops.web.site.admin.genadmin.dbadmin.PageDbAdminQueries;
import dev.mathops.web.site.admin.genadmin.dbadmin.PageDbAdminReport;
import dev.mathops.web.site.admin.genadmin.dbadmin.PageDbAdminReports;
import dev.mathops.web.site.admin.genadmin.reports.PageCourseExamsReport;
import dev.mathops.web.site.admin.genadmin.reports.PageCourseHomeworkReport;
import dev.mathops.web.site.admin.genadmin.reports.PageElmReport;
import dev.mathops.web.site.admin.genadmin.reports.PageMathPlanReport;
import dev.mathops.web.site.admin.genadmin.reports.PagePlacementReport;
import dev.mathops.web.site.admin.genadmin.reports.PagePrecalcReport;
import dev.mathops.web.site.admin.genadmin.reports.PageReports;
import dev.mathops.web.site.admin.genadmin.serveradmin.PageServerAdmin;
import dev.mathops.web.site.admin.genadmin.serveradmin.PageServerAdminControl;
import dev.mathops.web.site.admin.genadmin.serveradmin.PageServerAdminDiagnostics;
import dev.mathops.web.site.admin.genadmin.serveradmin.PageServerAdminMaintenance;
import dev.mathops.web.site.admin.genadmin.serveradmin.PageServerAdminSessions;
import dev.mathops.web.site.admin.genadmin.siteadmin.PageSiteAdmin;
import dev.mathops.web.site.admin.genadmin.student.PagePopulationPick;
import dev.mathops.web.site.admin.genadmin.student.PageStudent;
import dev.mathops.web.site.admin.genadmin.student.PageStudentCourseActivity;
import dev.mathops.web.site.admin.genadmin.student.PageStudentCourseStatus;
import dev.mathops.web.site.admin.genadmin.student.PageStudentInfo;
import dev.mathops.web.site.admin.genadmin.student.PageStudentMathPlan;
import dev.mathops.web.site.admin.genadmin.student.PageStudentPastExam;
import dev.mathops.web.site.admin.genadmin.student.PageStudentPick;
import dev.mathops.web.site.admin.genadmin.student.PageStudentPlacement;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
     * @param data    the web view data
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param session the login session (known not to be null)
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void subsiteGet(final WebViewData data, final String subpath,
                           final ImmutableSessionInfo session, final HttpServletRequest req,
                           final HttpServletResponse resp) throws IOException, SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            if ("home.html".equals(subpath)) {
                PageHome.doGet(data, this.site, req, resp, session);
            } else if ("student.html".equals(subpath)) {
                PageStudent.doGet(data, this.site, req, resp, session, null);
            } else if ("test_student.html".equals(subpath)) {
                PageTestStudent.doTestStudentsPage(data, this.site, req, resp, session);
            } else if ("utilities.html".equals(subpath)) {
                PageUtilities.doUtilitiesPage(data, this.site, req, resp, session);
            } else if ("server_admin.html".equals(subpath)) {
                PageServerAdmin.doGet(data, this.site, req, resp, session);
            } else if ("srvadm_sessions.html".equals(subpath)) {
                PageServerAdminSessions.doGet(data, this.site, req, resp, session);
            } else if ("srvadm_maintenance.html".equals(subpath)) {
                PageServerAdminMaintenance.doGet(data, this.site, req, resp, session);
            } else if ("srvadm_control.html".equals(subpath)) {
                PageServerAdminControl.doGet(data, this.site, req, resp, session);
            } else if ("srvadm_diagnostics.html".equals(subpath)) {
                PageServerAdminDiagnostics.doGet(data, this.site, req, resp, session);
            } else if ("db_admin.html".equals(subpath)) {
                PageDbAdmin.doDbAdminPage(data, this.site, req, resp, session);
            } else if ("dbadm_contexts.html".equals(subpath)) {
                PageDbAdminContexts.doGet(data, this.site, req, resp, session);
            } else if ("dbadm_batch.html".equals(subpath)) {
                PageDbAdminBatch.doGet(data, this.site, req, resp, session);
            } else if ("dbadm_batch_run.html".equals(subpath)) {
                PageDbAdminBatchRun.doGet(data, this.site, req, resp, session);
            } else if ("dbadm_reports.html".equals(subpath)) {
                PageDbAdminReports.doGet(data, this.site, req, resp, session);
            } else if ("dbadm_report.html".equals(subpath)) {
                PageDbAdminReport.doGet(data, this.site, req, resp, session);
            } else if ("dbadm_queries.html".equals(subpath)) {
                PageDbAdminQueries.doGet(data, this.site, req, resp, session);
            } else if ("db_admin_server.html".equals(subpath)) {
                PageDbAdminContextsServer.doGet(data, this.site, req, resp, session, null);
            } else if ("db_admin_server_logout.html".equals(subpath)) {
                PageDbAdminContextsServer.doGet(data, this.site, req, resp, session);
            } else if ("dbadm_metadata.html".equals(subpath)) {
                PageDbAdminContextsMetadata.doGet(data, this.site, req, resp, session);
            } else if ("dbadm_cache_metadata.html".equals(subpath)) {
                PageDbAdminContextsTableMetadata.doGet(data, this.site, req, resp, session);
            } else if ("dbadm_prod_views.html".equals(subpath)) {
                PageDbAdminContextsProdViews.doGet(data, this.site, req, resp, session);
            } else if ("site_admin.html".equals(subpath)) {
                PageSiteAdmin.doGet(data, this.site, req, resp, session);
            } else if ("automation.html".equals(subpath)) {
                PageAutomation.doGet(data, this.site, req, resp, session);
            } else if ("automation_bot.html".equals(subpath)) {
                PageAutomationBot.doGet(data, this.site, req, resp, session);
            } else if ("monitor.html".equals(subpath)) {
                PageReports.doGet(data, this.site, req, resp, session);
            } else if ("report_mathplan.html".equals(subpath)) {
                PageMathPlanReport.doGet(data, this.site, req, resp, session);
            } else if ("report_placement.html".equals(subpath)) {
                PagePlacementReport.doGet(data, this.site, req, resp, session);
            } else if ("report_elm.html".equals(subpath)) {
                PageElmReport.doGet(data, this.site, req, resp, session);
            } else if ("report_precalc.html".equals(subpath)) {
                PagePrecalcReport.doGet(data, this.site, req, resp, session);
            } else if ("report_course_exams.html".equals(subpath)) {
                PageCourseExamsReport.doGet(data, this.site, req, resp, session);
            } else if ("report_course_homework.html".equals(subpath)) {
                PageCourseHomeworkReport.doGet(data, this.site, req, resp, session);
            } else if ("student_info.html".equals(subpath)) {
                PageStudentInfo.doGet(data, this.site, req, resp, session);
            } else if ("student_placement.html".equals(subpath)) {
                PageStudentPlacement.doGet(data, this.site, req, resp, session);
            } else if ("student_course_status.html".equals(subpath)) {
                PageStudentCourseStatus.doGet(data, this.site, req, resp, session);
            } else if ("student_course_activity.html".equals(subpath)) {
                PageStudentCourseActivity.doGet(data, this.site, req, resp, session);
            } else if ("student_math_plan.html".equals(subpath)) {
                PageStudentMathPlan.doGet(data, this.site, req, resp, session);
            } else if ("xmlauthor.jnlp".equals(subpath)) {
                PageUtilities.doXmlAuthor(this.site, req, resp);
            } else if ("problemtester.jnlp".equals(subpath)) {
                PageUtilities.doProblemTester(this.site, req, resp);
            } else if ("examtester.jnlp".equals(subpath)) {
                PageUtilities.doExamTester(this.site, req, resp);
            } else if ("examprinter.jnlp".equals(subpath)) {
                PageUtilities.doExamPrinter(this.site, req, resp);
            } else if ("instructiontester.jnlp".equals(subpath)) {
                PageUtilities.doInstructionTester(this.site, req, resp);
            } else if ("glyphviewer.jnlp".equals(subpath)) {
                PageUtilities.doGlyphViewer(this.site, req, resp);
            } else if ("keyconfig.jnlp".equals(subpath)) {
                PageUtilities.doKeyConfig(this.site, req, resp);
            } else if ("pwdhash.jnlp".equals(subpath)) {
                PageUtilities.doPasswordHash(this.site, req, resp);
            } else if ("renamedirs.jnlp".equals(subpath)) {
                PageUtilities.doRenameDirs(this.site, req, resp);
            } else if ("jwabbit.jnlp".equals(subpath)) {
                PageUtilities.doJWabbit(this.site, req, resp);
            } else if ("see_past_exam.jnlp".equals(subpath)) {
                doSeePastExam(req, resp);
            } else {
                Log.warning("GET: unknown path '", subpath, "'");
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
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
     * @param data    the web view data
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param session the login session (known not to be null)
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void subsitePost(final WebViewData data, final String subpath, final ImmutableSessionInfo session,
                            final HttpServletRequest req, final HttpServletResponse resp) throws IOException,
            SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            final String stuId = session.getEffectiveUserId();
            LogBase.setSessionInfo(session.loginSessionId, stuId);

            if ("student_pick.html".equals(subpath)) {
                PageStudentPick.doPost(data, this.site, req, resp, session);
            } else if ("population_pick.html".equals(subpath)) {
                PagePopulationPick.doPost(data, this.site, req, resp, session);
            } else if ("student_info.html".equals(subpath)) {
                PageStudentInfo.doGet(data, this.site, req, resp, session);
            } else if ("student_placement.html".equals(subpath)) {
                PageStudentPlacement.doGet(data, this.site, req, resp, session);
            } else if ("student_course_status.html".equals(subpath)) {
                PageStudentCourseStatus.doGet(data, this.site, req, resp, session);
            } else if ("student_course_activity.html".equals(subpath)) {
                PageStudentCourseActivity.doGet(data, this.site, req, resp, session);
            } else if ("student_math_plan.html".equals(subpath)) {
                PageStudentMathPlan.doGet(data, this.site, req, resp, session);
            } else if ("student_view_past_exam.html".equals(subpath)) {
                PageStudentPastExam.startPastExam(data, this.site, req, resp, session);
            } else if ("student_update_past_exam.html".equals(subpath)) {
                PageStudentPastExam.updatePastExam(data, this.site, req, resp, session);
            } else if ("maint_mode_update.html".equals(subpath)) {
                PageServerAdminMaintenance.doMaintenanceModeUpdate(req, resp);
            } else if ("teststu_update_student.html".equals(subpath)) {
                PageTestStudent.updateStudent(data, req, resp);
            } else if ("teststu_update_special.html".equals(subpath)) {
                PageTestStudent.updateSpecial(data, req, resp);
            } else if ("teststu_update_placement.html".equals(subpath)) {
                PageTestStudent.updatePlacement(data, this.site, req, resp);
            } else if ("teststu_update_tutorial.html".equals(subpath)) {
                PageTestStudent.updateTutorials(data, req, resp);
            } else if ("teststu_update_etext.html".equals(subpath)) {
                PageTestStudent.updateETexts(data, req, resp);
            } else if ("teststu_update_reg.html".equals(subpath)) {
                PageTestStudent.updateRegistrations(data, req, resp);
            } else if ("db_admin_server_login.html".equals(subpath)) {
                PageDbAdminContextsServer.doPost(data, this.site, req, resp, session);
            } else if ("dbadm_update_banner.html".equals(subpath)) {
                PageDbAdminContexts.doPost(data, this.site, req, resp, session);
            } else if ("dbadm_batch_run.html".equals(subpath)) {
                PageDbAdminBatchRun.doPost(data, this.site, req, resp, session);
            } else if ("srvadm_sessions.html".equals(subpath)) {
                PageServerAdminSessions.doPost(data, this.site, req, resp, session);
            } else if ("srvadm_control.html".equals(subpath)) {
                PageServerAdminControl.doPost(data, this.site, req, resp, session);
            } else if ("srvadm_diagnostics.html".equals(subpath)) {
                PageServerAdminDiagnostics.doPost(data, this.site, req, resp, session);
            } else {
                Log.warning("POST: unknown path '", subpath, "'");
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
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
            final int serverPort = req.getServerPort();
            final String port = Integer.toString(serverPort);

            htm.addln("<?xml version='1.0' encoding='utf-8'?>");

            htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://", host, CoreConstants.COLON, port,
                    this.site.siteProfile.path, "'>");

            htm.addln("  <information>");
            htm.addln("    <title>Exam Review Tool</title>");
            htm.addln("    <vendor>Colorado State University</vendor>");
            htm.addln("    <homepage href='", this.site.siteProfile.path, "/index.html'/>");
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

            final long now = System.currentTimeMillis();
            resp.setDateHeader("Expires", now);
            resp.setDateHeader("Last-Modified", now);

            final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
            AbstractSite.sendReply(req, resp, "application/x-java-jnlp-file", bytes);
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
}
