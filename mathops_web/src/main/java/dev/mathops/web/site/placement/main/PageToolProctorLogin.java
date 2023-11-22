package dev.mathops.web.site.placement.main;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rawlogic.RawStudentLogic;
import dev.mathops.db.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates the content of a page with proctor login for the placement exam.
 */
enum PageToolProctorLogin {
    ;

    /**
     * Displays the login for a proctor to enter a password to launch a proctored exam.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException if there is an error writing the response
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session) throws IOException {

        final String error = req.getParameter("error");

        if (AbstractSite.isParamInvalid(error)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  error='", error, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = MPPage.startPage3(site, session);

            htm.sDiv("inset2");
            htm.sDiv("shaded2left");

            htm.sP();
            htm.addln("The ProctorU proctor must enter a password here to begin completing the Math Placement Tool.");
            htm.eP();

            htm.div("vgap");

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
                        legalName, CoreConstants.SPC, session.getEffectiveLastName()).eH(4);
            }
            htm.eDiv();

            htm.div("vgap2");

            htm.addln("<fieldset>");
            htm.addln("<legend>Instructions for Proctor:</legend>");

            htm.sP();
            htm.addln("If student identity cannot be verified using the normal procedure, ",
                    "please allow student to present a single acceptable photo ID, but file an ",
                    "incident report to indicate this was the procedure used for this exam attempt.");
            htm.eP();

            htm.sP();
            htm.addln("Students are allowed to use their personal graphing calculators while ",
                    "completing the Math Placement Tool. In particular, TI-83 and TI-84 are allowed.");
            htm.eP();

            htm.div("vgap");

            final String randomId = CoreConstants.newId(10);

            htm.sDiv("center");

            htm.addln("<form style='display:inline' id='search' ",
                    "class='authenticate_form' name='authenticate_form' method='post' ",
                    "action='tool_process_proctor_login_pu.html' autocomplete='off'>");

            htm.addln("<strong>Proctor, please enter password:</strong>");
            htm.addln("<div style='padding-top:6pt;'>");
            htm.addln("<input type='password' id='", randomId,
                    "' name='drowssap' data-lpignore='true' autocomplete='new-password'/>");
            htm.eDiv();

            htm.div("vgap");

            htm.addln("<input type='submit' class='btn' id='submit_image' value='Continue'/>");
            htm.addln("</form>");

            htm.eDiv(); // center

            if (error != null) {
                htm.div("vgap");
                htm.sDiv("center");
                htm.addln("<strong class='red2'>", error, "</strong>");
                htm.eDiv();
            }

            htm.div("vgap0");

            htm.addln("</fieldset>");

            htm.eDiv(); // shaded2left
            htm.eDiv(); // inset2

            MPPage.emitScripts(htm);
            MPPage.endPage(htm, req, resp);
        }
    }
}
