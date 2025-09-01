package dev.mathops.web.host.testing.adminsys.genadmin.student;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.schema.legacy.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdminTopic;
import dev.mathops.web.host.testing.adminsys.genadmin.GenAdminPage;
import dev.mathops.web.host.testing.adminsys.genadmin.PageError;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Pages that displays all information about a single selected student.
 */
public enum PageStudentInfo {
    ;

    /**
     * Shows the student information page (the student ID must be available in a request parameter named "stu").
     *
     * @param cache   the data cache
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String studentId = req.getParameter("stu");

        if (AbstractSite.isParamInvalid(studentId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  studentId='", studentId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (studentId == null) {
            PageError.doGet(cache, site, req, resp, session, "Student not found.");
        } else {
            final RawStudent student = RawStudentLogic.query(cache, studentId, false);

            if (student == null) {
                PageError.doGet(cache, site, req, resp, session, "Student not found.");
            } else {
                doStudentInfoPage(cache, site, req, resp, session, student);
            }
        }
    }

    /**
     * Shows the student information page for a provided student.
     *
     * @param cache   the data cache
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @param student the student for which to present information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doStudentInfoPage(final Cache cache, final AdminSite site, final ServletRequest req,
                                  final HttpServletResponse resp, final ImmutableSessionInfo session,
                                  final RawStudent student) throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.STUDENT_STATUS, htm);

        htm.sP("studentname").add("<strong class='largeish'>", student.getScreenName(), "</strong> (", student.stuId,
                ") &nbsp; <a class='ulink' href='student.html'>Clear</a>").eP();

        htm.addln("<nav class='menu'>");

        menuButton(htm, true, student.stuId, EAdminStudentCommand.STUDENT_INFO);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.PLACEMENT);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.REGISTRATIONS);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.ACTIVITY);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.MATH_PLAN);

        htm.add("</nav>");

        htm.addln("<main class='info'>");
        emitStudentInfo(htm, student);
        htm.addln("</main>");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits general student information and status.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param student the student record
     */
    private static void emitStudentInfo(final HtmlBuilder htm, final RawStudent student) {

        htm.sTable("report");

        htm.sTr().sTh().add("Application Term").eTh();
        htm.sTd().add(student.aplnTerm == null ? CoreConstants.EMPTY : student.aplnTerm).eTd();
        htm.eTr();

        htm.sTr().sTh().add("College/Dept.").eTh();
        htm.sTd().add(student.college == null ? CoreConstants.EMPTY : student.college,
                CoreConstants.SLASH, student.dept == null ? CoreConstants.EMPTY : student.dept).eTd();
        htm.eTr();

        htm.sTr().sTh().add("Transfer Credits").eTh();
        htm.sTd().add(student.trCredits == null ? CoreConstants.EMPTY : student.trCredits).eTd();
        htm.eTr();

        htm.sTr().sTh().add("High School GPA/Rank/Size").eTh();
        htm.sTd().add(student.hsGpa == null ? CoreConstants.EMPTY : student.hsGpa).add(CoreConstants.SLASH)
                .add(student.hsClassRank == null ? CoreConstants.EMPTY : student.hsClassRank).add(CoreConstants.SLASH)
                .add(student.hsSizeClass == null ? CoreConstants.EMPTY : student.hsSizeClass).eTd();
        htm.eTr();

        htm.sTr().sTh().add("SAT Math").eTh();
        htm.sTd().add(student.satScore == null ? CoreConstants.EMPTY : student.satScore).eTd();
        htm.eTr();

        htm.sTr().sTh().add("ACT Math").eTh();
        htm.sTd().add(student.actScore == null ? CoreConstants.EMPTY : student.actScore).eTd();
        htm.eTr();

        htm.sTr().sTh().add("AP Calculus").eTh();
        htm.sTd().add(student.apScore == null ? CoreConstants.EMPTY : student.apScore).eTd();
        htm.eTr();

        htm.sTr().sTh().add("Hold Severity").eTh();
        htm.sTd().add(student.sevAdminHold == null ? CoreConstants.EMPTY : student.sevAdminHold).eTd();
        htm.eTr();

        htm.sTr().sTh().add("Time Limit Factor").eTh();
        htm.sTd().add(student.timelimitFactor == null ? CoreConstants.EMPTY : student.timelimitFactor).eTd();
        htm.eTr();

        htm.sTr().sTh().add("Licensed").eTh();
        htm.sTd().add("Y".equals(student.licensed) ? "Yes" : "No").eTd();
        htm.eTr();

        htm.sTr().sTh().add("Campus").eTh();
        htm.sTd().add(student.campus == null ? CoreConstants.EMPTY : student.campus).eTd();
        htm.eTr();

        htm.sTr().sTh().add("E-mail").eTh();
        htm.sTd().add(student.stuEmail == null ? CoreConstants.EMPTY : student.stuEmail).eTd();
        htm.eTr();

        htm.sTr().sTh().add("Adviser E-mail").eTh();
        htm.sTd().add(student.adviserEmail == null ? CoreConstants.EMPTY : student.adviserEmail).eTd();
        htm.eTr();

        htm.sTr().sTh().add("Admit Type").eTh();
        htm.sTd().add(student.admitType == null ? CoreConstants.EMPTY : student.admitType).eTd();
        htm.eTr();

        htm.sTr().sTh().add("Pacing Structure").eTh();
        htm.sTd().add(student.pacingStructure == null ? CoreConstants.EMPTY : student.pacingStructure).eTd();
        htm.eTr();

        htm.eTable();
    }

    /**
     * Starts a navigation button.
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param selected  true if the button is selected
     * @param studentId the student ID
     * @param cmd       the command
     */
    private static void menuButton(final HtmlBuilder htm, final boolean selected, final String studentId,
                                   final EAdminStudentCommand cmd) {

        htm.addln("<form action='", cmd.url, "' method='post'>");

        htm.addln("<input type='hidden' name='stu' value='", studentId, "'/>");

        htm.add("<button type='submit'");
        if (selected) {
            htm.add(" class='menu selected' disabled");
        } else {
            htm.add(" class='menu'");
        }
        htm.add('>').add(cmd.label).add("</button>");

        htm.addln("</form>");
    }
}
