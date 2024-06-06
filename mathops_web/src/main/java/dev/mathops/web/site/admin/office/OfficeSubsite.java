package dev.mathops.web.site.admin.office;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.admin.AbstractSubsite;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A sub-site that provides administrative functions for Precalculus Center office staff.
 */
public final class OfficeSubsite extends AbstractSubsite {

    /**
     * Constructs a new {@code OfficeSubsite}.
     *
     * @param theSite the owning site
     */
    public OfficeSubsite(final AdminSite theSite) {

        super(theSite);
    }

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param studentData the student data object
     * @param subpath     the portion of the path beyond that which was used to select this site
     * @param session     the login session (known not to be null)
     * @param req         the request
     * @param resp        the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void subsiteGet(final StudentData studentData, final String subpath,
                           final ImmutableSessionInfo session, final HttpServletRequest req,
                           final HttpServletResponse resp) throws IOException, SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            if ("home.html".equals(subpath)) {
                PageHome.doGet(studentData, this.site, req, resp, session, null);
            } else if ("student_info.html".equals(subpath)) {
                PageStudentInfo.doGet(studentData, this.site, req, resp, session);
            } else if ("student_schedule.html".equals(subpath)) {
                PageStudentSchedule.doGet(studentData, this.site, req, resp, session);
            } else if ("student_activity.html".equals(subpath)) {
                PageStudentActivity.doGet(studentData, this.site, req, resp, session);
            } else if ("student_calendar.html".equals(subpath)) {
                PageStudentCalendar.doGet(studentData, this.site, req, resp, session);
            } else if ("student_exams.html".equals(subpath)) {
                PageStudentExams.doGet(studentData, this.site, req, resp, session);
            } else if ("student_placement.html".equals(subpath)) {
                PageStudentPlacement.doGet(studentData, this.site, req, resp, session);
            } else if ("resource.html".equals(subpath)) {
                PageResource.doGet(studentData, this.site, req, resp, session);
            } else if ("resource_loan.html".equals(subpath)) {
                PageResourceLoan.doGet(studentData, this.site, req, resp, session);
            } else if ("resource_return.html".equals(subpath)) {
                PageResourceReturn.doGet(studentData, this.site, req, resp, session);
            } else if ("resource_check.html".equals(subpath)) {
                PageResourceCheck.doGet(studentData, this.site, req, resp, session);
            } else if ("testing.html".equals(subpath)) {
                PageTesting.doGet(studentData, this.site, req, resp, session);
            } else if ("testing_issue_calc.html".equals(subpath)) {
                PageTestingIssueCalc.doGet(studentData, this.site, req, resp, session);
            } else if ("testing_collect_calc.html".equals(subpath)) {
                PageTestingCollectCalc.doGet(studentData, this.site, req, resp, session);
            } else if ("testing_issue_exam.html".equals(subpath)) {
                PageTestingIssueExam.doGet(studentData, this.site, req, resp, session);
            } else if ("proctoring.html".equals(subpath)) {
                PageProctoring.doGet(studentData, this.site, req, resp, session);
            } else if ("proctoring_teams.html".equals(subpath)) {
                PageProctoringTeams.doGet(studentData, this.site, req, resp, session);
            } else if ("proctoring_challenge_teams.html".equals(subpath)) {
                PageProctoringChallengeTeams.doGet(studentData, this.site, req, resp, session);
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
     * @param studentData the student data object
     * @param subpath     the portion of the path beyond that which was used to select this site
     * @param session     the login session (known not to be null)
     * @param req         the request
     * @param resp        the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void subsitePost(final StudentData studentData, final String subpath,
                            final ImmutableSessionInfo session, final HttpServletRequest req,
                            final HttpServletResponse resp) throws IOException, SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

            if ("student_pick.html".equals(subpath)) {
                PageStudentPick.doPost(studentData, this.site, req, resp, session);
            } else if ("student_info.html".equals(subpath)) {
                PageStudentInfo.doGet(studentData, this.site, req, resp, session);
            } else if ("student_schedule.html".equals(subpath)) {
                PageStudentSchedule.doPost(studentData, this.site, req, resp, session);
            } else if ("student_activity.html".equals(subpath)) {
                PageStudentActivity.doGet(studentData, this.site, req, resp, session);
            } else if ("student_exams.html".equals(subpath)) {
                PageStudentExams.doGet(studentData, this.site, req, resp, session);
            } else if ("student_view_past_exam.html".equals(subpath)) {
                PageStudentViewPastExam.doPost(studentData, this.site, req, resp, session);
            } else if ("student_placement.html".equals(subpath)) {
                PageStudentPlacement.doGet(studentData, this.site, req, resp, session);
            } else if ("resource.html".equals(subpath)) {
                PageResource.doGet(studentData, this.site, req, resp, session);
            } else if ("resource_loan.html".equals(subpath)) {
                PageResourceLoan.doPost(studentData, this.site, req, resp, session);
            } else if ("resource_return.html".equals(subpath)) {
                PageResourceReturn.doPost(studentData, this.site, req, resp, session);
            } else if ("resource_check.html".equals(subpath)) {
                PageResourceCheck.doPost(studentData, this.site, req, resp, session);
            } else if ("testing_issue_calc.html".equals(subpath)) {
                PageTestingIssueCalc.doPost(studentData, this.site, req, resp, session);
            } else if ("testing_collect_calc.html".equals(subpath)) {
                PageTestingCollectCalc.doPost(studentData, this.site, req, resp, session);
            } else if ("testing_issue_exam.html".equals(subpath)) {
                PageTestingIssueExam.doPost(studentData, this.site, req, resp, session);
            } else if ("proctoring_teams.html".equals(subpath)) {
                PageProctoringTeams.doPost(studentData, this.site, req, resp, session);
            } else if ("proctoring_challenge_teams.html".equals(subpath)) {
                PageProctoringChallengeTeams.doPost(studentData, this.site, req, resp, session);
            } else {
                Log.warning("POST: unknown path '", subpath, "'");
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            Log.warning("POST: invalid role");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
