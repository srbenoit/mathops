package dev.mathops.web.site.tutorial.precalc;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates a page with instructions for accessing online help.
 */
enum PageOnlineHelp {
    ;

    /**
     * Generates a page with information on orientation.
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
    static void doOnlineHelpPage(final Cache cache, final PrecalcTutorialSite site,
                                 final ServletRequest req, final HttpServletResponse resp,
                                 final ImmutableSessionInfo session, final PrecalcTutorialSiteLogic logic)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Precalculus Tutorial",
                "/precalc-tutorial/home.html", Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

        htm.sDiv("menupanel");
        TutorialMenu.buildMenu(session, logic, htm);
        htm.sDiv("panel");

        addHelpContent(cache, htm);
        addOnlineHelpContent(htm);

        htm.eDiv(); // (end "panel" div)
        htm.eDiv(); // (end "menupanel" div)

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates page content.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void addHelpContent(final Cache cache, final HtmlBuilder htm)
            throws SQLException {

        htm.sH(2).add("Getting Help").eH(2);

        htm.sDiv("indent11");

        htm.hr().div("vgap");
        htm.sH(3).add("In-Person Help").eH(3);
        htm.div("vgap0");

        htm.sP().add("The <b>Precalculus Center</b> has a staff of trained learning assistants who can provide help ",
                "with the math content of the ELM Tutorial in our Learning Center, Weber 136.").eP();

        AbstractPageSite.helpHours(cache, htm);

        htm.eDiv(); // indent11
    }

    /**
     * Generates page content.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void addOnlineHelpContent(final HtmlBuilder htm) {

        // htm.hr();
        // htm.div("vgap");
        //
        // htm.sH(3).add("Online Help").eH(3);
        //
        // htm.sDiv("indent22");
        //
        // htm.sP().add("<em>Online Help Hours via an open Microsoft Teams meeting</em>").eP();
        // htm.addln("<ul class='hours'>");
        // htm.addln("<li>January 17 through May 5, 2023<br>");
        // htm.addln("<table>");
        // htm.addln(" <tr><td>Monday</td> ",
        // "<td>&nbsp; Noon - 2:00 pm &nbsp; and &nbsp; 5:00pm - 8:00 pm</td></tr>");
        // htm.addln(" <tr><td>Tuesday</td> ", //
        // "<td>&nbsp; 1:00 pm - 7:00 pm</td></tr>");
        // htm.addln(" <tr><td>Wednesday</td>",
        // "<td>&nbsp; Noon - 2:00 pm &nbsp; and &nbsp; 5:00pm - 7:00 pm</td></tr>");
        // htm.addln(" <tr><td>Thursday</td> ", //
        // "<td>&nbsp; 4:00 pm - 5:00 pm &nbsp;</td></tr>");
        // htm.addln(" <tr><td>Friday</td> ",
        // "<td>&nbsp; 10:00 am - 11:00 am &nbsp; and &nbsp; Noon - 2:00 pm</td></tr>");
        // htm.addln(" </table>");
        // htm.addln("</ul>");
        //
        // htm.addln("<a style='font-size: 18px;text-decoration: underline;color: #6264a7;' ",
        // "href='https://teams.microsoft.com/l/meetup-join/19%3ameeting_NWZmZGQ4OTctZmJhMC00NmQ1LWFjMjUtOWZiNTk1ZjdmODhi%40thread.v2/0?context=%7b%22Tid%22%3a%22afb58802-ff7a-4bb1-ab21-367ff2ecfc8b%22%2c%22Oid%22%3a%22ebcdd035-78e6-465a-9538-ad4f39aefb65%22%7d'
        // target='_blank' rel='noreferrer noopener'>",
        // "Join Microsoft Teams Meeting</a>");
        //
        // htm.sDiv();
        // htm.addln("<a style='font-size: 14px;text-decoration: none;color: #6264a7;' ",
        // "href='tel:+1 970-628-0547,,422386473#' target='_blank' rel='noreferrer noopener'>",
        // "+1 970-628-0547</a>");
        // htm.addln(" <span style='font-size: 12px;'>United States, Grand Junction (Toll)
        // </span>");
        // htm.eDiv();
        //
        // htm.sDiv(null, "style='margin-top: 10px; margin-bottom: 20px;'");
        // htm.addln("<span style='font-size: 12px;'>Conference ID:</span>",
        // "<span style='font-size: 14px;'>422 386 473# </span>");
        // htm.eDiv();
        //
        // htm.addln(
        // "<a style='font-size: 12px;text-decoration: none;color: #6264a7;' target='_blank'",
        // "
        // href='https://dialin.teams.microsoft.com/0d8aa74a-565a-4429-8b5f-890a95fc1044?id=422386473'",
        // " rel='noreferrer noopener'>Local numbers</a> |");
        // htm.addln(
        // "<a style='font-size: 12px;text-decoration: none;color: #6264a7;' target='_blank' ",
        // "href='https://mysettings.lync.com/pstnconferencing' rel='noreferrer noopener'>",
        // "Reset PIN</a> | ",
        // "<a style='font-size: 12px;text-decoration: none;color: #6264a7;' target='_blank' ",
        // "href='https://aka.ms/JoinTeamsMeeting' rel='noreferrer noopener'>",
        // "Learn more about Teams</a> | ",
        // "<a style='font-size: 12px;text-decoration: none;color: #6264a7;' target='_blank' ",
        // "href='https://teams.microsoft.com/meetingOptions/?organizerId=ebcdd035-78e6-465a-9538-ad4f39aefb65&amp;tenantId=afb58802-ff7a-4bb1-ab21-367ff2ecfc8b&amp;threadId=19_meeting_NWZmZGQ4OTctZmJhMC00NmQ1LWFjMjUtOWZiNTk1ZjdmODhi@thread.v2&amp;messageId=0&amp;language=en-US'
        // rel='noreferrer noopener'>",
        // "Meeting options</a>");
        //
        // htm.eDiv(); // indent22
        //
        // htm.div("vgap");
    }
}
