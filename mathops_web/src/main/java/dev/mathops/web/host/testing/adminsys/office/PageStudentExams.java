package dev.mathops.web.host.testing.adminsys.office;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.field.ERole;
import dev.mathops.db.schema.legacy.impl.RawStchallengeLogic;
import dev.mathops.db.schema.legacy.impl.RawStexamLogic;
import dev.mathops.db.schema.legacy.impl.RawStmpeLogic;
import dev.mathops.db.schema.legacy.impl.RawStudentLogic;
import dev.mathops.db.schema.legacy.rec.RawExam;
import dev.mathops.db.schema.legacy.rec.RawStchallenge;
import dev.mathops.db.schema.legacy.rec.RawStexam;
import dev.mathops.db.schema.legacy.rec.RawStmpe;
import dev.mathops.db.schema.legacy.rec.RawStudent;
import dev.mathops.db.schema.main.rec.TermRec;
import dev.mathops.db.schema.RawRecordConstants;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Pages that displays a record of a student's exams.
 */
enum PageStudentExams {
    ;

    /**
     * Shows the student exams page (the student ID must be available in a request parameter named "stu").
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
                doStudentExamsPage(cache, site, req, resp, session, student);
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
    private static void doStudentExamsPage(final Cache cache, final AdminSite site, final ServletRequest req,
                                           final HttpServletResponse resp, final ImmutableSessionInfo session,
                                           final RawStudent student) throws IOException, SQLException {

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
            htm.addln("<button class='nav'>Activity</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_exams.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='navlit'>Exams</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_placement.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Placement</button>");
            htm.addln("</form>");
            htm.eDiv(); // narrowstack

            htm.sDiv("detail");
            emitStudentExams(cache, htm, student, session.getEffectiveRole());
            htm.eDiv(); // detail
        }

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits the student's exam record.
     *
     * @param cache   the data cache
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param student the student
     * @param role    the user role
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitStudentExams(final Cache cache, final HtmlBuilder htm,
                                         final RawStudent student, final ERole role) throws SQLException {

        final List<RawStexam> exams = RawStexamLogic.queryByStudent(cache, student.stuId, true);
        final List<RawStmpe> mpes = RawStmpeLogic.queryLegalByStudent(cache, student.stuId);
        final List<RawStchallenge> chals = RawStchallengeLogic.queryByStudent(cache, student.stuId);

        if (exams.isEmpty() && mpes.isEmpty() && chals.isEmpty()) {
            htm.sP().add("(No exams found)").eP();
        } else {
            exams.sort(new RawStexam.FinishDateTimeComparator());
            mpes.sort(new RawStmpe.FinishDateTimeComparator());
            chals.sort(new RawStchallenge.FinishDateTimeComparator());

            final TermRec active = cache.getSystemData().getActiveTerm();

            emitPlacementExamTable(active, htm, mpes, role);
            emitChallengeExamTable(active, htm, chals, role);
            emitUsersExamTable(active, htm, exams, role);
            emitTutorialExamTable(active, htm, exams, role);
            emitCourseExamTable(active, htm, exams, role);
        }
    }

    /**
     * Emits the table showing student placement exams.
     *
     * @param active the active term
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param mpes   the list of student placement attempt records
     * @param role   the user role
     */
    private static void emitPlacementExamTable(final TermRec active, final HtmlBuilder htm,
                                               final Collection<RawStmpe> mpes, final ERole role) {

        if (!mpes.isEmpty()) {
            htm.sH(4).add("Placement Attempts");

            htm.sTable("report",
                    "style='margin-top:5px;border-collapse:separate;border-spacing:0;min-width:688px;'");
            htm.sTr();
            htm.sTh().add("Date").eTh();
            htm.sTh().add("Time").eTh();
            htm.sTh().add("Serial").eTh();
            htm.sTh().add("Exam").eTh();
            htm.sTh().add("Dur. (min)").eTh();
            htm.sTh().add("Scores").eTh();
            htm.sTh().add("Placed").eTh();
            htm.sTh().add("Action").eTh();
            htm.eTr();

            for (final RawStmpe ex : mpes) {

                if (ex == null) {
                    Log.warning("Placement attempt was null!");
                    continue;
                }

                final LocalDateTime start = ex.getStartDateTime();
                final LocalDateTime fin = ex.getFinishDateTime();

                if (fin != null) {
                    htm.sTr();
                    htm.sTd().add(TemporalUtils.FMT_MD.format(fin)).eTd();
                    htm.sTd().add(TemporalUtils.FMT_HM_A.format(fin)).eTd();
                    htm.sTd().add(ex.serialNbr).eTd();
                    htm.sTd("ctr").add(ex.version).eTd();
                    htm.sTd("ctr").add(OfficePage.durationString(start, fin)).eTd();

                    htm.sTd("ctr");
                    htm.add(ex.stsA).add('/');
                    htm.add(ex.sts117).add('/');
                    htm.add(ex.sts118).add('/');
                    htm.add(ex.sts124).add('/');
                    htm.add(ex.sts125).add('/');
                    htm.add(ex.sts126);
                    htm.eTd();

                    htm.sTd("ctr").add(ex.placed).eTd();

                    htm.sTd("ctr");
                    if (active == null) {
                        htm.add("Active term is null!");
                    } else if (active.term == null) {
                        htm.add("Active term data is null!");
                    } else if (ex.stuId == null) {
                        htm.add("exam student ID is null!");
                    } else if (ex.serialNbr == null) {
                        htm.add("exam serial number is null!");
                    } else {
                        final String path = ExamWriter.makeWebExamPath(active.term.shortString,
                                ex.stuId, ex.serialNbr.longValue());

                        if (role.canActAs(ERole.ADMINISTRATOR)) {
                            htm.add("<form action='student_view_past_exam.html' method='post'>")
                                    .add("<input type='hidden' name='stu' value='", ex.stuId, "'/>")
                                    .add("<input type='hidden' name='ser' value='", ex.serialNbr, "'/>")
                                    .add("<input type='hidden' name='course' value='M 100P'/>")
                                    .add("<input type='hidden' name='exam' value='", ex.version, "'/>")
                                    .add("<input type='hidden' name='xml' value='",
                                            path, CoreConstants.SLASH, ExamWriter.EXAM_FILE, "'/>")
                                    .add("<input type='hidden' name='upd' value='",
                                            path, CoreConstants.SLASH, ExamWriter.ANSWERS_FILE, "'/>")
                                    .add("<input type='submit' value='View'></form>");
                        }
                    }
                    htm.eTd();
                    htm.eTr();
                }
            }

            htm.eTable();
            htm.div("vgap0");
        }
    }

    /**
     * Emits the table showing student challenge exams.
     *
     * @param active the active term
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param chals  the list of student challenge attempt records
     * @param role   the user role
     */
    private static void emitChallengeExamTable(final TermRec active, final HtmlBuilder htm,
                                               final Collection<RawStchallenge> chals, final ERole role) {

        if (!chals.isEmpty()) {
            htm.sH(4).add("Challenge Exams");

            htm.sTable("report",
                    "style='margin-top:5px;border-collapse:separate;border-spacing:0;min-width:688px;'");
            htm.sTr();
            htm.sTh().add("Date").eTh();
            htm.sTh().add("Time").eTh();
            htm.sTh().add("Serial").eTh();
            htm.sTh().add("Course").eTh();
            htm.sTh().add("Exam").eTh();
            htm.sTh().add("Dur. (min)").eTh();
            htm.sTh().add("Score").eTh();
            htm.sTh().add("Passed").eTh();
            htm.sTh().add("Action").eTh();
            htm.eTr();

            for (final RawStchallenge ex : chals) {

                final int tm = ex.finishTime.intValue();
                final LocalDateTime whenFinished =
                        LocalDateTime.of(ex.examDt, LocalTime.of(tm / 60, tm % 60));

                htm.sTr();
                htm.sTd().add(TemporalUtils.FMT_MD.format(whenFinished)).eTd();
                htm.sTd().add(TemporalUtils.FMT_HM_A.format(whenFinished)).eTd();
                htm.sTd().add(ex.serialNbr).eTd();
                htm.sTd("ctr").add(ex.course).eTd();
                htm.sTd("ctr").add(ex.version).eTd();
                htm.sTd("ctr").add(OfficePage.durationString(ex.startTime, ex.finishTime)).eTd();
                htm.sTd("ctr").add(ex.score).eTd();
                htm.sTd("ctr").add(ex.passed).eTd();

                final String path = ExamWriter.makeWebExamPath(active.term.shortString, ex.stuId,
                        ex.serialNbr.longValue());

                htm.sTd("ctr");
                if (role.canActAs(ERole.ADMINISTRATOR)) {
                    htm.add("<form action='student_view_past_exam.html' method='post'>")
                            .add("<input type='hidden' name='stu' value='", ex.stuId, "'/>")
                            .add("<input type='hidden' name='ser' value='", ex.serialNbr, "'/>")
                            .add("<input type='hidden' name='course' value='", ex.course, "'/>")
                            .add("<input type='hidden' name='exam' value='", ex.version, "'/>")
                            .add("<input type='hidden' name='xml' value='",
                                    path, CoreConstants.SLASH, ExamWriter.EXAM_FILE, "'/>")
                            .add("<input type='hidden' name='upd' value='",
                                    path, CoreConstants.SLASH, ExamWriter.ANSWERS_FILE, "'/>")
                            .add("<input type='submit' value='View'></form>");
                }
                htm.eTd();
                htm.eTr();
            }

            htm.eTable();
            htm.div("vgap0");
        }
    }

    /**
     * Emits the table showing student User's exams.
     *
     * @param active the active term
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param exams  the list of student exam records
     * @param role   the user role
     */
    private static void emitUsersExamTable(final TermRec active, final HtmlBuilder htm,
                                           final Iterable<RawStexam> exams, final ERole role) {

        boolean hasUsers = false;
        for (final RawStexam exam : exams) {
            if (RawRecordConstants.M100U.equals(exam.course)) {
                hasUsers = true;
                break;
            }
        }

        if (hasUsers) {
            htm.sH(4).add("User's Exams");

            htm.sTable("report",
                    "style='margin-top:5px;border-collapse:separate;border-spacing:0;min-width:688px;'");
            htm.sTr();
            htm.sTh().add("Date").eTh();
            htm.sTh().add("Time").eTh();
            htm.sTh().add("Serial").eTh();
            htm.sTh().add("Exam").eTh();
            htm.sTh().add("Dur. (min)").eTh();
            htm.sTh().add("Score").eTh();
            htm.sTh().add("Passed").eTh();
            htm.sTh().add("Action").eTh();
            htm.eTr();

            for (final RawStexam ex : exams) {
                if (RawRecordConstants.M100U.equals(ex.course)) {

                    final LocalDateTime fin = ex.getFinishDateTime();
                    if (fin != null) {
                        htm.sTr();
                        htm.sTd().add(TemporalUtils.FMT_MD.format(fin)).eTd();
                        htm.sTd().add(TemporalUtils.FMT_HM_A.format(fin)).eTd();
                        htm.sTd().add(ex.serialNbr).eTd();
                        htm.sTd("ctr").add(ex.version).eTd();
                        htm.sTd("ctr").add(OfficePage.durationString(ex.getStartDateTime(), fin)).eTd();
                        htm.sTd("ctr").add(ex.examScore).eTd();
                        htm.sTd("ctr").add(ex.passed).eTd();

                        final String path = ExamWriter.makeWebExamPath(active.term.shortString,
                                ex.stuId, ex.serialNbr.longValue());

                        htm.sTd("ctr");
                        if (role.canActAs(ERole.ADMINISTRATOR)) {
                            htm.add("<form action='student_view_past_exam.html' method='post'>")
                                    .add("<input type='hidden' name='stu' value='", ex.stuId, "'/>")
                                    .add("<input type='hidden' name='ser' value='", ex.serialNbr, "'/>")
                                    .add("<input type='hidden' name='course' value='", ex.course, "'/>")
                                    .add("<input type='hidden' name='exam' value='", ex.version, "'/>")
                                    .add("<input type='hidden' name='xml' value='",
                                            path, CoreConstants.SLASH, ExamWriter.EXAM_FILE, "'/>")
                                    .add("<input type='hidden' name='upd' value='",
                                            path, CoreConstants.SLASH, ExamWriter.ANSWERS_FILE, "'/>")
                                    .add("<input type='submit' value='View'></form>");
                        }
                        htm.eTd();
                        htm.eTr();
                    }
                }
            }

            htm.eTable();
            htm.div("vgap0");
        }
    }

    /**
     * Emits the table showing student tutorial exams.
     *
     * @param active the active term
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param exams  the list of student exam records
     * @param role   the user role
     */
    private static void emitTutorialExamTable(final TermRec active, final HtmlBuilder htm,
                                              final Collection<RawStexam> exams, final ERole role) {

        final Collection<RawStexam> tutorials = new ArrayList<>(exams.size());
        for (final RawStexam exam : exams) {
            if (RawRecordConstants.M100T.equals(exam.course)
                    || RawRecordConstants.M1170.equals(exam.course)
                    || RawRecordConstants.M1180.equals(exam.course)
                    || RawRecordConstants.M1240.equals(exam.course)
                    || RawRecordConstants.M1250.equals(exam.course)
                    || RawRecordConstants.M1260.equals(exam.course)) {
                tutorials.add(exam);
            }
        }

        if (!tutorials.isEmpty()) {
            htm.sH(4).add("Tutorial Exams");

            htm.sTable("report",
                    "style='margin-top:5px;border-collapse:separate;border-spacing:0;min-width:688px;'");
            htm.sTr();
            htm.sTh().add("Date").eTh();
            htm.sTh().add("Time").eTh();
            htm.sTh().add("Serial").eTh();
            htm.sTh().add("Tutorial").eTh();
            htm.sTh().add("Exam").eTh();
            htm.sTh().add("Dur. (min)").eTh();
            htm.sTh().add("Score").eTh();
            htm.sTh().add("Passed").eTh();
            htm.sTh().add("Action").eTh();
            htm.eTr();

            for (final RawStexam ex : tutorials) {
                final LocalDateTime fin = ex.getFinishDateTime();

                if (fin != null) {
                    htm.sTr();
                    htm.sTd().add(TemporalUtils.FMT_MD.format(fin)).eTd();
                    htm.sTd().add(TemporalUtils.FMT_HM_A.format(fin)).eTd();
                    htm.sTd().add(ex.serialNbr).eTd();
                    switch (ex.course) {
                        case RawRecordConstants.M100T -> htm.sTd("ctr").add("ELM").eTd();
                        case RawRecordConstants.M1170 -> htm.sTd("ctr").add("Algebra I").eTd();
                        case RawRecordConstants.M1180 -> htm.sTd("ctr").add("Algebra II").eTd();
                        case RawRecordConstants.M1240 -> htm.sTd("ctr").add("Logs & Exp.").eTd();
                        case RawRecordConstants.M1250 -> htm.sTd("ctr").add("Trig I").eTd();
                        case RawRecordConstants.M1260 -> htm.sTd("ctr").add("Trig II").eTd();
                        case null, default -> htm.sTd("ctr").add(ex.course).eTd();
                    }
                    htm.sTd("ctr").add(ex.version).eTd();
                    htm.sTd("ctr").add(OfficePage.durationString(ex.getStartDateTime(), fin)).eTd();
                    htm.sTd("ctr").add(ex.examScore).eTd();
                    htm.sTd("ctr").add(ex.passed).eTd();

                    final String path = ExamWriter.makeWebExamPath(active.term.shortString, ex.stuId,
                            ex.serialNbr.longValue());

                    htm.sTd("ctr");
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
            }

            htm.eTable();
            htm.div("vgap0");
        }
    }

    /**
     * Emits the table showing student course exams.
     *
     * @param active the active term
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param exams  the list of student exam records
     * @param role   the user role
     */
    private static void emitCourseExamTable(final TermRec active, final HtmlBuilder htm,
                                            final Collection<RawStexam> exams, final ERole role) {

        final Collection<RawStexam> course = new ArrayList<>(exams.size());
        for (final RawStexam exam : exams) {
            if (RawRecordConstants.M117.equals(exam.course)
                    || RawRecordConstants.M118.equals(exam.course)
                    || RawRecordConstants.M124.equals(exam.course)
                    || RawRecordConstants.M125.equals(exam.course)
                    || RawRecordConstants.M126.equals(exam.course)) {
                course.add(exam);
            }
        }

        if (!course.isEmpty()) {
            htm.sH(4).add("Course Exams");

            htm.sTable("report", "style='margin-top:5px;border-collapse:separate;border-spacing:0;min-width:688px;'");
            htm.sTr();
            htm.sTh().add("Date").eTh();
            htm.sTh().add("Time").eTh();
            htm.sTh().add("Serial").eTh();
            htm.sTh().add("Course").eTh();
            htm.sTh().add("Unit").eTh();
            htm.sTh().add("Type").eTh();
            htm.sTh().add("Exam").eTh();
            htm.sTh().add("Dur. (min)").eTh();
            htm.sTh().add("Score").eTh();
            htm.sTh().add("Passed").eTh();
            htm.sTh().add("Action").eTh();
            htm.eTr();

            for (final RawStexam ex : course) {
                final LocalDateTime fin = ex.getFinishDateTime();

                if (fin != null) {
                    htm.sTr();
                    htm.sTd().add(TemporalUtils.FMT_MD.format(fin)).eTd();
                    htm.sTd().add(TemporalUtils.FMT_HM_A.format(fin)).eTd();
                    htm.sTd().add(ex.serialNbr).eTd();
                    htm.sTd("ctr").add(ex.course).eTd();
                    if (ex.unit.intValue() == 0) {
                        htm.sTd().eTd();
                    } else {
                        htm.sTd("ctr").add(ex.unit).eTd();
                    }

                    switch (ex.examType) {
                        case "F" -> {
                            final String name = "SY".equals(ex.examSource) ? "Final Exam (synthetic)" : "Final Exam";
                            htm.sTd(null, "style='background-color:#cfc'").add(name).eTd();
                        }
                        case "U" -> htm.sTd(null, "style='background-color:#cfc'").add("Unit Exam").eTd();
                        case "R" -> {
                            if (Integer.valueOf(0).equals(ex.unit)) {
                                htm.sTd(null, "style='background-color:#cff'").add("Skills Review").eTd();
                            } else {
                                htm.sTd(null, "style='background-color:#ffc'").add("Unit Review").eTd();
                            }
                        }
                        case "Q" -> htm.sTd(null, "style='background-color:#cff'").add("Users Exam").eTd();
                        case null, default -> htm.sTd().add(RawExam.getExamTypeName(ex.examType)).eTd();
                    }

                    htm.sTd("ctr").add(ex.version).eTd();
                    htm.sTd("ctr").add(OfficePage.durationString(ex.getStartDateTime(), fin)).eTd();
                    htm.sTd("ctr").add(ex.examScore).eTd();
                    htm.sTd("ctr").add(ex.passed).eTd();

                    final String path = ExamWriter.makeWebExamPath(active.term.shortString, ex.stuId,
                            ex.serialNbr.longValue());

                    htm.sTd("ctr");
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
            }

            htm.eTable();
            htm.div("vgap0");
        }
    }
}
