package dev.mathops.web.host.placement.tutorial.elm;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.DateRange;
import dev.mathops.db.old.logic.DateRangeGroups;
import dev.mathops.db.old.logic.ELMTutorialStatus;
import dev.mathops.db.old.logic.PrerequisiteLogic;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
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
 * Generates the content of the home page.
 */
final class PageHome {

    /**
     * Private constructor to prevent instantiation.
     */
    private PageHome() {

        super();
    }

    /**
     * Generates the home page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param status  the student status with respect to the ELM Tutorial
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final ELMTutorialStatus status) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Entry Level Mathematics Tutorial",
                "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

        htm.sDiv("menupanel");
        TutorialMenu.buildMenu(cache, session, status, htm);
        htm.sDiv("panel");

        doPage(cache, htm, status);

        htm.eDiv(); // (end "panel" div)
        htm.eDiv(); // (end "menupanel" div)

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Creates the home page HTML.
     *
     * @param cache  the data cache
     * @param htm    the {@code HtmlBuilder} to which to append the HTML
     * @param status the student status with respect to the ELM Tutorial
     * @throws SQLException if there is an error accessing the database
     */
    private static void doPage(final Cache cache, final HtmlBuilder htm,
                               final ELMTutorialStatus status) throws SQLException {

        final String helpEmail = "precalc_math@colostate.edu";

        htm.sH(2, "center");
        final String name = status.student.getScreenName();
        htm.add("Welcome, ", name, ", to the").br().add("Entry Level Mathematics Tutorial");
        htm.eH(2);

        htm.sP("indent11");
        htm.addln("This tutorial is available to students who have completed the Math Placement Tool.  There is no ",
                "fee for using this tutorial.  Successful completion of specific portions of the ELM Tutorial can ",
                "satisfy the prerequisite for the entry-level mathematics courses:");
        htm.eP();

        htm.addln("<ul style='margin:3px 0 0 0'>");
        htm.addln(" <li style='margin-top:3px;'>MATH 117, College Algebra in Context I</li>");
        htm.addln(" <li style='margin-top:3px;'>MATH 120, College Algebra</li>");
        htm.addln(" <li style='margin-top:3px;'>MATH 127, Precalculus</li>");
        htm.addln("</ul>");

        htm.sP("indent11", "style='margin-top:0;'");
        htm.addln("Review the <a class='ulink' target='_blank' href='/www/media/ELM_information.pdf'>",
                "ELM Tutorial Instructions</a> for more detailed information.");
        htm.eP();

        final PrerequisiteLogic prereqLogic = new PrerequisiteLogic(cache, status.student.stuId);

        if (prereqLogic.hasSatisfiedPrerequisitesFor(RawRecordConstants.M117)) {

            htm.sDiv(null, "style='margin:8pt 30pt 10pt 30pt; padding:0 8pt 4pt 8pt; "
                    + "border:3px double #004f39; background-color:#ffffc6;'");

            htm.sP().add("You have already satisfied the prerequisite for MATH 117.  Completing ",
                    "the ELM Tutorial will not improve your placement results.  Of course, you may ",
                    "use the ELM tutorial as a review, but there is no need for you to take a ",
                    "proctored ELM exam.");

            htm.eDiv();
        }

        htm.sP("indent11");
        htm.addln("The tutorial <strong>cannot</strong> be used to place out of or earn credit ",
                "for any courses.  If you have already placed into MATH 117, the ELM Tutorial will ",
                "not change your placement results.");
        htm.eP();

        htm.div("vgap0").hr();
        htm.sP("indent11")
                .add("To request accommodations related to a disability for access to the ",
                        "ELM Tutorial, please contact ",
                        "<a class='ulink' href='https://disabilitycenter.colostate.edu/'>",
                        "The Student Disability Center</a>.")
                .eP();
        htm.hr().div("vgap0");

        // Display all holds on the student's account that aren't hold 30

        final List<RawAdminHold> stuHolds = status.holds.holds;
        for (final RawAdminHold hold : stuHolds) {
            if ("30".equals(hold.holdId)) {
                continue;
            }

            htm.div("vgap");
            htm.sDiv("indent22");
            htm.sDiv("errorbox");

            htm.addln("<strong>There is a hold on your record.</strong>");
            htm.div("vgap");

            final String msg = RawAdminHoldLogic.getStudentMessage(hold.holdId);
            if (msg != null) {
                htm.addln("&nbsp;<strong class='red'>",
                        msg, "</strong>");
                htm.div("vgap");
            }

            if ("F".equals(hold.sevAdminHold)) {
                htm.addln("<strong>",
                        "You will be unable to access tutorial exams until this hold is resolved. ",
                        "You must come to the Precalculus Center, Weber 137, or send an e-mail to ",
                        "<a class='ulink2' href='mailto:", helpEmail, "'>",
                        helpEmail, "</a> to resolve this hold.", //
                        "</strong>");
            }

            htm.eDiv(); // errorbox
            htm.eDiv(); // indent22

            htm.div("vgap3");
        }

        final DateRangeGroups siteRanges = status.webSiteAvailability;

        if (siteRanges.hasCurrentOrFuture()) {

            if (siteRanges.current == null) {

                htm.sDiv("advice");
                htm.addln("<span class='red'>",
                        "The ELM Tutorial is unavailable as we prepare for the upcoming semester.",
                        "</span>").br();
                htm.addln("The ELM Tutorial will become available once again on the following ",
                        "dates:").br();

                htm.addln("<ul style='margin-top:3px;margin-bottom:0;'>");

                for (final DateRange range : siteRanges.future) {
                    htm.addln("<li>", range, "</li>");
                }
                htm.addln("</ul>");
                htm.addln("These dates are based on the <strong>U.S. Mountain Time Zone</strong>.");

                htm.br().br();
                htm.addln("If you have any questions, please send an email to ",
                        "<a class='ulink2' href='mailto:precalc_math@colostate.edu'>",
                        "precalc_math@colostate.edu</a>.");
                htm.eDiv(); // advice
            } else {
                htm.sH(4).add("ELM Tutorial Availability:").eH(4);

                htm.sP("indent11");
                htm.addln(" The ELM Tutorial web site is available during the current ",
                        "semester on the following dates:");
                htm.eP();

                htm.sDiv("indent11");
                htm.addln("<ul style='margin-top:0;margin-bottom:0;padding-top:0;'>");
                htm.addln("<li>", siteRanges.current, "</li>");
                for (final DateRange range : siteRanges.future) {
                    htm.addln("<li>", range, "</li>");
                }
                htm.addln("</ul>");
                htm.eDiv(); // indent11

                final List<RawCampusCalendar> calendars = cache.getSystemData().getCampusCalendars();
                LocalDate start = null;
                LocalDate end = null;
                for (final RawCampusCalendar cal : calendars) {
                    if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_START1.equals(cal.dtDesc)) {
                        start = cal.campusDt;
                    } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_END1.equals(cal.dtDesc)) {
                        end = cal.campusDt;
                    }
                }

                if (start != null && end != null) {
                    final String closeStart = TemporalUtils.FMT_WMDY.format(start);
                    final String closeEnd = TemporalUtils.FMT_WMDY.format(end);

                    htm.sP("indent11", "style='padding-top:0;'");
                    htm.addln("<strong style='background-color:#FF9;'>");
                    htm.addln("The ELM Tutorial web site will be closed for maintenance:");
                    htm.addln("</strong>");
                    htm.sDiv("indent11");
                    htm.addln("<ul style='margin-top:3px;margin-bottom:0;padding-top:0;'>");
                    htm.addln("<li>", closeStart,
                            " &nbsp; through &nbsp; ", closeEnd,
                            "</li>");
                    htm.addln("</ul>");
                    htm.eDiv(); // indent11
                    htm.eP();
                }

                htm.sP("indent11", "style='padding:0;margin-top:4px;'");
                htm.addln("These dates are based on the <strong>U.S. Mountain Time Zone</strong>.");
                htm.eP();
            }
        } else {
            htm.sDiv("advice");
            htm.addln("<span class='red'>",
                    "The ELM Tutorial is no longer available.", //
                    "</span>").br().br();
            htm.addln("If you have any questions, please send an email to ",
                    "<a class='ulink2' href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu</a>.");
            htm.eDiv(); // advice
        }

        if (status.eligibleForElmTutorial) {
            htm.div("vgap2");

            htm.sH(3).add("The Precalculus Center: ").eH(3);

            htm.sP("indent22");
            htm.addln("The Precalculus Center office is located in Weber 137. ",
                    "You can contact the office staff at (970) 491-5761.");
            htm.eP();

            htm.sP("indent22");
            htm.addln("The Precalculus Center can answer questions online. ",
                    "<a class='ulink' href='onlinehelp.html'>Tell me how to access online help.</a>.");
            htm.eP();

            htm.div("vgap");
            AbstractPageSite.hours(cache, htm, true, true);
            htm.div("vgap2");

            maintWindow(htm);
        } else {
            htm.div("vgap3");
            htm.sDiv("indent22 note");
            htm.addln(" You are not currently eligible for the ELM Tutorial.  This tutorial is ",
                    "available to students who have completed the Math Placement Tool.</a>");
            htm.eDiv();
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
        htm.addln("A maintenance window is reserved from 6:00am&nbsp;-&nbsp;8:00am daily. ",
                "If you use the system during this time, you do so at your own risk.  The system may ",
                "be taken offline without warning during this window to perform system maintenance.");
        htm.eP();

        htm.eDiv(); // advice
    }
}
