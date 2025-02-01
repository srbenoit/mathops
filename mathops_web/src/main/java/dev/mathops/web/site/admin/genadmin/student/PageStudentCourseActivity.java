package dev.mathops.web.site.admin.genadmin.student;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.rec.TermWeekRec;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdminTopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;
import dev.mathops.web.site.admin.genadmin.PageError;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A page that shows all student activities in all enrolled courses (as well as other recorded activities like the
 * User's Exam or placement exams).
 */
public enum PageStudentCourseActivity {
    ;

    /**
     * Shows the student course activity page (the student ID must be available in a request parameter named "stu").
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
                emitPageContent(cache, site, req, resp, session, student);
            }
        }
    }

    /**
     * Shows the student course activity page for a provided student.
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
    private static void emitPageContent(final Cache cache, final AdminSite site, final ServletRequest req,
                                        final HttpServletResponse resp, final ImmutableSessionInfo session,
                                        final RawStudent student) throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.STUDENT_STATUS, htm);

        htm.sP("studentname")
                .add("<strong class='largeish'>", student.getScreenName(),
                        "</strong> (", student.stuId,
                        ") &nbsp; <a class='ulink' href='student.html'>Clear</a>")
                .eP();

        htm.addln("<nav class='menu'>");

        menuButton(htm, false, student.stuId, EAdminStudentCommand.STUDENT_INFO);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.PLACEMENT);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.REGISTRATIONS);
        menuButton(htm, true, student.stuId, EAdminStudentCommand.ACTIVITY);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.MATH_PLAN);

        htm.add("</nav>");

        htm.addln("<main class='info'>");
        emitCourseActivity(cache, htm, student, session.role);
        htm.addln("</main>");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
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

        htm.addln("<form action='", cmd.url,
                "' method='post'>");

        htm.addln("<input type='hidden' name='stu' value='",
                studentId, "'/>");

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
     * Emits the student's exam history.
     *
     * @param cache   the data cache
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param student the student
     * @param role    the user role
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitCourseActivity(final Cache cache, final HtmlBuilder htm,
                                           final RawStudent student, final ERole role) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        if (active == null) {
            htm.add("(Unable to query the active term)");
        } else {
            final List<TermWeekRec> weeks = cache.getSystemData().getTermWeeks();

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

            if (exams.isEmpty() && chals.isEmpty() && homeworks.isEmpty()) {
                htm.add("(No course activity found)");
            } else {
                emitActivityTable(htm, active, weeks, exams, mpes, chals, homeworks, role);
            }
        }
    }

    /**
     * Emits the table containing student activity.
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param active    the active term
     * @param weeks     the list of term weeks
     * @param exams     the list of student exam records
     * @param mpes      the list of student placement attempt records
     * @param chals     the list of student challenge attempt records
     * @param homeworks the list of student homework records
     * @param role      the user role
     */
    private static void emitActivityTable(final HtmlBuilder htm, final TermRec active,
                                          final List<TermWeekRec> weeks,
                                          final List<RawStexam> exams,
                                          final List<RawStmpe> mpes,
                                          final List<RawStchallenge> chals,
                                          final List<RawSthomework> homeworks, final ERole role) {

        Collections.sort(weeks); // Sorts by start date by default

        final LocalDate today = LocalDate.now();

        htm.sTable("report", "style='width:90%;border-collapse:separate;border-spacing:0;'");
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
        htm.sTh().add("Action").eTh();
        htm.eTr();

        for (final TermWeekRec week : weeks) {
            htm.sTr().sTd(null, "colspan='10'", "style='padding:1px;background:white;'").eTd().eTr();

            if (week.startDate.isAfter(today)) {
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
                    htm.add("<strong>", week.weekNbr,
                            "</strong>");
                }
                htm.eTd();

                htm.add("<td colspan='9' style='background-color:#eee;text-align:center;'>",
                        "(No activity during this week)").eTd();
                htm.eTr();

            } else {
                TermWeekRec toWrite = week;
                final List<LocalDateTime> dates = new ArrayList<>(10);

                if (nextMpe != null) {
                    dates.add(nextMpe.getFinishDateTime());
                }
                if (nextChal != null && nextChal.finishTime != null) {
                    final int tm = nextChal.finishTime.intValue();
                    dates.add(LocalDateTime.of(nextChal.examDt, LocalTime.of(tm / 60, tm % 60)));
                }
                if (nextExam != null) {
                    dates.add(nextExam.getFinishDateTime());
                }
                if (nextHw != null) {
                    dates.add(nextHw.getFinishDateTime());
                }
                Collections.sort(dates);

                while (!dates.isEmpty()) { // dates list changes in loop

                    final LocalDateTime when = dates.removeFirst();
                    if (nextMpe != null) {
                        final LocalDateTime fin = nextMpe.getFinishDateTime();

                        if (fin != null && fin.equals(when)) {
                            emitMpeRow(htm, active, toWrite, odd, nextMpe, role);
                            toWrite = null;
                            nextMpe = nextMpeInWeek(week, mpes);
                            if (nextMpe != null) {
                                dates.add(fin);
                                Collections.sort(dates);
                            }
                        }
                    }

                    if (nextChal != null && nextChal.finishTime != null) {
                        final int tm = nextChal.finishTime.intValue();
                        final LocalDateTime whenFinished =
                                LocalDateTime.of(nextChal.examDt, LocalTime.of(tm / 60, tm % 60));

                        if (whenFinished.equals(when)) {
                            emitChalRow(htm, active, toWrite, odd, nextChal, role);
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
                        emitExamRow(htm, active, toWrite, odd, nextExam, role);
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
    private static RawStexam nextExamInWeek(final TermWeekRec week,
                                            final List<RawStexam> exams) {

        final RawStexam nextExam;

        if (exams.isEmpty() || exams.getFirst().examDt.isAfter(week.endDate)) {
            nextExam = null;
        } else {
            nextExam = exams.removeFirst();
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
    private static RawStmpe nextMpeInWeek(final TermWeekRec week,
                                          final List<RawStmpe> mpes) {

        final RawStmpe nextMpe;

        if (mpes.isEmpty() || mpes.getFirst().examDt.isAfter(week.endDate)) {
            nextMpe = null;
        } else {
            nextMpe = mpes.removeFirst();
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
    private static RawStchallenge nextChalInWeek(final TermWeekRec week,
                                                 final List<RawStchallenge> chals) {

        final RawStchallenge nextChal;

        if (chals.isEmpty() || chals.getFirst().examDt.isAfter(week.endDate)) {
            nextChal = null;
        } else {
            nextChal = chals.removeFirst();
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
    private static RawSthomework nextHwInWeek(final TermWeekRec week,
                                              final List<RawSthomework> homeworks) {

        final RawSthomework nextHw;

        if (homeworks.isEmpty() || homeworks.getFirst().hwDt.isAfter(week.endDate)) {
            nextHw = null;
        } else {
            nextHw = homeworks.removeFirst();
        }

        return nextHw;
    }

    /**
     * Emits a table row for an exam.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param active the active term
     * @param week   the week
     * @param odd    true if this is an odd week number
     * @param ex     the student exam record
     * @param role   the user role
     */
    private static void emitExamRow(final HtmlBuilder htm, final TermRec active,
                                    final TermWeekRec week, final boolean odd, final RawStexam ex,
                                    final ERole role) {

        final String path =
                ExamWriter.makeWebExamPath(active.term.shortString, ex.stuId, ex.serialNbr.longValue());

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
        htm.sTd().add(ex.course).eTd();
        htm.sTd().add(ex.unit).eTd();

        switch (ex.examType) {
            case "F" -> {
                final String name = "SY".equals(ex.examSource) ? "Final Exam (synthetic)" : "Final Exam";
                htm.sTd(null, "style='background-color:#cfc'").add(name).eTd();
            }
            case "U" -> htm.sTd(null, "style='background-color:#cfc'").add("Unit Exam").eTd();
            case "R" -> {
                if (Integer.valueOf(0).equals(ex.unit)) {
                    htm.sTd(null, "style='background-color:#fdf'").add("Skills Review").eTd();
                } else {
                    htm.sTd(null, "style='background-color:#fdf'").add("Unit Review").eTd();
                }
            }
            case "Q" -> htm.sTd(null, "style='background-color:#fdf'").add("User's Exam").eTd();
            case null, default -> htm.sTd(null, "style='background-color:#cff'").add(ex.examType).eTd();
        }

        htm.sTd().add(ex.version).eTd();

        final LocalDateTime start = ex.getStartDateTime();
        final LocalDateTime fin = ex.getFinishDateTime();
        htm.sTd().add(fin == null ? "N/A" : TemporalUtils.FMT_WMD.format(fin)).eTd();
        htm.sTd().add(fin == null ? "N/A" : TemporalUtils.FMT_HMS_A.format(fin)).eTd();

        htm.sTd().add(start == null || fin == null ? "N/A" : durationString(start, fin)).eTd();
        htm.sTd().add(ex.examScore).eTd();
        if ("Y".equals(ex.isFirstPassed)) {
            htm.sTd().add("Y (First)").eTd();
        } else {
            htm.sTd().add(ex.passed).eTd();
        }
        htm.sTd();
        if (role.canActAs(ERole.ADMINISTRATOR)) {
            htm.add("<form action='student_view_past_exam.html' method='post'>")
                    .add("<input type='hidden' name='stu' value='", ex.stuId, "'/>")
                    .add("<input type='hidden' name='ser' value='", ex.serialNbr, "'/>")
                    .add("<input type='hidden' name='course' value='", ex.course, "'/>")
                    .add("<input type='hidden' name='exam' value='", ex.version, "'/>")
                    .add("<input type='hidden' name='xml' value='", path,
                            CoreConstants.SLASH, ExamWriter.EXAM_FILE, "'/>")
                    .add("<input type='hidden' name='upd' value='", path,
                            CoreConstants.SLASH, ExamWriter.ANSWERS_FILE, "'/>")
                    .add("<input type='submit' value='View'></form>");
        }
        htm.eTd();
        htm.eTr();
    }

    /**
     * Emits a table row for a placement attempt.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param active the active term
     * @param week   the week
     * @param odd    true if this is an odd week number
     * @param ex     the student placement attempt record
     * @param role   the user role
     */
    private static void emitMpeRow(final HtmlBuilder htm, final TermRec active,
                                   final TermWeekRec week, final boolean odd, final RawStmpe ex,
                                   final ERole role) {

        final String path = ExamWriter.makeWebExamPath(active.term.shortString, ex.stuId, ex.serialNbr.longValue());

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
        htm.sTd().add(RawRecordConstants.M100P).eTd();
        htm.sTd().add("N/A").eTd();
        htm.sTd(null, "style='background-color:#fdd'").add(RawExam.getExamTypeName("Q")).eTd();
        htm.sTd().add(ex.version).eTd();

        final LocalDateTime start = ex.getStartDateTime();
        final LocalDateTime fin = ex.getFinishDateTime();

        htm.sTd().add(fin == null ? "N/A" : TemporalUtils.FMT_WMD.format(fin)).eTd();
        htm.sTd().add(fin == null ? "N/A" : TemporalUtils.FMT_HMS_A.format(fin)).eTd();
        htm.sTd().add(start == null || fin == null ? "N/A" : durationString(start, fin)).eTd();

        htm.sTd();
        htm.add(ex.stsA).add('/');
        htm.add(ex.sts117).add('/');
        htm.add(ex.sts118).add('/');
        htm.add(ex.sts124).add('/');
        htm.add(ex.sts125).add('/');
        htm.add(ex.sts126);
        htm.eTd();

        htm.sTd().add(ex.placed).eTd();

        htm.sTd();
        if (role.canActAs(ERole.ADMINISTRATOR)) {
            htm.add("<form action='student_view_past_exam.html' method='post'>")
                    .add("<input type='hidden' name='stu' value='", ex.stuId, "'/>")
                    .add("<input type='hidden' name='ser' value='", ex.serialNbr, "'/>")
                    .add("<input type='hidden' name='course' value='M 100P'/>")
                    .add("<input type='hidden' name='exam' value='", ex.version, "'/>")
                    .add("<input type='hidden' name='xml' value='", path,
                            CoreConstants.SLASH, ExamWriter.EXAM_FILE, "'/>")
                    .add("<input type='hidden' name='upd' value='", path,
                            CoreConstants.SLASH, ExamWriter.ANSWERS_FILE, "'/>")
                    .add("<input type='submit' value='View'></form>");
        }
        htm.eTd();
        htm.eTr();
    }

    /**
     * Emits a table row for a challenge attempt.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param active the active term
     * @param week   the week
     * @param odd    true if this is an odd week number
     * @param ex     the student challenge attempt record
     * @param role   the user role
     */
    private static void emitChalRow(final HtmlBuilder htm, final TermRec active,
                                    final TermWeekRec week, final boolean odd, final RawStchallenge ex,
                                    final ERole role) {

        final String path = ExamWriter.makeWebExamPath(active.term.shortString, ex.stuId, ex.serialNbr.longValue());

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
        htm.sTd().add(ex.course).eTd();
        htm.sTd().add("N/A").eTd();
        htm.sTd(null, "style='background-color:#fdd'").add(RawExam.getExamTypeName("CH")).eTd();
        htm.sTd().add(ex.version).eTd();
        htm.sTd().add(TemporalUtils.FMT_WMD.format(ex.examDt)).eTd();
        final int tm = ex.finishTime.intValue();
        htm.sTd().add(TemporalUtils.FMT_HMS_A.format(LocalTime.of(tm / 60, tm % 60))).eTd();
        htm.sTd().add(durationString(ex.startTime, ex.finishTime)).eTd();
        htm.sTd().add(ex.score).eTd();
        htm.sTd().add(ex.passed).eTd();

        htm.sTd();
        if (role.canActAs(ERole.ADMINISTRATOR)) {
            htm.add("<form action='student_view_past_exam.html' method='post'>")
                    .add("<input type='hidden' name='stu' value='", ex.stuId, "'/>")
                    .add("<input type='hidden' name='ser' value='", ex.serialNbr, "'/>")
                    .add("<input type='hidden' name='course' value='", ex.course, "'/>")
                    .add("<input type='hidden' name='exam' value='", ex.version, "'/>")
                    .add("<input type='hidden' name='xml' value='", path,
                            CoreConstants.SLASH, ExamWriter.EXAM_FILE, "'/>")
                    .add("<input type='hidden' name='upd' value='", path,
                            CoreConstants.SLASH, ExamWriter.ANSWERS_FILE, "'/>")
                    .add("<input type='submit' value='View'></form>");
        }
        htm.eTd();
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
    private static void emitHomeworkRow(final HtmlBuilder htm, final TermWeekRec week,
                                        final boolean odd, final RawSthomework hw) {

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
        htm.sTd().add(hw.course).eTd();
        htm.sTd().add(hw.unit).eTd();
        htm.sTd(null, "style='background-color:#ffc'").add("Homework").eTd();
        htm.sTd().add(hw.version).eTd();
        htm.sTd().add(fin == null ? "N/A" : TemporalUtils.FMT_WMD.format(fin)).eTd();
        htm.sTd().add(fin == null ? "N/A" : TemporalUtils.FMT_HMS_A.format(fin)).eTd();
        htm.sTd().add(start == null || fin == null ? "N/A" : durationString(start, fin)).eTd();
        htm.sTd().add("N/A").eTd();
        htm.sTd().add(hw.passed).eTd();
        htm.sTd().eTd();
        htm.eTr();
    }

    /**
     * Generates a string representation of a duration.
     *
     * @param start  the start time
     * @param finish the finish time
     * @return the duration string
     */
    private static String durationString(final ChronoLocalDateTime<LocalDate> start,
                                         final ChronoLocalDateTime<LocalDate> finish) {

        final String result;

        if (start.isAfter(finish)) {
            result = "(negative)";
        } else {
            final int seconds =
                    (int) (finish.toEpochSecond(ZoneOffset.UTC) - start.toEpochSecond(ZoneOffset.UTC));

            final int hr = seconds / 3600;
            final int min = seconds % 3600 / 60;

            final StringBuilder dur = new StringBuilder(12);
            if (hr > 0) {
                dur.append(hr).append(':');
                if (min < 10) {
                    dur.append('0');
                }
            }
            dur.append(min);

            result = dur.toString();
        }

        return result;
    }

    /**
     * Generates a string representation of a duration.
     *
     * @param start  the start time
     * @param finish the finish time
     * @return the duration string
     */
    private static String durationString(final Integer start, final Integer finish) {

        final String result;

        if (start == null) {
            result = "(no start time)";
        } else if (finish == null) {
            result = "(no finish time)";
        } else if (start.intValue() > finish.intValue()) {
            result = "(neg)";
        } else {
            result = Integer.toString(finish.intValue() - start.intValue());
        }

        return result;
    }
}
