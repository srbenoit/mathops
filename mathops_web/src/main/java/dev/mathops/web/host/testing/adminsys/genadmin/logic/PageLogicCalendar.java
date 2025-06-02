package dev.mathops.web.host.testing.adminsys.genadmin.logic;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.TermCalendarLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdmSubtopic;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdminTopic;
import dev.mathops.web.host.testing.adminsys.genadmin.GenAdminPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * A page to test logic related to semester and campus calendars.
 */
public enum PageLogicCalendar {
    ;

    /**
     * Generates the logic testing page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = startPage(cache, site, session);

        endPage(cache, site, req, resp, htm);
    }

    /**
     * Starts a page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param session the login session
     * @return an {@code HtmlBuilder} with the started page content
     * @throws SQLException if there is an error accessing the database
     */
    private static HtmlBuilder startPage(final Cache cache, final AdminSite site, final ImmutableSessionInfo session)
            throws SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.LOGIC_TESTING, htm);
        htm.sH(1).add("Logic Testing").eH(1);

        PageLogicTesting.emitNavMenu(htm, EAdmSubtopic.LOGIC_CALENDAR);
        htm.hr().div("vgap");

        htm.sP().add("This page tests logic contained in the <code>TermCalendarLogic</code> class in the ",
                "<code>dev.mathops.db.old.logic</code> package.").eP();
        htm.hr().div("vgap0");

        final LocalDate firstOpen = TermCalendarLogic.getFirstClassDate(cache);

        final String firstOpenStr = TemporalUtils.FMT_WMDY.format(firstOpen);
        htm.sP().add("First open date: ", firstOpenStr).eP();

        final LocalDate lastOpen = TermCalendarLogic.getLastClassDate(cache);
        final String lastOpenStr = TemporalUtils.FMT_WMDY.format(lastOpen);
        htm.sP().add("Last open date: ", lastOpenStr).eP();

        final List<LocalDate> dates = TermCalendarLogic.getOpenDates(cache);
        htm.sP("indent").sTable("report");
        htm.sTr().sTh().add("Date").eTh().sTh().add("+1").eTh().sTh().add("+2").eTh().eTr();
        for (final LocalDate date : dates) {
            final String dateStr = TemporalUtils.FMT_WMDY.format(date);

            final LocalDate plus1 = TermCalendarLogic.nextOpenDay(cache, date, 1);
            final String plus1Str = plus1 == null ? "null" : TemporalUtils.FMT_WMDY.format(plus1);

            final LocalDate plus2 = TermCalendarLogic.nextOpenDay(cache, date, 2);
            final String plus2Str = plus2 == null ? "null" : TemporalUtils.FMT_WMDY.format(plus2);

            htm.sTr().sTd().add(dateStr).eTd().sTd().add(plus1Str).eTd().sTd().add(plus2Str).eTd().eTr();
        }
        htm.eTable().eP();

        return htm;
    }

    /**
     * Ends a page.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @param htm   the {@code HtmlBuilder} with the page content to send
     * @throws SQLException if there is an error accessing the database
     */
    private static void endPage(final Cache cache, final AdminSite site, final ServletRequest req,
                                final HttpServletResponse resp, final HtmlBuilder htm)
            throws IOException, SQLException {

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
