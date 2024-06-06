package dev.mathops.web.site.tutorial.elm;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates the content of the home page.
 */
enum PageProctorLoginElm {
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
    static void doGet(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String error = req.getParameter("error");

        if (AbstractSite.isParamInvalid(error)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  error='", error, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Entry Level Mathematics Tutorial",
                    "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

            doPage(cache, htm, session, error);

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                    htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Displays the login for a proctor to enter a password to launch a proctored exam.
     *
     * @param cache   the data cache
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the user's login session information
     * @param error   the error message
     */
    private static void doPage(final Cache cache, final HtmlBuilder htm,
                               final ImmutableSessionInfo session, final String error) {

        htm.sDiv("indent11");

        htm.sH(2).add("ELM Exam Administered by ProctorU").eH(2);

        htm.sDiv("center");

        String legalName = session.getEffectiveFirstName();

        final String userId = session.getEffectiveUserId();
        try {
            final RawStudent stu = RawStudentLogic.query(cache, userId, false);
            if (stu != null) {
                legalName = stu.firstName;
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to get student record", ex);
        }

        final String screenName = session.getEffectiveScreenName();

        if (screenName.startsWith(legalName)) {
            htm.sH(4, "green").add("Logged-in Student: ", session.getEffectiveScreenName()).eH(4);
        } else {
            htm.sH(4, "green").add("Logged-in Student: ", session.getEffectiveScreenName(), " - Legal name ",
                    legalName, " ", session.getEffectiveLastName()).eH(4);
        }
        htm.div("vgap");

        htm.sP().add("The ProctorU online proctor must enter a password here in order to ",
                "start the online <b>ELM Exam</b>").eP();
        htm.sP().add("(Invalid password attempts are logged)").eP();

        htm.eDiv(); // center

        htm.sDiv(null, "style='margin-left:60px; margin-right:60px;'");

        htm.addln("<fieldset>");
        htm.addln("<legend>Instructions for Proctor:</legend>");
        htm.addln(" If student identity cannot be verified using the normal procedure, please ",
                "allow student to present a single acceptable photo ID, but file an incident report ",
                "to indicate this was the procedure used for this exam attempt.");
        htm.div("vgap");
        htm.addln(" Students are allowed to use their personal graphing calculators on this Exam.  In particular, ",
                "TI-83 and TI-84 are allowed.");
        htm.div("vgap0");
        htm.addln("</fieldset>");

        final String randomId = CoreConstants.newId(10);

        htm.div("vgap");

        htm.sDiv("center");

        htm.sDiv("authenticate_form_locl");
        htm.sDiv("authenticate_form_div");

        htm.div("vgap0");
        htm.addln("<form id='search' method='post' name='authenticate_form' ",
                "action='process_proctor_login_elm.html' autocomplete='off'>");

        htm.addln("<strong>Proctor, please enter password:</strong>");
        htm.sDiv("local_login_div", "style='padding-top:4pt;'");

        htm.sDiv("password_label_div");
        htm.addln("<label class='password_label' for='", randomId, "'>Password:</label>");
        htm.eDiv(); // password_label_div

        htm.sDiv("password_input_div");
        htm.addln("<input type='password' data-lpignore='true' autocomplete='new-password' id='", randomId,
                "' name='drowssap'/>");
        htm.eDiv(); // password_input_div

        htm.eDiv(); // local_login_div

        htm.sDiv("authenticate_form_submit_div");
        htm.addln("<input type='submit' class='button' id='submit_image' value='Continue'/>");
        htm.eDiv();

        htm.addln("</form>");
        htm.div("vgap0");

        htm.eDiv(); // authenticate_form_div
        htm.eDiv(); // authenticate_form_locl

        htm.eDiv(); // center
        htm.eDiv(); // margin 60

        htm.addln(" <script>");
        htm.addln("   document.getElementById(\"", randomId,
                "\").focus();");
        htm.addln(" </script>");

        if (error != null) {
            htm.div("vgap");
            htm.sDiv("center");
            htm.sH(4, "red").add(error).eH(4);
            htm.eDiv();
        }

        htm.eDiv(); // indent11
    }
}
