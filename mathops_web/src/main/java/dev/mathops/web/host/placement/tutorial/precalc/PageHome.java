package dev.mathops.web.host.placement.tutorial.precalc;

import dev.mathops.db.Cache;
import dev.mathops.db.schema.legacy.impl.RawAdminHoldLogic;
import dev.mathops.db.schema.legacy.rec.RawAdminHold;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Generates the content of the home page for a course site.
 */
enum PageHome {
    ;

    /**
     * Generates the page with contact information.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final PrecalcTutorialSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session,
                             final PrecalcTutorialSiteLogic logic)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Precalculus Tutorial",
                "/precalc-tutorial/home.html", Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

        htm.sDiv("menupanel");
        TutorialMenu.buildMenu(session, logic, htm);
        htm.sDiv("panel");

        doPage(cache, session, htm, logic);

        htm.eDiv(); // (end "panel" div)
        htm.eDiv(); // (end "menupanel" div)

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Creates the home page HTML.
     *
     * @param cache   the data cache
     * @param session the user's login session information
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @param logic   the course site logic
     * @throws SQLException if there is an error accessing the database
     */
    private static void doPage(final Cache cache, final ImmutableSessionInfo session, final HtmlBuilder htm,
                               final PrecalcTutorialSiteLogic logic) throws SQLException {

        PageLogin.emitIntroText(htm);

        showHolds(htm, logic);

        if (!logic.hasTutorAccess()) {
            emitEligibility(session, htm, logic);
        }

        htm.div("vgap").hr();
        htm.sP("indent11").add("To request accommodations related to a disability for access to the Precalculus ",
                "Tutorial, please contact <a class='ulink' href='https://disabilitycenter.colostate.edu/'>",
                "The Student Disability Center</a>.").eP();
        htm.hr().div("vgap0");

        htm.div("vgap");
        htm.sH(4).add("The Precalculus Center").eH(4);

        htm.sP("indent22");
        htm.addln("The Precalculus Center office is located in Weber 137. ",
                "You can contact the office staff at (970) 491-5761.");
        htm.eP();

        htm.sP("indent22");
        htm.addln(" The Precalculus Center can also answer questions online. ",
                "<a class='ulink' href='onlinehelp.html'>Tell me how to access online help.</a>");
        htm.eP();

        htm.div("vgap");
        AbstractPageSite.hours(cache, htm, true, true);
        htm.div("vgap2");

        maintWindow(htm);
    }

    /**
     * Displays any holds on the student's account.
     *
     * @param htm   the {@code HtmlBuilder} to which to append the HTML
     * @param logic the course site logic
     */
    private static void showHolds(final HtmlBuilder htm, final PrecalcTutorialSiteLogic logic) {

        if (!logic.hasTutorAccess()) {
            // Display all holds on the student's account that aren't hold 30
            final List<RawAdminHold> stuHolds = logic.getStudentHolds();
            boolean hasNon30 = false;
            for (final RawAdminHold hold : stuHolds) {
                if (!"30".equals(hold.holdId)) {
                    hasNon30 = true;
                    break;
                }
            }

            if (hasNon30) {
                htm.div("vgap");
                htm.sDiv("advice");
                htm.addln(" <strong>There is a hold on your record.</strong>");

                for (final RawAdminHold hold : stuHolds) {
                    if ("30".equals(hold.holdId)) {
                        continue;
                    }

                    final String msg = RawAdminHoldLogic.getStudentMessage(hold.holdId);
                    if (msg != null) {
                        htm.div("vgap");
                        htm.addln(" &nbsp;<span class='red'><b>", msg, "</b></span>");
                    }

                    if ("F".equals(hold.sevAdminHold)) {
                        htm.div("vgap");
                        htm.addln("You will be unable to access Assignments and Exams until this hold is resolved.");
                        htm.div("vgap0");
                        htm.addln("You must come to the Precalculus Center, Weber 137, or e-mail the Precalculus " +
                                        "Center at ",
                                "<a class='ulink2' href='mailto:precalc_math@colostate.edu'>",
                                "precalc_math@colostate.edu</a> to resolve this hold.");
                    }
                }

                htm.eDiv(); // advice
            }
        }
    }

    /**
     * Emits messages relating to the user's eligibility.
     *
     * @param session the user's login session information
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param logic   the course site logic
     */
    private static void emitEligibility(final ImmutableSessionInfo session, final HtmlBuilder htm,
                                        final PrecalcTutorialSiteLogic logic) {

        final LocalDate today = session.getNow().toLocalDate();

        // The following tests whether the student has completed the Math Placement Tool and is currently eligible
        // based on application term - it does not check whether the student has already placed out of all courses.
        if (logic.isEligible(today)) {
            if (logic.getEligibleCourse() == null) {
                htm.div("vgap");
                htm.sDiv("advice");
                htm.sDiv("red").add("<b>You are not eligible for the Precalculus Tutorial.</b>").eDiv();
                htm.div("vgap0");
                htm.sDiv().add("You have already placed out of all five courses covered by the tutorial.").eDiv();
                htm.div("vgap0");
                htm.sDiv().add("If you have any questions, please send an email to ",
                        "<a class='ulink2' href='mailto:precalc_math@colostate.edu'>",
                        "precalc_math@colostate.edu</a>.").eDiv();
                htm.eDiv(); // advice
                htm.div("vgap");
            }
        } else {
            htm.div("vgap");
            htm.sDiv("advice");
            htm.sDiv("red").add("<b>You are not eligible for the Precalculus Tutorial.</b>").eDiv();
            htm.div("vgap0");
            htm.sDiv().add("This tutorial is available to <strong>incoming</strong> students, before their first ",
                    "semester of courses, who have completed the Math Placement Tool.").eDiv();
            htm.div("vgap0");
            htm.sDiv().add("If you have any questions, please send an email to <a class='ulink2' ",
                    "href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu</a>.").eDiv();
            htm.eDiv(); // advice
            htm.div("vgap");
        }
    }

    /**
     * Appends the HTML display of the system maintenance window to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void maintWindow(final HtmlBuilder htm) {

        htm.sDiv("advice");
        htm.sH(4).add("Scheduled System Maintenance:").eH(4);

        htm.sP("indent11");
        htm.addln(" A maintenance window is reserved from");
        htm.addln(" 6:00am&nbsp;-&nbsp;8:00am daily. If you use the system during this time,");
        htm.addln(" you do so at your own risk.  The system may be taken offline without");
        htm.addln(" warning during this window to perform system maintenance.");
        htm.eP();

        htm.eDiv();
    }
}
