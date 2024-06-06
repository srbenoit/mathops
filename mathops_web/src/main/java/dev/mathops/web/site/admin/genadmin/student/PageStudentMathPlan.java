package dev.mathops.web.site.admin.genadmin.student;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.logic.mathplan.data.MathPlanConstants;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdminTopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;
import dev.mathops.web.site.admin.genadmin.PageError;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * Pages that displays all math plan status for a selected student.
 */
public enum PageStudentMathPlan {
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
    private static void doStudentInfoPage(final Cache cache, final AdminSite site, final ServletRequest req,
                                          final HttpServletResponse resp, final ImmutableSessionInfo session,
                                          final RawStudent student) throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.STUDENT_STATUS, htm);

        htm.sP("studentname").add("<strong class='largeish'>", student.getScreenName(), "</strong> (", student.stuId,
                ") &nbsp; <a class='ulink' href='student.html'>Clear</a>").eP();

        htm.addln("<nav class='menu'>");

        menuButton(htm, false, student.stuId, EAdminStudentCommand.STUDENT_INFO);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.PLACEMENT);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.REGISTRATIONS);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.ACTIVITY);
        menuButton(htm, true, student.stuId, EAdminStudentCommand.MATH_PLAN);

        htm.add("</nav>");

        htm.addln("<main class='info'>");
        emitMathPlanStatus(cache, htm, student);
        htm.addln("</main>");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Starts a navigation button.
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param selected  true if the button is selected
     * @param studentId the student ID
     * @param cmd       the command
     */
    private static void menuButton(final HtmlBuilder htm, final boolean selected,
                                   final String studentId, final EAdminStudentCommand cmd) {

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

    /**
     * Emits the student's math plan status.
     *
     * @param cache   the data cache
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param student the student
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitMathPlanStatus(final Cache cache, final HtmlBuilder htm,
                                           final RawStudent student) throws SQLException {

        final List<RawStmathplan> wlcm1 = RawStmathplanLogic.queryLatestByStudentPage(cache,
                student.stuId, MathPlanConstants.MAJORS_PROFILE);
        final List<RawStmathplan> wlcm2 = RawStmathplanLogic.queryLatestByStudentPage(cache,
                student.stuId, MathPlanConstants.PLAN_PROFILE);
        final List<RawStmathplan> wlcm3 = RawStmathplanLogic.queryLatestByStudentPage(cache,
                student.stuId, MathPlanConstants.ONLY_RECOM_PROFILE);
        final List<RawStmathplan> wlcm4 = RawStmathplanLogic.queryLatestByStudentPage(cache,
                student.stuId, MathPlanConstants.EXISTING_PROFILE);
        final List<RawStmathplan> wlcm5 = RawStmathplanLogic.queryLatestByStudentPage(cache,
                student.stuId, MathPlanConstants.INTENTIONS_PROFILE);
        final List<RawStmathplan> wlcm6 = RawStmathplanLogic.queryLatestByStudentPage(cache,
                student.stuId, MathPlanConstants.REVIEWED_PROFILE);
        final List<RawStmathplan> wlcm7 = RawStmathplanLogic.queryLatestByStudentPage(cache,
                student.stuId, MathPlanConstants.CHECKED_RESULTS_PROFILE);

        if (wlcm1.isEmpty() && wlcm2.isEmpty() && wlcm3.isEmpty() && wlcm4.isEmpty()
                && wlcm5.isEmpty() && wlcm6.isEmpty() && wlcm7.isEmpty()) {
            htm.addln("Math plan not completed.<ul></ul>");
        } else {
            if (wlcm1.isEmpty()) {
                htm.addln("Majors: None selected.<ul></ul>");
            } else {
                htm.addln("Majors:<ul>");
                for (final RawStmathplan row : wlcm1) {
                    htm.addln("<li>", row.surveyNbr, "=", row.stuAnswer, " [",
                            TemporalUtils.toLocalDateTime(row.examDt, row.finishTime), "]</li>");
                }
                htm.addln("</ul>");
            }

            if (wlcm2.isEmpty()) {
                htm.addln("Plan Summary: not completed.<ul></ul>");
            } else {
                htm.addln("Plan Summary:<ul>");
                for (final RawStmathplan row : wlcm2) {
                    htm.addln("<li>", row.surveyNbr, "=", row.stuAnswer, " [",
                            TemporalUtils.toLocalDateTime(row.examDt, row.finishTime), "]</li>");
                }
                htm.addln("</ul>");
            }

            if (wlcm3.isEmpty()) {
                htm.addln("Affirmation: not completed.<ul></ul>");
            } else {
                htm.addln("Affirmation:<ul>");
                for (final RawStmathplan row : wlcm3) {
                    htm.addln("<li>", row.surveyNbr, "=", row.stuAnswer, " [",
                            TemporalUtils.toLocalDateTime(row.examDt, row.finishTime), "]</li>");
                }
                htm.addln("</ul>");
            }

            if (wlcm4.isEmpty()) {
                htm.addln("Check Existing: not completed.<ul></ul>");
            } else {
                htm.addln("Check Existing:<ul>");
                for (final RawStmathplan row : wlcm4) {
                    htm.addln("<li>", row.surveyNbr, "=", row.stuAnswer, " [",
                            TemporalUtils.toLocalDateTime(row.examDt, row.finishTime), "]</li>");
                }
                htm.addln("</ul>");
            }

            if (wlcm5.isEmpty()) {
                htm.addln("Indicate intentions: not completed.<ul></ul>");
            } else {
                htm.addln("Indicate intentions:<ul>");
                for (final RawStmathplan row : wlcm5) {
                    htm.addln("<li>", row.surveyNbr, "=", row.stuAnswer, " [",
                            TemporalUtils.toLocalDateTime(row.examDt, row.finishTime), "]</li>");
                }
                htm.addln("</ul>");
            }

            if (wlcm6.isEmpty()) {
                htm.addln("MPE Review: not completed.<ul></ul>");
            } else {
                htm.addln("MPE Review:<ul>");
                for (final RawStmathplan row : wlcm6) {
                    htm.addln("<li>", row.surveyNbr, "=", row.stuAnswer, " [",
                            TemporalUtils.toLocalDateTime(row.examDt, row.finishTime), "]</li>");
                }
                htm.addln("</ul>");
            }
            if (wlcm7.isEmpty()) {
                htm.addln("MPE Results: not completed.<ul></ul>");
            } else {
                htm.addln("MPE Results:<ul>");
                for (final RawStmathplan row : wlcm7) {
                    htm.addln("<li>", row.surveyNbr, "=", row.stuAnswer, " [",
                            TemporalUtils.toLocalDateTime(row.examDt, row.finishTime), "]</li>");
                }
                htm.addln("</ul>");
            }
        }
    }
}
