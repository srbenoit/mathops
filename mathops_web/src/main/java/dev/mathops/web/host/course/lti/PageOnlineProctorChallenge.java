package dev.mathops.web.host.course.lti;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.challenge.ChallengeExamLogic;
import dev.mathops.db.logic.challenge.ChallengeExamStatus;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.challengeexam.ChallengeExamSessionStore;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Generates a page needed to proctor a challenge exam via Zoom.
 */
enum PageOnlineProctorChallenge {
    ;

    /**
     * Generates the page.
     *
     * @param req          the request
     * @param resp         the response
     * @param studentError error message to show under student portion of form
     * @param proctorError error message to show under proctor portion of form
     * @throws IOException if there is an error writing the response
     */
    static void showPage(final ServletRequest req, final HttpServletResponse resp,
                         final String studentError, final String proctorError) throws IOException {

        final String access = req.getParameter("access");
        final String exam = req.getParameter("exam");

        if (AbstractSite.isParamInvalid(access) || AbstractSite.isParamInvalid(exam)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  access='", access, "'");
            Log.warning("  exam='", exam, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {

            final Enumeration<String> e1 = req.getParameterNames();
            while (e1.hasMoreElements()) {
                final String name = e1.nextElement();
                Log.fine("Parameter '", name, "' = '", req.getParameter(name), "'");
            }

            final HtmlBuilder htm = new HtmlBuilder(2000);

            Page.startPage(htm, "Online Proctored Challenge Exam", false, false);

            htm.addln("<body style='background:white;padding:15px'>");

            htm.sH(1).add("Online Proctored Exam").eH(1);

            htm.sP().add("This page allows a CSU proctor to supervise a proctored challenge exam ",
                    "using Microsoft Teams.").eP();

            htm.sP().add("This page should be accessed after connecting to your proctor on Microsoft ",
                    "Teams and sharing your desktop.  Your proctor will need to enter information ",
                    "on this page by taking control of your computer for a moment.").eP();

            htm.hr();

            htm.addln("<form action='beginproctorchallenge.html' method='post'>");

            htm.sH(2).add("Proctor Instructions:").eH(2);

            htm.sDiv("indent");

            htm.sP().add("Enter the student ID:").eP();

            if (access == null) {
                htm.addln("&nbsp; <input type='text' id='access' name='access' data-lpignore='true' size='20'/>");
            } else {
                htm.addln("&nbsp; <input type='text' id='access' name='access' data-lpignore='true' size='20' value='",
                        access, "'/>");
            }

            if (studentError != null) {
                htm.sP(null, "style='color:red;'").add(studentError).eP();
            }

            htm.sP().add("Select the exam being taken:").eP();
            htm.addln("&nbsp; <select name='exam'>");
            htm.addln(mkOption(ChallengeExamLogic.M117_CHALLENGE_EXAM_ID, "MATH 117 - Challenge Exam", exam));
            htm.addln(mkOption(ChallengeExamLogic.M118_CHALLENGE_EXAM_ID, "MATH 118 - Challenge Exam", exam));
            htm.addln(mkOption(ChallengeExamLogic.M124_CHALLENGE_EXAM_ID, "MATH 124 - Challenge Exam", exam));
            htm.addln(mkOption(ChallengeExamLogic.M125_CHALLENGE_EXAM_ID, "MATH 125 - Challenge Exam", exam));
            htm.addln(mkOption(ChallengeExamLogic.M126_CHALLENGE_EXAM_ID, "MATH 126 - Challenge Exam", exam));

            htm.addln("</select>");

            htm.sP().add("Enter your one-time proctor password for this exam. This password is ",
                    "only valid for one use, but if the student's browser tries to save the password, ",
                    "say 'No'.").eP();

            htm.addln("&nbsp; <input type='password' id='pwd' name='pwd' ",
                    "data-lpignore='true' size='20'/>");

            if (proctorError != null) {
                htm.sP(null, "style='color:red;'").add(proctorError).eP();
            }

            htm.div("vgap");
            htm.eDiv();

            htm.sP().add("<input type='submit' value='Start the Exam'/>").eP();

            htm.addln("</form>");

            htm.addln("</body>");
            htm.addln("</html>");

            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Creates an "option" element, selected if the value matches the exam parameter.
     *
     * @param value the value
     * @param name  the exam name
     * @param exam  the exam parameter
     * @return the option HTML
     */
    private static String mkOption(final String value, final String name, final String exam) {

        final HtmlBuilder result = new HtmlBuilder(50);

        if (exam != null && exam.equals(value)) {
            result.add("&nbsp; <option value='", value, "' selected>", name, "</option>");
        } else {
            result.add("&nbsp; <option value='", value, "'>", name, "</option>");
        }

        return result.toString();
    }

    /**
     * Processes a form POST to start a proctored exam.
     *
     * @param cache the data cache
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void processBeginProctor(final Cache cache, final ServletRequest req,
                                    final HttpServletResponse resp) throws IOException, SQLException {

        String stuId = req.getParameter("access");
        final String exam = req.getParameter("exam");
        final String pwd = req.getParameter("pwd");

        if (AbstractSite.isParamInvalid(stuId) || AbstractSite.isParamInvalid(exam)
                || AbstractSite.isParamInvalid(pwd)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  access='", stuId, "'");
            Log.warning("  exam='", exam, "'");
            Log.warning("  pwd='", pwd, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            String studentError = null;
            String proctorError = null;
            boolean ok = true;

            String course = null;
            String actualExamId = null;

            if (stuId == null || stuId.isEmpty()) {
                studentError = "Missing student ID.";
                ok = false;
            } else {
                stuId = stuId.trim().replace(CoreConstants.DASH, CoreConstants.EMPTY)
                        .replace(CoreConstants.SPC, CoreConstants.EMPTY);
            }

            if (exam == null || exam.isEmpty()) {
                proctorError = "Please select an exam.";
                ok = false;
            } else {
                switch (exam) {
                    case ChallengeExamLogic.M117_CHALLENGE_EXAM_ID -> course = RawRecordConstants.M117;
                    case ChallengeExamLogic.M118_CHALLENGE_EXAM_ID -> course = RawRecordConstants.M118;
                    case ChallengeExamLogic.M124_CHALLENGE_EXAM_ID -> course = RawRecordConstants.M124;
                    case ChallengeExamLogic.M125_CHALLENGE_EXAM_ID -> course = RawRecordConstants.M125;
                    case ChallengeExamLogic.M126_CHALLENGE_EXAM_ID -> course = RawRecordConstants.M126;
                    default -> {
                        proctorError = "Invalid exam configuration - unable to start exam.";
                        ok = false;
                    }
                }
                if (pwd == null || pwd.isEmpty()) {
                    proctorError = "Missing one-time proctor password.";
                    ok = false;
                } else if (ChallengeExamSessionStore.getInstance()
                        .validateOneTimeChallengeCodeNoDelete(stuId, pwd)) {

                    if (course != null) {
                        // Test eligibility for selected exam
                        final ChallengeExamLogic logic = new ChallengeExamLogic(cache, stuId);
                        final ChallengeExamStatus status = logic.getStatus(course);

                        actualExamId = status.availableExamId;
                        if (actualExamId == null) {
                            proctorError = "Student not eligible: " + status.reasonUnavailable;
                            ok = false;
                        }
                    }
                } else {
                    proctorError = "Invalid one-time proctor password.";
                    ok = false;
                }
            }

            if (ok) {
                resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
                final String redirect = "challenge.html?exam=" + actualExamId +
                        "&stu=" + stuId + "&pwd=" + pwd;
                resp.setHeader("Location", redirect);
            } else {
                showPage(req, resp, studentError, proctorError);
            }
        }
    }
}
