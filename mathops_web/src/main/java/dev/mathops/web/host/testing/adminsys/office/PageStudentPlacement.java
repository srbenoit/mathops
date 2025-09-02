package dev.mathops.web.host.testing.adminsys.office;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.field.ERole;
import dev.mathops.db.schema.legacy.impl.RawMpeCreditLogic;
import dev.mathops.db.schema.legacy.impl.RawMpecrDeniedLogic;
import dev.mathops.db.schema.legacy.impl.RawStchallengeLogic;
import dev.mathops.db.schema.legacy.impl.RawStexamLogic;
import dev.mathops.db.schema.legacy.impl.RawStmpeLogic;
import dev.mathops.db.schema.legacy.impl.RawStudentLogic;
import dev.mathops.db.schema.legacy.rec.RawMpeCredit;
import dev.mathops.db.schema.legacy.rec.RawMpecrDenied;
import dev.mathops.db.schema.legacy.rec.RawStchallenge;
import dev.mathops.db.schema.legacy.rec.RawStexam;
import dev.mathops.db.schema.legacy.rec.RawStmpe;
import dev.mathops.db.schema.legacy.rec.RawStudent;
import dev.mathops.db.schema.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Pages that displays a record of a student's activity.
 */
enum PageStudentPlacement {
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
            htm.addln("<button class='nav'>Activity</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_exams.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Exams</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_placement.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='navlit'>Placement</button>");
            htm.addln("</form>");
            htm.eDiv(); // narrowstack

            htm.sDiv("detail");
            emitStudentPlacement(cache, htm, student);
            htm.eDiv(); // detail
        }

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits general student placement status.
     *
     * @param cache   the data cache
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param student the student record
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitStudentPlacement(final Cache cache, final HtmlBuilder htm,
                                             final RawStudent student) throws SQLException {

        final String stuId = student.stuId;

        final List<RawStexam> elms = RawStexamLogic.getExams(cache, stuId, RawRecordConstants.M100T, true, "U");
        final List<RawStexam> pre117 = RawStexamLogic.getExams(cache, stuId, "M 1170", true, "U");
        final List<RawStexam> pre118 = RawStexamLogic.getExams(cache, stuId, "M 1180", true, "U");
        final List<RawStexam> pre124 = RawStexamLogic.getExams(cache, stuId, "M 1240", true, "U");
        final List<RawStexam> pre125 = RawStexamLogic.getExams(cache, stuId, "M 1250", true, "U");
        final List<RawStexam> pre126 = RawStexamLogic.getExams(cache, stuId, "M 1260", true, "U");

        final List<RawStchallenge> challenges = RawStchallengeLogic.queryByStudent(cache, stuId);
        final List<RawStmpe> attempts = RawStmpeLogic.queryLegalByStudent(cache, stuId);
        final List<RawMpeCredit> credit = RawMpeCreditLogic.queryByStudent(cache, stuId);
        final List<RawMpecrDenied> denied = RawMpecrDeniedLogic.queryByStudent(cache, stuId);

        htm.sH(4).add("Tutorial Exams").eH(4);

        if (elms.isEmpty() && pre117.isEmpty() && pre118.isEmpty() && pre124.isEmpty() && pre125.isEmpty()
                && pre126.isEmpty()) {
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

                    htm.addln("<li>ELM Exam submitted ", TemporalUtils.FMT_WMDY_AT_HM_A.format(fin)).br()
                            .add(" &nbsp; &nbsp; Version = ", row.version, " (serial # = ", row.serialNbr, ")")
                            .br().add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON,
                                    sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec)).br()//
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
                            .add(" &nbsp; &nbsp; Version = ", row.version, " (serial # = ", row.serialNbr, ")").br()
                            .add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON,
                                    sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec)).br()
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
                            .add(" &nbsp; &nbsp; Version = ", row.version, " (serial # = ", row.serialNbr, ")").br()
                            .add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON,
                                    sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec)).br()
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
                            .add(" &nbsp; &nbsp; Version = ", row.version, " (serial # = ", row.serialNbr, ")")
                            .br().add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON,
                                    sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec)).br()
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
                            .add(" &nbsp; &nbsp; Version = ", row.version, " (serial # = ", row.serialNbr, ")")
                            .br().add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON,
                                    sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec)).br()
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
                            .add(" &nbsp; &nbsp; Version = ", row.version, " (serial # = ", row.serialNbr, ")").br()
                            .add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON,
                                    sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec)).br()
                            .add(" &nbsp; &nbsp; Passed = ", row.passed).br()
                            .add(" &nbsp; &nbsp; Score = ", row.examScore, "/20</li>");
                }
            }

            htm.addln("</ul>");
        }

        htm.sH(4).add("Challenge Exam Attempts").eH(4);
        if (challenges.isEmpty()) {
            htm.sDiv("indent").add("(No attempts on record)").eDiv();
            htm.div("vgap");
        } else {
            htm.addln("<ul style='margin-top:0;';>");
            for (final RawStchallenge row : challenges) {

                long duration = 0L;
                LocalTime end = null;

                if (row.startTime != null && row.finishTime != null) {
                    final int startMin = row.startTime.intValue();
                    final int finishMin = row.finishTime.intValue();

                    duration = (long) (finishMin - startMin) * 60L;
                    end = LocalTime.of(finishMin / 60, finishMin % 60);
                }

                if (end != null) {
                    final long min = duration / 60L;

                    final LocalDateTime ldt = LocalDateTime.of(row.examDt, end);
                    htm.addln("<li>Attempt submitted ", TemporalUtils.FMT_WMDY_AT_HM_A.format(ldt)).br()
                            .add(" &nbsp; &nbsp; Version = ", row.version, " (serial # = ", row.serialNbr, ")").br()
                            .add(" &nbsp; &nbsp; Time Spent = ", Long.toString(min), CoreConstants.COLON, "00").br()
                            .add(" &nbsp; &nbsp; Passed = ", row.passed).br()
                            .add(" &nbsp; &nbsp; Score = ", row.score, "</li>");
                }
            }
            htm.addln("</ul>");
        }

        int numPlacementToolUsed = 0;
        htm.sH(4).add("Placement Attempts").eH(4);
        if (attempts.isEmpty()) {
            htm.sDiv("indent").add("(No attempts on record)").eDiv();
        } else {
            for (final RawStmpe row : attempts) {

                if (row.version.startsWith("MPT")) {
                    ++numPlacementToolUsed;
                }

                final LocalDateTime start = row.getStartDateTime();
                final LocalDateTime fin = row.getFinishDateTime();

                if (start != null && fin != null) {
                    final long duration = Duration.between(start, fin).getSeconds();
                    final long min = duration / 60L;
                    final long sec = duration % 60L;

                    htm.add("<details><summary>");
                    htm.addln("Attempt submitted ", TemporalUtils.FMT_WMDY_AT_HM_A.format(fin)).br();
                    htm.addln("</summary>");

                    htm.addln("<ul>");
                    htm.addln("<li>Version: ", row.version, " (serial #", row.serialNbr, ")</li>");
                    htm.addln("<li>Time Spent: ", Long.toString(min), CoreConstants.COLON,
                            sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec), "</li>");
                    htm.addln("<li>Placed: ", row.placed, "</li>");
                    htm.addln("<li>Subtests: A = ",
                            row.stsA, "/8; 117 = ",
                            row.sts117, "/12; 118 = ",
                            row.sts118, "/8; 124 = ",
                            row.sts124, "/10; 125 = ",
                            row.sts125, "/9; 126 = ",
                            row.sts126, "/8</li>");

                    boolean searching = true;
                    for (final RawMpeCredit cr : credit) {
                        if (cr.serialNbr.equals(row.serialNbr)) {
                            if ("P".equals(cr.examPlaced)) {
                                htm.add("<li>Placed out of ", cr.course, "</li>");
                                searching = false;
                            } else if ("C".equals(cr.examPlaced)) {
                                htm.add("<li>Earned credit for ", cr.course, "</li>");
                                searching = false;
                            }
                        }
                    }
                    if (searching) {
                        htm.add("<li>No placement earned</li>");
                    }

                    htm.addln("</ul>");
                    htm.add("</details>");
                }
            }
        }
        htm.div("vgap");

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

        htm.sH(4).add("Placement Credit Denied").eH(4);
        if (denied.isEmpty()) {
            htm.sDiv("indent").add("(None)").eDiv();
            htm.div("vgap");
        } else {
            htm.addln("<ul style='margin-top:0;';>");
            for (final RawMpecrDenied row : denied) {
                if ("P".equals(row.examPlaced)) {
                    htm.add("<li>Denied placement out of ", row.course, " (serial # ", row.serialNbr, ")</li>");
                } else if ("C".equals(row.examPlaced)) {
                    htm.add("<li>Denied credit for ", row.course, " (serial # ", row.serialNbr, ")</li>");
                }
            }
            htm.addln("</ul>");
        }

        if (numPlacementToolUsed < 2) {
            htm.sH(4).add("Placement Attempts Available: ", Integer.toString(2 - numPlacementToolUsed)).eH(4);
        } else {
            htm.sH(4).add("Placement Attempts Available: NONE").eH(4);
        }
    }
}
