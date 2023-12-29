package dev.mathops.web.site.admin.office;

import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawSemesterCalendarLogic;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawSemesterCalendar;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pages that displays a record of a student's activity in tabular form.
 */
enum PageStudentActivity {
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
                doStudentActivityPage(cache, site, req, resp, session, student);
            }
        }
    }

    /**
     * Shows the student activity page for a provided student.
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
    private static void doStudentActivityPage(final Cache cache, final AdminSite site,
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

        final TermRec term = TermLogic.get(cache).queryActive(cache);

        if (term == null) {
            htm.add("(Unable to query the active term)");
        } else {
            htm.sDiv();
            htm.addln("<form method='get' action='student_calendar.html'>");
            htm.addln("<input type='hidden' name='stu' value='",
                    student.stuId, "'/>");
            htm.addln(
                    "<button type='submit' class='btn' style='margin:0;'>Switch to Calendar View</button>");
            htm.addln("</form>");
            htm.eDiv();
            htm.div("vgap0");

            final List<RawSemesterCalendar> weeks =
                    RawSemesterCalendarLogic.INSTANCE.queryAll(cache);

            final List<RawStmpe> mpes = RawStmpeLogic.queryLegalByStudent(cache, student.stuId);
            mpes.sort(new RawStmpe.FinishDateTimeComparator());

            final List<RawStchallenge> chals =
                    RawStchallengeLogic.queryByStudent(cache, student.stuId);
            chals.sort(new RawStchallenge.FinishDateTimeComparator());

            final List<RawStexam> exams = RawStexamLogic.queryByStudent(cache, student.stuId, true);
            exams.sort(new RawStexam.FinishDateTimeComparator());

            final List<RawSthomework> homeworks =
                    RawSthomeworkLogic.queryByStudent(cache, student.stuId, false);
            homeworks.sort(new RawSthomework.FinishDateTimeComparator());

            if (mpes.isEmpty() && chals.isEmpty() && exams.isEmpty() && homeworks.isEmpty()) {
                htm.add("(No course activity found)");
            } else {
                emitActivityTable(htm, weeks, mpes, chals, exams, homeworks);
            }
        }
    }

    /**
     * Emits the table containing student activity.
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param weeks     the list of term weeks
     * @param mpes      the list of student placement attempt records
     * @param chals     the list of student challenge attempt records
     * @param exams     the list of student exam records
     * @param homeworks the list of student homework records
     */
    private static void emitActivityTable(final HtmlBuilder htm,
                                          final List<RawSemesterCalendar> weeks,
                                          final List<RawStmpe> mpes,
                                          final List<RawStchallenge> chals,
                                          final List<RawStexam> exams,
                                          final List<RawSthomework> homeworks) {

        Collections.sort(weeks); // Sorts by start date by default

        final LocalDate today = LocalDate.now();

        htm.sTable("report", "style='border-collapse:separate;border-spacing:0;min-width:688px;'");
        htm.sTr();
        htm.sTh().add("Week").eTh();
        htm.sTh().add("Course").eTh();
        htm.sTh().add("Unit").eTh();
        htm.sTh().add("Activity").eTh();
        htm.sTh().add("ID").eTh();
        htm.sTh().add("Submitted").eTh();
        htm.sTh().add("Time").eTh();
        htm.sTh().add("Dur. (min)").eTh();
        htm.sTh().add("Score").eTh();
        htm.sTh().add("Passed").eTh();
        htm.eTr();

        for (final RawSemesterCalendar week : weeks) {
            htm.sTr().sTd(null, "colspan='10'", "style='padding:1px;background:white;'").eTd().eTr();

            if (week.startDt.isAfter(today)) {
                break;
            }

            final boolean odd = (week.weekNbr.intValue() & 0x01) == 0x01;

            RawStexam nextExam = nextExamInWeek(week, exams);
            RawStmpe nextMpe = nextMpeInWeek(week, mpes);
            RawStchallenge nextChal = nextChalInWeek(week, chals);
            RawSthomework nextHw = nextHwInWeek(week, homeworks);

            if (nextMpe == null && nextChal == null && nextExam == null && nextHw == null) {

                htm.sTr("first");
                htm.sTd(odd ? "odd" : "even");
                if (week.weekNbr.intValue() < 1) {
                    htm.add("<strong>Before 1</strong>");
                } else {
                    htm.add("<strong>", week.weekNbr, "</strong>");
                }
                htm.eTd();

                htm.add("<td colspan='9' style='background-color:#eee;text-align:center;'>",
                        "(No activity during this week)").eTd();
                htm.eTr();

            } else {
                RawSemesterCalendar toWrite = week;
                final List<LocalDateTime> dates = new ArrayList<>(10);
                final LocalDateTime fin;

                if (nextMpe == null) {
                    fin = null;
                } else {
                    fin = nextMpe.getFinishDateTime();
                    dates.add(fin);
                }
                if (nextChal != null) {
                    final int time = nextChal.finishTime.intValue();
                    final LocalTime tm = LocalTime.of(time / 60, time % 60);
                    dates.add(LocalDateTime.of(nextChal.examDt, tm));
                }
                if (nextExam != null) {
                    dates.add(nextExam.getFinishDateTime());
                }
                if (nextHw != null) {
                    dates.add(nextHw.getFinishDateTime());
                }
                Collections.sort(dates);

                while (!dates.isEmpty()) { // dates list changes in loop

                    final LocalDateTime when = dates.remove(0);

                    if (nextMpe != null && fin != null && fin.equals(when)) {
                        emitMpeRow(htm, toWrite, odd, nextMpe);
                        toWrite = null;
                        nextMpe = nextMpeInWeek(week, mpes);
                        if (nextMpe != null) {
                            dates.add(nextMpe.getFinishDateTime());
                            Collections.sort(dates);
                        }
                    }

                    if (nextChal != null) {
                        final int time = nextChal.finishTime.intValue();
                        final LocalTime tm = LocalTime.of(time / 60, time % 60);
                        final LocalDateTime whenFinished = LocalDateTime.of(nextChal.examDt, tm);

                        if (whenFinished.equals(when)) {
                            emitChalRow(htm, toWrite, odd, nextChal);
                            toWrite = null;
                            nextChal = nextChalInWeek(week, chals);
                            if (nextChal != null) {
                                dates.add(whenFinished);
                                Collections.sort(dates);
                            }
                        }
                    }

                    if (nextExam != null && nextExam.getFinishDateTime() != null
                            && nextExam.getFinishDateTime().equals(when)) {
                        emitExamRow(htm, toWrite, odd, nextExam);
                        toWrite = null;
                        nextExam = nextExamInWeek(week, exams);
                        if (nextExam != null) {
                            dates.add(nextExam.getFinishDateTime());
                            Collections.sort(dates);
                        }
                    }

                    if (nextHw != null && nextHw.getFinishDateTime() != null
                            && nextHw.getFinishDateTime().equals(when)) {
                        emitHomeworkRow(htm, toWrite, odd, nextHw);
                        toWrite = null;
                        nextHw = nextHwInWeek(week, homeworks);
                        if (nextHw != null) {
                            dates.add(nextHw.getFinishDateTime());
                            Collections.sort(dates);
                        }
                    }
                }
            }
        }

        htm.eTable();
    }

    /**
     * Retrieves the next student exam record that falls within a specified week and removes it from the list of exam
     * records if found.
     *
     * @param week  the week
     * @param exams the list of exams
     * @return the earliest student exam record that was finished before the end of the specified week in the list of
     *         exams (this record is removed from the list before returning)
     */
    private static RawStexam nextExamInWeek(final RawSemesterCalendar week,
                                            final List<RawStexam> exams) {

        final RawStexam nextExam;

        if (exams.isEmpty() || exams.get(0).examDt.isAfter(week.endDt)) {
            nextExam = null;
        } else {
            nextExam = exams.remove(0);
        }

        return nextExam;
    }

    /**
     * Retrieves the next student placement attempt record that falls within a specified week and removes it from the
     * list of placement attempt records if found.
     *
     * @param week the week
     * @param mpes the list of placement attempts
     * @return the earliest student placement attempt record that was finished before the end of the specified week in
     *         the list of placement attempts (this record is removed from the list before returning)
     */
    private static RawStmpe nextMpeInWeek(final RawSemesterCalendar week,
                                          final List<RawStmpe> mpes) {

        final RawStmpe nextMpe;

        if (mpes.isEmpty() || mpes.get(0).examDt.isAfter(week.endDt)) {
            nextMpe = null;
        } else {
            nextMpe = mpes.remove(0);
        }

        return nextMpe;
    }

    /**
     * Retrieves the next student challenge attempt record that falls within a specified week and removes it from the
     * list of challenge attempt records if found.
     *
     * @param week  the week
     * @param chals the list of challenge attempts
     * @return the earliest student challenge attempt record that was finished before the end of the specified week in
     *         the list of challenge attempts (this record is removed from the list before returning)
     */
    private static RawStchallenge nextChalInWeek(final RawSemesterCalendar week,
                                                 final List<RawStchallenge> chals) {

        final RawStchallenge nextChal;

        if (chals.isEmpty() || chals.get(0).examDt.isAfter(week.endDt)) {
            nextChal = null;
        } else {
            nextChal = chals.remove(0);
        }

        return nextChal;
    }

    /**
     * Retrieves the next student homework record that falls within a specified week and removes it from the list of
     * homework records if found.
     *
     * @param week      the week
     * @param homeworks the list of homeworks
     * @return the earliest student homework record that was finished before the end of the specified week in the list
     *         of homeworks (this record is removed from the list before returning)
     */
    private static RawSthomework nextHwInWeek(final RawSemesterCalendar week,
                                              final List<RawSthomework> homeworks) {

        final RawSthomework nextHw;

        if (homeworks.isEmpty() || homeworks.get(0).hwDt.isAfter(week.endDt)) {
            nextHw = null;
        } else {
            nextHw = homeworks.remove(0);
        }

        return nextHw;
    }

    /**
     * Emits a table row for an exam.
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param week the week
     * @param odd  true if this is an odd week number
     * @param ex   the student exam record
     */
    private static void emitExamRow(final HtmlBuilder htm, final RawSemesterCalendar week, final boolean odd,
                                    final RawStexam ex) {

        htm.sTr(week == null ? null : "first");

        htm.sTd(odd ? "odd" : "even");
        if (week != null) {
            if (week.weekNbr.intValue() < 1) {
                htm.add("<strong>Before 1</strong>");
            } else {
                htm.add("<Strong>", week.weekNbr, "</strong>");
            }
        }
        htm.eTd();
        if (ex.course.startsWith("M 100")) {
            htm.sTd().eTd();
            htm.sTd().eTd();
        } else {
            htm.sTd("ctr").add(ex.course).eTd();
            htm.sTd("ctr").add(ex.unit).eTd();
        }

        if ("F".equals(ex.examType)) {
            final String name = "SY".equals(ex.examSource) ? "Final Exam (synthetic)" : "Final Exam";
            htm.sTd(null, "style='background-color:#cfc'").add(name).eTd();
        } else if ("U".equals(ex.examType)) {
            htm.sTd(null, "style='background-color:#cfc'").add("Unit Exam").eTd();
        } else if ("R".equals(ex.examType)) {
            if (Integer.valueOf(0).equals(ex.unit)) {
                htm.sTd(null, "style='background-color:#cff'").add("Skills Review").eTd();
            } else {
                htm.sTd(null, "style='background-color:#ffc'").add("Unit Review").eTd();
            }
        } else if ("Q".equals(ex.examType)) {
            htm.sTd(null, "style='background-color:#fdf'").add("User's Exam").eTd();
        } else {
            htm.sTd(null, "style='background-color:#cff'").add(ex.examType).eTd();
        }

        htm.sTd("ctr").add(ex.version).eTd();

        final LocalDateTime fin = ex.getFinishDateTime();
        htm.sTd().add(fin == null ? "N/A" : TemporalUtils.FMT_MDY.format(fin)).eTd();
        htm.sTd().add(fin == null ? "N/A" : TemporalUtils.FMT_HM_A.format(fin)).eTd();

        htm.sTd("ctr").add(fin == null ? "N/A" : OfficePage.durationString(ex.getStartDateTime(), fin)).eTd();
        htm.sTd("ctr").add(ex.examScore).eTd();
        if ("Y".equals(ex.isFirstPassed)) {
            htm.sTd().add("Y (First)").eTd();
        } else {
            htm.sTd().add(ex.passed).eTd();
        }
        htm.eTr();
    }

    /**
     * Emits a table row for a placement attempt.
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param week the week
     * @param odd  true if this is an odd week number
     * @param ex   the student placement attempt record
     */
    private static void emitMpeRow(final HtmlBuilder htm, final RawSemesterCalendar week, final boolean odd,
                                   final RawStmpe ex) {

        final LocalDateTime start = ex.getStartDateTime();
        final LocalDateTime fin = ex.getFinishDateTime();

        htm.sTr(week == null ? null : "first");
        htm.sTd(odd ? "odd" : "even");
        if (week != null) {
            if (week.weekNbr.intValue() < 1) {
                htm.add("<strong>Before 1</strong>");
            } else {
                htm.add("<Strong>", week.weekNbr, "</strong>");
            }
        }
        htm.eTd();
        htm.sTd().eTd();
        htm.sTd().eTd();
        htm.sTd(null, "style='background-color:#fdd'").add("Placement").eTd();
        htm.sTd("ctr").add(ex.version).eTd();
        htm.sTd().add(fin == null ? "N/A" : TemporalUtils.FMT_MDY.format(fin)).eTd();
        htm.sTd().add(fin == null ? "N/A" : TemporalUtils.FMT_HM_A.format(fin)).eTd();
        htm.sTd("ctr").add(fin == null ? "N/A" : OfficePage.durationString(start, fin)).eTd();

        htm.sTd("ctr");
        htm.add(ex.stsA).add('/');
        htm.add(ex.sts117).add('/');
        htm.add(ex.sts118).add('/');
        htm.add(ex.sts124).add('/');
        htm.add(ex.sts125).add('/');
        htm.add(ex.sts126);
        htm.eTd();

        htm.sTd().add(ex.placed).eTd();
        htm.eTr();
    }

    /**
     * Emits a table row for a challenge attempt.
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param week the week
     * @param odd  true if this is an odd week number
     * @param ex   the student challenge attempt record
     */
    private static void emitChalRow(final HtmlBuilder htm, final RawSemesterCalendar week, final boolean odd,
                                    final RawStchallenge ex) {

        htm.sTr(week == null ? null : "first");
        htm.sTd(odd ? "odd" : "even");
        if (week != null) {
            if (week.weekNbr.intValue() < 1) {
                htm.add("<strong>Before 1</strong>");
            } else {
                htm.add("<Strong>", week.weekNbr, "</strong>");
            }
        }
        htm.eTd();
        htm.sTd().eTd();
        htm.sTd().eTd();
        htm.sTd(null, "style='background-color:#fdd'")
                .add("Challenge").eTd();
        htm.sTd("ctr").add(ex.version).eTd();
        htm.sTd().add(TemporalUtils.FMT_MDY.format(ex.examDt)).eTd();

        final int time = ex.finishTime.intValue();
        htm.sTd().add(TemporalUtils.FMT_HM_A.format(LocalTime.of(time / 60, time % 60))).eTd();
        htm.sTd("ctr").add(OfficePage.durationString(ex.startTime, ex.finishTime)).eTd();
        htm.sTd().add(ex.score).eTd();
        htm.sTd().add(ex.passed).eTd();
        htm.eTr();
    }

    /**
     * Emits a table row for a homework.
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param week the week
     * @param odd  true if this is an odd week number
     * @param hw   the student homework record
     */
    private static void emitHomeworkRow(final HtmlBuilder htm, final RawSemesterCalendar week, final boolean odd,
                                        final RawSthomework hw) {

        htm.sTr(week == null ? null : "first");
        htm.sTd(odd ? "odd" : "even");
        if (week != null) {
            if (week.weekNbr.intValue() < 1) {
                htm.add("<strong>Before 1</strong>");
            } else {
                htm.add("<Strong>", week.weekNbr, "</strong>");
            }
        }

        final LocalDateTime fin = hw.getFinishDateTime();
        final LocalDateTime start = hw.getStartDateTime();

        htm.eTd();
        htm.sTd("ctr").add(hw.course).eTd();
        htm.sTd("ctr").add(hw.unit).eTd();
        htm.sTd(null, "style='background-color:#ddd'").add("Homework").eTd();
        htm.sTd("ctr").add(hw.version).eTd();
        htm.sTd().add(fin == null ? "N/A" : TemporalUtils.FMT_MDY.format(fin)).eTd();
        htm.sTd().add(fin == null ? "N/A" : TemporalUtils.FMT_HM_A.format(fin)).eTd();
        htm.sTd("ctr").add(fin == null ? "N/A" : OfficePage.durationString(start, fin)).eTd();
        htm.sTd().eTd();
        htm.sTd().add(hw.passed).eTd();
        htm.eTr();
    }
}
