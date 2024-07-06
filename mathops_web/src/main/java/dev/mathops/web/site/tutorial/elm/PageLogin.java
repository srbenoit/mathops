package dev.mathops.web.site.tutorial.elm;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawCampusCalendarLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Generates the content of the login page.
 */
enum PageLogin {
    ;

    /** A date/time formatter for printing LocalDates in simple text representations. */
    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy",
            Locale.US);

    /** A date/time formatter for printing LocalDates in simple text representations. */
    private static final DateTimeFormatter LOCAL_DATE_NO_YEAR = DateTimeFormatter.ofPattern("MMMM dd", Locale.US);

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
    static void doGet(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                      final HttpServletResponse resp)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);

        Page.startOrdinaryPage(htm, "Entry Level Mathematics Tutorial", null, true,
                "Entry Level Mathematics Tutorial", "index.html", Page.ADMIN_BAR, null, false, true);

        htm.div("vgap");
        htm.sDiv("center");
        htm.sH(1).add("Welcome to the Entry Level Mathematics Tutorial").eH(1);
        htm.eDiv();

        htm.div("vgap");
        htm.sDiv("center");
        htm.addln("<a class='btn' href='secure/shibboleth.html'>Log in using your eID</a><br>");
        htm.addln("<a href='https://eid.colostate.edu/' target='_blank'>Create your eID</a>");
        htm.eDiv();

        final TermRec active = cache.getSystemData().getActiveTerm();

        final List<RawCampusCalendar> calendarRows = RawCampusCalendarLogic.INSTANCE.queryAll(cache);

        LocalDate maint1Start = null;
        LocalDate maint1End = null;
        LocalDate maint2Start = null;
        LocalDate maint2End = null;
        LocalDate maint3Start = null;
        LocalDate maint3End = null;

        for (final RawCampusCalendar cal : calendarRows) {
            final String type = cal.dtDesc;

            if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_START1.equals(type)) {
                maint1Start = cal.campusDt;
            } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_END1.equals(type)) {
                maint1End = cal.campusDt;
            } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_START2.equals(type)) {
                maint2Start = cal.campusDt;
            } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_END2.equals(type)) {
                maint2End = cal.campusDt;
            } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_START3.equals(type)) {
                maint3Start = cal.campusDt;
            } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_END3.equals(type)) {
                maint3End = cal.campusDt;
            }
        }

        htm.div("vgap2");
        htm.sDiv("infobox", "id='bottext'");
        htm.sP("center").add("<strong>Scheduled System Maintenance</strong>").eP();
        htm.sP().add("This site is closed for maintenance between semesters.").eP();
        if (maint1Start != null && maint1End != null) {
            htm.sP().addln("Upcoming maintenance periods:").eP();
            htm.addln("<ul>");
            htm.addln("<li>", maint1Start.format(LOCAL_DATE_NO_YEAR),
                    " - ", maint1End.format(LOCAL_DATE_FORMATTER), "</li>");
            if (maint2Start != null && maint2End != null) {
                htm.addln("<li>", maint2Start.format(LOCAL_DATE_NO_YEAR),
                        " - ", maint2End.format(LOCAL_DATE_FORMATTER), "</li>");
            }
            if (maint3Start != null && maint3End != null) {
                htm.addln("<li>", maint3Start.format(LOCAL_DATE_NO_YEAR),
                        " - ", maint3End.format(LOCAL_DATE_FORMATTER), "</li>");
            }
            htm.addln("</ul>");
        }
        htm.sP().add("Log in for the exact dates you will be eligible to access the ELM Tutorial.").eP();

        if (active != null) {
            htm.hr();
            htm.sP("center").add("<strong>Daily Maintenance Window</strong>").eP();
            htm.sP().add("A maintenance window is reserved from 6:00am - 8:00am daily. If you use the ",
                    "system during this time, you do so at your own risk. The system may be taken ",
                    "offline without warning during this window to perform system maintenance.").eP();
        }
        htm.eDiv();
        htm.div("vgap2");

        Page.endOrdinaryPage(cache, site, null, htm, true);

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}
