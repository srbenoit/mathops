package dev.mathops.web.site.course;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates the content of the home page.
 */
enum PageProctorLogin {
    ;

    /**
     * Generates the home page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String course = req.getParameter("course");
        final String exam = req.getParameter("exam");
        final String error = req.getParameter("error");

        if (AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(exam)
                || AbstractSite.isParamInvalid(error)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            Log.warning("  exam='", exam, "'");
            Log.warning("  error='", error, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false,
                    Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            doPage(htm, session, course, exam, error);

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                    htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Displays the login for a proctor to enter a password to launch a proctored exam.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the user's login session information
     * @param course  the course ID
     * @param exam    the exam ID
     * @param error   the error message
     */
    private static void doPage(final HtmlBuilder htm, final ImmutableSessionInfo session,
                               final String course, final String exam, final String error) {

        final String title;
        if (exam.endsWith("4ME")) {
            title = "Final Exam";
        } else if (exam.endsWith("ME")) {
            title = "Midterm Exam";
        } else if (exam.endsWith("FE")) {
            title = "Final Exam";
        } else if (exam.endsWith("UE")) {
            title = "Unit Exam";
        } else {
            title = "Exam";
        }

        htm.sDiv("center");

        htm.sH(2).add("Proctored ", title).eH(2);

        htm.sH(4).add("Logged-in Student: ", session.getEffectiveScreenName()).eH(4).br();

        htm.sP().add(" The authorized proctor or the ProctorU online proctor must enter a ",
                "password here in order to access the online <b>", title, "</b>:").eP();
        htm.sP().add("(Invalid password attempts are logged)").eP();

        htm.eDiv(); // center

        htm.sDiv("indent44");
        htm.sDiv("indent44");
        htm.sDiv("boxed");
        htm.addln(" <strong>Proctors</strong>: Students are allowed to use a personal graphing ",
                "calculator for this exam.").br();
        htm.addln("In particular, the TI-83 and TI-84 are allowed.");
        htm.eDiv();
        htm.eDiv();
        htm.eDiv().br().br();

        final String randomId = CoreConstants.newId(10);

        htm.sDiv(null, "style='margin-left:60px;margin-right:60px;'");
        htm.sDiv("authenticate_form_locl");
        htm.sDiv("authenticate_form_div");
        htm.addln("<form id='search' class='authenticate_form' name='authenticate_form'");
        htm.addln("       action='process_proctor_login.html' method='post'");
        htm.addln("       autocomplete='off')>");
        htm.sDiv("local_login_div");
        htm.sDiv("password_label_div");
        htm.addln(" <label class='password_label' for='", randomId, "'>Password:</label>");
        htm.eDiv();
        htm.sDiv("password_input_div");
        htm.addln(" <input type='password' data-lpignore='true' autocomplete='new-password' id='", randomId,
                "' name='drowssap'/>");
        htm.eDiv();
        htm.eDiv();
        htm.sDiv("authenticate_form_submit_div");
        htm.addln(" <input type='submit' id='submit_image' value='Continue'/>");
        htm.eDiv();
        htm.addln(" <input type='hidden' name='course' id='course' value='", course, "'/>");
        htm.addln(" <input type='hidden' name='exam' id='exam' value='", exam, "'/>");
        htm.addln("   </form>");
        htm.eDiv();
        htm.eDiv();

        htm.addln(" <script>");
        htm.addln("   document.authenticate_form.password.focus();");
        htm.addln(" </script>");

        if (error != null) {
            htm.sDiv("center");
            htm.sH(4, "red").add(error).eH(4);
            htm.eDiv();
        }

        htm.sDiv("center");
        htm.addln("<a href='course.html?mode=course&course=", course,
                "'>After the exam click here to go to the course status page</a>");
        htm.eDiv();
    }
}
