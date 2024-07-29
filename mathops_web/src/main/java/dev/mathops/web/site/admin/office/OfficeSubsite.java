package dev.mathops.web.site.admin.office;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
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
                case "home.html" -> PageHome.doGet(cache, this.site, req, resp, session, null);
                case "student_info.html" -> PageStudentInfo.doGet(cache, this.site, req, resp, session);
                case "student_schedule.html" -> PageStudentSchedule.doGet(cache, this.site, req, resp, session);
                case "student_activity.html" -> PageStudentActivity.doGet(cache, this.site, req, resp, session);
                case "student_calendar.html" -> PageStudentCalendar.doGet(cache, this.site, req, resp, session);
                case "student_exams.html" -> PageStudentExams.doGet(cache, this.site, req, resp, session);
                case "student_placement.html" -> PageStudentPlacement.doGet(cache, this.site, req, resp, session);
                case "resource.html" -> PageResource.doGet(cache, this.site, req, resp, session);
                case "resource_loan.html" -> PageResourceLoan.doGet(cache, this.site, req, resp, session);
                case "resource_return.html" -> PageResourceReturn.doGet(cache, this.site, req, resp, session);
                case "resource_check.html" -> PageResourceCheck.doGet(cache, this.site, req, resp, session);
                case "testing.html" -> PageTesting.doGet(cache, this.site, req, resp, session);
                case "testing_issue_calc.html" -> PageTestingIssueCalc.doGet(cache, this.site, req, resp, session);
                case "testing_collect_calc.html" -> PageTestingCollectCalc.doGet(cache, this.site, req, resp, session);
                case "testing_issue_exam.html" -> PageTestingIssueExam.doGet(cache, this.site, req, resp, session);
                case "proctoring.html" -> PageProctoring.doGet(cache, this.site, req, resp, session);
                case "proctoring_teams.html" -> PageProctoringTeams.doGet(cache, this.site, req, resp, session);
                case "proctoring_challenge_teams.html" ->
                        PageProctoringChallengeTeams.doGet(cache, this.site, req, resp, session);
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
    public void subsitePost(final Cache cache, final String subpath,
                            final ImmutableSessionInfo session, final HttpServletRequest req,
                            final HttpServletResponse resp) throws IOException, SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

            switch (subpath) {
                case "student_pick.html" -> PageStudentPick.doPost(cache, this.site, req, resp, session);
                case "student_info.html" -> PageStudentInfo.doGet(cache, this.site, req, resp, session);
                case "student_schedule.html" -> PageStudentSchedule.doPost(cache, this.site, req, resp, session);
                case "student_activity.html" -> PageStudentActivity.doGet(cache, this.site, req, resp, session);
                case "student_exams.html" -> PageStudentExams.doGet(cache, this.site, req, resp, session);
                case "student_view_past_exam.html" ->
                        PageStudentViewPastExam.doPost(cache, this.site, req, resp, session);
                case "student_placement.html" -> PageStudentPlacement.doGet(cache, this.site, req, resp, session);
                case "resource.html" -> PageResource.doGet(cache, this.site, req, resp, session);
                case "resource_loan.html" -> PageResourceLoan.doPost(cache, this.site, req, resp, session);
                case "resource_return.html" -> PageResourceReturn.doPost(cache, this.site, req, resp, session);
                case "resource_check.html" -> PageResourceCheck.doPost(cache, this.site, req, resp, session);
                case "testing_issue_calc.html" -> PageTestingIssueCalc.doPost(cache, this.site, req, resp, session);
                case "testing_collect_calc.html" -> PageTestingCollectCalc.doPost(cache, this.site, req, resp, session);
                case "testing_issue_exam.html" -> PageTestingIssueExam.doPost(cache, this.site, req, resp, session);
                case "proctoring_teams.html" -> PageProctoringTeams.doPost(cache, this.site, req, resp, session);
                case "proctoring_challenge_teams.html" ->
                        PageProctoringChallengeTeams.doPost(cache, this.site, req, resp, session);
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
}
