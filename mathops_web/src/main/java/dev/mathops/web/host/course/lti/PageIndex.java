package dev.mathops.web.host.course.lti;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.schema.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;
import dev.mathops.session.login.AutoLoginProcessor;
import dev.mathops.session.login.IAuthenticationMethod;
import dev.mathops.session.sitelogic.servlet.StudentCourseStatus;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates the index page.
 */
final class PageIndex {

    /**
     * Private constructor to prevent instantiation.
     */
    private PageIndex() {

        super();
    }

    /**
     * Generates the page.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void showPage(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        // Log.info("GET request to index.html"); 

        final String exam = req.getParameter("exam");

        if (exam == null || exam.length() != 5) {
            Log.warning("Missing or invalid exam ID");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (AbstractSite.isParamInvalid(exam)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  exam='", exam, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            generatePage(req, resp, exam, CoreConstants.EMPTY, null);
        }
    }

    /**
     * Generates the page content.
     *
     * @param req          the request
     * @param resp         the response
     * @param exam         the exam ID
     * @param errorMessage an optional error message
     * @param initialStuId the initial value for the student ID field
     * @throws IOException if there is an error writing the response
     */
    static void generatePage(final ServletRequest req, final HttpServletResponse resp, final String exam,
                             final String initialStuId, final String errorMessage) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(2000);

        final String title;

        final String digits = exam.substring(0, 2);
        if ("MT".equals(digits)) {
            title = "ELM Exam";
        } else if (exam.endsWith("FIN")) {
            final String courseLabel = "MATH 1" + digits;
            title = courseLabel + " Final Exam";
        } else {
            final String courseLabel = "MATH 1" + digits;
            final String unitLabel = exam.substring(2, 3);
            title = courseLabel + " Unit " + unitLabel + " Exam";
        }

        Page.startPage(htm, title, false, false);

        htm.addln("<body style='background:white'>");

        htm.sH(1).add(title + ": Canvas Portal").eH(1);

        htm.sP(null, "style='color:#600;'").add("Please enter your 9-digit CSU ID number:").eP();

        htm.addln("<form action='gainaccess.html' method='post'>");
        htm.addln("<input type='hidden' id='exam' name='exam' value='", exam, "'/>");

        htm.sP();
        htm.add("<label style='display:inline-block;width:120px;text-align:right;padding-right:10px;' ",
                "for='stu'>Student ID:</label><input type='text' id='stu' name='stu' size='20'");
        if (initialStuId != null) {
            htm.add(" value='", initialStuId, "'");
        }
        htm.addln("/>");
        htm.eP();

        htm.sP(null, "style='margin-left:120px;'");
        htm.addln("<button class='btn' type='submit'>Submit</button/>");
        htm.eP();

        htm.addln("</form>");

        if (errorMessage != null) {
            htm.sP(null, "style='margin-left:120px;color:#900;'").add(errorMessage).eP();
        }

        htm.addln("</body>");
        htm.addln("</html>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Processes the POST submission of the access code form.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void processAccessCode(final Cache cache, final LtiSite site, final ServletRequest req,
                                  final HttpServletResponse resp) throws IOException, SQLException {

        final String exam = req.getParameter("exam");
        String stu = req.getParameter("stu");

        if (AbstractSite.isParamInvalid(exam)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  exam='", exam, "'");
            Log.warning("  stu='", stu, "'");
            generatePage(req, resp, exam, CoreConstants.EMPTY, "Invalid exam requested");
        } else if (AbstractSite.isParamInvalid(stu)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  exam='", exam, "'");
            Log.warning("  stu='", stu, "'");
            generatePage(req, resp, exam, CoreConstants.EMPTY, "Invalid student ID");
        } else if (exam.length() != 5) {
            Log.warning("Exam ID was ", exam);
            generatePage(req, resp, exam, CoreConstants.EMPTY, "Invalid exam requested");
        } else {
            stu = stu.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY)
                    .replace(CoreConstants.DASH, CoreConstants.EMPTY);
            if (stu.length() > 9) {
                stu = stu.substring(0, 9);
            }

            final HtmlBuilder htm = new HtmlBuilder(2000);

            final String title;
            final String course;

            final String digits = exam.substring(0, 2);
            if ("MT".equals(digits)) {
                title = "ELM Exam";
                course = RawRecordConstants.M100T;
            } else {
                final String courseLabel = "MATH 1" + digits;
                if (exam.endsWith("FIN")) {
                    title = courseLabel + " Final Exam";
                } else {
                    final String unitLabel = exam.substring(2, 3);
                    title = courseLabel + " Unit " + unitLabel + " Exam";
                }
                course = "M 1" + exam.substring(0, 2);
            }

            Page.startPage(htm, title, false, false);

            // Check eligibility for this exam - exam code is like "171UE" or "17FIN"
            int unitNum = 0;
            if (exam.endsWith("FIN")) {
                unitNum = 5;
            } else if (exam.charAt(2) == '1') {
                unitNum = 1;
            } else if (exam.charAt(2) == '2') {
                unitNum = 2;
            } else if (exam.charAt(2) == '3') {
                unitNum = 3;
            } else if (exam.charAt(2) == '4') {
                unitNum = 4;
            }

            final IAuthenticationMethod proc = new AutoLoginProcessor();
            final Map<String, String> fieldValues = new HashMap<>(3);
            fieldValues.put(AutoLoginProcessor.CSUID, stu);
            final SessionResult sr = SessionManager.getInstance().login(cache, proc, fieldValues,
                    site.getLiveRefreshes());
            final ImmutableSessionInfo session = sr.session;
            if (session == null) {
                Log.info("Student ", stu, " attempting to start ", title, ", can't create login session");
                generatePage(req, resp, exam, stu, "Unable to create an exam session");
            } else {
                final StudentCourseStatus courseStatus = new StudentCourseStatus(site.site.profile);

                courseStatus.gatherData(cache, session, stu, course, false, false);

                if (courseStatus.getStudent() == null) {
                    Log.info("Student ID ", stu, " not found - attempted to start unit exam");
                    generatePage(req, resp, exam, stu, "Invalid student ID");
                } else {
                    final boolean unitAvail = courseStatus.isProctoredExamAvailable(unitNum);

                    if (unitAvail) {
                        Log.info("Student ", stu, " attempting to start ", title, ": starting");
                        resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
                        final String redirect = "home.html?exam=" + exam + "&sid=" + session.loginSessionId;
                        resp.setHeader("Location", redirect);
                    } else {
                        Log.info("Student ", stu, " attempting to start ", title, ", but is not eligible: ",
                                courseStatus.getProctoredReason(unitNum));
                        generatePage(req, resp, exam, stu, "You are not currently eligible for this exam");
                    }
                }
            }
        }
    }
}
