package dev.mathops.web.site.admin.office;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawSemesterCalendar;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Pages that displays a record of a student's activity in calendar form.
 */
enum PageStudentCalendar {
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
    static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String studentId = req.getParameter("stu");

        if (AbstractSite.isParamInvalid(studentId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  studentId='", studentId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (studentId == null) {
            PageHome.doGet(cache, site, req, resp, session, "Student not found.");
        } else {
            final RawStudent student = RawStudentLogic.query(cache, studentId, false);

            if (student == null) {
                PageHome.doGet(cache, site, req, resp, session, "Student not found.");
            } else {
                doStudentCalendarPage(cache, site, req, resp, session, student);
            }
        }
    }

    /**
     * Shows the student calendar page for a provided student.
     *
     * @param cache   the data cache
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param student the student for which to present information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void doStudentCalendarPage(final Cache cache, final AdminSite site,
                                              final ServletRequest req, final HttpServletResponse resp,
                                              final ImmutableSessionInfo session, final RawStudent student)
            throws IOException, SQLException {

        final HtmlBuilder htm = OfficePage.startOfficePage(cache, site, session, true);

        htm.sP("studentname").add("<strong>", student.getScreenName(), "</strong> &nbsp; <strong><code>",
                student.stuId, "</code></strong>").eP();

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {

            htm.sDiv("narrowstack");
            htm.addln("<form method='get' action='student_info.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Registrations</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_schedule.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Schedule</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_activity.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='navlit'>Activity</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_exams.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Exams</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_placement.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Placement</button>");
            htm.addln("</form>");
            htm.eDiv(); // narrowstack

            htm.sDiv("detail");
            emitStudentActivity(cache, htm, student);
            htm.eDiv(); // detail
        }

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits the student's activity history.
     *
     * @param cache   the data cache
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param student the student
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitStudentActivity(final Cache cache, final HtmlBuilder htm,
                                            final RawStudent student) throws SQLException {

        final TermRec term = cache.getSystemData().getActiveTerm();

        if (term == null) {
            htm.add("(Unable to query the active term)");
        } else {
            htm.sDiv();
            htm.addln("<form method='get' action='student_activity.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button type='submit' class='btn' style='margin:0;'>Switch to Table View</button>");
            htm.addln("</form>");
            htm.eDiv();
            htm.div("vgap0");

            final List<RawSemesterCalendar> weeks = cache.getSystemData().getSemesterCalendars();

            final List<RawCampusCalendar> holidays = cache.getSystemData().getCampusCalendarsByType(
                    RawCampusCalendar.DT_DESC_HOLIDAY);

            final List<RawStmpe> mpes = RawStmpeLogic.queryLegalByStudent(cache, student.stuId);

            final List<RawStchallenge> chals =
                    RawStchallengeLogic.queryByStudent(cache, student.stuId);

            final List<RawStexam> exams = RawStexamLogic.getExams(cache, student.stuId, true);

            final List<RawSthomework> homeworks =
                    RawSthomeworkLogic.queryByStudent(cache, student.stuId, false);

            if (exams.isEmpty() && mpes.isEmpty() && homeworks.isEmpty()) {
                htm.add("(No course activity found)");
            } else {
                emitLegend(htm);
                emitCalendar(htm, weeks, holidays, mpes, chals, exams, homeworks);
            }
        }
    }

    /**
     * Emits a right-float legend
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitLegend(final HtmlBuilder htm) {

        htm.sDiv("right", "style='border:2px solid gray;min-width:120px;padding-top:4px;background:#f4f4f4'");
        htm.sH(4, "center").add("Legend").eH(4);

        htm.sDiv(null, "style='border:1px solid gray; margin:2px;background:white;font-size:14px;'");

        htm.sDiv(null, "style='margin:4px;'")
                .add("<span class='circle calmpe'>&nbsp;</span> &nbsp; Placement Attempts")
                .eDiv();

        htm.sDiv(null, "style='margin:4px;'")
                .add("<span class='circle calchal'>&nbsp;</span> &nbsp; Challenge Attempts")
                .eDiv();

        htm.sDiv(null, "style='margin:4px;'")
                .add("<span class='circle calusers'>&nbsp;</span> &nbsp; User's Exams")
                .eDiv();

        htm.sDiv(null, "style='margin:4px;'")
                .add("<span class='circle calsr'>&nbsp;</span> &nbsp; Skills Review Exams")
                .eDiv();

        htm.sDiv(null, "style='margin:4px;'")
                .add("<span class='circle calrev'>&nbsp;</span> &nbsp; Unit Review Exams")
                .eDiv();

        htm.sDiv(null, "style='margin:4px;'")
                .add("<span class='circle calunit'>&nbsp;</span> &nbsp; Unit/Final Exams")
                .eDiv();

        htm.sDiv(null, "style='margin:4px;'")
                .add("<span class='circle calhw'>&nbsp;</span> &nbsp; Homework Assignments")
                .eDiv();

        htm.eDiv();
        htm.eDiv();
    }

    /**
     * Emits the calendar display of student activity
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param weeks     the list of term weeks
     * @param holidays  the list of 'holiday' calendar records
     * @param mpes      the list of student placement attempt records
     * @param chals     the list of student challenge attempt records
     * @param exams     the list of student exam records
     * @param homeworks the list of student homework records
     */
    private static void emitCalendar(final HtmlBuilder htm,
                                     final List<RawSemesterCalendar> weeks,
                                     final Iterable<RawCampusCalendar> holidays,
                                     final Iterable<RawStmpe> mpes,
                                     final Iterable<RawStchallenge> chals,
                                     final Iterable<RawStexam> exams,
                                     final Iterable<RawSthomework> homeworks) {

        Collections.sort(weeks);

        // Find range of dates that represents all complete months that the term overlaps

        LocalDate start = weeks.getFirst().startDt;
        final Month startMonth = start.getMonth();
        while (start.getMonth() == startMonth) {
            start = start.minusDays(1L);
        }
        start = start.plusDays(1L);

        LocalDate end = weeks.getLast().endDt;
        final Month endMonth = end.getMonth();
        while (end.getMonth() == endMonth) {
            end = end.plusDays(1L);
        }

        // "start" is the first day to show, "end" is day after last day to show

        // Emit the calendar
        Month curMonth = null;
        LocalDate curDate = start;
        final LocalDate today = LocalDate.now();
        while (!curDate.equals(end)) {

            int numPlacement = 0;
            int numChallenge = 0;
            int numUsers = 0;
            int numSkillsReview = 0;
            int numReviewExam = 0;
            int numUnitFinalExam = 0;
            int numHw = 0;

            for (final RawStmpe mpe : mpes) {
                if (mpe.examDt.equals(curDate)) {
                    ++numPlacement;
                }
            }
            for (final RawStchallenge chal : chals) {
                if (chal.examDt.equals(curDate)) {
                    ++numChallenge;
                }
            }
            for (final RawStexam ex : exams) {
                if (ex.examDt.equals(curDate)) {

                    if ("Q".equals(ex.examType)) {
                        ++numUsers;
                    } else if ("R".equals(ex.examType)) {
                        if (Integer.valueOf(0).equals(ex.unit)) {
                            ++numSkillsReview;
                        } else {
                            ++numReviewExam;
                        }
                    } else if ("U".equals(ex.examType)
                            || "F".equals(ex.examType)) {
                        ++numUnitFinalExam;
                    }
                }
            }

            for (final RawSthomework hw : homeworks) {
                if (hw.hwDt.equals(curDate)) {
                    ++numHw;
                }
            }

            if (curDate.getMonth() != curMonth) {
                if (curMonth != null) {

                    // Emit empty blocks up to first day if needed
                    final int day = curDate.getDayOfWeek().getValue(); // 1=monday, 7=sunday
                    for (int i = day; i < 7; ++i) {
                        htm.sTd("calempty").eTd();
                    }

                    // End prior month
                    htm.eTr().eTable();
                    htm.div("vgap0");
                }

                curMonth = curDate.getMonth();

                // Emit month header row
                htm.sTable("calendar");
                htm.sTr().sTd("calmonth", "colspan='7'")
                        .add(curMonth.getDisplayName(TextStyle.FULL, Locale.US), ", ",
                                Integer.toString(curDate.getYear()))
                        .eTd().eTr();

                htm.sTr().sTd("caldays").add("Sun").eTd()
                        .sTd("caldays").add("Mon").eTd()
                        .sTd("caldays").add("Tue").eTd()
                        .sTd("caldays").add("Wed").eTd()
                        .sTd("caldays").add("Thu").eTd()
                        .sTd("caldays").add("Fri").eTd()
                        .sTd("caldays").add("Sat").eTd()
                        .eTr();

                htm.sTr();

                // Emit empty blocks up to first day if needed
                final int day = curDate.getDayOfWeek().getValue(); // 1=monday, 7=sunday
                if (day < 7) {
                    for (int i = 0; i < day; ++i) {
                        htm.sTd("calempty").eTd();
                    }
                }
            }

            // See if the day is special

            String addClass = CoreConstants.EMPTY;
            if (curDate.equals(today)) {
                addClass = "today";
            }
            boolean isHoliday = false;
            for (final RawCampusCalendar test : holidays) {
                if (test.campusDt.equals(curDate)) {
                    isHoliday = true;
                    break;
                }
            }

            // Emit the day
            RawSemesterCalendar inWeek = null;
            for (final RawSemesterCalendar week : weeks) {
                if (curDate.isBefore(week.startDt) || curDate.isAfter(week.endDt)) {
                    continue;
                }
                inWeek = week;
            }

            final int dayOfWeek = curDate.getDayOfWeek().getValue();

            if (isHoliday) {
                htm.sTd("caldayout" + addClass).add(curDate.getDayOfMonth());
                htm.sDiv("right").add("<span class='weeknum'>Closed</span>").eDiv().div("clear");
            } else if (inWeek == null || inWeek.weekNbr.intValue() == 0
                    || inWeek.weekNbr.intValue() > 15) {
                htm.sTd("caldayout" + addClass).add(curDate.getDayOfMonth());
            } else if ((dayOfWeek == 1) || (curDate.getDayOfMonth() == 1 && dayOfWeek < 6)) {
                htm.sTd("caldayin" + addClass).add(Integer.toString(curDate.getDayOfMonth()));
                htm.sDiv("right").add("<span class='weeknum'>Week ", inWeek.weekNbr, "</span>")
                        .eDiv().div("clear");
            } else if (dayOfWeek < 6) {
                htm.sTd("caldayin" + addClass).add(curDate.getDayOfMonth());
            } else {
                htm.sTd("caldayout" + addClass).add(curDate.getDayOfMonth());
            }

            // Add activity marks.
            htm.sDiv("center");
            if (numPlacement > 0) {
                htm.add("<span class='circle calmpe'>", Integer.toString(numPlacement), "</span>");
            }
            if (numChallenge > 0) {
                htm.add("<span class='circle calchal'>", Integer.toString(numChallenge), "</span>");
            }
            if (numUsers > 0) {
                htm.add("<span class='circle calusers'>", Integer.toString(numUsers), "</span>");
            }
            if (numSkillsReview > 0) {
                htm.add("<span class='circle calsr'>", Integer.toString(numSkillsReview), "</span>");
            }
            if (numReviewExam > 0) {
                htm.add("<span class='circle calrev'>", Integer.toString(numReviewExam), "</span>");
            }
            if (numUnitFinalExam > 0) {
                htm.add("<span class='circle calunit'>", Integer.toString(numUnitFinalExam), "</span>");
            }
            if (numHw > 0) {
                htm.add("<span class='circle calhw'>", Integer.toString(numHw), "</span>");
            }
            htm.eDiv(); // center
            htm.eTd();

            if (dayOfWeek == 6) {
                // Start a new week row
                htm.eTr().sTr();
            }

            curDate = curDate.plusDays(1L);
        }

        final int day = curDate.getDayOfWeek().getValue(); // 1=monday, 7=sunday
        for (int i = day; i < 7; ++i) {
            htm.sTd("calempty").eTd();
        }
        htm.eTr().eTable();
    }
}
