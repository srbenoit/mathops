package dev.mathops.web.site.admin.genadmin.student;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdminTopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;
import dev.mathops.web.site.admin.genadmin.PageError;
import dev.mathops.web.site.html.pastexam.PastExamSession;
import dev.mathops.web.site.html.pastexam.PastExamSessionStore;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Presents a past exam.
 */
public enum PageStudentPastExam {
    ;

    /**
     * Starts a review exam and presents the exam instructions.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void startPastExam(final Cache cache, final AdminSite site,
                                     final ServletRequest req, final HttpServletResponse resp,
                                     final ImmutableSessionInfo session) throws IOException, SQLException {

        final String stu = req.getParameter("stu");
        final String ser = req.getParameter("ser");
        final String xml = req.getParameter("xml");
        final String upd = req.getParameter("upd");
        final String course = req.getParameter("course");
        final String exam = req.getParameter("exam");

        if (AbstractSite.isFileParamInvalid(stu) || AbstractSite.isFileParamInvalid(ser)
                || AbstractSite.isFileParamInvalid(xml) || AbstractSite.isFileParamInvalid(upd)
                || AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(exam)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  stu='", stu, "'");
            Log.warning("  ser='", ser, "'");
            Log.warning("  xml='", xml, "'");
            Log.warning("  upd='", upd, "'");
            Log.warning("  course='", course, "'");
            Log.warning("  exam='", exam, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final RawStudent student = RawStudentLogic.query(cache, stu, false);

            if (student == null) {
                PageError.doGet(cache, site, req, resp, session, "Student not found.");
            } else {
                final PastExamSessionStore store = PastExamSessionStore.getInstance();
                PastExamSession pes = store.getPastExamSession(session.loginSessionId, xml);

                if (pes == null) {
                    final String redirect = "student_course_activity.html?stu=" + stu;

                    pes = new PastExamSession(cache, site.siteProfile, session.loginSessionId, exam,
                            xml, session.getEffectiveUserId(), redirect);
                    store.setPastExamSession(pes);
                }

                final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

                emitPastExamDisplay(cache, htm, student, course, ser);

                htm.addln("<form id='past_exam_form' ",
                        "action='student_update_past_exam.html' method='POST'>");
                htm.addln(" <input type='hidden' name='stu' value='", student.stuId, "'>");
                htm.addln(" <input type='hidden' name='ser' value='", ser, "'>");
                htm.addln(" <input type='hidden' name='xml' value='", pes.xmlFilename, "'>");
                htm.addln(" <input type='hidden' name='upd' value='", upd, "'>");
                htm.addln(" <input type='hidden' name='course' value='", course, "'>");
                htm.addln(" <input type='hidden' name='exam' value='", pes.version, "'>");
                htm.addln(" <input type='hidden' id='past_exam_act' name='action'>");
                pes.generateHtml(session.getNow(), xml, upd, htm);
                htm.addln("</form>");

                htm.addln("</main>");

                Page.endOrdinaryPage(cache, site, htm, true);
                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
            }
        }
    }

    /**
     * Handles a POST request.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void updatePastExam(final Cache cache, final AdminSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String stu = req.getParameter("stu");
        final String ser = req.getParameter("ser");
        final String xml = req.getParameter("xml");
        final String upd = req.getParameter("upd");
        final String course = req.getParameter("course");
        final String exam = req.getParameter("exam");

        if (AbstractSite.isFileParamInvalid(stu) || AbstractSite.isFileParamInvalid(ser)
                || AbstractSite.isFileParamInvalid(xml) || AbstractSite.isFileParamInvalid(upd)
                || AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(exam)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  stu='", stu, "'");
            Log.warning("  ser='", ser, "'");
            Log.warning("  xml='", xml, "'");
            Log.warning("  upd='", upd, "'");
            Log.warning("  course='", course, "'");
            Log.warning("  exam='", exam, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final RawStudent student = RawStudentLogic.query(cache, stu, false);

            if (student == null) {
                PageError.doGet(cache, site, req, resp, session, "Student not found.");
            } else {
                final PastExamSessionStore store = PastExamSessionStore.getInstance();
                PastExamSession pes = store.getPastExamSession(session.loginSessionId, xml);

                if (pes == null) {
                    final String redirect = "student_course_activity.html?stu=" + stu;

                    pes = new PastExamSession(cache, site.siteProfile, session.loginSessionId, exam,
                            xml, session.getEffectiveUserId(), redirect);
                    store.setPastExamSession(pes);
                }

                final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

                emitPastExamDisplay(cache, htm, student, course, ser);

                htm.addln("<form id='past_exam_form' action='student_update_past_exam.html' method='POST'>");
                htm.addln(" <input type='hidden' name='stu' value='", student.stuId, "'>");
                htm.addln(" <input type='hidden' name='ser' value='", ser, "'>");
                htm.addln(" <input type='hidden' name='xml' value='", pes.xmlFilename, "'>");
                htm.addln(" <input type='hidden' name='upd' value='", upd, "'>");
                htm.addln(" <input type='hidden' name='course' value='", course, "'>");
                htm.addln(" <input type='hidden' name='exam' value='", pes.version, "'>");
                htm.addln(" <input type='hidden' id='past_exam_act' name='action'>");
                final String redirect = pes.processPost(session, req, htm);
                htm.addln("</form>");

                htm.addln("</main>");

                if (redirect == null) {
                    Page.endOrdinaryPage(cache, site, htm, true);
                    AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
                } else {
                    resp.sendRedirect(redirect);
                }
            }
        }
    }

    /**
     * Emits a display of tabular information on the exam session being displayed.
     *
     * @param cache   the data cache
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param student the student
     * @param course  the course
     * @param ser     the serial number
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitPastExamDisplay(final Cache cache, final HtmlBuilder htm, final RawStudent student,
                                            final String course, final String ser) throws SQLException {

        GenAdminPage.emitNavBlock(EAdminTopic.STUDENT_STATUS, htm);

        htm.sP().add("<strong class='largeish'>", student.getScreenName(), "</strong> (", student.stuId,
                ") &nbsp; <a class='ulink' href='student.html'>Clear</a>").eP();

        htm.addln("<nav class='menu'>");

        menuButton(htm, false, student.stuId, EAdminStudentCommand.STUDENT_INFO);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.PLACEMENT);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.REGISTRATIONS);
        menuButton(htm, true, student.stuId, EAdminStudentCommand.ACTIVITY);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.MATH_PLAN);

        htm.add("</nav>");

        htm.addln("<main class='info'>");

        final List<RawStexam> exams = RawStexamLogic.getExams(cache, student.stuId, course, true);

        RawStexam ex = null;
        for (final RawStexam test : exams) {
            if (test.serialNbr.toString().equals(ser)) {
                ex = test;
                break;
            }
        }

        if (ex == null) {
            htm.sP().add("(No exams found)").eP();
        } else {
            htm.sTable("report");
            htm.sTr();
            htm.sTh().add("Course").eTh();
            htm.sTh().add("Unit").eTh();
            htm.sTh().add("Type").eTh();
            htm.sTh().add("Exam ID").eTh();
            htm.sTh().add("Started").eTh();
            htm.sTh().add("Finished").eTh();
            htm.sTh().add("Score").eTh();
            htm.sTh().add("Passed").eTh();
            htm.eTr();

            htm.sTr();
            htm.sTd().add(ex.course).eTd();
            htm.sTd().add(ex.unit).eTd();
            htm.sTd().add(RawExam.getExamTypeName(ex.examType)).eTd();
            htm.sTd().add(ex.version).eTd();
            htm.sTd().add(ex.getStartDateTime() == null ? "N/A"
                    : TemporalUtils.FMT_MDY_AT_HMS_A.format(ex.getStartDateTime())).eTd();
            htm.sTd().add(ex.getFinishDateTime() == null ? "N/A"
                    : TemporalUtils.FMT_MDY_AT_HMS_A.format(ex.getFinishDateTime())).eTd();
            htm.sTd().add(ex.examScore).eTd();
            htm.sTd().add(ex.passed).eTd();
            htm.eTr();

            htm.eTable();
        }
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
