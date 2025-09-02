package dev.mathops.web.host.testing.adminsys.proctor;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.schema.legacy.rec.RawWhichDb;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminPage;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import dev.mathops.web.site.html.challengeexam.ChallengeExamSessionStore;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A page to display the process for proctoring via Microsoft Teams.
 */
enum PageProctoringChallengeTeams {
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

        final String stuId = req.getParameter("stuId");

        if (AbstractSite.isParamInvalid(stuId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  stuId='", stuId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final RawWhichDb whichDb = cache.getSystemData().getWhichDb();

            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();
            Page.startOrdinaryPage(htm, siteTitle, null, false, null, "home.html",
                    Page.NO_BARS, null, false, true);
            AdminPage.emitPageHeader(htm, session, whichDb, true);

            htm.sH(3).add("Online Proctoring: Challenge Exams").eH(3);

            doPageContent(req, htm, stuId);

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Appends page content to an {@code HtmlBuilder}.
     *
     * @param req   the request
     * @param htm   the {@code HtmlBuilder} to which to write
     * @param stuId the student ID
     */
    private static void doPageContent(final ServletRequest req, final HtmlBuilder htm,
                                      final String stuId) {

        final boolean isDev = req.getServerName().contains("dev.");

        htm.addln("<ul>");
        htm.addln("<li>Coordinate with the student to arrange the exam time - consider any time ",
                "extension the student has (Challenge exams are 75 minutes long, before any ",
                "adjustments due to accommodations).</li>");
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
        htm.addln("<li>Type the student ID below to generate the proctor password for this exam ",
                "(this code is valid for one-time use only).</li>");
        htm.addln("<li>Explain to the student that you need to type in the proctor password and ",
                "go to the exam itself, and then use <b>Request Control</b> on the Teams control bar ",
                "to request control of the student's screen.</li>");
        htm.addln("<li>Paste this into the student's browser address bar:<br> ",
                "&nbsp; <code style='color:red;'>",
                (isDev ? "https://coursedev.math.colostate.edu/lti/onlineproctorchallenge.html"
                        : "https://course.math.colostate.edu/lti/onlineproctorchallenge.html"),
                "</code></li>");

        htm.addln("<ul>");
        htm.addln("<li>Type or paste in the student ID.</li>");
        htm.addln("<li>Choose the exam.</li>");
        htm.addln("<li>Enter the one-time password generated below.</li>");
        htm.addln("</ul>");

        htm.addln("<li>The exam should begin - <b>give control back</b> to the student, and ",
                "<b>mute</b> your mic/video during their exam.</li>");
        htm.addln("</ul>");

        htm.sP().add("<b>Generating one-time proctor password:</b>").eP();

        htm.addln("<a id='form'><form action='proctoring_challenge_teams.html#form' method='post'>");
        htm.add("Student ID: ");
        if (stuId == null || stuId.isEmpty()) {
            htm.addln("<input type='test' data-lpignore='true' name='stuId'/>");
            htm.sP().add("<input type='submit' value='Compute Password'/>").eP();
        } else {
            htm.addln("<input type='test' data-lpignore='true' name='stuId' value='", stuId, "'/>");

            final String trimmed = stuId.trim().replace(CoreConstants.DASH, CoreConstants.EMPTY)
                    .replace(CoreConstants.SPC, CoreConstants.EMPTY);

            if (trimmed.length() == 9 && trimmed.charAt(0) == '8') {
                try {
                    Integer.parseInt(trimmed);

                    final String code = ChallengeExamSessionStore.getInstance().createOneTimeChallengeCode(trimmed);
                    htm.br().addln("<span style='color:blue;'>Proctor password is ", code, "</span>");

                } catch (final NumberFormatException ex) {
                    htm.br().addln("<span style='color:red;'>Invalid Student ID</span>");
                }
            } else {
                htm.br().addln("<span style='color:red;'>Invalid Student ID</span>");
            }
        }
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
