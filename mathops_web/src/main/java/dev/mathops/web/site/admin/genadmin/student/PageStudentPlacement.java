package dev.mathops.web.site.admin.genadmin.student;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.rawlogic.RawMpecrDeniedLogic;
import dev.mathops.db.rawlogic.RawStexamLogic;
import dev.mathops.db.rawlogic.RawStmpeLogic;
import dev.mathops.db.rawlogic.RawStudentLogic;
import dev.mathops.db.rawrecord.RawMpeCredit;
import dev.mathops.db.rawrecord.RawMpecrDenied;
import dev.mathops.db.rawrecord.RawRecordConstants;
import dev.mathops.db.rawrecord.RawStexam;
import dev.mathops.db.rawrecord.RawStmpe;
import dev.mathops.db.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdminTopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;
import dev.mathops.web.site.admin.genadmin.PageError;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pages that displays all information about a single selected student.
 */
public enum PageStudentPlacement {
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
                doStudentPlacementPage(cache, site, req, resp, session, student);
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
    private static void doStudentPlacementPage(final Cache cache, final AdminSite site,
                                               final ServletRequest req, final HttpServletResponse resp,
                                               final ImmutableSessionInfo session, final RawStudent student)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.STUDENT_STATUS, htm);

        htm.sP("studentname")
                .add("<strong class='largeish'>", student.getScreenName(),
                        "</strong> (", student.stuId,
                        ") &nbsp; <a class='ulink' href='student.html'>Clear</a>")
                .eP();

        htm.addln("<nav class='menu'>");

        menuButton(htm, false, student.stuId, EAdminStudentCommand.STUDENT_INFO);
        menuButton(htm, true, student.stuId, EAdminStudentCommand.PLACEMENT);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.REGISTRATIONS);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.ACTIVITY);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.MATH_PLAN);

        htm.add("</nav>");

        htm.addln("<main class='info'>");
        emitStudentPlacementStatus(cache, htm, student);
        htm.addln("</main>");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits general student placement status.
     *
     * @param cache   the data cache
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param student the student record
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitStudentPlacementStatus(final Cache cache, final HtmlBuilder htm,
                                                   final RawStudent student) throws SQLException {

        final String stuId = student.stuId;

        final List<RawStexam> elms = RawStexamLogic.getExams(cache, stuId, RawRecordConstants.M100T, true, "U");
        final List<RawStexam> pre117 = RawStexamLogic.getExams(cache, stuId, "M 1170", true, "U");
        final List<RawStexam> pre118 = RawStexamLogic.getExams(cache, stuId, "M 1180", true, "U");
        final List<RawStexam> pre124 = RawStexamLogic.getExams(cache, stuId, "M 1240", true, "U");
        final List<RawStexam> pre125 = RawStexamLogic.getExams(cache, stuId, "M 1250", true, "U");
        final List<RawStexam> pre126 = RawStexamLogic.getExams(cache, stuId, "M 1260", true, "U");

        htm.sH(4).add("Tutorial Exams").eH(4);
        if (elms.isEmpty() && pre117.isEmpty() && pre118.isEmpty() && pre124.isEmpty()
                && pre125.isEmpty() && pre126.isEmpty()) {
            htm.sDiv("indent").add("(No tutorial exams on record)").eDiv();
            htm.div("vgap");
        } else {
            htm.addln("<ul style='margin-top:0;';>");

            for (final RawStexam row : elms) {
                final LocalDateTime start = row.getStartDateTime();
                final LocalDateTime fin = row.getFinishDateTime();
                if (start != null && fin != null) {
                    final long duration = Duration.between(start, fin).getSeconds();
                    final long min = duration / 60L;
                    final long sec = duration % 60L;

                    htm.addln("<li>ELM Exam submitted ", TemporalUtils.FMT_WMDY_AT_HM_A.format(fin))
                            .br().add(" &nbsp; &nbsp; Version = ", row.version).br()
                            .add(" &nbsp; &nbsp; Serial # = ", row.serialNbr).br()
                            .add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON,
                                    sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec))
                            .br()//
                            .add(" &nbsp; &nbsp; Passed = ", row.passed).br()
                            .add(" &nbsp; &nbsp; Score = ", row.examScore, "/20</li>");
                }
            }

            for (final RawStexam row : pre117) {
                final LocalDateTime start = row.getStartDateTime();
                final LocalDateTime fin = row.getFinishDateTime();
                if (start != null && fin != null) {
                    final long duration = Duration.between(start, fin).getSeconds();
                    final long min = duration / 60L;
                    final long sec = duration % 60L;

                    htm.addln("<li>Precalc Tutorial (117) Exam submitted ",
                                    TemporalUtils.FMT_WMDY_AT_HM_A.format(fin)).br()
                            .add(" &nbsp; &nbsp; Version = ", row.version).br()
                            .add(" &nbsp; &nbsp; Serial # = ", row.serialNbr).br()
                            .add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON,
                                    sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec))
                            .br()//
                            .add(" &nbsp; &nbsp; Passed = ", row.passed).br()
                            .add(" &nbsp; &nbsp; Score = ", row.examScore, "/20</li>");
                }
            }

            for (final RawStexam row : pre118) {
                final LocalDateTime start = row.getStartDateTime();
                final LocalDateTime fin = row.getFinishDateTime();
                if (start != null && fin != null) {
                    final long duration = Duration.between(start, fin).getSeconds();
                    final long min = duration / 60L;
                    final long sec = duration % 60L;

                    htm.addln("<li>Precalc Tutorial (118) Exam submitted ",
                                    TemporalUtils.FMT_WMDY_AT_HM_A.format(fin)).br()
                            .add(" &nbsp; &nbsp; Version = ", row.version).br()
                            .add(" &nbsp; &nbsp; Serial # = ", row.serialNbr).br()
                            .add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON,
                                    sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec))
                            .br()//
                            .add(" &nbsp; &nbsp; Passed = ", row.passed).br()
                            .add(" &nbsp; &nbsp; Score = ", row.examScore, "/20</li>");
                }
            }

            for (final RawStexam row : pre124) {
                final LocalDateTime start = row.getStartDateTime();
                final LocalDateTime fin = row.getFinishDateTime();
                if (start != null && fin != null) {
                    final long duration = Duration.between(start, fin).getSeconds();
                    final long min = duration / 60L;
                    final long sec = duration % 60L;

                    htm.addln("<li>Precalc Tutorial (124) Exam submitted ",
                                    TemporalUtils.FMT_WMDY_AT_HM_A.format(fin)).br()
                            .add(" &nbsp; &nbsp; Version = ", row.version).br()
                            .add(" &nbsp; &nbsp; Serial # = ", row.serialNbr).br()
                            .add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON,
                                    sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec))
                            .br()//
                            .add(" &nbsp; &nbsp; Passed = ", row.passed).br()
                            .add(" &nbsp; &nbsp; Score = ", row.examScore, "/20</li>");
                }
            }

            for (final RawStexam row : pre125) {
                final LocalDateTime start = row.getStartDateTime();
                final LocalDateTime fin = row.getFinishDateTime();
                if (start != null && fin != null) {
                    final long duration = Duration.between(start, fin).getSeconds();
                    final long min = duration / 60L;
                    final long sec = duration % 60L;

                    htm.addln("<li>Precalc Tutorial (125) Exam submitted ",
                                    TemporalUtils.FMT_WMDY_AT_HM_A.format(fin)).br()
                            .add(" &nbsp; &nbsp; Version = ", row.version).br()
                            .add(" &nbsp; &nbsp; Serial # = ", row.serialNbr).br()
                            .add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON,
                                    sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec))
                            .br()//
                            .add(" &nbsp; &nbsp; Passed = ", row.passed).br()
                            .add(" &nbsp; &nbsp; Score = ", row.examScore, "/20</li>");
                }
            }

            for (final RawStexam row : pre126) {
                final LocalDateTime start = row.getStartDateTime();
                final LocalDateTime fin = row.getFinishDateTime();
                if (start != null && fin != null) {
                    final long duration = Duration.between(start, fin).getSeconds();
                    final long min = duration / 60L;
                    final long sec = duration % 60L;

                    htm.addln("<li>Precalc Tutorial (126) Exam submitted ",
                                    TemporalUtils.FMT_WMDY_AT_HM_A.format(fin)).br()
                            .add(" &nbsp; &nbsp; Version = ", row.version).br()
                            .add(" &nbsp; &nbsp; Serial # = ", row.serialNbr).br()
                            .add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON,
                                    sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec))
                            .br()//
                            .add(" &nbsp; &nbsp; Passed = ", row.passed).br()
                            .add(" &nbsp; &nbsp; Score = ", row.examScore, "/20</li>");
                }
            }

            htm.addln("</ul>");
        }

        final List<RawStmpe> attempts = RawStmpeLogic.queryLegalByStudent(cache, stuId);

        htm.sH(4).add("Placement Attempts").eH(4);
        if (attempts.isEmpty()) {
            htm.sDiv("indent").add("(No attempts on record)").eDiv();
            htm.div("vgap");
        } else {
            htm.addln("<ul style='margin-top:0;';>");
            for (final RawStmpe row : attempts) {
                final LocalDateTime start = row.getStartDateTime();
                final LocalDateTime fin = row.getFinishDateTime();

                if (start != null && fin != null) {
                    final long duration = Duration.between(start, fin).getSeconds();
                    final long min = duration / 60L;
                    final long sec = duration % 60L;

                    htm.addln("<li>Attempt submitted ", TemporalUtils.FMT_WMDY_AT_HM_A.format(fin)).br()
                            .add(" &nbsp; &nbsp; Version = ", row.version).br()
                            .add(" &nbsp; &nbsp; Serial # = ", row.serialNbr).br()
                            .add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON,
                                    sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec))
                            .br()//
                            .add(" &nbsp; &nbsp; Placed = ", row.placed).br()
                            .add(" &nbsp; &nbsp; Subtests: A = ", //
                                    row.stsA, "/8; 117 = ",
                                    row.sts117, "/12; 118 = ",
                                    row.sts118, "/8; 124 = ",
                                    row.sts124, "/10; 125 = ",
                                    row.sts125, "/9; 126 = ",
                                    row.sts126, "/8</li>");
                }
            }
            htm.addln("</ul>");
        }

        final List<RawMpeCredit> credit = RawMpeCreditLogic.queryByStudent(cache, stuId);
        htm.sH(4).add("Placement Credit Earned").eH(4);
        if (credit.isEmpty()) {
            htm.sDiv("indent").add("(None)").eDiv();
            htm.div("vgap");
        } else {
            htm.addln("<ul style='margin-top:0;';>");
            for (final RawMpeCredit row : credit) {
                if ("P".equals(row.examPlaced)) {
                    htm.add("<li>Placed out of ", row.course, " (serial # ", row.serialNbr, ")</li>");
                } else if ("C".equals(row.examPlaced)) {
                    htm.add("<li>Earned credit for ", row.course, " (serial # ", row.serialNbr, ")</li>");
                }
            }
            htm.addln("</ul>");
        }

        final List<RawMpecrDenied> denied = RawMpecrDeniedLogic.queryByStudent(cache, stuId);

        htm.sH(4).add("Placement Credit Denied").eH(4);
        if (denied.isEmpty()) {
            htm.sDiv("indent").add("(None)").eDiv();
            htm.div("vgap");
        } else {
            htm.addln("<ul style='margin-top:0;';>");
            for (final RawMpecrDenied row : denied) {
                if ("P".equals(row.examPlaced)) {
                    htm.add("<li>Denied placement out of ", row.course, " (serial # ",
                            row.serialNbr, ")</li>");
                } else if ("C".equals(row.examPlaced)) {
                    htm.add("<li>Denied credit for ", row.course, " (serial # ", row.serialNbr,
                            ")</li>");
                }
            }
            htm.addln("</ul>");
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
}
