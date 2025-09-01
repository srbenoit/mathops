package dev.mathops.web.host.placement.tutorial.precalc;

import dev.mathops.db.Cache;
import dev.mathops.db.schema.legacy.RawCampusCalendar;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Generates the content of the login page.
 */
enum PageLogin {
    ;

    /** A date/time formatter for printing LocalDates in simple text representations. */
    private static final DateTimeFormatter LOCAL_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US);

    /** A date/time formatter for printing LocalDates in simple text representations. */
    private static final DateTimeFormatter LOCAL_DATE_NO_YEAR =
            DateTimeFormatter.ofPattern("MMMM d", Locale.US);

    /**
     * Generates the login page.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final PrecalcTutorialSite site, final ServletRequest req,
                      final HttpServletResponse resp) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);

        Page.startOrdinaryPage(htm, "Precalculus Tutorial", null, true, "Precalculus Tutorial", "index.html",
                Page.ADMIN_BAR, null, false, true);

        emitIntroText(htm);

        htm.div("vgap");
        htm.sDiv("center");
        htm.addln("<a class='btn' href='secure/shibboleth.html'>Log in using your eID</a><br>");
        htm.addln("<a href='https://eid.colostate.edu/' target='_blank'>Create your eID</a>");
        htm.eDiv();

        final List<RawCampusCalendar> calendarRows = cache.getSystemData().getCampusCalendars();

        LocalDate window1Start = null;
        LocalDate window1End = null;
        LocalDate window2Start = null;
        LocalDate window2End = null;
        LocalDate window3Start = null;
        LocalDate window3End = null;

        for (final RawCampusCalendar cal : calendarRows) {
            final String type = cal.dtDesc;

            if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_START1.equals(type)) {
                window1Start = cal.campusDt;
            } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_END1.equals(type)) {
                window1End = cal.campusDt;
            } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_START2.equals(type)) {
                window2Start = cal.campusDt;
            } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_END2.equals(type)) {
                window2End = cal.campusDt;
            } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_START3.equals(type)) {
                window3Start = cal.campusDt;
            } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_END3.equals(type)) {
                window3End = cal.campusDt;
            }
        }

        htm.div("vgap2");
        htm.sDiv("infobox", "id='bottext'");

        final LocalDate yesterday = LocalDate.now().minusDays(1L);

        final boolean hasWindow1 = checkForWindow(window1Start, window1End, yesterday);
        final boolean hasWindow2 = checkForWindow(window2Start, window2End, yesterday);
        final boolean hasWindow3 = checkForWindow(window3Start, window3End, yesterday);
        final boolean showMaintenance = hasWindow1 || hasWindow2 || hasWindow3;

        if (showMaintenance) {
            htm.sP("center").add("<strong>Scheduled System Maintenance</strong>").eP();
            htm.sP().add("This site is closed briefly for maintenance at the end of each semester .").eP();

            htm.sP().addln("Upcoming maintenance periods:").eP();
            htm.addln("<ul>");
            if (hasWindow1) {
                htm.addln("<li>", window1Start.format(LOCAL_DATE_NO_YEAR), " - ",
                        window1End.format(LOCAL_DATE_FORMATTER), "</li>");
            }
            if (hasWindow2) {
                htm.addln("<li>", window2Start.format(LOCAL_DATE_NO_YEAR), " - ",
                        window2End.format(LOCAL_DATE_FORMATTER), "</li>");
            }
            if (hasWindow3) {
                htm.addln("<li>", window3Start.format(LOCAL_DATE_NO_YEAR), " - ",
                        window3End.format(LOCAL_DATE_FORMATTER), "</li>");
            }
            htm.addln("</ul>");

            htm.sP().add("Log in for the exact dates you will be eligible to access the Precalculus Tutorial.").eP();
        }

        htm.hr();
        htm.sP("center").add("<strong>Daily Maintenance Window</strong>").eP();
        htm.sP().add("A maintenance window is reserved from 6:00am - 8:00am daily. If you use the system during ",
                "this time, you do so at your own risk. The system may be taken offline without warning during ",
                "this window to perform system maintenance.").eP();
        htm.eDiv();
        htm.div("vgap2");

        Page.endOrdinaryPage(cache, site, null, htm, true);

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits introductory text.  This is presented here as well as in the home page.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitIntroText(final HtmlBuilder htm) {

        htm.div("vgap");
        htm.sDiv("center");
        htm.sH(1).add("Welcome to the Precalculus Tutorial").eH(1);
        htm.eDiv();

        htm.div("vgap");

        htm.sDiv(null, "style='padding:0 2em 0 2em;'");
        htm.sP().addln("The Precalculus Tutorial is available to <b>incoming</b> students before their first semester ",
                "of classes at CSU who have completed the Math Placement Tool.  It is intended to allow students to ",
                "satisfy prerequisites for courses they need in their first semester.  Successfully completing a ",
                "section of this tutorial is equivalent to placing out of the corresponding course on the Math ",
                "Placement Tool -- it satisfies prerequisites as if the course had been passed.").eP();
        htm.div("vgap0");
        htm.sP().addln("This Tutorial requires an e-text access code.  The CSU Bookstore will automatically activate ",
                "an access code for any student who works on this tutorial before their first semester at ",
                "CSU, and bill the student account $20 for that code when the following semester begins (you ",
                "will not need to purchase or enter a code yourself).").eP();
        htm.div("vgap0");
        htm.sP().addln("If you use this tutorial and then take any of the one-credit Precalculus courses in the ",
                "following semester, your access code will continue to work through that semester's courses ",
                "(there is no need to purchase a new code for that semester).").eP();
        htm.eDiv(); // padding
    }

    /**
     * Tests whether a maintenance window is still active or has not yet begun.  This is determined by testing whether
     * the end date is after yesterday - if so, {@code true} is returned.
     *
     * @param start     the start date of the window ({@code null} if window is not defined)
     * @param end       the end date of the window ({@code null} if window is not defined)
     * @param yesterday yesterday's date
     * @return {@code true} if the window is currently in progress or has not yet begun
     */
    private static boolean checkForWindow(final ChronoLocalDate start, final ChronoLocalDate end,
                                          final ChronoLocalDate yesterday) {

        return start != null && end != null && end.isAfter(yesterday);
    }
}
