package dev.mathops.web.site.lti;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.unitexam.UnitExamSessionStore;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * Generates a page needed to proctor an exam via Zoom.
 */
enum PageOnlineProctor {
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

            Page.startPage(htm, "Online Proctored Exam", false, false);

            htm.addln("<body style='background:white;padding:15px'>");

            htm.sH(1).add("Online Proctored Exam").eH(1);

            htm.sP().add("This page allows a CSU proctor to supervise a proctored exam using ",
                    "Microsoft Teams.").eP();

            htm.sP()
                    .add("This page should be accessed after connecting to your proctor on Microsoft ",
                            "Teams and sharing your desktop.  Your proctor will need to enter information ",
                            "on this page by taking control of your computer for a moment.")
                    .eP();

            htm.hr();

            htm.addln("<form action='beginproctor.html' method='post'>");

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

            htm.addln(mkOption("171UE", "MATH 117 - Unit 1 Exam", exam));
            htm.addln(mkOption("172UE", "MATH 117 - Unit 2 Exam", exam));
            htm.addln(mkOption("173UE", "MATH 117 - Unit 3 Exam", exam));
            htm.addln(mkOption("174UE", "MATH 117 - Unit 4 Exam", exam));
            htm.addln(mkOption("17FIN", "MATH 117 - Final Exam", exam));

            htm.addln(mkOption("181UE", "MATH 118 - Unit 1 Exam", exam));
            htm.addln(mkOption("182UE", "MATH 118 - Unit 2 Exam", exam));
            htm.addln(mkOption("183UE", "MATH 118 - Unit 3 Exam", exam));
            htm.addln(mkOption("184UE", "MATH 118 - Unit 4 Exam", exam));
            htm.addln(mkOption("18FIN", "MATH 118 - Final Exam", exam));

            htm.addln(mkOption("241UE", "MATH 124 - Unit 1 Exam", exam));
            htm.addln(mkOption("242UE", "MATH 124 - Unit 2 Exam", exam));
            htm.addln(mkOption("243UE", "MATH 124 - Unit 3 Exam", exam));
            htm.addln(mkOption("244UE", "MATH 124 - Unit 4 Exam", exam));
            htm.addln(mkOption("24FIN", "MATH 124 - Final Exam", exam));

            htm.addln(mkOption("251UE", "MATH 125 - Unit 1 Exam", exam));
            htm.addln(mkOption("252UE", "MATH 125 - Unit 2 Exam", exam));
            htm.addln(mkOption("253UE", "MATH 125 - Unit 3 Exam", exam));
            htm.addln(mkOption("254UE", "MATH 125 - Unit 4 Exam", exam));
            htm.addln(mkOption("25FIN", "MATH 125 - Final Exam", exam));

            htm.addln(mkOption("261UE", "MATH 126 - Unit 1 Exam", exam));
            htm.addln(mkOption("262UE", "MATH 126 - Unit 2 Exam", exam));
            htm.addln(mkOption("263UE", "MATH 126 - Unit 3 Exam", exam));
            htm.addln(mkOption("264UE", "MATH 126 - Unit 4 Exam", exam));
            htm.addln(mkOption("26FIN", "MATH 126 - Final Exam", exam));

            htm.addln(mkOption("MT4UE", "ELM Exam", exam));

            htm.addln(mkOption("7T4UE", "Precalc Tutorial - Algebra I Exam", exam));
            htm.addln(mkOption("8T4UE", "Precalc Tutorial - Algebra II Exam", exam));
            htm.addln(mkOption("4T4UE", "Precalc Tutorial - Functions Exam", exam));
            htm.addln(mkOption("5T4UE", "Precalc Tutorial - Trig I Exam", exam));
            htm.addln(mkOption("6T4UE", "Precalc Tutorial - Trig II Exam", exam));

            htm.addln("</select>");

            htm.sP().add("Enter your proctor password. This password changes with each exam, ",
                    "but if the student's browser tries to save the password, say 'No'.").eP();

            htm.addln("&nbsp; <input type='password' id='pwd' name='pwd' data-lpignore='true' size='20'/>");

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
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void processBeginProctor(final ServletRequest req, final HttpServletResponse resp) throws IOException {

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
            ImmutableSessionInfo session = null;
            String studentError = null;
            String proctorError = null;
            String sid = null;

            if (stuId == null || stuId.isEmpty()) {
                studentError = "Missing student ID.";
            } else if (exam != null && exam.startsWith("MC1")) {
                // For challenge exams, student will not have an exam code

            } else {
                stuId = stuId.trim().replace(CoreConstants.DASH, CoreConstants.EMPTY)
                        .replace(CoreConstants.SPC, CoreConstants.EMPTY);
                sid = UnitExamSessionStore.getInstance().lookupStudent(stuId);
                if (sid != null) {
                    session = SessionManager.getInstance().getUserSession(sid);
                }

                if (session == null) {
                    studentError = "Student has no exam code; have them refresh their course outline page.";
                }
            }

            if (exam == null || exam.isEmpty()) {
                proctorError = "Please select an exam.";
            } else if (pwd == null || pwd.isEmpty()) {
                proctorError = "Missing proctor password.";
            } else if (session != null) {
                final String expect = session.loginSessionId.substring(3, 13);
                if (!expect.equals(pwd)) {
                    proctorError = "Invalid proctor password.";
                }
            }

            if (studentError == null && proctorError == null) {
                resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
                final String redirect = "home.html?exam=" + exam + "&sid=" + sid;
                resp.setHeader("Location", redirect);
            } else {
                showPage(req, resp, studentError, proctorError);
            }
        }
    }
}
