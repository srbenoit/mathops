package dev.mathops.web.site.admin.genadmin.dbadmin;

import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rawlogic.RawStvisitLogic;
import dev.mathops.db.rawrecord.RawStvisit;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdmSubtopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A page that presents a specific report.
 */
public enum PageDbAdminReport {
    ;

    /**
     * Generates the database administration "Report" page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String t = req.getParameter("t");

        if (AbstractSite.isParamInvalid(t)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  t='", t, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = GenAdminPage.startGenAdminPage(site, session, true);
            htm.sH(2, "gray").add("Database Administration").eH(2);
            htm.hr("orange");

            PageDbAdmin.emitNavMenu(htm, EAdmSubtopic.DB_REPORTS);

            if ("stvisit".equals(t)) {
                doStudentVisitReport(cache, htm);
            } else {
                htm.addln("<div style='column-width:220px;line-height:1.5em;'>");
                htm.addln("Report not implemented.").br();
                htm.eDiv();
            }

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Generates the "student visits" report.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to write
     * @throws SQLException if there is an error accessing the database
     */
    private static void doStudentVisitReport(final Cache cache, final HtmlBuilder htm)
            throws SQLException {

        htm.hr();
        htm.sH(2).add("Student Visits").eH(2);

        final List<RawStvisit> all = RawStvisitLogic.INSTANCE.queryAll(cache);

        // Categorize by date, then by start time
        final Map<LocalDate, Map<LocalTime, List<RawStvisit>>> categorized = new HashMap<>(150);
        final List<LocalDate> dates = new ArrayList<>(150);

        for (final RawStvisit visit : all) {
            final LocalDateTime start = visit.whenStarted;
            final LocalDate startDate = start.toLocalDate();
            final LocalTime startTime = start.toLocalTime();

            Map<LocalTime, List<RawStvisit>> dateMap = categorized.get(startDate);
            if (dateMap == null) {
                dateMap = new TreeMap<>();
                categorized.put(startDate, dateMap);
                dates.add(startDate);
            }

            final List<RawStvisit> list = dateMap.computeIfAbsent(startTime, lt -> new ArrayList<>(2));

            list.add(visit);
        }

        Collections.sort(dates);
        Collections.reverse(dates);

        // Now, generate the report (most recent dates first)
        for (final LocalDate date : dates) {

            htm.div("vgap");
            htm.sH(3).add(TemporalUtils.FMT_MDY.format(date)).eH(3);
            htm.sDiv("indent");

            final Map<LocalTime, List<RawStvisit>> times = categorized.get(date);
            for (final Map.Entry<LocalTime, List<RawStvisit>> entry : times.entrySet()) {
                for (final RawStvisit visit : entry.getValue()) {
                    // Emit a row for the visit
                    htm.addln(visit).br();
                }
            }

            htm.eDiv();
        }
    }
}
