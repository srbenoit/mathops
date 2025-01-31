package dev.mathops.web.site.admin.office;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.ChallengeExamLogic;
import dev.mathops.db.old.logic.PrerequisiteLogic;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawlogic.RawPendingExamLogic;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.db.old.rawrecord.RawPendingExam;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
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
import java.util.List;
import java.util.Objects;

/**
 * A page to handle issuance of an exam on a specified testing station.
 */
enum PageTestingIssueExam {
    ;

    /**
     * Generates the page that gathers information to issue a testing center calculator.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = OfficePage.startOfficePage(cache, site, session, true);

        htm.sDiv("center");
        htm.sH(2).add("Testing Center").eH(2);

        htm.sDiv("buttonstack");

        htm.hr().div("vgap");
        htm.sH(3).add("Testing Center Calculators").eH(3);

        htm.addln("<form method='get' action='testing_issue_calc.html' ",
                "style='display:inline-block; width:150px;'>",
                "<button class='nav'>Issue</button>", //
                "</form>",
                "<form method='get' action='testing_collect_calc.html' ",
                "style='display:inline-block; width:150px;'>",
                "<button class='nav'>Collect</button>", //
                "</form>");

        htm.hr().div("vgap");
        htm.sH(3).add("Testing Center & Quiet Testing").eH(3);

        htm.addln("<form method='get' action='testing_issue_exam.html'>");
        htm.add("<button class='navlit'>Issue Exam</button>");
        htm.addln("</form>");

        htm.hr().div("vgap");

        htm.eDiv(); // buttonstack

        final String stuId = req.getParameter("stu");
        final String exam = req.getParameter("exam");
        final String seat = req.getParameter("seat");

        htm.addln("<form class='stuform' method='post' action='testing_issue_exam.html'>");

        if (stuId == null || stuId.isEmpty()) {
            // Request the student ID
            emitStudentIdField(htm, null, false);
            emitSubmit(htm, "Submit");
        } else {
            final String cleanStu = stuId.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY)
                    .replace(CoreConstants.DASH, CoreConstants.EMPTY);

            final RawStudent stu = RawStudentLogic.query(cache, cleanStu, false);

            if (stu == null) {
                // Invalid student ID - request the student ID again
                emitStudentIdField(htm, null, false);
                emitError(htm, "Student not found");
                emitSubmit(htm, "Reset Form");
            } else {
                final List<RawPendingExam> pend =
                        RawPendingExamLogic.queryByStudent(cache, stu.stuId);

                if (pend.isEmpty()) {
                    boolean go = false;
                    if (req.getParameter("ignorehold") != null) {
                        go = true;
                    } else if (req.getParameter("cancel") != null) {
                        emitStudentIdField(htm, null, false);
                        emitError(htm, "Canceled.");
                        emitSubmit(htm, "Reset Form");
                    } else {
                        final List<RawAdminHold> holds;
                        if ("F".equals(stu.sevAdminHold)) {
                            holds = RawAdminHoldLogic.queryByStudent(cache, stu.stuId);
                            if (holds.isEmpty()) {
                                RawStudentLogic.updateHoldSeverity(cache, stu.stuId, null);
                                go = true;
                            } else {
                                emitStudentIdField(htm, stuId, true);
                                emitError(htm, holds.size() == 1 ? "Student has a hold:" : "Student has holds:");

                                for (final RawAdminHold h : holds) {
                                    final String msg = RawAdminHoldLogic.getStaffMessage(h.holdId);
                                    emitInfo(htm, Objects.requireNonNullElseGet(msg, () -> "Hold " + h.holdId));
                                }

                                emitContinue(htm);
                            }
                        } else {
                            go = true;
                        }
                    }

                    if (go) {
                        final String name = (stu.prefName == null ? stu.firstName : stu.prefName)
                                + CoreConstants.SPC + stu.lastName;
                        htm.addln("<input type='hidden' name='ignorehold' value='x'/>");

                        if (exam == null) {
                            // Request the exam ID
                            emitStudentIdField(htm, stuId, true);
                            emitInfo(htm, name);
                            emitExamList(htm, null);
                            emitSubmit(htm, "Submit");
                        } else {
                            final String errmsg = verifyEligible(cache, stu, exam);

                            if (errmsg == null) {
                                // Student is eligible
                                if (seat == null) {
                                    // Choose a seat...
                                    emitStudentIdField(htm, stuId, true);
                                    emitInfo(htm, name);
                                    emitExamList(htm, exam);
                                    emitSeatList(cache, htm);
                                    emitSubmit(htm, "Submit");
                                } else {
                                    final String err = issueExam(cache, stu, exam, seat);

                                    if (err == null) {
                                        final String station = seat.substring(2);
                                        if (seat.startsWith("4_")) {
                                            emitInfo(htm, "Exam issued to station " + station
                                                    + " in the <b>Quiet Testing</b> room; student should "
                                                    + "log in right away");
                                        } else {
                                            emitInfo(htm, "Exam issued to station " + station
                                                    + " in the <b>Testing Center</b>; student should "
                                                    + "log in right away");

                                        }
                                    }
                                    emitClose(htm);
                                }
                            } else {
                                emitStudentIdField(htm, stuId, true);
                                emitInfo(htm, name);
                                emitExamList(htm, exam);
                                emitError(htm, "Student not eligible");
                                emitInfo(htm, errmsg);
                                emitSubmit(htm, "Reset Form");
                            }
                        }
                    }
                } else {
                    // Student has exam in progress
                    emitStudentIdField(htm, null, false);
                    emitError(htm, "Student already has proctored exam in progress");
                    emitSubmit(htm, "Reset Form");
                }
            }
        }

        htm.addln("</form>");

        htm.eDiv(); // buttonstack

        htm.eDiv(); // Center

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Tests whether the student is eligible for the selected exam.
     *
     * @param cache   the data cache
     * @param student the student record
     * @param examId  the exam ID
     * @return an error message if the student is not eligible; {@code null} if eligible
     * @throws SQLException if there is an error accessing the database
     */
    private static String verifyEligible(final Cache cache, final RawStudent student,
                                         final String examId) throws SQLException {

        String errmsg = null;
        final String studentId = student.stuId;

        final TermRec active = cache.getSystemData().getActiveTerm();

        final Integer unit1 = Integer.valueOf(1);
        final Integer unit2 = Integer.valueOf(2);
        final Integer unit3 = Integer.valueOf(3);
        final Integer unit4 = Integer.valueOf(4);

        if ("MT4UE".equals(examId)) {
            // Student MUST have a passed Unit Review exam in unit 4 of M 100T
            final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId, RawRecordConstants.M100T, unit4,
                    true, "R");
            if (exams.isEmpty()) {
                errmsg = "Student has not passed Unit 4 Review in the ELM Tutorial.";
            }
        } else if ("7T4UE".equals(examId)) {
            // Student MUST have a passed Unit Review exam in unit 4 of M 1170
            final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId, RawRecordConstants.M1170, unit4,
                    true, "R");
            if (exams.isEmpty()) {
                errmsg = "Student has not passed Unit 4 Review in the Algebra I Tutorial.";
            }
        } else if ("8T4UE".equals(examId)) {
            // Student MUST have a passed Unit Review exam in unit 4 of M 1180
            final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId, RawRecordConstants.M1180, unit4,
                    true, "R");
            if (exams.isEmpty()) {
                errmsg = "Student has not passed Unit 4 Review in the Algebra II Tutorial.";
            }
        } else if ("4T4UE".equals(examId)) {
            // Student MUST have a passed Unit Review exam in unit 4 of M 1240
            final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId, RawRecordConstants.M1240, unit4,
                    true, "R");
            if (exams.isEmpty()) {
                errmsg = "Student has not passed Unit 4 Review in the Functions Tutorial.";
            }
        } else if ("5T4UE".equals(examId)) {
            // Student MUST have a passed Unit Review exam in unit 4 of M 1250
            final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId, RawRecordConstants.M1250, unit4,
                    true, "R");
            if (exams.isEmpty()) {
                errmsg = "Student has not passed Unit 4 Review in the Trig I Tutorial.";
            }
        } else if ("6T4UE".equals(examId)) {
            // Student MUST have a passed Unit Review exam in unit 4 of M 1260
            final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId, RawRecordConstants.M1260, unit4,
                    true, "R");
            if (exams.isEmpty()) {
                errmsg = "Student has not passed Unit 4 Review in the Trig II Tutorial.";
            }
        } else if ("MPTTC".equals(examId)) {
            // Student MUST have a passed Unit Review exam in unit 4 of M 1260
            final List<RawStmpe> exams = RawStmpeLogic.queryLegalByStudent(cache, studentId);
            int numUsed = 0;
            for (final RawStmpe attempt : exams) {
                final String id = attempt.version;
                if ("PPPPP".equals(id) || id.startsWith("MPT")) {
                    ++numUsed;
                }
            }
            if (numUsed >= 2) {
                errmsg = "Student has no placement attempts remaining.";
            }
        } else if (ChallengeExamLogic.M117_CHALLENGE_EXAM_ID.equals(examId)) {
            final List<RawStchallenge> tries =
                    RawStchallengeLogic.queryByStudentCourse(cache, studentId, RawRecordConstants.M117);

            if (tries.isEmpty()) {
                final List<RawStcourse> regs =
                        RawStcourseLogic.queryByStudent(cache, studentId, active.term, false, false);
                boolean in117 = false;
                for (final RawStcourse reg : regs) {
                    // An "ignored" registration is still considered a registration
                    if (reg.course.equals(RawRecordConstants.M117)) {
                        in117 = true;
                        break;
                    }
                }
                if (in117) {
                    errmsg = "Student cannot challenge MATH 117 while enrolled";
                } else if (!new PrerequisiteLogic(cache, studentId)
                        .hasSatisfiedPrerequisitesFor(RawRecordConstants.M117)) {
                    errmsg = "Student has not satisfied prereqs for MATH 117";
                }
            } else {
                errmsg = "Student has already challenged MATH 117";
            }
        } else if (ChallengeExamLogic.M118_CHALLENGE_EXAM_ID.equals(examId)) {
            final List<RawStchallenge> tries =
                    RawStchallengeLogic.queryByStudentCourse(cache, studentId, RawRecordConstants.M118);

            if (tries.isEmpty()) {
                final List<RawStcourse> regs =
                        RawStcourseLogic.queryByStudent(cache, studentId, active.term, false, false);
                boolean in118 = false;
                for (final RawStcourse reg : regs) {
                    // An "ignored" registration is still considered a registration
                    if (reg.course.equals(RawRecordConstants.M118)) {
                        in118 = true;
                        break;
                    }
                }
                if (in118) {
                    errmsg = "Student cannot challenge MATH 118 while enrolled";
                } else if (!new PrerequisiteLogic(cache, studentId)
                        .hasSatisfiedPrerequisitesFor(RawRecordConstants.M118)) {
                    errmsg = "Student has not satisfied prereqs for MATH 118";
                }
            } else {
                errmsg = "Student has already challenged MATH 118";
            }
        } else if (ChallengeExamLogic.M124_CHALLENGE_EXAM_ID.equals(examId)) {
            final List<RawStchallenge> tries =
                    RawStchallengeLogic.queryByStudentCourse(cache, studentId, RawRecordConstants.M124);

            if (tries.isEmpty()) {
                final List<RawStcourse> regs =
                        RawStcourseLogic.queryByStudent(cache, studentId, active.term, false, false);
                boolean in124 = false;
                for (final RawStcourse reg : regs) {
                    // An "ignored" registration is still considered a registration
                    if (reg.course.equals(RawRecordConstants.M124)) {
                        in124 = true;
                        break;
                    }
                }
                if (in124) {
                    errmsg = "Student cannot challenge MATH 124 while enrolled";
                } else if (!new PrerequisiteLogic(cache, studentId)
                        .hasSatisfiedPrerequisitesFor(RawRecordConstants.M124)) {
                    errmsg = "Student has not satisfied prereqs for MATH 124";
                }
            } else {
                errmsg = "Student has already challenged MATH 124";
            }
        } else if (ChallengeExamLogic.M125_CHALLENGE_EXAM_ID.equals(examId)) {
            final List<RawStchallenge> tries =
                    RawStchallengeLogic.queryByStudentCourse(cache, studentId, RawRecordConstants.M125);

            if (tries.isEmpty()) {
                final List<RawStcourse> regs =
                        RawStcourseLogic.queryByStudent(cache, studentId, active.term, false, false);

                boolean in125 = false;
                for (final RawStcourse reg : regs) {
                    // An "ignored" registration is still considered a registration
                    if (reg.course.equals(RawRecordConstants.M125)) {
                        in125 = true;
                        break;
                    }
                }
                if (in125) {
                    errmsg = "Student cannot challenge MATH 125 while enrolled";
                } else if (!new PrerequisiteLogic(cache, studentId)
                        .hasSatisfiedPrerequisitesFor(RawRecordConstants.M125)) {
                    errmsg = "Student has not satisfied prereqs for MATH 125";
                }
            } else {
                errmsg = "Student has already challenged MATH 125";
            }
        } else if (ChallengeExamLogic.M126_CHALLENGE_EXAM_ID.equals(examId)) {
            final List<RawStchallenge> tries =
                    RawStchallengeLogic.queryByStudentCourse(cache, studentId, RawRecordConstants.M126);

            if (tries.isEmpty()) {
                final List<RawStcourse> regs =
                        RawStcourseLogic.queryByStudent(cache, studentId, active.term, false, false);
                boolean in126 = false;
                for (final RawStcourse reg : regs) {
                    // An "ignored" registration is still considered a registration
                    if (reg.course.equals(RawRecordConstants.M126)) {
                        in126 = true;
                        break;
                    }
                }
                if (in126) {
                    errmsg = "Student cannot challenge MATH 126 while enrolled";
                } else if (!new PrerequisiteLogic(cache, studentId)
                        .hasSatisfiedPrerequisitesFor(RawRecordConstants.M126)) {
                    errmsg = "Student has not satisfied prereqs for MATH 126";
                }
            } else {
                errmsg = "Student has already challenged MATH 126";
            }
        } else if ("Y".equals(student.licensed)) {

            switch (examId) {
                case "171UE" -> {
                    // Student MUST have a passed Unit 1 Review exam in M 117
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M117, unit1, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 1 Review in MATH 117.";
                    }
                }
                case "172UE" -> {
                    // Student MUST have a passed Unit 2 Review exam in M 117
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M117, unit2, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 2 Review in MATH 117.";
                    }
                }
                case "173UE" -> {
                    // Student MUST have a passed Unit 3 Review exam in M 117
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M117, unit3, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 3 Review in MATH 117.";
                    }
                }
                case "174UE" -> {
                    // Student MUST have a passed Unit 4 Review exam in M 117
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M117, unit1, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 4 Review in MATH 117.";
                    }
                }
                case "17FIN" -> {
                    // Student MUST have a passed Unit 4 exam in M 117
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M117, unit4, true, "U");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 4 Exam in MATH 117.";
                    }
                }
                case "181UE" -> {
                    // Student MUST have a passed Unit 1 Review exam in M 118
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M118, unit1, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 1 Review in MATH 118.";
                    }
                }
                case "182UE" -> {
                    // Student MUST have a passed Unit 2 Review exam in M 118
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M118, unit2, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 2 Review in MATH 118.";
                    }
                }
                case "183UE" -> {
                    // Student MUST have a passed Unit 3 Review exam in M 118
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M118, unit3, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 3 Review in MATH 118.";
                    }
                }
                case "184UE" -> {
                    // Student MUST have a passed Unit 4 Review exam in M 118
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M118, unit4, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 4 Review in MATH 118.";
                    }
                }
                case "18FIN" -> {
                    // Student MUST have a passed Unit 4 exam in M 118
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M118, unit4, true, "U");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 4 Exam in MATH 118.";
                    }
                }
                case "241UE" -> {
                    // Student MUST have a passed Unit 1 Review exam in M 124
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M124, unit1, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 1 Review in MATH 124.";
                    }
                }
                case "242UE" -> {
                    // Student MUST have a passed Unit 2 Review exam in M 124
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M124, unit2, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 2 Review in MATH 124.";
                    }
                }
                case "243UE" -> {
                    // Student MUST have a passed Unit 3 Review exam in M 124
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M124, unit3, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 3 Review in MATH 124.";
                    }
                }
                case "244UE" -> {
                    // Student MUST have a passed Unit 4 Review exam in M 124
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M124, unit4, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 4 Review in MATH 124.";
                    }
                }
                case "24FIN" -> {
                    // Student MUST have a passed Unit 4 exam in M 124
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M124, unit4, true, "U");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 4 Exam in MATH 124.";
                    }
                }
                case "251UE" -> {
                    // Student MUST have a passed Unit 1 Review exam in M 125
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M125, unit1, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 1 Review in MATH 125.";
                    }
                }
                case "252UE" -> {
                    // Student MUST have a passed Unit 2 Review exam in M 125
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M125, unit2, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 2 Review in MATH 125.";
                    }
                }
                case "253UE" -> {
                    // Student MUST have a passed Unit 3 Review exam in M 125
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M125, unit3, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 3 Review in MATH 125.";
                    }
                }
                case "254UE" -> {
                    // Student MUST have a passed Unit 4 Review exam in M 125
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M125, unit4, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 4 Review in MATH 125.";
                    }
                }
                case "25FIN" -> {
                    // Student MUST have a passed Unit 4 exam in M 125
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M125, unit4, true, "U");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 4 Exam in MATH 125.";
                    }
                }
                case "261UE" -> {
                    // Student MUST have a passed Unit 1 Review exam in M 125
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M126, unit1, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 1 Review in MATH 126.";
                    }
                }
                case "262UE" -> {
                    // Student MUST have a passed Unit 2 Review exam in M 125
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M126, unit2, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 2 Review in MATH 126.";
                    }
                }
                case "263UE" -> {
                    // Student MUST have a passed Unit 3 Review exam in M 125
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M126, unit3, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 3 Review in MATH 126.";
                    }
                }
                case "264UE" -> {
                    // Student MUST have a passed Unit 4 Review exam in M 125
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M126, unit4, true, "R");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 4 Review in MATH 126.";
                    }
                }
                case "26FIN" -> {
                    // Student MUST have a passed Unit 4 exam in M 125
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId,
                            RawRecordConstants.M126, unit4, true, "U");
                    if (exams.isEmpty()) {
                        errmsg = "Student has not passed Unit 4 Exam in MATH 126.";
                    }
                }
                case null, default -> errmsg = "Exam " + examId + " is not implemented";
            }
        } else {
            errmsg = "Student has not passed the User's Exam.";
        }

        return errmsg;
    }

    /**
     * Attempts to issue the exam.
     *
     * @param cache   the data cache
     * @param student the student record
     * @param examId  the exam ID
     * @param seat    a string with the testing center ID, an underscore, and the station number
     * @return an error message if the student is not eligible; {@code null} if eligible
     * @throws SQLException if there is an error accessing the database
     */
    private static String issueExam(final Cache cache, final RawStudent student,
                                    final String examId, final String seat) throws SQLException {

        String errmsg = null;

        final List<RawClientPc> stations = RawClientPcLogic.queryByTestingCenter(cache, seat.substring(1));
        final String stationNumber = seat.substring(2);

        RawClientPc station = null;
        for (final RawClientPc test : stations) {
            if (stationNumber.equals(test.stationNbr)) {
                station = test;
                break;
            }
        }

        if (station == null) {
            errmsg = "There was an error selecting the testing station";
        } else if (RawClientPc.STATUS_LOCKED.equals(station.currentStatus)) {

            String courseId = null;
            int unit = 0;

            switch (examId) {
                case "MT4UE" -> {
                    courseId = RawRecordConstants.M100T;
                    unit = 4;
                }
                case "7T4UE" -> {
                    courseId = RawRecordConstants.M1170;
                    unit = 4;
                }
                case "8T4UE" -> {
                    courseId = RawRecordConstants.M1180;
                    unit = 4;
                }
                case "4T4UE" -> {
                    courseId = RawRecordConstants.M1240;
                    unit = 4;
                }
                case "5T4UE" -> {
                    courseId = RawRecordConstants.M1250;
                    unit = 4;
                }
                case "6T4UE" -> {
                    courseId = RawRecordConstants.M1260;
                    unit = 4;
                }
                case "MPTTC" -> {
                    courseId = RawRecordConstants.M100P;
                    unit = 1;
                }
                case ChallengeExamLogic.M117_CHALLENGE_EXAM_ID -> courseId = RawRecordConstants.M117;
                case ChallengeExamLogic.M118_CHALLENGE_EXAM_ID -> courseId = RawRecordConstants.M118;
                case ChallengeExamLogic.M124_CHALLENGE_EXAM_ID -> courseId = RawRecordConstants.M124;
                case ChallengeExamLogic.M125_CHALLENGE_EXAM_ID -> courseId = RawRecordConstants.M125;
                case ChallengeExamLogic.M126_CHALLENGE_EXAM_ID -> courseId = RawRecordConstants.M126;
                case "171UE" -> {
                    courseId = RawRecordConstants.M117;
                    unit = 1;
                }
                case "172UE" -> {
                    courseId = RawRecordConstants.M117;
                    unit = 2;
                }
                case "173UE" -> {
                    courseId = RawRecordConstants.M117;
                    unit = 3;
                }
                case "174UE" -> {
                    courseId = RawRecordConstants.M117;
                    unit = 4;
                }
                case "17FIN" -> {
                    courseId = RawRecordConstants.M117;
                    unit = 5;
                }
                case "181UE" -> {
                    courseId = RawRecordConstants.M118;
                    unit = 1;
                }
                case "182UE" -> {
                    courseId = RawRecordConstants.M118;
                    unit = 2;
                }
                case "183UE" -> {
                    courseId = RawRecordConstants.M118;
                    unit = 3;
                }
                case "184UE" -> {
                    courseId = RawRecordConstants.M118;
                    unit = 4;
                }
                case "18FIN" -> {
                    courseId = RawRecordConstants.M118;
                    unit = 5;
                }
                case "241UE" -> {
                    courseId = RawRecordConstants.M124;
                    unit = 1;
                }
                case "242UE" -> {
                    courseId = RawRecordConstants.M124;
                    unit = 2;
                }
                case "243UE" -> {
                    courseId = RawRecordConstants.M124;
                    unit = 3;
                }
                case "244UE" -> {
                    courseId = RawRecordConstants.M124;
                    unit = 4;
                }
                case "24FIN" -> {
                    courseId = RawRecordConstants.M124;
                    unit = 5;
                }
                case "251UE" -> {
                    courseId = RawRecordConstants.M125;
                    unit = 1;
                }
                case "252UE" -> {
                    courseId = RawRecordConstants.M125;
                    unit = 2;
                }
                case "253UE" -> {
                    courseId = RawRecordConstants.M125;
                    unit = 3;
                }
                case "254UE" -> {
                    courseId = RawRecordConstants.M125;
                    unit = 4;
                }
                case "25FIN" -> {
                    courseId = RawRecordConstants.M125;
                    unit = 5;
                }
                case "261UE" -> {
                    courseId = RawRecordConstants.M126;
                    unit = 1;
                }
                case "262UE" -> {
                    courseId = RawRecordConstants.M126;
                    unit = 2;
                }
                case "263UE" -> {
                    courseId = RawRecordConstants.M126;
                    unit = 3;
                }
                case "264UE" -> {
                    courseId = RawRecordConstants.M126;
                    unit = 4;
                }
                case "26FIN" -> {
                    courseId = RawRecordConstants.M126;
                    unit = 5;
                }
                case null, default -> errmsg = "Exam " + examId + " is not implemented";
            }

            if (!RawClientPcLogic.updateAllCurrent(cache, station.computerId,
                    RawClientPc.STATUS_LOGIN_NOCHECK, student.stuId, courseId, Integer.valueOf(unit), examId)) {
                errmsg = "There was an error issuing the exam.";
            }
        } else {
            errmsg = "Station is no longer available (checkin may have assigned a student to it)";
        }

        return errmsg;
    }

    /**
     * Processes the POST from the form to issue a make-up exam. This method validates the request parameters, and
     * inserts a new record of an in-progress make-up exam, then prints status.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final AdminSite site, final ServletRequest req,
                       final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        doGet(cache, site, req, resp, session);
    }

    /**
     * Emits the labeled field for the student ID.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param value    the initial value of the field
     * @param readonly true if the field should be read-only
     */
    private static void emitStudentIdField(final HtmlBuilder htm, final String value, final boolean readonly) {

        htm.sTable().sTr();
        htm.sTd("r", "style='width:88px;'").add("Student&nbsp;ID:").eTd().sTd();
        htm.add("<input style='width:188px;' type='text' name='stu'");
        if (readonly) {
            htm.add(" readonly");
        } else {
            htm.add(" autocomplete='off' data-lpignore='true' autofocus");
        }
        if (value != null) {
            htm.add(" value='", value, "'");
        }
        htm.add("/>");
        htm.eTd().eTr().eTable();
    }

    /**
     * Emits the list of exams from which to select.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param examId the selected exam ID; null if none
     */
    private static void emitExamList(final HtmlBuilder htm, final String examId) {

        htm.sTable().sTr();
        htm.sTd("r", "style='width:88px;'").add("Exam:").eTd().sTd();
        htm.addln("<select style='width:188px;' name='exam' id='exam'>");
        htm.addln(" <optgroup label='MATH 117'>");
        htm.addln("  <option value='171UE'", sel("171UE", examId), ">MATH 117, Unit 1</option>");
        htm.addln("  <option value='172UE'", sel("172UE", examId), ">MATH 117, Unit 2</option>");
        htm.addln("  <option value='173UE'", sel("173UE", examId), ">MATH 117, Unit 3</option>");
        htm.addln("  <option value='174UE'", sel("174UE", examId), ">MATH 117, Unit 4</option>");
        htm.addln("  <option value='17FIN'", sel("17FIN", examId), ">MATH 117, Final</option>");
        htm.addln("  <option value='MC117'", sel(ChallengeExamLogic.M117_CHALLENGE_EXAM_ID, examId),
                ">MATH 117, Challenge</option>");
        htm.addln(" </optgroup>");
        htm.addln(" <optgroup label='MATH 118'>");
        htm.addln("  <option value='181UE'", sel("181UE", examId), ">MATH 118, Unit 1</option>");
        htm.addln("  <option value='182UE'", sel("182UE", examId), ">MATH 118, Unit 2</option>");
        htm.addln("  <option value='183UE'", sel("183UE", examId), ">MATH 118, Unit 3</option>");
        htm.addln("  <option value='184UE'", sel("184UE", examId), ">MATH 118, Unit 4</option>");
        htm.addln("  <option value='18FIN'", sel("18FIN", examId), ">MATH 118, Final</option>");
        htm.addln("  <option value='MC118'", sel(ChallengeExamLogic.M118_CHALLENGE_EXAM_ID, examId),
                ">MATH 118, Challenge</option>");
        htm.addln(" </optgroup>");
        htm.addln(" <optgroup label='MATH 124'>");
        htm.addln("  <option value='241UE'", sel("241UE", examId), ">MATH 124, Unit 1</option>");
        htm.addln("  <option value='242UE'", sel("242UE", examId), ">MATH 124, Unit 2</option>");
        htm.addln("  <option value='243UE'", sel("243UE", examId), ">MATH 124, Unit 3</option>");
        htm.addln("  <option value='244UE'", sel("244UE", examId), ">MATH 124, Unit 4</option>");
        htm.addln("  <option value='24FIN'", sel("24FIN", examId), ">MATH 124, Final</option>");
        htm.addln("  <option value='MC124'", sel(ChallengeExamLogic.M124_CHALLENGE_EXAM_ID, examId),
                ">MATH 124, Challenge</option>");
        htm.addln(" </optgroup>");
        htm.addln(" <optgroup label='MATH 125'>");
        htm.addln("  <option value='251UE'", sel("251UE", examId), ">MATH 125, Unit 1</option>");
        htm.addln("  <option value='252UE'", sel("252UE", examId), ">MATH 125, Unit 2</option>");
        htm.addln("  <option value='253UE'", sel("253UE", examId), ">MATH 125, Unit 3</option>");
        htm.addln("  <option value='254UE'", sel("254UE", examId), ">MATH 125, Unit 4</option>");
        htm.addln("  <option value='25FIN'", sel("25FIN", examId), ">MATH 125, Final</option>");
        htm.addln("  <option value='MC125'", sel(ChallengeExamLogic.M125_CHALLENGE_EXAM_ID, examId),
                ">MATH 125, Challenge</option>");
        htm.addln(" </optgroup>");
        htm.addln(" <optgroup label='MATH 126'>");
        htm.addln("  <option value='261UE'", sel("261UE", examId), ">MATH 126, Unit 1</option>");
        htm.addln("  <option value='262UE'", sel("262UE", examId), ">MATH 126, Unit 2</option>");
        htm.addln("  <option value='263UE'", sel("263UE", examId), ">MATH 126, Unit 3</option>");
        htm.addln("  <option value='264UE'", sel("264UE", examId), ">MATH 126, Unit 4</option>");
        htm.addln("  <option value='26FIN'", sel("26FIN", examId), ">MATH 126, Final</option>");
        htm.addln("  <option value='MC126'", sel(ChallengeExamLogic.M126_CHALLENGE_EXAM_ID, examId),
                ">MATH 126, Challenge</option>");
        htm.addln(" </optgroup>");
        htm.addln(" <optgroup label='Tutorials'>");
        htm.addln("  <option value='MT4UE'", sel("MT4UE", examId), ">ELM Exam</option>");
        htm.addln("  <option value='7T4UE'", sel("7T4UE", examId), ">Algebra I Tutorial</option>");
        htm.addln("  <option value='8T4UE'", sel("8T4UE", examId), ">Algebra II Tutorial</option>");
        htm.addln("  <option value='4T4UE'", sel("4T4UE", examId), ">Functions Tutorial</option>");
        htm.addln("  <option value='5T4UE'", sel("5T4UE", examId), ">Trig. I Tutorial</option>");
        htm.addln("  <option value='6T4UE'", sel("6T4UE", examId), ">Trig. II Tutorial</option>");
        htm.addln(" </optgroup>");
        htm.addln(" <optgroup label='Other'>");
        htm.addln("  <option value='UOOOO'", sel("UOOOO", examId), ">User's Exam</option>");
        htm.addln("  <option value='MPTTC'", sel("MPTTC", examId), ">Placement Tool</option>");
        htm.addln(" </optgroup>");
        htm.addln("</select>");

        htm.eTd().eTr().eTable();
    }

    /**
     * Emits a "selected" string if two strings are equal; an empty string if not.
     *
     * @param str1 the first string
     * @param str2 the second string
     * @return the result
     */
    private static String sel(final String str1, final String str2) {

        return str1 != null && str1.equals(str2) ? " selected" : CoreConstants.EMPTY;
    }

    /**
     * Emits a list of all seats in the testing center (including quiet testing) and allows the user to select an
     * available seat.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitSeatList(final Cache cache, final HtmlBuilder htm) throws SQLException {

        final List<RawClientPc> allMain = RawClientPcLogic.queryByTestingCenter(cache, "1");
        final List<RawClientPc> allQuiet = RawClientPcLogic.queryByTestingCenter(cache, "4");

        htm.sTable().sTr();
        htm.sTd("r", "style='width:88px;'").add("Station:").eTd().sTd();
        htm.addln("<select style='width:188px;' name='seat' id='seat'>");
        htm.addln(" <optgroup label='Quiet Testing'>");
        for (final RawClientPc station : allQuiet) {
            if (RawClientPc.STATUS_LOCKED.equals(station.currentStatus)) {
                final String id = station.testingCenterId + "_" + station.stationNbr;
                htm.addln("  <option value='", id, "'>Station ", station.stationNbr, "</option>");
            }
        }
        htm.addln(" </optgroup>");
        htm.addln(" <optgroup label='Testing Center'>");
        for (final RawClientPc station : allMain) {
            if (RawClientPc.STATUS_LOCKED.equals(station.currentStatus)) {
                final String id = station.testingCenterId + "_" + station.stationNbr;
                htm.addln("  <option value='", id, "'>Station ", station.stationNbr, "</option>");
            }
        }
        htm.addln(" </optgroup>");
        htm.addln("</select>");

        htm.eTd().eTr().eTable();
    }

    /**
     * Emits an informational message.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param message the message
     */
    private static void emitInfo(final HtmlBuilder htm, final String message) {

        htm.sP("info").add(message).eP();
    }

    /**
     * Emits an error message.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param message the message
     */
    private static void emitError(final HtmlBuilder htm, final String message) {

        htm.sP("error").add(message).eP();
    }

    /**
     * Emits the form submit button with a specified label.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param label the button label
     */
    private static void emitSubmit(final HtmlBuilder htm, final String label) {

        htm.div("vgap");
        htm.addln("<button class='btn' type='submit'>", label, "</button>");
    }

    /**
     * Emits the form submit button with a specified label.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitContinue(final HtmlBuilder htm) {

        htm.div("vgap");
        htm.addln("Do you wish to continue?");
        htm.addln("<button class='btn' name='ignorehold' value='x' type='submit'>Yes</button>");
        htm.addln("<button class='btn' name='cancel' value='x' type='submit'>No</button>");
    }

    /**
     * Emits the form submit button with a specified label.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitClose(final HtmlBuilder htm) {

        htm.div("vgap");
        htm.addln("<a class='btn' href='testing.html'>Close</a>");
    }
}
