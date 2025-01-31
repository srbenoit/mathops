package dev.mathops.web.site.admin.office;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.html.unitexam.UnitExamSessionStore;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * A page to display the process for proctoring via Microsoft Teams.
 */
enum PageProctoringTeams {
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

        final String code = req.getParameter("code");
        if (AbstractSite.isParamInvalid(code)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  code='", code, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = OfficePage.startOfficePage(cache, site, session, true);

            htm.sH(3).add("Online Proctoring: Course Exams, ELM Exam, Precalc Tutorial Exams").eH(3);

            doPageContent(htm, code);

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Appends page content to an {@code HtmlBuilder}.
     *
     * @param htm  the {@code HtmlBuilder} to which to write
     * @param code the exam code, if any
     */
    private static void doPageContent(final HtmlBuilder htm, final String code) {

        htm.addln("<ul>");
        htm.addln("<li>Coordinate with the student to arrange the exam time - consider any time ",
                "extension the student has.</li>");
        htm.addln("<li>Ask the student to have Teams running when it is time for the exam. ",
                "<a href='https://www.acns.colostate.edu/Microsoft-Teams-Students/' target='_blank' ",
                "style='text-decoration:underline'>Information for students on Teams</a></li>");
        htm.addln("<li>At exam time, open <b>Teams</b>, go into <b>Calls</b> on the left-hand ",
                "menu, and click <b>Make a call</b> at the bottom, and choose <b>Video Call</b>.</li>");
        htm.addln("<li>Once the student answers the call, have them turn on the webcam, and do ",
                "what you can to verify their identity (have them show you a photo ID).</li>");
        htm.addln("<li>Ask the student to use their webcam to show you their desktop, walls ",
                "behind the screen (note whether they have multiple monitors).</li>");
        htm.addln("<li>Ask the student to share their desktop (not just a window).  If they have ",
                "two screens on their computer, see if they can share both.</li>");
        htm.addln("<li>Ask the student to log into our web site (Precalc, ELM, etc.), and ",
                "navigate to the exam they want to take (with the link to Canvas)</li>");
        htm.addln("<li>Once there, type the student ID below to generate the proctor password ",
                "for this exam (this code changes with each login session).</li>");
        htm.addln("<li>Explain to the student that you need to type in the proctor password and ",
                "go to the exam itself, and then use <b>Request Control</b> on the Teams control bar ",
                "to request control of the student's screen.</li>");
        htm.addln("<li>Paste this into the student's browser address bar:<br> ",
                "&nbsp; <code style='color:red;'>",
                "https://course.math.colostate.edu/lti/onlineproctor.html</code></li>");

        htm.addln("<ul>");
        htm.addln("<li>Type or paste in the student ID.</li>");
        htm.addln("<li>Choose the exam.</li>");
        htm.addln("<li>Enter the proctor password generated below.</li>");
        htm.addln("</ul>");

        htm.addln("<li>The exam should begin - <b>give control back</b> to the student, and ",
                "<b>mute</b> your mic/video during their exam.</li>");
        htm.addln("</ul>");

        htm.sP().add("<b>Generating proctor password:</b>").eP();

        htm.addln("<a id='form'><form action='proctoring_teams.html#form' method='post'>");
        htm.add("Student ID: ");
        if (code == null || code.isEmpty()) {
            htm.addln("<input type='test' name='code'/>");
        } else {
            htm.addln("<input type='test' name='code' value='",
                    code, "'/>");

            ImmutableSessionInfo session = null;

            final String trimmed = code.trim().replace(CoreConstants.DASH, CoreConstants.EMPTY)
                    .replace(CoreConstants.SPC, CoreConstants.EMPTY);
            final String sid = UnitExamSessionStore.getInstance().lookupStudent(trimmed);
            if (sid != null) {
                session = SessionManager.getInstance().getUserSession(sid);
            }

            if (session == null) {
                htm.br().addln("<span style='color:red;'>Student ID has no exam code - ",
                        "make sure student is logged in and looking at their course outline.</span>");
            } else {
                final String pwd = session.loginSessionId.substring(3, 13);
                htm.br().addln("<span style='color:blue;'>Proctor password is ", pwd, "</span>");
            }
        }
        htm.sP().add("<input type='submit' value='Compute Password'/>").eP();
        htm.addln("</form>");
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
}
