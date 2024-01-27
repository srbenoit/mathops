package dev.mathops.web.site.reporting;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.dbjobs.report.HtmlCsvCourseProgressReport;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Generates the page to select and generate Precalculus Course progress reports by lists of student ID.
 */
enum PagePrecalcStatusByStudents {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final ReportingSite site, final ServletRequest req,
                         final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String csv = req.getParameter("csv");

        if (csv == null || csv.isEmpty()) {
            emitHtmlPage(cache, site, req, resp, session);
        } else {
            emitCsvData(req, resp);
        }
    }

    /**
     * Generates the HTML page with the report.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitHtmlPage(final Cache cache, final ReportingSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String idlist = req.getParameter("idlist");

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        htm.sH(2).add(Res.get(Res.HOME_HEADING)).eH(2);
        htm.sDiv().add("<a href='home.html'>Home</a>").eDiv();
        htm.hr();

        htm.sH(3).add("Precalculus Course progress for specified students").eH(3);

        htm.add("<form action='precalc_by_students.html' method='POST'>");

        htm.sP().add("Paste a list of CSU ID numbers into the box below.  IDs can be separated by spaces, tabs, ",
                "commas, line-feeds, or any combination of these.").eP();

        htm.sP().add("<textarea name='idlist' id='idlist' rows='10' cols='30'>");
        if (idlist != null && !idlist.isBlank()) {
            htm.addln(idlist);
        }
        htm.add("</textarea>").eP();

        htm.sP().addln("<input type='submit' name='generate' value='View Report'/> ",
                "<input type='submit' name='csv' value='Download Report Data (CSV)'/>").eP();
        htm.addln("</form>");
        htm.hr();

        // Run the report if student IDs were provided
        if (idlist != null && !idlist.isBlank()) {
            final List<String> studentIds = ReportingSite.extractIds(idlist);

            final Collection<String> report = new ArrayList<>(10);
            final Collection<String> csv = new ArrayList<>(10);
            final HtmlCsvCourseProgressReport job = new HtmlCsvCourseProgressReport(studentIds, null,
                    "Status for a provided list of student IDs");
            job.generate(report, csv);

            for (final String rep : report) {
                htm.addln(rep);
            }
        }

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
    /**
     * Generates a CSV file with report data.
     *
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     */
    private static void emitCsvData(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final String idlist = req.getParameter("idlist");

        // Run the report if student IDs were provided
        if (idlist != null && !idlist.isBlank()) {
            final List<String> studentIds = ReportingSite.extractIds(idlist);

            final Collection<String> report = new ArrayList<>(10);
            final Collection<String> csv = new ArrayList<>(10);
            final HtmlCsvCourseProgressReport job = new HtmlCsvCourseProgressReport(studentIds, null,
                    "Status for a provided list of student IDs");
            job.generate(report, csv);

            final HtmlBuilder csvData = new HtmlBuilder(2000);
            for (final String row : csv) {
                csvData.addln(row);
            }

            resp.setHeader("Content-Disposition", "attachment;filename=placement_data.csv;");
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_CSV,
                    csvData.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
